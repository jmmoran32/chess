package dbobjects;

public class UserData {
    public long id;
    public String username;
    public String password;
    public String email;
    private static long nextID = 1;

    public UserData(String username, String password, String email) {
        this.id = nextID++;
        this.password = password;
        this.email = email;
    }
}
