package dbobjects;

public class AuthData {
    private long id;
    private String authToken;
    private String username;
    private static long nextID = 1;

    public AuthData(String authToken, String username) {
        this.authToken = authToken;
        this.username = username;
        this.id = nextID++; 
    }

    public long id() {return this.id;}
    public String authToken() {return this.authToken;}
    public String username() {return this.username;}
}
