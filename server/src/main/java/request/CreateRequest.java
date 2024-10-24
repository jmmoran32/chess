package request;

public class CreateRequest {
    public String authToken;
    public String gameName;

    public CreateRequest(String t, String n) {
        this.authToken = t;
        this.gameName = n;
    }
}
