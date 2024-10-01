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

    boolean isWinningState(State state) {
        if (state.over) {
            if (!state.check) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected State chooseMove(State root) {

        if (isWinningState(root)) {
            return root;
        }

        int positionCounter = 1;

        boolean maximizingPlayer = root.player == Player.WHITE;

        Result bestMove = new Result(root,
                maximizingPlayer ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
        List<Result> bestMoves = new ArrayList<>();

        for (int depth = 0; depth < 3; depth++) {

            if (bestMoves.size() > 0) {
                positionCounter = 1;
                bestMove = bestMoves.get(maximizingPlayer ? bestMoves.size() - positionCounter : positionCounter - 1);
            }

            bestMove = min_max_ab(root, maximizingPlayer, depth, SearchType.QUIESCENCE,
                    bestMoves.size() > 0 ? bestMove.value
                            : maximizingPlayer ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
            bestMoves.add(bestMove);

            bestMove = min_max_ab(root, maximizingPlayer, depth, SearchType.MINMAX,
                    bestMoves.size() > 0 ? bestMove.value
                            : maximizingPlayer ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
            bestMoves.add(bestMove);

            if (root.searchLimitReached()) {
                break;
            }
        }

        bestMoves.sort(Comparator.comparingDouble(result -> result.value));

        positionCounter = 1;
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

    private ArrayList<State> gatherQuiescentChildren(State state, Piece piece) {
        ArrayList<State> children = new ArrayList<>();
        boolean maximizingPlayer = piece.player == Player.WHITE;

        if (piece.getClass() == Pawn.class) {
            int[][] pawnMoves = maximizingPlayer ? new int[][] { { 1, 0 }, { 1, 1 }, { 1, -1 }, { 2, 0 } }
                    : new int[][] { { -1, 0 }, { -1, 1 }, { -1, -1 }, { -2, 0 } };
            for (int[] move : pawnMoves) {
                int newFile = piece.file + move[0];
                int newRank = piece.rank + move[1];
                if (state.board.pieceAt(newFile, newRank)) {
                    if (isMoveLegal(state, piece, new Pawn(piece.player, newFile, newRank))) {

                        State newMove = state.next(piece, new Pawn(piece.player, newFile, newRank));
                        long hash = newMove.hashCode();

                        if (!visitedStates.containsKey(hash)) {
                            visitedStates.put(hash, newMove);
                            children.add(newMove);
                        }

                    }
                }

            }
        } else if (piece.getClass() == Knight.class) {
            int[][] knightMoves = {
                    { 2, 1 }, { 2, -1 }, { -2, 1 }, { -2, -1 },
                    { 1, 2 }, { 1, -2 }, { -1, 2 }, { -1, -2 }
            };
            for (int[] move : knightMoves) {
                int newFile = piece.file + move[0];
                int newRank = piece.rank + move[1];
                if (state.board.pieceAt(newFile, newRank)) {
                    if (isMoveLegal(state, piece, new Knight(piece.player, newFile, newRank))) {

                        State newMove = state.next(piece, new Knight(piece.player, newFile, newRank));
                        long hash = newMove.hashCode();

                        if (!visitedStates.containsKey(hash)) {
                            visitedStates.put(hash, newMove);
                            children.add(newMove);
                        }

                    }
                }
            }
        } else if (piece.getClass() == Bishop.class) {
            int[][] bishopMoves = {
                    { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 }
            };
            for (int[] move : bishopMoves) {
                for (int i = 1; i < 8; i++) {
                    int newFile = piece.file + i * move[0];
                    int newRank = piece.rank + i * move[1];
                    if (state.board.pieceAt(newFile, newRank)) {
                        if (isMoveLegal(state, piece, new Bishop(piece.player, newFile, newRank))) {

                            State newMove = state.next(piece, new Bishop(piece.player, newFile, newRank));
                            long hash = newMove.hashCode();

                            if (!visitedStates.containsKey(hash)) {
                                visitedStates.put(hash, newMove);
                                children.add(newMove);
                            }
                        }

                    }
                }
            }
        } else if (piece.getClass() == Rook.class) {
            int[][] rookMoves = {
                    { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }
            };
            for (int[] move : rookMoves) {
                for (int i = 1; i < 8; i++) {
                    int newFile = piece.file + i * move[0];
                    int newRank = piece.rank + i * move[1];
                    if (state.board.pieceAt(newFile, newRank)) {
                        if (isMoveLegal(state, piece, new Rook(piece.player, newFile, newRank))) {

                            State newMove = state.next(piece, new Rook(piece.player, newFile, newRank));
                            long hash = newMove.hashCode();

                            if (!visitedStates.containsKey(hash)) {
                                visitedStates.put(hash, newMove);
                                children.add(newMove);
                            }

                        }
                    }
                }
            }
        } else if (piece.getClass() == Queen.class) {
            int[][] queenMoves = {
                    { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 },
                    { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }
            };
            for (int[] move : queenMoves) {
                for (int i = 1; i < 8; i++) {
                    int newFile = piece.file + i * move[0];
                    int newRank = piece.rank + i * move[1];
                    if (state.board.pieceAt(newFile, newRank)) {
                        if (isMoveLegal(state, piece, new Queen(piece.player, newFile, newRank))) {

                            State newMove = state.next(piece, new Queen(piece.player, newFile, newRank));
                            long hash = newMove.hashCode();

                            if (!visitedStates.containsKey(hash)) {
                                visitedStates.put(hash, newMove);
                                children.add(newMove);
                            }

                        }
                    }
                }
            }
        } else if (piece.getClass() == King.class) {
            int[][] kingMoves = {
                    { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 },
                    { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }
            };
            for (int[] move : kingMoves) {
                int newFile = piece.file + move[0];
                int newRank = piece.rank + move[1];
                if (state.board.pieceAt(newFile, newRank)) {
                    if (isMoveLegal(state, piece, new King(piece.player, newFile, newRank))) {

                        State newMove = state.next(piece, new King(piece.player, newFile, newRank));
                        long hash = newMove.hashCode();

                        if (!visitedStates.containsKey(hash)) {
                            visitedStates.put(hash, newMove);
                            children.add(newMove);
                        }

                    }
                }
            }
        }

        return children;
    }

    private Result min_max_ab(State state, boolean maximizingPlayer, int depth, SearchType searchType,
            double bestValue) {
        Result result = maximizingPlayer
                ? max_ab(state, depth, bestValue, Double.POSITIVE_INFINITY, searchType)
                : min_ab(state, depth, Double.NEGATIVE_INFINITY, bestValue, searchType);

        return result;
    }

    private ArrayList<State> gatherChildren(State state) {
        ArrayList<State> children = new ArrayList<>();

        Iterator<State> iterator = state.next().iterator();
        int counter = 0;

        while (!state.searchLimitReached() && iterator.hasNext() && counter < 20) {
            State move = iterator.next();
            long hash = move.hashCode();

            if (!visitedStates.containsKey(hash)) {
                visitedStates.put(hash, move);
                children.add(move);
                counter++;
            }

        }

        return children;
    }

    private Result max_ab(State state, int depth, double alpha, double beta, SearchType searchType) {
        if (depth == 0 || state.countDescendants() == 0) {
            return materialValue(state);
        }

        if (isWinningState(state)) {
            return new Result(state, Double.POSITIVE_INFINITY);
        }

        if (state.over && !state.check) {
            return new Result(state, Double.NEGATIVE_INFINITY);
        }

        Result best = new Result(state, Double.NEGATIVE_INFINITY);

        ArrayList<State> children = new ArrayList<>();

        if (searchType == SearchType.MINMAX) {
            children = gatherChildren(state);
        } else {
            for (Piece piece : state.board) {
                children = gatherQuiescentChildren(state, piece);
            }
        }

        if (children.size() == 0) {
            return materialValue(state);
        }

        for (State child : children) {
            Result value = min_ab(child, depth - 1, alpha, beta, searchType);

            if (value.value > best.value) {
                best = value;
            }
            alpha = Math.max(alpha, best.value);

            if (beta <= alpha)
                break;
        }
        return best;
    }

    private Result min_ab(State state, int depth, double alpha, double beta, SearchType searchType) {
        if (depth == 0 || state.countDescendants() == 0 || isWinningState(state)) {
            return materialValue(state);
        }

        if (isWinningState(state)) {
            return new Result(state, Double.NEGATIVE_INFINITY);
        }

        if (state.over && !state.check) {
            return new Result(state, Double.POSITIVE_INFINITY);
        }

        Result best = new Result(state, Double.POSITIVE_INFINITY);

        ArrayList<State> children = new ArrayList<>();

        if (searchType == SearchType.MINMAX) {
            children = gatherChildren(state);
        } else {
            for (Piece piece : state.board) {
                children = gatherQuiescentChildren(state, piece);
            }
        }
        if (children.size() == 0) {
            return materialValue(state);
        }

        for (State child : children) {
            Result value = max_ab(child, depth - 1, alpha, beta, searchType);

            if (best.value < value.value) {
                best = value;
            }
            beta = Math.min(beta, best.value);

            if (beta <= alpha)
                break;

        }

        return best;
    }

    private Result materialValue(State state) {
        double value = 0;

        value += state.player == Player.WHITE ? evaluateKingSafety(state) : -evaluateKingSafety(state);

        for (Piece piece : state.board) {
            value += rawMaterialValue(piece);
            value += getPieceSquareValue(state, piece);
            value += mobilityScore(state, piece);
        }

        return new Result(state, value);
    }

    private double rawMaterialValue(Piece piece) {
        double value = 0;

        boolean maximizingPlayer = piece.player == Player.WHITE;

        if (piece.getClass() == Pawn.class) {
            value += maximizingPlayer ? 100 : -100;
        }
        if (piece.getClass() == Knight.class) {
            value += maximizingPlayer ? 320 : -320;
        }
        if (piece.getClass() == Bishop.class) {
            value += maximizingPlayer ? 330 : -330;
        }
        if (piece.getClass() == Rook.class) {
            value += maximizingPlayer ? 500 : -500;
        }
        if (piece.getClass() == Queen.class) {
            value += maximizingPlayer ? 900 : -900;
        }
        if (piece.getClass() == King.class) {
            value += maximizingPlayer ? 20000 : -20000;
        }

        return value;
    }

    private enum GamePhase {
        MIDDLE_GAME, END_GAME
    }

    private GamePhase determineGamePhase(State state) {
        int whiteQueenCount = 0;
        int blackQueenCount = 0;
        int whiteOtherPiecesCount = 0;
        int blackOtherPiecesCount = 0;

        for (Piece piece : state.board) {
            if (piece.getClass() == Queen.class) {
                if (piece.player == Player.WHITE) {
                    whiteQueenCount++;
                } else if (piece.player == Player.BLACK) {
                    blackQueenCount++;
                }
            } else if (piece.getClass() != King.class) {
                if (piece.player == Player.WHITE) {
                    whiteOtherPiecesCount++;
                } else if (piece.player == Player.BLACK) {
                    blackOtherPiecesCount++;
                }
            }
        }

        boolean whiteEndGame = (whiteQueenCount == 0) || (whiteQueenCount == 1 && whiteOtherPiecesCount <= 1);
        boolean blackEndGame = (blackQueenCount == 0) || (blackQueenCount == 1 && blackOtherPiecesCount <= 1);

        if (whiteEndGame && blackEndGame) {
            return GamePhase.END_GAME;
        } else {
            return GamePhase.MIDDLE_GAME;
        }
    }

    private double getPieceSquareValue(State state, Piece piece) {
        // Values for square tables copied from
        // https://www.chessprogramming.org/Simplified_Evaluation_Function

        double value = 0;
        boolean maximizingPlayer = piece.player == Player.WHITE;

        if (piece.getClass() == Pawn.class) {
            value += maximizingPlayer ? whitePawnPieceSquareTable[piece.file][piece.rank]
                    : -blackPawnPieceSquareTable[piece.file][piece.rank];
        }
        if (piece.getClass() == Knight.class) {
            value += maximizingPlayer ? whiteKnightPieceSquareTable[piece.file][piece.rank]
                    : -blackKnightPieceSquareTable[piece.file][piece.rank];
        }
        if (piece.getClass() == Bishop.class) {
            value += maximizingPlayer ? whiteBishopPieceSquareTable[piece.file][piece.rank]
                    : -blackBishopPieceSquareTable[piece.file][piece.rank];
        }
        if (piece.getClass() == Rook.class) {
            value += maximizingPlayer ? whiteRookPieceSquareTable[piece.file][piece.rank]
                    : -blackRookPieceSquareTable[piece.file][piece.rank];
        }
        if (piece.getClass() == Queen.class) {
            value += maximizingPlayer ? whiteQueenPieceSquareTable[piece.file][piece.rank]
                    : -blackQueenPieceSquareTable[piece.file][piece.rank];
        }
        if (piece.getClass() == King.class) {
            GamePhase gamePhase = determineGamePhase(state);
            if (gamePhase == GamePhase.MIDDLE_GAME) {
                value += maximizingPlayer ? whiteKingMiddleGamePieceSquareTable[piece.file][piece.rank]
                        : -blackKingMiddleGamePieceSquareTable[piece.file][piece.rank];
            } else {
                value += maximizingPlayer ? whiteKingEndGamePieceSquareTable[piece.file][piece.rank]
                        : -blackKingEndGamePieceSquareTable[piece.file][piece.rank];
            }
        }

        return value;
    }

    private double mobilityScore(State state, Piece piece) {
        // Idea for looping through possible moves taken from
        // https://stackoverflow.com/questions/71594433/how-to-write-the-moves-the-knight-can-perform

        double value = 0;
        boolean maximizingPlayer = piece.player == Player.WHITE;

        if (piece.getClass() == Pawn.class) {
            if (maximizingPlayer) {
                if (isMoveLegal(state, piece, new Pawn(piece.player, piece.file + 1, piece.rank)))
                    value++;
                if (isMoveLegal(state, piece, new Pawn(piece.player, piece.file + 1, piece.rank + 1)))
                    value++;
                if (isMoveLegal(state, piece, new Pawn(piece.player, piece.file + 1, piece.rank - 1)))
                    value++;
                if (piece.file == 1)
                    if (isMoveLegal(state, piece, new Pawn(piece.player, piece.file + 2, piece.rank)))
                        value++;
            } else {
                if (piece.file == 6)
                    if (isMoveLegal(state, piece, new Pawn(piece.player, piece.file - 2, piece.rank)))
                        value++;
                if (isMoveLegal(state, piece, new Pawn(piece.player, piece.file - 1, piece.rank)))
                    value++;
                if (isMoveLegal(state, piece, new Pawn(piece.player, piece.file - 1, piece.rank + 1)))
                    value++;
                if (isMoveLegal(state, piece, new Pawn(piece.player, piece.file - 1, piece.rank - 1)))
                    value++;
            }
        } else if (piece.getClass() == Knight.class) {
            int[][] knightMoves = {
                    { 2, 1 }, { 2, -1 }, { -2, 1 }, { -2, -1 },
                    { 1, 2 }, { 1, -2 }, { -1, 2 }, { -1, -2 }
            };
            for (int[] move : knightMoves) {
                if (isMoveLegal(state, piece, new Knight(piece.player, piece.file + move[0], piece.rank + move[1])))
                    value++;
            }
        } else if (piece.getClass() == Bishop.class) {
            int[][] bishopMoves = {
                    { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 }
            };
            for (int[] move : bishopMoves) {
                for (int i = 1; i < 8; i++) {
                    if (isMoveLegal(state, piece,
                            new Bishop(piece.player, piece.file + i * move[0], piece.rank + i * move[1])))
                        value++;
                }
            }
        } else if (piece.getClass() == Rook.class) {
            int[][] rookMoves = {
                    { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }
            };
            for (int[] move : rookMoves) {
                for (int i = 1; i < 8; i++) {
                    if (isMoveLegal(state, piece,
                            new Rook(piece.player, piece.file + i * move[0], piece.rank + i * move[1])))
                        value++;
                }
            }
        } else if (piece.getClass() == Queen.class) {
            int[][] queenMoves = {
                    { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 },
                    { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }
            };
            for (int[] move : queenMoves) {
                for (int i = 1; i < 8; i++) {
                    if (isMoveLegal(state, piece,
                            new Queen(piece.player, piece.file + i * move[0], piece.rank + i * move[1])))
                        value++;
                }
            }
        } else if (piece.getClass() == King.class) {
            int[][] kingMoves = {
                    { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 },
                    { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }
            };
            for (int[] move : kingMoves) {
                if (isMoveLegal(state, piece, new King(piece.player, piece.file + move[0], piece.rank + move[1])))
                    value++;
            }
        }

        return value;
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

    private double evaluateKingSafety(State state) {
        double value = 0;
        King king = state.board.getKing(state.player);
        boolean maximizingPlayer = state.player == Player.WHITE;

        if (maximizingPlayer) {
            int[] ranks = { king.rank - 1, king.rank, king.rank + 1 };
            for (int rank : ranks) {
                if (state.board.pieceAt(king.file + 1, rank)) {
                    Piece piece = state.board.getPieceAt(king.file + 1, rank);
                    if (piece.player == state.player && piece.getClass() == Pawn.class) {
                        value++;
                    } else {
                        value--;
                    }
                }
            }
        } else {
            int[] ranks = { king.rank - 1, king.rank, king.rank + 1 };
            for (int rank : ranks) {
                if (state.board.pieceAt(king.file - 1, rank)) {
                    Piece piece = state.board.getPieceAt(king.file - 1, rank);
                    if (piece.player == state.player && piece.getClass() == Pawn.class) {
                        value++;
                    } else {
                        value--;
                    }
                }
            }
        }

        for (Piece piece : state.board) {
            if (piece.player != state.player) {
                int ranks[] = { king.rank - 1, king.rank, king.rank + 1 };
                int files[] = { king.file - 1, king.file, king.file + 1 };

                for (int rank : ranks) {
                    for (int file : files) {
                        if (piece.getClass() == Pawn.class) {
                            if (isMoveLegal(state, piece, new Pawn(piece.player, file, rank))) {
                                value -= 2;
                            }
                        }
                        if (piece.getClass() == Knight.class) {
                            if (isMoveLegal(state, piece, new Knight(piece.player, file, rank))) {
                                value -= 2;
                            }
                        }
                        if (piece.getClass() == Bishop.class) {
                            if (isMoveLegal(state, piece, new Bishop(piece.player, file, rank))) {
                                value -= 2;
                            }
                        }
                        if (piece.getClass() == Rook.class) {
                            if (isMoveLegal(state, piece, new Rook(piece.player, file, rank))) {
                                value -= 2;
                            }
                        }
                        if (piece.getClass() == Queen.class) {
                            if (isMoveLegal(state, piece, new Queen(piece.player, file, rank))) {
                                value -= 2;
                            }
                        }
                        if (piece.getClass() == King.class) {
                            if (isMoveLegal(state, piece, new King(piece.player, file, rank))) {
                                value -= 2;
                            }
                        }
                    }
                }
            }
        }

        return value;
    }

    private double[][] whitePawnPieceSquareTable = new double[][] {
            { 0, 0, 0, 0, 0, 0, 0, 0 },
            { 5, 10, 10, -20, -20, 10, 10, 5 },
            { 5, -5, -10, 0, 0, -10, -5, 5 },
            { 0, 0, 0, 20, 20, 0, 0, 0 },
            { 5, 5, 10, 25, 25, 10, 5, 5 },
            { 10, 10, 20, 30, 30, 20, 10, 10 },
            { 50, 50, 50, 50, 50, 50, 50, 50 },
            { 0, 0, 0, 0, 0, 0, 0, 0 }
    };
    private double[][] blackPawnPieceSquareTable = new double[][] {
            { 0, 0, 0, 0, 0, 0, 0, 0 },
            { 50, 50, 50, 50, 50, 50, 50, 50 },
            { 10, 10, 20, 30, 30, 20, 10, 10 },
            { 5, 5, 10, 25, 25, 10, 5, 5 },
            { 0, 0, 0, 20, 20, 0, 0, 0 },
            { 5, -5, -10, 0, 0, -10, -5, 5 },
            { 5, 10, 10, -20, -20, 10, 10, 5 },
            { 0, 0, 0, 0, 0, 0, 0, 0 }
    };

    private double[][] whiteKnightPieceSquareTable = new double[][] {
            { -50, -40, -30, -30, -30, -30, -40, -50 },
            { -40, -20, 0, 0, 0, 0, -20, -40 },
            { -30, 0, 10, 15, 15, 10, 0, -30 },
            { -30, 5, 15, 20, 20, 15, 5, -30 },
            { -30, 0, 15, 20, 20, 15, 0, -30 },
            { -30, 5, 10, 15, 15, 10, 5, -30 },
            { -40, -20, 0, 5, 5, 0, -20, -40 },
            { -50, -40, -30, -30, -30, -30, -40, -50 }
    };

    private double[][] blackKnightPieceSquareTable = new double[][] {
            { -50, -40, -30, -30, -30, -30, -40, -50 },
            { -40, -20, 0, 5, 5, 0, -20, -40 },
            { -30, 5, 10, 15, 15, 10, 5, -30 },
            { -30, 0, 15, 20, 20, 15, 0, -30 },
            { -30, 5, 15, 20, 20, 15, 5, -30 },
            { -30, 0, 10, 15, 15, 10, 0, -30 },
            { -40, -20, 0, 0, 0, 0, -20, -40 },
            { -50, -40, -30, -30, -30, -30, -40, -50 }
    };

    private double[][] whiteBishopPieceSquareTable = new double[][] {
            { -20, -10, -10, -10, -10, -10, -10, -20 },
            { -10, 0, 0, 0, 0, 0, 0, -10 },
            { -10, 0, 5, 10, 10, 5, 0, -10 },
            { -10, 5, 5, 10, 10, 5, 5, -10 },
            { -10, 0, 10, 10, 10, 10, 0, -10 },
            { -10, 10, 10, 10, 10, 10, 10, -10 },
            { -10, 5, 0, 0, 0, 0, 5, -10 },
            { -20, -10, -10, -10, -10, -10, -10, -20 }
    };

    private double[][] blackBishopPieceSquareTable = new double[][] {
            { -20, -10, -10, -10, -10, -10, -10, -20 },
            { -10, 5, 0, 0, 0, 0, 5, -10 },
            { -10, 10, 10, 10, 10, 10, 10, -10 },
            { -10, 0, 10, 10, 10, 10, 0, -10 },
            { -10, 5, 5, 10, 10, 5, 5, -10 },
            { -10, 0, 5, 10, 10, 5, 0, -10 },
            { -10, 0, 0, 0, 0, 0, 0, -10 },
            { -20, -10, -10, -10, -10, -10, -10, -20 }
    };

    private double[][] whiteRookPieceSquareTable = new double[][] {
            { 0, 0, 0, 0, 0, 0, 0, 0 },
            { 5, 10, 10, 10, 10, 10, 10, 5 },
            { -5, 0, 0, 0, 0, 0, 0, -5 },
            { -5, 0, 0, 0, 0, 0, 0, -5 },
            { -5, 0, 0, 0, 0, 0, 0, -5 },
            { -5, 0, 0, 0, 0, 0, 0, -5 },
            { -5, 0, 0, 0, 0, 0, 0, -5 },
            { 0, 0, 0, 5, 5, 0, 0, 0 }
    };

    private double[][] blackRookPieceSquareTable = new double[][] {
            { 0, 0, 0, 5, 5, 0, 0, 0 },
            { -5, 0, 0, 0, 0, 0, 0, -5 },
            { -5, 0, 0, 0, 0, 0, 0, -5 },
            { -5, 0, 0, 0, 0, 0, 0, -5 },
            { -5, 0, 0, 0, 0, 0, 0, -5 },
            { -5, 0, 0, 0, 0, 0, 0, -5 },
            { 5, 10, 10, 10, 10, 10, 10, 5 },
            { 0, 0, 0, 0, 0, 0, 0, 0 }
    };

    private double[][] whiteQueenPieceSquareTable = new double[][] {
            { -20, -10, -10, -5, -5, -10, -10, -20 },
            { -10, 0, 0, 0, 0, 0, 0, -10 },
            { -10, 0, 5, 5, 5, 5, 0, -10 },
            { -5, 0, 5, 5, 5, 5, 0, -5 },
            { 0, 0, 5, 5, 5, 5, 0, -5 },
            { -10, 5, 5, 5, 5, 5, 0, -10 },
            { -10, 0, 5, 0, 0, 0, 0, -10 },
            { -20, -10, -10, -5, -5, -10, -10, -20 }
    };

    private double[][] blackQueenPieceSquareTable = new double[][] {
            { -20, -10, -10, -5, -5, -10, -10, -20 },
            { -10, 0, 5, 0, 0, 0, 0, -10 },
            { -10, 5, 5, 5, 5, 5, 0, -10 },
            { 0, 0, 5, 5, 5, 5, 0, -5 },
            { -5, 0, 5, 5, 5, 5, 0, -5 },
            { -10, 0, 5, 5, 5, 5, 0, -10 },
            { -10, 0, 0, 0, 0, 0, 0, -10 },
            { -20, -10, -10, -5, -5, -10, -10, -20 }
    };

    private double[][] whiteKingMiddleGamePieceSquareTable = new double[][] {
            { -30, -40, -40, -50, -50, -40, -40, -30 },
            { -30, -40, -40, -50, -50, -40, -40, -30 },
            { -30, -40, -40, -50, -50, -40, -40, -30 },
            { -30, -40, -40, -50, -50, -40, -40, -30 },
            { -20, -30, -30, -40, -40, -30, -30, -20 },
            { -10, -20, -20, -20, -20, -20, -20, -10 },
            { 20, 20, 0, 0, 0, 0, 20, 20 },
            { 20, 30, 10, 0, 0, 10, 30, 20 }
    };

    private double[][] blackKingMiddleGamePieceSquareTable = new double[][] {
            { 20, 30, 10, 0, 0, 10, 30, 20 },
            { 20, 20, 0, 0, 0, 0, 20, 20 },
            { -10, -20, -20, -20, -20, -20, -20, -10 },
            { -20, -30, -30, -40, -40, -30, -30, -20 },
            { -30, -40, -40, -50, -50, -40, -40, -30 },
            { -30, -40, -40, -50, -50, -40, -40, -30 },
            { -30, -40, -40, -50, -50, -40, -40, -30 },
            { -30, -40, -40, -50, -50, -40, -40, -30 }
    };

    private double[][] whiteKingEndGamePieceSquareTable = new double[][] {
            { -50, -40, -30, -20, -20, -30, -40, -50 },
            { -30, -20, -10, 0, 0, -10, -20, -30 },
            { -30, -10, 20, 30, 30, 20, -10, -30 },
            { -30, -10, 30, 40, 40, 30, -10, -30 },
            { -30, -10, 30, 40, 40, 30, -10, -30 },
            { -30, -10, 20, 30, 30, 20, -10, -30 },
            { -30, -30, 0, 0, 0, 0, -30, -30 },
            { -50, -30, -30, -30, -30, -30, -30, -50 }
    };

    private double[][] blackKingEndGamePieceSquareTable = new double[][] {
            { -50, -30, -30, -30, -30, -30, -30, -50 },
            { -30, -30, 0, 0, 0, 0, -30, -30 },
            { -30, -10, 20, 30, 30, 20, -10, -30 },
            { -30, -10, 30, 40, 40, 30, -10, -30 },
            { -30, -10, 30, 40, 40, 30, -10, -30 },
            { -30, -10, 20, 30, 30, 20, -10, -30 },
            { -30, -20, -10, 0, 0, -10, -20, -30 },
            { -50, -40, -30, -20, -20, -30, -40, -50 }
    };
}