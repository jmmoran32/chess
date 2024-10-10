package Service;

public class UserService {
    private String registration(registerRequest req) {}

    private String login(loginRequest req) {}

    private boolean matchHash(String password, long hash) {}

    private boolean authenticate(Token t) {}

    public boolean logout(LogoutRequest req) {}

    public String[] listGames(ListRequest req) {}

    public String createGame(CreateRequest req) {}

    public boolean joinGame(JoinRequest req) {}

    public boolean clearApplication(ClearRequest req) {}
}
