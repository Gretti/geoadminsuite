package fr.sie.brique.web.encoding;

import java.io.IOException;

import javax.servlet.*;

/**
 * Filte qui force l'encodage de la requête à UTF-8.
 * 
 * @author mauclerc
 * @version $Id: EncodingFilter.java,v 1.2 2009/06/24 16:22:25 mauclerc Exp $
 */
public class EncodingFilter implements Filter {

	/**
	 * Force l'encodage de la requête à UTF-8.
	 *  
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		request.setCharacterEncoding("UTF-8");
		chain.doFilter(request, response);
	}

	/** 
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
	}

	/** 
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
	}
}

