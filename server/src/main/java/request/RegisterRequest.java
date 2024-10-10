package request;

public class RegisterRequest {
    public String username;
    public String password;
    public String email;

    public RegisterRequest(String us, String pas, String em) {
        this.username = us;
        this.password = pas;
        this.email = em;
    }
}
