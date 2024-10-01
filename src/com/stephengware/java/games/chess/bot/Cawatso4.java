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

        System.out.println("printing results list");
        for (Result result : results) {
            System.out.println(result.state);
            System.out.println(result.value);
        }

        int position = 1;
        Result bestMove = results.get(results.size() - position);

        if (maximizingPlayer) {
            bestMove = results.get(results.size() - position);
        } else {
            bestMove = results.get(position - 1);
        }

        try {
            while (bestMove.state.previous != root) {
                bestMove.state = bestMove.state.previous;
            }
        } catch (NullPointerException e) {
            position++;
            if (maximizingPlayer) {
                bestMove = results.get(results.size() - position);
            } else {
                bestMove = results.get(position - 1);
            }
        }

        return bestMove.state;
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
            return new Result(state, Double.NEGATIVE_INFINITY);
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
            return new Result(state, Double.POSITIVE_INFINITY);
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
        // build the list manually from possible moves of every
        // one of our pieces on the board

        ArrayList<State> children = new ArrayList<>();

        for (Piece piece : state.board) {
            if (piece.player == state.player) {
                // build possible moves of said piece based on rules of its movement
                if (piece.getClass() == Pawn.class) {
                    int[][] pawnMoves = generatePawnMoves(piece);
                    for (int[] move : pawnMoves) {
                        int x = piece.rank + move[0];
                        int y = piece.file + move[1];

                        Pawn newPawn = new Pawn(piece.player, y, x);
                        if (isMoveLegal(state, piece, newPawn)) {
                            children.add(state.next(piece, newPawn));
                        }
                    }
                }
                if (piece.getClass() == Knight.class) {
                    int[][] knightMoves = generateKnightMoves();

                    for (int[] move : knightMoves) {
                        int x = piece.rank + move[0];
                        int y = piece.file + move[1];

                        Knight newKnight = new Knight(piece.player, y, x);
                        if (isMoveLegal(state, piece, newKnight)) {
                            children.add(state.next(piece, newKnight));
                        }
                    }
                }
                if (piece.getClass() == Bishop.class) {
                    int[][] bishopMoves = generateBishopMoves();

                    for (int i = 1; i < 8; i++) {
                        for (int[] move : bishopMoves) {
                            int x = piece.rank + move[0] * i;
                            int y = piece.file + move[1] * i;

                            Bishop newBishop = new Bishop(piece.player, y, x);
                            if (isMoveLegal(state, piece, newBishop)) {
                                children.add(state.next(piece, newBishop));
                            }
                        }
                    }
                }
                if (piece.getClass() == Rook.class) {
                    int[][] rookMoves = generateRookMoves();

                    for (int i = 1; i < 8; i++) {
                        for (int[] move : rookMoves) {
                            int x = piece.rank + move[0] * i;
                            int y = piece.file + move[1] * i;

                            Rook newRook = new Rook(piece.player, y, x);
                            if (isMoveLegal(state, piece, newRook)) {
                                children.add(state.next(piece, newRook));
                            }
                        }
                    }
                }
                if (piece.getClass() == Queen.class) {
                    int[][] queenMoves = generateQueenMoves();

                    for (int i = 1; i < 8; i++) {
                        for (int[] move : queenMoves) {
                            int x = piece.rank + move[0] * i;
                            int y = piece.file + move[1] * i;

                            Queen newQueen = new Queen(piece.player, y, x);
                            if (isMoveLegal(state, piece, newQueen)) {
                                children.add(state.next(piece, newQueen));
                            }
                        }
                    }
                }
                if (piece.getClass() == King.class) {
                    int[][] kingMoves = generateQueenMoves();

                    for (int[] move : kingMoves) {
                        int x = piece.rank + move[0];
                        int y = piece.file + move[1];

                        King newKing = new King(piece.player, y, x);
                        if (isMoveLegal(state, piece, newKing)) {
                            children.add(state.next(piece, newKing));
                        }
                    }
                }
            }
        }

        return children;
    }

    private int[][] generateQueenMoves() {
        int[][] moves = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }, { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };
        return moves;
    }

    private int[][] generateRookMoves() {
        int[][] moves = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
        return moves;
    }

    private int[][] generateBishopMoves() {
        int[][] moves = { { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };
        return moves;
    }

    private int[][] generateKnightMoves() {
        int[][] moves = { { 2, 1 }, { 2, -1 }, { -2, 1 }, { -2, -1 }, { 1, 2 }, { 1, -2 }, { -1, 2 }, { -1, -2 } };
        return moves;
    }

    private int[][] generatePawnMoves(Piece piece) {
        int[][] moves = { { 1, 0 }, { 2, 0 }, { 1, 1 }, { 1, -1 } };
        if (piece.player == Player.BLACK) {
            for (int y = 0; y < moves.length; y++) {
                for (int x = 0; x < moves[y].length; x++) {
                    moves[y][x] *= -1;
                }
            }
        }
        return moves;
    }

    private boolean isMoveLegal(State state, Piece from, Piece to) {
        try {
            if (!state.searchLimitReached()) {
                state.next(from, to);
                return true;
            } else {
                return false;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
