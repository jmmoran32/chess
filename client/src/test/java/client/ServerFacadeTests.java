package client;

import org.junit.jupiter.api.*;
import server.Server;
import facade.*;
import spark.*;
import java.sql.*;
import dataaccess.SQLDataAccess;


public class ServerFacadeTests {
    private static final String url = "http://localhost:";
    private static Server server;
    private static ServerFacade facade;
    private static String goodUser[] = {"Charlie", "1234", "c.e.com"};

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
            facade.logout(newAuth);
            Assertions.fail("No exception was thrown");
        }
        catch(ResponseException e) {
            Assertions.assertEquals(401, e.getStatus());
        }
        catch(Exception e) {
            Assertions.fail("Wrong exception thrown");
        }
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
