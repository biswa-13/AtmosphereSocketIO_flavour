package org.saravato_atmos.managed;

import jakarta.servlet.DispatcherType;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereServlet;
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;

public class EmbeddedJettyForManagedService {
    private static final Logger log = LoggerFactory.getLogger(EmbeddedJettyForManagedService.class);

    public static void main(String[] args) throws Exception {
        new EmbeddedJettyForManagedService().run();
    }

    private void run() throws Exception {
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setIdleTimeout(60000); // 1 minute
        connector.setPort(8071);
        server.addConnector(connector);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // Configure CORS
        context.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        context.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,HEAD,OPTIONS,PUT,DELETE");
        context.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin");
        context.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true");
        FilterHolder corsFilterHolder = new FilterHolder(new CrossOriginFilter());
        context.addFilter(corsFilterHolder, "/*", EnumSet.allOf(DispatcherType.class));

        // Set the base resource path
        Path resourceBasePath = Paths.get("C:\\Users\\bichh\\eclipse-workspace\\JavaPrac\\v3.x_POC_saravato_atmos\\src\\main\\webapp");
        context.setBaseResource(ResourceFactory.root().newResource(resourceBasePath.toUri()));

        // Add DefaultServlet to serve static content
        ServletHolder defaultServlet = new ServletHolder("default", DefaultServlet.class);
        defaultServlet.setInitParameter("dirAllowed", "true");
        defaultServlet.setInitParameter("welcomeServlets", "true");
        defaultServlet.setInitParameter("redirectWelcome", "true");
        context.addServlet(defaultServlet, "/*");

        // Add and configure AtmosphereServlet via ServletHolder
        ServletHolder atmosphereServletHolder = new ServletHolder(AtmosphereServlet.class);
        atmosphereServletHolder.setInitParameter(ApplicationConfig.ANNOTATION_PACKAGE, "org.saravato_atmos.managed");
        atmosphereServletHolder.setInitParameter(ApplicationConfig.WEBSOCKET_CONTENT_TYPE, "application/json");
        atmosphereServletHolder.setAsyncSupported(true);
        context.addServlet(atmosphereServletHolder, "/websocket/*");

        // Configure WebSocket
        JakartaWebSocketServletContainerInitializer.configure(context, null);

        server.setHandler(context);

        // Start the server
        server.start();
        log.info("Jetty is listening on: http://localhost:{}", connector.getPort());
        log.info("Server started on port {}", connector.getPort());

        // Access the AtmosphereServlet after the server has started
        AtmosphereServlet atmosphereServlet = (AtmosphereServlet) atmosphereServletHolder.getServlet();
        if (atmosphereServlet != null) {
            AtmosphereFramework atmosphereFramework = atmosphereServlet.framework();
            atmosphereFramework.interceptor(new HeartbeatInterceptor().heartbeatFrequencyInSeconds(30));
            log.info("AtmosphereFramework initialized with HeartbeatInterceptor.");
        } else {
            log.error("Failed to initialize AtmosphereFramework.");
        }

        server.join();
    }
}
