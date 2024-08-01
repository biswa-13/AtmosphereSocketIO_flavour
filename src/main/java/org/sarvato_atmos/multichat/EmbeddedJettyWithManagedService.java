package org.sarvato_atmos.multichat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;

import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereServlet;
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

public class EmbeddedJettyWithManagedService {
	private static final Logger log = LoggerFactory.getLogger(EmbeddedJettyWithManagedService.class);

    public static void main(String[] args) throws Exception {
        new EmbeddedJettyWithManagedService().run();
    }

    private void run() throws Exception {
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setIdleTimeout(30000); // 30 seconds
        connector.setPort(8079);
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
        ServletHolder atmosphereServlet = new ServletHolder(AtmosphereServlet.class);
        atmosphereServlet.setInitParameter(ApplicationConfig.ANNOTATION_PACKAGE, "org.saravato_atmos");
        atmosphereServlet.setInitParameter(ApplicationConfig.WEBSOCKET_CONTENT_TYPE, "application/json");
        atmosphereServlet.setAsyncSupported(true);
        context.addServlet(atmosphereServlet, "/chat/*");
        

        // Configure WebSocket
        JakartaWebSocketServletContainerInitializer.configure(context, null);

        server.setHandler(context);

        log.info("Resource base: {}", context.getBaseResource());
        log.info("Resource base exists: {}", context.getBaseResource().exists());
        Path indexPath = resourceBasePath.resolve("index.html");
        log.info("index.html exists: {}", Files.exists(indexPath));
        if (Files.exists(indexPath)) {
            log.info("index.html content:\n{}", Files.readString(indexPath));
        } else {
            log.warn("index.html not found at {}", indexPath);
        }
        log.info("Default servlet path spec: /*");
//        log.info("Atmosphere servlet path spec: /chat/*");
        log.info("Atmosphere servlet path spec: /socket/*");
        log.info("WebSocket support added");



        server.start();
        log.info("Server started on port {}", connector.getPort());
        server.join();
    }
}
