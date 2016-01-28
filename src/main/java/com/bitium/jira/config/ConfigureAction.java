package com.bitium.jira.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.bitium.saml.X509Utils;

public class ConfigureAction extends JiraWebActionSupport {
	private static final long serialVersionUID = 1L;

	private String id;
	private String loginUrl;
	private String logoutUrl;
	private String entityId;
	private String uidAttribute;
	private String nameAttribute;
	private String mailAttribute;
	private String autoCreateUser;
	private String removeFromGroups;
	private String x509Certificate;
	private String idpRequired;
	private String success = "";
	private String submitAction;
    private String lastEntityId;

    public String getLastEntityId() {
        return lastEntityId;
    }

    public void setLastEntityId(String lastEntityId) {
        this.lastEntityId = lastEntityId;
    }

    private final SamlPluginSettings samlPluginSettings;

	public ConfigureAction(SamlPluginSettings samlPluginSettings) {
        this.samlPluginSettings = samlPluginSettings;
    }

	public String getX509Certificate() {
		return x509Certificate;
	}

	public void setX509Certificate(String x509Certificate) {
		this.x509Certificate = x509Certificate;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getUidAttribute() {
		return uidAttribute;
	}

	public void setUidAttribute(String uidAttribute) {
		this.uidAttribute = uidAttribute;
	}

	public String getNameAttribute() {
		return nameAttribute;
	}

	public void setNameAttribute(String nameAttribute) {
		this.nameAttribute = nameAttribute;
	}

	public String getMailAttribute() {
		return mailAttribute;
	}

	public void setMailAttribute(String mailAttribute) {
		this.mailAttribute = mailAttribute;
	}


	public String getLogoutUrl() {
		return logoutUrl;
	}

	public void setLogoutUrl(String logoutUrl) {
		this.logoutUrl = logoutUrl;
	}

	public String getLoginUrl() {
		return loginUrl;
	}

	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}

    public String getAutoCreateUser() {
        return autoCreateUser;
    }

    public void setAutoCreateUser(String autoCreateUser) {
        this.autoCreateUser = autoCreateUser;
    }

    public String getRemoveFromGroups() {
        return removeFromGroups;
    }

    public void setRemoveFromGroups(String removeFromGroups) {
        this.removeFromGroups = removeFromGroups;
    }

    public String getIdpRequired() {
        return idpRequired;
    }

    public void setIdpRequired(String idpRequired) {
        this.idpRequired = idpRequired;
    }

    public String getSuccess() {
		return success;
	}

	public void setSuccess(String success) {
		this.success = success;
	}

	public String getSubmitAction() {
		return submitAction;
	}

	public void setSubmitAction(String submitAction) {
		this.submitAction = submitAction;
	}

    public List<NameValuePair> getIdps() {
        return samlPluginSettings.getIdps();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void doValidation() {
		setSuccess("");
		if (getSubmitAction() == null || getSubmitAction().equals("")) {
			return;
		}
		if (!isSystemAdministrator()) {
			addErrorMessage(getText("saml2Plugin.admin.notAdministrator"));
		}
		if (StringUtils.isBlank(getLoginUrl())) {
			addErrorMessage(getText("saml2Plugin.admin.loginUrlEmpty"));
		} else {
			try {
				new URL(getLoginUrl());
			} catch (MalformedURLException e) {
				addErrorMessage(getText("saml2Plugin.admin.loginUrlInvalid"));
			}
		}
		if (StringUtils.isBlank(getLogoutUrl())) {
			//addActionError(getText("saml2Plugin.admin.logoutUrlEmpty"));
		} else {
			try {
				new URL(getLogoutUrl());
			} catch (MalformedURLException e) {
				addErrorMessage(getText("saml2Plugin.admin.logoutUrlInvalid"));
			}
		}
		if (StringUtils.isBlank(getEntityId())) {
			addErrorMessage(getText("saml2Plugin.admin.entityIdEmpty"));
		}
		if (StringUtils.isBlank(getUidAttribute())) {
			addErrorMessage(getText("saml2Plugin.admin.uidAttributeEmpty"));
		}
		if (StringUtils.isBlank(getNameAttribute())) {
			addErrorMessage(getText("saml2Plugin.admin.nameAttributeEmpty"));
		}
		if (StringUtils.isBlank(getMailAttribute())) {
			addErrorMessage(getText("saml2Plugin.admin.mailAttributeEmpty"));
		}
		if (StringUtils.isBlank(getX509Certificate())) {
			addErrorMessage(getText("saml2Plugin.admin.x509CertificateEmpty"));
		} else {
			try {
				X509Utils.generateX509Certificate(getX509Certificate());
			} catch (Exception e) {
				addErrorMessage(getText("saml2Plugin.admin.x509CertificateInvalid"));
			}
		}

        if (StringUtils.isBlank(getIdpRequired())) {
            setIdpRequired("false");
        } else {
            setIdpRequired("true");
        }
        if (StringUtils.isBlank(getAutoCreateUser())) {
            setAutoCreateUser("false");
        } else {
            setAutoCreateUser("true");
        }

        if (StringUtils.isBlank(getRemoveFromGroups())) {
            setRemoveFromGroups("false");
        } else {
            setRemoveFromGroups("true");
        }
   	}



	public String doExecute() throws Exception {
        SAMLJiraConfig saml2Config = null;

		if (getSubmitAction() == null || getSubmitAction().equals("")) {
            String entityId = StringUtils.defaultString(getHttpRequest().getParameter("entityId"));
            if (!entityId.isEmpty()) {
                saml2Config = samlPluginSettings.get(entityId);
            } else if (lastEntityId != null) {
                saml2Config = samlPluginSettings.get(lastEntityId);
            }
            if (saml2Config == null) {
                saml2Config = samlPluginSettings.getFirst();
            }

            setId(saml2Config.getId());
			setLoginUrl(saml2Config.getLoginUrl());
			setLogoutUrl(saml2Config.getLogoutUrl());
			setEntityId(saml2Config.getIdpEntityId());
			setUidAttribute(saml2Config.getUidAttribute());
			setNameAttribute(saml2Config.getNameAttribute());
			setMailAttribute(saml2Config.getMailAttribute());
			setX509Certificate(saml2Config.getX509Certificate());
            setIdpRequired(String.valueOf(samlPluginSettings.isIdpRequired()));
            setAutoCreateUser(String.valueOf(saml2Config.isAutoCreateUser()));
            setRemoveFromGroups(String.valueOf(saml2Config.isRemoveFromGroups()));
			return "success";
		}

        if (getId() == null || getId().isEmpty() || getId().equals(SamlPluginSettings.NEW_ID_EMPTY)) {
            setId("id" + System.currentTimeMillis());
        }
        saml2Config = new SAMLJiraConfig();
		saml2Config.setId(getId());
		saml2Config.setLoginUrl(getLoginUrl());
		saml2Config.setLogoutUrl(getLogoutUrl());
		saml2Config.setEntityId(getEntityId());
		saml2Config.setUidAttribute(getUidAttribute());
		saml2Config.setNameAttribute(getNameAttribute());
		saml2Config.setMailAttribute(getMailAttribute());
		saml2Config.setX509Certificate(getX509Certificate());
		saml2Config.setIdpRequired(Boolean.valueOf(getIdpRequired()));
		saml2Config.setAutoCreateUser(Boolean.valueOf(getAutoCreateUser()));
		saml2Config.setRemoveFromGroups(Boolean.valueOf(getRemoveFromGroups()));
        samlPluginSettings.save(saml2Config);
		setSuccess("success");
		return "success";
	}

}
