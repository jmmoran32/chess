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
        Collection<ChessMove> moves = new ArrayList<ChessMove>();
        ChessPiece p = board.getPiece(startPosition);
        moves = p.pieceMoves(this.board, startPosition);
        //ChessRule.filter(moves);
        return moves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
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

    private boolean isPositionChecked(ChessPosition position) {}

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if(!isInCheck(teamColor)) return false;

        ChessPiece pk;
        ArrayList<ChessMove> pMoves;
        ArrayList<ChessMove> kMoves;
        ChessPosition myKingPosition;
        ArrayList<ChessPiece> enemyTeam;
        ArrayList<ChessPiece> checkingPieces;

        myKingPosition = teamColor == ChessGame.TeamColor.BLACK ? this.bKing : this.wKing;
        pk = board.getPiece(myKingPosition);
        kMoves = (ArrayList<ChessMove>) pk.pieceMoves(this.board, myKingPosition);

        

        for(ChessMove mov : kMoves) {
            if(!isPositionChecked(mov.getEndPosition()))
                return false;
            
        }

        return true;
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
