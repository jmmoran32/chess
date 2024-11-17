import chess.*;
import ui.UI;


public class Main {
    public static final String URL = "http://localhost";
    public static final String PORT = "8080";

    public static void main(String[] args) {
        String connectionUrl = URL + ":" + PORT;
        
        UI.run(connectionUrl);
    }
}
