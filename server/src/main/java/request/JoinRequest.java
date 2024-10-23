package request;

public class JoinRequest {
    public String authtoken;
    public String color;
    public int gameID;

    public JoinRequest(String t, String c, int g) {
        this.authtoken = t;
        this.color = c;
        this.gameID = g;
    }
}
