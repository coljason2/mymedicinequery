package com.config;

import javax.servlet.ServletContext;
import javax.servlet.FilterRegistration.Dynamic;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
import org.springframework.web.filter.CharacterEncodingFilter;

public class SecurityWebApplicationInitializer extends AbstractSecurityWebApplicationInitializer {
	/**
	 * 必須在security 之前配置编码
	 */
	@Override
	protected void beforeSpringSecurityFilterChain(ServletContext servletContext) {
		// encoding
		Dynamic characterEncodingFilter = servletContext.addFilter("encodingFilter", new CharacterEncodingFilter());
		characterEncodingFilter.setInitParameter("encoding", "UTF-8");
		characterEncodingFilter.setInitParameter("forceEncoding", "true");
		characterEncodingFilter.addMappingForUrlPatterns(null, false, "/*");

	}
}
