package pentago.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import pentago.game.models.Direction;
import pentago.game.PentagoBoard;
import pentago.game.models.Mark;
import pentago.game.models.Quadrant;
import pentago.game.models.Rotation;
import pentago.game.models.Move;

import java.util.ArrayList;
import java.util.Random;


public class PentagoBoardTest {
    private PentagoBoard board;

    @BeforeEach
    public void setUp() {
        board = new PentagoBoard();
    }

    @Test
    public void testIndex() {
        int index = 0;
        for (int i = 0; i < PentagoBoard.DIM; i++) {
            for (int j = 0; j < PentagoBoard.DIM; j++) {
                assertEquals(index, board.index(i, j));
                index += 1;
            }
        }
        assertEquals(0, board.index(0, 0));
        assertEquals(1, board.index(0, 1));
        assertEquals(25, board.index(4, 1));
        assertEquals(35, board.index(5, 5));
    }

    @Test
    public void testIsFieldIndex() {
        assertFalse(board.isField(-1));
        assertTrue(board.isField(0));
        assertTrue(board.isField(PentagoBoard.DIM * PentagoBoard.DIM - 1));
        assertFalse(board.isField(PentagoBoard.DIM * PentagoBoard.DIM));
    }

    @Test
    public void testRotationTopLeft() {
        board.setField(0, Mark.BLACK);
        board.setField(1, Mark.WHITE);
        board.setField(7, Mark.WHITE);

        board.rotate(new Rotation(Quadrant.TOP_LEFT, Direction.CLOCKWISE));

        assertNotEquals(Mark.BLACK, board.getField(0));
        assertEquals(Mark.BLACK, board.getField(2));

        assertNotEquals(Mark.WHITE, board.getField(1));
        assertEquals(Mark.WHITE, board.getField(8));

        assertEquals(Mark.WHITE, board.getField(7));

        // Rotating counterclockwise
        board.rotate(new Rotation(Quadrant.TOP_LEFT, Direction.COUNTER_CLOCKWISE));

        assertNotEquals(Mark.BLACK, board.getField(2));
        assertEquals(Mark.BLACK, board.getField(0));

        assertNotEquals(Mark.WHITE, board.getField(8));
        assertEquals(Mark.WHITE, board.getField(1));

        assertEquals(Mark.WHITE, board.getField(7));
    }

    @Test
    public void testRotationTopRight() {
        board.setField(3, Mark.BLACK);
        board.setField(4, Mark.WHITE);
        board.setField(10, Mark.WHITE);

        board.rotate(new Rotation(Quadrant.TOP_RIGHT, Direction.CLOCKWISE));

        assertNotEquals(Mark.BLACK, board.getField(3));
        assertEquals(Mark.BLACK, board.getField(5));

        assertNotEquals(Mark.WHITE, board.getField(4));
        assertEquals(Mark.WHITE, board.getField(11));

        assertEquals(Mark.WHITE, board.getField(10));

        // Rotating counterclockwise
        board.rotate(new Rotation(Quadrant.TOP_RIGHT, Direction.COUNTER_CLOCKWISE));

        assertNotEquals(Mark.BLACK, board.getField(5));
        assertEquals(Mark.BLACK, board.getField(3));

        assertNotEquals(Mark.WHITE, board.getField(11));
        assertEquals(Mark.WHITE, board.getField(4));

        assertEquals(Mark.WHITE, board.getField(10));
    }

