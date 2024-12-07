package websocket.messages;

import java.util.Objects;
import com.google.gson.Gson;
import chess.ChessGame;

/**
 * Represents a Message the server can send through a WebSocket
 * 
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    ServerMessageType serverMessageType;
    String message = null;
    ChessGame game = null;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    public ServerMessage(ServerMessageType type, String message) {
        this.serverMessageType = type;
        this.message = message;
    }

    public ServerMessage(ServerMessageType type, ChessGame game) {
        this.serverMessageType = type;
        this.game = game;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    public chess.ChessGame getGame() {
        return this.game;
    }
    
    public String getMessage() {
        return this.message;
    }

    private class NotificationJson {
        ServerMessage.ServerMessageType serverMessageType;
        String message;
        NotificationJson(ServerMessage.ServerMessageType type, String message) {
            this.serverMessageType = type;
            this.message = message;
        }
    }

    private class ErrorJson {
        ServerMessage.ServerMessageType serverMessageType;
        String errorMessage;

        ErrorJson(ServerMessage.ServerMessageType type, String message) {
            this.serverMessageType = type;
            this.errorMessage = message;
        }
    }

    private class TouchJson {
        ServerMessage.ServerMessageType serverMessageType;
        String game;
        TouchJson(ServerMessage.ServerMessageType type, String game) {
            this.serverMessageType = type;
            this.game = game;
        }
    }

    public static ServerMessage deSerialize(String serial) {
        Gson g = new Gson();
        String split[] = serial.split(",");
        if(split[0].contains("NOTIFICATION")) {
            NotificationJson json = g.fromJson(serial, NotificationJson.class);
            return new ServerMessage(json.serverMessageType, json.message);
        }
        if(split[0].contains("ERROR")) {
            ErrorJson json = g.fromJson(serial, ErrorJson.class);
            return new ServerMessage(json.serverMessageType, json.errorMessage);
        }
        if(split[0].contains("LOAD_GAME")) {
            TouchJson json = g.fromJson(serial, TouchJson.class);
            return new ServerMessage(json.serverMessageType, ChessGame.deSerialize(json.game));
        }
        else {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage)) {
            return false;
        }
        ServerMessage that = (ServerMessage) o;
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }


}
