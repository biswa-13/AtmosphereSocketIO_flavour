var chatUrl = 'ws://localhost:8071/websocket/Blue/BlueBoyz';
var socket = atmosphere;
var subSocket;
var request = {
	url: chatUrl,
	contentType: "application/json",
	transport: "websocket",
//	fallbackTransport: 'long-pooling',
	timeout1: 1200000,
	reconnectInterval1: 10000,
	maxReconnectOnClose: 20,
	uuid: "13125",
	// Time to wait before timing out the connection attempt
	connectTimeout: 2000,
	reconnect: true,
	trackMessageLength: true,
	withCredentials: true,
	onOpen: function(response) {
	    $('#chatBox').append('<p>Connected to the chat server.</p>');
	    console.log("onOpen called-->" + new Date());
	},
	// Callback function called when the connection is closed
	onClose: function(response) {
	    console.log("onClosed called --> " + new Date());
	},
	// Callback function called when a message is received
    onMessage : function (response) {
        var message = response.responseBody;
        try {
            var json = JSON.parse(message);
            console.log("Data rcvd from Srvr -->",json);

            // Check for custom events
            if (json.CustomEvent) {
                console.log('Custom event received:', json.CustomEvent, 'with data:', json.data);
                if(json.CustomEvent == "Client Info") {
                    onClientInfo(json.data);
                } else if (json.CustomEvent == "Disconnect Info") {
                    onDisconnectInfo(json.data);
                } else {
                    console.error("Please register the listener for the server event:", json.CustomEvent);
                }
            } else {
                $('#chatBox').append('<p>' + json.message + '</p>');
            }
        } catch (e) {
            console.log('Invalid JSON: ', message);
            return;
        }
    },
	// Callback function called when a reconnect attempt is made
	onReconnect: function(request, response) {
	    console.log("onReconnect called-->", new Date());
	    $('#chatBox').append('<p>Re-Connecting to the chat server.</p>');
	},
	// Callback function called when an error occurs
	onError: function(response) {
	    $('#chatBox').append('<p>Error in the chat server.</p>');
	    console.log("onError called -->" + new Date(), "\n", response.reasonPhrase);
	},
	// Callback function called when the client times out
	onClientTimeout: function(request) {
	$('#chatBox').append('<p>Client Timeout</p>');
	    console.log("onClientTimeout() called ...");
	},
	// Callback function called when the transport fails
	onTransportFailure: function(errorMsg, request) {
	    console.log("onTransportFailure() called --> " + errorMsg);
	    $('#chatBox').append('<p>Transport Failure.</p>');
	},
	// Callback function called when the connection is reopened
	onReopen: function(response) {
	    $('#chatBox').append('<p>Connection reopened </p>');
	    console.log("onReopen() called -->", new Date());
	},
	// Callback function called when a local message is received
	onLocalMessage: function(response) {
	    $('#chatBox').append('<p>Local message recvd </p>');
	    console.log("onLocalMessage() called --> " + response.responseBody);
	}
}

function connect() {
	subSocket = socket.subscribe(request);
	console.log("First time connecting to server -->",subSocket)
}

// Function to send a message
function sendMessage() {
    if (subSocket) {
		console.log("sendMessage msg -->",$("#msgBox").val())
        subSocket.push(JSON.stringify({ message: $("#msgBox").val() }));
		$("#msgBox").val('');
        console.log("Message sent");
    }
}

// triggering/emitting a custom event
function triggerCustomEvent() {
    if(subSocket) {
        console.log("triggering custom event: getServerTime()");
        // Emit a custom event to the server
        var eventMessage = JSON.stringify({
            CustomEvent: "getServerTime",
            data: {message: "This is a custom event emitted from the client, to get the serverTime..."}
        });
        subSocket.push(eventMessage);
    }
}

function close() {
    if (subSocket) {
        subSocket.close();
        console.log("closing the connection");
    }
}

// Function to disconnect the socket
function disconnect() {
    if (subSocket) {
        subSocket.disconnect(); // Close the connection
        console.log("Connection disconnected");
    }
}

function onClientInfo(data) {
    // Custom logic
    console.log('Executing custom event // onClientInfo(), data: ', data);
    $('#chatBox').append('<p> Client Info emitted from server: ' + data + '</p>');
}

function onDisconnectInfo(data) {
    // Custom logic
    console.log('Executing custom event // onDisconnectInfo(), data: ', data);
    $('#chatBox').append('<p>Disconnect Info emitted from server: ' + data + '</p>');
}


// =============================== Second Namespace ====================================

