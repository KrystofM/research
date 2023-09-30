package logger;

public class Logger {
    //whether we display debugging messages of different levels or not
    private static final boolean DEBUG = false; //display ALL logs

    private static final boolean ERROR = true; //display error messages
    private static final boolean SEVERE = true; //display severe messages
    private static final boolean INFO = false; //display info messages
    private static final boolean STATUS = false; //display status messages
    private static final boolean PACKET = false; //print packets when create

    private Logger() { throw new IllegalStateException("Utility class"); }

    public static void log(Level lvl, String log) {
        if (DEBUG || ERROR && lvl == Level.ERROR || SEVERE && lvl == Level.SEVERE
                || INFO && lvl == Level.INFO || STATUS && lvl == Level.STATUS || PACKET && lvl == Level.PACKET) {
            System.out.println("["+lvl+"] "+ log);
        }
    }
}
