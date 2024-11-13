package server;

import spark.*;
import com.google.gson.Gson;
import request.*;
import service.*;
import response.*;
import dataaccess.*;
import java.util.ArrayList;
import java.sql.SQLException;

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
        Spark.exception(Exception.class, this::exceptionHandler);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        //Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    private Object exceptionHandler(Exception e, spark.Request req, spark.Response res) {
        Gson g = new Gson();
        try {
            throw e;
        }
        catch(BadRequestException ex) {
           res.status(400);
           ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
           res.body(g.toJson(errorResponse));
           return g.toJson(errorResponse);
        }
        catch(AlreadyTakenException ex) {
           res.status(403);
           ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
           res.body(g.toJson(errorResponse));
           return g.toJson(errorResponse);
        }
        catch(UnauthorizedException ex) {
           res.status(401);
           ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
           res.body(g.toJson(errorResponse));
           return g.toJson(errorResponse);
        }
        catch(Exception ex) {
           res.status(500);
           ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
           res.body(g.toJson(errorResponse));
           return g.toJson(errorResponse);
        }
    }

    private Object registration(spark.Request req, spark.Response res) throws DataAccessException, SQLException {
        Gson g = new Gson();
        RegisterRequest registerRequest = g.fromJson(req.body(), RegisterRequest.class);
        RegisterResponse registerResponse;
        registerResponse = service.Service.registration(registerRequest);  
        res.status(200);
        return g.toJson(registerResponse);
    }

    private Object login(spark.Request req, spark.Response res) throws DataAccessException, SQLException {
        Gson g = new Gson();
        LoginRequest loginRequest = g.fromJson(req.body(), LoginRequest.class);
        LoginResponse loginResponse;
        loginResponse = service.Service.login(loginRequest);
        res.status(200);
        return g.toJson(loginResponse);
    }

    private Object logout(spark.Request req, spark.Response res) throws DataAccessException, SQLException{
        Gson g = new Gson();
        LogoutRequest logoutRequest = new LogoutRequest(req.headers("authorization"));
        LogoutResponse logoutResponse;
        logoutResponse = service.Service.logout(logoutRequest);
        res.status(200);
        return g.toJson(logoutResponse);
    }

    private Object createGame(Request req, Response res) throws DataAccessException, SQLException{
        Gson g = new Gson();
        CreateRequest createRequest = g.fromJson(req.body(), CreateRequest.class);
        createRequest.authToken = req.headers("authorization");
        CreateResponse createResponse;
        createResponse = service.Service.createGame(createRequest);
        res.status(200);
        return g.toJson(createResponse);
    }

    private Object listGames(spark.Request req, spark.Response res) throws DataAccessException, SQLException {
        Gson g = new Gson();
        ListRequest listRequest = new ListRequest(req.headers("authorization"));
        ListGameResponse listResponse;
        res.type("application/json");
        listResponse = service.Service.listGames(listRequest);
        res.status(200);
        return g.toJson(listResponse);
    }

    private Object joinGame(spark.Request req, spark.Response res) throws DataAccessException, SQLException {
        Gson g = new Gson();
        JoinRequest joinRequest = g.fromJson(req.body(), JoinRequest.class);
        joinRequest.authToken = req.headers("Authorization");
        JoinResponse joinResponse;
        joinResponse = service.Service.joinGame(joinRequest);
        res.status(200);
        return g.toJson(joinResponse);
    }

    private Object clearApplication(spark.Request req, spark.Response res) throws DataAccessException, SQLException {
        Gson g = new Gson();
        ClearRequest clearRequest;
        ClearResponse clearResponse;
        clearResponse = service.Service.clearApplication();
        res.status(200);
        return g.toJson(clearResponse);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