var chatUrl1 = 'ws://localhost:8069/websocket/Red/RedChillyz';
var socket1 = atmosphere;
var subSocket1;
var request1 = {
	url: chatUrl1,
	contentType: "application/json",
	transport: "websocket",
//	fallbackTransport: 'long-pooling',
	timeout: 1200000,
	reconnectInterval: 10000,
	maxReconnectOnClose: 12,
	uuid: "131256",
	// Time to wait before timing out the connection attempt
	connectTimeout: 15000,
	reconnect: true,
	trackMessageLength: true,
	withCredentials: true,
	onOpen: function(response) {
	    $('#chatBox1').append('<p>Connected to the chat server.</p>');
	    console.log("Connected -->" + response);
	},
	// Callback function called when the connection is closed
	onClose: function(response) {
	    console.log("Disconnected --> " + response);
	},
	// Callback function called when a message is received
    onMessage : function (response) {
        var message = response.responseBody;
        try {
            var json = JSON.parse(message);
            console.log("Data rcvd from Srvr -->",json);

            // Check for custom events
            if (json.CustomEvent) {
                console.log('Custom event received:', json.CustomEvent, 'with data:', json.data);
                if(json.CustomEvent == "Client Info") {
                    onClientInfo1(json.data);
                } else if (json.CustomEvent == "Disconnect Info") {
                    onDisconnectInfo1(json.data);
                } else {
                    console.error("Please register the listener for the server event:", json.CustomEvent);
                }
            } else {
                $('#chatBox1').append('<p>' + json.message + '</p>');
            }
        } catch (e) {
            console.log('Invalid JSON: ', message);
            return;
        }
    },
	// Callback function called when a reconnect attempt is made
	onReconnect: function(request, response) {
	    console.log("Reconnecting req/res-->",request, response);
	    $('#chatBox1').append('<p>Re-Connecting to the chat server.</p>');
	},
	// Callback function called when an error occurs
	onError: function(response) {
	    $('#chatBox1').append('<p>Error in the chat server.</p>');
	    console.log("Error: " + response.reasonPhrase);
	},
	// Callback function called when the client times out
	onClientTimeout: function(request) {
	$('#chatBox1').append('<p>Client Timeout</p>');
	    console.log("Client timeout");
	},
	// Callback function called when the transport fails
	onTransportFailure: function(errorMsg, request) {
	    console.log("Transport failure: " + errorMsg);
	    $('#chatBox1').append('<p>Transport Failure.</p>');
	},
	// Callback function called when the connection is reopened
	onReopen: function(response) {
	    $('#chatBox1').append('<p>Connection reopened </p>');
	    console.log("Connection reopened res -->", response);
	},
	// Callback function called when a local message is received
	onLocalMessage: function(response) {
	    $('#chatBox1').append('<p>Local message recvd </p>');
	    console.log("Local message res --> " + response.responseBody);
	}
}

function connect1() {
	subSocket1 = socket1.subscribe(request1);
	console.log("trying to connect subSocket1 -->",subSocket1)
}

// Function to send a message
function sendMessage1() {
    if (subSocket1) {
		console.log("sendMessage msg1 -->",$("#msgBox").val())
        subSocket1.push(JSON.stringify({ message: $("#msgBox1").val() }));
		$("#msgBox1").val('');
        console.log("Message sent1");
    }
}

// triggering/emitting a custom event
function triggerCustomEvent1() {
    if(subSocket1) {
        console.log("triggering custom event1 : getServerTime()");
        // Emit a custom event to the server
        var eventMessage = JSON.stringify({
            CustomEvent: "getServerTime",
            data: {message: "This is a custom event emitted from the client, to get the serverTime..."}
        });
        subSocket1.push(eventMessage);
    }
}

function close1() {
    if (subSocket1) {
        subSocket1.close();
        console.log("closing the connection 1");
    }
}

// Function to disconnect the socket
function disconnect1() {
    if (subSocket1) {
        subSocket1.disconnect(); // Close the connection
        console.log("Connection disconnected 1");
    }
}

function onClientInfo1(data) {
    // Custom logic
    console.log('Executing custom event // onClientInfo(), data 1: ', data);
    $('#chatBox1').append('<p> Client Info emitted from server 1: ' + data + '</p>');
}

function onDisconnectInfo1(data) {
    // Custom logic
    console.log('Executing custom event // onDisconnectInfo(), data 1: ', data);
    $('#chatBox1').append('<p>Disconnect Info emitted from server: 1' + data + '</p>');
}