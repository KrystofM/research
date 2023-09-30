package pentago.networking.client;

import pentago.game.AIPlayer;
import pentago.game.HumanPlayer;
import pentago.game.PentagoBoard;
import pentago.networking.dto.MoveDto;
import pentago.networking.dto.MoveDtoException;
import pentago.networking.mappers.MoveMapper;
import pentago.game.models.*;
import pentago.networking.models.Command;
import pentago.networking.models.GameOver;
import pentago.utils.TextIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Client is the player who plays Pentago
 * and communicates with the server itself.
 */
public class Client implements Runnable {
    private static final String DESCRIPTION = "Mother of Clients";
    /**
     * Socket connection.
     */
    private Socket socket;

    /**
     * Run state of the client.
     */
    private boolean run;

    /**
     * Out and in connections.
     */
    private PrintWriter out;
    private BufferedReader in;

    /**
     * Board that the game is being played on.
     */
    private PentagoBoard board;

    /**
     * State of the client determines what
     * can be done and what is not possible.
     */
    private ClientState state;

    /**
     * Player that can make moves.
     */
    private PlayerBody player;

    /**
     * Stores the player name when player not available.
     */
    private String playerName;

    public static void main(String[] args) {
        Client client = new Client();
        boolean isConnected = false;
        while (!isConnected) {
            System.out.println("\n> What port of the server?");
            int serverPort = TextIO.getlnInt();
            System.out.println("\n> Next what is the address of the server?");
            String serverAddress = TextIO.getlnString();
            try {
                if (!client.connect(InetAddress.getByName(serverAddress), serverPort)) {
                    System.out.println("\n ERROR: Failed to connect! Try again.");
                }
                isConnected = true;
                System.out.println("\n Connected!");
            } catch (UnknownHostException e) {
                System.out.println("\n ERROR: Failed to find the Server Address! Try again.");
            }
        }
    }

    /**
     * Creates a new Client object.
     */
    public Client() {
        this.run = false;
        this.board = new PentagoBoard();
        this.state = ClientState.INITIAL;
    }

