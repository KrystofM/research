package pentago.networking.dto;

public class MoveDtoException extends Exception {
    /**
     * Constructs a new exception with the specified detail message and
     * cause.
     */
    public MoveDtoException() {
        super("Incorrect creation of move data transfer object");
    }
}
