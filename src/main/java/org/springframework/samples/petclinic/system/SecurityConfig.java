package org.springframework.samples.petclinic.system;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@SuppressWarnings("unused")
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(
					authorize -> authorize.requestMatchers(PathRequest.toStaticResources().atCommonLocations())
						.permitAll()
						.anyRequest()
						.permitAll())
			.formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
			.logout(logout -> logout.logoutSuccessUrl("/").permitAll());
		return http.build();
	}

	@Bean
	UserDetailsService users(PasswordEncoder passwordEncoder) {
		UserDetails user = User.builder()
			.username("petclinic")
			.password(passwordEncoder.encode("petclinic"))
			.roles("USER")
			.build();
		return new InMemoryUserDetailsManager(user);
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

}
