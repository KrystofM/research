package pentago.networking.server;

import pentago.networking.dto.MoveDto;
import pentago.networking.dto.MoveDtoException;
import pentago.networking.mappers.MoveMapper;
import pentago.game.models.*;
import pentago.networking.models.ClientGame;
import pentago.networking.models.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created for each connection to Server.
 * Takes care for specific player each time
 * therefore it has some Skeleton properties
 * of a player also ( it is not able to do
 * moves on its own and there not having Body ).
 * Closely intertwined with Server and
 * of course Runnable.
 */
public class ClientHandler extends PlayerSkeleton implements Runnable {
    /**
     * Stores the given socket connection.
     */
    private final Socket socket;

    /**
     * Stores the given server reference.
     */
    private final Server server;

    /**
     * Inputs from the socket.
     */
    private final BufferedReader in;


    /**
     * Outputs from the socket
     */
    private final PrintWriter out;

    /**
     * Variable keeping track of running state.
     */
    private boolean run;

    /**
     * Assigned game uuid currently being in.
     */
    private String gameId;

    /**
     * State of client handler.
     */
    private ClientHandlerState state;


    private static final String ERROR_HELLO_FIRST = "You have to say HELLO first.";
    private static final String ERROR_ARGUMENTS = "Wrong amount of arguments.";
    private static final String ERROR_UNKNOWN_COMMAND = "Cannot recognize command.";
    private static final String ERROR_WRONG_ARGUMENT
            = "Arguments that you have provided are wrong.";
    private static final String ERROR_NOT_PLAYING
            = "Unfortunately it is not your turn in game right now.";
    private static final String ERROR_NOT_STARTED = "Unfortunately you are not in a game yet.";
    private static final String ERROR_WRONG_MOVE = "Your move is invalid! Try again.";

    /**
     * Creates a new ClientHandler object and properly
     * sets the input reader and output printer.
     *
     * @param socket socket connection
     * @param server server reference
     * @throws IOException when creating reader/writer fails
     */
    public ClientHandler(Socket socket, Server server) throws IOException {
        super(null, null);
        this.socket = socket;
        this.server = server;
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.out = new PrintWriter(this.socket.getOutputStream(), true);
        this.run = true;
        this.state = ClientHandlerState.INITIAL;
    }

