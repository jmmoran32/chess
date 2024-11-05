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

        StringBuilder sb = new StringBuilder();
        sb.append("USE " + DatabaseManager.getDBName() + ";");
        executePreparedStatement(sb.toString());

        sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS USER_DATA ");
        sb.append("(ID INT NOT NULL AUTO_INCREMENT, ");
        sb.append("USERNAME VARCHAR(255) NOT NULL, ");
        sb.append("PASS_HASH VARCHAR(255) NOT NULL, ");
        sb.append("EMAIL VARCHAR(255) NOT NULL, ");
        sb.append("PRIMARY KEY (ID), ");
        sb.append ("INDEX (USERNAME));");
        executePreparedStatement(sb.toString());

        sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS GAME_DATA ");
        sb.append("(GAME_ID INT NOT NULL AUTO_INCREMENT, ");
        sb.append("WHITE_USERNAME VARCHAR(255), ");
        sb.append("BLACK_USERNAME VARCHAR(255), ");
        sb.append("GAME_NAME VARCHAR(255) NOT NULL, ");
        sb.append("GAME VARCHAR(255) NOT NULL, ");
        sb.append("PRIMARY KEY (GAME_ID), ");
        sb.append ("INDEX (GAME_ID));");
        executePreparedStatement(sb.toString());

        sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS AUTH_DATA ");
        sb.append("(ID INT NOT NULL AUTO_INCREMENT, ");
        sb.append("AUTH_TOKEN VARCHAR(255) NOT NULL, ");
        sb.append("USERNAME VARCHAR(255) NOT NULL, ");
        sb.append("PRIMARY KEY (ID), ");
        sb.append ("INDEX (USERNAME));");
        executePreparedStatement(sb.toString());
    }

    static void executePreparedStatement(String statement) {
        try(PreparedStatement pStatement = CONN.prepareStatement(statement)) {
            pStatement.executeUpdate();
        }
        catch(SQLException e) {
            throw new RuntimeException(String.format("There was a problem executing init: %s", e.getMessage()));
        }
    }

    public static void executeUpdateStatement(String updateString) throws SQLException {
        try(PreparedStatement updateStatement = CONN.prepareStatement(updateString)) {
            updateStatement.executeUpdate();
        }
        catch(SQLException e) {
            throw new SQLException("There was a problem running an update statement: " + e.getMessage());
        }
    }

    public static boolean checkIfExists(String queryString) throws SQLException {
        try(PreparedStatement queryStatement = CONN.prepareStatement(queryString)) {
            ResultSet result = queryStatement.executeQuery();
            if(!result.isBeforeFirst()) {
                return false;
            }
            return true;
        }
        catch(SQLException e) {
            throw new SQLException("There was a problem getting a query statement: " + e.getMessage());
        }
    }
}
