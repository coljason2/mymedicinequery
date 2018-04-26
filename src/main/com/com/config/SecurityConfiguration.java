package com.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	@Autowired
	public void configureGlobalSecurity(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication().withUser("000000").password("000000").roles("USER");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/resources/**", "/login").permitAll()
				.antMatchers("/home", "/get/**", "/query/**").access("hasRole('USER')").and().httpBasic().and()
				.formLogin().usernameParameter("username").passwordParameter("password").loginPage("/login")
				.defaultSuccessUrl("/home").and().logout().logoutSuccessUrl("/login?logout").and().exceptionHandling()
				.accessDeniedPage("/Access_Denied");
//				.and().requiresChannel().antMatchers("/home", "/get/**", "/query/**").requiresSecure();
		http.portMapper()
				.http(80).mapsTo(443);
		http.portMapper() 
				.http(8080).mapsTo(443);
	}
}
