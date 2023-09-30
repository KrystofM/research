package pentago.game.models;

import pentago.game.PentagoBoard;

/**
 * PlayerBody is an extension of PlayerSkeleton
 * because it enables the Player to actually make
 * decisions and determine next moves.
 */
public abstract class PlayerBody extends PlayerSkeleton {
    /**
     * Creates a new PlayerSkeleton object.
     *
     * @param name name of the player
     * @param mark indication mark of the player
     */
    public PlayerBody(String name, Mark mark) {
        super(name, mark);
    }

    /**
     * Determines the field for the next move.
     * @param board the current game board
     * @return the player's choice
     */
    public Move determineMove(PentagoBoard board) {
        int field = determineMoveField(board);
        Rotation rotation = determineMoveRotation(board);

        return new Move(field, rotation);
    }

    /**
     * Helper function gets field for the overall determine move.
     *
     * @param board the game board
     * @return the player's chosen field
     */
    protected abstract Rotation determineMoveRotation(PentagoBoard board);

    /**
     * Helper function gets field for the overall determine move.
     *
     * @param board the game board
     * @return the player's chosen field
     */
    protected abstract int determineMoveField(PentagoBoard board);
}
