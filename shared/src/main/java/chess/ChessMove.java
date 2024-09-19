package chess;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {
    private ChessPosition start;
    private ChessPosition end;
    private ChessPiece.PieceType promotion;
    private static int iterator = 1;
    private int obid;
    private static final int clid = 5;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        this.start = startPosition;
        this.end = endPosition;
        this.promotion = promotionPiece;
        this.obid = iterator++;
    }

    @Override
    public String toString() {
       StringBuilder sb = new StringBuilder();
       sb.append(String.format("{s:%s | e:%s |", this.start.toString(), this.end.toString()));
       if(this.promotion == null) sb.append(" p:null}");
       else sb.append(String.format(" p:%s}", promotion.toString()));
       return sb.toString();
    }

    @Override
    public boolean equals(Object ob) {
        ChessMove op = (ChessMove) ob;
        if(!this.start.equals(op.getStartPosition())) return false;
        if(!this.end.equals(op.getEndPosition())) return false;
        if(op.getPromotionPiece() == null || this.promotion == null) {
            if(this.promotion != op.getPromotionPiece()) return false;
        }
        else if(!this.promotion.equals(op.getPromotionPiece())) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = clid;
        int factor = 51;
        hash = factor * hash + (this.start != null ? this.start.hashCode() : 0);
        hash = factor * hash + (this.end != null ? this.end.hashCode() : 0);
        hash = factor * hash + (this.promotion != null ? this.promotion.hashCode() : 0);
        return hash;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return this.start;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return this.end;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promotion;
    }
}
