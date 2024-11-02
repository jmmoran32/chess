package passoff.server;

import org.junit.jupiter.api.*;
import java.sql.*;
import dataaccess.*;
import dbobjects.*;
import chess.ChessGame;
import chess.ChessBoard;


public class DataAccessTests {
    private static dbobjects.UserData goodUser;
    private static dbobjects.UserData otherUser;
    private static dbobjects.UserData thirdUser;
    private static chess.ChessGame game;
    private static chess.ChessBoard board;
    private static String defaultSerial;

    @BeforeAll
    public static void init() {
        goodUser = new UserData("Charlie", "1234", "c.e.com");
        otherUser = new UserData("Albert", "5678", "c.g.net");
        thirdUser = new UserData("Greyson", "9123", "c.b.org");
        game = new ChessGame();
        game.getBoard().resetBoard();
        StringBuilder sb = new StringBuilder();
        sb.append("1{");
        sb.append("rnbqkbnr");
        sb.append("pppppppp");
        sb.append("        ");
        sb.append("        ");
        sb.append("        ");
        sb.append("        ");
        sb.append("PPPPPPPP");
        sb.append("RNBQKBNR}");
        defaultSerial = sb.toString();
    }

    @AfterEach
    public void clearDB() throws Exception {
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
    @DisplayName("Create User Good")
    public void createUserGood() throws Exception {
        dataaccess.UserDataAccess.createUser(goodUser.username(), goodUser.password(), goodUser.email());
       Assertions.assertTrue(findGoodUser()); 
    }


    @Test
    @DisplayName("Create User Duplicate")
    public void createUserDuplicate () throws Exception {
        boolean correctExceptionThrown = false;
        dataaccess.UserDataAccess.createUser(goodUser.username(), goodUser.password(), goodUser.email());
        try {
        dataaccess.UserDataAccess.createUser(goodUser.username(), goodUser.password(), goodUser.email());
        }
        catch(AlreadyTakenException e) {
            Assertions.assertTrue(true);
        }
        catch(Exception e) {
            Assertions.fail("Expected AlreadyTakenException, but got: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Get User Good")
    public void getUserGood() throws Exception {
        dataaccess.UserDataAccess.createUser(otherUser.username(), otherUser.password(), otherUser.email());
        dataaccess.UserDataAccess.createUser(goodUser.username(), goodUser.password(), goodUser.email());
        dataaccess.UserDataAccess.createUser(thirdUser.username(), thirdUser.password(), thirdUser.email());
        Assertions.assertTrue(findGoodUser());
    }

    @Test
    @DisplayName("Get User Not Exists")
    public void getUserNotExists() throws Exception {
        dataaccess.UserDataAccess.createUser(otherUser.username(), otherUser.password(), otherUser.email());
        dataaccess.UserDataAccess.createUser(goodUser.username(), goodUser.password(), goodUser.email());
        dataaccess.UserDataAccess.createUser(thirdUser.username(), thirdUser.password(), thirdUser.email());
        Assertions.assertFalse(findUser("asdf"));
    }

    @Test
    @DisplayName("Clear Users")
    public void clearUsers() throws Exception {
        dataaccess.UserDataAccess.createUser(otherUser.username(), otherUser.password(), otherUser.email());
        dataaccess.UserDataAccess.createUser(goodUser.username(), goodUser.password(), goodUser.email());
        dataaccess.UserDataAccess.createUser(thirdUser.username(), thirdUser.password(), thirdUser.email());
        UserDataAccess.clearUsers();
        Assertions.assertEquals(0, numRows("USER_DATA"));
    }

    @Test
    @DisplayName("Serialize Game")
    public void serializeGame() throws Exception {
        String serial = game.serialize();
        Assertions.assertEquals(defaultSerial, serial);
    }

    @Test
    @DisplayName("de-Serialize Game")
    public void deSerializeGame() throws Exception {
        ChessGame deSerial = ChessGame.deSerialize(defaultSerial);
        Assertions.assertEquals(game.getBoard(), deSerial.getBoard());
    }

    @Test
    @DisplayName("Create Game Good")
    public void createGameGood() throws Exception {
        GameDataAccess.newGame("A New Game", 1);
        Assertions.assertEquals(1, numRows("GAME_DATA"));
    }

    @Test
    @DisplayName("Create Game Duplicate")
    public void createGameDuplicate() throws Exception {
        GameDataAccess.newGame("A New Game", 1);
        try {
            GameDataAccess.newGame("Duplicate Game", 1);
        }
        catch(AlreadyTakenException e) {
            Assertions.assertTrue(true);
        }
        catch(Exception e) {
            Assertions.fail("Expected AlreadyTakenException, but got: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Get Game Good")
    public void getGameGood() throws Exception {
        GameDataAccess.newGame("A New Game", 1);
        GameDataAccess.newGame("Another New Game", 2);
        GameDataAccess.newGame("Yet Another New Game", 3);
        ChessGame returned = GameDataAccess.getGame(2);
        Assertions.assertEquals(game.getBoard(), returned.getBoard());
    }

    private int numRows(String tableName) throws Exception  {
        int numRows = -1;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT *\n");
        sb.append("FROM " + tableName + ";");

        try(PreparedStatement getStatement = SQLDataAccess.CONN.prepareStatement(sb.toString())) {
            ResultSet result = getStatement.executeQuery();
            numRows = 0;
            while(result.next()) {
                numRows++;
            }
        }
        catch(Exception e) {
            throw new Exception(e.getMessage());
        }
        return numRows;
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

    private boolean findUser(String username) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT *\n");
        sb.append("FROM USER_DATA\n");
        sb.append("WHERE USERNAME = ");
        sb.append("'" + username + "';");

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
}
