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

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
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
        return sb.toString();
    }

    public static ServerMessage deSerialize(String serial) {
        switch(serial) {
            case "LOAD_GAME":
                return new ServerMessage(ServerMessageType.LOAD_GAME);
            case "ERROR":
                return new ServerMessage(ServerMessageType.ERROR);
            case "NOTIFICATION":
                return new ServerMessage(ServerMessageType.NOTIFICATION);
            default:
                return null;
        }
    }
}
