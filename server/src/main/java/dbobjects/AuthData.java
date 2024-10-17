package dbobjects;

public class AuthData {
    long id;
    private String authToken;
    private String username;
    private static long nextID = 1;

    public AuthData(String authToken, String username) {
        this.authToken = authToken;
        this.username = username;
        this.id = nextID++; 
    }
}
