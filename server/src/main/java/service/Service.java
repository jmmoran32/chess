package service;

import request.*;
import response.*;
import dataaccess.*;
import java.util.UUID;
import dbobjects.*;
import java.util.ArrayList;

public class Service {
    private static int nextGameID = 1;
    public static RegisterResponse registration(RegisterRequest req) throws DataAccessException {
        dbobjects.UserData user = UserDataAccess.getUser(req.username); 
        if(user != null)
            throw new AlreadyTakenException("Error: already taken");
        String uuid = createAuthData();
        AuthDataAccess.createAuth(uuid, req.username);
        UserDataAccess.createUser(req.username, req.password, req.email);
        return new RegisterResponse(req.username, uuid);
    }

    public static LoginResponse login(LoginRequest req) throws DataAccessException {
        dbobjects.UserData user = UserDataAccess.getUser(req.username);
        if(user == null)
            throw new DataAccessException(String.format("Error: no user with username: %s found", req.username));
        if(!user.password().equals(req.password))
            throw new UnauthorizedException("Error: unauthorized");
        String uuid = createAuthData();
        AuthDataAccess.createAuth(uuid, user.username());
        return new LoginResponse(req.username, uuid);
    }

    private static String createAuthData() {
        return UUID.randomUUID().toString();
    }

    //private static boolean matchHash(String password, long hash) {}

    private static boolean authenticate(String token) {
        String auth = AuthDataAccess.getAuthToken(token);
        if(auth == null)
            return false;
        else
            return true;
    }

    public static LogoutResponse logout(LogoutRequest req) throws DataAccessException {
        if(!authenticate(req.authtoken))
            throw new UnauthorizedException("Error: unauthorized");
        if(!AuthDataAccess.deleteAuthToken(req.authtoken))
            throw new DataAccessException("Error: Server Error: unable to delete authtoken");
        return new LogoutResponse("");
    }

   //public ArrayList<dbobjects.GameData> listGames(ListRequest req) {}

    public static CreateResponse createGame(CreateRequest req) throws DataAccessException{
        if(!authenticate(req.authtoken))
            throw new UnauthorizedException("Error: unauthorized");
        GameDataAccess.newGame(String.format("game%d", nextGameID), nextGameID);
        return new CreateResponse(Integer.toString(nextGameID++));
    }

    public static JoinResponse joinGame(JoinRequest req) throws DataAccessException {
        if(!authenticate(req.authtoken))
            throw new UnauthorizedException("Error: unauthorized");
        String username = AuthDataAccess.getUsername(req.authtoken);
        dbobjects.UserData user = UserDataAccess.getUser(username);
        if(user == null)
            throw new DataAccessException("Error: User isn't found");
        chess.ChessGame game = GameDataAccess.getGame(req.gameID);
        GameDataAccess.joinGame(user, req.color, req.gameID);
        return new JoinResponse("");
    }

    public static ListGameResponse listGames(ListRequest req) throws DataAccessException {
        ListGameResponse response = new ListGameResponse(new ArrayList<ChessGameRecord>());
        ArrayList<dbobjects.GameData> table = GameDataAccess.getGames();
        for(dbobjects.GameData r : table) {
            response.games().add(new ChessGameRecord(Integer.toString(r.gameID()), r.whiteUsername(), r.blackUsername(), r.game()));
        }
        return response;
    }

    public static ClearResponse clearApplication(ClearRequest req) throws DataAccessException {
        UserDataAccess.clearUsers();
        AuthDataAccess.clearAuth();
        GameDataAccess.clearGameData();
        return new ClearResponse("");
    }
}
