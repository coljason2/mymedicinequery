package com.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForceHttpToHttpsFilter implements Filter {
	private static Logger Logger = LoggerFactory.getLogger(ForceHttpToHttpsFilter.class);
	public static final String X_FORWARDED_PROTO = "x-forwarded-proto";
	private FilterConfig filterConfig;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpReq = (HttpServletRequest) request;
		HttpServletResponse httpResp = (HttpServletResponse) response;
		if ("http".equalsIgnoreCase(httpReq.getHeader("x-forwarded-proto"))) {
			StringBuffer requestURL = httpReq.getRequestURL();
			httpResp.sendRedirect("https" + requestURL.substring(requestURL.indexOf(":")));
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
		// nothing
	}
}
