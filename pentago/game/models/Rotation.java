package pentago.game.models;

/**
 * Rotation in Pentago combines the quadrant
 * and the direction in which a move is played.
 * This model models that.
 */
public class Rotation {
    private Quadrant quadrant;
    private Direction direction;

    public Rotation(Quadrant quadrant, Direction direction) {
        this.quadrant = quadrant;
        this.direction = direction;
    }

    public Quadrant getQuadrant() {
        return quadrant;
    }

    public Direction getDirection() {
        return direction;
    }
}
