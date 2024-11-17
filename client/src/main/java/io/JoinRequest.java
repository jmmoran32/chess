package io;

public class JoinRequest {
    public String playerColor;
    public String gameID;

    public JoinRequest(String c, String g) {
        this.playerColor = c;
        this.gameID = g;
    }
}