    /**
     * Creates a new Socket object, initialize a connection with a server given the destination
     * and start the client.
     *
     * @param address InetAdrress of the destination
     * @param port port number of the server
     * @return boolean value based on connection success
     */
    public boolean connect(InetAddress address, int port) {
        try {
            this.socket = new Socket(address, port);
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(this).start();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Terminate the client by closing the socket and
     * setting global boolean variable run to false.
     */
    public void close() {
        try {
            this.socket.close();
            this.run = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Run method from Runnable starts the client and waits
     * for messages as long as it is on.
     */
    @Override
    public void run() {
        sendCommand(Command.HELLO, DESCRIPTION);
        setState(ClientState.HELLO_AWAITING);
        this.run = true;
        while (run) {
            try {
                String msg = in.readLine();
                if (msg == null) {
                    this.close();
                    continue;
                }
                handleMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
                this.close();
            }
        }
    }

    /**
     * Sends a command to the connected server
     * and can be used with multiple arguments
     * that are handled by the standard defined
     * in the protocol for sending messages.
     *
     * @param command string version of Command
     * @param arguments whatever string name/move etc.
     */
    private void sendCommand(String command, String... arguments) {
        String msg = command;
        for (String argument : arguments) {
            msg += "~" + argument;
        }
        System.out.println("Sending command: " + msg);
        this.sendMessage(msg);
    }

    /**
     * Wrapper method for sending messages
     * very useful in case there would be
     * some kind of changes to the way
     * messages are sent.
     *
     * @param msg raw message
     */
    private void sendMessage(String msg) {
        out.println(msg);
    }

    /**
     * Helper method to resolving username
     * and logging into the server. Used by
     * handleHello() and handleAlreadyLoggedIn()
     */
    private void getUsernameAndLogin() {
        String playerNewName = TextIO.getlnString();
        this.playerName = playerNewName;
        sendCommand(Command.LOGIN, playerNewName);
        setState(ClientState.LOGIN_AWAITING);
    }

    /**
     * Function for handling getting the
     * hello message back from server. Asks
     * for username and makes a login.
     */
    private void handleHello() {
        if (state.equals(ClientState.HELLO_AWAITING)) {
            System.out.println("\n> Welcome player, first what is your name?");
            this.getUsernameAndLogin();
        }
    }

    /**
     * Function for handling getting the
     * alreadylogged message back from server.
     * Asks for username again and makes a login.
     */
    private void handleAlreadyLoggedIn() {
        System.out.println("Unfortunately the name you chose is already taken. Try again:");
        this.getUsernameAndLogin();
    }

    /**
     * Function for handling getting the
     * login confirmation message back from
     * server. For the first time lets
     * the user decide what command to do.
     */
    private void handleLogin() {
        if (state.equals(ClientState.LOGIN_AWAITING)) {
            setState(ClientState.DECISION_STAGE);
            this.letUserDecide();
        }
    }

    /**
     * Function for handling getting the newgame
     * command from server. Creates an AI player
     * if specified and handles making the first
     * move of the game.
     *
     * @param arguments split arguments from server
     */
    private void handleNewgame(String[] arguments) {
        if (state.equals(ClientState.NEWGAME_AWAITING)) {
            // We are the first player
            boolean playingFirst = arguments[1].equals(playerName);
            if (playingFirst) {
                System.out.println("New game with " + arguments[2] + " started!");
            } else {
                System.out.println("New game with " + arguments[1] + " started!");
            }

            // Asks if AI should play instead
            System.out.println("Do you want AI to play instead of you? " +
                                "Type YES, or anything else if not.");
            Scanner scanner = new Scanner(System.in);
            String aiPlay = scanner.nextLine().toUpperCase();
            if (aiPlay.equals("YES")) {
                player = new AIPlayer(playerName, null);
            } else {
                player = new HumanPlayer(playerName, null);
            }

            // Sets mark and waits or makes a move
            if (playingFirst) {
                player.setMark(Mark.BLACK);
                this.makeMove();
                setState(ClientState.MOVE_AWAITING);
            } else {
                player.setMark(Mark.WHITE);
                System.out.println("Waiting for the other player to play.");
                setState(ClientState.MOVE_AWAITING);
            }
        }
    }


    /**
     * Handled actual determination of move
     * and sending of move to the server. It
     * is used in many function as a wrapper
     * around making an actual move.
     */
    private void makeMove() {
        Move move = player.determineMove(board);
        MoveDto moveDto;
        try {
            moveDto = MoveMapper.toMoveDto(move);
        } catch (MoveDtoException e) {
            System.out.println("ERROR: This is an invalid move!");
            return;
        }
        sendCommand(
                Command.MOVE,
                Integer.toString(moveDto.getA()),
                Integer.toString(moveDto.getB())
        );
    }

    /**
     * Wrapper function for handling the receivable
     * of a move from the server. Creates an
     * appropriate new object from DTO and calls
     * the board to update accordingly.
     *
     * @param a field index
     * @param b rotation integer
     */
    private void receiveMove(int a, int b) {
        MoveDto moveDto;
        try {
            moveDto = new MoveDto(a, b);
        } catch (MoveDtoException e) {
            System.out.println("ERROR: Server send an invalid move!");
            return;
        }
        Move move = MoveMapper.toMove(moveDto);
        this.board.makeAutomatedMove(move);
        System.out.println(this.board.printBoard());
    }

    /**
     * Function for handling getting the move
     * command from server. Playes the move
     * on local board and returns when game
     * is over or goes and determines the next
     * move. Combines the methods makeMove()
     * and receiveMove().
     *
     * @param arguments split arguments from server
     */
    private void handleMove(String[] arguments) {
        if (state.equals(ClientState.MOVE_AWAITING)) {
            // Play the move on the board
            this.receiveMove(Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]));

            // Do not play anymore if game is already done
            if (this.board.isGameOver()) {
                return;
            }

            // Determine/Ask the next move if we are supposed to play
            if (this.board.getMoveCounter() % 2 == 0 && player.getMark() == Mark.BLACK ||
                    this.board.getMoveCounter() % 2 == 1 && player.getMark() == Mark.WHITE) {
                this.makeMove();
            } else {
                System.out.println("Waiting for the other player to play.");
            }
        }
    }

    /**
     * Function for handling getting the game over
     * command from server. Prints out state and
     * resets the board and state.
     *
     * @param arguments split arguments from server
     */
    private void handleGameOver(String[] arguments) {
        String playerWon = "Player " + arguments[2] + " has won!";
        switch (arguments[1]) {
            case GameOver.VICTORY:
                if (arguments[2].equals(playerName)) {
                    System.out.println("Victory! " + playerWon);
                } else {
                    System.out.println("Lost! " + playerWon);
                }
                break;
            case GameOver.DISCONNECT:
                System.out.println("Disconnect. " + playerWon);
                break;
            case GameOver.DRAW:
                System.out.println("Draw. There is no winner!");
                break;
            default:
                System.out.println("ERROR: Client does not recognize command from server.");
                break;
        }
        this.board.reset();
        setState(ClientState.DECISION_STAGE);
        this.letUserDecide();
    }

    /**
     * Function for handling getting the pong
     * command from server. Prints to console.
     */
    private void handlePong() {
        System.out.println("PONG");
        this.letUserDecide();
    }

    /**
     * Function for handling getting the list
     * command from server. Prints to console
     * the list of all players active on server.
     *
     * @param arguments split arguments from server
     */
    private void handleList(String[] arguments) {
        System.out.println("Players active: ");
        for (int i = 1; i < arguments.length; i++) {
            System.out.println(arguments[i]);
        }
        this.letUserDecide();
    }

    /**
     * Function for handling getting the quit
     * command from server. Prints state to
     * console and closes the client.
     */
    private void handleQuit() {
        System.out.println("Server shutdown the connection!");
        this.close();
    }

    /**
     * Function for handling getting the error
     * command from server. Prints error to console.
     */
    private void handleError(String[] arguments) {
        System.out.println("ERROR: " + arguments[1]);
        this.letUserDecide();
    }

    /**
     * Distributive function for handling the
     * message from the server. Splits the msg
     * according to the protocol by the tilda
     * and refers to specific functions based on
     * the command given of the message.
     *
     * @param msg raw message from the server received
     */
    private void handleMessage(String msg) {
        String[] split = msg.split("~");
        switch (split[0]) {
            case Command.HELLO:
                handleHello();
                break;
            case Command.LOGIN:
                handleLogin();
                break;
            case Command.NEWGAME:
                handleNewgame(split);
                break;
            case Command.MOVE:
                handleMove(split);
                break;
            case Command.GAMEOVER:
                handleGameOver(split);
                break;
            case Command.PONG:
                handlePong();
                break;
            case Command.LIST:
                handleList(split);
                break;
            case Command.QUIT:
                handleQuit();
                break;
            case Command.ALREADYLOGGEDIN:
                handleAlreadyLoggedIn();
                break;
            case Command.ERROR:
                handleError(split);
                break;
            default:
                System.out.println("ERROR: Client does not know command from server.");
                break;
        }
    }

    /**
     * Lets user decide on his next action
     * while being connected to the server.
     * Lists the commands available and listens
     * to the input of user and sends commands
     * to server accordingly.
     */
    private void letUserDecide() {
        this.printHelp();
        Scanner sc = new Scanner(System.in);

        String commandAnswer = sc.nextLine().toUpperCase();
        boolean isCommandValid = false;
        while (!isCommandValid) {
            switch (commandAnswer) {
                case Command.QUEUE:
                    sendCommand(Command.QUEUE);
                    setState(ClientState.NEWGAME_AWAITING);
                    isCommandValid = true;
                    break;
                case Command.LIST:
                    sendCommand(Command.LIST);
                    isCommandValid = true;
                    break;
                case Command.PING:
                    sendCommand(Command.PING);
                    isCommandValid = true;
                    break;
                case Command.HELP:
                    this.printHelp();
                    isCommandValid = true;
                    break;
                case Command.QUIT:
                    sendCommand(Command.QUIT);
                    isCommandValid = true;
                    break;
                default:
                    System.out.println("ERROR: command " + commandAnswer
                            + " is not a valid choice.");
                    this.printHelp();
                    commandAnswer = sc.nextLine().toUpperCase();
                    break;
            }
        }

    }

    /**
     * Prints the help menu for different commands.
     */
    private void printHelp() {
        final String helpMenu =
                String.format("%nCommands:%n") +
                String.format("- %s................... queue for a new game%n", Command.QUEUE) +
                String.format("- %s ............ list all the active clients%n", Command.LIST) +
                String.format("- %s .. send a ping message and get pong back%n", Command.PING) +
                String.format("- %s ..... prints the help menu with commands %n", Command.HELP) +
                String.format("- %s .... end connection with the other party %n", Command.QUIT);
        System.out.println(helpMenu);
    }

    /**
     * Getter.
     *
     * @return client state object
     */
    public ClientState getState() {
        return state;
    }

    /**
     * Setter.
     *
     * @param state client state object
     */
    public void setState(ClientState state) {
        this.state = state;
    }
}
