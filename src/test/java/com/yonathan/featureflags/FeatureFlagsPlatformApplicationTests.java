package com.yonathan.featureflags;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = ApiInfoController.class, excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
class FeatureFlagsPlatformApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void infoEndpointReturnsServiceStatus() throws Exception {
		mockMvc.perform(get("/api/v1/info"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.service").value("feature-flag-trials"))
				.andExpect(jsonPath("$.status").value("ok"));
	}

}
