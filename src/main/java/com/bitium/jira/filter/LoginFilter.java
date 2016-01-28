package com.bitium.jira.filter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.sal.api.auth.LoginUriProvider;
import com.bitium.jira.config.SamlPluginSettings;

public class LoginFilter implements Filter {
	
	private SamlPluginSettings samlPluginSettings;
	private LoginUriProvider loginUriProvider;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
        boolean idpRequired = samlPluginSettings.isIdpRequired();
        HttpServletRequest req = (HttpServletRequest)request;
    	HttpServletResponse res = (HttpServletResponse)response;
    	
        if (idpRequired == true) {
        	try {
				res.sendRedirect(loginUriProvider.getLoginUri((new URI(req.getRequestURI().toString()))).toString() + "&samlerror=general");
			} catch (URISyntaxException e) {
			}        	
        } else {
        	chain.doFilter(request, response);
        }
	}

	@Override
	public void destroy() {
	}

	public void setSamlPluginSettings(SamlPluginSettings samlPluginSettings) {
		this.samlPluginSettings = this.samlPluginSettings;
	}

	public void setLoginUriProvider(LoginUriProvider loginUriProvider) {
		this.loginUriProvider = loginUriProvider;
	}

}
