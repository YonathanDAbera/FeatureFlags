package com.yonathan.featureflags;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class SecurityConfig {
	private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.cors(Customizer.withDefaults())
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/actuator/health", "/api/v1/info").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/environments/*/flags").hasRole("ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/v1/environments/*/flags/*/targeting-rules").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/v1/environments/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PATCH, "/api/v1/environments/*/flags/*").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/v1/environments/**").hasAnyRole("ADMIN", "EVALUATOR")
						.anyRequest().authenticated()
				)
				.exceptionHandling(exceptions -> exceptions.accessDeniedHandler((request, response, exception) -> {
					var authentication = SecurityContextHolder.getContext().getAuthentication();
					log.warn("Denied {} {} for authorities {}", request.getMethod(), request.getRequestURI(), authentication == null ? "none" : authentication.getAuthorities());
					response.sendError(403, "Administrator role required for this action");
				}))
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:5173"));
		configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Actor", "X-Request-Id"));
		configuration.setExposedHeaders(List.of("X-Request-Id"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	private Converter<Jwt, Collection<GrantedAuthority>> realmRoleConverter() {
		return jwt -> {
			Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
			if (realmAccess == null || !(realmAccess.get("roles") instanceof Collection<?> roles)) {
				return List.of();
			}

			return roles.stream()
					.filter(String.class::isInstance)
					.map(String.class::cast)
					.map(role -> new SimpleGrantedAuthority("ROLE_" + role))
					.map(GrantedAuthority.class::cast)
					.toList();
		};
	}

	private JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(realmRoleConverter());
		return converter;
	}
}
