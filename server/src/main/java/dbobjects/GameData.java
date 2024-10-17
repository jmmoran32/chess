package dbobjects;

import chess.ChessGame;

public class GameData {
    private long id;
    private int gameID;
    private String whiteUsername;
    private String blackUsername;
    private String gameName;
    private ChessGame game;
    private static long nextID = 1;

    public GameData(int gameID, String gameName, ChessGame game) {
        this.gameID = gameID;
        this.gameName = gameName;
        this.game = game;
        this.id = nextID++;
    }

    public void joinWhite(String username) {this.whiteUsername = username;}

    public void joinBlack(String username) {this.blackUsername = username;}

    public long id() {return this.id;}
    public int gameID() {return this.gameID;}
    public String whiteUsername() {return this.whiteUsername;}
    public String blackUsername() {return this.blackUsername;}
    public String gameName() {return this.gameName;}
    public chess.ChessGame game() {return this.game;}
}

