package dbobjects;

public class UserData {
    private long id;
    private String username;
    private String password;
    private String email;
    private static long nextID = 1;

    public UserData(String username, String password, String email) {
        this.id = nextID++;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public String username() {return this.username;}
    public String password() {return this.password;}
    public String email() {return this.email;}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if(this.username != null) {
            sb.append(String.format("%s, ", this.username));
        }
        else {
            sb.append("null, ");
        }
        if(this.password != null) {
            sb.append(String.format("%s, ", this.password));
        }
        else {
            sb.append("null, ");
        }
        if(this.email != null) {
            sb.append(String.format("%s]", this.email));
        }
        else {
            sb.append("null]");
        }
        return sb.toString();
    }
}
