package service;

import request.*;
import response.*;
import dataaccess.*;
import java.util.UUID;
import dbobjects.*;
import java.util.ArrayList;
import java.sql.SQLException;

public class Service {
    private static int nextGameID;

    static {
        nextGameID = 1;
    }
    public static RegisterResponse registration(RegisterRequest req) throws DataAccessException, SQLException {
        if(req.username == null || req.password == null || req.email == null) {
            throw new BadRequestException("Error: bad request");
        }
        String uuid = createAuthData();
        UserDataAccess.createUser(req.username, req.password, req.email);
        AuthDataAccess.createAuth(uuid, req.username);
        return new RegisterResponse(req.username, uuid);
    }

    public static LoginResponse login(LoginRequest req) throws DataAccessException, SQLException {
        dbobjects.UserData user = UserDataAccess.getUser(req.username);
        if(user == null) {
            throw new UnauthorizedException(String.format("Error: no user with username: %s found", req.username));
        }
        if(!UserDataAccess.checkPass(req.password, user.password())) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        String uuid = createAuthData();
        AuthDataAccess.createAuth(uuid, user.username());
        return new LoginResponse(req.username, uuid);
    }

    private static String createAuthData() {
        return UUID.randomUUID().toString();
    }

    //private static boolean matchHash(String password, long hash) {}

    private static boolean authenticate(String token) throws SQLException {
        String auth = AuthDataAccess.getAuthToken(token);
        if(auth == null)
            return false;
        else
            return true;
    }

    public static LogoutResponse logout(LogoutRequest req) throws DataAccessException, SQLException {
        if(!authenticate(req.authToken)) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        if(!AuthDataAccess.deleteAuthToken(req.authToken)) {
            throw new DataAccessException("Error: Server Error: unable to delete authtoken");
        }
        return new LogoutResponse("");
    }

    public static CreateResponse createGame(CreateRequest req) throws DataAccessException, SQLException {
        int gameID;
        if(req.authToken == null || req.gameName == null)
            throw new BadRequestException("Error: bad request");
        if(!authenticate(req.authToken))
            throw new UnauthorizedException("Error: unauthorized");
        gameID = GameDataAccess.newGame(req.gameName);
        return new CreateResponse(Integer.toString(gameID));
    }

    public static JoinResponse joinGame(JoinRequest req) throws DataAccessException, SQLException {
        if(req.authToken == null || req.playerColor == null || req.gameID == null)
            throw new BadRequestException("Error: bad request");
        if(!authenticate(req.authToken))
            throw new UnauthorizedException("Error: unauthorized");
        String username = AuthDataAccess.getUsername(req.authToken);
        dbobjects.UserData user = UserDataAccess.getUser(username);
        if(user == null)
            throw new DataAccessException("Error: User isn't found");
        chess.ChessGame.TeamColor requestColor;
        dbobjects.GameData gameObject = GameDataAccess.getGameObject(Integer.parseInt(req.gameID));
        if(req.playerColor.equals("BLACK")) {
            requestColor = chess.ChessGame.TeamColor.BLACK;
            if(gameObject.blackUsername() != null)
                throw new AlreadyTakenException("Error: Black team already taken");
        }
        else if(req.playerColor.equals("WHITE")) {
            requestColor = chess.ChessGame.TeamColor.WHITE;
            if(gameObject.whiteUsername() != null)
                throw new AlreadyTakenException("Error: White team already taken");
        }
        else
            throw new BadRequestException("Error: bad request: invalid team color");

        chess.ChessGame game = GameDataAccess.getGame(Integer.parseInt(req.gameID));
        if(game == null)
            throw new DataAccessException("Error: Game isn't found");
        GameDataAccess.joinGame(user, requestColor, Integer.parseInt(req.gameID));
        return new JoinResponse("");
    }

    public static ListGameResponse listGames(ListRequest req) throws DataAccessException, SQLException {
        if(!authenticate(req.authtoken))
            throw new UnauthorizedException("Error: unauthorized");
        ListGameResponse response = new ListGameResponse(new ArrayList<ChessGameRecord>());
        ArrayList<dbobjects.GameData> table = GameDataAccess.getGames();
        for(dbobjects.GameData r : table) {
            response.games().add(new ChessGameRecord(Integer.toString(r.gameID()), r.whiteUsername(), r.blackUsername(), r.gameName(), r.game()));
        }
        return response;
    }

    public static ClearResponse clearApplication() throws DataAccessException, SQLException {
        UserDataAccess.clearUsers();
        AuthDataAccess.clearAuth();
        GameDataAccess.clearGameData();
        return new ClearResponse();
    }
}
