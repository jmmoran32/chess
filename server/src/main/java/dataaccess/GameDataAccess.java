package dataaccess;

import dbobjects.UserData;
import dbobjects.GameData;
import chess.*;
import java.util.ArrayList;
import java.sql.*;

public class GameDataAccess extends SQLDataAccess {
    private static final ArrayList<dbobjects.GameData> TABLE = new ArrayList<dbobjects.GameData>();


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

    public static chess.ChessGame getGame(int gameID) throws SQLException {
        GameData game = getGameObject(gameID);
        if(game == null) {
            return null;
        }
        return game.game();
    }

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

    public static int newGame(String gameName) throws DataAccessException, SQLException {
        StringBuilder sb = new StringBuilder();
        ChessGame game = new ChessGame();
        int gameID;

        sb = new StringBuilder();
        sb.append("INSERT INTO GAME_DATA\n");
        sb.append("(GAME_NAME, GAME)\n");
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
        TABLE.add(new GameData(gameID, gameName, game));
        return gameID;
    }



    public static void joinGame(dbobjects.UserData user, chess.ChessGame.TeamColor color, int gameID) throws SQLException {
        dbobjects.GameData record = GameDataAccess.getGameObject(gameID);
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE GAME_DATA\n");

        if(color == chess.ChessGame.TeamColor.BLACK){
            record.joinBlack(user.username());
            sb.append("SET BLACK_USERNAME = ?");
        }
        else {
            record.joinWhite(user.username());
            sb.append("SET WHITE_USERNAME = ?");
        }
        sb.append("\nWHERE GAME_ID = " + gameID + ";");

        try(PreparedStatement updateStatement = CONN.prepareStatement(sb.toString())) {
            updateStatement.setString(1, user.username());
            updateStatement.executeUpdate();
        }
        catch(SQLException e) {
            throw new SQLException("There was a problem joining a user to game: " + e.getMessage());
        }
    }

    public static boolean LeaveGame(chess.ChessGame.TeamColor color, int gameID) throws SQLException {
        dbobjects.GameData record = GameDataAccess.getGameObject(gameID);
        if(record == null) {
            return false;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE GAME_DATA\n");

        if(color == chess.ChessGame.TeamColor.BLACK){
            sb.append("SET BLACK_USERNAME = NULL\n");
        }
        else {
            sb.append("SET WHITE_USERNAME = NULL\n");
        }
        sb.append("\nWHERE GAME_ID = " + gameID + ";");

        try(PreparedStatement updateStatement = CONN.prepareStatement(sb.toString())) {
            updateStatement.executeUpdate();
            return true;
        }
        catch(SQLException e) {
            throw new SQLException("There was a problem removing a user from game: " + e.getMessage());
        }
    }

    public static boolean updateGame(int gameID, String newGame) throws SQLException {
        dbobjects.GameData record = GameDataAccess.getGameObject(gameID);
        if(record == null) {
            return false;
        }
        StringBuilder sb = new StringBuilder();

        sb.append("UPDATE GAME_DATA\n");
        sb.append("SET GAME = ?\n");
        sb.append("WHERE GAME_ID = ?;");

        try(PreparedStatement updateStatement = CONN.prepareStatement(sb.toString())) {
            updateStatement.setString(1, newGame);
            updateStatement.setInt(2, gameID);
            updateStatement.executeUpdate();
            return true;
        }
        catch(SQLException e) {
            throw new SQLException("There was a problem updating the game: " + e.getMessage());
        }
    }


    public static void clearGameData() throws SQLException {
        GameDataAccess.TABLE.clear();
        String truncateStatement = "TRUNCATE GAME_DATA;";
        executeUpdateStatement(truncateStatement);
    }
}
