package ui;

import java.util.Scanner;
import facade.ServerFacade;
import facade.ResponseException;
import java.util.concurrent.ConcurrentHashMap;
import chess.ChessGame;
import java.util.ArrayList;
import facade.ChessGameRecord;
import ws.WebSocketFacade;

public class UI {
    private static String header;
    private static ServerFacade facade;
    private static String authToken;
    private static String username;
    private static Scanner s;
    private static boolean isLoggedIn = false;
    private static boolean quit = false;
    private static final ConcurrentHashMap<String, ChessGameRecord> GAMES_MAP = new ConcurrentHashMap<String, ChessGameRecord>();
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
        System.out.println("An uncaught error occurred.");
        System.out.println(e.getMessage());
        System.out.println("Remember to delete that dump");
        return true;
    }

    private static void drawPreLog() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nregister <username> <password> <email>\n");
        sb.append("login <username> <password>\n");
        sb.append("quit\n");
        sb.append("help\n");
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
                    return;
                }
                String username = input[1];
                String password = input[2];
                String email = input[3];
                String regAuthToken = facade.registration(username, password, email);
                if(regAuthToken == null) {
                    System.out.println("Invalid command");
                    drawPreLog();
                    return;
                }
                System.out.println("Registration Successful");
                UI.authToken = regAuthToken;
                UI.isLoggedIn = true;
                header = "[" + username + "]$ ";

                return;

            case "login":
                if(input.length < 3) {
                    System.out.println("Invalid command");
                    drawPreLog();
                    return;
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
        sb.append("\ncreate <name>\n");
        sb.append("list\n");
        sb.append("join <id> <team>\n");
        sb.append("spectate <id>\n");
        sb.append("logout\n");
        sb.append("quit\n");
        sb.append("\nhelp");
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
                    return;
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
                    return;
                }
                ChessGame.TeamColor color;
                if(input[2].equalsIgnoreCase("white")) {
                    color = ChessGame.TeamColor.WHITE;
                }
                else if(input[2].equalsIgnoreCase("black")) {
                    color = ChessGame.TeamColor.BLACK;
                }
                else {
                    System.out.println("Invalid color. choose \'white\' or \'black\'");
                    return;
                }

                String index = input [1];
                ChessGameRecord r = GAMES_MAP.get(index);
                if(r == null) {
                    System.out.println("Invalid game id. type 'list' to see available options");
                    return;
                }
                if(facade.joinGame(authToken, color, r.gameID())) {
                    Game.playGame(r.game(), Integer.parseInt(r.gameID()), color, UI.authToken);
                }
                else {
                    System.out.println("There was a problem joining the game. This shouldn't happen");
                    return;
                }

                return;
            case "spectate":
                if(input.length < 2) {
                    System.out.println("Invalid command");
                    drawPostLog();
                    return;
                }

                String specIndex = input[1];
                ChessGameRecord specR = GAMES_MAP.get(specIndex);
                if(specR == null) {
                    System.out.println("Invalid game id. type 'list' to see available options");
                    return;
                }

                Game.spectate(specR.game());
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
        for(ChessGameRecord r : gameList) {
            String retrievedIndex = gamesMapContains(r.gameID());;
            if(retrievedIndex != null) {
                GAMES_MAP.put(retrievedIndex, r);
            }
            else {
                GAMES_MAP.put(Integer.toString(listIndex++), r);
            }
        }

        ArrayList<String> toBeRemoved = new ArrayList<String>();
        for(String i : GAMES_MAP.keySet()) {
            if(!gameListContainsID(gameList, i)) {
                toBeRemoved.add(i);
            }
        }

        for(String i : toBeRemoved) {
            GAMES_MAP.remove(i);
        }
    }

    private static boolean gameListContainsID(ArrayList<ChessGameRecord> gameList, String key) {
        String stupidId = GAMES_MAP.get(key).gameID();
        for(ChessGameRecord r : gameList) {
            if(r.gameID().equals(stupidId)) {
                return true;
            }
        }
        return false;
    }

    private static String gamesMapContains(String stupidId) {
        ChessGameRecord r;
        for(String i : GAMES_MAP.keySet()) {
            r = GAMES_MAP.get(i);
            if(r.gameID().equals(stupidId)) {
                return i;
            }
        }
        return null;
    }


    private static void printGameList() {
        StringBuilder sb = new StringBuilder();

        for(String i : GAMES_MAP.keySet()) {
            ChessGameRecord r = GAMES_MAP.get(i);
            sb.append("++++++++++++++++\n");
            sb.append(i);
            sb.append(String.format("\nGame name: %s\n", r.gameName()));
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
