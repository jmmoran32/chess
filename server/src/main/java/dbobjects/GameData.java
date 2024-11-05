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

    public int gameID() {return this.gameID;}
    public String whiteUsername() {return this.whiteUsername;}
    public String blackUsername() {return this.blackUsername;}
    public String gameName() {return this.gameName;}
    public chess.ChessGame game() {return this.game;}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(String.format("%d, ", this.gameID));
        if(this.whiteUsername != null) {
            sb.append(String.format("%s, ", this.whiteUsername));
        }
        else {
            sb.append("null, ");
        }
        if(this.blackUsername != null) {
            sb.append(String.format("%s, ", this.blackUsername));
        }
        else {
            sb.append("null, ");
        }
        if(this.gameName != null) {
            sb.append(String.format("%s, ", this.gameName));
        }
        else {
            sb.append("null, ");
        }
        if(this.game != null) {
            sb.append(String.format("%s]", this.game.toString()));
        }
        else {
            sb.append("null]");
        }
        return sb.toString();
    }
}