    @Test
    public void testRotationBottomLeft() {
        board.setField(18, Mark.BLACK);
        board.setField(19, Mark.WHITE);
        board.setField(25, Mark.WHITE);

        board.rotate(new Rotation(Quadrant.BOTTOM_LEFT, Direction.CLOCKWISE));

        assertNotEquals(Mark.BLACK, board.getField(18));
        assertEquals(Mark.BLACK, board.getField(20));

        assertNotEquals(Mark.WHITE, board.getField(19));
        assertEquals(Mark.WHITE, board.getField(26));

        assertEquals(Mark.WHITE, board.getField(25));

        // Rotating counterclockwise
        board.rotate(new Rotation(Quadrant.BOTTOM_LEFT, Direction.COUNTER_CLOCKWISE));

        assertNotEquals(Mark.BLACK, board.getField(20));
        assertEquals(Mark.BLACK, board.getField(18));

        assertNotEquals(Mark.WHITE, board.getField(26));
        assertEquals(Mark.WHITE, board.getField(19));

        assertEquals(Mark.WHITE, board.getField(25));
    }

    @Test
    public void testRotationBottomRight() {
        board.setField(21, Mark.BLACK);
        board.setField(22, Mark.WHITE);
        board.setField(28, Mark.WHITE);

        board.rotate(new Rotation(Quadrant.BOTTOM_RIGHT, Direction.CLOCKWISE));

        assertNotEquals(Mark.BLACK, board.getField(21));
        assertEquals(Mark.BLACK, board.getField(23));

        assertNotEquals(Mark.WHITE, board.getField(22));
        assertEquals(Mark.WHITE, board.getField(29));

        assertEquals(Mark.WHITE, board.getField(28));

        // Rotating counterclockwise
        board.rotate(new Rotation(Quadrant.BOTTOM_RIGHT, Direction.COUNTER_CLOCKWISE));

        assertNotEquals(Mark.BLACK, board.getField(23));
        assertEquals(Mark.BLACK, board.getField(21));

        assertNotEquals(Mark.WHITE, board.getField(29));
        assertEquals(Mark.WHITE, board.getField(22));

        assertEquals(Mark.WHITE, board.getField(28));
    }

    @Test
    public void testIsFieldRowCol() {
        assertFalse(board.isField(-1));
        assertFalse(board.isField(36));
        assertTrue(board.isField(0));
        assertTrue(board.isField(17));
        assertTrue(board.isField(23));
    }

    @Test
    public void testSetAndGetFieldIndex() {
        board.setField(0, Mark.BLACK);
        assertEquals(Mark.BLACK, board.getField(0));
        for (int i = 1; i < (PentagoBoard.DIM*PentagoBoard.DIM); i++) {
            assertEquals(Mark.EMPTY, board.getField(i));
        }
    }

    @Test
    public void testSetAndGetFieldRowCol() {
        board.setField(0, 0, Mark.BLACK);
        assertEquals(Mark.BLACK, board.getField(0, 0));
        assertEquals(Mark.EMPTY, board.getField(0, 1));
        assertEquals(Mark.EMPTY, board.getField(1, 0));
        assertEquals(Mark.EMPTY, board.getField(1, 1));
    }

    @Test
    public void testSetup() {
        for (int i = 0; i < PentagoBoard.DIM * PentagoBoard.DIM; i++) {
            assertEquals(Mark.EMPTY, board.getField(i));
        }
    }

    @Test
    public void testReset() {
        board.setField(0, Mark.BLACK);
        board.setField(PentagoBoard.DIM * PentagoBoard.DIM - 1, Mark.WHITE);
        board.reset();
        this.testSetup();
    }

    @Test
    public void testDeepCopy() {
        board.setField(0, Mark.BLACK);
        board.setField(PentagoBoard.DIM * PentagoBoard.DIM - 1, Mark.WHITE);
        PentagoBoard deepCopyPentagoBoard = board.deepCopy();

        // First test if all the fields are the same
        for (int i = 0; i < PentagoBoard.DIM * PentagoBoard.DIM; i++) {
            assertEquals(board.getField(i), deepCopyPentagoBoard.getField(i));
        }

        // Check if a field in the deepcopied board the original remains the same
        deepCopyPentagoBoard.setField(0, Mark.WHITE);

        assertEquals(Mark.BLACK, board.getField(0));
        assertEquals(Mark.WHITE, deepCopyPentagoBoard.getField(0));
    }

