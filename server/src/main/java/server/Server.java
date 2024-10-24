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
        //use Spark.halt for error codes. Will send errors back to client. Have it in a catch block for your own exceptions.
        Gson g = new Gson();
        RegisterRequest r = g.fromJson(req.body(), RegisterRequest.class);
        RegisterResponse register_response;
        try {
            register_response = service.Service.registration(r);  
        }
        catch(AlreadyTakenException e) {
            ErrorResponse error_response = new ErrorResponse(e.getMessage());
            res.status(403);
            return g.toJson(error_response);
        }
        catch(Exception e) {
            ErrorResponse error_response = new ErrorResponse(e.getMessage());
            res.status(500);
            return g.toJson(error_response);
        }
        res.status(200);
        return g.toJson(register_response);
    }

    private Object login(spark.Request req, spark.Response res) {
        Gson g = new Gson();
        LoginRequest login_request = g.fromJson(req.body(), LoginRequest.class);
        LoginResponse login_response;
        try {
            login_response = service.Service.login(login_request);
        }
        catch(UnauthorizedException e) {
            ErrorResponse error_response = new ErrorResponse(e.getMessage());
            res.status(401);
            return g.toJson(error_response);
        }
        catch(Exception e) {
            ErrorResponse error_response = new ErrorResponse(e.getMessage());
            res.status(500);
            return g.toJson(error_response);
        }
        res.status(200);
        return g.toJson(login_response);
    }

    private Object logout(spark.Request req, spark.Response res) {
        System.out.println("starting logout");
        Gson g = new Gson();
        LogoutRequest logout_request = g.fromJson(req.body(), LogoutRequest.class);
        logout_request.authtoken = req.headers("authorization");
        LogoutResponse logout_response;
        try {
            System.out.println("In try block");
            logout_response = service.Service.logout(logout_request);
        }
        catch(UnauthorizedException e) {
            ErrorResponse error_response = new ErrorResponse(e.getMessage());
            res.status(401);
            return g.toJson(error_response);
        }
        catch(Exception e) {
            ErrorResponse error_response = new ErrorResponse(e.getMessage());
            res.status(500);
            return g.toJson(error_response);
        }
        res.status(200);
        return g.toJson(logout_response);
    }

    private Object createGame(Request req, Response res) {
        Gson g = new Gson();
        CreateRequest create_request = g.fromJson(req.body(), CreateRequest.class);
        //create_request.authtoken = req.headers("authorization");
        String temp = req.headers("authorization");
        System.out.println(temp);
        CreateResponse create_response;
        try {
            create_response = service.Service.createGame(create_request);
        }
        catch(UnauthorizedException e) {
            ErrorResponse error_response = new ErrorResponse(e.getMessage());
            res.status(401);
            return g.toJson(error_response);
        }
        catch(AlreadyTakenException e) {
            ErrorResponse error_response = new ErrorResponse(e.getMessage());
            res.status(403);
            return g.toJson(error_response);
        }
        catch(Exception e) {
            ErrorResponse error_response = new ErrorResponse(e.getMessage());
            res.status(500);
            return g.toJson(error_response);
        }
        res.status(200);
        return g.toJson(create_response);
    }

    private Object listGames(spark.Request req, spark.Response res) {
        Gson g = new Gson();
        ListRequest list_request = g.fromJson(req.body(), ListRequest.class);
        list_request.authtoken = req.headers("authorization");
        ListGameResponse list_response;
        res.type("application/json");
        try {
            list_response = service.Service.listGames(list_request);
        }
        catch(AlreadyTakenException e) {
            ErrorResponse error_response = new ErrorResponse(e.getMessage());
            res.status(403);
            return g.toJson(error_response);
        }
        catch(Exception e) {
            ErrorResponse error_response = new ErrorResponse(e.getMessage());
            res.status(500);
            return g.toJson(error_response);
        }
        res.status(200);
        return g.toJson(list_response);
    }

    private Object joinGame(spark.Request req, spark.Response res) {
        Gson g = new Gson();
        JoinRequest join_request = g.fromJson(req.body(), JoinRequest.class);
        join_request.authtoken = req.headers("authorization");
        JoinResponse join_response;
        try {
            join_response = service.Service.joinGame(join_request);
        }
        catch(UnauthorizedException e) {
            ErrorResponse error_response = new ErrorResponse(e.getMessage());
            res.status(401);
            return g.toJson(error_response);
        }
        catch(Exception e) {
            ErrorResponse error_response = new ErrorResponse(e.getMessage());
            res.status(500);
            return g.toJson(error_response);
        }
        res.status(200);
        return g.toJson(join_response);
    }

    private Object clearApplication(spark.Request req, spark.Response res) {
        Gson g = new Gson();
        ClearRequest clear_request = g.fromJson(req.body(), ClearRequest.class);
        ClearResponse clear_response;
        try {
            clear_response = service.Service.clearApplication(clear_request);
        }
        catch(Exception e) {
            ErrorResponse error_response = new ErrorResponse(e.getMessage());
            res.status(500);
            return g.toJson(error_response);
        }
        res.status(200);
        return g.toJson(clear_response);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
