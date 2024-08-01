package org.saravato_atmos.managed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.atmosphere.config.service.Disconnect;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Ready;
import org.atmosphere.config.service.Message;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.Broadcaster;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

@ManagedService(path = "/websocket/chat")
public class AtosManagedService {

	private final Logger logger = LoggerFactory.getLogger(AtosManagedService.class);
	private final ObjectMapper mapper = new ObjectMapper();
	private final ConcurrentHashMap<String, AtmosphereResource> connectedClients = new ConcurrentHashMap<>();
	@Ready
	public void onReady(final AtmosphereResource r) {
		System.out.println("Client {} connected."+ r.uuid());

		// Store the connected client details for further usage
		connectedClients.put(r.uuid(), r);

		// emitting a custom event to the client
		emit(r, "Client Info", "Client Identification: "+r.uuid());
	}

	@Disconnect
	public void onDisconnect(AtmosphereResourceEvent event) {
		if (event.isCancelled()) {
			System.out.println("Client {} unexpectedly disconnected"+event.getResource().uuid());
		} else if (event.isClosedByClient()) {
			System.out.println("Client {} closed the connection"+ event.getResource().uuid());
		}

		// Remove the disconnected client
		connectedClients.remove(event.getResource().uuid());

		// emitting a custom event to the client
		emit(event.getResource(), "Disconnect Info", "Client Disconnected with ID:"+event.getResource().uuid());
	}

	@Message
	public String onMessage(String message) {
		System.out.println("Received message: {}"+ message);

		// Process custom events from the client
		if(message.contains("CustomEvent")) {
			String customEvent = message.substring("CustomEvent".length());
			try {
				JsonNode jsonNode = mapper.readTree(message);
				String eventName = jsonNode.get("CustomEvent").asText();
				JsonNode data = jsonNode.get("data");
				System.out.println("Received custom event: {}, with data: {}"+ eventName+ data);

				// Execute custom events triggered from the client, based on the event name and data and broadcast a response/result
				return onEmit(eventName, data);

			} catch (Exception ex) {
				System.out.println("Failed to process message: {}"+ message+ ex);
				return "Error processing message";
			}
		} else {
			// Echo the message back to the client
			return message;
		}
	}

	// responsible for responding to the custom event triggered from the client
	public String onEmit(String eventName, JsonNode data) {
		String result = "";
		try {
			System.out.println("executeCustomLogic() is called for event:"+eventName);
			if(eventName.equalsIgnoreCase("getServerTime")) {
				result = this.onGetServerTime(data);
			}
		} catch (Exception e) {
			System.out.println("Exception occurred -->"+e.getCause());
        }
		return result;
	}

	public String onGetServerTime(JsonNode data) {
		// Get current date and time in the server's timezone
		LocalDateTime localDateTime = LocalDateTime.now(ZoneId.systemDefault());

		// Format the date and time as you need
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		System.out.println("getServerTime() called -->"+localDateTime.format(dateTimeFormatter));
		JSONObject jsonObject = new JSONObject();
		return jsonObject.put("message", localDateTime.format(dateTimeFormatter)).toString();
	}

	public void emit(AtmosphereResource resource, String eventName, String message){
		String eventJson = String.format("{\"CustomEvent\":\"%s\",\"data\":\"%s\"}", eventName, message);
		connectedClients.values().forEach(item -> {
			Broadcaster broadcaster = item.getBroadcaster();
			broadcaster.broadcast(eventJson);
		});
		//resource.getBroadcaster().broadcast(eventJson);
		System.out.println("Emitted event {} with message: {}"+ eventName+ message);
	}
}
