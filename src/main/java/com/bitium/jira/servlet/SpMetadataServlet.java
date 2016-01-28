package com.bitium.jira.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


public class SpMetadataServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
//    private Log log = LogFactory.getLog(SpMetadataServlet.class);

    @Override
    public void init() throws ServletException {
        super.init();
    }

    String xmlTmpl = "<md:EntityDescriptor xmlns:md=\"urn:oasis:names:tc:SAML:2.0:metadata\" entityID=\"%s\">\n" +
            "<md:SPSSODescriptor WantAssertionsSigned=\"true\" protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\">\n" +
            "<md:NameIDFormat>\n" +
            "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified\n" +
            "</md:NameIDFormat>\n" +
            "<md:AssertionConsumerService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" Location=\"%s\" index=\"0\"/>\n" +
            "</md:SPSSODescriptor>\n" +
            "</md:EntityDescriptor>";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
//        String entityId = request.getParameter("entityId");
        String url = request.getRequestURL().toString();
        if (!url.endsWith("/")) url += "/";
        url = url.replaceAll("[^/]+?/$", "auth");
        response.setHeader("Content-Type", "text/xml");
        PrintWriter writer = response.getWriter();
        writer.write(String.format(xmlTmpl, url, url));
        writer.close();
    }
}
