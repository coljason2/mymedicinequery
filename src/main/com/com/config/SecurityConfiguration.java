package com.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	@Autowired
	public void configureGlobalSecurity(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication().withUser("000000").password("000000").roles("USER");
		auth.inMemoryAuthentication().withUser("root").password("root").roles("USER");
	}
	
	
	@Override
	public void configure(WebSecurity web) {
	    web.ignoring().antMatchers("/resources/**", "/layout/**");
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.authorizeRequests().anyRequest().hasAnyRole("USER")
		 		.and()
		 		.authorizeRequests().antMatchers("/login**").permitAll()
		 		.and()
				.formLogin()
				.loginPage("/login")
				.loginProcessingUrl("/signin")
				.usernameParameter("username")
				.passwordParameter("password")
				.defaultSuccessUrl("/home")
				.failureHandler((req,res,exp)->{  // Failure handler invoked after authentication failure
			         String errMsg="";
			         if(exp.getClass().isAssignableFrom(BadCredentialsException.class)){
			            errMsg="帳號/密碼錯誤！";
			         }else{
			            errMsg="不詳錯誤 - "+exp.getMessage();
			         }
			         req.getSession().setAttribute("message", errMsg);
			         res.sendRedirect(req.getContextPath()+"/login"); // Redirect user to login page with error message.
			      })
				.permitAll()
				.and()
				.logout()
				.logoutSuccessUrl("/login?logout")
				.logoutSuccessHandler((req,res,auth)->{   // Logout handler called after successful logout 
			         req.getSession().setAttribute("message", "您已經成功登出!");
			         res.sendRedirect(req.getContextPath()+"/login"); // Redirect user to login page with message.
			      })
				.permitAll()
				.and()
				.exceptionHandling()
				.accessDeniedPage("/Access_Denied")
				.and()
				.csrf().disable();;
	}
	
}
