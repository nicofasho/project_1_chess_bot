package com.stephengware.java.games.chess.bot;

import java.util.ArrayList;
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

        State bestMove = root;

        for (int depth = 0; depth <= 2; depth++) {
            bestMove = min_max_ab(root, root.player == Player.WHITE, depth).state;

            if (root.searchLimitReached()) {
                break;
            }
        }

        while (bestMove.previous != root) {
            bestMove = bestMove.previous;
        }

        return bestMove;
    }

    private Result min_max_ab(State state, boolean maximizingPlayer, int depth) {
        return maximizingPlayer ? max_ab(state, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
                : min_ab(state, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    private ArrayList<State> gatherChildren(State state) {
        ArrayList<State> children = new ArrayList<>();

        for (Piece piece : state.board) {
            if (piece.player == state.player) {

                Iterator<State> iterator = state.next(piece).iterator();
                int counter = 0;

                while (!state.searchLimitReached() && iterator.hasNext() && counter < 20) {
                    State move = iterator.next();
                    children.add(move);
                    counter++;
                }
            }
        }

        return children;
    }

    private Result max_ab(State state, int depth, double alpha, double beta) {
        if (depth == 0) {
            System.out.println("returning: " + state);
            System.out.println("materialValue: " + materialValue(state).value);
            return materialValue(state);
        }
        double best = Double.NEGATIVE_INFINITY;

        ArrayList<State> children = gatherChildren(state);
        List<Result> bestResults = new ArrayList<>();

        if (children.isEmpty()) {
            return materialValue(state);
        }

        for (State child : children) {
            Result value = min_ab(child, depth - 1, alpha, beta);

            if (value.state.check) {
                value.value += 100;
            }

            if (value.state.check && value.state.over) {
                value.value += 1000;
            }

            if (value.value > best) {
                best = value.value;
                bestResults.clear();
                bestResults.add(value);
            } else if (value.value == best) {
                bestResults.add(value);
            }

            if (best >= beta) {
                System.out.println("pruning in max_ab");
                break;
            }
            alpha = Math.max(alpha, best);
        }

        return bestResults.get(new Random().nextInt(bestResults.size()));
    }

    private Result min_ab(State state, int depth, double alpha, double beta) {
        if (depth == 0) {
            System.out.println("returning: " + state);
            System.out.println("materialValue: " + materialValue(state).value);
            return materialValue(state);
        }

        double best = Double.POSITIVE_INFINITY;

        ArrayList<State> children = gatherChildren(state);
        List<Result> bestResults = new ArrayList<>();

        if (children.isEmpty()) {
            return materialValue(state);
        }

        for (State child : children) {
            Result value = max_ab(child, depth - 1, alpha, beta);

            if (value.state.check) {
                value.value -= 100;
            }
            
            if (value.state.check && value.state.over) {
                value.value -= 1000;
            }

            if (value.value < best) {
                best = value.value;
                bestResults.clear();
                bestResults.add(value);
            } else if (value.value == best) {
                bestResults.add(value);
            }

            if (best <= alpha) {
                System.out.println("pruning in min_ab");
                break;
            }

            beta = Math.min(beta, best);
        }

        return bestResults.get(new Random().nextInt(bestResults.size()));
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