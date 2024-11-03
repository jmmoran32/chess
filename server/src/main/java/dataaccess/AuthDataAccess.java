package dataaccess;

import dbobjects.AuthData;
import java.util.ArrayList;
import java.sql.*;

public class AuthDataAccess extends SQLDataAccess {
    private static final ArrayList<dbobjects.AuthData> table = new ArrayList<dbobjects.AuthData>();

    /*
    public static void createAuth(String authToken, String username) throws DataAccessException {
        for(dbobjects.AuthData record : table)
            if(record.authToken().equals(authToken))
                throw new DataAccessException(String.format("Authtoken %d already exists in AuthData as record no %ld.", record.authToken(), record.id()));
        AuthDataAccess.table.add(new dbobjects.AuthData(authToken, username));
    }
    */

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
        sb.append(String.format("VALUES ('%s', '%s');", authToken, username));

        executeUpdateStatement(sb.toString());
    }

    /*
    public static String getAuthToken(String authToken) {
        for(dbobjects.AuthData record : table)
            if(record.authToken().equals(authToken))
                return record.authToken();
        return null;
    }
    */

    public static String getAuthToken(String authToken) throws SQLException {
        String query = "SELECT AUTH_TOKEN FROM AUTH_DATA WHERE AUTH_TOKEN = '" + authToken + "';";
        if(checkIfExists(query)) {
            return authToken;
        }
        return null;
    }

    /*
    public static String getUsername(String authToken) throws DataAccessException {
        for(dbobjects.AuthData record : table) 
            if(record.authToken().equals(authToken))
                return record.username();
        throw new DataAccessException(String.format("User not found matching authtoken %s"));
    }
    */

    public static String getUsername(String authToken) throws SQLException, DataAccessException {
        if(getAuthToken(authToken) == null) {
            throw new DataAccessException(String.format("User not found matching authtoken %s"));
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

    /*
    public static boolean deleteAuthToken(String authToken) {
        boolean success = false;
        for(int i = 0; i < table.size(); i++) {
            if(table.get(i).authToken().equals(authToken))
                table.remove(i);
            success = true;
        }
        return success;
    }
    */

    public static boolean deleteAllAuthOf(String username) throws SQLException {
        String queryString = "SELECT AUTH_TOKEN FROM AUTH_DATA WHERE USERNAME = '" + username + "';";
        String bucket;
        try(PreparedStatement query = CONN.prepareStatement(queryString)) {
            ResultSet result = query.executeQuery();

            while(result.next()) {
                bucket = result.getString(1);
                if(!deleteAuthToken(bucket)) {
                    return false;
                }
            }
        }
        catch(SQLException e) {
            throw new SQLException("There was a proplem deleting all authtokens for user " + username + ": " + e.getMessage());
        }
        return true;
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
        AuthDataAccess.table.clear();
        String truncate = "TRUNCATE TABLE AUTH_DATA";
        executeUpdateStatement(truncate);
    }

    public static String dumpTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(dbobjects.AuthData record : table) {
            sb.append(record.toString());
        }
        sb.append("]");
        return sb.toString();
    }
}
