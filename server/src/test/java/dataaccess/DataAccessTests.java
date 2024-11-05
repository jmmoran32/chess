package passoff.server;

import org.junit.jupiter.api.*;
import java.sql.*;
import dataaccess.*;
import dbobjects.*;
import chess.ChessGame;
import chess.ChessBoard;
import java.util.UUID;

public class DataAccessTests {
    private static dbobjects.UserData goodUser;
    private static dbobjects.UserData otherUser;
    private static dbobjects.UserData thirdUser;
    private static chess.ChessGame game;
    private static chess.ChessBoard board;
    private static String defaultSerial;
    private static String weirdUsername;
    private static String authToken;

    @BeforeAll
    public static void init() {
        goodUser = new UserData("Charlie", "1234", "c.e.com");
        otherUser = new UserData("Albert", "5678", "c.g.net");
        thirdUser = new UserData("Greyson", "9123", "c.b.org");
        weirdUsername = "A name with'nt quote'th' mark'ths";
        game = new ChessGame();
        game.getBoard().resetBoard();
        authToken = UUID.randomUUID().toString();
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
        GameDataAccess.newGame("A New Game");
        Assertions.assertEquals(1, numRows("GAME_DATA"));
    }

    @Test
    @DisplayName("Create Game Duplicate")
    public void createGameDuplicate() throws Exception {
        GameDataAccess.newGame("A New Game");
        try {
            GameDataAccess.newGame("Duplicate Game");
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
        GameDataAccess.newGame("A New Game");
        GameDataAccess.newGame("Another New Game");
        GameDataAccess.newGame("Yet Another New Game");
        ChessGame returned = GameDataAccess.getGame(2);
        Assertions.assertEquals(game.getBoard(), returned.getBoard());
    }

    @Test
    @DisplayName("Get Game Not Exists")
    public void getGameNotExists() throws Exception {
        GameDataAccess.newGame("A New Game");
        GameDataAccess.newGame("Another New Game");
        GameDataAccess.newGame("Yet Another New Game");
        ChessGame returned = GameDataAccess.getGame(5);
        Assertions.assertNull(returned);
    }

    @Test
    @DisplayName("Get Game Object Good")
    public void getGameObjectGood() throws Exception {
        dbobjects.GameData gameOb;
        GameDataAccess.newGame("A New Game");
        dbobjects.GameData returned = GameDataAccess.getGameObject(1);
        Assertions.assertEquals(1, returned.gameID());
        Assertions.assertEquals(null, returned.blackUsername());
        Assertions.assertEquals(null, returned.whiteUsername());
        Assertions.assertEquals("A New Game", returned.gameName());
        Assertions.assertEquals(defaultSerial, returned.game().serialize());
    }

    @Test
    @DisplayName("Get Game Object Not Exists")
    public void getGameObjectNotExists() throws Exception {
        dbobjects.GameData gameOb;
        GameDataAccess.newGame("A New Game");
        dbobjects.GameData returned = GameDataAccess.getGameObject(2);
        Assertions.assertNull(returned);
    }

    @Test
    @DisplayName("Join Game Good")
    public void joinGameGood() throws Exception {
        dbobjects.UserData user = new dbobjects.UserData(weirdUsername, "1234", "a.e.com");
        GameDataAccess.newGame("A New Game");
        GameDataAccess.joinGame(user, chess.ChessGame.TeamColor.BLACK, 1);
        dbobjects.GameData returned = GameDataAccess.getGameObject(1);
        Assertions.assertNull(returned.whiteUsername());
        Assertions.assertEquals(weirdUsername, returned.blackUsername());

        GameDataAccess.joinGame(goodUser, chess.ChessGame.TeamColor.WHITE, 1);
        dbobjects.GameData returned2 = GameDataAccess.getGameObject(1);
        Assertions.assertEquals(goodUser.username(), returned2.whiteUsername());
        Assertions.assertEquals(weirdUsername, returned2.blackUsername());
    }

    @Test
    @DisplayName("Join Game Not Exists")
    public void joinGameNotExists() throws Exception {
        try {
            GameDataAccess.joinGame(goodUser, chess.ChessGame.TeamColor.WHITE, 1);
        }
        catch(Exception e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    @DisplayName("Clear Game Good")
    public void clearGameGood() throws Exception {
        GameDataAccess.clearGameData();
        Assertions.assertEquals(0, numRows("GAME_DATA"));
    }

    @Test
    @DisplayName("Create Auth Good")
    public void createAuthGood() throws Exception {
        AuthDataAccess.createAuth(authToken, goodUser.username());
        Assertions.assertTrue(findAuth(authToken));
        Assertions.assertTrue(findAuthUser(goodUser.username()));
    }

    @Test
    @DisplayName("Create Auth Already Exists")
    public void createAuthAlreadyExists() throws Exception {
        AuthDataAccess.createAuth(authToken, goodUser.username());
        try {
            AuthDataAccess.createAuth(authToken, goodUser.username());
        }
        catch(DataAccessException e) {
            Assertions.assertTrue(true);
        }
        catch(SQLException e) {
            Assertions.fail("Wrong type of exception thrown: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Get Auth Good")
    public void getAuthGood() throws Exception {
        AuthDataAccess.createAuth(authToken, goodUser.username());
        Assertions.assertEquals(authToken, AuthDataAccess.getAuthToken(authToken));
    }

    @Test
    @DisplayName("Get Auth Not Exists")
    public void getAuthNotExists() throws Exception {
        AuthDataAccess.createAuth(authToken, goodUser.username());
        Assertions.assertNull(AuthDataAccess.getAuthToken("blah blah blah"));
    }

    @Test
    @DisplayName("Get Auth Username Good")
    public void getAuthUsernameGood() throws Exception {
        AuthDataAccess.createAuth(authToken, goodUser.username());
        Assertions.assertEquals(goodUser.username(), AuthDataAccess.getUsername(authToken));
    }

    @Test
    @DisplayName("Get Auth Username Not Exists")
    public void getAuthUsernameNotExits() throws Exception {
        AuthDataAccess.createAuth(authToken, goodUser.username());
        Assertions.assertNull(AuthDataAccess.getUsername("blah blah blah"));
    }

    @Test
    @DisplayName("Delete Auth Good") 
    public void deleteAuthGood() throws Exception {
        AuthDataAccess.createAuth(authToken, goodUser.username());
        AuthDataAccess.createAuth(UUID.randomUUID().toString(), otherUser.username());
        AuthDataAccess.createAuth(UUID.randomUUID().toString(), thirdUser.username());
        Assertions.assertTrue(AuthDataAccess.deleteAuthToken(authToken));
        Assertions.assertNull(AuthDataAccess.getAuthToken(authToken));
    }

    @Test
    @DisplayName("Delete Auth Not Exists")
    public void deleteAuthNotExists() throws Exception {
        String authToken2 = UUID.randomUUID().toString();
        String authToken3 = UUID.randomUUID().toString();
        AuthDataAccess.createAuth(authToken2, otherUser.username());
        AuthDataAccess.createAuth(authToken3, thirdUser.username());
        Assertions.assertFalse(AuthDataAccess.deleteAuthToken(authToken));
        Assertions.assertEquals(authToken2, AuthDataAccess.getAuthToken(authToken2));
        Assertions.assertEquals(authToken3, AuthDataAccess.getAuthToken(authToken3));
    }

    @Test
    @DisplayName("Clear Auth Data Good")
    public void clearAuthDataGood() throws Exception {
        String authToken2 = UUID.randomUUID().toString();
        String authToken3 = UUID.randomUUID().toString();
        AuthDataAccess.createAuth(authToken2, otherUser.username());
        AuthDataAccess.createAuth(authToken3, thirdUser.username());
        AuthDataAccess.clearAuth();
        Assertions.assertEquals(0, numRows("AUTH_DATA"));
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

    private boolean findAuthUser(String username) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT *\n");
        sb.append("FROM AUTH_DATA\n");
        sb.append("WHERE USERNAME = ?");

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
}
