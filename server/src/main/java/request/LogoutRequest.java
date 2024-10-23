package request;

public class LogoutRequest {
    public String authtoken;

    public LogoutRequest(String t) {
        this.authtoken = t;
    }
}
