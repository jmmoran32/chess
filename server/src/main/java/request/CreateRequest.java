package request;

public class CreateRequest {
    public String authtoken;
    public String name;

    public CreateRequest(String t, String n) {
        this.authtoken = t;
        this.name = n;
    }
}
