package com.bitium.jira.config;

import com.bitium.saml.SAMLConfig;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;

public class SAMLJiraConfig implements Serializable, SAMLConfig {

    private static final long serialVersionUID = 6354598008471163539L;

    private String defaultBaseURL;

    private String id;
    private String loginUrl;
    private String logoutUrl;
    private String entityId;
    private String uidAttribute = "NameID";
    private String nameAttribute = "cn";
    private String mailAttribute = "mail";
    private String x509Certificate;
    private boolean idpRequired = false;
    private boolean autoCreateUser = true;
    private boolean removeFromGroups = false;

    public SAMLJiraConfig() {
    }

    public SAMLJiraConfig(String id) {
        this.id = id;
    }

    @Override
    @JsonIgnore
    public String getAlias() {
        return SamlPluginSettings.getBaseAlias();
    }

    @Override
    @JsonIgnore
    public String getBaseUrl() {
        return StringUtils.defaultString(defaultBaseURL);
    }

    @Override
    @JsonIgnore
    public String getSpEntityId() {
        return defaultBaseURL + "/" + getAlias();
    }

    @Override
    @JsonIgnore
    public String getIdpEntityId() {
        return entityId;
    }

    @Override
    public void setDefaultBaseUrl(String defaultBaseURL) {
        this.defaultBaseURL = defaultBaseURL;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
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

    public String getX509Certificate() {
        return x509Certificate;
    }

    public void setX509Certificate(String x509Certificate) {
        this.x509Certificate = x509Certificate;
    }

    public boolean isIdpRequired() {
        return idpRequired;
    }

    public void setIdpRequired(boolean idpRequired) {
        this.idpRequired = idpRequired;
    }

    public boolean isAutoCreateUser() {
        return autoCreateUser;
    }

    public void setAutoCreateUser(boolean autoCreateUser) {
        this.autoCreateUser = autoCreateUser;
    }

    public boolean isRemoveFromGroups() {
        return removeFromGroups;
    }

    public void setRemoveFromGroups(boolean removeFromGroups) {
        this.removeFromGroups = removeFromGroups;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
