package com.techstore.backend.config.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import com.techstore.backend.config.properties.JwtProperties;
import com.techstore.backend.user.domain.AppUser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Service;

@Service
public class JwtService {
	private final SecretKey key;
	private final JwtProperties jwtProperties;

	public JwtService(JwtProperties jwtProperties) {
		if (jwtProperties.secret() == null || jwtProperties.secret().getBytes(StandardCharsets.UTF_8).length < 32) {
			throw new IllegalStateException("JWT secret must be at least 256 bits");
		}
		this.jwtProperties = jwtProperties;
		this.key = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(AppUser user) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(user.getEmail())
				.issuer(jwtProperties.issuer())
				.claim("userId", user.getId())
				.claim("role", user.getRole().name())
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusSeconds(jwtProperties.expirationMinutes() * 60)))
				.signWith(key)
				.compact();
	}

	public String extractEmail(String token) {
		return claims(token).getSubject();
	}

	public boolean isValid(String token) {
		return claims(token).getExpiration().after(new Date());
	}

	private Claims claims(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.requireIssuer(jwtProperties.issuer())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
