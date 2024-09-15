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

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.color = pieceColor;
        this.type = type;
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
        m = myPosition.getRow() - 1;
        n = myPosition.getColumn() - 1;
        ChessPiece bucket;
        int i;
        switch(this.type) {     //black is top
            case PieceType.PAWN:
                if(this.color == ChessGame.TeamColor.BLACK) {
                    if(m == 1 && board.getPiece(m + 2, n) == null)      //BLACK ^ first move 
                        moves.add(new ChessMove(myPosition, new ChessPosition((m + 1) + 2, (n + 1)), null)); 
                    if(board.getPiece(m + 1, n) == null)                //BLACK ^ down
                        moves.add(new ChessMove(myPosition, new ChessPosition((m + 1) + 1, (n + 1)), null));

                    bucket = board.getPiece(m + 1, n + 1);
                    if(bucket != null && bucket.getTeamColor() == ChessGame.TeamColor.WHITE)    //BLACK ^ capture right
                        moves.add(new ChessMove(myPosition, new ChessPosition((m + 1) + 1, (n + 1) + 1), null));
                    bucket = board.getPiece(m + 1, n - 1);
                    if(bucket != null && bucket.getTeamColor() == ChessGame.TeamColor.WHITE)    //BLACK ^ capture left
                        moves.add(new ChessMove(myPosition, new ChessPosition((m + 1) + 1, (n + 1) - 1), null));
                }
                else {
                    if(m == 6 && board.getPiece(m - 2, n) == null)      //WHITE ^ first move 
                        moves.add(new ChessMove(myPosition, new ChessPosition((m + 1) - 2, (n + 1)), null)); 
                    if(board.getPiece(m - 1, n) == null)                //WHITE ^ up
                        moves.add(new ChessMove(myPosition, new ChessPosition((m + 1) - 1, (n + 1)), null));

                    bucket = board.getPiece(m - 1, n + 1);
                    if(bucket != null && bucket.getTeamColor() == ChessGame.TeamColor.BLACK)    //WHITE ^ capture right
                        moves.add(new ChessMove(myPosition, new ChessPosition((m + 1) - 1, (n + 1) + 1), null));
                    bucket = board.getPiece(m - 1, n - 1);
                    if(bucket != null && bucket.getTeamColor() == ChessGame.TeamColor.BLACK)    //WHITE ^ capture left
                        moves.add(new ChessMove(myPosition, new ChessPosition((m + 1) - 1, (n + 1) - 1), null));
                    }
                break;

            case PieceType.ROOK:
                i = 1;
                while(n + i < 8) {   //right
                   bucket = board.getPiece(m, n + i); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, new ChessPosition((m + 1), (n + 1) + i), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) 
                            moves.add(new ChessMove(myPosition, new ChessPosition((m + 1), (n + 1) + i), null));
                        break;
                   }
                }

                i = 1;
                while(n - i <= 0) {   //left
                   bucket = board.getPiece(m, n - i); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, new ChessPosition((m + 1), (n + 1) - i), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) 
                            moves.add(new ChessMove(myPosition, new ChessPosition((m + 1), (n + 1) - i), null));
                        break;
                   }
                }

                i = 1;
                while(m - i <= 0) {   //up
                   bucket = board.getPiece(m - i, n); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, new ChessPosition((m + 1) - i, (n + 1)), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) 
                            moves.add(new ChessMove(myPosition, new ChessPosition((m + 1) - i, (n + 1)), null));
                        break;
                   }
                }

                i = 1;
                while(m + i <= 0) {   //down
                   bucket = board.getPiece(m + i, n); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, new ChessPosition((m + 1) + i, (n + 1)), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) 
                            moves.add(new ChessMove(myPosition, new ChessPosition((m + 1) + i, (n + 1)), null));
                        break;
                   }
                }
                break;

            case PieceType.KNIGHT:
                if(this.color == ChessGame.TeamColor.BLACK) {

                }
                else {

                }
                break;

            case PieceType.BISHOP:
                i = 1;
                while(m - i >= 0 && n + i < 8) {   //up-right
                   bucket = board.getPiece(m + i, n + i); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, new ChessPosition((m + 1) - i, (n + 1) + i), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) 
                            moves.add(new ChessMove(myPosition, new ChessPosition((m + 1) - i, (n + 1) + i), null));
                        break;
                   }
                }

                i = 1;
                while(m + i < 8 && n + i < 8) {   //down-right
                   bucket = board.getPiece(m + i, n + i); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, new ChessPosition((m + 1) + i, (n + 1) + i), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) 
                            moves.add(new ChessMove(myPosition, new ChessPosition((m + 1) + i, (n + 1) + i), null));
                        break;
                   }
                }

                i = 1;
                while(m + i < 8 && n - i > 0) {   //down-left
                   bucket = board.getPiece(m + i, n - i); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, new ChessPosition((m + 1) + i, (n + 1) - i), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) 
                            moves.add(new ChessMove(myPosition, new ChessPosition((m + 1) + i, (n + 1) - i), null));
                        break;
                   }
                }

                i = 1;
                while(m - i > 0 && n - i > 0) {   //up-left
                   bucket = board.getPiece(m - i, n - i); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, new ChessPosition((m + 1) - i, (n + 1) - i), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) 
                            moves.add(new ChessMove(myPosition, new ChessPosition((m + 1) - i, (n + 1) - i), null));
                        break;
                   }
                }
                break;

            case PieceType.QUEEN:
                if(this.color == ChessGame.TeamColor.BLACK) {

                }
                else {

                }
                break;
            case PieceType.KING:
                if(m - 1 >= 0) {
                    m--;
                    bucket = board.getPiece(m, n);
                    if(bucket == null || bucket.getTeamColor() != this.color) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(m + 1, n + 1), null));
                    }
                    m++;
                }

                if(m + 1 <= 7) {
                    m++;
                    bucket = board.getPiece(m, n);
                    if(bucket == null || bucket.getTeamColor() != this.color) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(m + 1, n + 1), null));
                    }
                }
                m--;
                if(n - 1 >= 0) {
                    n--;
                    bucket = board.getPiece(m, n);
                    if(bucket == null || bucket.getTeamColor() != this.color) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(m + 1, n + 1), null));
                    }
                    n++;
                }
                if(n + 1 <= 7) {
                    n++;
                    bucket = board.getPiece(m, n);
                    if(bucket == null || bucket.getTeamColor() != this.color) {
                        moves.add(new ChessMove(myPosition, new ChessPosition(m + 1, n + 1), null));
                    }
                    n--;
                }
                break;
        }
        
        return moves;
    }
}
