package server;

import spark.*;
import com.google.gson.Gson;
import request.*;
import service.*;
import response.*;
import dataaccess.*;
import java.util.ArrayList;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::registration);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.delete("/db", this::clearApplication);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        //Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    private Object registration(spark.Request req, spark.Response res) {
        Gson g = new Gson();
        RegisterRequest registerRequest = g.fromJson(req.body(), RegisterRequest.class);
        RegisterResponse registerResponse;
        try {
            registerResponse = service.Service.registration(registerRequest);  
        }
        catch(BadRequestException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            res.status(400);
            return g.toJson(errorResponse);
        }
        catch(AlreadyTakenException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            res.status(403);
            return g.toJson(errorResponse);
        }
        catch(Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            res.status(500);
            return g.toJson(errorResponse);
        }
        res.status(200);
        return g.toJson(registerResponse);
    }

    private Object login(spark.Request req, spark.Response res) {
        Gson g = new Gson();
        LoginRequest loginRequest = g.fromJson(req.body(), LoginRequest.class);
        LoginResponse loginResponse;
        try {
            loginResponse = service.Service.login(loginRequest);
        }
        catch(UnauthorizedException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            res.status(401);
            return g.toJson(errorResponse);
        }
        catch(Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            res.status(500);
            return g.toJson(errorResponse);
        }
        res.status(200);
        return g.toJson(loginResponse);
    }

    private Object logout(spark.Request req, spark.Response res) {
        Gson g = new Gson();
        LogoutRequest logoutRequest = new LogoutRequest(req.headers("authorization"));
        LogoutResponse logoutResponse;
        try {
            logoutResponse = service.Service.logout(logoutRequest);
        }
        catch(UnauthorizedException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            res.status(401);
            return g.toJson(errorResponse);
        }
        catch(Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            res.status(500);
            return g.toJson(errorResponse);
        }
        res.status(200);
        return g.toJson(logoutResponse);
    }

    private Object createGame(Request req, Response res) {
        Gson g = new Gson();
        CreateRequest createRequest = g.fromJson(req.body(), CreateRequest.class);
        createRequest.authToken = req.headers("authorization");
        CreateResponse createResponse;
        try {
            createResponse = service.Service.createGame(createRequest);
        }
        catch(BadRequestException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            res.status(400);
            return g.toJson(errorResponse);
        }
        catch(UnauthorizedException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            res.status(401);
            return g.toJson(errorResponse);
        }
        catch(AlreadyTakenException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            res.status(403);
            return g.toJson(errorResponse);
        }
        catch(Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            res.status(500);
            return g.toJson(errorResponse);
        }
        res.status(200);
        return g.toJson(createResponse);
    }

    private Object listGames(spark.Request req, spark.Response res) {
        Gson g = new Gson();
        ListRequest listRequest = new ListRequest(req.headers("authorization"));
        ListGameResponse listResponse;
        res.type("application/json");
        try {
            listResponse = service.Service.listGames(listRequest);
        }
        catch(UnauthorizedException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            res.status(401);
            return g.toJson(errorResponse);
        }
        catch(AlreadyTakenException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            res.status(403);
            return g.toJson(errorResponse);
        }
        catch(Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            res.status(500);
            return g.toJson(errorResponse);
        }
        res.status(200);
        return g.toJson(listResponse);
    }

    private Object joinGame(spark.Request req, spark.Response res) {
        Gson g = new Gson();
        JoinRequest joinRequest = g.fromJson(req.body(), JoinRequest.class);
        joinRequest.authToken = req.headers("Authorization");
        JoinResponse joinResponse;
        try {
            joinResponse = service.Service.joinGame(joinRequest);
        }
        catch(BadRequestException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            res.status(400);
            return g.toJson(errorResponse);
        }
        catch(UnauthorizedException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            res.status(401);
            return g.toJson(errorResponse);
        }
        catch(AlreadyTakenException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            res.status(403);
            return g.toJson(errorResponse);
        }
        catch(Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            res.status(500);
            return g.toJson(errorResponse);
        }
        res.status(200);
        return g.toJson(joinResponse);
    }

    private Object clearApplication(spark.Request req, spark.Response res) {
        Gson g = new Gson();
        ClearRequest clearRequest;
        ClearResponse clearResponse;
        try {
            clearResponse = service.Service.clearApplication();
        }
        catch(Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            res.status(500);
            return g.toJson(errorResponse);
        }
        res.status(200);
        return g.toJson(clearResponse);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
