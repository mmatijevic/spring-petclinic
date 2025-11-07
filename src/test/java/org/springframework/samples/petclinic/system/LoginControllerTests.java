package org.springframework.samples.petclinic.system;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
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
		mockMvc.perform(formLogin("/login").user("petclinic").password("petclinic"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/"))
			.andExpect(authenticated().withUsername("petclinic"));
	}

	@Test
	void shouldRejectInvalidCredentials() throws Exception {
		mockMvc.perform(formLogin("/login").user("petclinic").password("wrong"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login?error"))
			.andExpect(unauthenticated());
	}

	@Test
	void shouldRejectNonExistentUser() throws Exception {
		mockMvc.perform(formLogin("/login").user("nonexistent").password("password"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login?error"))
			.andExpect(unauthenticated());
	}

	@Test
	void shouldRejectEmptyUsername() throws Exception {
		mockMvc.perform(formLogin("/login").user("").password("petclinic"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login?error"))
			.andExpect(unauthenticated());
	}

	@Test
	void shouldRejectEmptyPassword() throws Exception {
		mockMvc.perform(formLogin("/login").user("petclinic").password(""))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login?error"))
			.andExpect(unauthenticated());
	}

	@Test
	@WithMockUser
	void shouldLogoutSuccessfully() throws Exception {
		mockMvc.perform(logout()).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
	}

	@Test
	@WithMockUser(username = "petclinic", roles = "USER")
	void shouldAccessWelcomePageWhenAuthenticated() throws Exception {
		mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("welcome"));
	}

	@Test
	void shouldAccessWelcomePageWhenNotAuthenticated() throws Exception {
		// All pages are permitAll according to SecurityConfig
		mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("welcome"));
	}

}
