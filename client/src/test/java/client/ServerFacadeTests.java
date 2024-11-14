package client;

import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;
import dataaccess.*;
import java.sql.*;
import request.*;
import response.*;
import dbobjects.*;
import chess.*;

public class ServerFacadeTests {
    private static dbobjects.UserData goodUser;
    private static Server server;
    private static ServerFacade facade;
    private static chess.ChessGame game;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
        goodUser = new UserData("Charlie", "1234", "c.e.com");
        game = new ChessGame();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @AfterEach
    void cleanup() throws Exception {
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


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    @DisplayName("Create User Good")
    public void createTestGood() {
        try {
        String authToken = facade.registration(goodUser.username(), goodUser.password(), goodUser.email());
        Assertions.assertTrue(findAuth(authToken));
        Assertions.assertTrue(findGoodUser());
        }
        catch(Exception e) {
            Assertions.fail("An Exception was thrown: " + e.getMessage());
        }
    }

    private boolean findGoodUser() throws Exception {
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

}
