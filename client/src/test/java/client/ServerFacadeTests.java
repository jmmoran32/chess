package client;

import org.junit.jupiter.api.*;
import server.Server;
import facade.*;
import spark.*;
import java.sql.*;
import dataaccess.SQLDataAccess;
import java.util.ArrayList;
import chess.ChessGame;

public class ServerFacadeTests {
    private static final String url = "http://localhost:";
    private static Server server;
    private static ServerFacade facade;
    private static String goodUser[] = {"Charlie", "1234", "c.e.com"};
    private static String game1 = "A game with ' semi' colons";
    private static String game2 = "a different; game";
    private static String game3 = "last on^e";

    @BeforeAll
    public static void init() throws Exception {
        clearDB();
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(url + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @AfterEach
    void cleanup() throws Exception {
        clearDB();
    }

    @Test
    @DisplayName("register good")
    public void registerGood() {
        try {
            String authToken = facade.registration(goodUser[0], goodUser[1], goodUser[2]);
            Assertions.assertTrue(findAuth(authToken));
            Assertions.assertTrue(findGoodUser());
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when registering good user");
        }
    }

    @Test
    @DisplayName("register already exists")
    public void registerAlreadyExists() {
        try {
            facade.registration(goodUser[0], goodUser[1], goodUser[2]);
            String invalidAuth = facade.registration(goodUser[0], goodUser[1], goodUser[2]);
            Assertions.fail("No exception was thrown");
        }
        catch(ResponseException e) {
            Assertions.assertEquals(403, e.getStatus());
        }
        catch(Exception e) {
            Assertions.fail("Wrong type of Exception thrown: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("login good")
    public void loginGood() {
        try {
            String authToken = facade.registration(goodUser[0], goodUser[1], goodUser[2]);
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when regiistering good user");
        }
        try {
            String newAuth = facade.login(goodUser[0], goodUser[1]);
            Assertions.assertTrue(findAuth(newAuth));
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when loggin in good user");
        }
    }

    @Test
    @DisplayName("login unauthorized")
    public void loginNotRegistered() {
        try {
            String newAuth = facade.login(goodUser[0], goodUser[1]);
            Assertions.fail("No exception was thrown");
        }
        catch(ResponseException e) {
            Assertions.assertEquals(401, e.getStatus());
        }
        catch(Exception e) {
            Assertions.fail("Wrong type of Exception thrown: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("logout good")
    public void logoutGood() {
        String newAuth = "";
        try {
            String authToken = facade.registration(goodUser[0], goodUser[1], goodUser[2]);
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when regiistering good user");
        }
        try {
            newAuth = facade.login(goodUser[0], goodUser[1]);
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when loggin in good user");
        }
        try {
            facade.logout(newAuth);
            Assertions.assertFalse(findAuth(newAuth));
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when logging out good user");
        }
    }

    @Test
    @DisplayName("logout unauthorized")
    public void logoutUnauthorized() {
        String newAuth = "";
        try {
            String authToken = facade.registration(goodUser[0], goodUser[1], goodUser[2]);
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when regiistering good user");
        }
        try {
            newAuth = facade.login(goodUser[0], goodUser[1]);
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when loggin in good user");
        }
        try {
            facade.logout("asdf");
            Assertions.fail("No exception was thrown");
        }
        catch(ResponseException e) {
            Assertions.assertEquals(401, e.getStatus());
        }
        catch(Exception e) {
            Assertions.fail("Wrong exception thrown");
        }
    }

    @Test
    @DisplayName("create game good")
    public void createGameGood() {
        String gameID = "";
        String newAuth = "";
        try {
            String authToken = facade.registration(goodUser[0], goodUser[1], goodUser[2]);
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when regiistering good user");
        }
        try {
            newAuth = facade.login(goodUser[0], goodUser[1]);
            Assertions.assertTrue(findAuth(newAuth));
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when loggin in good user");
        }
        try {
            gameID = facade.createGame(newAuth, "Good Game");
            Assertions.assertTrue(true);
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when creating a game");
        }
    }

    @Test
    @DisplayName("create game unauthorized")
    public void createGameUnauthorized() {
        String gameID = "";
        String newAuth = "";
        try {
            String authToken = facade.registration(goodUser[0], goodUser[1], goodUser[2]);
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when regiistering good user");
        }
        try {
            newAuth = facade.login(goodUser[0], goodUser[1]);
            Assertions.assertTrue(findAuth(newAuth));
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when loggin in good user");
        }
        try {
            gameID = facade.createGame("asdf", "Good Game");
            Assertions.fail("No exception was thrown");
        }
        catch(ResponseException e) {
            Assertions.assertEquals(401, e.getStatus());
        }
        catch(Exception e) {
            Assertions.fail("Wrong exception thrown: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("list games good")
    public void listGamesGood() {
        String newAuth = "";
        try {
            String authToken = facade.registration(goodUser[0], goodUser[1], goodUser[2]);
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when regiistering good user");
        }
        try {
            newAuth = facade.login(goodUser[0], goodUser[1]);
            Assertions.assertTrue(findAuth(newAuth));
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when loggin in good user");
        }
        try {
            facade.createGame(newAuth, game1);
            facade.createGame(newAuth, game2);
            facade.createGame(newAuth, game3);
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when creating a game");
        }
        try {
            ArrayList<ChessGame> games = facade.listGames(newAuth);
            Assertions.assertEquals(3, games.size());
        }
        catch(Exception e) {
            Assertions.fail("An exceptinon was thrown when requesting a game list: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("list games unauthorized")
    public void listGamesUnauthorized() {
        String newAuth = "";
        try {
            String authToken = facade.registration(goodUser[0], goodUser[1], goodUser[2]);
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when regiistering good user");
        }
        try {
            newAuth = facade.login(goodUser[0], goodUser[1]);
            Assertions.assertTrue(findAuth(newAuth));
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when loggin in good user");
        }
        try {
            facade.createGame(newAuth, game1);
            facade.createGame(newAuth, game2);
            facade.createGame(newAuth, game3);
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when creating a game");
        }
        try {
            ArrayList<ChessGame> games = facade.listGames("1234");
            Assertions.assertEquals(3, games.size());
            Assertions.fail("No exception was thrown");
        }
        catch(ResponseException e) {
            Assertions.assertEquals(401, e.getStatus());
        }
        catch(Exception e) {
            Assertions.fail("wrong exception thrown: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("join white good")
    public void joinWhiteGood() {
        String gameID = "";
        String newAuth = "";
        try {
            String authToken = facade.registration(goodUser[0], goodUser[1], goodUser[2]);
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when regiistering good user");
        }
        try {
            newAuth = facade.login(goodUser[0], goodUser[1]);
            Assertions.assertTrue(findAuth(newAuth));
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when loggin in good user");
        }
        try {
            gameID = facade.createGame(newAuth, "Good Game");
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when creating a game");
        }
        try {
            Assertions.assertTrue(facade.joinGame(newAuth, ChessGame.TeamColor.WHITE, gameID));
            Assertions.assertTrue(checkJoined(goodUser[0], ChessGame.TeamColor.WHITE, gameID));
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when joining a game");
        }
    }

    @Test
    @DisplayName("join white already taken")
    public void joinWhiteAlreadyTaken() {
        String gameID = "";
        String newAuth = "";
        String secondAuth = "";
        String secondUsername = "Alvin";
        try {
            String authToken = facade.registration(goodUser[0], goodUser[1], goodUser[2]);
            String authToken2 = facade.registration(secondUsername, "5678", "org.com");
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when registering users");
        }
        try {
            newAuth = facade.login(goodUser[0], goodUser[1]);
            secondAuth = facade.login(secondUsername, "5678");
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when loggin in users");
        }
        try {
            gameID = facade.createGame(newAuth, "Good Game");
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when creating a game");
        }
        try {
            Assertions.assertTrue(facade.joinGame(newAuth, ChessGame.TeamColor.WHITE, gameID));
            Assertions.assertTrue(checkJoined(goodUser[0], ChessGame.TeamColor.WHITE, gameID));

            facade.joinGame(secondAuth, ChessGame.TeamColor.WHITE, gameID);
            Assertions.fail("no exceptions were thrown");
        }
        catch(ResponseException e) {
            Assertions.assertEquals(403, e.getStatus());
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when joining a game");
        }
    }

    @Test
    @DisplayName("clear")
    public void clear() {
        String newAuth = "";
        try {
            String authToken = facade.registration(goodUser[0], goodUser[1], goodUser[2]);
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when regiistering good user");
        }
        try {
            newAuth = facade.login(goodUser[0], goodUser[1]);
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when loggin in good user");
        }
        try {
            facade.createGame(newAuth, game1);
            facade.createGame(newAuth, game2);
            facade.createGame(newAuth, game3);
        }
        catch(Exception e) {
            Assertions.fail("An exception was thrown when creating a game");
        }
        try {
            ArrayList<ChessGame> games = facade.listGames(newAuth);
        }
        catch(Exception e) {
            Assertions.fail("An exceptinon was thrown when requesting a game list: " + e.getMessage());
        }
        try {
            facade.clearApplication();
            Assertions.assertTrue(isEmpty());
        }
        catch(Exception e) {
            Assertions.fail("There was an error while clearing the database");
        }
    }

    private boolean checkJoined(String username, ChessGame.TeamColor color, String gameID) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT *\n");
        sb.append("FROM GAME_DATA\n");
        if(color == ChessGame.TeamColor.WHITE) {
            sb.append("WHERE WHITE_USERNAME = ?;");
        }
        else {
            sb.append("WHERE BLACK_USERNAME = ?;");
        }

        try(PreparedStatement getStatement = SQLDataAccess.CONN.prepareStatement(sb.toString())) {
            getStatement.setString(1, username);
            ResultSet result = getStatement.executeQuery();

            if(!result.isBeforeFirst()) {
                return false;
            }
            return true;
        }
        catch(Exception e) {
            throw e;
        }
    }

    private boolean isEmpty() throws Exception {
        boolean empty1 = false;
        boolean empty2 = false;
        boolean empty3 = false;
        String query = "SELECT * FROM USER_DATA;";
        try(PreparedStatement getStatement = SQLDataAccess.CONN.prepareStatement(query)) {
            ResultSet result = getStatement.executeQuery();
            if(!result.isBeforeFirst()) {
                empty1 = true;
            }
        }
        query = "SELECT * FROM GAME_DATA;";
        try(PreparedStatement getStatement = SQLDataAccess.CONN.prepareStatement(query)) {
            ResultSet result = getStatement.executeQuery();
            if(!result.isBeforeFirst()) {
                empty2 = true;
            }
        }
        query = "SELECT * FROM AUTH_DATA;";
        try(PreparedStatement getStatement = SQLDataAccess.CONN.prepareStatement(query)) {
            ResultSet result = getStatement.executeQuery();
            if(!result.isBeforeFirst()) {
                empty3 = true;
            }
        }
        return empty1 && empty2 && empty3;
    }

    private boolean findAuth(String auth) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT *\n");
        sb.append("FROM AUTH_DATA\n");
        sb.append("WHERE AUTH_TOKEN = ?");

        try(PreparedStatement getStatement = SQLDataAccess.CONN.prepareStatement(sb.toString())) {
            getStatement.setString(1, auth);
            ResultSet result = getStatement.executeQuery();

            if(!result.isBeforeFirst()) {
                return false;
            }
            return true;
        }
        catch(Exception e) {
            throw e;
        }
    }

    private static boolean findGoodUser() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT *\n");
        sb.append("FROM USER_DATA\n");
        sb.append("WHERE USERNAME = 'Charlie'\n");
        sb.append("AND EMAIL  = 'c.e.com';");

        try(PreparedStatement getStatement = SQLDataAccess.CONN.prepareStatement(sb.toString())) {
            ResultSet result = getStatement.executeQuery();
            if(!result.next()) {
                return false;
            }
            else {
                return true;
            }
        }
        catch(Exception e) {
            throw new Exception("Test issue: couldn't find good user: " + e.getMessage());
        }
    }

    private static void clearDB() throws Exception {
        String truncate = "TRUNCATE TABLE USER_DATA";
        try(PreparedStatement statement = SQLDataAccess.CONN.prepareStatement(truncate)) {
            statement.executeUpdate();
        }
        catch(Exception e) {
            throw new Exception("Unable to clearDB after a test: " + e.getMessage());
        }
        
        truncate = "TRUNCATE TABLE GAME_DATA";
        try(PreparedStatement statement = SQLDataAccess.CONN.prepareStatement(truncate)) {
            statement.executeUpdate();
        }
        catch(Exception e) {
            throw new Exception("Unable to clearDB after a test: " + e.getMessage());
        }

        truncate = "TRUNCATE TABLE AUTH_DATA";
        try(PreparedStatement statement = SQLDataAccess.CONN.prepareStatement(truncate)) {
            statement.executeUpdate();
        }
        catch(Exception e) {
            throw new Exception("Unable to clearDB after a test: " + e.getMessage());
        }
    }
}
