package chess.piece;

import java.util.ArrayList;
import chess.*;

public class Pawn {
    public static void pawnMoves(int m, int n, ChessBoard board, ArrayList<ChessMove> moves, ChessPosition myPosition, ChessGame.TeamColor color) {
        ChessPiece bucket;

                if(color == ChessGame.TeamColor.BLACK) {
                    if(m == 1 && (board.getPiece(m + 1, n) == null && board.getPiece(m + 2, n) == null)) {     //BLACK ^ first move 
                        moves.add(new ChessMove(myPosition, trans(m + 2, n), null)); 
                    }
                    if(board.getPiece(m + 1, n) == null) {               //BLACK ^ down
                        if(m == 6) {
                            moves.add(new ChessMove(myPosition, trans(m + 1, n), ChessPiece.PieceType.ROOK));
                            moves.add(new ChessMove(myPosition, trans(m + 1, n), ChessPiece.PieceType.KNIGHT));
                            moves.add(new ChessMove(myPosition, trans(m + 1, n), ChessPiece.PieceType.BISHOP));
                            moves.add(new ChessMove(myPosition, trans(m + 1, n), ChessPiece.PieceType.QUEEN));
                        }
                        else {
                            moves.add(new ChessMove(myPosition, trans(m + 1, n), null));
                        }
                    }

                    if(m + 1 < 8 && n + 1 < 8) { 
                        bucket = board.getPiece(m + 1, n + 1);
                        if(bucket != null && bucket.getTeamColor() == ChessGame.TeamColor.WHITE) {   //BLACK ^ capture right
                            if(m == 6) {
                                moves.add(new ChessMove(myPosition, trans(m + 1, n + 1), ChessPiece.PieceType.ROOK));
                                moves.add(new ChessMove(myPosition, trans(m + 1, n + 1), ChessPiece.PieceType.KNIGHT));
                                moves.add(new ChessMove(myPosition, trans(m + 1, n + 1), ChessPiece.PieceType.BISHOP));
                                moves.add(new ChessMove(myPosition, trans(m + 1, n + 1), ChessPiece.PieceType.QUEEN));
                            }
                            else {
                                moves.add(new ChessMove(myPosition, trans(m + 1, n + 1), null));
                            }
                        }
                    }
                    if(m + 1 < 8 && n - 1 >=0) {
                        bucket = board.getPiece(m + 1, n - 1);
                        if(bucket != null && bucket.getTeamColor() == ChessGame.TeamColor.WHITE) {   //BLACK ^ capture left
                            if(m == 6) {
                                moves.add(new ChessMove(myPosition, trans(m + 1, n - 1), ChessPiece.PieceType.ROOK));
                                moves.add(new ChessMove(myPosition, trans(m + 1, n - 1), ChessPiece.PieceType.KNIGHT));
                                moves.add(new ChessMove(myPosition, trans(m + 1, n - 1), ChessPiece.PieceType.BISHOP));
                                moves.add(new ChessMove(myPosition, trans(m + 1, n - 1), ChessPiece.PieceType.QUEEN));
                            }
                            else {
                                moves.add(new ChessMove(myPosition, trans(m + 1, n - 1), null));
                            }
                        }
                    }
                }
                else {
                    if(board.getPiece(m - 1, n) == null) {               //WHITE ^ up
                        if(m == 1) {
                            moves.add(new ChessMove(myPosition, trans(m - 1, n), ChessPiece.PieceType.ROOK));
                            moves.add(new ChessMove(myPosition, trans(m - 1, n), ChessPiece.PieceType.KNIGHT));
                            moves.add(new ChessMove(myPosition, trans(m - 1, n), ChessPiece.PieceType.BISHOP));
                            moves.add(new ChessMove(myPosition, trans(m - 1, n), ChessPiece.PieceType.QUEEN));
                        }
                        else {
                            moves.add(new ChessMove(myPosition, trans(m - 1, n), null));
                        }
                        if(m == 6 && (board.getPiece(m - 2, n) == null)) {                  //White First Move
                            moves.add(new ChessMove(myPosition, trans(m - 2, n), null));
                        }
                    }
                    if(m - 1 >= 0 && n + 1 < 8) {
                        bucket = board.getPiece(m - 1, n + 1);
                        if(bucket != null && bucket.getTeamColor() == ChessGame.TeamColor.BLACK) {   //WHITE ^ capture right
                            if(m == 1) {
                                moves.add(new ChessMove(myPosition, trans(m - 1, n + 1), ChessPiece.PieceType.ROOK));
                                moves.add(new ChessMove(myPosition, trans(m - 1, n + 1), ChessPiece.PieceType.KNIGHT));
                                moves.add(new ChessMove(myPosition, trans(m - 1, n + 1), ChessPiece.PieceType.BISHOP));
                                moves.add(new ChessMove(myPosition, trans(m - 1, n + 1), ChessPiece.PieceType.QUEEN));
                            }
                            moves.add(new ChessMove(myPosition, trans(m - 1, n + 1), null));
                        }
                    }

                    if(m - 1 >= 0 && n - 1 >= 0) {
                        bucket = board.getPiece(m - 1, n - 1);
                        if(bucket != null && bucket.getTeamColor() == ChessGame.TeamColor.BLACK) {   //WHITE ^ capture left
                            if(m == 1) {
                                moves.add(new ChessMove(myPosition, trans(m - 1, n - 1), ChessPiece.PieceType.ROOK));
                                moves.add(new ChessMove(myPosition, trans(m - 1, n - 1), ChessPiece.PieceType.KNIGHT));
                                moves.add(new ChessMove(myPosition, trans(m - 1, n - 1), ChessPiece.PieceType.BISHOP));
                                moves.add(new ChessMove(myPosition, trans(m - 1, n - 1), ChessPiece.PieceType.QUEEN));
                            }
                            else {
                                moves.add(new ChessMove(myPosition, trans(m - 1, n - 1), null));
                            }
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
