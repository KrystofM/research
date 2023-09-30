package pentago.game.models;

/**
 * Model the whole Move in one object.
 * Move in Pentago consists of choosing
 * a field and the making a rotation.
 */
public class Move {
    private Rotation rotation;
    private int field;

    public Move(int field, Rotation rotation) {
        this.rotation = rotation;
        this.field = field;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public int getField() {
        return field;
    }
    
}
