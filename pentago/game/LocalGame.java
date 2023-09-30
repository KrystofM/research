package pentago.game;

import pentago.game.models.Mark;
import pentago.game.models.Move;
import pentago.game.models.PlayerSkeleton;
import pentago.utils.TextIO;

/**
 * Easy implementation of the Pentago game
 * where we use one control for both players.
 * Made this to test out game logic without
 * having networking done.
 */
public class LocalGame {
    public static final int NUMBER_PLAYERS = 2;

    /**
     * The board.
     */
    private PentagoBoard board;

    /**
     * The 2 players of the game.
     */
    private HumanPlayer[] players;

    public LocalGame(HumanPlayer pl1, HumanPlayer pl2) {
        this.board = new PentagoBoard();
        players = new HumanPlayer[NUMBER_PLAYERS];
        players[0] = pl1;
        players[1] = pl2;
    }

    public static void main(String[] args) {
        System.out.println("\n> Player number 1, what is your name?");
        String firstPlayerName = TextIO.getlnString();
        System.out.println("\n> Player number 2, what is your name?");
        String secondPlayerName = TextIO.getlnString();
        HumanPlayer firstHumanPlayer = new HumanPlayer(firstPlayerName, Mark.BLACK);
        HumanPlayer secondHumanPlayer = new HumanPlayer(secondPlayerName, Mark.WHITE);
        (new LocalGame(firstHumanPlayer, secondHumanPlayer)).start();
    }

    /**
     * Starts the Pentago game. <br>
     * Asks after each ended game if the user want to continue. Continues until
     * the user does not want to play anymore.
     */
    public void start() {
        boolean continueGame = true;
        while (continueGame) {
            reset();
            play();
            System.out.println("\n> Play another time? (y/n)?");
            continueGame = TextIO.getBoolean();
        }
    }

    /**
     * Resets the game. <br>
     * The board is emptied and player[0] becomes the current player.
     */
    private void reset() {
        board.reset();
    }

    /**
     * Plays the Pentago game. <br>
     * First the (still empty) board is shown. Then the game is played
     * until it is over. Players can make a move one after the other.
     * After each move, the changed game situation is printed.
     */
    private void play() {
        System.out.println(board.printBoard());
        int count = 1;
        while (!board.isGameOver()) {
            Move move;
            if (count % 2 == 0) {
                move = players[0].determineMove(board);
            } else {
                move = players[1].determineMove(board);
            }
            board.makeAutomatedMove(move);
            update();
            count++;
        }
        printResult();
    }

    /**
     * Prints the game situation.
     */
    private void update() {
        System.out.println("\ncurrent game situation: \n\n" + board.printBoard()
                + "\n");
    }

    /**
     * Prints the result of the last game.
     */
    private void printResult() {
        if (board.hasOneWinner()) {
            PlayerSkeleton winner = board.isWinner(players[0].getMark()) ? players[0]
                    : players[1];
            System.out.println("Player " + winner.getName() + " ("
                    + winner.getMark().toString() + ") has won!");
        } else {
            System.out.println("Draw. There is no winner!");
        }
    }

    public PentagoBoard getBoard() {
        return board;
    }

    public void setBoard(PentagoBoard board) {
        this.board = board;
    }

    public HumanPlayer[] getPlayers() {
        return players;
    }

    public void setPlayers(HumanPlayer[] players) {
        this.players = players;
    }

}
