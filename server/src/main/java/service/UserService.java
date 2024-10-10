package Service;

import request.*;

public class UserService {
    private String registration(RegisterRequest req) {}

    private String login(LoginRequest req) {}

    private boolean matchHash(String password, long hash) {}

    private boolean authenticate(String Token) {}

    public boolean logout(LogoutRequest req) {}

    public String[] listGames(ListRequest req) {}

    public String createGame(CreateRequest req) {}

    public boolean joinGame(JoinRequest req) {}

    public boolean clearApplication(ClearRequest req) {}
}
