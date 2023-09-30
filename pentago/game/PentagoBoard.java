package pentago.game;

import pentago.game.models.*;

/**
 * Main Pentago game logic.
 */
public class PentagoBoard {
    /**
     * Constant that holds the dimension of the PentagoBoard field.
     */
    public static final int DIM = 6;

    /**
     * The main array that contains the current state of the game.
     */
    private final Mark[] fields;


    /**
     * The amount of Marks in sequence in row/column/diagonal to get in order to win.
     */
    private static final int WIN_DIM = 5;


    /**
     * Spacing for the board printing into the console.
     */
    private static final String DELIM = "           ";
    private static final String SPLITTER = "----+----+----||----+----+----";
    private static final String DIVIDER = "====+====+====||====+====+====";


    /**
     * Index numbering of the fields for the PentagoBoard with styling for printing.
     */
    private static final String[] NUMBERING = {" 00 | 01 | 02 || 03 | 04 | 05 ", SPLITTER,
        " 06 | 07 | 08 || 09 | 10 | 11 ", SPLITTER, " 12 | 13 | 14 || 15 | 16 | 17 ",
        DIVIDER, " 18 | 19 | 20 || 21 | 22 | 23 ", SPLITTER,
        " 24 | 25 | 26 || 27 | 28 | 29 ", SPLITTER, " 30 | 31 | 32 || 33 | 34 | 35 "};


    /**
     * Storing the divider of the field for easier access later.
     */
    private static final String LINE = NUMBERING[1];


    /**
     * Variable to keep track of the amount of moves that have been played by both players.
     */
    private int moveCounter;

    /**
     * Constructor of the board, initializes with fields set to Mark.EMPTY.
     */
    public PentagoBoard() {
        this.fields = new Mark[DIM * DIM];
        this.reset();
        this.moveCounter = 0;
    }

    /**
     * Prints the current state of the board.
     */
    public String printBoard() {
        String s = "";
        for (int i = 0; i < DIM; i++) {
            String row = "";
            for (int j = 0; j < DIM; j++) {
                String letter = getField(i, j).toString().substring(0, 1).replace("E", " ");
                row = row + " " + letter + letter + " ";
                if (j == DIM / 2 - 1) {
                    row = row + "||";
                } else if (j < DIM - 1) {
                    row = row + "|";
                }
            }
            s = s + row + DELIM + NUMBERING[i * 2];
            if (i == DIM / 2 - 1) {
                s = s + "\n" + DIVIDER + DELIM + NUMBERING[i * 2 + 1] + "\n";
            } else if (i < DIM - 1) {
                s = s + "\n" + LINE + DELIM + NUMBERING[i * 2 + 1] + "\n";
            }
        }
        return s;
    }

    /**
     * Returns if the index of the field points to an existing field.
     *
     * @param i index of the field
     * @return is the given index of field valid
     */
    public boolean isField(int i) {
        return i < DIM * DIM && i >= 0;
    }

    /**
     * Returns whether the field is empty.
     *
     * @param i index of the field
     * @return is the given field empty
     */
    public boolean isEmptyField(int i) {
        return fields[i].equals(Mark.EMPTY);
    }

    /**
     * Checks the gameOver condition.
     *
     * @return Check if the game over conditions are met
     */
    public boolean isGameOver() {
        return isFull() || hasOneWinner() || hasTwoWinners();
    }

