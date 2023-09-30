package protocol;

public class ProtocolSetup {
    // The host to connect to. Set this to localhost when using the audio interface tool.
    public static final String SERVER_IP = "netsys.ewi.utwente.nl";
    // The port to connect to. 8954 for the simulation server.
    public static final int SERVER_PORT = 8954;
    // The frequency to use.
    public static final int FREQUENCY = 3000;

    public static final int BACKOFF_TIMEOUT_BASE = 6000;
    public static final int BACKOFF_TIMEOUT_INCREMENT = 2000;

    public static final int REACHABILITY_TTL = 90000;
    public static final int REACHABILITY_UPDATE_TIME = REACHABILITY_TTL*2/3;

    public static final int RETS_FOR_LOST_NEIGHBOUR = 3;

    public static final int RANDOM_TRANSMISSION_DELAY = 500;

    private ProtocolSetup() { throw new IllegalStateException("Utility class"); }
}
