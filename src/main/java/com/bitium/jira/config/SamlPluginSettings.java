package com.bitium.jira.config;

import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SamlPluginSettings {
    private static final Log log = LogFactory.getLog(SamlPluginSettings.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static final String NEW_ID_EMPTY = "newIdP";
    private Map<String, SAMLJiraConfig> settings = null;
    private List<NameValuePair> idps = null;

    private final PluginSettings pluginSettings;
    private final I18nResolver i18n;

    public SamlPluginSettings(I18nResolver i18n, PluginSettingsFactory pluginSettingsFactory) {
        this.i18n = i18n;
        this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
        update();
    }

    private boolean idpRequired;

    public static final String SETTINGS = "saml2.settings";
    public static final String IDP_REQUIRED_SETTING = "saml2.idpRequired";

    public static String getBaseAlias() {
        return "plugins/servlet/saml/auth";
    }

    public void add(SAMLJiraConfig config) {
        settings.put(config.getEntityId(), config);
        persist();
        update();
    }

    public void persist() {
        try {
            String ss = objectMapper.writeValueAsString(settings);
            pluginSettings.put(SETTINGS, ss);
            pluginSettings.put(IDP_REQUIRED_SETTING, String.valueOf(idpRequired));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void update() {
        Boolean idpRequired = null;
        try {
            idpRequired = Boolean.valueOf((String) pluginSettings.get(IDP_REQUIRED_SETTING));
        } catch (ClassCastException e) {
            idpRequired = false;
        }
        if (idpRequired == null) idpRequired = false;
        this.idpRequired = idpRequired;
        try {
            String s = StringUtils.defaultString((String) pluginSettings.get(SETTINGS));
            if (!s.isEmpty()) {
                settings = objectMapper.readValue((String) pluginSettings.get(SETTINGS), new TypeReference<Map<String, SAMLJiraConfig>>() {});
                idps = settings.entrySet().stream()
                        .map(v -> new NameValuePair(v.getKey(), v.getValue().getId()))
                        .sorted((u, v) -> v.getValue().compareTo(u.getValue()))
                        .collect(Collectors.toList());
                String name = i18n.getText("saml2Plugin.admin.newIdP");
                if (!settings.containsKey(name)) {
                    createEmpty();
                }
                return;
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        createEmpty();
    }

    public void remove(String entityId) {
        if (settings.remove(entityId) != null) {
            persist();
            update();
        }
    }

    public SAMLJiraConfig createEmpty() {
        if (settings == null || settings.isEmpty()) settings = new HashMap<>();
        if (idps == null || idps.isEmpty()) idps = new ArrayList<>();
        SAMLJiraConfig samlJiraConfig = new SAMLJiraConfig();
        samlJiraConfig.setId(NEW_ID_EMPTY);
        String name = "Новый IdentityProvider";//i18n.getText("saml2Plugin.admin.newIdP");
        settings.put(name, samlJiraConfig);
        idps.add(new NameValuePair(name, samlJiraConfig.getId()));
        return samlJiraConfig;
    }

    public List<NameValuePair> getIdps() {
        return idps;
    }

    public Map<String, SAMLJiraConfig> getSettings() {
        return settings;
    }

    public void save(SAMLJiraConfig samlJiraConfig) {
        String id = StringUtils.defaultString(samlJiraConfig.getId());
        if (id.isEmpty() || id.equals(NEW_ID_EMPTY)) {
            samlJiraConfig.setId("id" + System.currentTimeMillis());
        }
        setIdpRequired(samlJiraConfig.isIdpRequired());
        add(samlJiraConfig);
    }

    public void save(Map<String, SAMLJiraConfig> settings) {
        this.settings = settings;
        persist();
        update();
    }

    public SAMLJiraConfig get(String entityId) {
        return settings.get(entityId);
    }

    public SAMLJiraConfig getFirst() {
        if (idps.isEmpty()) return createEmpty();
        return settings.get(idps.get(0).getName());
    }

    public boolean isIdpRequired() {
        return idpRequired;
    }

    public void setIdpRequired(boolean idpRequired) {
        pluginSettings.put(IDP_REQUIRED_SETTING, String.valueOf(idpRequired));
        this.idpRequired = idpRequired;
    }
}
