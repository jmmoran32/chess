package chess;

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
    private static final int clid = 11;

    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
        this.obid = iterator++;
    }

    public boolean equals(ChessPosition op) {
       if(this.row != op.getRow()) return false;
       if(this.col != op.getColumn()) return false;
       return true;
    }

    public int hashCode() {
        int hash = obid;
        int factor = 79;
        hash = factor * hash + Integer.hashCode(this.row);
        hash = factor * hash + Integer.hashCode(this.col);
        return hash;
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
