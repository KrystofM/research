package ui;

public class ConsoleColors {
    static final String COLOR_BASE = "\u001B[";
    static final String BLACK = COLOR_BASE + "30m";
    static final String RED = COLOR_BASE + "31m";
    static final String GREEN = COLOR_BASE + "32m";
    static final String YELLOW = COLOR_BASE + "38;5;230m";
    static final String BLUE = COLOR_BASE + "34m";
    static final String PURPLE = COLOR_BASE + "35m";
    static final String CYAN = COLOR_BASE + "36m";
    static final String WHITE = COLOR_BASE + "37m";

    private ConsoleColors() { throw new IllegalStateException("Utility class"); }
}
