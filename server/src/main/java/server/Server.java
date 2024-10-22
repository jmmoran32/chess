package server;

import spark.*;
import com.google.gson.Gson;
import request.*;
import service.*;
import response.*;

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
        Spark.exception(ResponseException.class, this::exceptionHandler);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    private Object registration(spark.Request req, spark.Response res) {
        //use Spark.halt for error codes. Will send errors back to client. Have it in a catch block for your own exceptions.
        Gson g = new Gson();
        RegisterRequest r = g.fromJson(req.body(), RegisterRequest.class);
        RegisterResponse register_response = service.Service.registration(r);  
        res.status(200);
        return g.toJson(register_response);
    }


    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
