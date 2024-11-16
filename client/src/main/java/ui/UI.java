package ui;

import java.util.Scanner;
import facade.ServerFacade;
import facade.ResponseException;

public class UI {
    public static String HEADER;
    public static ServerFacade facade;
    public static String authToken;
    public static Scanner s;
    public static boolean isLoggedIn = false;
    public static boolean quit = false;

    public static void run(String url) {
        facade = new ServerFacade(url);
        s = new Scanner(System.in);

        while(!quit) {

            try {
                while(!isLoggedIn && !quit) {
                    preLog();
                }

                while(isLoggedIn && !quit) {
                }
            }
            catch(Exception e) {
                if(handleException(e)) {
                    return;
                }
            }
        }
        return;
    }

    public static boolean handleException(Exception e) {
        if(e instanceof ResponseException) {
            ResponseException re = (ResponseException) e;
            switch(re.getStatus()) {
                case 400:
                    System.out.println("A problem occurred. Try loggin out and back in.");
                    return false;
                case 401:
                    System.out.println("A problem occurred. Try creating a new account.");
                    return false;
                case 403:
                    System.out.println("A problem occurred. Username or game name already taken");
                    return false;
                case 500:
                    System.out.println("A server error occurred.");
                    return true;
                default:
                    System.out.println("An unknonwn error occurred.");
                    return true;
            }
        }
        System.out.println("An unknonwn error occurred.");
        return true;
    }

    public static void drawPreLog() {
        StringBuilder sb = new StringBuilder();
        sb.append("register <username> <password> <email>\n");
        sb.append("login <username> <password>\n");
        sb.append("quit\n");
        sb.append("help");
        System.out.println(sb.toString());
    }

    public static void preLog() throws Exception {
        String input[];

            System.out.print(HEADER);
            input = s.nextLine().split(" ");
            if(input.length == 0) {
                System.out.println("Invalid command");
                drawPreLog();
                return;
            }

        switch(input[0]) {
            case "register":
                if(input.length < 4) {
                    System.out.println("Invalid command");
                    drawPreLog();
                }
                String username = input[1];
                String password = input[2];
                String email = input[3];
                if(facade.registration(username, password, email) == null) {
                    throw new Exception("An unknown error occurred");
                }
                System.out.println("Registration Successful");
                return;

            case "login":
                if(input.length < 3) {
                    System.out.println("Invalid command");
                    drawPreLog();
                }
                String usernameL = input[1];
                String passwordL = input[2];
                try {
                    String newAuth = facade.login(usernameL, passwordL);
                    if(newAuth == null) {
                        throw new ResponseException(500, "A problem occurred loging in");
                    }
                    authToken = newAuth;
                }
                catch(ResponseException e) {
                    if(e.getStatus() == 401) {
                        System.out.println("Incorrect username or password");
                    }
                    else {
                        throw e;
                    }
                }

                isLoggedIn = true;
                System.out.println("Successfully logged in");
                drawPostLog();
                return;

            case "quit":
                System.out.println("Bye-bye");
                quit = true;
                return;

            case "help":
                drawPreLog();
                break;
            default:
                System.out.println("Invalid command");
                drawPreLog();
                return;
        }
    }

    public static void drawPostLog() {
        quit = true;
        System.out.println("Not ready for post log yet");
        return;
    }

    public static void printCliHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("Usage: <bin> [OPTION]\n");
        sb.append("\tOptions:\n");
        sb.append("\t\t-u [ARG] [ARG]: log in with username and password\n");
        sb.append("\t\t-h: print this message and quit\n");
        System.out.print(sb.toString());
    }
}
