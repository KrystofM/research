package pentago.networking.server;

import pentago.game.PentagoBoard;
import pentago.networking.models.ClientGame;
import pentago.game.models.Move;
import pentago.networking.models.GameOver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Server implements Runnable {
    /**
     * Server socket connection.
     */
    private ServerSocket ss;


    /**
     * Thread reference of server socket.
     */
    private Thread ssThread;

    /**
     * Port the server will run on.
     */
    private final int port;


    /**
     * List of all ClientHandlers that
     * are taking care of each connected
     * client to the server.
     */
    private final List<ClientHandler> handlers;


    /**
     * Basically Queue for being in the next game
     * since out game is 2 based player there only
     * has to be one player waiting each time.
     */
    private ClientHandler waitingHandler;


    /**
     * The repository of all games currently in the
     * progress being played and unfinished. Stored
     * in a HashMap with a UUID of each game as a
     * key identifier.
     */
    private final HashMap<String, ClientGame> games;


    /**
     * Description name of the server.
     */
    public static final String DESCRIPTION = "Mother of Servers";

    public static void main(String[] args) {
        Server chatServer = new Server(5555);
        chatServer.start();
    }

    /**
     * Constructor.
     *
     * @param port port for the server to run on
     */
    public Server(int port) {
        this.port = port;
        this.handlers = new ArrayList<>();
        this.games = new HashMap<>();
    }

    /**
     * Start method of the server. Use this to
     * start up the server.
     */
    public void start() {
        try {
            ss = new ServerSocket(port);
            System.out.println("Started server at port " + ss.getLocalPort());

            ssThread = new Thread(this);
            ssThread.start();
        } catch (IOException e) {
            System.out.println("Could not start server at port " + port);
        }
    }

    /**
     * Override from runnable. Called once the thread of
     * the server has started. For each new connection a
     * new ClientHandler instance is created and added to
     * the list of all ClientHandlers.
     */
    @Override
    public void run() {
        boolean run = true;
        int threadCount = 0;
        while (run) {
            try {
                System.out.println("Waiting for new accept at " + ss.getInetAddress());
                Socket sock = ss.accept();

                ClientHandler newClientHandler = new ClientHandler(sock, this);
                System.out.println("New thread " + threadCount + " has been created.");
                threadCount += 1;
                this.addClientHandler(newClientHandler);

                new Thread(newClientHandler).start();
            } catch (IOException e) {
                System.out.println("Oops something went wrong here.");
                run = false;
            }
        }
    }

    /**
     * Called by the ClientHandler to request a
     * game in the Server. If there is nobody in the
     * queue the CH goes into queue otherwise new
     * game is started and marked in games.
     *
     * @param requestingClient client handler that request a game
     */
    public synchronized void requestGame(ClientHandler requestingClient) {
        if (waitingHandler != null) {
            ClientGame game = new ClientGame(waitingHandler, requestingClient);
            games.put(game.getUuid(), game);
            waitingHandler.startGame(game, true);
            requestingClient.startGame(game, false);

            waitingHandler = null;
        } else {
            waitingHandler = requestingClient;
        }
    }

    /**
     * Records the move when ClientHandlers gets a move.
     * Gets the game by the gameId and playes the move
     * on the board of that game. Also checks for the
     * game over condition by checking a winner or if
     * there is a draw board. Sends the made move back
     * to all the client handlers from the game.
     *
     * @param move model of the move
     * @param gameId uuid of the game
     */
    public synchronized void recordMove(Move move, String gameId) {
        ClientGame game = this.games.get(gameId);
        PentagoBoard board = game.getBoard();
        ClientHandler firstClient = game.getFirstClientHandler();
        ClientHandler secondClient = game.getSecondClientHandler();
        boolean isFieldAnswerValid =
                board.isField(move.getField()) && board.isEmptyField(move.getField());
        // Somehow gets invalid move from ClientHandler
        if (!isFieldAnswerValid) {
            firstClient.invalidMove();
            secondClient.invalidMove();
            return;
        }
        board.makeAutomatedMove(move);

        game.getFirstClientHandler().makeMove(move);
        game.getSecondClientHandler().makeMove(move);

        // Check for winner
        if (board.hasOneWinner()) {
            String winnerName = board.isWinner(firstClient.getMark()) ?
                    firstClient.getName() : secondClient.getName();
            firstClient.endGame(GameOver.VICTORY, winnerName);
            secondClient.endGame(GameOver.VICTORY, winnerName);
            this.games.remove(gameId);
            return;
        }
        // Check for draw
        if (board.hasTwoWinners() || board.isFull()) {
            firstClient.endGame(GameOver.DRAW, null);
            secondClient.endGame(GameOver.DRAW, null);
            this.games.remove(gameId);
        }
    }

    /**
     * Getter.
     *
     * @return port of the server
     */
    public int getPort() {
        return ss.getLocalPort();
    }

    /**
     * Stops the server from running.
     */
    public void stop() {
        try {
            ss.close();
            ssThread.join();
        } catch (IOException | InterruptedException e) {
            System.out.println("Oops something went wrong here.");
        }
    }

    public synchronized void addClientHandler(ClientHandler handler) {
        handlers.add(handler);
    }

    public synchronized void removeClientHandler(ClientHandler handler) {
        handlers.remove(handler);
    }

    /**
     * Called by ClientHandler while disconnecting.
     * When ClientHandler suddenly stops it informs
     * other player in the game about the disconnect.
     *
     * @param handler disconnection
     * @param gameId game id to be disconnected from
     */
    public synchronized void disconnectClientHandlerFromGame(ClientHandler handler, String gameId) {
        ClientGame game = this.games.get(gameId);
        ClientHandler newWinner;
        // When both end at same time.
        if (handler.getName() == null) {
            newWinner = game.getFirstClientHandler();
        } else {
            newWinner = handler.getName().equals(game.getFirstClientHandler().getName())
                    ? game.getSecondClientHandler() : game.getFirstClientHandler();
        }
        newWinner.endGame(GameOver.DISCONNECT, newWinner.getName());
        this.games.remove(gameId);
    }

    /**
     * Called by ClientHandler to get the list of
     * client handler usernames.
     *
     * @return list of connected client handler usernames
     */
    public synchronized ArrayList<String> getClientHandlerUsernames() {
        ArrayList<String> result = new ArrayList<>();

        for (ClientHandler handler : this.handlers) {
            if (handler.isLoggedIn()) {
                result.add(handler.getName());
            }
        }

        return result;
    }

    /**
     * Called by ClientHandler to get check if
     * the username is taken.
     *
     * @param requestedName requested name of client
     * @return is client handler username taken
     */
    public synchronized boolean isClientHandlerUsernameTaken(String requestedName) {
        ArrayList<String> names = this.getClientHandlerUsernames();

        for (String name : names) {
            if (requestedName.equals(name)) {
                return true;
            }
        }

        return false;
    }
}
