package com.techstore.backend.config.security;

import java.util.Arrays;

import jakarta.servlet.http.HttpServletResponse;

import com.techstore.backend.config.properties.CorsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CorsProperties corsProperties;
	private final OAuth2AuthenticationSuccessHandler oauth2SuccessHandler;
	private final OAuth2AuthenticationFailureHandler oauth2FailureHandler;
	private final OAuth2ModeCaptureFilter oauth2ModeCaptureFilter;

	public SecurityConfig(
			JwtAuthenticationFilter jwtAuthenticationFilter,
			CorsProperties corsProperties,
			OAuth2AuthenticationSuccessHandler oauth2SuccessHandler,
			OAuth2AuthenticationFailureHandler oauth2FailureHandler,
			OAuth2ModeCaptureFilter oauth2ModeCaptureFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.corsProperties = corsProperties;
		this.oauth2SuccessHandler = oauth2SuccessHandler;
		this.oauth2FailureHandler = oauth2FailureHandler;
		this.oauth2ModeCaptureFilter = oauth2ModeCaptureFilter;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
				.cors(cors -> { })
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/health").permitAll()
						.requestMatchers("/h2-console/**").permitAll()
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
						.requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
						.requestMatchers("/pagos/webhooks/**").permitAll()
						.requestMatchers("/auth/**", "/productos/**", "/categorias/**").permitAll()
						.anyRequest().authenticated())
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint((request, response, authException) ->
								response.sendError(HttpServletResponse.SC_FORBIDDEN)))
				.oauth2Login(oauth2 -> oauth2
						.successHandler(oauth2SuccessHandler)
						.failureHandler(oauth2FailureHandler))
				.addFilterBefore(oauth2ModeCaptureFilter, OAuth2AuthorizationRequestRedirectFilter.class)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(corsProperties.allowedOrigins());
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
