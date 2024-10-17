package dataaccess;

import dbobjects.UserData;
import dbobjects.GameData;
import chess.ChessGame;
import chess.ChessGame;
import java.util.ArrayList;

public class GameDataAccess {
    private static final ArrayList<dbobjects.GameData> table = new ArrayList<dbobjects.GameData>();

    public static Object getGames() {
        return table.clone();
    }

    public static void newGame(String gameName, int gameID) throws DataAccessException {
        for(dbobjects.GameData record : table)
            if(record.gameID() == gameID)
                throw new DataAccessException(String.format("A game with gameID %d already exists in GameData as record no %ld", record.gameID(), record.id()));
        table.add(new dbobjects.GameData(gameID, gameName, new chess.ChessGame()));
    }

    public static dbobjects.GameData getGame(int gameID) {
        for(dbobjects.GameData record : table)
            if(record.gameID() == gameID)
                return record;
        return null;
    }

    public static void joinGame(dbobjects.UserData user, chess.ChessGame.TeamColor color, int gameID) throws DataAccessException {
        dbobjects.GameData record = GameDataAccess.getGame(gameID);
        if(color == ChessGame.TeamColor.BLACK)
            record.joinBlack(user.username());
        else
            record.joinWhite(user.username());
    }

    public static void DeleteGames() {GameDataAccess.table.clear();}

    public static void write() {
        throw new RuntimeException("Not implemented");
    }
}
