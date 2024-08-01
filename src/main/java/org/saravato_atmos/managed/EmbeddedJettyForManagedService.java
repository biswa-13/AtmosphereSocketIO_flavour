package org.saravato_atmos.managed;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;

import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereServlet;
import org. atmosphere. cpr. AtmosphereServlet.*;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.interceptor.HeartbeatInterceptor;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.FilterHolder;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.DispatcherType;

import static org.atmosphere.cpr.Universe.framework;

public class EmbeddedJettyForManagedService {
    private static final Logger log = LoggerFactory.getLogger(EmbeddedJettyForManagedService.class);

    public static void main(String[] args) throws Exception {
        new EmbeddedJettyForManagedService().run();
    }

    private void run() throws Exception {
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setIdleTimeout(30000); // 30 seconds
        connector.setPort(8069);
        server.addConnector(connector);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // Configure the ServletContextHandler before starting the server
        context.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        context.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,HEAD,OPTIONS,PUT,DELETE");
        context.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin");
        context.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true");

        // Add the CrossOriginFilter
        FilterHolder corsFilterHolder = new FilterHolder(new CrossOriginFilter());
        context.addFilter(corsFilterHolder, "/*", EnumSet.allOf(DispatcherType.class));


        Path resourceBasePath = Paths.get("C:\\Users\\bichh\\eclipse-workspace\\JavaPrac\\saravato_atmos\\src\\main\\webapp");
        context.setBaseResource(ResourceFactory.root().newResource(resourceBasePath.toUri()));

        // Add DefaultServlet to serve static content
        ServletHolder defaultServlet = new ServletHolder("default", DefaultServlet.class);
        defaultServlet.setInitParameter("dirAllowed", "true");
        defaultServlet.setInitParameter("welcomeServlets", "true");
        defaultServlet.setInitParameter("redirectWelcome", "true");
        context.addServlet(defaultServlet, "/*");

        // Add AtmosphereServlet
        ServletHolder atmosphereServletHolder = new ServletHolder(AtmosphereServlet.class);
        atmosphereServletHolder.setInitParameter(ApplicationConfig.ANNOTATION_PACKAGE, "org.saravato_atmos.managed");
        atmosphereServletHolder.setInitParameter(ApplicationConfig.WEBSOCKET_CONTENT_TYPE, "application/json");
        atmosphereServletHolder.setAsyncSupported(true);
        context.addServlet(atmosphereServletHolder, "/websocket/*");

        // Access and configure AtmosphereFramework
        AtmosphereServlet atmosphereServlet = new AtmosphereServlet();
        AtmosphereFramework atmosphereFramework = atmosphereServlet.framework();
        atmosphereFramework.interceptor(new HeartbeatInterceptor().heartbeatFrequencyInSeconds(10));
        // Register custom encoders and decoders


        // Configure WebSocket
        JakartaWebSocketServletContainerInitializer.configure(context, null);

        server.setHandler(context);

        System.out.println("Resource base: {}"+ context.getBaseResource());
        System.out.println("Resource base exists: {}"+ context.getBaseResource().exists());
        Path indexPath = resourceBasePath.resolve("index.html");
        System.out.println("index.html exists: {}"+ Files.exists(indexPath));
        if (Files.exists(indexPath)) {
            System.out.println("index.html content:\n{}"+ Files.readString(indexPath));
        } else {
            System.out.println("index.html not found at {}"+ indexPath);
        }
        System.out.println("Default servlet path spec: /*");
        System.out.println("Atmosphere servlet path spec: /websocket/*");
        System.out.println("WebSocket support added");



        server.start();
        System.out.println("Jetty is listening on: http://localhost:"+connector.getPort());
        System.out.println("Server started on port {}"+ connector.getPort());
        server.join();
    }
}
