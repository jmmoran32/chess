package facade;

import com.google.gson.Gson;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import chess.ChessGame;
import java.util.Map;

public class ServerFacade {
    private String url;

    public ServerFacade(String url) {
        this.url = url;
    }

    public String registration(String username, String password, String email) throws ResponseException {
        RegisterRequest req = new RegisterRequest(username, password, email);
        RegisterResponse res = makeRequest("POST", "/user", req, RegisterResponse.class, null); 
        return res.authToken();
    }

    public String login(String username, String password) throws ResponseException {
        LoginRequest req = new LoginRequest(username, password);
        LoginResponse res = makeRequest("POST", "/session", req, LoginResponse.class, null);
        return res.authToken();
    }

    public void logout(String authToken) throws ResponseException {
        LogoutRequest req = new LogoutRequest();
        LogoutResponse res = makeRequest("DELETE", "/session", req, LogoutResponse.class, authToken);
    }

    public ArrayList<ChessGame> listGames(String authToken) throws ResponseException {
        ListRequest req = new ListRequest();
        ListGameResponse res = makeRequest("GET", "/game", req, ListGameResponse.class, authToken);

        ArrayList<ChessGame> arr = new ArrayList<ChessGame>();;
        for(ChessGameRecord r : res.games()) {
            arr.add(r.game());
        }
        return arr;
    }

    public String createGame(String authToken, String gameName) throws ResponseException {
        CreateRequest req = new CreateRequest(gameName);
        CreateResponse res = makeRequest("POST", "/game", req, CreateResponse.class, authToken);
        return res.gameID();
    }

    /*
    public void joinGame(String authToken, ChessGame.TeamColor color, int gameID) {
    }

    public void clearApplication() {
    }
    */

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws ResponseException {
        try {
            URL url = (new URI(this.url + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);
            if(authToken != null) {
                http.setRequestProperty("Authorization", authToken);
            }

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } 
        catch(ResponseException e) {
            throw e;
        }
        catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        int status = http.getResponseCode();
        if (!isSuccessful(status)) { 
            try(InputStream err = http.getErrorStream()) {
                InputStreamReader reader = new InputStreamReader(err);
                Map<?, ?> map = new Gson().fromJson(reader, Map.class);
                String messageStream = (String) map.get("message");
                String message = http.getResponseMessage();
                if(message == null) {
                    throw new ResponseException(500, "An unknown error occurred");
                }
                else if(message.contains("Bad Request")) {
                    throw new ResponseException(400, message);
                }
                else if(message.contains("Unauthorized")) {
                    throw new ResponseException(401, message);
                }
                else if(message.contains("Already Taken")) {
                    throw new ResponseException(403, message);
                }
                else {
                    throw new ResponseException(status, "An undefined exception occurred in facade: " + message);
                }
            }
            //throw new ResponseException(status, "failure: " + status);
        }
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
