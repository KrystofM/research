package ui;

import protocol.ProtocolSetup;
import protocol.reachability.ReachabilityTimeout;

import java.util.HashMap;
import java.util.Map;

public class Printer {
    public static final String RESET = "\033[0m";  // Text Reset
    public static final String BOLD = "\u001b[1m";
    public static final String BRACKET_COLOR = "\u001b[38;5;247m";
    public static final int OUR_COLOR = 12; // color for us
    public static final String START_BRACKET = BOLD + BRACKET_COLOR + "[" + RESET;
    public static final String END_BRACKET = BOLD + BRACKET_COLOR + "]" + RESET;
    public final Map<Integer, Integer> colorsToIPMap; // colors for other nodes that we receive messages from
    private static final int[] availableColors = new int[]{226, 208, 196, 50, 118, 277, 141};//yellow, orange, red, cyan, green, pink, purple
    private int ownIP;

    public Printer(int ownIP) {
        colorsToIPMap = new HashMap<>();
        this.ownIP= ownIP;
        colorsToIPMap.put(ownIP, OUR_COLOR);
    }

    public static void main(String[] args) {
        Printer printer = new Printer(1);
        printer.displayWelcomeMessage();
        printer.displayMessage("Hello", 1);
        printer.displayMessage("Hi", 144);
        printer.displayMessage("Did you know that Hessel sucks?", 12);
        printer.displayMessage("Thats common knowledge", 11);
        printer.displayMessage("Couldnt be the any other way", 144);
        printer.displayMessage("I agree.", 12);
        printer.displayMessage("Yeah, I just wanted to let you guys know.", 12);
    }

    public void changeOwnIP(int oldIP, int newIP) {
        this.ownIP = newIP;
        colorsToIPMap.remove(oldIP);
        colorsToIPMap.put(newIP, OUR_COLOR);

        printWithColor("Your new IP is " +newIP, ConsoleColors.GREEN, false);
    }

    public void displayDirectMessage(String message, int originIp, int destinationIp) {
        String originIpFormatted = formatIp(originIp, "\u001b[38;5;" + getColorCode(originIp) + "m");
        String destinationIpFormatted = formatIp(destinationIp, "\u001b[38;5;" + getColorCode(destinationIp) + "m");

        System.out.println(originIpFormatted + BRACKET_COLOR + "->" + RESET + destinationIpFormatted + " " + stringWithColor(message, getColorCode(originIp), true));
    }

    public void displayMessage(String message, int originIp) {
        int colorCode = getColorCode(originIp);
        String color = "\u001b[38;5;" + colorCode + "m";//

        String formattedIp = formatIp(originIp, color);

        System.out.println(formattedIp + " " + stringWithColor(message, colorCode, true));
    }

    private int getColorCode(int ip) {
        int colorCode;

        if (colorsToIPMap.containsKey(ip)) {
            colorCode = colorsToIPMap.get(ip);
        } else {
            colorCode = availableColors[(colorsToIPMap.size() - 1) % availableColors.length];
            colorsToIPMap.put(ip, colorCode);
        }

        return colorCode;
    }

    private String formatIp(int ip, String color) {
        return BOLD + BRACKET_COLOR + "[" + RESET + BOLD + color + addZeros(ip) + ip + RESET + BOLD + BRACKET_COLOR + "]" + RESET;
    }

    public void displayIp(int ip) {
        int colorCode;

        if (colorsToIPMap.containsKey(ip)) {
            colorCode = colorsToIPMap.get(ip);
        } else {
            colorCode = availableColors[(colorsToIPMap.size() - 1) % availableColors.length];
            colorsToIPMap.put(ip, colorCode);
        }

        System.out.print(stringWithColor(ip + " ", colorCode, true));
    }

    public void displayWelcomeMessage() {
        printWithColor("Welcome to communication under water with the ", ConsoleColors.YELLOW, false);
        printWithColor("DAP", ConsoleColors.RED, true);
        printWithColor(" protocol.\n", ConsoleColors.YELLOW, false);
        printWithColor("Your ip in the groupchat will be: ", ConsoleColors.YELLOW, false);
        System.out.println(stringWithColor("" + ownIP, OUR_COLOR, true));
        printReachCommand();
        printDmCommand();
        System.out.print("\n");
    }

    public static void printReachCommand() {
        printWithColor("Type ", ConsoleColors.YELLOW, false);
        printWithColor("REACH ",ConsoleColors.RED, true);
        printWithColor("to show available nodes (time to converge ~" + (ProtocolSetup.REACHABILITY_TTL /1000) + " seconds)\n", ConsoleColors.YELLOW, false);
    }

    public static void printDmCommand() {
        printWithColor("Type", ConsoleColors.YELLOW, false);
        printWithColor(" DM ", ConsoleColors.RED, true);
        System.out.print(START_BRACKET);
        printWithColor("DEST" + END_BRACKET + " ", ConsoleColors.GREEN, true);
        System.out.print(START_BRACKET);
        printWithColor("MSG" + END_BRACKET, ConsoleColors.BLUE, true);
        printWithColor(" to send a direct message to the node with ip", ConsoleColors.YELLOW, false);
        printWithColor(" DEST\n", ConsoleColors.GREEN, true);
    }

    public static void printWithColor(String message, String consoleColor, boolean bold) {
        String b = bold ? BOLD : "";
        System.out.print(consoleColor + b + message + RESET);
    }

    public static String stringWithColor(String message, int colorCode, boolean bold) {
        String b = bold ? BOLD : "";
        return ("\u001b[38;5;" + colorCode + "m" + b + message + RESET);
    }

    public static String addZeros(int ip) {
        if(ip < 10) return "00";
        if(ip < 100) return "0";
        else return "";
    }

}

