package dataaccess;

import dbobjects.UserData;
import dbobjects.GameData;
import chess.*;
import java.util.ArrayList;
import java.sql.*;

public class GameDataAccess extends SQLDataAccess {
    private static final ArrayList<dbobjects.GameData> table = new ArrayList<dbobjects.GameData>();

    /*
    static {
        loadTable();
    }
    */

    /*
    public static ArrayList<dbobjects.GameData> getGames() {
        return table;
    }
    */

    public static ArrayList<dbobjects.GameData> getGames() throws SQLException {
        ArrayList<GameData> games = new ArrayList<GameData>();
        String queryString = "SELECT GAME_ID FROM GAME_DATA;";

        try(PreparedStatement queryStatement = CONN.prepareStatement(queryString)) {
            ResultSet result = queryStatement.executeQuery();
            
            while(result.next()) {
                games.add(getGameObject(result.getInt(1)));
            }
        }
        catch(SQLException e) {
            throw new SQLException("There was a problem getting a query statement: " + e.getMessage());
        }
        return games;
    }

    /*
    public static chess.ChessGame getGame(int gameID) throws DataAccessException {
        for(dbobjects.GameData record : table) {
            if(record.gameID() == gameID)
                return record.game();
        }
        return null;
    }
    */

    public static chess.ChessGame getGame(int gameID) throws SQLException {
        GameData game = getGameObject(gameID);
        if(game == null) {
            return null;
        }
        return game.game();
    }

    /*
    public static dbobjects.GameData getGameObject(int gameID) {
        for(dbobjects.GameData record : table)
            if(record.gameID() == gameID)
                return record;
        return null;
    }
    */

    public static dbobjects.GameData getGameObject(int gameID) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT GAME_ID, GAME_NAME, GAME, WHITE_USERNAME, BLACK_USERNAME\n");
        sb.append("FROM GAME_DATA\n");
        sb.append("WHERE GAME_ID = " + gameID + ";");
        boolean whiteNull = false;
        boolean blackNull = false;

        try(PreparedStatement queryStatement = CONN.prepareStatement(sb.toString())) {
            ResultSet result = queryStatement.executeQuery();

            if(!result.isBeforeFirst()) {
                return null;
            }

            result.next();

            if(!result.isLast()) {
                throw new SQLException("More than 1 gameID was returned");
            }

            int dbGameID = result.getInt(1);
            String gameName = result.getString(2);
            String serial = result.getString(3);
            String whiteUsername = result.getString(4);
            
            if(result.wasNull()) {
                whiteNull = true;
            }
            String blackUsername = result.getString(5);
            if(result.wasNull()) {
                blackNull = true;
            }

            GameData obj = new GameData(dbGameID, gameName, ChessGame.deSerialize(serial));
            if(!whiteNull) {
                obj.joinWhite(whiteUsername);
            }
            if(!blackNull) {
                obj.joinBlack(blackUsername);
            }

            return obj;
        }
        catch(SQLException e) {
            throw new SQLException("There was a problem getting a query statement: " + e.getMessage());
        }
    }

    /*
    public static void newGame(String gameName, int gameID) throws DataAccessException {
        for(dbobjects.GameData record : table)
            if(record.gameID() == gameID)
                throw new DataAccessException(String.format("A game with gameID %d already exists in GameData as record no %ld", record.gameID(), record.id()));
        table.add(new dbobjects.GameData(gameID, gameName, new chess.ChessGame()));
    }
    */

    public static int newGame(String gameName) throws DataAccessException, SQLException {
        StringBuilder sb = new StringBuilder();
        ChessGame game = new ChessGame();
        int gameID;

        sb = new StringBuilder();
        sb.append("INSERT INTO GAME_DATA\n");
        sb.append("(GAME_NAME, GAME)\n");
        //sb.append(String.format("VALUES ('%s', '%s');", gameName, game.serialize()));
        sb.append("VALUES (?, ?);");

        try(PreparedStatement update = CONN.prepareStatement(sb.toString())) {
            update.setString(1, gameName);
            update.setString(2, game.serialize());
            update.executeUpdate();
        }
        catch(SQLException e) {
            throw new SQLException("There was a problem addint the new game: " + e.getMessage());
        }


        String lastID = "SELECT LAST_INSERT_ID();";
        try(PreparedStatement query = CONN.prepareStatement(lastID)) {
            ResultSet result = query.executeQuery();
            result.next();
            gameID = result.getInt(1);
        }
        catch(SQLException e) {
            throw new SQLException("There was a problem getting the ID of the last game inserted: " + e.getMessage());
        }
        table.add(new GameData(gameID, gameName, game));
        return gameID;
    }


    /*
    public static void joinGame(dbobjects.UserData user, chess.ChessGame.TeamColor color, int gameID) throws SQLException {
        dbobjects.GameData record = GameDataAccess.getGameObject(gameID);
        if(color == chess.ChessGame.TeamColor.BLACK)
            record.joinBlack(user.username());
        else
            record.joinWhite(user.username());
    }
    */

    public static void joinGame(dbobjects.UserData user, chess.ChessGame.TeamColor color, int gameID) throws SQLException {
        dbobjects.GameData record = GameDataAccess.getGameObject(gameID);
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE GAME_DATA\n");

        if(color == chess.ChessGame.TeamColor.BLACK){
            record.joinBlack(user.username());
            sb.append(String.format("SET BLACK_USERNAME = '%s'", user.username()));
        }
        else {
            record.joinWhite(user.username());
            sb.append(String.format("SET WHITE_USERNAME = '%s'", user.username()));
        }
        sb.append("\nWHERE GAME_ID = " + gameID + ";");

        executeUpdateStatement(sb.toString());
    }

    public static String dumpTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(dbobjects.GameData record : table) {
            sb.append(record.toString());
        }
        sb.append("]");
        return sb.toString();
    }

    private static void loadTable() {
        ResultSet result;
        String getString = "SELECT * FROM GAME_DATA";
        try(PreparedStatement selectStatement = CONN.prepareStatement(getString)) {
            result = selectStatement.executeQuery();
            ChessGame game;
            while(result.next()) {
                String gameString = result.getString(4);
                game = chess.ChessGame.deSerialize(gameString);
                table.add(new dbobjects.GameData(result.getInt(0), result.getString(1), game));
            }
        }
        catch(SQLException e) {
            throw new RuntimeException("There was a problem loading the memory implementation of USER_DATA: " + e.getMessage());
        }
    }


    public static void clearGameData() throws SQLException {
        GameDataAccess.table.clear();
        String truncateStatement = "TRUNCATE GAME_DATA;";
        executeUpdateStatement(truncateStatement);
    }
}
