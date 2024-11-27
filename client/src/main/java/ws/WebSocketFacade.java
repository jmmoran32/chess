package ws;

import server.Server;
import javax.websocket.*;
import java.net.URI;
import java.io.IOException;

public final class WebSocketFacade extends Endpoint {
    public Session session;
    private URI uri;

    public WebSocketFacade(String url) throws Exception {
        //"ws://localhost:8080/ws"
        this.uri = new URI(url);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);
        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                System.out.println(message);
            }
        });
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
