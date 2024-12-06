package server;

import service.Service;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.messages.ServerMessage;
import websocket.commands.UserGameCommand;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.sql.SQLException;
import dataaccess.DataAccessException;
import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;

@WebSocket
public class WebSocketHandler {
    private static ConcurrentHashMap<Integer, Session> SESSIONS = new ConcurrentHashMap<Integer, Session>();
    private final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Session>> GAMES = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Session>>();

    public WebSocketHandler() {
    }

    @OnWebSocketConnect
    public void connect(Session session) {
        System.out.printf("OnConnect called for session %d\n", session.hashCode());
        if(SESSIONS.put(session.hashCode(), session) != null) {
            System.out.printf("added session %d to master list\n", session.hashCode());
        }
    }

    @OnWebSocketClose
    public void close(Session session, int status, String reason) {
        //System.out.printf("OnClose called for session %d by reason of %s\n", session.hashCode(), reason);
        if(SESSIONS.remove(session.hashCode()) != null) {
            //System.out.printf("removed session %d from master list in onCLose\n", session.hashCode());
        }
        ConcurrentHashMap<Integer, Session> map;
        for(Integer i : GAMES.keySet()) {
            map = GAMES.get(i);
                if(map.remove(session.hashCode()) != null) {
                    //System.out.printf("removed session %d from game %d in onClose\n", session.hashCode(), i);
                    }
        }

        /*
        for(Integer i : GAMES.keySet()) {
            for(Session s : GAMES.get(i)) {
                if(session.hashCode() == s.hashCode()) {
                    if(GAMES.get(i).remove(s)) {
                        System.out.printf("removed session %d from game %d in onClose\n", s.hashCode(), i);
                    }
                }
            }
        }
        */
    }

    @OnWebSocketError
    public void onErrorRecieved(Throwable e) {
        System.out.println("Websocket error: " + e.getMessage());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        System.out.println("\nRecieved a message from: " + session.hashCode());
        //trimSessions();
        UserGameCommand command = deSerialize(message);
        try {
            if(command != null) {
                parseCommand(session, command);
            }
            else {
                throw new WebSocketException("Unknown command");
            }
        }
        catch(Exception e) {
            System.out.println("There was an error when parsing messge: " + e.getClass().getSimpleName() + e.getMessage());
        }


        /*
        //delete
        System.out.printf("Recieved %s", message);
        session.getRemote().sendString("WebSocket response: " + message);
        //Add logic to intepret what type of message has just been recieved
        */
    }

    /*
    private void trimSessions() {
        //String playerName;
        System.out.println("Trimming...");
        for(Integer i : GAMES.keySet()) {
            for(Session s : GAMES.get(i)) {
                if(!s.isOpen()) {
                    try {
                        //playerName = dataaccess.AuthDataAccess.getUsername(AUTHS.get(s));
                        //dataaccess.GameDataAccess.LeaveGame(COLORS.get(s), i);
                        if(GAMES.get(i).remove(s)) {
                            System.out.printf("trimmed session %d\n", s.hashCode());
                        }
                        //String message = String.format("Player %s has lost connection", playerName);
                        //broadcast(i, message);
                    }
                    catch(Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }
    */

    private void parseCommand(Session session, UserGameCommand command) throws WebSocketException, SQLException, DataAccessException, IOException, Exception {
        try {
            switch(command.getCommandType()) {
                case CONNECT:
                    joinGame(session, command);
                    break;
                case MAKE_MOVE:
                    makeMove(session, command);
                    break;
                case LEAVE:
                    leave(session, command);
                    break;
                case RESIGN:
                    resign(session, command);
                    break;
                //TODO: should I make another command for plain broadcasts? may use it for check warnings
            }
        }
        catch(WebSocketException e) {
            System.out.println("command error: " + e.getMessage());
            sendError(session, e.getMessage());
        }
        catch(IOException e) {
            System.out.println("io exception thrown in parseCommand");
            throw e;
        }
        catch(DataAccessException e) {
            System.out.println("dataaccess exception thrown in parseCommand");
            throw e;
        }
        catch(SQLException e) {
            System.out.println("sql exception thrown in parseCommand");
            throw e;
        }
        catch(Exception e) {
            System.out.println("unkown exception thrown in parseCommand");
            throw e;
        }
    }

