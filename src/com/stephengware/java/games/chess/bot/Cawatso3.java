package com.stephengware.java.games.chess.bot;

import java.util.ArrayList;
import java.util.Iterator;
import com.stephengware.java.games.chess.state.*;

public class Cawatso3 extends Bot {

    public Cawatso3() {
        super("Cawatso3 Bot");
    }

    @Override
    protected State chooseMove(State root) {

        State bestMove = root;

        for (int depth = 2; depth <= 6; depth += 2) {
            bestMove = min_max_ab(root, root.player == Player.WHITE, depth).state;

            if (root.searchLimitReached()) {
                break;
            }
        }

        return bestMove;
    }

    private Result min_max_ab(State state, boolean maximizingPlayer, int depth) {
        return maximizingPlayer ? max_ab(state, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
                : min_ab(state, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    private Result max_ab(State state, int depth, double alpha, double beta) {
        if (depth == 0) {
            System.out.println("returning: " + state);
            System.out.println("materialValue: " + materialValue(state).value);
            return materialValue(state);
        }
        double best = Double.NEGATIVE_INFINITY;

        ArrayList<State> children = new ArrayList<>();
        Iterator<State> iterator = state.next().iterator();

        while (!state.searchLimitReached() && iterator.hasNext()) {
            State move = iterator.next();
            move.setSearchLimit(20);
            children.add(move);
        }

        for (State child : children) {
            Result value = min_ab(child, depth - 1, alpha, beta);
            best = Math.max(best, value.value);
            if (best >= beta) {
                return new Result(state, best);
            }
            alpha = Math.max(alpha, best);
        }

        return new Result(state, best);
    }

    private Result min_ab(State state, int depth, double alpha, double beta) {
        if (depth == 0) {
            System.out.println("returning: " + state);
            System.out.println("materialValue: " + materialValue(state).value);
            return materialValue(state);
        }

        double best = Double.POSITIVE_INFINITY;

        ArrayList<State> children = new ArrayList<>();
        Iterator<State> iterator = state.next().iterator();

        while (!state.searchLimitReached() && iterator.hasNext()) {
            State move = iterator.next();
            move.setSearchLimit(20);
            children.add(move);
        }

        for (State child : children) {
            Result value = max_ab(child, depth - 1, alpha, beta);
            best = Math.min(best, value.value);
            if (best <= alpha) {
                return new Result(state, best);
            }
            beta = Math.min(beta, best);
        }

        return new Result(state, best);
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
        }
        return new Result(state, value);
    }
}