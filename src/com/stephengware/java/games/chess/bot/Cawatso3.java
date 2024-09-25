package com.stephengware.java.games.chess.bot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.stephengware.java.games.chess.state.*;
import com.stephengware.java.games.chess.bot.Result;

public class Cawatso3 extends Bot {

    public Cawatso3() {
        super("Cawatso3 Bot");
    }

    private enum SearchType {
        QUIESCENCE, MINMAX
    }

    private HashMap<Long, State> visitedStates = new HashMap<>();

    @Override
    protected State chooseMove(State root) {

        boolean maximizingPlayer = root.player == Player.WHITE;

        Result bestMove = new Result(root,
                maximizingPlayer ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
        List<Result> bestMoves = new ArrayList<>();

        for (int depth = 0; depth < 6; depth++) {

            bestMove = min_max_ab(root, maximizingPlayer, depth, SearchType.QUIESCENCE);
            bestMoves.add(bestMove);

            bestMove = min_max_ab(root, root.player == Player.WHITE, depth, SearchType.MINMAX);
            bestMoves.add(bestMove);

            if (root.searchLimitReached()) {
                break;
            }
        }

        bestMoves.sort(Comparator.comparingDouble(result -> result.value));

        int positionCounter = 1;

        while (bestMove.state == root) {
            positionCounter++;
            bestMove = bestMoves.get(maximizingPlayer ? bestMoves.size() - positionCounter : positionCounter - 1);
        }

        System.out.println("Looping back to root");
        while (bestMove.state.previous != root) {

            if (bestMove.state == root) {
                break;
            }

            bestMove.state = bestMove.state.previous;
        }

        System.out.println("bestMove value: " + bestMove.value);
        System.out.println("Best move: " + bestMove.state);

        return bestMove.state;
    }

    private boolean addIfThreaten(State state, ArrayList<State> children, Piece piece, int file, int rank,
            Player opponent) {
        // Check if the file and rank are within the bounds of the chessboard
        try {
            if (file >= 0 && file < 8 && rank >= 0 && rank < 8) {
                if (state.board.pieceAt(file, rank, opponent)) {
                    long hash = state.hashCode();
                    if (!visitedStates.containsKey(hash)) {
                        children.add(state.next(piece, piece.move(file, rank)));
                        visitedStates.put(hash, state);
                        return true;
                    }

                } else if (state.board.pieceAt(file, rank)) {
                    return true;
                }
            }
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private ArrayList<State> gatherQuiescentChildren(State state) {
        ArrayList<State> children = new ArrayList<>();
        for (Piece piece : state.board) {

            boolean maximizingPlayer = piece.player == Player.WHITE;

            if (piece.getClass() == Pawn.class) {
                if (maximizingPlayer) {
                    addIfThreaten(state, children, piece, piece.file + 1, piece.rank + 1, Player.BLACK);
                    addIfThreaten(state, children, piece, piece.file + 1, piece.rank - 1, Player.BLACK);
                } else {
                    addIfThreaten(state, children, piece, piece.file - 1, piece.rank - 1, Player.WHITE);
                    addIfThreaten(state, children, piece, piece.file - 1, piece.rank + 1, Player.WHITE);
                }
            }
            if (piece.getClass() == Knight.class) {
                if (maximizingPlayer) {
                    addIfThreaten(state, children, piece, piece.file + 2, piece.rank + 1, Player.BLACK);
                    addIfThreaten(state, children, piece, piece.file + 2, piece.rank - 1, Player.BLACK);
                    addIfThreaten(state, children, piece, piece.file - 2, piece.rank + 1, Player.BLACK);
                    addIfThreaten(state, children, piece, piece.file - 2, piece.rank - 1, Player.BLACK);
                    addIfThreaten(state, children, piece, piece.file + 1, piece.rank + 2, Player.BLACK);
                    addIfThreaten(state, children, piece, piece.file + 1, piece.rank - 2, Player.BLACK);
                    addIfThreaten(state, children, piece, piece.file - 1, piece.rank + 2, Player.BLACK);
                    addIfThreaten(state, children, piece, piece.file - 1, piece.rank - 2, Player.BLACK);
                } else {
                    addIfThreaten(state, children, piece, piece.file + 2, piece.rank + 1, Player.WHITE);
                    addIfThreaten(state, children, piece, piece.file + 2, piece.rank - 1, Player.WHITE);
                    addIfThreaten(state, children, piece, piece.file - 2, piece.rank + 1, Player.WHITE);
                    addIfThreaten(state, children, piece, piece.file - 2, piece.rank - 1, Player.WHITE);
                    addIfThreaten(state, children, piece, piece.file + 1, piece.rank + 2, Player.WHITE);
                    addIfThreaten(state, children, piece, piece.file + 1, piece.rank - 2, Player.WHITE);
                    addIfThreaten(state, children, piece, piece.file - 1, piece.rank + 2, Player.WHITE);
                    addIfThreaten(state, children, piece, piece.file - 1, piece.rank - 2, Player.WHITE);
                }
            }
            if (piece.getClass() == Bishop.class) {
                if (maximizingPlayer) {
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file + i, piece.rank + i, Player.BLACK)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file + i, piece.rank - i, Player.BLACK)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file - i, piece.rank + i, Player.BLACK)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file - i, piece.rank - i, Player.BLACK)) {
                            break;
                        }
                    }
                } else {
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file + i, piece.rank + i, Player.WHITE)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file + i, piece.rank - i, Player.WHITE)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file - i, piece.rank + i, Player.WHITE)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file - i, piece.rank - i, Player.WHITE)) {
                            break;
                        }
                    }
                }
            }

            if (piece.getClass() == Rook.class) {
                if (maximizingPlayer) {
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file + i, piece.rank, Player.BLACK)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file - i, piece.rank, Player.BLACK)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file, piece.rank + i, Player.BLACK)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file, piece.rank - i, Player.BLACK)) {
                            break;
                        }
                    }
                } else {
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file + i, piece.rank, Player.WHITE)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file - i, piece.rank, Player.WHITE)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file, piece.rank + i, Player.WHITE)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file, piece.rank - i, Player.WHITE)) {
                            break;
                        }
                    }
                }
            }

            if (piece.getClass() == Queen.class) {
                if (maximizingPlayer) {
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file + i, piece.rank + i, Player.BLACK)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file + i, piece.rank - i, Player.BLACK)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file - i, piece.rank + i, Player.BLACK)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file - i, piece.rank - i, Player.BLACK)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file + i, piece.rank, Player.BLACK)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file - i, piece.rank, Player.BLACK)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file, piece.rank + i, Player.BLACK)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file, piece.rank - i, Player.BLACK)) {
                            break;
                        }
                    }
                } else {
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file + i, piece.rank + i, Player.WHITE)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file + i, piece.rank - i, Player.WHITE)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file - i, piece.rank + i, Player.WHITE)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file - i, piece.rank - i, Player.WHITE)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file + i, piece.rank, Player.WHITE)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file - i, piece.rank, Player.WHITE)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file, piece.rank + i, Player.WHITE)) {
                            break;
                        }
                    }
                    for (int i = 1; i < 8; i++) {
                        if (addIfThreaten(state, children, piece, piece.file, piece.rank - i, Player.WHITE)) {
                            break;
                        }
                    }
                }
            }
            if (piece.getClass() == King.class) {
                if (maximizingPlayer) {
                    addIfThreaten(state, children, piece, piece.file + 1, piece.rank + 1, Player.BLACK);
                    addIfThreaten(state, children, piece, piece.file + 1, piece.rank - 1, Player.BLACK);
                    addIfThreaten(state, children, piece, piece.file - 1, piece.rank + 1, Player.BLACK);
                    addIfThreaten(state, children, piece, piece.file - 1, piece.rank - 1, Player.BLACK);
                    addIfThreaten(state, children, piece, piece.file + 1, piece.rank, Player.BLACK);
                    addIfThreaten(state, children, piece, piece.file - 1, piece.rank, Player.BLACK);
                    addIfThreaten(state, children, piece, piece.file, piece.rank + 1, Player.BLACK);
                    addIfThreaten(state, children, piece, piece.file, piece.rank - 1, Player.BLACK);
                } else {
                    addIfThreaten(state, children, piece, piece.file + 1, piece.rank + 1, Player.WHITE);
                    addIfThreaten(state, children, piece, piece.file + 1, piece.rank - 1, Player.WHITE);
                    addIfThreaten(state, children, piece, piece.file - 1, piece.rank + 1, Player.WHITE);
                    addIfThreaten(state, children, piece, piece.file - 1, piece.rank - 1, Player.WHITE);
                    addIfThreaten(state, children, piece, piece.file + 1, piece.rank, Player.WHITE);
                    addIfThreaten(state, children, piece, piece.file - 1, piece.rank, Player.WHITE);
                    addIfThreaten(state, children, piece, piece.file, piece.rank + 1, Player.WHITE);
                    addIfThreaten(state, children, piece, piece.file, piece.rank - 1, Player.WHITE);
                }
            }
        }

        return children;
    }

    private Result min_max_ab(State state, boolean maximizingPlayer, int depth, SearchType searchType) {
        Result result = maximizingPlayer
                ? max_ab(state, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, searchType)
                : min_ab(state, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, searchType);

        return result;
    }

    private ArrayList<State> gatherChildren(State state) {
        ArrayList<State> children = new ArrayList<>();

        Iterator<State> iterator = state.next().iterator();

        while (!state.searchLimitReached() && iterator.hasNext()) {
            State move = iterator.next();
            long hash = move.hashCode();

            if (!visitedStates.containsKey(hash)) {
                visitedStates.put(hash, move);
                children.add(move);
            }
        }

        return children;
    }

    private Result max_ab(State state, int depth, double alpha, double beta, SearchType searchType) {
        if (depth == 0 || state.countDescendants() == 0) {
            Result result = materialValue(state);
            if (result.value == Double.POSITIVE_INFINITY) {
                System.out.println("Infinity at depth " + depth + "in max_ab");
            }
            return result;
        }
        Result best = new Result(state, Double.NEGATIVE_INFINITY);

        ArrayList<State> children;

        if (searchType == SearchType.MINMAX) {
            children = gatherChildren(state);
        } else {
            children = gatherQuiescentChildren(state);
        }

        if (children.size() == 0) {
            return materialValue(state);
        }

        for (State child : children) {
            Result value = min_ab(child, depth - 1, alpha, beta, searchType);

            if (value.state.check && value.state.previous == null) {
                value.value += 500;
            }

            if (value.state.check && value.state.over && value.state.previous == null) {
                value.value += 1000;
            }

            if (value.value > best.value) {
                best = value;
            }

            if (best.value >= beta) {
                return best;
            }
            alpha = Math.max(alpha, best.value);
        }
        if (best.value == Double.POSITIVE_INFINITY) {
            System.out.println("Infinity at the bottom of max_ab");
        }
        return best;
    }

    private Result min_ab(State state, int depth, double alpha, double beta, SearchType searchType) {
        if (depth == 0 || state.countDescendants() == 0) {
            if (materialValue(state).value == Double.POSITIVE_INFINITY) {
                System.out.println("Infinity at depth " + depth + "in min_ab");
            }
            return materialValue(state);
        }

        Result best = new Result(state, Double.POSITIVE_INFINITY);

        ArrayList<State> children;

        if (searchType == SearchType.MINMAX) {
            children = gatherChildren(state);
        } else {
            children = gatherQuiescentChildren(state);
        }
        if (children.size() == 0) {
            return materialValue(state);
        }

        for (State child : children) {
            Result value = max_ab(child, depth - 1, alpha, beta, searchType);

            if (value.value == Double.POSITIVE_INFINITY) {
                System.out.println("Infinity in the loop in min_ab");
            }

            if (value.state.check && value.state.previous == null) {
                value.value -= 500;
            }

            if (value.state.check && value.state.over && value.state.previous == null) {
                value.value -= 1000;
            }

            if (value.value < best.value) {
                best = value;
            }

            if (best.value <= alpha) {
                return best;
            }

            beta = Math.min(beta, best.value);
        }

        if (best.value == Double.POSITIVE_INFINITY) {
            System.out.println("Infinity at the bottom of min_ab");
        }
        return best;
    }

    private Result materialValue(State state) {
        double value = 0;
        for (Piece piece : state.board) {
            boolean maximizingPlayer = piece.player == Player.WHITE;

            if (piece.getClass() == Pawn.class) {
                value += maximizingPlayer ? 1 : -1;
            }
            if (piece.getClass() == Knight.class) {
                value += maximizingPlayer ? 3 : -3;
            }
            if (piece.getClass() == Bishop.class) {
                value += maximizingPlayer ? 3 : -3;
            }
            if (piece.getClass() == Rook.class) {
                value += maximizingPlayer ? 5 : -5;
            }
            if (piece.getClass() == Queen.class) {
                value += maximizingPlayer ? 9 : -9;
            }
            if (piece.getClass() == King.class) {
                value += maximizingPlayer ? 100 : -100;
            }

            // if (piece.rank > 2 && piece.rank < 5) {
            // if (piece.file > 2 && piece.file < 5) {
            // value *= maximizingPlayer ? 1.1 : -1.1;
            // }
            // }
        }

        return new Result(state, value);
    }
}