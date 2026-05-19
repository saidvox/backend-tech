package com.techstore.backend.product.application;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
import com.techstore.backend.common.exception.ApiException;
import com.techstore.backend.config.properties.GoogleDriveStorageProperties;
import com.techstore.backend.product.api.ProductImageUploadResponse;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductImageStorageService {
	private static final String DRIVE_SCOPE = "https://www.googleapis.com/auth/drive";
	private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/jpeg", "image/png", "image/webp", "image/gif");

	private final GoogleDriveStorageProperties properties;
	private final ObjectMapper objectMapper = JsonMapper.builder().build();

	public ProductImageStorageService(GoogleDriveStorageProperties properties) {
		this.properties = properties;
	}

	public ProductImageUploadResponse upload(MultipartFile file) {
		validate(file);

		try {
			String token = accessToken();
			DriveUploadResponse uploadResponse = uploadToDrive(file, token);
			if (uploadResponse == null || !StringUtils.hasText(uploadResponse.id())) {
				throw new ApiException(HttpStatus.BAD_GATEWAY, "Google Drive no devolvio el id del archivo");
			}
			makePublic(uploadResponse.id(), token);
			return new ProductImageUploadResponse(uploadResponse.id(), publicImageUrl(uploadResponse.id()));
		} catch (RestClientResponseException ex) {
			throw new ApiException(HttpStatus.BAD_GATEWAY, "Google Drive rechazo la imagen: " + ex.getResponseBodyAsString());
		} catch (IOException ex) {
			throw new ApiException(HttpStatus.BAD_GATEWAY, "No se pudo subir la imagen a Google Drive: " + ex.getMessage());
		} catch (IllegalArgumentException ex) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Configuracion invalida de Google Drive: " + ex.getMessage());
		} catch (RuntimeException ex) {
			throw new ApiException(HttpStatus.BAD_GATEWAY, "Error al procesar la subida a Google Drive: "
					+ ex.getClass().getSimpleName() + " - " + ex.getMessage());
		}
	}

	private void validate(MultipartFile file) {
		if (!properties.enabled()) {
			throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Google Drive no esta habilitado");
		}
		if (!StringUtils.hasText(properties.folderId())) {
			throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Google Drive no tiene carpeta configurada");
		}
		if (!properties.hasCredentials()) {
			throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Google Drive no tiene credenciales configuradas");
		}
		if (file == null || file.isEmpty()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Debes seleccionar una imagen");
		}
		if (properties.maxImageSizeBytes() > 0 && file.getSize() > properties.maxImageSizeBytes()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "La imagen supera el tamano maximo permitido");
		}
		if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Formato de imagen no permitido");
		}
	}

	private DriveUploadResponse uploadToDrive(MultipartFile file, String token) throws IOException {
		String filename = uniqueFilename(file);
		Map<String, Object> metadata = Map.of(
				"name", filename,
				"parents", List.of(properties.folderId()));

		HttpHeaders metadataHeaders = new HttpHeaders();
		metadataHeaders.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> metadataPart = new HttpEntity<>(objectMapper.writeValueAsString(metadata), metadataHeaders);

		HttpHeaders mediaHeaders = new HttpHeaders();
		mediaHeaders.setContentType(MediaType.parseMediaType(file.getContentType()));
		HttpEntity<ByteArrayResource> mediaPart = new HttpEntity<>(new NamedByteArrayResource(file.getBytes(), filename), mediaHeaders);

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("metadata", metadataPart);
		body.add("media", mediaPart);

		return driveClient(token).post()
				.uri("/upload/drive/v3/files?uploadType=multipart&fields=id")
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.body(body)
				.retrieve()
				.body(DriveUploadResponse.class);
	}

	private void makePublic(String fileId, String token) {
		driveClient(token).post()
				.uri("/drive/v3/files/{fileId}/permissions", fileId)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("role", "reader", "type", "anyone"))
				.retrieve()
				.toBodilessEntity();
	}

	private RestClient driveClient(String token) {
		return RestClient.builder()
				.baseUrl("https://www.googleapis.com")
				.defaultHeaders(headers -> {
					headers.setBearerAuth(token);
					headers.setAccept(List.of(MediaType.APPLICATION_JSON));
				})
				.build();
	}

	private String accessToken() throws IOException {
		if (properties.hasOAuthCredentials()) {
			GoogleCredentials credentials = UserCredentials.newBuilder()
					.setClientId(properties.clientId())
					.setClientSecret(properties.clientSecret())
					.setRefreshToken(properties.refreshToken())
					.build()
					.createScoped(List.of(DRIVE_SCOPE));
			credentials.refreshIfExpired();
			return credentials.getAccessToken().getTokenValue();
		}

		try (InputStream inputStream = credentialsStream()) {
			GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream).createScoped(List.of(DRIVE_SCOPE));
			credentials.refreshIfExpired();
			return credentials.getAccessToken().getTokenValue();
		}
	}

	private InputStream credentialsStream() throws IOException {
		if (StringUtils.hasText(properties.credentialsBase64())) {
			String normalized = properties.credentialsBase64().replaceAll("\\s", "");
			return new ByteArrayInputStream(Base64.getDecoder().decode(normalized));
		}
		Path credentialsPath = Path.of(properties.credentialsPath()).normalize();
		if (!Files.exists(credentialsPath)) {
			throw new IOException("No existe el archivo de credenciales en " + credentialsPath.toAbsolutePath());
		}
		return Files.newInputStream(credentialsPath);
	}

	private String uniqueFilename(MultipartFile file) {
		String original = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "product-image";
		String sanitized = original.replaceAll("[^A-Za-z0-9._-]", "-");
		String extension = extensionFor(file.getContentType(), sanitized);
		String baseName = sanitized;
		int dotIndex = sanitized.lastIndexOf('.');
		if (dotIndex > 0) {
			baseName = sanitized.substring(0, dotIndex);
		}
		return UUID.randomUUID() + "-" + baseName + extension;
	}

	private String extensionFor(String contentType, String filename) {
		String existingExtension = "";
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
			existingExtension = filename.substring(dotIndex).toLowerCase();
		}
		if (Arrays.asList(".jpg", ".jpeg", ".png", ".webp", ".gif").contains(existingExtension)) {
			return existingExtension;
		}
		return switch (contentType) {
			case "image/jpeg" -> ".jpg";
			case "image/png" -> ".png";
			case "image/webp" -> ".webp";
			case "image/gif" -> ".gif";
			default -> "";
		};
	}

	private String publicImageUrl(String fileId) {
		return "https://drive.google.com/thumbnail?id=" + fileId + "&sz=w1000";
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record DriveUploadResponse(String id) {
	}

	private static final class NamedByteArrayResource extends ByteArrayResource {
		private final String filename;

		private NamedByteArrayResource(byte[] byteArray, String filename) {
			super(byteArray);
			this.filename = filename;
		}

		@Override
		public String getFilename() {
			return filename;
		}
	}
}
