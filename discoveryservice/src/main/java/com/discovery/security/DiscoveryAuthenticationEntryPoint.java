package com.discovery.security;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
@Component
public class DiscoveryAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        response.setHeader("WWW-Authenticate", "Basic real=" + getRealmName());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        var writer = response.getWriter();
        //writer.println("HTTP Status 401 - " + exception.getMessage());
        writer.println("HTTP Status 401 -  You are not logged in");
        //response.setContentType("TEXT/HTML");
        /*writer.println("""
                <html>
                <head>
                </head>
                </html>
                """);*/
    }

    @Override
    public void afterPropertiesSet() {
        setRealmName("myreal");
        super.afterPropertiesSet();
    }
}
