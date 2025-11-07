package org.springframework.samples.petclinic.system;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class LoginControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void shouldDisplayLoginPage() throws Exception {
		mockMvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("login"));
	}

	@Test
	void shouldAuthenticateWithValidCredentials() throws Exception {
		mockMvc.perform(post("/login").param("username", "petclinic").param("password", "petclinic").with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/"));
	}

	@Test
	void shouldRejectInvalidCredentials() throws Exception {
		mockMvc.perform(post("/login").param("username", "petclinic").param("password", "wrong").with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login?error"));
	}

}
