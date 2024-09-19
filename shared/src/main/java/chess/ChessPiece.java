package chess;

import java.util.Collection;
import java.util.ArrayList;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor color;
    private PieceType type;
    private static int iterator = 1;
    private int obid;
    private static final int clid = 7;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.color = pieceColor;
        this.type = type;
        this.obid = iterator++;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    @Override
    public boolean equals(Object ob) {
        ChessPiece op = (ChessPiece) ob;
        if(!this.color.equals(op.getTeamColor())) return false;
        if(!this.type.equals(op.getPieceType())) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = clid;
        int factor = 83;
        hash = factor * hash + (this.color != null ? this.color.hashCode() : 0);
        hash = factor * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
       return String.format("[%s %s]", this.color.toString(), this.type.toString());
    }

    public char getPieceChar() {
        char c = 'U';
        switch(this.type) {
            case(PieceType.PAWN) :
                c = 'p';
                break;
            case(PieceType.ROOK) :
                c = 'r';
                break;
            case(PieceType.KNIGHT) :
                c = 'n';
                break;
            case(PieceType.BISHOP) :
                c = 'b';
                break;
            case(PieceType.QUEEN) :
                c = 'q';
                break;
            case(PieceType.KING) :
                c = 'k';
                break;
        }
         if(this.color == ChessGame.TeamColor.WHITE) return Character.toUpperCase(c);
         return c;
        }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        //throw new RuntimeException("Not implemented");
        ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
        
        int m;
        int n;
        m = 8 - myPosition.getRow();
        n = myPosition.getColumn() - 1;
        ChessPiece bucket;
        int i;
        switch(this.type) {     //black is top
            case PieceType.PAWN:
                if(this.color == ChessGame.TeamColor.BLACK) {
                    if(m == 1 && board.getPiece(m + 2, n) == null)      //BLACK ^ first move 
                        moves.add(new ChessMove(myPosition, trans(m, n), null)); 
                    if(board.getPiece(m + 1, n) == null)                //BLACK ^ down
                        moves.add(new ChessMove(myPosition, trans(m + 1, n), null));

                    if(m + 1 < 8 && n + 1 < 8) { 
                        bucket = board.getPiece(m + 1, n + 1);
                        if(bucket != null && bucket.getTeamColor() == ChessGame.TeamColor.WHITE)    //BLACK ^ capture right
                            moves.add(new ChessMove(myPosition, trans(m + 1, n + 1), null));
                    }
                    if(m + 1 < 8 && n - 1 >=0) {
                        bucket = board.getPiece(m + 1, n - 1);
                        if(bucket != null && bucket.getTeamColor() == ChessGame.TeamColor.WHITE)    //BLACK ^ capture left
                            moves.add(new ChessMove(myPosition, trans(m + 1, n - 1), null));
                    }
                }
                else {
                    if(m == 6 && board.getPiece(m - 2, n) == null)      //WHITE ^ first move 
                        moves.add(new ChessMove(myPosition, trans(m - 2, n), null)); 
                    if(board.getPiece(m - 1, n) == null)                //WHITE ^ up
                        moves.add(new ChessMove(myPosition, trans(m - 1, n), null));
                    if(m - 1 >= 0 && n + 1 < 8) {
                        bucket = board.getPiece(m - 1, n + 1);
                        if(bucket != null && bucket.getTeamColor() == ChessGame.TeamColor.BLACK)    //WHITE ^ capture right
                            moves.add(new ChessMove(myPosition, trans(m - 1, n + 1), null));
                    }

                    if(m - 1 >= 0 && n - 1 >= 0) {
                        bucket = board.getPiece(m - 1, n - 1);
                        if(bucket != null && bucket.getTeamColor() == ChessGame.TeamColor.BLACK)    //WHITE ^ capture left
                            moves.add(new ChessMove(myPosition, trans(m - 1, n - 1), null));
                    }
                }
                break;

            case PieceType.ROOK:
                i = 1;
                while(n + i < 8) {   //right
                   bucket = board.getPiece(m, n + i); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, trans(m, n + i), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) 
                            moves.add(new ChessMove(myPosition, trans(m, n + i), null));
                        break;
                   }
                }

                i = 1;
                while(n - i >= 0) {   //left
                   bucket = board.getPiece(m, n - i); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, trans(m, n - i), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) 
                            moves.add(new ChessMove(myPosition, trans(m, n - i), null));
                        break;
                   }
                }

                i = 1;
                while(m - i >= 0) {   //up
                   bucket = board.getPiece(m - i, n); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, trans(m - i, n), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) 
                            moves.add(new ChessMove(myPosition, trans(m - i, n), null));
                        break;
                   }
                }

                i = 1;
                while(m + i < 8) {   //down
                   bucket = board.getPiece(m + i, n); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, trans(m + i, n), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) 
                            moves.add(new ChessMove(myPosition, trans(m + i, n), null));
                        break;
                   }
                }
                break;

            case PieceType.KNIGHT:
                if(m - 2 >= 0) {         //up
                    if(n + 1 < 8) {     //up-right
                        bucket = board.getPiece(m - 2, n + 1);
                        if(bucket == null || bucket.getTeamColor() != this.color)
                            moves.add(new ChessMove(myPosition, trans(m - 2, n + 1), null));
                    }
                    if(n - 1 >= 0) {    //up-left
                        bucket = board.getPiece(m - 2, n - 1);
                        if(bucket == null || bucket.getTeamColor() != this.color)
                            moves.add(new ChessMove(myPosition, trans(m - 2, n - 1), null));
                    }
                }
                if(n + 2 < 8) {         //right
                    if(m - 1 >= 0) {     //right-up
                        bucket = board.getPiece(m - 1, n + 2);
                        if(bucket == null || bucket.getTeamColor() != this.color)
                            moves.add(new ChessMove(myPosition, trans(m - 1, n + 2), null));
                    }
                    if(m + 1 < 8) {    //right-down
                        bucket = board.getPiece(m + 1, n + 2);
                        if(bucket == null || bucket.getTeamColor() != this.color)
                            moves.add(new ChessMove(myPosition, trans(m + 1, n + 2), null));
                    }
                }
                if(m + 2 < 8) {         //down
                    if(n + 1 < 8) {     //down-right
                        bucket = board.getPiece(m + 2, n + 1);
                        if(bucket == null || bucket.getTeamColor() != this.color)
                            moves.add(new ChessMove(myPosition, trans(m + 2, n + 1), null));
                    }
                    if(n - 1 >= 0) {    //down-left
                        bucket = board.getPiece(m + 2, n - 1);
                        if(bucket == null || bucket.getTeamColor() != this.color)
                            moves.add(new ChessMove(myPosition, trans(m + 2, n - 1), null));
                    }
                }
                if(n - 2 >= 0) {         //left
                    if(m - 1 >= 0) {     //left-up
                        bucket = board.getPiece(m - 1, n - 2);
                        if(bucket == null || bucket.getTeamColor() != this.color)
                            moves.add(new ChessMove(myPosition, trans(m - 1, n - 2), null));
                    }
                    if(m + 1 < 8) {    //left-down
                        bucket = board.getPiece(m + 1, n - 2);
                        if(bucket == null || bucket.getTeamColor() != this.color)
                            moves.add(new ChessMove(myPosition, trans(m + 1, n - 2), null));
                    }
                }
                break;

            case PieceType.BISHOP:
                i = 1;
                while(m - i >= 0 && n + i < 8) {   //up-right
                   bucket = board.getPiece(m - i, n + i); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, trans(m - i, n + i), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) 
                            moves.add(new ChessMove(myPosition, trans(m - i, n + i), null));
                        break;
                   }
                }

                i = 1;
                while(m + i < 8 && n + i < 8) {   //down-right
                   bucket = board.getPiece(m + i, n + i); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, trans(m + i, n + i), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) 
                            moves.add(new ChessMove(myPosition, trans(m + i, n + i), null));
                        break;
                   }
                }

                i = 1;
                while(m + i < 8 && n - i >= 0) {   //down-left
                   bucket = board.getPiece(m + i, n - i); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, trans(m + i, n - i), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) 
                            moves.add(new ChessMove(myPosition, trans(m + i, n - i), null));
                        break;
                   }
                }

                i = 1;
                while(m - i >= 0 && n - i >= 0) {   //up-left
                   bucket = board.getPiece(m - i, n - i); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, trans(m - i, n - i), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) 
                            moves.add(new ChessMove(myPosition, trans(m - i, n - i), null));
                        break;
                   }
                }
                break;

            case PieceType.QUEEN:
                i = 1;
                while(n + i < 8) {   //right
                   bucket = board.getPiece(m, n + i); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, trans(m, n + i), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) 
                            moves.add(new ChessMove(myPosition, trans(m, n + i), null));
                        break;
                   }
                }

                i = 1;
                while(n - i >= 0) {   //left
                   bucket = board.getPiece(m, n - i); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, trans(m, n - i), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) 
                            moves.add(new ChessMove(myPosition, trans(m, n - i), null));
                        break;
                   }
                }

                i = 1;
                while(m - i >= 0) {   //up
                   bucket = board.getPiece(m - i, n); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, trans(m - i, n), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) 
                            moves.add(new ChessMove(myPosition, trans(m - i, n), null));
                        break;
                   }
                }

                i = 1;
                while(m + i < 8) {   //down
                   bucket = board.getPiece(m + i, n); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, trans(m + i, n), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) 
                            moves.add(new ChessMove(myPosition, trans(m + i, n), null));
                        break;
                   }
                }
                i = 1;
                while(m - i >= 0 && n + i < 8) {   //up-right
                   bucket = board.getPiece(m - i, n + i); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, trans(m - i, n + i), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) 
                            moves.add(new ChessMove(myPosition, trans(m - i, n + i), null));
                        break;
                   }
                }

                i = 1;
                while(m + i < 8 && n + i < 8) {   //down-right
                   bucket = board.getPiece(m + i, n + i); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, trans(m + i, n + i), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) 
                            moves.add(new ChessMove(myPosition, trans(m + i, n + i), null));
                        break;
                   }
                }

                i = 1;
                while(m + i < 8 && n - i >= 0) {   //down-left
                   bucket = board.getPiece(m + i, n - i); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, trans(m + i, n - i), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) 
                            moves.add(new ChessMove(myPosition, trans(m + i, n - i), null));
                        break;
                   }
                }

                i = 1;
                while(m - i >= 0 && n - i >= 0) {   //up-left
                   bucket = board.getPiece(m - i, n - i); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, trans(m - i, n - i), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) 
                            moves.add(new ChessMove(myPosition, trans(m - i, n - i), null));
                        break;
                   }
                }
                break;
            case PieceType.KING:
                if(m - 1 >= 0) {        //up
                    bucket = board.getPiece(m - 1, n);
                    if(bucket == null || bucket.getTeamColor() != this.color) {
                        moves.add(new ChessMove(myPosition, trans(m - 1, n), null));
                    }
                }

                if(m + 1 < 8) {        //down
                    bucket = board.getPiece(m, n);
                    if(bucket == null || bucket.getTeamColor() != this.color) {
                        moves.add(new ChessMove(myPosition, trans(m + 1, n), null));
                    }
                }
                if(n - 1 >= 0) {        //left
                    bucket = board.getPiece(m, n);
                    if(bucket == null || bucket.getTeamColor() != this.color) {
                        moves.add(new ChessMove(myPosition, trans(m, n - 1), null));
                    }
                }
                if(n + 1 < 8) {        //right
                    bucket = board.getPiece(m, n);
                    if(bucket == null || bucket.getTeamColor() != this.color) {
                        moves.add(new ChessMove(myPosition, trans(m, n + 1), null));
                    }
                }
                break;
        }
        
        return moves;
    }

    private ChessPosition trans(int m, int n) {
        int row = 8 - m;
        int col = n + 1;
        return new ChessPosition(row, col);
    }

}
