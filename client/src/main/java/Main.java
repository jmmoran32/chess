import chess.*;
import ui.UI;


public class Main {
    public static String URL = "http://localhost";
    public static String PORT = "8080";
    public static boolean IS_LOGGED_IN = false;

    public static void main(String[] args) {
        String connectionUrl = URL + ":" + PORT;
        
        UI.run(connectionUrl);
    }
}
