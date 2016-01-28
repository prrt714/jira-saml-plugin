package com.bitium.jira.servlet;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.DelegatingApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.seraph.auth.Authenticator;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.config.SecurityConfigFactory;
import com.bitium.jira.config.SAMLJiraConfig;
import com.bitium.jira.config.SamlPluginSettings;
import com.bitium.saml.SAMLContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.util.SAMLUtil;
import org.springframework.security.saml.websso.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;

public class SsoLoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(SsoLoginServlet.class);

	private final SamlPluginSettings samlPluginSettings;

    public SsoLoginServlet(SamlPluginSettings samlPluginSettings) {
        this.samlPluginSettings = samlPluginSettings;
    }

	@Override
	public void init() throws ServletException {
		super.init();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
        SAMLJiraConfig samlJiraConfig = null;
        String entityId = request.getParameter("entityId");
        if (entityId != null) {
            samlJiraConfig = samlPluginSettings.get(entityId);
        } //else {
            //samlJiraConfig = samlPluginSettings.getFirst();
        //}
        if (samlJiraConfig == null) {
            response.sendRedirect("/jira/login.jsp?samlerror=unknown_idp");
            return;
        }
		try {
			SAMLContext context = new SAMLContext(request, samlJiraConfig);
			SAMLMessageContext messageContext = context.createSamlMessageContext(request, response);

			// Generate options for the current SSO request
	        WebSSOProfileOptions options = new WebSSOProfileOptions();
	        options.setBinding(org.opensaml.common.xml.SAMLConstants.SAML2_REDIRECT_BINDING_URI);
                options.setIncludeScoping(false);

			// Send request
	        WebSSOProfile webSSOprofile = new WebSSOProfileImpl(context.getSamlProcessor(), context.getMetadataManager());
	        webSSOprofile.sendAuthenticationRequest(messageContext, options);
		} catch (Exception e) {
		    log.error("saml plugin error + " + e.getMessage());
			response.sendRedirect("/jira/login.jsp?samlerror=general");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        String entityId = SAMLContext.getIssuer(request);
        SAMLJiraConfig samlJiraConfig = null;
        if (entityId != null) {
            samlJiraConfig = samlPluginSettings.get(entityId);
        }
        if (samlJiraConfig == null) {
            try {
                response.sendRedirect("/jira/login.jsp?samlerror=general");
            } catch (IOException e) {
                throw new ServletException(e.getMessage(), e);
            }
            return;
        }
        try {
			SAMLContext context = new SAMLContext(request, samlJiraConfig);
			SAMLMessageContext messageContext = context.createSamlMessageContext(request, response);

			// Process response
	        context.getSamlProcessor().retrieveMessage(messageContext);

	        messageContext.setLocalEntityEndpoint(SAMLUtil.getEndpoint(messageContext.getLocalEntityRoleMetadata().getEndpoints(), messageContext.getInboundSAMLBinding(), request.getRequestURL().toString()));
	        messageContext.getPeerEntityMetadata().setEntityID(samlJiraConfig.getIdpEntityId());

	        WebSSOProfileConsumer consumer = new WebSSOProfileConsumerImpl(context.getSamlProcessor(), context.getMetadataManager());
            SAMLCredential credential = consumer.processAuthenticationResponse(messageContext);

	        request.getSession().setAttribute("SAMLCredential", credential);


			String[] userNames;
			String uidAttribute = samlJiraConfig.getUidAttribute();
			if (uidAttribute.equals("NameID")) {
				userNames = new String[1];
				userNames[0] = credential.getNameID().getValue();
			}
			else {
				userNames = credential.getAttributeAsStringArray(uidAttribute);
				// null is returned when attribute is not present - handle that
				if (userNames == null){
					userNames = new String[0];
				}
			}

			authenticateUserAndLogin(request, response, userNames, samlJiraConfig, credential);
		} catch (AuthenticationException e) {
			try {
			    log.error("saml plugin error + " + e.getMessage());
				response.sendRedirect("/jira/login.jsp?samlerror=plugin_exception");
			} catch (IOException e1) {
				throw new ServletException();
			}
		} catch (Exception e) {
			try {
			    log.error("saml plugin error + " + e.getMessage());
				response.sendRedirect("/jira/login.jsp?samlerror=plugin_exception");
			} catch (IOException e1) {
				throw new ServletException();
			}
		}
	}

	private void authenticateUserAndLogin(HttpServletRequest request,
			HttpServletResponse response, String[] usernames, SAMLJiraConfig samlJiraConfig, SAMLCredential credential)
			throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, IOException, PermissionException, CreateException {
		Authenticator authenticator = SecurityConfigFactory.getInstance().getAuthenticator();

		if (authenticator instanceof DefaultAuthenticator) {
			//DefaultAuthenticator defaultAuthenticator = (DefaultAuthenticator)authenticator;

		    Method getUserMethod = DefaultAuthenticator.class.getDeclaredMethod("getUser", new Class[]{String.class});
		    getUserMethod.setAccessible(true);
		    Object userObject = null;

			for (String username : usernames){
				userObject = getUserMethod.invoke(authenticator, new Object[]{username});
				// stop at first match
				if (userObject != null) {
					break;
				}
			}
			// if not found, see if we're allowed to auto-create the user
			if (userObject == null && usernames.length > 0) {
				userObject = tryCreateOrUpdateUser(usernames[0], samlJiraConfig, credential);
			}
		    if(userObject != null && userObject instanceof DelegatingApplicationUser) {
		    	Principal principal = (Principal)userObject;

		    	Method authUserMethod = DefaultAuthenticator.class.getDeclaredMethod("authoriseUserAndEstablishSession",
		    			new Class[]{HttpServletRequest.class, HttpServletResponse.class, Principal.class});
		    	authUserMethod.setAccessible(true);
		    	Boolean result = (Boolean)authUserMethod.invoke(authenticator, new Object[]{request, response, principal});

		        if (result) {
		        	response.sendRedirect("/jira/secure/Dashboard.jspa");
		        	return;
		        } else {
                    response.sendRedirect("/jira/login.jsp?samlerror=auth_error");
                    return;
                }
		    }
		}

		response.sendRedirect("/jira/login.jsp?samlerror=user_not_found");
	}

	private Object tryCreateOrUpdateUser(String userName, SAMLJiraConfig samlJiraConfig, SAMLCredential credential) throws PermissionException, CreateException{
		if (samlJiraConfig.isAutoCreateUser()){
			UserUtil uu = ComponentAccessor.getUserUtil();

            //use format e.g. '%s %s:sn, givenname'
			String[] nameAttribute = samlJiraConfig.getNameAttribute().split(":");
            String pattern = "";
            if (nameAttribute.length == 1) {
                pattern = "%s %s";
                nameAttribute = nameAttribute[0].split(",");
            } else if (nameAttribute.length > 1) {
                pattern = nameAttribute[0];
                nameAttribute = nameAttribute[1].split(",");
            }

            String[] nameParts = new String[nameAttribute.length];
            for (int i=0; i< nameAttribute.length;i++) {
                nameParts[i] = credential.getAttributeAsString(nameAttribute[i]);
            }

            String fullName = String.format(pattern, nameParts).trim();

            String mailAttribute = samlJiraConfig.getMailAttribute();
            String email = credential.getAttributeAsString(mailAttribute);
			log.info("Creating user account for " + userName);
			ApplicationUser u = uu.createUserNoNotification(userName, null, email, fullName);

            if (samlJiraConfig.isRemoveFromGroups()) {
                for (Group g : uu.getGroupsForUser(userName)) {
                    try {
                        uu.removeUserFromGroup(g, u);
                    } catch (RemoveException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }

			return u;//uu.getUserByName(userName);// above returns api.User but we need ApplicationUser so search for it

        } else {
			// not allowed to auto-create user
			log.debug("User not found and auto-create disabled: " + userName);
		}
		return null;
	}
}
