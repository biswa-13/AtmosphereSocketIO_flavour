package org.saravato_atmos.managed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.atmosphere.config.service.*;
import org.atmosphere.config.service.Message;
import org.atmosphere.cpr.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ManagedService(path = "/websocket/*")
public class AtosManagedService {

    private final Logger logger = LoggerFactory.getLogger(AtosManagedService.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final ConcurrentMap<String, ConcurrentMap<String, Broadcaster>> namespaces = new ConcurrentHashMap<>();
    private BroadcasterFactory broadcasterFactory;

    @Ready
    public void onReady(final AtmosphereResource r) {
        this.broadcasterFactory = r.getAtmosphereConfig().getBroadcasterFactory();
        logger.info("Client {} connected.", r.uuid());

        String namespace = getNamespace(r);
        String room = getRoom(r);

        Broadcaster broadcaster = getOrCreateBroadcaster(namespace, room);
        broadcaster.addAtmosphereResource(r);

        emit(broadcaster, "Client Info", "Client connected: " + r.uuid());
    }

    @Disconnect
    public void onDisconnect(AtmosphereResourceEvent event) {
        AtmosphereResource r = event.getResource();
        String namespace = getNamespace(r);
        String room = getRoom(r);

        Broadcaster broadcaster = getOrCreateBroadcaster(namespace, room);
        broadcaster.removeAtmosphereResource(r);

        emit(broadcaster, "Disconnect Info", "Client disconnected: " + r.uuid());
    }

    @Message
    public void onMessage(AtmosphereResource atmosphereResource, String message) {
        try {
            System.out.println("onMessage() is called ...");
            if (!message.contains("fromServer")) {
                logger.info("Received message: {}", message);
                String namespace = getNamespace(atmosphereResource);
                String room = getRoom(atmosphereResource);
                Map<String, String> respObbj = new HashMap<>();
                String result = "";
                // Parse the incoming message as JSON
                JsonNode jsonNode = mapper.readTree(message);
                if(jsonNode.has("CustomEvent")) {
                    String eventName = jsonNode.get("CustomEvent").asText();
                    JsonNode data = jsonNode.get("data");
                    // Execute custom logic based on the event name
                    respObbj.put("message", executeCustomLogic(eventName, data));
                } else {
                    result = jsonNode.get("message").toString();
                    respObbj.put("fromServer", "true");
                    respObbj.put("message", result);
                }

                // Retrieve the correct broadcaster
                Broadcaster broadcaster = getOrCreateBroadcaster(namespace, room);

                // Prevent broadcasting the same message back and forth
                //if (!message.contains("fromServer")) {
                broadcaster.broadcast(mapper.writeValueAsString(respObbj));
                //}
            }
        } catch (Exception e) {
            logger.error("Failed to process message: {}", message, e);
        }
    }

    private String executeCustomLogic(String eventName, JsonNode data) {
        System.out.println("executeCustomLogic() is called ...");
        // Implement your custom logic here based on the event name
        switch (eventName) {
            case "getServerTime":
                return onGetServerTime(data);
            case "customEvent":
                return "Handled custom event with data: " + data.toString();
            default:
                return "Unknown event: " + eventName;
        }
    }

    private String onGetServerTime(JsonNode data) {
        System.out.println("onGetServerTime() is called ...");
        // Get current date and time in the server's timezone
        LocalDateTime localDateTime = LocalDateTime.now(ZoneId.systemDefault());
        // Format the date and time as needed
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "{\"serverTime\":\"" + localDateTime.format(dateTimeFormatter) + "\"}";
    }

    private Broadcaster getOrCreateBroadcaster(String namespace, String room) {
        return namespaces
                .computeIfAbsent(namespace, ns -> new ConcurrentHashMap<>())
                .computeIfAbsent(room, rm -> broadcasterFactory.lookup(namespace + "/" + room, true));
    }

    private String getNamespace(AtmosphereResource r) {
        String[] pathParts = r.getRequest().getRequestURI().split("/");
        return pathParts.length >= 4 ? pathParts[2] : "defaultNamespace";
    }

    private String getRoom(AtmosphereResource r) {
        String[] pathParts = r.getRequest().getRequestURI().split("/");
        return pathParts.length >= 4 ? pathParts[3] : "defaultRoom";
    }

    private void emit(Broadcaster broadcaster, String eventName, String message) {
        System.out.println("emit() is called // event name -->"+eventName);
        String eventJson = String.format("{\"CustomEvent\":\"%s\", \"fromServer\":\"true\" ,\"data\":\"%s\"}", eventName, message);
        broadcaster.broadcast(eventJson);
        logger.info("Emitted event {} with message: {}", eventName, message);
    }
}