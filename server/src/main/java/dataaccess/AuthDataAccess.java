package dataaccess;

import dbobjects.AuthData;
import java.util.ArrayList;

public class AuthDataAccess {
    private static final ArrayList<dbobjects.AuthData> table = new ArrayList<dbobjects.AuthData>();

    public static void CreateAuth(String authToken, String username) throws DataAccessException {
        for(dbobjects.AuthData record : table)
            if(record.authToken == authToken)
                throw new DataAccessException(String.format("Authtoken %d already exists in AuthData as record no %ld.", record.authToken, record.id));
        AuthDataAccess.table.add(new dbobjects.AuthData(authToken, username));
    }
}
