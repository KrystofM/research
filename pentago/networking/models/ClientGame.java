package pentago.networking.models;

import pentago.game.PentagoBoard;
import pentago.networking.server.ClientHandler;

import java.util.UUID;

/**
 * Simple model structure to store players,
 * board state and uuid. These together create
 * a game.
 */
public class ClientGame {
    /**
     * Unique identifier of the game.
     */
    private final String uuid;

    /**
     * First player alias ClientHandler of game.
     */
    private final ClientHandler clientHandler00;

    /**
     * Second player alias ClientHandler of game.
     */
    private final ClientHandler clientHandler01;


    /**
     * Pentago board stored.
     */
    private PentagoBoard board;

    public ClientGame(ClientHandler clientHandler00, ClientHandler clientHandler01) {
        this.clientHandler00 = clientHandler00;
        this.clientHandler01 = clientHandler01;
        this.board = new PentagoBoard();
        this.uuid = UUID.randomUUID().toString();
    }

    public String getUuid() {
        return uuid;
    }

    public ClientHandler getFirstClientHandler() {
        return this.clientHandler00;
    }

    public ClientHandler getSecondClientHandler() {
        return this.clientHandler01;
    }

    public PentagoBoard getBoard() {
        return board;
    }

    public void setBoard(PentagoBoard board) {
        this.board = board;
    }

}
