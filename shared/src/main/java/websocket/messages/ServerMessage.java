package websocket.messages;

import java.util.Objects;
import com.google.gson.Gson;

/**
 * Represents a Message the server can send through a WebSocket
 * 
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    ServerMessageType serverMessageType;
    String message = null;
    chess.ChessGame game = null;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    public ServerMessage(ServerMessageType type, String message, chess.ChessGame game) {
        this.serverMessageType = type;
        this.message = message;
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

    public static ServerMessage deSerializeJson(String json) {
        String split[] = json.split(",");
        if(split[0].contains("LOAD_GAME")) {
            String game = split[1].substring(8, split[1].length() - 2);
            chess.ChessGame gameObj = chess.ChessGame.deSerialize(game);
            return new ServerMessage(ServerMessageType.LOAD_GAME, null, gameObj);
        }
        else if(split[0].contains("ERROR")) {
            String message = split[1].substring(16, split[1].length() - 2);
            return new ServerMessage(ServerMessageType.ERROR, message, null);
        }
        else if(split[0].contains("NOTIFICATION")) {
            String message = split[1].substring(11, split[1].length() - 2);
            return new ServerMessage(ServerMessageType.ERROR, message, null);
        }
        else {
            return null;
        }
    }

    public String serializeJson() {
        String message;
        switch(this.serverMessageType) {
            case ServerMessageType.LOAD_GAME:
                message = "{\"serverMessageType\":\"LOAD_GAME\",\"game\":\"" + this.game + "\"}";
                return message;
            case ServerMessageType.ERROR:
                message = "{\"serverMessageType\":\"ERROR\",\"errorMessage\":\"" + this.message + "\"}";
                return message;
            case ServerMessageType.NOTIFICATION:
                message = "{\"serverMessageType\":\"NOTIFICATION\",\"message\":\"" + this.message + "\"}";
                return message;
            default:
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

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        switch(this.serverMessageType) {
            case LOAD_GAME:
                sb.append("LOAD_GAME");
                break;
            case ERROR:
                sb.append("ERROR");
                break;
            case NOTIFICATION:
                sb.append("NOTIFICATION");
                break;
            default:
                sb.append("UNDEFINED");
        }
        sb.append("\t");
        sb.append(this.message);
        sb.append("\t");
        if(this.game == null) {
            sb.append('U');
        }
        else {
            sb.append(this.game.serialize());
        }
        return sb.toString();
    }

    public static ServerMessage deSerialize(String serial) {
        String deSerial[] = serial.split("\t");
        if(deSerial.length != 3) {
            return null;
        }
        switch(deSerial[0]) {
            case "LOAD_GAME":
                if(deSerial[2].equals("U")) {
                    System.out.println("Cannot load an undefined game!");
                    return null;
                }
                return new ServerMessage(ServerMessageType.LOAD_GAME, deSerial[1], chess.ChessGame.deSerialize(deSerial[2]));
            case "ERROR":
                return new ServerMessage(ServerMessageType.ERROR, deSerial[1], null);
            case "NOTIFICATION":
                return new ServerMessage(ServerMessageType.NOTIFICATION, deSerial[1], null);
            default:
                return null;
        }
    }
}
