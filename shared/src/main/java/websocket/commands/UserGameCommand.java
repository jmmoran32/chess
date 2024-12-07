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

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID) {
        this.commandType = commandType;
        this.authToken = authToken;
        this.gameID = gameID;
        this.move = null;
    }

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID, ChessMove move) {
        this.commandType = commandType;
        this.authToken = authToken;
        this.gameID = gameID;
        this.move = move;
    }

    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN
    }

    private class CommandJson {
        UserGameCommand.CommandType commandType;
        String authToken;
        Integer gameID;

        CommandJson(UserGameCommand.CommandType type, String token, Integer id) {
            this.commandType = type;
            this.authToken = token;
            this.gameID = id;
        }
    }

    private class MoveJson {
        UserGameCommand.CommandType commandType;
        String authToken;
        Integer gameID;
        chess.ChessMove move;

        MoveJson(UserGameCommand.CommandType type, String token, Integer id, chess.ChessMove move) {
            this.commandType = type;
            this.authToken = token;
            this.gameID = id;
            this.move = move;
        }
    }

    public String serialize() {
        Gson g = new Gson();
        if(this.commandType == CommandType.MAKE_MOVE) {
            MoveJson json = new MoveJson(this.commandType, this.authToken, this.gameID, this.move); 
            return g.toJson(json);
        }
        else {
            CommandJson json = new CommandJson(this.commandType, this.authToken, this.gameID);
            return g.toJson(json);
        }
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

    public ChessMove getMove() {
        return this.move;
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
