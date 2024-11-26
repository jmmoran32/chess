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

@WebSocket
public class WebSocketHandler {
    private static final String SERVER_MESSAGES[] = {"LOAD_GAME", "ERROR", "NOTIFICATION"};
    private static final String USER_COMMANDS[] = {"CONNECT", "MAKE_MOVE", "LEAVE", "RESIGN"};
    private static final ConcurrentHashMap<Integer, HashSet<Session>> GAMES = new ConcurrentHashMap<Integer, HashSet<Session>>();

    public WebSocketHandler() {
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message)  throws IOException {
        Object raw = deSerialize(message);
        if(raw instanceof ServerMessage) {
           parseMessage(session, (ServerMessage) raw); 
        }
        else if(raw instanceof UserGameCommand) {
            parseCommand(session, (UserGameCommand) raw);
        }
        else {
        }


        /*
        //delete
        System.out.printf("Recieved %s", message);
        session.getRemote().sendString("WebSocket response: " + message);
        //Add logic to intepret what type of message has just been recieved
        */
    }

    private void parseMessage(Session session, ServerMessage message) {}

    private void parseCommand(Session session, UserGameCommand command) {
        switch(command.getCommandType()) {
            case CONNECT:
                joinGame(session, command);
                break;
            case MAKE_MOVE:
                break;
            case LEAVE:
                break;
            case RESIGN:
                break;
        }
    }

    private void joinGame(Session session, UserGameCommand command) {
        int gameID = command.getGameID();
        if(!GAMES.containsKey(gameID)) {
            GAMES.put(gameID, new HashSet<Session>());
        }
        GAMES.get(gameID).add(session);

        int team = command.getTeam();
        String playerName = dataaccess.AuthDataAccess.getUsername(command.getAuthToken());
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

    private void broadcast(int gameID, String message) throws IOException {
        //TODO: make this send a notification to all players in the given game
        for(Session s : GAMES.get(gameID)) {
        }
    }

    private Object deSerialize(String message) {
        String first[] = message.split("\t");
        Object command;

        if(Arrays.asList(SERVER_MESSAGES).contains(first[0])) {
            command = ServerMessage.deSerialize(message);
        }
        else if(Arrays.asList(USER_COMMANDS).contains(first[0])) {
            command = UserGameCommand.deSerialize(message);
        }
        else {
            command = null;
        }

        return command;
    }
}
