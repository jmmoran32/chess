package chess;

import chess.ChessPiece;
import chess.ChessPosition;
import chess.ChessGame;
import java.util.Objects;
import java.util.Arrays;
import java.util.Collection;
import java.util.ArrayList;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private ChessPiece[][] board;
    private ChessPiece[] whiteBox;
    private byte wbn;
    private ChessPiece[] blackBox;
    private byte bbn;
    private static int iterator = 1;
    private int obid;
    private static final int clid = 1;

    public ChessBoard() {
        this.board = new ChessPiece[8][8];
        this.whiteBox = new ChessPiece[32];
        this.blackBox = new ChessPiece[32];
        wbn = 0;
        bbn = 0;
        this.obid = iterator++;
    }

    public ChessPosition kingAt(ChessGame.TeamColor c) {
        for(int i = 0; i < 8; i++)
            for(int j = 0; j < 8; j++) {
                if(this.board[i][j] == null)
                    continue;
                if(this.board[i][j].getPieceType() == ChessPiece.PieceType.KING && this.board[i][j].getTeamColor() == c) {
                    ChessPosition kingAt = new ChessPosition(8 - i, j + 1);
                    //System.out.println(String.format("Found king %s king at %s", c.toString(), kingAt.toString()));
                    return kingAt;
                }
            }
        return null;
    }

    public Collection<ChessPiece> getTeam(ChessGame.TeamColor c) {
        ArrayList<ChessPiece> pieces = new ArrayList<ChessPiece>();
        for(int i = 0; i < 8; i++)
            for(int j = 0; j < 8; j++)
                if(this.board[i][j].getTeamColor() == c)
                    pieces.add(this.board[i][j]);
        return pieces;
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        int m;
        int n;
        m = 8 - position.getRow();
        n = position.getColumn() - 1;
        this.board[m][n] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        int m;
        int n;
        m = 8 - position.getRow();
        n = position.getColumn() - 1;
        //System.out.println(String.format("Getting a piece from %d %d", m, n));
        //System.out.println(String.format("Getting Piece at actual position %d, %d", m, n));
        return this.board[m][n];
    }

    public ChessPiece getPiece(int m, int n) {
        return this.board[m][n];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        this.board[0][0] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
        this.board[0][1] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        this.board[0][2] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        this.board[0][3] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN);
        this.board[0][4] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING);
        this.board[0][5] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        this.board[0][6] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        this.board[0][7] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
        this.board[7][0] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        this.board[7][1] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        this.board[7][2] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        this.board[7][3] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN);
        this.board[7][4] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);
        this.board[7][5] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        this.board[7][6] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        this.board[7][7] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        for(byte i = 0; i < 8; i++) this.board[1][i] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
        for(byte i = 0; i < 8; i++) this.board[6][i] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
    }

    /**
     * places a piece in a box
     */
    public void boxPiece(ChessPosition position) {
        int m = position.getRow() - 1;
        int n = position.getColumn() - 1;
        if(this.board[m][n] == null) {
            //System.out.println(String.format("This position (%d:%d) is already empty!", position.getRow() - 1, position.getColumn() - 1));
            return; 
        }

        ChessPiece p = this.board[m][n];
        if(p.getTeamColor() == ChessGame.TeamColor.WHITE) {
           this.whiteBox[wbn++] = p; 
        }
        else if(p.getTeamColor() == ChessGame.TeamColor.BLACK) {
           this.blackBox[bbn++] = p; 
        }
        this.board[m][n] = null;

        return;
    }

    /**
     * clears the entire board, and the boxes
     */
    private void clearBoard() {
        for(byte i = 0; i < 8; i++) {
            for(byte j = 0; j < 8; j++) {
                this.board[i][j] = null;
            }
        }
        for(byte i = 0; i < 64; i++) this.whiteBox[i] = null;
        for(byte i = 0; i < 64; i++) this.blackBox[i] = null;
    }

    @Override
    public int hashCode() {return Arrays.deepHashCode(this.board);}

    @Override
    public boolean equals(Object ob) {
        ChessBoard op = (ChessBoard) ob;
        ChessPiece bucket;
        for(int i = 0; i < 8; i++) 
           for(int j = 0; j < 8; j++) { 
                bucket = op.getPiece(i, j);
                if(bucket == null) {
                    if(this.board[i][j] != null) return false;
                    continue;
                }
                if(!bucket.equals(this.board[i][j])) return false;
           }
       return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        ChessPiece p;
        for(int i = 0; i < 8; i++) {
            sb.append('|');
            for(int j = 0; j < 8; j++) {
               p = this.board[i][j];
               if(p == null) sb.append(" |");
               else sb.append(String.format("%c|", p.getPieceChar()));
            }
            sb.append('\n');
        }
        return sb.toString();
    }

}
