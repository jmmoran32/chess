package websocket.commands;

import java.util.Objects;

/**
 * Represents a command a user can send the server over a websocket
 *
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class UserGameCommand {

    private final CommandType commandType;

    private final String authToken;

    private final Integer gameID;

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID) {
        this.commandType = commandType;
        this.authToken = authToken;
        this.gameID = gameID;
    }

    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        switch(this.commandType) {
            case CONNECT:
                sb.append("CONNECT\t");
                break;
            case MAKE_MOVE:
                sb.append("MAKE_MOVE\t");
                break;
            case LEAVE:
                sb.append("LEAVE\t");
                break;
            case RESIGN:
                sb.append("RESIGN\t");
                break;
            default:
                return null;
        }

        sb.append(this.authToken);
        sb.append('\t');
        sb.append(Integer.toString(this.gameID));
        return sb.toString();
    }

    public static UserGameCommand deSerialize(String serial) {
        String serialArray[] = serial.split("\t");
        if(serialArray.length < 3) {
            return null;
        }

        CommandType ctype;
        switch(serialArray[0]) {
            case "CONNECT":
                ctype = CommandType.CONNECT;
                break;
            case "MAKE_MOVE":
                ctype = CommandType.MAKE_MOVE;
                break;
            case "LEAVE":
                ctype = CommandType.LEAVE;
                break;
            case "RESIGN":
                ctype = CommandType.RESIGN;
                break;
            default:
                return null;
        }
        
        return new UserGameCommand(ctype, serialArray[1], Integer.parseInt(serialArray[2]));
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public String getAuthToken() {
        return authToken;
    }

    public Integer getGameID() {
        return gameID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserGameCommand)) {
            return false;
        }
        UserGameCommand that = (UserGameCommand) o;
        return getCommandType() == that.getCommandType() &&
                Objects.equals(getAuthToken(), that.getAuthToken()) &&
                Objects.equals(getGameID(), that.getGameID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommandType(), getAuthToken(), getGameID());
    }
}
