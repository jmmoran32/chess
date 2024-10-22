package service;

import request.*;
import response.*;
import dataaccess.*;
import java.util.UUID;

public class Service {
    public static RegisterResponse registration(RegisterRequest req) throws DataAccessException {
        UserDataAccess.createUser(req.username, req.password, req.email);
        //if null, call createAuthData
        String uuid = createAuthData();
        return new RegisterResponse(req.username, uuid);
    }

    private String login(LoginRequest req) {}

    private static String createAuthData() {
        return UUID.randomUUID().toString();
    }

    private boolean matchHash(String password, long hash) {}

    private boolean authenticate(String Token) {}

    public boolean logout(LogoutRequest req) {}

    public String[] listGames(ListRequest req) {}

    public String createGame(CreateRequest req) {}

    public boolean joinGame(JoinRequest req) {}

    public boolean clearApplication(ClearRequest req) {}
}
