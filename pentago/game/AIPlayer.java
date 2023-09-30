package pentago.game;

import pentago.game.models.*;

import java.util.ArrayList;
import java.util.Random;

/**
 * DUMB AI Player makes random moves.
 */
public class AIPlayer extends PlayerBody {
    private final Random random;
    /**
     * Creates a new PlayerSkeleton object.
     */
    public AIPlayer(String name, Mark mark) {
        super(name, mark);
        this.random = new Random();
    }


    /**
     * Helper function gets field for the overall determine move.
     *
     * @return the player's chosen field
     */
    @Override
    protected Rotation determineMoveRotation(PentagoBoard board) {
        Quadrant quadrant = Quadrant.values()[random.nextInt(Quadrant.values().length)];
        Direction direction = Direction.values()[random.nextInt(Direction.values().length)];

        return new Rotation(quadrant, direction);
    }

    /**
     * Helper function gets field for the overall determine move.
     *
     * @param board the game board
     * @return the player's chosen field
     */
    @Override
    protected int determineMoveField(PentagoBoard board) {
        ArrayList<Integer> playableFieldIndexes = new ArrayList<>();
        for (int i = 0; i < 36; i++) {
            if (board.isEmptyField(i)) {
                playableFieldIndexes.add(i);
            }
        }

        int index = random.nextInt(playableFieldIndexes.size());
        return playableFieldIndexes.get(index);
    }
}
