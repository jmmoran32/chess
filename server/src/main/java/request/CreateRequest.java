package request;

public class CreateRequest {
    public String token;
    public String name;

    public CreateRequest(String t, String n) {
        this.token = t;
        this.name = n;
    }
}
