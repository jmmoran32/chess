package ui;

import chess.ChessGame;
import java.util.Scanner;
import websocket.commands.UserGameCommand;
import ws.WebSocketFacade;
import chess.*;
import java.util.ArrayList;

public class Game {
    private static final String WHITE_TILE = "43m";
    private static final String BLACK_TILE = "45m";
    private static final String WHITE_PIECE = "32;";
    private static final String BLACK_PIECE = "31;";
    private static final String HIGHLIGHT = "5m";
    private static boolean quit = false;
    private static ChessGame game;
    private static int gameID;
    private static String authToken;
    private static ChessGame.TeamColor color;
    private static WebSocketFacade WS;
    public static Scanner s = new Scanner(System.in);

    public static void playGame(ChessGame game, int gameID, ChessGame.TeamColor playerColor, String authToken) {
        Game.game = game;
        Game.gameID = gameID;
        Game.color = playerColor;
        Game.authToken = authToken;
        Game.WS = WS;
        try {
            WS = new WebSocketFacade("ws://localhost:8080/ws");
        }
        catch (Exception e) {
            System.out.println("Could not initialize websocket");
            return;
        }
        while(!quit) {
            takeTurn();
        }
        quit = false;
        return;
    }

    public static void spectate(ChessGame game) {
        Game.game = game;
        Game.color = ChessGame.TeamColor.WHITE;
        while(!quit) {
            awaitTurn();
        }
        quit = false;
        return;
    }

    private static void takeTurn() {
        String team;
        if(color == ChessGame.TeamColor.WHITE) {
            team = "Player white: ";
        }
        else {
            team = "Player black: ";
        }
        drawBoard();
        String input[];
        while(!quit) {
            System.out.print(team);
            input = s.nextLine().split(" ");
            switch(input[0]) {
                case "h":
                    drawHelp();
                    break;
                case "rd":
                    drawBoard();
                    break;
                case "l":
                    leave();
                    quit = true;
                    break;
                case "m":
                    if(input.length < 3) {
                        System.out.println("Invalid command");
                        drawHelp();
                        break;
                    }
                    String promotion = null;
                    if(input.length > 3) {
                        promotion = input[3];
                    }
                    move(input[1], input[2], input[3]);
                    break;
                case "r":
                    resign();
                    break;
                case "hi":
                    if(input.length < 2) {
                        System.out.println("Invalid command");
                        drawHelp();
                        break;
                    }
                    highlight();
                    break;
                default:
                    System.out.println("Invalid command");
                    drawHelp();
            }
        }
        return;
    }

    private static void move(String fromStr, String toStr, String promotionStr) {
        if(Game.game.getTeamTurn() != Game.color) {
            System.out.println("It is not your turn yet!");
            return;
        }
        char from[] = fromStr.toCharArray();
        char to[] = toStr.toCharArray();
        if(from.length < 2 || to.length < 2) {
            System.out.println("Invalid move or position\n\tm [1-8|[a-h] [1-8]|[a-h] [r,n,b,q]");
        }
        if(from[0] < '1' || from[0] > '8') {
            System.out.println("Invalid move or position\n\tm [1-8|[a-h] [1-8]|[a-h] [r,n,b,q]");
            return;
        }
        else if(from[1] < 'a' || from[1] < 'h') {
            System.out.println("Invalid move or position\n\tm [1-8|[a-h] [1-8]|[a-h] [r,n,b,q]");
            return;
        }
        if(to[0] < '1' || to[0] > '8') {
            System.out.println("Invalid move or position\n\tm [1-8|[a-h] [1-8]|[a-h] [r,n,b,q]");
            return;
        }
        else if(to[1] < 'a' || to[1] < 'h') {
            System.out.println("Invalid move or position\n\tm [1-8|[a-h] [1-8]|[a-h] [r,n,b,q]");
            return;
        }

        ChessPosition fromPos = new ChessPosition(from[0] - '0', from[1] - '`');
        ChessPosition toPos = new ChessPosition(to[0] - '0', to[1] - '`');
        ChessPiece.PieceType type;
        switch(promotionStr) {
            case "r":
                type = ChessPiece.PieceType.ROOK;
                break;
            case "n":
                type = ChessPiece.PieceType.KNIGHT;
                break;
            case "b":
                type = ChessPiece.PieceType.BISHOP;
                break;
            case "q":
                type = ChessPiece.PieceType.QUEEN;
            case null:
                type = null;
            default:
            System.out.println("Invalid move or position\n\tm [1-8|[a-h] [1-8]|[a-h] [r,n,b,q]");
            return;
        }

        try {
            Game.game.makeMove(new ChessMove(fromPos, toPos, type));
        }
        catch(InvalidMoveException e) {
            System.out.println(e.getMessage());
            return;
        }
        
        UserGameCommand newMove = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, Game.authToken, Game.gameID, Game.game.serialize(), Game.color == ChessGame.TeamColor.WHITE ? 1 : 0);

