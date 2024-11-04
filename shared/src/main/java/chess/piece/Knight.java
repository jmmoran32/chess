package chess.piece;

import java.util.ArrayList;
import chess.*;

public class Knight {
    public static void knightMoves(int m, int n, ChessBoard board, ArrayList<ChessMove> moves, ChessPosition myPosition, ChessGame.TeamColor color) {
        ChessPiece bucket;

                if(m - 2 >= 0) {         //up
                    if(n + 1 < 8) {     //up-right
                        bucket = board.getPiece(m - 2, n + 1);
                        if(bucket == null || bucket.getTeamColor() != color) {
                            moves.add(new ChessMove(myPosition, trans(m - 2, n + 1), null));
                        }
                    }
                    if(n - 1 >= 0) {    //up-left
                        bucket = board.getPiece(m - 2, n - 1);
                        if(bucket == null || bucket.getTeamColor() != color) {
                            moves.add(new ChessMove(myPosition, trans(m - 2, n - 1), null));
                        }
                    }
                }
                if(n + 2 < 8) {         //right
                    if(m - 1 >= 0) {     //right-up
                        bucket = board.getPiece(m - 1, n + 2);
                        if(bucket == null || bucket.getTeamColor() != color) {
                            moves.add(new ChessMove(myPosition, trans(m - 1, n + 2), null));
                        }
                    }
                    if(m + 1 < 8) {    //right-down
                        bucket = board.getPiece(m + 1, n + 2);
                        if(bucket == null || bucket.getTeamColor() != color) {
                            moves.add(new ChessMove(myPosition, trans(m + 1, n + 2), null));
                        }
                    }
                }
                if(m + 2 < 8) {         //down
                    if(n + 1 < 8) {     //down-right
                        bucket = board.getPiece(m + 2, n + 1);
                        if(bucket == null || bucket.getTeamColor() != color) {
                            moves.add(new ChessMove(myPosition, trans(m + 2, n + 1), null));
                        }
                    }
                    if(n - 1 >= 0) {    //down-left
                        bucket = board.getPiece(m + 2, n - 1);
                        if(bucket == null || bucket.getTeamColor() != color) {
                            moves.add(new ChessMove(myPosition, trans(m + 2, n - 1), null));
                        }
                    }
                }
                if(n - 2 >= 0) {         //left
                    if(m - 1 >= 0) {     //left-up
                        bucket = board.getPiece(m - 1, n - 2);
                        if(bucket == null || bucket.getTeamColor() != color) {
                            moves.add(new ChessMove(myPosition, trans(m - 1, n - 2), null));
                        }
                    }
                    if(m + 1 < 8) {    //left-down
                        bucket = board.getPiece(m + 1, n - 2);
                        if(bucket == null || bucket.getTeamColor() != color) {
                            moves.add(new ChessMove(myPosition, trans(m + 1, n - 2), null));
                        }
                    }
                }
    }

    private static ChessPosition trans(int m, int n) {
        int row = 8 - m;
        int col = n + 1;
        return new ChessPosition(row, col);
    }

}
