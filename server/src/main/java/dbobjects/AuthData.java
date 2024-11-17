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

    public String username() {return this.username;}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if(this.authToken != null) {
            sb.append(String.format("%s, ", this.authToken));
        }
        else {
            sb.append("null, ");
        }
        if(this.username != null) {
            sb.append(String.format("%s]", this.username));
        }
        else {
            sb.append("null]");
        }
        return sb.toString();
    }
}
