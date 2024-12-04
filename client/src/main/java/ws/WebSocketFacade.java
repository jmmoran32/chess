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

    public WebSocketFacade(String url, String authToken) throws Exception {
        //"ws://localhost:8080/ws"
        this.uri = new URI(url);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);
        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
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

    
    public void sendCommand(UserGameCommand command) {
        try {
            this.session.getBasicRemote().sendText(command.serialize());
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void parseMessage(String input) throws WebSocketException {
        ServerMessage message = deSerialize(input);
        if(message == null) {
            throw new WebSocketException("The message recieved did not match any expected message: " + message);
        }

        switch(message.getServerMessageType()) {
            case LOAD_GAME:
                loadGame(message);
                break;
            case ERROR:
                notification(message);
                break;
            case NOTIFICATION:
                notification(message);
                break;
        }
    }

    private static void loadGame(ServerMessage message) {
        ui.Game.touchBoard(message.getGame().serialize());
    }

    private void notification(ServerMessage message) {
        ui.Game.notify(message.getMessage());
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