    private void joinGame(Session session, UserGameCommand command) throws WebSocketException, SQLException, DataAccessException, IOException {
        //System.out.println("Now joining to game: " + session.hashCode());
        int gameID = command.getGameID();
        dbobjects.GameData gameData = dataaccess.GameDataAccess.getGameObject(gameID);
        if(gameData == null) {
            throw new DataAccessException("Error finding player name in db");
        }
        chess.ChessGame game = gameData.game();
        /*
        if(game == null) {
            throw new WebSocketException("no game was found matchin gameID: " + gameID);
        }
        */
        if(!GAMES.containsKey(gameID)) {
            GAMES.put(gameID, new ConcurrentHashMap<Integer, Session>());
        }
        if(GAMES.get(gameID).put(session.hashCode(), session) != null) {
            //System.out.printf("added session %s to game %d\n", session.hashCode(), gameID);
        }

        String playerName = dataaccess.AuthDataAccess.getUsername(command.getAuthToken());
        if(playerName == null) {
            throw new WebSocketException("Unable to find player name associated with token: " + command.getAuthToken());
        }
        //System.out.printf("%d has been identified as player %s\n", session.hashCode(), playerName);
        int team;

        if(gameData.whiteUsername().equals(playerName)) {
            team = 1;
        }
        else if(gameData.blackUsername().equals(playerName)) {
            team = 0;
        }
        else {
            team = -1;
        }
        String message;

        if(team == 1) {  //white
            //System.out.printf("%d as player %s has been identified as joining team white\n", session.hashCode(), playerName);
            message = String.format("%s has joined team white", playerName);
        }
        else if(team == -1) { //spectator
            //System.out.printf("%d as player %s has been identified as joining team spectator\n", session.hashCode(), playerName);
            message = String.format("%s has joined as a spectator", playerName);
        }
        else if(team == 0) {  //black
            //System.out.printf("%d as player %s has been identified as joining team black\n", session.hashCode(), playerName);
            message = String.format("%s has joined team black", playerName);
        }
        else {
            throw new WebSocketException("Couldn't identify team");
        }

        touchOne(session, gameID);
        broadcast(session, gameID, message);
        //System.out.printf("%d has finished joining game\n", session.hashCode());
    }

    private void makeMove(Session session, UserGameCommand command) throws WebSocketException, SQLException, DataAccessException, IOException, Exception {
        String authToken = command.getAuthToken();
        String playerName = dataaccess.AuthDataAccess.getUsername(authToken);
        if(playerName == null) {
            throw new DataAccessException("The player couldn't be found in the DB");
        }
        int gameID = command.getGameID();
        if(!dataaccess.GameDataAccess.isPlayer(gameID, playerName)) {
            throw new WebSocketException(String.format("Player %s isn't white or black", playerName));
        }
        ChessGame game;
        game = dataaccess.GameDataAccess.getGame(gameID);
        if(game == null) {
            throw new DataAccessException("unable to get game from DB");
        }
        chess.ChessMove move = command.getMove();
        if(move == null) {
            throw new Exception("couldn't find move in makeMove()");
        }

        try {
            game.makeMove(move);
        }
        catch(InvalidMoveException e) {
            sendError(session, "move error: " + e.getMessage());
            return;
        }
        System.out.printf("\nsession %d, player %s has made a move\n", session.hashCode(), playerName);

        String newGame = game.serialize();
        if(!dataaccess.GameDataAccess.updateGame(gameID, newGame)) {
            throw new WebSocketException("The game in the DB couldn't be updated.");
        }
        if(game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
            broadcast(gameID, "White is in checkmate!");
        }
        else if(game.isInCheck(ChessGame.TeamColor.WHITE)) {
            broadcast(gameID, "White is in check...");
        }
        if(game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
            broadcast(gameID, "Black is in checkmate!");
        }
        else if(game.isInCheck(ChessGame.TeamColor.BLACK)) {
            broadcast(gameID, "Black is in check...");
        }
        touchGame(gameID);
    }

