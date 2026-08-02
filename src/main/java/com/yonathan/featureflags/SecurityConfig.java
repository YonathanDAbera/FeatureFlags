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
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/actuator/health", "/api/v1/info").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/environments/*/flags").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PATCH, "/api/v1/environments/*/flags/*").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/v1/environments/**").hasAnyRole("ADMIN", "EVALUATOR")
						.anyRequest().authenticated()
				)
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
		return http.build();
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