    @Test
    public void testIsEmptyFieldIndex() {
        board.setField(0, Mark.BLACK);
        assertFalse(board.isEmptyField(0));
        assertTrue(board.isEmptyField(1));
    }

    @Test
    public void testIsFull() {
        for (int i = 0; i < PentagoBoard.DIM * PentagoBoard.DIM - 1; i++) {
            board.setField(i, Mark.BLACK);
        }
        assertFalse(board.isFull());

        board.setField(PentagoBoard.DIM * PentagoBoard.DIM - 1, Mark.BLACK);
        assertTrue(board.isFull());
    }


    @Test
    public void testGameOverAndHasNoWinnerFullPentagoBoard() {
        /**
         *  BB | WW | BB || BB | WW | BB
         * ----+----+----||----+----+----
         *  WW | BB | WW || WW | BB | WW
         * ----+----+----||----+----+----
         *  WW | BB | WW || BB | WW | BB
         * ====+====+====||====+====+====
         *  BB | BB | WW || BB | WW | BB
         * ----+----+----||----+----+----
         *  WW | BB | BB || WW | BB | WW
         * ----+----+----||----+----+----
         *  WW | WW | BB || BB | WW | WW
         */
        board.setField(0, 0, Mark.BLACK);
        board.setField(0, 1, Mark.WHITE);
        board.setField(0, 2, Mark.BLACK);
        board.setField(0, 3, Mark.BLACK);
        board.setField(0, 4, Mark.WHITE);
        board.setField(0, 5, Mark.BLACK);
        board.setField(1, 0, Mark.WHITE);
        board.setField(1, 1, Mark.BLACK);
        board.setField(1, 2, Mark.WHITE);
        board.setField(1, 3, Mark.WHITE);
        board.setField(1, 4, Mark.BLACK);
        board.setField(1, 5, Mark.WHITE);
        board.setField(2, 0, Mark.WHITE);
        board.setField(2, 1, Mark.BLACK);
        board.setField(2, 2, Mark.WHITE);
        board.setField(2, 3, Mark.BLACK);
        board.setField(2, 4, Mark.WHITE);
        board.setField(2, 5, Mark.BLACK);
        board.setField(3, 0, Mark.BLACK);
        board.setField(3, 1, Mark.BLACK);
        board.setField(3, 2, Mark.WHITE);
        board.setField(3, 3, Mark.BLACK);
        board.setField(3, 4, Mark.WHITE);
        board.setField(3, 5, Mark.BLACK);
        board.setField(4, 0, Mark.WHITE);
        board.setField(4, 1, Mark.BLACK);
        board.setField(4, 2, Mark.BLACK);
        board.setField(4, 3, Mark.WHITE);
        board.setField(4, 4, Mark.BLACK);
        board.setField(4, 5, Mark.WHITE);
        board.setField(5, 0, Mark.WHITE);
        board.setField(5, 1, Mark.WHITE);
        board.setField(5, 2, Mark.BLACK);
        board.setField(5, 3, Mark.BLACK);
        board.setField(5, 4, Mark.WHITE);

        assertFalse(board.isGameOver());
        assertFalse(board.isFull());

        board.setField(5, 5, Mark.WHITE);
        assertTrue(board.isFull());
        assertTrue(board.isGameOver());
        assertFalse(board.hasOneWinner());
        assertFalse(board.hasTwoWinners());
    }

