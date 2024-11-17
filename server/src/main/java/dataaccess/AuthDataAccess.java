package dataaccess;

import dbobjects.AuthData;
import java.util.ArrayList;
import java.sql.*;

public class AuthDataAccess extends SQLDataAccess {
    private static final ArrayList<dbobjects.AuthData> TABLE = new ArrayList<dbobjects.AuthData>();

    public static void createAuth(String authToken, String username) throws DataAccessException , SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT AUTH_TOKEN\n");
        sb.append("FROM AUTH_DATA\n");
        sb.append(String.format("WHERE AUTH_TOKEN = '%s';", authToken));
        if(checkIfExists(sb.toString())) {
            throw new DataAccessException(String.format("Authtoken %s Already exists", authToken));
        }

        sb = new StringBuilder();
        sb.append("INSERT INTO AUTH_DATA\n");
        sb.append("(AUTH_TOKEN, USERNAME)\n");
        sb.append("VALUES (?, ?);");

        try(PreparedStatement statement = CONN.prepareStatement(sb.toString())) {
            statement.setString(1, authToken);
            statement.setString(2, username);
            statement.executeUpdate();
        }
        catch(SQLException e) {
            throw new SQLException("There was a problem creating a new Auth: " + e.getMessage());
        }
    }

    public static String getAuthToken(String authToken) throws SQLException {
        String query = "SELECT AUTH_TOKEN FROM AUTH_DATA WHERE AUTH_TOKEN = '" + authToken + "';";
        if(checkIfExists(query)) {
            return authToken;
        }
        return null;
    }

    public static String getUsername(String authToken) throws SQLException, DataAccessException {
        if(getAuthToken(authToken) == null) {
            return null;
        }

        String queryString = "SELECT USERNAME FROM AUTH_DATA WHERE AUTH_TOKEN = '" + authToken + "';";
        try(PreparedStatement query = CONN.prepareStatement(queryString)) {
            ResultSet result = query.executeQuery();
            result.next();
            return result.getString(1);
        }
        catch(SQLException e) {
            throw new SQLException("There was a problem getting username from authtoken: " + e.getMessage());
        }
    }

    public static boolean deleteAuthToken(String authToken) throws SQLException {
        String queryString = "SELECT AUTH_TOKEN FROM AUTH_DATA WHERE AUTH_TOKEN = '" + authToken + "';";
        if(!checkIfExists(queryString)) {
            return false;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("DELETE\n");
        sb.append("FROM AUTH_DATA\n");
        sb.append(String.format("WHERE AUTH_TOKEN = '%s'", authToken));

        try {
            executeUpdateStatement(sb.toString());
        }
        catch(SQLException e) {
            return false;
        }
        return true;
    }

    public static void clearAuth() throws SQLException {
        AuthDataAccess.TABLE.clear();
        String truncate = "TRUNCATE TABLE AUTH_DATA";
        executeUpdateStatement(truncate);
    }
}
