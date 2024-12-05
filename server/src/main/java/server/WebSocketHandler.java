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

@WebSocket
public class WebSocketHandler {
    private static final String USER_COMMANDS[] = {"CONNECT", "MAKE_MOVE", "LEAVE", "RESIGN"};
    private static final ConcurrentHashMap<Integer, HashSet<Session>> GAMES = new ConcurrentHashMap<Integer, HashSet<Session>>();
    private static final ConcurrentHashMap<Session, ChessGame.TeamColor> COLORS = new ConcurrentHashMap<Session, ChessGame.TeamColor>();
    private static final ConcurrentHashMap<Session, String> AUTHS = new ConcurrentHashMap<Session, String>();

    public WebSocketHandler() {
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message)  throws IOException, WebSocketException, SQLException, DataAccessException {
        trimSessions();
        Object raw = deSerialize(message);
        if(raw instanceof UserGameCommand) {
            parseCommand(session, (UserGameCommand) raw);
        }
        else {
            throw new WebSocketException("Unknown command");
        }


        /*
        //delete
        System.out.printf("Recieved %s", message);
        session.getRemote().sendString("WebSocket response: " + message);
        //Add logic to intepret what type of message has just been recieved
        */
    }

    private void trimSessions() {
        String playerName;
        for(Integer i : GAMES.keySet()) {
            for(Session s : GAMES.get(i)) {
                if(!s.isOpen()) {
                    try {
                        playerName = dataaccess.AuthDataAccess.getUsername(AUTHS.get(s));
                        dataaccess.GameDataAccess.LeaveGame(COLORS.get(s), i);
                        AUTHS.remove(s);
                        COLORS.remove(s);
                        GAMES.get(i).remove(s);
                        String message = String.format("Player %s has lost connection", playerName);
                        broadcast(i, message);
                    }
                    catch(Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }

    private void parseCommand(Session session, UserGameCommand command) throws WebSocketException, SQLException, DataAccessException, IOException {
        try {
            switch(command.getCommandType()) {
                case CONNECT:
                    joinGame(session, command);
                    break;
                case MAKE_MOVE:
                    makeMove(command);
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
            System.out.println(e.getMessage());
        }
        catch(SQLException e) {
            throw e;
        }
        catch(IOException e) {
            throw e;
        }
    }

    private void joinGame(Session session, UserGameCommand command) throws WebSocketException, SQLException, DataAccessException, IOException {
        int gameID = command.getGameID();
        if(!GAMES.containsKey(gameID)) {
            GAMES.put(gameID, new HashSet<Session>());
        }
        GAMES.get(gameID).add(session);

        int team = command.getTeam();
        String playerName = dataaccess.AuthDataAccess.getUsername(command.getAuthToken());
        if(playerName == null) {
            throw new WebSocketException("Unable to find player name associated with token: " + command.getAuthToken());
        }
        String message;

        if(team > 0) {  //white
            COLORS.put(session, ChessGame.TeamColor.WHITE);
            message = String.format("%s has joined team white", playerName);
        }
        else if(team < 0) { //spectator
            message = String.format("%s has joined as a spectator", playerName);
        }
        else {  //black
            COLORS.put(session, ChessGame.TeamColor.WHITE);
            message = String.format("%s has joined team black", playerName);
        }
        AUTHS.put(session, command.getAuthToken());

        broadcast(gameID, message);
    }

    private void makeMove(UserGameCommand command) throws WebSocketException, SQLException, DataAccessException, IOException {
        int gameID = command.getGameID();
        String newGame = command.getNewGame();
        if(!dataaccess.GameDataAccess.updateGame(gameID, newGame)) {
            throw new WebSocketException("The game in the DB couldn't be updated.");
        }
        ChessGame game = ChessGame.deSerialize(newGame);
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
        COLORS.remove(session);
        AUTHS.remove(session);
        int gameID = command.getGameID();
        HashSet<Session> sessions = GAMES.get(gameID);
        if(sessions == null) {
            throw new WebSocketException("No sessions were found associated with gameID: " + gameID);
        }

        if(!sessions.remove(session)) {
            throw new WebSocketException("The player cannot leave a game they haven't joined");
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

        String newGame = command.getNewGame();
        if(!dataaccess.GameDataAccess.updateGame(gameID, newGame)) {
            throw new WebSocketException("The game in the DB couldn't be updated.");
        }
        touchGame(gameID);

        String message = String.format("Player %s has resigned", playerName);
        broadcast(gameID, message);
    }

    private void broadcast(int gameID, String message) throws IOException {
        ServerMessage announcement = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message, null);
        for(Session s : GAMES.get(gameID)) {
            s.getRemote().sendString(announcement.serialize());
        }
    }

    private void touchGame(int gameID) throws IOException, SQLException {
        ChessGame game = dataaccess.GameDataAccess.getGame(gameID);
        ServerMessage touch = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, null, game);
        for(Session s : GAMES.get(gameID)) {
            s.getRemote().sendString(touch.serialize());
        }
    }

    private Object deSerialize(String message) {
        String first[] = message.split("\t");
        Object command;

        if(Arrays.asList(USER_COMMANDS).contains(first[0])) {
            command = UserGameCommand.deSerialize(message);
        }
        else {
            command = null;
        }

        return command;
    }

    @SuppressWarnings("serial")
    private class WebSocketException extends Exception {
        WebSocketException(String message) {super(message);}
    }
}
