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
    private static final String HIGHLIGHT = "44m";
    private static final String WHITE_PIECE = "32;";
    private static final String BLACK_PIECE = "31;";
    private static boolean quit = false;
    private static ChessGame game;
    private static int gameID;
    private static String authToken;
    private static ChessGame.TeamColor color;
    private static boolean isSpectator;
    private static WebSocketFacade ws;
    public static Scanner s = new Scanner(System.in);

    public static void playGame(ChessGame game, int gameID, ChessGame.TeamColor playerColor, String authToken) {
        Game.game = game;
        Game.gameID = gameID;
        Game.color = playerColor;
        Game.authToken = authToken;
        Game.isSpectator = false;
        try {
            ws = new WebSocketFacade("ws://localhost:8080/ws", Game.authToken);
        }
        catch (Exception e) {
            System.out.println("Could not initialize websocket");
            return;
        }
        try {
            UserGameCommand join = new UserGameCommand(UserGameCommand.CommandType.CONNECT, Game.authToken, Game.gameID);
            ws.sendCommand(join);
        }
        catch(Exception e) {
            System.out.println("Failed to join game: " + e.getMessage());
            return;
        }
            //drawBoard(null);
        while(!quit) {
            takeTurn();
        }
        quit = false;
        return;
    }

    public ChessGame.TeamColor getColor() {
        return Game.color;
    }

    public static void spectate(ChessGame game) {
        color = ChessGame.TeamColor.WHITE;
        Game.game = game;
        Game.isSpectator = true;
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
        String input[];
        while(!quit) {
            System.out.print(team);
            input = s.nextLine().split(" ");
            switch(input[0]) {
                case "h":
                    drawHelp();
                    break;
                case "rd":
                    drawBoard(null);
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
                    int status = move(input[1], input[2]);
                    if(status == 1 || status == 0) {
                        return;
                    }
                    if(status == 2) {
                        String from = input[1];
                        String to = input[2];
                        promotePrompt(from, to);
                    }
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
                    highlight(input[1]);
                    break;
                default:
                    System.out.println("Invalid command");
                    drawHelp();
                    return;
            }
        }
        return;
    }

    private static void promotePrompt(String from, String to) {
        while(true) {
                            System.out.print("Your pawn will be promoted. What piece should it be promoted to?: ");
                            String input[];
                            input = s.nextLine().split(" ");
                            if(input.length < 1) {
                                System.out.println("Invalid piece type. Type r, n, b, q, or x to cancel.");
                            }
                            if(input[0].equals("x")) {
                                return;
                            }
                            if(!promote(from, to, input[0])) {
                                System.out.println("Invalid piece type. Type r, n, b, q, or x to cancel.");
                            }
        }
    }

    private static void highlight(String fromStr) {
        char from[] = fromStr.toCharArray();
        if(from.length < 2) {
            System.out.println("Invalid move or position\n\tm [1-8|[a-h] [1-8]|[a-h] [r,n,b,q]");
        }
        if(from[0] < '1' || from[0] > '8') {
            System.out.println("Invalid move or position\n\tm [1-8|[a-h] [1-8]|[a-h] [r,n,b,q]");
            return;
        }
        if(from[1] < 'a' || from[1] > 'h') {
            System.out.println("Invalid move or position\n\tm [1-8|[a-h] [1-8]|[a-h] [r,n,b,q]");
            return;
        }

        ChessPosition fromPos = new ChessPosition(from[0] - '0', from[1] - '`');

        ArrayList<ChessPosition> to = new ArrayList<ChessPosition>();
        for(ChessMove m : Game.game.validMoves(fromPos)) {
            to.add(m.getEndPosition());
        }

        ArrayList<int[]> emEn = new ArrayList<int[]>();
        if(Game.color == ChessGame.TeamColor.WHITE) {
            for(int i = 0; i < to.size(); i++) {
                emEn.add(new int[2]);
                emEn.get(i)[0] = 8 - to.get(i).getRow();
                emEn.get(i)[1] = to.get(i).getColumn() - 1;
            }
        }
        else {
            for(int i = 0; i < to.size(); i++) {
                emEn.add(new int[2]);
                emEn.get(i)[0] = to.get(i).getRow() - 1;
                emEn.get(i)[1] =  8 - to.get(i).getColumn();
            }
        }

        drawBoard(emEn);
        return;
    }

    private static void resign() {
        if(Game.game.getTeamTurn() != Game.color) {
            System.out.println("It is not your turn yet!");
            return;
        }
        if(Game.game.isResigned()) {
            String player = Game.game.getTeamTurn() == ChessGame.TeamColor.WHITE ? "white" : "black";
            System.out.printf("Player %s has already resigned\n", player);
            return;
        }
        Game.game.resign();

        UserGameCommand resignedGame = new UserGameCommand(UserGameCommand.CommandType.RESIGN, Game.authToken, Game.gameID);
        Game.ws.sendCommand(resignedGame);
    }

    private static int move(String fromStr, String toStr) {
        if(Game.game.isResigned()) {
            String player = (Game.game.getTeamTurn() == ChessGame.TeamColor.WHITE ? "white" : "black");
            System.out.println(String.format("Player %s has resigned. no more moves may be made.", player));
            return 1;
        }

        if(Game.game.getTeamTurn() != Game.color) {
            System.out.println("It is not your turn yet!");
            return 1;
        }
        char from[] = fromStr.toCharArray();
        char to[] = toStr.toCharArray();
        if(from.length < 2 || to.length < 2) {
            System.out.println("Invalid move or position\n\tm [1-8|[a-h] [1-8]|[a-h]");
        }
        if(from[0] < '1' || from[0] > '8') {
            System.out.println("Invalid move or position\n\tm [1-8|[a-h] [1-8]|[a-h]");
            return 1;
        }
        if(from[1] < 'a' || from[1] > 'h') {
            System.out.println("Invalid move or position\n\tm [1-8|[a-h] [1-8]|[a-h]");
            return 1;
        }
        if(to[0] < '1' || to[0] > '8') {
            System.out.println("Invalid move or position\n\tm [1-8|[a-h] [1-8]|[a-h]");
            return 1;
        }
        if(to[1] < 'a' || to[1] > 'h') {
            System.out.println("Invalid move or position\n\tm [1-8|[a-h] [1-8]|[a-h]");
            return 1;
        }

        ChessPosition fromPos = new ChessPosition(from[0] - '0', from[1] - '`');
        ChessPosition toPos = new ChessPosition(to[0] - '0', to[1] - '`');
        ChessMove move = new ChessMove(fromPos, toPos, null);
        if(Game.game.willPromote(move)) {
            return 2;
        }

        try {
            Game.game.makeMove(move);
        }
        catch(InvalidMoveException e) {
            System.out.println(e.getMessage());
            return 1;
        }
        
        UserGameCommand newMove = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, Game.authToken, Game.gameID, move);

        ws.sendCommand(newMove);
        try {
            Thread.sleep(100);
        }
        catch(Exception e) {
        }
        return 0;
    }

    private static boolean promote(String fromStr, String toStr, String promotionStr) {
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
                break;
            default:
                return false;
        }
        char from[] = fromStr.toCharArray();
        char to[] = toStr.toCharArray();
        ChessPosition fromPos = new ChessPosition(from[0] - '0', from[1] - '`');
        ChessPosition toPos = new ChessPosition(to[0] - '0', to[1] - '`');
        ChessMove move = new ChessMove(fromPos, toPos, type);
        try {
            Game.game.makeMove(move);
        }
        catch(InvalidMoveException e) {
            System.out.println(e.getMessage());
            return false;
        }

        UserGameCommand newMove = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, Game.authToken, Game.gameID, move);

        ws.sendCommand(newMove);
        try {
            Thread.sleep(100);
        }
        catch(Exception e) {
        }
        return true;
    }

    private static void leave() {
        int team = Game.color == ChessGame.TeamColor.WHITE ? 1 : 0;
        if(Game.isSpectator) {
            team = -1;
        }
        UserGameCommand leaveCommand = new UserGameCommand(UserGameCommand.CommandType.LEAVE, Game.authToken, Game.gameID);
        ws.sendCommand(leaveCommand);
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
        drawBoard(null);
        String team = "spectator: ";
        String myinput[];
        while(!quit) {
            System.out.print(team);
            myinput = s.nextLine().split(" ");
            switch(myinput[0]) {
                case "h":
                    drawHelp();
                    break;
                case "l":
                    leave();
                    quit = true;
                    break;
                case "m":
                    System.out.println("This command is not available to spectators");
                    break;
                case "rd":
                    drawBoard(null);
                    break;
                case "r":
                    System.out.println("This command is not available to spectators");
                    break;
                case "hi":
                    if(myinput.length < 2) {
                        System.out.println("Invalid command");
                        drawHelp();
                        break;
                    }
                    highlight(myinput[1]);
                    break;
                default:
                    System.out.println("Invalid command");
                    drawHelp();
                    return;
            }
        return;
        }
    }

    private static void drawBoard(ArrayList<int[]> hi) {
        StringBuilder sb = new StringBuilder();
        int margin;
        String footer;

        if(color == ChessGame.TeamColor.WHITE) {
            footer = " abcdefgh \n";
        }
        else {
            footer = " hgfedcba \n";
        }

        if(color == ChessGame.TeamColor.WHITE || color == null) {
            margin = 8;
        }
        else {
            margin = 1;
        }

        sb.append("\n\n");
        if(Game.game.isResigned()) {
            String player = Game.game.getTeamTurn() == ChessGame.TeamColor.WHITE ? "white" : "black";
            sb.append(String.format("++++++++++++++++ Player %s has resigned ++++++++++++++++\n", player));
        }
        for(String row : formattedBoard(hi)) {
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

    private static String[] formattedBoard(ArrayList<int[]> hi) {
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
                if(hi != null) {
                            bucketTile = ihateThis(hi, bucketTile, sb, i, j);
                }
                sb.append(bucketPiece);
                sb.append(bucketTile);
                sb.append(bucketChar);

                whitetile ^= true;
            }
            sb.append("\033[25;0m\n");
            board[i] = sb.toString();
            sb = new StringBuilder();
            whitetile ^= true;
        }
        return board;
    }

    private static String ihateThis(ArrayList<int[]> hi, String def, StringBuilder sb, int i, int j) {
                    for(int p[] : hi) {
                        if((p[0] == i && p[1] == j)) {
                            sb.append("5;");
                            return HIGHLIGHT;
                        }
                    }
                    return def;
    }

    public static void notify(String message) {
        String team; 
        if(color == ChessGame.TeamColor.WHITE) {
            team = "Player white: ";
        }
        else {
            team = "Player black: ";
        }
        System.out.printf("\n%s\n%s", message, team);
    }

    public static void updateGame(String serial) {
        Game.game = chess.ChessGame.deSerialize(serial);
    }

    public static void touchBoard(String serial) {
        updateGame(serial);
        drawBoard(null);
        String team; 
        if(color == ChessGame.TeamColor.WHITE) {
            team = "Player white: ";
        }
        else {
            team = "Player black: ";
        }
        System.out.print(team);
    }


}
