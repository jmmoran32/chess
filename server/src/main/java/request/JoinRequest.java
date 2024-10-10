package request;

public class JoinRequest {
    public String token;
    public String color;
    public String gameID;

    public JoinRequest(String t, String c, String g) {
        this.token = t;
        this.color = c;
        this.gameID = g;
    }
}