    private void leave(Session session, UserGameCommand command) throws WebSocketException, SQLException, DataAccessException, IOException {
        int gameID = command.getGameID();
        ConcurrentHashMap<Integer, Session> sessions = GAMES.get(gameID);
        if(sessions == null) {
            throw new WebSocketException("No sessions were found associated with gameID: " + gameID);
        }

        if(sessions.remove(session.hashCode()) != null) {
            throw new WebSocketException("The player cannot leave a game they haven't joined");
        }
        else {
            System.out.printf("removed session %d from game %d by reason of leave command\n", session.hashCode(), gameID);
        }

        String playerName = dataaccess.AuthDataAccess.getUsername(command.getAuthToken());
        if(playerName == null) {
            throw new WebSocketException("Unable to find player name associated with token: " + command.getAuthToken());
        }
        
        int team = command.getTeam();
        chess.ChessGame.TeamColor color;

        if(team > 0) {
            color = chess.ChessGame.TeamColor.WHITE;
        }
        else if(team < 0) {
            String message = String.format("Player %s has left spectating", playerName);
            broadcast(gameID, message);
            return;
        }
        else {
            color = chess.ChessGame.TeamColor.BLACK;
        }

        if(!dataaccess.GameDataAccess.LeaveGame(color, gameID)) {
            throw new WebSocketException("couldn't find a game associated with: " + gameID);
        }

        String message = String.format("Player %s has left the game", playerName);
        broadcast(gameID, message);
    }

    private void resign(Session session, UserGameCommand command) throws SQLException, WebSocketException, DataAccessException, IOException {
        int gameID = command.getGameID();
        String playerName = dataaccess.AuthDataAccess.getUsername(command.getAuthToken());
        if(playerName == null) {
            throw new WebSocketException("Unable to find player name associated with token: " + command.getAuthToken());
        }

        ChessGame game;
        game = dataaccess.GameDataAccess.getGame(gameID);
        if(game == null) {
            throw new SQLException("unable to get game from DB");
        }
        String newGame = game.serialize();
        if(!dataaccess.GameDataAccess.updateGame(gameID, newGame)) {
            throw new WebSocketException("The game in the DB couldn't be updated.");
        }
        touchGame(gameID);

        String message = String.format("Player %s has resigned", playerName);
        broadcast(gameID, message);
    }

    private class errorJson {
        ServerMessage.ServerMessageType serverMessageType;
        String errorMessage;

        errorJson(ServerMessage.ServerMessageType type, String message) {
            this.serverMessageType = type;
            this.errorMessage = message;
        }
    }

    private void sendError(Session session, String message) throws IOException {
        errorJson j = new errorJson(ServerMessage.ServerMessageType.ERROR, message);
        Gson g = new Gson();
        session.getRemote().sendString(g.toJson(j));
    }

    private class notificationJson {
        ServerMessage.ServerMessageType serverMessageType;
        String message;
        notificationJson(ServerMessage.ServerMessageType type, String message) {
            this.serverMessageType = type;
            this.message = message;
        }
    }

