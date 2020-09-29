package com.lionpig.webui.http.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class RedirectFilter implements Filter {
	private ServletContext context = null;

	@Override
	public void destroy() {}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1,
			FilterChain arg2) throws IOException, ServletException {
		if (context == null)
			return;
		
		String url = context.getInitParameter("URL");
		if (url == null)
			return;
		((HttpServletResponse)arg1).sendRedirect(url);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		context = arg0.getServletContext();
	}

}
