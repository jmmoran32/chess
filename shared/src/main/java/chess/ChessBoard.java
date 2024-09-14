package chess;

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

    public ChessBoard() {
        this.board = new ChessPiece[8][8];
        this.whiteBox = new ChessPiece[32];
        this.blackBox = new ChessPiece[32];
        wbn = 0;
        bbn = 0;
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        this.board[position.getRow()][position.getColumn()] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return this.board[position.getRow()][position.getColumn()];
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
        int m = position.getRow();
        int n = position.getColumn();
        if(this.board[m][n] == null) {
            System.out.println(String.format("This position (%d:%d) is already empty!", position.getRow(), position.getColumn()));
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
}
