package com.vvs.spendwise_api.config;

import com.vvs.spendwise_api.security.CustomUserDetailsService;
import com.vvs.spendwise_api.security.JwtAuthFilter;
import com.vvs.spendwise_api.security.JwtService;
import com.vvs.spendwise_api.security.SecurityConfig;
import com.vvs.spendwise_api.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
@Import({SecurityConfig.class, CustomUserDetailsService.class, JwtAuthFilter.class, JwtService.class})
class HealthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserRepository userRepository;

	@Test
	void healthReturnsUp() throws Exception {
		mockMvc.perform(get("/api/v1/health"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("application/json"))
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.service").value("SpendWise API"));
	}
}
