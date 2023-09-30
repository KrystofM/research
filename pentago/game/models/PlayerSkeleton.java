package pentago.game.models;

/**
 * PlayerSkeleton is the root of what
 * makes a player. Skeleton cannot
 * make any decision on its own.
 */
public abstract class PlayerSkeleton {
    protected String name;
    protected Mark mark;

    /**
     * Creates a new PlayerSkeleton object.
     */
    public PlayerSkeleton(String name, Mark mark) {
        this.name = name;
        this.mark = mark;
    }

    /**
     * Returns the name of the player.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the player.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the mark of the player.
     */
    public Mark getMark() {
        return mark;
    }

    /**
     * Sets the mark of the player.
     */
    public void setMark(Mark mark) {
        this.mark = mark;
    }
}
