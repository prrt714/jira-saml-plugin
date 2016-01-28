package com.bitium.jira.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bitium.jira.config.SamlPluginSettings;


/**
 * For now it does only one function: returns idpRequired Field
 * 
 * This might probably get expanded to return json with other fields as well
 *
 */
public class ConfigAjaxServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private SamlPluginSettings samlPluginSettings;
	
	public void setSamlPluginSettings(SamlPluginSettings samlPluginSettings) {
		this.samlPluginSettings = samlPluginSettings;
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String parameter = request.getParameter("param");
		if (parameter != null) {
			if (parameter.equals("idpRequired")) {
				response.getWriter().write(String.valueOf(samlPluginSettings.isIdpRequired()));
			} else if (parameter.equals("logoutUrl")) {
				response.getWriter().write(samlPluginSettings.getFirst().getLogoutUrl());
			}
		} 
		
	}
	    
}