    // test if draw game, both winners won
    @Test
    public void testGameOverAndHasTwoWinners() {
        /**
         *     |    |    ||    | WW | BB
         * ----+----+----||----+----+----
         *     | WW | WW ||    | WW | BB
         * ----+----+----||----+----+----
         *     | BB | BB ||    | WW | BB
         * ====+====+====||====+====+====
         *     |    |    ||    |    |
         * ----+----+----||----+----+----
         *     |    |    ||    |    |
         * ----+----+----||----+----+----
         *     |    |    ||    |    |
         */

        board.setField(7, Mark.WHITE);
        board.setField(8, Mark.WHITE);
        board.setField(13, Mark.BLACK);
        board.setField(14, Mark.BLACK);

        board.setField(4, Mark.WHITE);
        board.setField(10, Mark.WHITE);
        board.setField(16, Mark.WHITE);
        board.setField(5, Mark.BLACK);
        board.setField(11, Mark.BLACK);
        board.setField(17, Mark.BLACK);

        board.rotate(new Rotation(Quadrant.TOP_RIGHT, Direction.CLOCKWISE));

        assertFalse(board.hasOneWinner());
        assertTrue(board.hasTwoWinners());
        assertTrue(board.isGameOver());
    }

    @Test
    public void testHasRow() {
        board.setField(0, Mark.BLACK);
        board.setField(1, Mark.BLACK);
        board.setField(2, Mark.BLACK);
        board.setField(3, Mark.BLACK);
        assertFalse(board.hasRow(Mark.BLACK));
        assertFalse(board.isWinner(Mark.BLACK));
        assertFalse(board.hasRow(Mark.WHITE));
        assertFalse(board.isWinner(Mark.WHITE));

        board.setField(4, Mark.BLACK);
        assertTrue(board.hasRow(Mark.BLACK));
        assertTrue(board.isWinner(Mark.BLACK));
        assertFalse(board.hasRow(Mark.WHITE));
        assertFalse(board.isWinner(Mark.WHITE));

        board.reset();

        board.setField(7, Mark.WHITE);
        board.setField(8, Mark.WHITE);
        board.setField(9, Mark.WHITE);
        board.setField(10, Mark.WHITE);
        assertFalse(board.hasRow(Mark.WHITE));
        assertFalse(board.isWinner(Mark.WHITE));
        assertFalse(board.hasRow(Mark.BLACK));
        assertFalse(board.isWinner(Mark.BLACK));

        board.setField(11, Mark.WHITE);
        assertTrue(board.hasRow(Mark.WHITE));
        assertTrue(board.isWinner(Mark.WHITE));
        assertFalse(board.hasRow(Mark.BLACK));
        assertFalse(board.isWinner(Mark.BLACK));
    }

    @Test
    public void testHasColumn() {
        board.setField(0, 0, Mark.BLACK);
        board.setField(1, 0, Mark.BLACK);
        board.setField(2, 0, Mark.BLACK);
        board.setField(3, 0, Mark.BLACK);
        assertFalse(board.hasColumn(Mark.BLACK));
        assertFalse(board.hasColumn(Mark.WHITE));

        board.setField(4, 0, Mark.BLACK);
        assertTrue(board.hasColumn(Mark.BLACK));
        assertFalse(board.hasColumn(Mark.WHITE));

        board.reset();

        board.setField(0, 1, Mark.WHITE);
        board.setField(1, 1, Mark.WHITE);
        board.setField(2, 1, Mark.WHITE);
        board.setField(3, 1, Mark.WHITE);
        board.setField(5, 1, Mark.WHITE);
        assertFalse(board.hasColumn(Mark.BLACK));
        assertFalse(board.hasColumn(Mark.WHITE));

        board.setField(4, 1, Mark.WHITE);
        assertTrue(board.hasColumn(Mark.WHITE));
        assertFalse(board.hasColumn(Mark.BLACK));
    }

