package ui;

import java.util.Scanner;
import facade.ServerFacade;

public class UI {
    public static String HEADER;
    public static ServerFacade facade;

    public static void run(String url) {
        facade = new ServerFacade(url);
    }

    public static void preLog() {
        Scanner s = new Scanner(System.in);
        String input;

        while(true) {
            System.out.print(HEADER);
            input = s.next();

            switch(input) {
                case "create":
                    System.out.println("You chose create");
                    break;
                case "list":
                    break;
                case "join":
                    break;
                case "observe":
                    break;
                case "logout":
                    break;
                case "quit":
                    break;
                default:
                    return;
            }
        }
    }

    public static void drawPostLog() {
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