    /**
     * Check winning condition for specific Mark in all rows.
     *
     * @param m Player mark
     * @return Does the player have a winning condition in row
     */
    public boolean hasRow(Mark m) {
        for (int r = 0; r < DIM; r++) {
            int count = 0;
            for (int c = 0; c < DIM; c++) {
                if (getField(r, c).equals(m)) {
                    count++;
                } else {
                    count = 0;
                }

                if (count >= WIN_DIM) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check winning condition for specific Mark in all columns.
     *
     * @param m Player mark
     * @return Does the player have a winning condition in column
     */
    public boolean hasColumn(Mark m) {
        for (int c = 0; c < DIM; c++) {
            int count = 0;
            for (int r = 0; r < DIM; r++) {
                if (getField(r, c).equals(m)) {
                    count++;
                } else {
                    count = 0;
                }

                if (count >= WIN_DIM) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check winning condition for specific Mark in all diagonals.
     *
     * @param m Player mark
     * @return Does the player have a winning condition in diagonal
     */
    public boolean hasDiagonal(Mark m) {
        int count1 = 0;
        int count2 = 0;
        int count3 = 0;
        int count4 = 0;
        int count5 = 0;
        int count6 = 0;

        for (int i = 0; i < DIM; i++) {
            // Checking the main middle diagonal
            if (getField(i, i).equals(m)) {
                count1++;
            } else {
                count1 = 0;
            }

            // Checking the two adjacent to the main diagonal
            if (i < DIM - 1 && getField(i, i + 1).equals(m)) {
                count2++;
            }
            if (i < DIM - 1 && getField(i + 1, i).equals(m)) {
                count3++;
            }

            // Checking the main middle diagonal from right top corner
            if (getField(i, DIM - i - 1).equals(m)) {
                count4++;
            } else {
                count4 = 0;
            }

            // Checking the two adjancet to the main middle diagonal from right top corner
            if (i < DIM - 1 && getField(i, DIM - i - 2).equals(m)) {
                count5++;
            }
            if (i < DIM - 1 && getField(i + 1, DIM - i - 1).equals(m)) {
                count6++;
            }

            if (count1 >= WIN_DIM || count2 >= WIN_DIM || count3 >= WIN_DIM
                    || count4 >= WIN_DIM || count5 >= WIN_DIM || count6 >= WIN_DIM) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether specific mark is a winner on the board.
     *
     * @param m Player mark
     * @return Is the given player a winner
     */
    public boolean isWinner(Mark m) {
        return hasRow(m) || hasColumn(m) || hasDiagonal(m);
    }

    /**
     * Checks whether any mark is a winner on the board, performs XOR.
     *
     * @return Is there a winner on the board
     */
    public boolean hasOneWinner() {
        return isWinner(Mark.BLACK) && !isWinner(Mark.WHITE)
                || !isWinner(Mark.BLACK) && isWinner(Mark.WHITE);
    }

    /**
     * Checks if both of players have won the game.
     *
     * @return Have both players won
     */
    public boolean hasTwoWinners() {
        return isWinner(Mark.BLACK) && isWinner(Mark.WHITE);
    }

    /**
     * Tests if the whole board is full.
     *
     * @return true if all fields are occupied
     */
    public boolean isFull() {
        for (int i = 0; i < fields.length; i++) {
            if (isEmptyField(i)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Resets the board fields back to being Empty.
     */
    public void reset() {
        for (int i = 0; i < DIM * DIM; i++) {
            setField(i, Mark.EMPTY);
        }
        this.moveCounter = 0;
    }

    /**
     * @param i Index of the field being set
     * @param m Given player mark that will be set on field
     */
    public void setField(int i, Mark m) {
        fields[i] = m;
    }

    /**
     * @param row the row of the field
     * @param col the column of the field
     */
    public void setField(int row, int col, Mark m) {
        fields[index(row, col)] = m;
    }

    /**
     * Returns the content of the field i.
     *
     * @param i the number of the field
     * @return the mark on the field
     */
    public Mark getField(int i) {
        return fields[i];
    }

    /**
     * Returns the content of the field referred to by the (row,col) pair.
     *
     * @param row the row of the field
     * @param col the column of the field
     * @return the mark on the field
     */
    public Mark getField(int row, int col) {
        return fields[index(row, col)];
    }

    /**
     * Calculates the index in the linear array of fields from a (row, col)
     * pair.
     *
     * @return the index belonging to the (row,col)-field
     */
    public int index(int row, int col) {
        return (DIM * row) + col;
    }

    /**
     * Creates a deep copy of this field.
     */
    public PentagoBoard deepCopy() {
        PentagoBoard result = new PentagoBoard();

        for (int i = 0; i < DIM * DIM; i++) {
            result.setField(i, this.getField(i));
        }

        return result;
    }

    /**
     * Getter for the moveCounter that shows amount of moves done so far.
     *
     * @return the amount of moves done so far
     */
    public int getMoveCounter() {
        return moveCounter;
    }

    /**
     * Rotation function takes the row and col offset
     * of the quadrant it is going to rotate and with
     * a help of a bit of math it positions itself
     * correctly to rotate the quadrant by either 90
     * degrees to the right or left.
     *
     * @param rotation Rotation object with quadrant and direction
     */
    public void rotate(Rotation rotation) {
        int rowOffset = this.getRowOffset(rotation.getQuadrant());
        int colOffset = this.getColOffset(rotation.getQuadrant());

        PentagoBoard board = this.deepCopy();
        for (int i = rowOffset; i < (DIM / 2) + rowOffset; i++) {
            for (int j = colOffset; j < (DIM / 2) + colOffset; j++) {
                if (rotation.getDirection().equals(Direction.CLOCKWISE)) {
                    this.setField(i, j,
                            board.getField(
                                    ((DIM / 2) - 1) + rowOffset + colOffset - j,
                                    i - rowOffset + colOffset
                            )
                    );
                } else {
                    this.setField(i, j,
                            board.getField(
                                    j + rowOffset - colOffset,
                                    ((DIM / 2) - 1) + rowOffset + colOffset - i
                            )
                    );
                }
            }
        }
    }

    /**
     * Get the row offset for each quadrant.
     *
     * @param quadrant The quadrant that we want to get the row offset of
     * @return the row index of the first field in the quadrant
     */
    public int getRowOffset(Quadrant quadrant) {
        return (quadrant.equals(Quadrant.BOTTOM_LEFT) || quadrant.equals(Quadrant.BOTTOM_RIGHT))
                ? DIM / 2 : 0;
    }

    /**
     * Get the column offset for each quadrant
     * (indicates the col index of the first field in the quadrant).
     *
     * @param quadrant The quadrant that we want to get the column offset of
     * @return the row index of the first field in the quadrant
     */
    public int getColOffset(Quadrant quadrant) {
        return (quadrant.equals(Quadrant.TOP_RIGHT) || quadrant.equals(Quadrant.BOTTOM_RIGHT))
                ? DIM / 2 : 0;
    }

    /**
     * Makes a valid move on the Pentago board with a
     * given mark for the move.
     *
     * @param move Move object with Rotation and Field
     * @param mark mark of the player making a move
     */
    public void makeMove(Move move, Mark mark) {
        this.setField(move.getField(), mark);
        this.rotate(move.getRotation());
        this.moveCounter++;
    }

    /**
     * Based on the amount of moves a move with the
     * correct mark is played, therefore allowing for
     * making a move without knowing who is playing.
     *
     * @param move Move object with Rotation and Field
     */
    public void makeAutomatedMove(Move move) {
        if (this.moveCounter % 2 == 0) {
            this.makeMove(move, Mark.BLACK);
        } else {
            this.makeMove(move, Mark.WHITE);
        }
    }
}
