package chess;

import java.util.Objects;
/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    int row;
    int col;
    private static int iterator = 1;
    private int obid;
    private static final int CLID = 11;

    public ChessPosition(int row, int col) {
        this.row =  row;
        this.col = col;
        this.obid = iterator++;
    }

    @Override
    public boolean equals(Object ob) {
        ChessPosition op = (ChessPosition) ob;
        if(this.row != op.getRow()) {
            return false;
        }
        if(this.col != op.getColumn()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.row, this.col);
    }

    @Override
    public String toString() {
        return String.format("[row: %d | col %d]", this.row, this.col);
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return this.row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return this.col;
    }
}
