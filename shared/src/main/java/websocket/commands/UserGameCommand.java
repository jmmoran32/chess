package websocket.commands;

import java.util.Objects;
import chess.ChessMove;
import com.google.gson.Gson;

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

    private final ChessMove move;
    //1 for white, 0 for black, -1 for spectator, 2 for undefined
    private Integer team = 2;

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID) {
        this.commandType = commandType;
        this.authToken = authToken;
        this.gameID = gameID;
        this.team = 2;
        this.move = null;
    }

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID, ChessMove move) {
        this.commandType = commandType;
        this.authToken = authToken;
        this.gameID = gameID;
        this.team = team;
        this.move = move;
    }

    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN
    }

    /*
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

        if(this.authToken == null) {sb.append("U");}
        else {sb.append(this.authToken);}
        sb.append('\t');
        if(this.gameID == null) {sb.append("U");}
        else {sb.append(Integer.toString(this.gameID));}
        sb.append('\t');
        if(this.team == null) {sb.append("U");}
        else {sb.append(Integer.toString(this.team));}
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
        String authToken = serialArray[1];
        if(authToken.equals("U")) {authToken = null;}

        Integer gameID;
        if(serialArray[2].equals("U")) {gameID = null;}
        else {gameID = Integer.parseInt(serialArray[2]);}

        if(serialArray.length == 3) {
            return new UserGameCommand(ctype, authToken, gameID);
        }
        else if(serialArray.length != 5) {
            return null;
        }
        Integer team;
        if(serialArray[4].equals("U")) {team = null;}
        else {team = Integer.parseInt(serialArray[4]);}

        return new UserGameCommand(ctype, authToken, gameID, serialArray[3], team);
    }
    */


    public CommandType getCommandType() {
        return commandType;
    }

    public String getAuthToken() {
        return authToken;
    }

    public Integer getGameID() {
        return gameID;
    }

    public ChessMove getMove() {
        return this.move;
    }

    public int getTeam() {
        return this.team;
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
