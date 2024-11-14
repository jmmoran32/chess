package facade;

public class JoinRequest {
    public String authToken;
    public String playerColor;
    public String gameID;

    public JoinRequest(String t, String c, String g) {
        this.authToken = t;
        this.playerColor = c;
        this.gameID = g;
    }
}
