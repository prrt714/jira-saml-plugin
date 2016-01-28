package com.bitium.jira.rest;

import com.bitium.jira.config.SAMLJiraConfig;
import com.bitium.jira.config.SamlPluginSettings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/")
//@AnonymousAllowed
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class RestResource {

    private final SamlPluginSettings samlPluginSettings;

    public RestResource(SamlPluginSettings samlPluginSettings) {
        this.samlPluginSettings = samlPluginSettings;
    }

    @GET
    public Response getConfig() {
        Map<String, SAMLJiraConfig> m = samlPluginSettings.getSettings();
        return Response.ok(m.entrySet().stream().map(e -> e.getValue()).filter(c -> c.getEntityId() != null).collect(Collectors.toList())).build();
    }

    @GET
    @Path("/idps")
    public Response getIdps() {
        Map<String, SAMLJiraConfig> m = samlPluginSettings.getSettings();
        return Response.ok(m.entrySet().stream().map(e -> e.getValue().getEntityId()).filter(e -> e != null && !e.isEmpty()).collect(Collectors.toList())).build();
    }

    @POST
    public void setConfig(final Map<String, SAMLJiraConfig> m) {
        samlPluginSettings.save(m);
    }

    @DELETE
    public void deleteIdP(@QueryParam("entityId") String entityId) {
        samlPluginSettings.remove(entityId);
    }
}