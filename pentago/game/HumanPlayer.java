package pentago.game;

import pentago.game.models.*;
import pentago.utils.TextIO;

import java.util.Scanner;

/**
 * Creates moves based on the System input.
 * Asks the user for inputs for each case.
 */
public class HumanPlayer extends PlayerBody {
    /**
     * Shadow player that can be asked to hint
     * the Human player in how to determine a move.
     */
    private final PlayerBody shadowPlayer;

    /**
     * Activation code that enables the shadow player.
     */
    private static final int HINT_ACTIVATION = 6969;
    private static final String HINT_TEXT = "Try out ";


    /**
     * Creates a new human player object.
     */
    public HumanPlayer(String name, Mark mark, PlayerBody shadowPlayer) {
        super(name, mark);
        this.shadowPlayer = shadowPlayer;
    }

    public HumanPlayer(String name, Mark mark) {
        super(name, mark);
        this.shadowPlayer = new AIPlayer(name, mark);
    }

    /**
     * Sets mark for shadow player too.
     *
     * @param mark indication of player
     */
    @Override
    public void setMark(Mark mark) {
        this.mark = mark;
        this.shadowPlayer.setMark(mark);
    }

    /**
     * Helper function gets field for the overall determine move.
     *
     * @param board the game board
     * @return the player's chosen field
     */
    @Override
    public int determineMoveField(PentagoBoard board) {
        String askForField = "> " + getName() + " (" + getMark().toString() + ")"
                + ", enter a valid index to play (for hint type cheat code " + HINT_ACTIVATION + "): ";
        System.out.println(askForField);
        int fieldAnswer = TextIO.getInt();
        if (fieldAnswer == HINT_ACTIVATION) {
            Move move = this.shadowPlayer.determineMove(board);
            System.out.println(HINT_TEXT + move.getField() + " field.");
        }
        boolean isFieldAnswerValid = board.isField(fieldAnswer) && board.isEmptyField(fieldAnswer);
        while (!isFieldAnswerValid) {
            System.out.println("ERROR: field " + fieldAnswer
                    + " is no valid fieldChoice.");
            System.out.println(askForField);
            fieldAnswer = TextIO.getInt();
            isFieldAnswerValid = board.isField(fieldAnswer) && board.isEmptyField(fieldAnswer);
        }

        return fieldAnswer;
    }

    /**
     * Helper function gets rotation for the overall determine move.
     *
     * @return the player's chosen rotation (quadrant, direction)
     */
    @Override
    public Rotation determineMoveRotation(PentagoBoard board) {
        Scanner sc = new Scanner(System.in);

        String askForQuadrant = "> " + getName() + " (" + getMark().toString() + ")"
                + ", choose the quadrant to rotate (TL, TR, BL, BR): ";
        System.out.println(askForQuadrant);
        String quadrantAnswer = sc.nextLine().toUpperCase();
        boolean isQuadrantValid = false;
        Quadrant quadrant = null;
        while (!isQuadrantValid) {
            switch (quadrantAnswer) {
                case "TOP_LEFT":
                case "TL":
                    quadrant = Quadrant.TOP_LEFT;
                    isQuadrantValid = true;
                    break;
                case "TOP_RIGHT":
                case "TR":
                    quadrant = Quadrant.TOP_RIGHT;
                    isQuadrantValid = true;
                    break;
                case "BOTTOM_LEFT":
                case "BL":
                    quadrant = Quadrant.BOTTOM_LEFT;
                    isQuadrantValid = true;
                    break;
                case "BOTTOM_RIGHT":
                case "BR":
                    quadrant = Quadrant.BOTTOM_RIGHT;
                    isQuadrantValid = true;
                    break;
                case "" + HINT_ACTIVATION:
                    Move move = this.shadowPlayer.determineMove(board);
                    System.out.println(HINT_TEXT + move.getRotation().getQuadrant().name() + " quadrant.");
                    System.out.println(askForQuadrant);
                    quadrantAnswer = sc.nextLine().toUpperCase();
                    break;
                default:
                    System.out.println("ERROR: quadrant " + quadrantAnswer
                            + " is not a valid choice.");
                    System.out.println(askForQuadrant);
                    quadrantAnswer = sc.nextLine().toUpperCase();
                    break;
            }
        }

        String askForDirection = "> " + getName() + " (" + getMark().toString() + ")"
                + ", choose the direction you want to rotate (CLOCKWISE, COUNTERCLOCKWISE): ";
        System.out.println(askForDirection);
        String directionAnswer = sc.nextLine().toUpperCase();
        boolean isDirectionValid = false;
        Direction direction = null;
        while (!isDirectionValid) {
            switch (directionAnswer) {
                case "CLOCKWISE":
                    direction = Direction.CLOCKWISE;
                    isDirectionValid = true;
                    break;
                case "COUNTER_CLOCKWISE":
                case "COUNTERCLOCKWISE":
                    direction = Direction.COUNTER_CLOCKWISE;
                    isDirectionValid = true;
                    break;
                case "" + HINT_ACTIVATION:
                    Move move = this.shadowPlayer.determineMove(board);
                    System.out.println(HINT_TEXT + move.getRotation().getDirection().name() + " direction.");
                    System.out.println(askForDirection);
                    directionAnswer = sc.nextLine().toUpperCase();
                    break;
                default:
                    System.out.println("ERROR: direction " + directionAnswer
                            + " is not a valid choice.");
                    System.out.println(askForDirection);
                    directionAnswer = sc.nextLine().toUpperCase();
                    break;
            }
        }

        return new Rotation(quadrant, direction);
    }

}
