package com.stephengware.java.games.chess.bot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.stephengware.java.games.chess.state.*;

public class Cawatso3 extends Bot {

    public Cawatso3() {
        super("Cawatso3 Bot");
    }

    @Override
    protected State chooseMove(State root) {

        Result bestMove = new Result(root,
                root.player == Player.WHITE ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
        List<Result> bestMoves = new ArrayList<>();

        for (int depth = 0; depth < 6; depth++) {
            bestMove = min_max_ab(root, root.player == Player.WHITE, depth);
            bestMoves.add(bestMove);

            if (root.searchLimitReached()) {
                break;
            }
        }

        bestMoves.sort(Comparator.comparingDouble(result -> result.value));

        System.out.println("bestMoves size: " + bestMoves.size());

        bestMove = bestMoves.get(bestMoves.size() - 1);
        System.out.println(bestMove.state);

        if (bestMove.state == null) {
            System.out.println("bestMove.state is null");
        }

        while (bestMove.state != null && bestMove.state.previous != root) {
            bestMove.state = bestMove.state.previous;
        }

        System.out.println("bestMove value: " + bestMove.value);

        return bestMove.state;
    }

    private Result min_max_ab(State state, boolean maximizingPlayer, int depth) {
        Result result = maximizingPlayer ? max_ab(state, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
                : min_ab(state, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        return result;
    }

    private ArrayList<State> gatherChildren(State state) {
        ArrayList<State> children = new ArrayList<>();

        for (Piece piece : state.board) {
            if (piece.player == state.player) {

                Iterator<State> iterator = state.next(piece).iterator();

                while (!state.searchLimitReached() && iterator.hasNext()) {
                    State move = iterator.next();
                    children.add(move);
                }
            }
        }

        return children;
    }

    private Result max_ab(State state, int depth, double alpha, double beta) {
        if (depth == 0 || state.countDescendants() == 0) {
            Result result = materialValue(state);
            if (result.value == Double.POSITIVE_INFINITY) {
                System.out.println("Infinity at depth " + depth + "in max_ab");
            }
            return result;
        }
        Result best = new Result(state, Double.NEGATIVE_INFINITY);

        ArrayList<State> children = gatherChildren(state);

        if (children.size() == 0) {
            return materialValue(state);
        }

        for (State child : children) {
            Result value = min_ab(child, depth - 1, alpha, beta);

            if (value.state.check) {
                value.value += 5;
            }

            if (value.state.check && value.state.over) {
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

    private Result min_ab(State state, int depth, double alpha, double beta) {
        if (depth == 0 || state.countDescendants() == 0) {
            if (materialValue(state).value == Double.POSITIVE_INFINITY) {
                System.out.println("Infinity at depth " + depth + "in min_ab");
            }
            return materialValue(state);
        }

        Result best = new Result(state, Double.POSITIVE_INFINITY);

        ArrayList<State> children = gatherChildren(state);

        if (children.size() == 0) {
            return materialValue(state);
        }

        for (State child : children) {
            Result value = max_ab(child, depth - 1, alpha, beta);

            if (value.value == Double.POSITIVE_INFINITY) {
                System.out.println("Infinity in the loop in min_ab");
            }

            if (value.state.check) {
                value.value -= 5;
            }

            if (value.state.check && value.state.over) {
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
        }
        if (value == Double.POSITIVE_INFINITY)
            System.out.println("Infinity");

        return new Result(state, value);
    }
}