package server;

import request.*;
import response.*;
import com.google.gson.Gson;
import java.io.*;
import java.net.*;
import exception.ResponseException;
import dbobjects.GameData;
import java.util.ArrayList;
import chess.ChessGame;

public class ServerFacade {
    private String url;

    public ServerFacade(String url) {
        this.url = url;
    }

    public String registration(String username, String password, String email) {
        RegisterRequest req = new RegisterRequest(username, password, email);
        RegisterResponse res = this.makeRequest("POST", "/user", req, RegisterResponse.class); 
        return res.authToken();
    }

    public String login(String username, String password) {
    }

    public void logout(String authToken) {
    }

    public ArrayList<GameData> listGames(String authToken) {
    }

    public int createGame(String authToken, String gameName) {
    }

    public void joinGame(String authToken, ChessGame.TeamColor color, int gameID) {
    }

    public void clearApplication() {
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws Exception {
        try {
            URL url = (new URI(url + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
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
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            throw new ResponseException(status, "failure: " + status);
        }
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