    private void broadcast(Session session, int gameID, String message) throws IOException {
        ServerMessage announcement = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message, null);
        ConcurrentHashMap<Integer, Session> map = GAMES.get(gameID);
        for(Session s : map.values()) {
            System.out.printf("attempting to broadcast to %d int gameID %d\n", s.hashCode(), gameID);
            if(s.hashCode() == session.hashCode()) {
                System.out.println("skipping broadcast to " + session.hashCode());
                continue;
            }
            System.out.printf("broadcasting %d in gameID: %d\n", s.hashCode(), gameID);
            notificationJson n = new notificationJson(ServerMessage.ServerMessageType.NOTIFICATION, message);
            Gson g = new Gson();
            s.getRemote().sendString(g.toJson(n));
        }
    }

    private void broadcast(int gameID, String message) throws IOException {
        ServerMessage announcement = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message, null);
        notificationJson n = new notificationJson(ServerMessage.ServerMessageType.NOTIFICATION, message);
           // s.getRemote().sendString(announcement.serializeJson());
        Gson g = new Gson();
        for(Session s : GAMES.get(gameID).values()) {
            System.out.printf("broadcasting %d in gameID: %d\n", s.hashCode(), gameID);
            //s.getRemote().sendString(announcement.serialize());
            //s.getRemote().sendString(announcement.serializeJson());
            s.getRemote().sendString(g.toJson(n));
        }
    }

    private class touchJson {
        ServerMessage.ServerMessageType serverMessageType;
        String game;
        touchJson(ServerMessage.ServerMessageType type, String game) {
            this.serverMessageType = type;
            this.game = game;
        }
    }

    private void touchOne(Session s, int gameID) throws IOException, SQLException {
        ChessGame game = dataaccess.GameDataAccess.getGame(gameID);
        Gson g = new Gson();
        touchJson n = new touchJson(ServerMessage.ServerMessageType.LOAD_GAME, game.serialize());
        System.out.printf("touching %d for %d only\n", gameID, s.hashCode());
        System.out.printf("\n%s\n\n", g.toJson(n));
        s.getRemote().sendString(g.toJson(n));
    }

    private void touchGame(int gameID) throws IOException, SQLException {
        ChessGame game = dataaccess.GameDataAccess.getGame(gameID);
        ServerMessage touch = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, null, game);
        Gson g = new Gson();
        touchJson n = new touchJson(ServerMessage.ServerMessageType.LOAD_GAME, game.serialize());
        for(Session s : GAMES.get(gameID).values()) {
            System.out.printf("touching %d in gameID: %d\n", s.hashCode(), gameID);
            //s.getRemote().sendString(touch.serialize());
            //s.getRemote().sendString(touch.serializeJson());
            s.getRemote().sendString(g.toJson(n));
        }
    }

    /*
    private class commandJson {
        String commandType;
        String authToken;
        int gameID;

        String serial() {
            StringBuilder sb = new StringBuilder();
            sb.append(this.commandType);
            sb.append('\t');
            sb.append(this.authToken);
            sb.append('\t');
            sb.append(Integer.toString(gameID));
            return sb.toString();
        }
    }

    private String jsonDeSerialize(String message) {
        Gson g = new Gson();
        commandJson j = g.fromJson(message, commandJson.class);
        return j.serial();
    }
    */

    private class commandJson {
        UserGameCommand.CommandType commandType;
        String authToken;
        Integer gameID;

        commandJson(UserGameCommand.CommandType type, String token, Integer id) {
            this.commandType = type;
            this.authToken = token;
            this.gameID = id;
        }
    }

    private class moveJson extends commandJson {
        chess.ChessMove move;

        moveJson(UserGameCommand.CommandType type, String token, Integer id, chess.ChessMove move) {
            super(type, token, id);
            this.move = move;
        }
    }

    private UserGameCommand deSerialize(String message) {
        String first[] = message.split(",");
        Gson g = new Gson();

        if(first[0].contains("CONNECT")) {
            commandJson j = g.fromJson(message, commandJson.class);
            UserGameCommand command = new UserGameCommand(j.commandType, j.authToken, j.gameID);
            return command;
        }
        if(first[0].contains("MAKE_MOVE")) {
            moveJson j = g.fromJson(message, moveJson.class);
            UserGameCommand command = new UserGameCommand(j.commandType, j.authToken, j.gameID, j.move);
            return command;
        }
        if(first[0].contains("LEAVE")) {
            commandJson j = g.fromJson(message, commandJson.class);
            UserGameCommand command = new UserGameCommand(j.commandType, j.authToken, j.gameID);
            return command;
        }
        if(first[0].contains("RESIGN")) {
            commandJson j = g.fromJson(message, commandJson.class);
            UserGameCommand command = new UserGameCommand(j.commandType, j.authToken, j.gameID);
            return command;
        }
        else {
            return null;
        }
    }

    @SuppressWarnings("serial")
    private class WebSocketException extends Exception {
        WebSocketException(String message) {super(message);}
    }
}
