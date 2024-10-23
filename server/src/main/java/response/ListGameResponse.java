package response;

import java.util.ArrayList;
import dbobjects.*;

public record ListGameResponse(ArrayList<ChessGameRecord> games) {}
