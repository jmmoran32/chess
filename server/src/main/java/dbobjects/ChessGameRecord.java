package dbobjects;

import chess.ChessGame;

public record ChessGameRecord(String gameID, String whiteUsername, String blackUsername, ChessGame game) {}
