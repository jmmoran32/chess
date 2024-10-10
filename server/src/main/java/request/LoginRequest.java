package request;

public class LoginRequest {
    public String username;
    public String password;

    public LoginRequest(String us, String pas) {
        this.username = us;
        this.password = pas;
    }
}
