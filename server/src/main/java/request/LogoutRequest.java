package request;

public class LogoutRequest {
    public String authToken;

    public LogoutRequest(String t) {
        this.authToken = t;
    }
}