    /**
     * Run method override from Runnable.
     * Being called when Thread.start() is run.
     * Runs until run state changes and reads
     * from the input continuously.
     */
    @Override
    public void run() {
        while (this.run) {
            try {
                String msg = in.readLine();
                if (msg == null) {
                    System.out.println("Oops, message was null");
                    this.close();
                    continue;
                }
                handleMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Called from Server when enough players
     * are in queue in order to play a game.
     * Game state playing is set according to
     * either being first or second in game.
     *
     * @param clientGame client game object with game details
     * @param firstPlayer are we the first player to move in game
     */
    public synchronized void startGame(ClientGame clientGame, boolean firstPlayer) {
        setState(firstPlayer ? ClientHandlerState.IN_GAME_PLAYING
                : ClientHandlerState.IN_GAME_WAITING);
        setMark(firstPlayer ? Mark.BLACK : Mark.WHITE);

        this.gameId = clientGame.getUuid();

        sendCommand(
                Command.NEWGAME,
                clientGame.getFirstClientHandler().getName(),
                clientGame.getSecondClientHandler().getName()
        );
    }

    /**
     * Called from Server to send a move to
     * client that could have been played be either
     * us or the opponent in the game.
     *
     * @param move move object
     */
    public synchronized void makeMove(Move move) {
        setState(state.equals(ClientHandlerState.IN_GAME_WAITING) ?
                ClientHandlerState.IN_GAME_PLAYING : ClientHandlerState.IN_GAME_WAITING);

        MoveDto moveDto;
        try {
            moveDto = MoveMapper.toMoveDto(move);
        } catch (MoveDtoException e) {
            sendCommand(Command.ERROR, ERROR_WRONG_MOVE);
            return;
        }

        sendCommand(
                Command.MOVE,
                Integer.toString(moveDto.getA()),
                Integer.toString(moveDto.getB())
        );
    }

    /**
     * Can be called from Server in case the client
     * has sent an invalid move.
     */
    public synchronized void invalidMove() {
        if (state.equals(ClientHandlerState.IN_GAME_PLAYING)) {
            sendCommand(Command.ERROR, ERROR_WRONG_MOVE);
        }
    }

    /**
     * Called by Server once the game over condition
     * has been reached. The state of client handler
     * is set back to logged in.
     *
     * @param gameOver game over condition
     * @param winnerName name of winner if applicable
     */
    public synchronized void endGame(String gameOver, String winnerName) {
        setState(ClientHandlerState.LOGGED_IN);
        this.gameId = null;
        if (winnerName != null) {
            sendCommand(Command.GAMEOVER, gameOver, winnerName);
        } else {
            sendCommand(Command.GAMEOVER, gameOver);
        }
    }

    /**
     * Simple state generalization.
     *
     * @return is client logged in
     */
    public synchronized boolean isLoggedIn() {
        return !state.equals(ClientHandlerState.INITIAL) &&
                !state.equals(ClientHandlerState.WAITING_LOGIN);
    }

    /**
     * Simple state generalization.
     *
     * @return is client playing a game
     */
    private synchronized boolean isPlayingGame() {
        return state.equals(ClientHandlerState.IN_GAME_PLAYING) ||
                state.equals(ClientHandlerState.IN_GAME_WAITING);
    }

    /**
     * Function that handles the receiving of
     * the message hello from the client.
     */
    private synchronized void handleHello() {
        if (state.equals(ClientHandlerState.INITIAL)) {
            sendCommand(Command.HELLO, Server.DESCRIPTION);
            setState(ClientHandlerState.WAITING_LOGIN);
        }
    }

    /**
     * Function that handles the receiving of
     * the login request from the client. Checks
     * for different error and side scenarios.
     *
     * @param arguments split arguments from client
     */
    private synchronized void handleLogin(String[] arguments) {
        if (arguments.length != 2) {
            sendCommand(Command.ERROR, ERROR_ARGUMENTS);
            return;
        }
        String name = arguments[1];
        if (state.equals(ClientHandlerState.INITIAL)) {
            sendCommand(Command.ERROR, ERROR_HELLO_FIRST);
            return;
        }
        if (server.isClientHandlerUsernameTaken(name)) {
            sendCommand(Command.ALREADYLOGGEDIN);
            return;
        }
        if (state.equals(ClientHandlerState.WAITING_LOGIN)) {
            setName(name);
            sendCommand(Command.LOGIN);
            setState(ClientHandlerState.LOGGED_IN);
        }
    }

    /**
     * Function that handles the receiving of
     * the queue request from the client. Has
     * to be in logged in to request a queue.
     */
    private synchronized void handleQueue() {
        if (isLoggedIn()) {
            server.requestGame(this);
        }
    }

    /**
     * Function that handles the receiving of
     * move request from the client. Checks for
     * all different scenarios. It has to be players
     * turn to play and move has to be correct.
     *
     * @param arguments split arguments from client
     */
    private synchronized void handleMove(String[] arguments) {
        if (arguments.length != 3) {
            sendCommand(Command.ERROR, ERROR_ARGUMENTS);
            return;
        }
        if (state.equals(ClientHandlerState.IN_GAME_WAITING)) {
            sendCommand(Command.ERROR, ERROR_NOT_PLAYING);
            return;
        }

        if (state.equals(ClientHandlerState.IN_GAME_PLAYING)) {
            MoveDto moveDto;
            try {
                moveDto = new MoveDto(arguments[1], arguments[2]);
            } catch (MoveDtoException e) {
                sendCommand(Command.ERROR, ERROR_WRONG_ARGUMENT);
                return;
            }
            Move move = MoveMapper.toMove(moveDto);
            server.recordMove(move, this.gameId);
        } else {
            sendCommand(Command.ERROR, ERROR_NOT_STARTED);
        }
    }

    /**
     * Function that handles the receiving of
     * ping request from the client. Returns pong.
     */
    private synchronized void handlePing() {
        sendCommand(Command.PONG);
    }

    /**
     * Function that handles the receiving of
     * list request from the client. Asks the server
     * for that information. Has to be logged in.
     */
    private synchronized void handleList() {
        if (isLoggedIn()) {
            ArrayList<String> names = server.getClientHandlerUsernames();
            sendCommand(Command.LIST, names.toArray(new String[0]));
        }
    }

    /**
     * Function that handles the receiving of
     * quit request from the client. Closes connection.
     */
    private synchronized void handleQuit() {
        this.close();
        sendCommand(Command.QUIT);
    }

    /**
     * Distributive function for handling the
     * message from the client. Splits the msg
     * according to the protocol by the tilda
     * and refers to specific functions based on
     * the command given of the message.
     *
     * @param msg raw message from the client received
     */
    private synchronized void handleMessage(String msg) {
        String[] split = msg.split("~");
        System.out.println("Getting command: " + msg);
        switch (split[0]) {
            case Command.HELLO:
                handleHello();
                break;
            case Command.LOGIN:
                handleLogin(split);
                break;
            case Command.QUEUE:
                handleQueue();
                break;
            case Command.MOVE:
                handleMove(split);
                break;
            case Command.PING:
                handlePing();
                break;
            case Command.LIST:
                handleList();
                break;
            case Command.QUIT:
                handleQuit();
                break;
            default:
                sendCommand(Command.ERROR, ERROR_UNKNOWN_COMMAND);
                break;
        }
    }

    /**
     * Setter.
     *
     * @param state server state object
     */
    private synchronized void setState(ClientHandlerState state) {
        this.state = state;
    }

    /**
     * Sends a command to the connected client
     * and can be used with multiple arguments
     * that are handled by the standard defined
     * in the protocol for sending messages.
     *
     * @param command string version of Command
     * @param arguments whatever string name/move etc.
     */
    private synchronized void sendCommand(String command, String... arguments) {
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
    private synchronized void sendMessage(String msg) {
        out.println(msg);
    }

    /**
     * Closes the connection with the server
     * and handles the disconnect from a game
     * if there is a game being played.
     */
    private synchronized void close() {
        try {
            sendCommand(Command.QUIT);
            this.socket.close();
            if (isPlayingGame()) {
                this.server.disconnectClientHandlerFromGame(this, gameId);
            }
            this.server.removeClientHandler(this);
            this.run = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
