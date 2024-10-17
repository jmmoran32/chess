package dbobjects;

public class AuthData {
    public long id;
    public String authToken;
    public String username;
    private static long nextID = 1;

    public AuthData(String authToken, String username) {
        this.authToken = authToken;
        this.username = username;
        this.id = nextID++; 
    }
}
