package chess;

import java.util.Collection;
import java.util.ArrayList;
import chess.ChessBoard;

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
    private static final int CLID = 7;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.color = pieceColor;
        this.type = type;
        this.obid = iterator++;
    }

    public static ChessPiece serialize(char p) {
        switch(p) {
            case('p'):
                return new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
            case('P'):
                return new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
            case('r'):
                return new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
            case('R'):
                return new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
            case('b'):
                return new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
            case('B'):
                return new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
            case('n'):
                return new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
            case('N'):
                return new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
            case('k'):
                return new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING);
            case('K'):
                return new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);
            case('q'):
                return new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN);
            case('Q'):
                return new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN);
            default:
                return null;
        }
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
        if(!this.color.equals(op.getTeamColor())) {
            return false;
        }
        if(!this.type.equals(op.getPieceType())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = CLID;
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
         if(this.color == ChessGame.TeamColor.WHITE) {
             return Character.toUpperCase(c);
         }
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
        ArrayList<ChessMove> moves = new ArrayList<ChessMove>();
        
        int m;
        int n;
        m = 8 - myPosition.getRow();
        n = myPosition.getColumn() - 1;
        ChessPiece bucket;
        int i;
        switch(this.type) {     //black is top
            case PieceType.PAWN:
                chess.piece.Pawn.pawnMoves(m, n, board, moves, myPosition, color);
                break;

            case PieceType.ROOK:
                cardinal(m, n, moves, board, myPosition);
                break;

            case PieceType.KNIGHT:
                chess.piece.Knight.knightMoves(m, n, board, moves, myPosition, color);
                break;

            case PieceType.BISHOP:
                diagonal(m, n, moves, board, myPosition);
                break;

            case PieceType.QUEEN:
                cardinal(m, n, moves, board, myPosition);
                diagonal(m, n, moves, board, myPosition);
                break;
            case PieceType.KING:
                if(m - 1 >= 0) {        //up
                    bucket = board.getPiece(m - 1, n);
                    if(bucket == null || bucket.getTeamColor() != this.color) {
                        moves.add(new ChessMove(myPosition, trans(m - 1, n), null));
                    }
                    if(n + 1 < 8) {     //up-right
                        bucket = board.getPiece(m - 1, n + 1);
                        if(bucket == null || bucket.getTeamColor() != this.color) {
                            moves.add(new ChessMove(myPosition, trans(m - 1, n + 1), null));
                        }
                    }
                    if(n - 1 >= 0) {     //up-left
                        bucket = board.getPiece(m - 1, n - 1);
                        if(bucket == null || bucket.getTeamColor() != this.color) {
                            moves.add(new ChessMove(myPosition, trans(m - 1, n - 1), null));
                        }
                    }
                }

                if(m + 1 < 8) {        //down
                    bucket = board.getPiece(m + 1, n);
                    if(bucket == null || bucket.getTeamColor() != this.color) {
                        moves.add(new ChessMove(myPosition, trans(m + 1, n), null));
                    }
                    if(n + 1 < 8) {     //down-right
                        bucket = board.getPiece(m + 1, n + 1);
                        if(bucket == null || bucket.getTeamColor() != this.color) {
                            moves.add(new ChessMove(myPosition, trans(m + 1, n + 1), null));
                        }
                    }
                    if(n - 1 >= 0) {     //down-left
                        bucket = board.getPiece(m + 1, n - 1);
                        if(bucket == null || bucket.getTeamColor() != this.color) {
                            moves.add(new ChessMove(myPosition, trans(m + 1, n - 1), null));
                        }
                    }
                }
                if(n - 1 >= 0) {        //left
                    bucket = board.getPiece(m, n - 1);
                    if(bucket == null || bucket.getTeamColor() != this.color) {
                        moves.add(new ChessMove(myPosition, trans(m, n - 1), null));
                    }
                }
                if(n + 1 < 8) {        //right
                    bucket = board.getPiece(m, n + 1);
                    if(bucket == null || bucket.getTeamColor() != this.color) {
                        moves.add(new ChessMove(myPosition, trans(m, n + 1), null));
                    }
                }
                break;
        }
        return moves;
    }

    private void cardinal(int m, int n, ArrayList<ChessMove> moves, ChessBoard board, ChessPosition myPosition) {
        ChessPiece bucket;
        int i = 1;
                while(n + i < 8) {   //right
                   bucket = board.getPiece(m, n + i); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, trans(m, n + i), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) {
                            moves.add(new ChessMove(myPosition, trans(m, n + i), null));
                        }
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
                        if(bucket.getTeamColor() != this.color) {
                            moves.add(new ChessMove(myPosition, trans(m, n - i), null));
                        }
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
                        if(bucket.getTeamColor() != this.color) {
                            moves.add(new ChessMove(myPosition, trans(m - i, n), null));
                        }
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
                        if(bucket.getTeamColor() != this.color) {
                            moves.add(new ChessMove(myPosition, trans(m + i, n), null));
                        }
                        break;
                   }
                }
    }

    private void diagonal(int m, int n, ArrayList<ChessMove> moves, ChessBoard board, ChessPosition myPosition) {
        ChessPiece bucket;
        int i = 1;
                while(m - i >= 0 && n + i < 8) {   //up-right
                   bucket = board.getPiece(m - i, n + i); 
                   if(bucket == null) {
                       moves.add(new ChessMove(myPosition, trans(m - i, n + i), null));
                       i++;
                       continue;
                   }
                   else {
                        if(bucket.getTeamColor() != this.color) {
                            moves.add(new ChessMove(myPosition, trans(m - i, n + i), null));
                        }
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
                        if(bucket.getTeamColor() != this.color) {
                            moves.add(new ChessMove(myPosition, trans(m + i, n + i), null));
                        }
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
                        if(bucket.getTeamColor() != this.color) {
                            moves.add(new ChessMove(myPosition, trans(m + i, n - i), null));
                        }
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
                        if(bucket.getTeamColor() != this.color) {
                            moves.add(new ChessMove(myPosition, trans(m - i, n - i), null));
                        }
                        break;
                   }
                }
    }

    private ChessPosition trans(int m, int n) {
        int row = 8 - m;
        int col = n + 1;
        return new ChessPosition(row, col);
    }

}
