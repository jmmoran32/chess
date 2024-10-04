package chess;

import java.util.Collection;
import java.util.ArrayList;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private boolean whiteTurn;
    private ChessBoard board;
    private ChessPosition bKing;
    private ChessPosition wKing;
    private boolean bCheck;
    private boolean wCheck;
    private static int iterator = 1;
    private int obid;
    private static final int clid = 3;

    public ChessGame() {
        this.obid = iterator++;
        this.whiteTurn = true;
        this.board = new ChessBoard();
        this.bKing = new ChessPosition(8, 5);
        this.wKing = new ChessPosition(1, 5);
        this.wCheck = false;
        this.bCheck = false;
    }

    /*
    public ChessGame(ChessBoard board) {
        this.obid = iterator++;
        setBoard(board);
    }
    */

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.whiteTurn ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.whiteTurn = (team == ChessGame.TeamColor.WHITE);
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ArrayList<ChessMove> moves;
        ChessPiece p = board.getPiece(startPosition);
        moves = (ArrayList<ChessMove>) p.pieceMoves(this.board, startPosition);
        //ChessRule.filter(moves);
        ChessMove m;
        for(int i = 0; i < moves.size(); i++) {
           m = moves.get(i);
           if(!testMove(m))
               moves.remove(i);
        }
        return moves;
    }

    private boolean testMove(ChessMove mov) {
        ChessGame.TeamColor c;
        ChessPiece p = this.board.getPiece(mov.getStartPosition());
        ChessPiece at = this.board.getPiece(mov.getEndPosition());
        boolean isChecked = false;
        c = p.getTeamColor();

        this.board.addPiece(mov.getStartPosition(), null);
        this.board.addPiece(mov.getEndPosition(), p);
        if(isInCheck(c)) 
            isChecked = true;
        this.board.addPiece(mov.getEndPosition(), at);
        this.board.addPiece(mov.getStartPosition(), p);
        if(isChecked) return false;
        return true;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessGame.TeamColor c;
        ChessPiece p = this.board.getPiece(move.getStartPosition());
        c = p.getTeamColor();
        if(!validMoves(move.getStartPosition()).contains(move))
            throw new InvalidMoveException("Invalid move!");
        this.board.addPiece(move.getStartPosition(), null);
        this.board.addPiece(move.getEndPosition(), p);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition bucket;
        ChessPiece p;
        ArrayList<ChessMove> pMoves;
        ChessPosition myKingPosition;

        myKingPosition = teamColor == ChessGame.TeamColor.BLACK ? this.bKing : this.wKing;

        for(int i = 1; i <= 8; i++)
            for(int j = 1; j <= 8; j++) {
                bucket = new ChessPosition(i, j);
                p = this.board.getPiece(bucket);
                if(p == null)
                    continue;
                if(p.getTeamColor() == teamColor)
                    continue;
                pMoves = (ArrayList<ChessMove>) p.pieceMoves(this.board, bucket);
                for(ChessMove mov : pMoves) 
                    if(myKingPosition.equals(mov.getEndPosition()))
                        return true;
            }
        return false;
    }

    private ArrayList<ChessPiece> getTeamPieces(TeamColor teamColor) {
        ArrayList<ChessPiece> pieces = new ArrayList<ChessPiece>();
        for(int i = 0; i < 8; i++)
            for(int j = 0; j < 8; j++)
                if(board.getPiece(i, j).getTeamColor() == teamColor)
                    pieces.add(board.getPiece(i, j));
        return pieces;
    }

    private ChessPosition trans(int m, int n) {
        int row = 8 - m;
        int col = n + 1;
        return new ChessPosition(row, col);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        ChessPosition myKingPos;
        ArrayList<ChessPosition> criticalTiles = new ArrayList<ChessPosition>();
        int m;
        int n;

        myKingPos = teamColor == ChessGame.TeamColor.WHITE ? this.wKing : this.bKing;
        m = 8 - myKingPos.getRow();
        n = myKingPos.getColumn() - 1;
        criticalTiles.add(myKingPos);
        for(ChessMove mov : this.board.getPiece(myKingPos).pieceMoves(this.board, myKingPos))
            criticalTiles.add(mov.getEndPosition());
        

        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
        //TODO how do I restore turn from a loaded board?
        this.whiteTurn = true; 
        this.bKing = board.kingAt(ChessGame.TeamColor.BLACK);
        this.wKing = board.kingAt(ChessGame.TeamColor.WHITE);
        this.bCheck = isInCheck(ChessGame.TeamColor.BLACK);
        this.wCheck = isInCheck(ChessGame.TeamColor.WHITE);
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }
}