        WS.sendCommand(newMove);
        Thread.sleep(100);
        return;
        /*
        if(Game.game.getTeamTurn() != Game.color) {
            System.out.println("Wait your turn");
            return;
        }
                    char from[] = fromStr.toCharArray();
                    char to[] = toStr.toCharArray();
                    if(from.length > 2 || to.length > 2) {
                        System.out.println("Invalid move or position\n\tm [1-8|[a-h] [1-8]|[a-h] [r,n,b,q]");
                        return;
                    }
                    if(from[0] < '1' || from[0] > '8') {
                        System.out.println("Invalid move or position\n\tm [1-8|[a-h] [1-8]|[a-h] [r,n,b,q]");
                        return;
                    }
                    else if(from[1] < 'a' || from[1] < 'h') {
                        System.out.println("Invalid move or position\n\tm [1-8|[a-h] [1-8]|[a-h] [r,n,b,q]");
                        return;
                    }
                    ChessPosition selected = new ChessPosition(from[0] - '0', from[1] - '`');
                    ArrayList<ChessMove> validMoves = (ArrayList<ChessMove>) Game.game.validMoves(selected);
        ChessPosition target = new ChessPosition(to[0] - '0', to[1] - '`');
        //TODO: add promotion to move
        if(!validMoves.contains(proposition)) {
            System.out.println("Invalid move. To see valid moves, type: hi <position>");
            return;
        }
        */
    }

    private static void leave() {
        UserGameCommand leaveCommand = new UserGameCommand(UserGameCommand.CommandType.LEAVE, Game.authToken, Game.gameID, null, null);
        WS.sendCommand(leaveCommand);
    }

    private static void drawHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nh\thelp\n\t\tdisplay this message\n");
        sb.append("rd\tredraw board\n\t\trefresh the board\n");
        sb.append("l\tleave\n\t\tleave the game\n");
        sb.append("m <position> <position>\tmove\n\t\tmove a piece from one position to another\n");
        sb.append("r\tresign\n\t\tforfeight the game in your opponent's favor\n");
        sb.append("hi <position>\thighlight legal moves\n\t\tshows what moves a given piece may make\n");
        System.out.println(sb.toString());
    }

    private static void awaitTurn() {
        drawBoard();
        System.out.print("This part of the game has not been implemented yet. Press any key to exit: ");
        s.nextLine();
        quit = true;
        return;
    }

    private static void drawBoard() {
        StringBuilder sb = new StringBuilder();
        int margin;
        String footer;

        if(color == ChessGame.TeamColor.WHITE) {
            footer = " abcdefgh \n";
        }
        else {
            footer = " hgfedcba \n";
        }

        if(color == ChessGame.TeamColor.WHITE) {
            margin = 8;
        }
        else {
            margin = 1;
        }

        sb.append("\n\n");
        for(String row : formattedBoard()) {
            if(color == ChessGame.TeamColor.WHITE) {
                sb.append(margin--);
            }
            else {
                sb.append(margin++);
            }
            sb.append(row);
        }
        sb.append(footer);
        System.out.println(sb.toString());
    }

    private static String[] formattedBoard() {
        char[] whiteBoard = new char[64];
        char[] blackBoard;
        char[] bucketBoard;
        char bucketChar;
        String bucketTile;
        String bucketPiece;
        
        whiteBoard = game.getBoard().serialize().toCharArray();
        if(Game.color == ChessGame.TeamColor.BLACK) {
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

                whitetile ^= true;
            }
            sb.append("\033[0m\n");
            board[i] = sb.toString();
            sb = new StringBuilder();
            whitetile ^= true;
        }
        return board;
    }

    public static void notify(String message) {
        System.out.println(message);
    }

    public static void updateGame(String serial) {
        Game.game = chess.ChessGame.deSerialize(serial);
    }

    public static void touchBoard(String serial) {
        updateGame(serial);
        drawBoard();
    }
}
