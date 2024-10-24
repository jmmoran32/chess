package dataaccess;

import dbobjects.AuthData;
import java.util.ArrayList;

public class AuthDataAccess {
    private static final ArrayList<dbobjects.AuthData> table = new ArrayList<dbobjects.AuthData>();

    public static void createAuth(String authToken, String username) throws DataAccessException {
        for(dbobjects.AuthData record : table)
            if(record.authToken().equals(authToken))
                throw new DataAccessException(String.format("Authtoken %d already exists in AuthData as record no %ld.", record.authToken(), record.id()));
        AuthDataAccess.table.add(new dbobjects.AuthData(authToken, username));
    }

    public static String getAuthToken(String authToken) {
        for(dbobjects.AuthData record : table)
            if(record.authToken().equals(authToken))
                return record.authToken();
        return null;
    }

    public static String getUsername(String authToken) throws DataAccessException {
        for(dbobjects.AuthData record : table) 
            if(record.authToken().equals(authToken))
                return record.username();
        throw new DataAccessException(String.format("User not found matching authtoken %s"));
    }

    public static boolean deleteAuthToken(String authToken) {
        boolean success = false;
        for(int i = 0; i < table.size(); i++) {
            if(table.get(i).authToken().equals(authToken))
                table.remove(i);
            success = true;
        }
        return success;
    }

    public static void clearAuth() {
        AuthDataAccess.table.clear();
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

    public static void write() {
        throw new RuntimeException("Not implemented");
    }
}
