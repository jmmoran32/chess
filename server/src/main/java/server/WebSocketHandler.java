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

@WebSocket
public class WebSocketHandler {
    private static final String USER_COMMANDS[] = {"CONNECT", "MAKE_MOVE", "LEAVE", "RESIGN"};
    private static final ConcurrentHashMap<Integer, HashSet<Session>> GAMES = new ConcurrentHashMap<Integer, HashSet<Session>>();

    public WebSocketHandler() {
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message)  throws IOException, WebSocketException, SQLException, DataAccessException {
        Object raw = deSerialize(message);
        if(raw instanceof ServerMessage) {
           parseMessage(session, (ServerMessage) raw); 
        }
        else if(raw instanceof UserGameCommand) {
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

    private void parseMessage(Session session, ServerMessage message) {}

    private void parseCommand(Session session, UserGameCommand command) throws WebSocketException, SQLException, DataAccessException {
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
            throw e;
        }
        catch(SQLException e) {
            throw e;
        }
        catch(Exception e) {
            throw e;
        }
    }

    private void joinGame(Session session, UserGameCommand command) throws WebSocketException, SQLException, DataAccessException {
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
            message = String.format("%s has joined team white", playerName);
        }
        else if(team < 0) { //spectator
            message = String.format("%s has joined as a spectator", playerName);
        }
        else {  //black
            message = String.format("%s has joined team black", playerName);
        }

        broadcast(gameID, message);
    }

    private void makeMove(UserGameCommand command) throws WebSocketException, SQLException, DataAccessException {
        int gameID = command.getGameID();
        String newGame = command.getNewGame();
        if(!dataaccess.GameDataAccess.updateGame(gameID, newGame)) {
            throw new WebSocketException("The game in the DB couldn't be updated.");
        }
        touchGame(gameID);
    }

    private void leave(Session session, UserGameCommand command) throws WebSocketException, SQLException, DataAccessException {
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

    private void resign(Session session, UserGameCommand command) throws SQLException, WebSocketException, DataAccessException {
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

    private void broadcast(int gameID, String message) {
        //TODO: make this send a notification to all players in the given game
        for(Session s : GAMES.get(gameID)) {
        }
    }

    private void touchGame(int gameID) {
        //TODO: sends a signal to all joined players to update their game
        for(Session s : GAMES.get(gameID)) {
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
