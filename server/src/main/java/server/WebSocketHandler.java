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
    private static final ConcurrentHashMap<Integer, Session> SESSIONS = new ConcurrentHashMap<Integer, Session>();
    private static final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Session>> GAMES = 
        new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Session>>();
    private static final ConcurrentHashMap<Integer, Integer> COLORS = new ConcurrentHashMap<Integer, Integer>();

    public WebSocketHandler() {
    }

    @OnWebSocketConnect
    public void connect(Session session) {
        SESSIONS.put(session.hashCode(), session);
    }

    @OnWebSocketClose
    public void close(Session session, int status, String reason) {
        SESSIONS.remove(session.hashCode());
        COLORS.remove(session.hashCode());
        ConcurrentHashMap<Integer, Session> map;
        for(Integer i : GAMES.keySet()) {
            map = GAMES.get(i);
            map.remove(session.hashCode());
        }
    }


    @OnWebSocketError
    public void onErrorRecieved(Throwable e) {
        System.out.println("Websocket error: " + e.getMessage());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
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
    }

    private void parseCommand(Session session, UserGameCommand command) 
            throws WebSocketException, SQLException, DataAccessException, IOException, Exception {
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
        int gameID = command.getGameID();
        dbobjects.GameData gameData = dataaccess.GameDataAccess.getGameObject(gameID);
        if(gameData == null) {
            String message = "Error: Unable to find a game with ID: " + gameID;
            sendError(session, message);
        }
        chess.ChessGame game = gameData.game();

        String playerName = dataaccess.AuthDataAccess.getUsername(command.getAuthToken());
        if(playerName == null) {
            throw new WebSocketException("Unable to find player name associated with token: " + command.getAuthToken());
        }
        int team;

        String w = gameData.whiteUsername();
        String b = gameData.blackUsername();
        if(w != null && gameData.whiteUsername().equals(playerName)) {
            if(b != null && gameData.blackUsername().equals(playerName)) {
                for(Integer h : GAMES.get(gameID).keySet()) {
                    if(COLORS.get(h.hashCode()) != null) {
                        team = COLORS.get(h.hashCode()) == 1 ? 0 : 1;
                    }
                }
            }
            team = 1;
        }
        else if(gameData.blackUsername().equals(playerName)) {
            team = 0;
        }
        else {
            team = -1;
        }
        if(team == 1 || team == 0) {
            COLORS.put(session.hashCode(), team);
        }
        if(!GAMES.containsKey(gameID)) {
            GAMES.put(gameID, new ConcurrentHashMap<Integer, Session>());
        }
        GAMES.get(gameID).put(session.hashCode(), session);
        String message;

        if(team == 1) {  //white
            message = String.format("%s has joined team white", playerName);
        }
        else if(team == -1) { //spectator
            message = String.format("%s has joined as a spectator", playerName);
        }
        else if(team == 0) {  //black
            message = String.format("%s has joined team black", playerName);
        }
        else {
            throw new WebSocketException("Couldn't identify team");
        }

        touchOne(session.hashCode(), gameID);
        broadcast(session, gameID, message);
    }

    private void makeMove(Session session, UserGameCommand command) 
            throws WebSocketException, SQLException, DataAccessException, IOException, Exception {
        String authToken = command.getAuthToken();
        String playerName = dataaccess.AuthDataAccess.getUsername(authToken);
        if(playerName == null) {
            sendError(session, "Error: player not found. try reloggin.");
            throw new DataAccessException("The player couldn't be found in the DB");
        }
        int gameID = command.getGameID();
        if(!dataaccess.GameDataAccess.isPlayer(gameID, playerName)) {
            throw new WebSocketException(String.format("Player %s isn't white or black", playerName));
        }
        dbobjects.GameData gameData = dataaccess.GameDataAccess.getGameObject(gameID);
        if(gameData == null) {
            throw new DataAccessException("Error finding player name in db");
        }
        chess.ChessGame game = gameData.game();
        if(game == null) {
            throw new DataAccessException("unable to get game from DB");
        }
        if(game.isResigned()) {
            String message = String.format("Team %s has resigned. no more moves may be made", game.getTeamTurn().toString());
            sendError(session, message);
            return;
        }
        chess.ChessMove move = command.getMove();
        if(move == null) {
            throw new Exception("couldn't find move in makeMove()");
        }

        int team;
        if(COLORS.get(session.hashCode()) == null) {
            team = -1;
        }
        else {
            team = COLORS.get(session.hashCode());
        }

        if(team == -1) {
            sendError(session, "Error: spectators may not make moves");
            return;
        }
        ChessGame.TeamColor color = team == 1 ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
        if(color != game.getTeamTurn()) {
            String message = String.format("Error: it is not the turn of %s", color.toString());
            sendError(session, message);
            return;
        }

        try {
            game.makeMove(move);
        }
        catch(InvalidMoveException e) {
            sendError(session, "move error: " + e.getMessage());
            return;
        }

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
        String moveMessage = String.format("%s has made a move: %s", color.toString(), move.toString());
        broadcast(session, gameID, moveMessage);
    }

    private void leave(Session session, UserGameCommand command) throws WebSocketException, SQLException, DataAccessException, IOException {
        int gameID = command.getGameID();
        ConcurrentHashMap<Integer, Session> sessions = GAMES.get(gameID);
        if(sessions == null) {
            throw new WebSocketException("No sessions were found associated with gameID: " + gameID);
        }

        sessions.remove(session.hashCode());

        String playerName = dataaccess.AuthDataAccess.getUsername(command.getAuthToken());
        if(playerName == null) {
            throw new WebSocketException("Unable to find player name associated with token: " + command.getAuthToken());
        }
        
        int team = -1;
        Integer hashColor = COLORS.get(session.hashCode());
        if(hashColor != null) {team = hashColor;} 

        ChessGame.TeamColor color;

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

        if(!dataaccess.GameDataAccess.leaveGame(color, gameID)) {
            throw new WebSocketException("couldn't find a game associated with: " + gameID);
        }

        String message = String.format("Player %s has left the game", playerName);
        broadcast(gameID, message);
    }

    private void resign(Session session, UserGameCommand command) throws SQLException, WebSocketException, DataAccessException, IOException {
        int gameID = command.getGameID();
        dbobjects.GameData gameData = dataaccess.GameDataAccess.getGameObject(gameID);
        String playerName = dataaccess.AuthDataAccess.getUsername(command.getAuthToken());
        if(playerName == null) {
            throw new WebSocketException("Unable to find player name associated with token: " + command.getAuthToken());
        }

        ChessGame game;
        game = gameData.game();
        if(game == null) {
            throw new SQLException("unable to get game from DB");
        }
        if(game.isResigned()) {
            String message = String.format("this game has already been resigned by team %s", game.getTeamTurn().toString());
            sendError(session, message);
            return;
        }
        if(!(gameData.whiteUsername().equals(playerName) || gameData.blackUsername().equals(playerName))) {
            String message = "Only players may resign";
            sendError(session, message);
            return;
        }
        game.resign();
        String newGame = game.serialize();
        if(!dataaccess.GameDataAccess.updateGame(gameID, newGame)) {
            throw new WebSocketException("The game in the DB couldn't be updated.");
        }
        String message = String.format("Player %s has resigned", playerName);
        broadcast(gameID, message);
    }

    private class ErrorJson {
        ServerMessage.ServerMessageType serverMessageType;
        String errorMessage;

        ErrorJson(ServerMessage.ServerMessageType type, String message) {
            this.serverMessageType = type;
            this.errorMessage = message;
        }
    }

    private void sendError(Session session, String message) throws IOException {
        ErrorJson j = new ErrorJson(ServerMessage.ServerMessageType.ERROR, message);
        Gson g = new Gson();
        session.getRemote().sendString(g.toJson(j));
    }

    private class NotificationJson {
        ServerMessage.ServerMessageType serverMessageType;
        String message;
        NotificationJson(ServerMessage.ServerMessageType type, String message) {
            this.serverMessageType = type;
            this.message = message;
        }
    }

    private void broadcast(Session session, int gameID, String message) throws IOException {
        ServerMessage announcement = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        ConcurrentHashMap<Integer, Session> map = GAMES.get(gameID);
        for(Session s : map.values()) {
            if(s.hashCode() == session.hashCode()) {
                continue;
            }
            NotificationJson n = new NotificationJson(ServerMessage.ServerMessageType.NOTIFICATION, message);
            Gson g = new Gson();
            String json = g.toJson(n);
            s.getRemote().sendString(json);
        }
    }

    private void broadcast(int gameID, String message) throws IOException {
        ServerMessage announcement = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        NotificationJson n = new NotificationJson(ServerMessage.ServerMessageType.NOTIFICATION, message);
        Gson g = new Gson();
        for(Session s : GAMES.get(gameID).values()) {
            String json = g.toJson(n);
            s.getRemote().sendString(json);
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

    private void touchOne(int hash, int gameID) throws IOException, SQLException {
        Session s = GAMES.get(gameID).get(hash);
        ChessGame game = dataaccess.GameDataAccess.getGame(gameID);
        Gson g = new Gson();
        TouchJson n = new TouchJson(ServerMessage.ServerMessageType.LOAD_GAME, game.serialize());
        String json = g.toJson(n);
        s.getRemote().sendString(json);
    }

    private void touchGame(int gameID) throws IOException, SQLException {
        ChessGame game = dataaccess.GameDataAccess.getGame(gameID);
        ServerMessage touch = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
        Gson g = new Gson();
        TouchJson n = new TouchJson(ServerMessage.ServerMessageType.LOAD_GAME, game.serialize());
        for(Session s : GAMES.get(gameID).values()) {
            String json = g.toJson(n);
            s.getRemote().sendString(json);
        }
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

    private class MoveJson extends CommandJson {
        chess.ChessMove move;

        MoveJson(UserGameCommand.CommandType type, String token, Integer id, chess.ChessMove move) {
            super(type, token, id);
            this.move = move;
        }
    }

    private UserGameCommand deSerialize(String message) {
        String first[] = message.split(",");
        Gson g = new Gson();

        if(first[0].contains("CONNECT")) {
            CommandJson j = g.fromJson(message, CommandJson.class);
            UserGameCommand command = new UserGameCommand(j.commandType, j.authToken, j.gameID);
            return command;
        }
        if(first[0].contains("MAKE_MOVE")) {
            MoveJson j = g.fromJson(message, MoveJson.class);
            UserGameCommand command = new UserGameCommand(j.commandType, j.authToken, j.gameID, j.move);
            return command;
        }
        if(first[0].contains("LEAVE")) {
            CommandJson j = g.fromJson(message, CommandJson.class);
            UserGameCommand command = new UserGameCommand(j.commandType, j.authToken, j.gameID);
            return command;
        }
        if(first[0].contains("RESIGN")) {
            CommandJson j = g.fromJson(message, CommandJson.class);
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
