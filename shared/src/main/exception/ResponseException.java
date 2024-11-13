package exception;

public class ResponseException extends Exception {
    final private int status;

    public ResponseException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
