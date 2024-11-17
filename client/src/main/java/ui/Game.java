package ui;

import chess.ChessGame;
import java.util.Scanner;

public class Game {
    private static final String WHITE_TILE = "43m";
    private static final String BLACK_TILE = "45m";
    private static final String WHITE_PIECE = "32;";
    private static final String BLACK_PIECE = "31;";
    private static boolean quit = false;
    private static ChessGame game;
    private static ChessGame.TeamColor color;
    public static final Scanner s = new Scanner(System.in);

    public static void playGame(ChessGame game, ChessGame.TeamColor playerColor) {
        Game.game = game;
        Game.color = playerColor;
        while(!quit) {
            takeTurn();
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
        System.out.print(team);
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

        sb.append("\003[2j;\r\n\n\n");
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
}
