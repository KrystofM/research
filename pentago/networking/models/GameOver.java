package pentago.networking.models;

/**
 * Utility public class with strings
 * as the only part of it. Used over
 * enum because of having good use
 * when dealing with switch case
 * that are based on Strings.
 */
public class GameOver {
    /**
     * Private constructor to hide the implicit
     * public one.
     */
    private GameOver() {
        throw new IllegalStateException("Utility class");
    }

    public static final String VICTORY = "VICTORY";
    public static final String DISCONNECT = "DISCONNECT";
    public static final String DRAW = "DRAW";
}
