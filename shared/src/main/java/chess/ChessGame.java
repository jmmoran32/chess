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
    private boolean resigned = false;
    private static int iterator = 1;
    private int obid;
    private static final int CLID = 3;

    public ChessGame() {
        this.obid = iterator++;
        this.whiteTurn = true;
        this.board = new ChessBoard();
        this.board.resetBoard();
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        int whiteTurn;
        if(this.whiteTurn) {
            whiteTurn = 1;
        }
        else {
            whiteTurn = 0;
        }
        if(this.resigned) {
            whiteTurn += 2;
        }
        sb.append(String.format("%d{", whiteTurn));
        sb.append(String.format("%s}", this.board.serialize()));
        return sb.toString();
    }

    public static ChessGame deSerialize(String serial) {
        ChessGame game = new ChessGame();
        char whiteTurn = serial.charAt(0);
        String boardOnly = serial.substring(2, 66);
        game.setBoard(ChessBoard.deSerialize(boardOnly));
        if(whiteTurn > '1') {
            game.resign();
            whiteTurn -= 2;
        }
        if(whiteTurn == '0') {
            game.setTeamTurn(ChessGame.TeamColor.BLACK);
        }
        return game;
    }

    public void resign() {
        this.resigned = true;
    }

    public boolean isResigned() {
        return this.resigned;
    }

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
        ChessMove m;
        ArrayList<ChessMove> validMoves = new ArrayList<ChessMove>();
        for(int i = 0; i < moves.size(); i++) {
           m = moves.get(i);
           if(!testMove(m)) {
               continue;
           }
           else {
               validMoves.add(m);
           }
        }
        return validMoves;
    }

    public boolean willPromote(ChessMove mov) {
        for(ChessMove m : validMoves(mov.getStartPosition())) {
            if(m.getPromotionPiece() != null) {
                if(mov.getEndPosition().equals(m.getEndPosition())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean testMove(ChessMove mov) {
        ChessGame.TeamColor c;
        ChessPiece p = this.board.getPiece(mov.getStartPosition());
        ChessPiece at = this.board.getPiece(mov.getEndPosition());
        boolean isChecked = false;
        c = p.getTeamColor();

        this.board.addPiece(mov.getStartPosition(), null);
        this.board.addPiece(mov.getEndPosition(), p);
        if(isInCheck(c)) { 
            isChecked = true;
        }
        this.board.addPiece(mov.getEndPosition(), at);
        this.board.addPiece(mov.getStartPosition(), p);
        if(isChecked) {return false;}
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
        if(p == null) {
            throw new InvalidMoveException(String.format("%s does not contain a piece!", move.getStartPosition()));
        }
        c = p.getTeamColor();
        if(this.whiteTurn && c != ChessGame.TeamColor.WHITE) {
            throw new InvalidMoveException(String.format("The piece at %s Is not part of the team whose turn it is now!", move.toString()));
        }
        if(!this.whiteTurn && c == ChessGame.TeamColor.WHITE) {
            throw new InvalidMoveException(String.format("The piece at %s Is not part of the team whose turn it is now!", move.toString()));
        }
        ArrayList<ChessMove> validMoves = (ArrayList<ChessMove>) validMoves(move.getStartPosition());
        if(!validMoves.contains(move)) {
            throw new InvalidMoveException(String.format(
                        "%s is not a valid move for %s. Valid moves: %s", move.getEndPosition(), move.getStartPosition(), validMoves.toString()));
        }
        this.board.addPiece(move.getStartPosition(), null);
        if(move.getPromotionPiece() != null) {
            ChessPiece promotee = new ChessPiece(c, move.getPromotionPiece()); 
            this.board.addPiece(move.getEndPosition(), promotee);
        }
        else {
            this.board.addPiece(move.getEndPosition(), p);
        }
        this.whiteTurn ^= true;
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

        myKingPosition = 
            teamColor == ChessGame.TeamColor.BLACK ? this.board.kingAt(ChessGame.TeamColor.BLACK) : this.board.kingAt(ChessGame.TeamColor.WHITE);

        if(myKingPosition == null) {
            return false;
        }
        for(int i = 1; i <= 8; i++) {
            for(int j = 1; j <= 8; j++) {
                bucket = new ChessPosition(i, j);
                p = this.board.getPiece(bucket);
                if(p == null) {
                    continue;
                }
                if(p.getTeamColor() == teamColor) {
                    continue;
                }
                pMoves = (ArrayList<ChessMove>) p.pieceMoves(this.board, bucket);
                for(ChessMove mov : pMoves) {
                    if(myKingPosition.equals(mov.getEndPosition())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private ArrayList<ChessPosition> getTeamPositions(TeamColor teamColor) {
        ArrayList<ChessPosition> pieces = new ArrayList<ChessPosition>();
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                if(board.getPiece(i, j) == null) {
                    continue;
                }
                if(board.getPiece(i, j).getTeamColor() == teamColor) {
                    pieces.add(trans(i, j));
                }
            }
        }
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
        if(!isInCheck(teamColor)) {return false;}
        for(ChessPosition p : getTeamPositions(teamColor)) {
            if(validMoves(p).size() > 0) {
                return false;
            }
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
        if(isInCheckmate(teamColor)) {
            return false;
        }
        ArrayList<ChessPosition> teamPositions = getTeamPositions(teamColor);
        ArrayList<ChessMove> validMoves;
        if(teamPositions.size() == 0) {
            return true;
        }
        for(ChessPosition p : teamPositions) {
            validMoves = (ArrayList<ChessMove>) validMoves(p); 
            if(validMoves.size() > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
        this.whiteTurn = true; 
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
