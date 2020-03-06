package com.medicine.query.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(WebSecurity web) {
//        web.ignoring().antMatchers("/webjars/**");
        web.ignoring().antMatchers("/v2/api-docs/**");
        web.ignoring().antMatchers("/swagger.json");
        web.ignoring().antMatchers("/swagger-ui.html");
        web.ignoring().antMatchers("/swagger-resources/**");
        web.ignoring().antMatchers("/resources/static/**")
                .and().ignoring().antMatchers(
                HttpMethod.GET,
                "/*.html",
                "/favicon.ico",
                "/**/*.html",
                "/**/*.css",
                "/**/*.js"
        );
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        auth.inMemoryAuthentication()
                .withUser("user").password("{noop}user123456").roles("USER")
                .and()
                .withUser("root").password("{noop}root").roles("USER")
                .and()
                .withUser("admin").password("{noop}admin123456").roles("USER", "ADMIN");

    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.cors()
                .and()
                .csrf().disable();

        http
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and().formLogin()
                .and().httpBasic()
                .and()
                .logout()
                .logoutSuccessUrl("/login?logout")
                .logoutSuccessHandler((req, res, auth) -> {   // Logout handler called after successful logout
                    req.getSession().setAttribute("message", "您已經成功登出!");
                    res.sendRedirect(req.getContextPath() + "/login"); // Redirect user to login page with message.
                });
    }
}
