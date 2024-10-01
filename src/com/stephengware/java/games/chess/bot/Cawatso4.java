package com.stephengware.java.games.chess.bot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.stephengware.java.games.chess.state.*;
import com.stephengware.java.games.chess.bot.Result;

public class Cawatso4 extends Bot {

    public Cawatso4() {
        super("Cawatso4 Bot");
    }

    private HashMap<Long, State> visitedStates = new HashMap<>();

    @Override
    protected State chooseMove(State root) {
        visitedStates.clear();

        boolean maximizingPlayer = root.player == Player.WHITE;

        List<Result> results = new ArrayList<>();

        for (int depth = 0; depth < 3; depth++) {
            if (root.searchLimitReached()) {
                break;
            }

            Result result = min_max_ab(root, depth);
            results.add(result);
        }

        results.sort(Comparator.comparingDouble(result -> result.value));

        int position = 1;

        if (maximizingPlayer) {
            return results.get(results.size() - position).state;
        } else {
            return results.get(position - 1).state;
        }
    }

    private boolean detectCheckmate(State state) {
        if (state.over) {
            if (state.check) {
                return true;
            }
        }
        return false;
    }

    private Result min_max_ab(State state, int depth) {
        if (state.player == Player.WHITE) {
            return max_ab(state, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        } else {
            return min_ab(state, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        }
    }

    private Result max_ab(State state, int depth, double alpha, double beta) {
        if (depth == 0) {
            return new Result(state, evaluateState(state));
        }

        if (detectCheckmate(state)) {
            if (state.player == Player.WHITE) {
                return new Result(state, Double.NEGATIVE_INFINITY);
            } else {
                return new Result(state, Double.POSITIVE_INFINITY);
            }
        }

        Result best = new Result(state, Double.NEGATIVE_INFINITY);

        for (State child : gatherChildren(state)) {
            Result result = min_ab(child, depth - 1, alpha, beta);
            best.value = Math.max(best.value, result.value);

            if (best.value == result.value) {
                best.state = result.state;
            }

            alpha = Math.max(alpha, best.value);
            if (beta <= alpha) {
                break;
            }
        }
        return best;
    }

    private Result min_ab(State state, int depth, double alpha, double beta) {
        if (depth == 0) {
            return new Result(state, evaluateState(state));
        }

        if (detectCheckmate(state)) {
            if (state.player == Player.WHITE) {
                return new Result(state, Double.NEGATIVE_INFINITY);
            } else {
                return new Result(state, Double.POSITIVE_INFINITY);
            }
        }

        Result best = new Result(state, Double.POSITIVE_INFINITY);

        for (State child : gatherChildren(state)) {
            Result result = max_ab(child, depth - 1, alpha, beta);
            best.value = Math.min(best.value, result.value);

            if (best.value == result.value) {
                best.state = result.state;
            }

            beta = Math.min(beta, best.value);
            if (beta <= alpha) {
                break;
            }
        }
        return best;
    }

    private double evaluateState(State state) {
        double value = 0;

        for (Piece piece : state.board) {
            value += rawMaterialValue(piece);
        }

        return value;
    }

    private double rawMaterialValue(Piece piece) {
        double value = 0;

        boolean maximizingPlayer = piece.player == Player.WHITE;

        if (piece.getClass() == Pawn.class) {
            value += 100;
        }
        if (piece.getClass() == Knight.class) {
            value += 320;
        }
        if (piece.getClass() == Bishop.class) {
            value += 330;
        }
        if (piece.getClass() == Rook.class) {
            value += 500;
        }
        if (piece.getClass() == Queen.class) {
            value += 900;
        }
        if (piece.getClass() == King.class) {
            value += 20000;
        }

        if (!maximizingPlayer) {
            value *= -1;
        }

        return value;
    }

    private ArrayList<State> gatherChildren(State state) {
        ArrayList<State> children = new ArrayList<>();
        Iterator<State> iterator = state.next().iterator();

        while (iterator.hasNext() && !state.searchLimitReached()) {
            State child = iterator.next();
            long hash = child.hashCode();

            if (!visitedStates.containsKey(hash)) {
                visitedStates.put(hash, child);
                children.add(child);
            }
        }
        return children;
    }

}