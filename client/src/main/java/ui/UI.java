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
    private static final HashMap<String, ChessGame> gamesMap = new HashMap<String, ChessGame>();

    private static final String WHITE_TILE = "43m";
    private static final String BLACK_TILE = "45m";
    private static final String WHITE_PIECE = "32;";
    private static final String BLACK_PIECE = "31;";
    private static String WHITE_ON_WHITE;
    private static String WHITE_ON_BLACK;
    private static String BLACK_ON_WHITE;
    private static String BLACK_ON_BLACK;

    public static void run(String url) {
        //WHITE_ON_BLACK = "\033[" + WHITE_PIECE + BLACK_TILE + "m%c";
        //BLACK_ON_BLACK = "\033[" + BLACK_PIECE + BLACK_TILE + "m%c";
        //WHITE_ON_WHITE = "\033[" + WHITE_PIECE + WHITE_TILE + "m%c";
        //BLACK_ON_WHITE = "\033[" + BLACK_PIECE + WHITE_TILE + "m%c";
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

        System.out.print(header);
        input = s.nextLine().split(" ");
        if(input.length == 0) {
            System.out.println("Invalid command");
            drawPreLog();
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
                return;
            case "join":
                return;
            case "logout":
                return;
            case "quit":
                return;
            case "help":
                return;
            default:
                return;
        }
    }

    private static void updateGameList() throws Exception {
        ArrayList<ChessGameRecord> gameList = facade.listGames(authToken);
        ArrayList<String> keysFromDB = new ArrayList<String>();

        for(ChessGameRecord r : gameList) {
            gamesMap.put(r.gameID(), r.game());
            keysFromDB.add(r.gameID());
        }

        for(String i : gamesMap.keySet()) {
            if(!keysFromDB.contains(i)) {
                gamesMap.remove(i);
            }
        }
    }

    private static void printGameList() {
    }

    private String[] formattedBoard(ChessGame game) {
        char[] whiteBoard = new char[64];
        char[] blackBoard;
        char[] bucketBoard;
        char bucketChar;
        String bucketTile;
        String bucketPiece;
        
        whiteBoard = game.getBoard().serialize().toCharArray();
        if(game.getTeamTurn() == ChessGame.TeamColor.BLACK) {
            blackBoard = new char[64];
            int sum;
            for(int i = 0; i < 64; i++) {
                blackBoard[i] = whiteBoard[63 - i];
            }
            bucketBoard = blackBoard;
        }
        else {
            bucketBoard = whiteBoard;
        }
        StringBuilder sb = new StringBuilder();
        String board[] = new String[8];
        boolean whitetile = true;
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                bucketChar = bucketBoard[i * 8 + j];
                if(bucketChar < 91) {
                    bucketPiece = WHITE_PIECE;
                }
                else {
                    bucketPiece = BLACK_PIECE;
                }
                if(whitetile) {
                    bucketTile = WHITE_TILE;
                }
                else {
                    bucketTile = BLACK_TILE;
                }
                sb.append("\033[");
                sb.append(bucketPiece);
                sb.append(bucketTile);
                sb.append(bucketChar);
            }
            sb.append("\033[0m\n");
            board[i] = sb.toString();
            sb = new StringBuilder();
            whitetile ^= true;
        }
        return board;
    }
}
