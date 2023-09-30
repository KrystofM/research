package pentago.networking.models;

/**
 * Utility public class with strings
 * as the only part of it. Used over
 * enum because of having good use
 * when dealing with switch case
 * that are based on Strings.
 */
public class Command {
    /**
     * Private constructor to hide the implicit
     * public one.
     */
    private Command() {
        throw new IllegalStateException("Utility class");
    }

    public static final String HELLO = "HELLO";
    public static final String LOGIN = "LOGIN";
    public static final String ALREADYLOGGEDIN = "ALREADYLOGGEDIN";
    public static final String QUEUE = "QUEUE";
    public static final String NEWGAME = "NEWGAME";
    public static final String MOVE = "MOVE";
    public static final String GAMEOVER = "GAMEOVER";
    public static final String QUIT = "QUIT";
    public static final String PING = "PING";
    public static final String PONG = "PONG";
    public static final String HELP = "HELP";
    public static final String LIST = "LIST";
    public static final String ERROR = "ERROR";
}