    @Test
    public void testHasDiagonalDecreasing() {
        // middle
        board.setField(7, Mark.BLACK);
        board.setField(14, Mark.BLACK);
        board.setField(21, Mark.BLACK);
        board.setField(28, Mark.BLACK);
        assertFalse(board.hasDiagonal(Mark.BLACK));
        assertFalse(board.hasDiagonal(Mark.WHITE));

        board.setField(35, Mark.BLACK);
        assertTrue(board.hasDiagonal(Mark.BLACK));
        assertFalse(board.hasDiagonal(Mark.WHITE));

        // adjacent on the left
        board.reset();
        board.setField(6, Mark.BLACK);
        board.setField(13, Mark.BLACK);
        board.setField(20, Mark.BLACK);
        board.setField(27, Mark.BLACK);
        assertFalse(board.hasDiagonal(Mark.BLACK));
        assertFalse(board.hasDiagonal(Mark.WHITE));

        board.setField(34, Mark.BLACK);
        assertTrue(board.hasDiagonal(Mark.BLACK));
        assertFalse(board.hasDiagonal(Mark.WHITE));

        // adjacent on the right
        board.reset();
        board.setField(1, Mark.BLACK);
        board.setField(8, Mark.BLACK);
        board.setField(15, Mark.BLACK);
        board.setField(22, Mark.BLACK);
        assertFalse(board.hasDiagonal(Mark.BLACK));
        assertFalse(board.hasDiagonal(Mark.WHITE));

        board.setField(29, Mark.BLACK);
        assertTrue(board.hasDiagonal(Mark.BLACK));
        assertFalse(board.hasDiagonal(Mark.WHITE));
    }

    @Test
    public void testHasDiagonalIncreasing() {
        // main middle diagonal
        board.setField(5, Mark.BLACK);
        board.setField(10, Mark.BLACK);
        board.setField(15, Mark.BLACK);
        board.setField(20, Mark.BLACK);
        board.setField(30, Mark.BLACK);
        assertFalse(board.hasDiagonal(Mark.BLACK));
        assertFalse(board.hasDiagonal(Mark.WHITE));

        board.setField(25, Mark.BLACK);
        assertTrue(board.hasDiagonal(Mark.BLACK));
        assertFalse(board.hasDiagonal(Mark.WHITE));

        board.reset();

        // Adjacent to the left
        board.setField(4, Mark.BLACK);
        board.setField(9, Mark.BLACK);
        board.setField(14, Mark.BLACK);
        board.setField(19, Mark.BLACK);
        assertFalse(board.hasDiagonal(Mark.BLACK));
        assertFalse(board.hasDiagonal(Mark.WHITE));

        board.setField(24, Mark.BLACK);
        assertTrue(board.hasDiagonal(Mark.BLACK));
        assertFalse(board.hasDiagonal(Mark.WHITE));

        board.reset();

        // Adjacent to the right
        board.setField(11, Mark.BLACK);
        board.setField(16, Mark.BLACK);
        board.setField(21, Mark.BLACK);
        board.setField(26, Mark.BLACK);
        assertFalse(board.hasDiagonal(Mark.BLACK));
        assertFalse(board.hasDiagonal(Mark.WHITE));

        board.setField(31, Mark.BLACK);
        assertTrue(board.hasDiagonal(Mark.BLACK));
        assertFalse(board.hasDiagonal(Mark.WHITE));
        assertTrue(board.isGameOver());
    }

    @Test
    public void testRandomGame() {
        ArrayList<Integer> playableFieldIndexes = new ArrayList<>();
        for (int i = 0; i < 36; i++) {
            playableFieldIndexes.add(i);
        }
        int index;
        Quadrant quadrant;
        Direction direction;

        while (!board.isGameOver()) {
            index = (int) (Math.random()*playableFieldIndexes.size());
            quadrant = Quadrant.values()[new Random().nextInt(Quadrant.values().length)];
            direction = Direction.values()[new Random().nextInt(Direction.values().length)];
            board.makeAutomatedMove(new Move(playableFieldIndexes.get(index), new Rotation(quadrant, direction)));
            playableFieldIndexes.removeAll(playableFieldIndexes);
            for (int i = 0; i < 36; i++) {
                if (board.isEmptyField(i)) {
                    playableFieldIndexes.add(i);
                }
            }
        }
        assertTrue(board.isGameOver());
    }

    @Test
    public void testMultipleRandomGames() {
        for (int i = 0; i < 30000; i++) {
            this.testRandomGame();
        }
    }
}
