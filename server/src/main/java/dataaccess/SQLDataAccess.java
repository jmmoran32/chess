package dataaccess;

import java.sql.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public abstract class SQLDataAccess {
    public static final Connection CONN;

    static  {
        /*
        try {
            DBManager.createDatabase();
        }
        catch(DataAccessException e) {
            throw new RuntimeException(String.format("Failed to create the database: %s", e.getMessage()));
        }
        */
        try {
            CONN = DBManager.getConnection();
        }
        catch(DataAccessException e) {
            throw new RuntimeException(String.format("Failed to create a connection: %s", e.getMessage()));
        }
        /*
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
        */
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE DATABASE IF NOT EXISTS chess;");
        executePreparedStatement(sb.toString());
        sb = new StringBuilder();
        sb.append("USE chess;");
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
}
