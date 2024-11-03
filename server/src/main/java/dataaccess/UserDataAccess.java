package dataaccess;

import dbobjects.UserData;
import java.util.ArrayList;
import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

public class UserDataAccess extends SQLDataAccess {
    private static final ArrayList<dbobjects.UserData> table = new ArrayList<dbobjects.UserData>();

    public static dbobjects.UserData getUser(String username) throws DataAccessException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT USERNAME, PASS_HASH, EMAIL\n");
        sb.append("FROM USER_DATA\n");
        sb.append("WHERE USERNAME = ?;");
        try(PreparedStatement getStatement = CONN.prepareStatement(sb.toString())) {
            getStatement.setString(1, username);
            ResultSet result = getStatement.executeQuery();

            if(!result.isBeforeFirst()) {
                return null;
            }

            if(result.next()) {
                dbobjects.UserData user = new dbobjects.UserData(result.getString(1), result.getString(2), result.getString(3));
                table.add(user);
                return user;
            }
        }
        catch(SQLException e) {
            throw new SQLDataAccessException(String.format("There was a problem querying for %s in USER_DATA: " + username));
        }
        return null;
    }

    public static void createUser(String username, String password, String email) throws DataAccessException {
        String hash = hashPass(password);
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT USERNAME\n");
        sb.append("FROM USER_DATA\n");
        sb.append("WHERE USERNAME = '" + username + "';");

        try(PreparedStatement getStatement = CONN.prepareStatement(sb.toString())) {
            ResultSet result = getStatement.executeQuery();
            if(result.isBeforeFirst()) {
                throw new AlreadyTakenException("Error: already taken");
            }
        }
        catch(SQLException e) {
            throw new SQLDataAccessException("There was a problem finding instance of existing user in the database: " + e.getMessage());
        }

        sb = new StringBuilder();
        sb.append("INSERT INTO USER_DATA ");
        sb.append("(USERNAME, PASS_HASH, EMAIL)\n");
        sb.append("VALUES (?, ?, ?);");
        try(PreparedStatement createStatement = CONN.prepareStatement(sb.toString())) {
            createStatement.setString(1, username);
            createStatement.setString(2, hash);
            createStatement.setString(3, email);
            createStatement.executeUpdate();
        }
        catch(SQLException e) {
            throw new SQLDataAccessException("There was a problem creating a new user in the SQL database: " + e.getMessage());
        }
        table.add(new dbobjects.UserData(username, hash, email));
    }

    public static void clearUsers() throws DataAccessException {
        String truncateString = "TRUNCATE TABLE USER_DATA;";
        try(PreparedStatement truncateStatement = CONN.prepareStatement(truncateString)) {
            truncateStatement.executeUpdate();
        }
        catch(SQLException e) {
            throw new SQLDataAccessException("There was a problem truncating table USER_DATA: " + e.getMessage());
        }
        UserDataAccess.table.clear();
    }

    public static String dumpTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(dbobjects.UserData record : table) {
            sb.append(record.toString());
        }
        sb.append("]");
        return sb.toString();
    }

    private static void loadTable() {
        ResultSet result;
        String getString = "SELECT * FROM USER_DATA";
        try(PreparedStatement selectStatement = CONN.prepareStatement(getString)) {
            result = selectStatement.executeQuery();
            while(result.next()) {
                table.add(new dbobjects.UserData(result.getString(1), result.getString(2), result.getString(3)));
            }
        }
        catch(SQLException e) {
            throw new RuntimeException("There was a problem loading the memory implementation of USER_DATA: " + e.getMessage());
        }
    }

    private static String hashPass(String pass) {
        return BCrypt.hashpw(pass.toString(), BCrypt.gensalt(7));
    }

    public static boolean checkPass(String pass, String hash) {
        return BCrypt.checkpw(pass, hash);
    }
}
