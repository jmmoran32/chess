package server;

import service.Service;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.messages.ServerMessage;
import websocket.commands.UserGameCommand;
import java.io.IOException;

@WebSocket
public class WebSocketHandler {
    public WebSocketHandler() {
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message)  throws IOException {
        //delete
        System.out.printf("Recieved %s", message);
        session.getRemote().sendString("WebSocket response: " + message);
        //Add logic to intepret what type of message has just been recieved
    }
}
