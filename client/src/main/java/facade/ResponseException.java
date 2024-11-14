package facade;

public class ResponseException extends Exception {
    private int status;

    public ResponseException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {return this.status;}
}
