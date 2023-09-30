package ui;

import logger.Level;
import logger.Logger;
import protocol.Protocol;
import protocol.ProtocolListener;

import java.util.Scanner;

public class View implements ProtocolListener {
    private final Printer printer;
    private final Protocol protocol;

    public View() {
        this.protocol = new Protocol(this);
        this.printer = new Printer(this.protocol.getIPAddress());

        this.printer.displayWelcomeMessage();
        // handle sending from stdin from this thread.
        Scanner sc = new Scanner(System.in);
        String input = "";
        while (true) {
            input = sc.nextLine(); // read input
            Logger.log(Level.INFO, "Handling input!");
            handleNodeInput(input);
        }
    }

    public static void main(String[] args) {
        new View();
    }

    private void handleNodeInput(String input) {
        // listen to reach command
        if(input.equalsIgnoreCase("reach")) {
            showReachableNodes();
            return;
        }
        // ignore empty messages
        if(input.equals("")) return;

        // parse arguments check if p2p
        String[] args = input.split(" ");
        boolean p2p = false;
        int destination = 0;
        if(args[0].equalsIgnoreCase("dm")) {
            if(args.length < 3) {
                Printer.printWithColor("Command should have 3 arguments\n", ConsoleColors.YELLOW, false);
                Printer.printDmCommand();
                return;
            }
            try {
                destination = Integer.parseInt(args[1]);
                if(!protocol.getReachableNodes().containsKey(destination)) {
                    Printer.printWithColor("Node not reachable\n", ConsoleColors.YELLOW, false);
                    showReachableNodes();
                    return;
                }
            } catch (NumberFormatException e) {
                Printer.printWithColor("Destination should be a number\n", ConsoleColors.YELLOW, false);
                Printer.printDmCommand();
                return;
            }
            p2p = true;
            String newInput = "";
            for(int i = 2;i < args.length - 1;i++) {
                newInput += args[i] + " ";
            }
            newInput += args[args.length - 1];
            input = newInput;
        }

        protocol.sendMessage(input, p2p, destination);
    }

    private void showReachableNodes() {
        Printer.printWithColor("Reachable nodes: ", ConsoleColors.YELLOW, false);
        for(int node : protocol.getReachableNodes().keySet()) {
            printer.displayIp(node);
        }
        System.out.print("\n");
    }

    @Override
    public void displayMessage(String message, int ip) {
        printer.displayMessage(message, ip);
    }

    @Override
    public void displayP2PMessage(String message, int originIp, int destinationIp) {
        printer.displayDirectMessage(message, originIp, destinationIp);
    }

    @Override
    public void changeOwnIP(int oldIp, int newIp) {
        printer.changeOwnIP(oldIp, newIp);
    }
}
