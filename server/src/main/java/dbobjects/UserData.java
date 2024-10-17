package dbobjects;

public class UserData {
    private long id;
    private String username;
    private String password;
    private String email;
    private static long nextID = 1;

    public UserData(String username, String password, String email) {
        this.id = nextID++;
        this.password = password;
        this.email = email;
    }

    public long id() {return this.id;}
    public String username() {return this.username;}
    public String password() {return this.password;}
    public String email() {return this.email;}
}
