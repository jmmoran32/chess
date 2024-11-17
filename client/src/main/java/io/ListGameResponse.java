package io;

import java.util.ArrayList;
import facade.ChessGameRecord;

public record ListGameResponse(ArrayList<ChessGameRecord> games) {}
