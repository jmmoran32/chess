package dataaccess;

import dbobjects.UserData;
import java.util.ArrayList;

public class UserDataAccess {
    private static final ArrayList<dbobjects.UserData> table = new ArrayList<dbobjects.UserData>();

    public dbobjects.UserData getUser(String username) {
        for(dbobjects.UserData record : table) {
            if(record.username.equals(username))
                return record;
        }
        return null;
    }

    public void createUser(String username, String password, String email) throws DataAccessException {
        for(dbobjects.UserData record : table) 
            if(record.username.equals(username))
                throw new DataAccessException(String.format("User \"%s\" already exists as record no %ld", record.username, record.id));
        UserDataAccess.table.add(new dbobjects.UserData(username, password, email));
    }

    public void deleteUsers() {UserDataAccess.table.clear();}
}
