package ws;

import server.Server;
import javax.websocket.*;
import java.net.URI;
import java.io.IOException;
import java.util.Arrays;
import websocket.messages.ServerMessage;
import websocket.commands.UserGameCommand;

public final class WebSocketFacade extends Endpoint {
    private static final String SERVER_MESSAGES[] = {"LOAD_GAME", "ERROR", "NOTIFICATION"};
    private static final String USER_COMMANDS[] = {"CONNECT", "MAKE_MOVE", "LEAVE", "RESIGN"};
    public Session session;
    private URI uri;

    public WebSocketFacade(String url) throws Exception {
        //"ws://localhost:8080/ws"
        this.uri = new URI(url);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);
        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                try {
                    parseMessage(message);
                }
                catch(WebSocketException e) {
                }
                catch(Exception e) {
                }
                //System.out.println(message);
            }
        });
    }

    private void parseMessage(String input) throws WebSocketException {
        ServerMessage message = deSerialize(input);
        if(message == null) {
            throw new WebSocketException("The message recieved did not match any expected message: " + message);
        }

        switch(message.getServerMessageType()) {
            case LOAD_GAME:
                break;
            case ERROR:
                break;
            case NOTIFICATION:
                break;
        }
    }

    private void loadGame() {
    }

    private void notification(ServerMessage message) {
    }

    private ServerMessage deSerialize(String message) {
        String first[] = message.split("\t");
        ServerMessage serverMessage;

        if(Arrays.asList(SERVER_MESSAGES).contains(first[0])) {
            serverMessage = ServerMessage.deSerialize(message);
        }
        else {
            serverMessage = null;
        }

        return serverMessage;
    }

    @SuppressWarnings("serial")
    private class WebSocketException extends Exception {
        public WebSocketException(String message) {super(message);}
    }
    /*
    public void onMessage(String message) {
    }
    */

    /*
    public void echo(String message) {
        try {
            this.session.getBasicRemote().sendText(message);
        }
        catch(IOException e){}
    }
    */

    @Override
    public void onOpen(Session session, EndpointConfig config) {
    }
}
