package ui;

import java.util.Scanner;
import facade.ServerFacade;
import facade.ResponseException;
import java.util.HashMap;
import chess.ChessGame;
import java.util.ArrayList;
import facade.ChessGameRecord;

public class UI {
    private static String header;
    private static ServerFacade facade;
    private static String authToken;
    private static String username;
    private static Scanner s;
    private static boolean isLoggedIn = false;
    private static boolean quit = false;
    private static final HashMap<String, ChessGameRecord> gamesMap = new HashMap<String, ChessGameRecord>();
    private static final HashMap<String, Integer> indexMap = new HashMap<String, Integer>();
    private static int listIndex = 1;


    public static void run(String url) {
        header = "[Logged out]$ "; 
        facade = new ServerFacade(url);
        s = new Scanner(System.in);

        while(!quit) {

            try {
                while(!isLoggedIn && !quit) {
                    preLog();
                }

                while(isLoggedIn && !quit) {
                    postLog();
                }
            }
            catch(Exception e) {
                if(handleException(e)) {
                    return;
                }
            }
        } return;
    }

    private static boolean handleException(Exception e) {
        if(e instanceof ResponseException) {
            ResponseException re = (ResponseException) e;
            switch(re.getStatus()) {
                case 400:
                    System.out.println("A problem occurred. Try loggin out and back in.");
                    return false;
                case 401:
                    System.out.println("A problem occurred. Try creating a new account.");
                    return false;
                case 403:
                    System.out.println("A problem occurred. Username or game name already taken");
                    return false;
                case 500:
                    System.out.println("A server error occurred.");
                    return true;
                default:
                    System.out.println("An unknonwn error occurred.");
                    return true;
            }
        }
        System.out.println("An unknonwn error occurred.");
        return true;
    }

    private static void drawPreLog() {
        StringBuilder sb = new StringBuilder();
        sb.append("register <username> <password> <email>\n");
        sb.append("login <username> <password>\n");
        sb.append("quit\n");
        sb.append("help");
        System.out.println(sb.toString());
    }

    private static void preLog() throws Exception {
        String input[];

            System.out.print(header);
            input = s.nextLine().split(" ");
            if(input.length == 0) {
                System.out.println("Invalid command");
                drawPreLog();
                return;
            }

        switch(input[0]) {
            case "register":
                if(input.length < 4) {
                    System.out.println("Invalid command");
                    drawPreLog();
                }
                String username = input[1];
                String password = input[2];
                String email = input[3];
                if(facade.registration(username, password, email) == null) {
                    System.out.println("Invalid command");
                    drawPreLog();
                    return;
                }
                System.out.println("Registration Successful");
                return;

            case "login":
                if(input.length < 3) {
                    System.out.println("Invalid command");
                    drawPreLog();
                }
                String usernameL = input[1];
                String passwordL = input[2];
                try {
                    String newAuth = facade.login(usernameL, passwordL);
                    if(newAuth == null) {
                        throw new ResponseException(500, "A problem occurred loging in");
                    }
                    authToken = newAuth;
                    username = usernameL;
                    header = "[" + username + "]$ ";
                }
                catch(ResponseException e) {
                    if(e.getStatus() == 401) {
                        System.out.println("Incorrect username or password");
                        return;
                    }
                    else {
                        throw e;
                    }
                }

                isLoggedIn = true;
                System.out.println("Successfully logged in");
                drawPostLog();
                return;

            case "quit":
                System.out.println("Bye-bye");
                quit = true;
                return;

            case "help":
                drawPreLog();
                break;
            default:
                System.out.println("Invalid command");
                drawPreLog();
                return;
        }
    }

    private static void drawPostLog() {
        StringBuilder sb = new StringBuilder();
        sb.append("create <name>\n");
        sb.append("list\n");
        sb.append("join <id>\n");
        sb.append("logout\n");
        sb.append("quit\n");
        sb.append("help");
        System.out.println(sb.toString());
        return;
    }

    private static void postLog() throws Exception {
        String input[];

        updateGameList();
        System.out.print(header);
        input = s.nextLine().split(" ");
        if(input.length == 0) {
            System.out.println("Invalid command");
            drawPostLog();
            return;
        }

        switch(input[0]) {
            case "create":
                if(input.length < 2) {
                    System.out.println("Invalid command");
                    drawPostLog();
                }
                String gameName = input[1];
                String newGameID = facade.createGame(authToken, gameName);
                System.out.println("Created game: " + gameName);
                return;

            case "list":
                updateGameList();
                printGameList();
                return;
            case "join":
                if(input.length < 3) {
                    System.out.println("Invalid command");
                    drawPostLog();
                }
                ChessGame.TeamColor color;
                if(input[1].equalsIgnoreCase("white")) {
                    color = ChessGame.TeamColor.WHITE;
                }
                else if(input[1].equalsIgnoreCase("black")) {
                    color = ChessGame.TeamColor.BLACK;
                }
                else {
                    System.out.println("Invalid color. choose \'white\' or \'black\'");
                    return;
                }

                Integer gameIDInt = indexMap.get(input[2]);
                if(gameIDInt == null) {
                    System.out.println("Invalid game id. type 'list' to see available options");
                    return;
                }
                ChessGameRecord r = gamesMap.get(gameIDInt.toString());
                if(r == null) {
                    System.out.println("Invalid game id. type 'list' to see available options");
                    return;
                }
                if(facade.joinGame(authToken, color, r.gameID())) {
                    Game.playGame(r.game(), color);
                }
                else {
                    System.out.println("There was a problem joining the game. This shouldn't happen");
                    return;
                }

                return;
            case "logout":
                facade.logout(authToken);
                authToken = "";
                isLoggedIn = false;
                header = "[Logged out]$ "; 
                return;
            case "quit":
                facade.logout(authToken);
                System.out.println("Bye-bye");
                quit = true;
                return;
            case "help":
                drawPostLog();
                return;
            default:
                System.out.println("Invalid command");
                drawPostLog();
                return;
        }
    }

    private static void updateGameList() throws Exception {
        ArrayList<ChessGameRecord> gameList = facade.listGames(authToken);
        ArrayList<String> keysFromDB = new ArrayList<String>();

        for(ChessGameRecord r : gameList) {
            if(indexMap.get(r.gameID()) == null) {
                indexMap.put(r.gameID(), listIndex++);
            }
            gamesMap.put(r.gameID(), r);
            keysFromDB.add(r.gameID());
        }

        for(String i : gamesMap.keySet()) {
            if(!keysFromDB.contains(i)) {
                gamesMap.remove(i);
            }
        }
    }

    private static void printGameList() {
        StringBuilder sb = new StringBuilder();

        for(ChessGameRecord r : gamesMap.values()) {
            sb.append("++++++++++++++++\n");
            sb.append(indexMap.get(r.gameID()));
            sb.append(String.format("Game name: %s\n", r.gameName()));
            sb.append("Current turn: ");
            if(r.game().getTeamTurn() == ChessGame.TeamColor.WHITE) {
                sb.append("white\n");
            }
            else {
                sb.append("black\n");
            }
            sb.append("player white: ");
            if(r.whiteUsername() == null) {
                sb.append("none\n");
            }
            else {
                sb.append(String.format("%s\n", r.whiteUsername()));
            }
            sb.append("player black: ");
            if(r.blackUsername() == null) {
                sb.append("none\n");
            }
            else {
                sb.append(String.format("%s\n", r.blackUsername()));
            }
            sb.append(r.game().getBoard().toString());
            sb.append('\n');
        }
        System.out.println(sb.toString());
    }

}
