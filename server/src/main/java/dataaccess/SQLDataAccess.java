package dataaccess;

import java.sql.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public abstract class SQLDataAccess {
    public static final Connection CONN;

    static  {
        try {
            DBManager.createDatabase();
        }
        catch(DataAccessException e) {
            throw new RuntimeException(String.format("Failed to create the database: %s", e.getMessage()));
        }
        try {
            CONN = DBManager.getConnection();
        }
        catch(DataAccessException e) {
            throw new RuntimeException(String.format("Failed to create a connection: %s", e.getMessage()));
        }
        File init = new File("server/src/main/resources/init.sql");
        StringBuilder initStatement = new StringBuilder();
        try(Scanner s = new Scanner(init)) {
            while(s.hasNextLine()) {
                initStatement.append(s.nextLine());
            }
        }
        catch(FileNotFoundException e) {
            throw new RuntimeException(String.format("Init file could not be found: %s", e.getMessage()));
        }
        try(PreparedStatement statement = CONN.prepareStatement(initStatement.toString())) {
            statement.executeUpdate();
        }
        catch(SQLException e) {
            throw new RuntimeException(String.format("There was a problem executing init: %s", e.getMessage()));
        }
    }
}
