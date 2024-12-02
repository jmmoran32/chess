package websocket.messages;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * 
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    ServerMessageType serverMessageType;
    String message;
    chess.ChessGame game;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type, String message, chess.ChessGame game) {
        this.serverMessageType = type;
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
