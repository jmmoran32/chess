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

    public static void write() {
        throw new RuntimeException("Not implemented");
    }
}
