package com.stephengware.java.games.chess.bot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.stephengware.java.games.chess.state.*;

public class Cawatso4 extends Bot {

    public Cawatso4() {
        super("Cawatso4 Bot");
    }

    private HashMap<Long, State> visitedStates = new HashMap<>();

    @Override
    protected State chooseMove(State root) {

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

        System.out.println("\nprinting results list");
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

            while (bestMove.state.previous != root) {
                bestMove.state = bestMove.state.previous;
            }
        }

        return bestMove.state;
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

        if (state.over) {
            if (state.check) {
                return new Result(state, Double.NEGATIVE_INFINITY);
            } else {
                return new Result(state, Double.POSITIVE_INFINITY);
            }
        }

        Result best = new Result(state, Double.NEGATIVE_INFINITY);

        ArrayList<Result> children = gatherChildren(state);

        for (int i = children.size() - 1; i >= 0; i--) {
            Result result = min_ab(children.get(i).state, depth - 1, alpha, beta);
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

        if (state.over) {
            if (state.check) {
                return new Result(state, Double.POSITIVE_INFINITY);
            } else {
                return new Result(state, Double.NEGATIVE_INFINITY);
            }
        }

        Result best = new Result(state, Double.POSITIVE_INFINITY);

        ArrayList<Result> children = gatherChildren(state);

        for (int i = 0; i < children.size(); i++) {
            Result result = max_ab(children.get(i).state, depth - 1, alpha, beta);
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
            value += getPieceSquareValue(state, piece);
            value += mobilityValue(state, piece);
            value += kingSafetyValue(state, piece);
        }

        return value;
    }

    private double mobilityValue(State state, Piece piece) {
        double value = 0;

        boolean maximizingPlayer = piece.player == Player.WHITE;

        if (piece.getClass() == Pawn.class) {
            int[][] pawnMoves = generatePawnMoves(piece);
            for (int[] move : pawnMoves) {
                int y = piece.file + move[0];
                int x = piece.rank + move[1];

                Pawn newPawn = new Pawn(piece.player, y, x);
                if (isMoveLegal(state, piece, newPawn)) {
                    value += 10;
                }
            }
        }

        if (piece.getClass() == Knight.class) {
            int[][] knightMoves = generateKnightMoves();

            for (int[] move : knightMoves) {
                int y = piece.file + move[0];
                int x = piece.rank + move[1];

                Knight newKnight = new Knight(piece.player, y, x);
                if (isMoveLegal(state, piece, newKnight)) {
                    value += 10;
                }
            }
        }

        if (piece.getClass() == Bishop.class) {
            int[][] bishopMoves = generateBishopMoves();

            for (int i = 1; i < 8; i++) {
                for (int[] move : bishopMoves) {
                    int y = piece.file + move[0] * i;
                    int x = piece.rank + move[1] * i;

                    Bishop newBishop = new Bishop(piece.player, y, x);
                    if (isMoveLegal(state, piece, newBishop)) {
                        value += 10;
                    }
                }
            }
        }

        if (piece.getClass() == Rook.class) {
            int[][] rookMoves = generateRookMoves();

            for (int i = 1; i < 8; i++) {
                for (int[] move : rookMoves) {
                    int y = piece.file + move[0] * i;
                    int x = piece.rank + move[1] * i;

                    Rook newRook = new Rook(piece.player, y, x);
                    if (isMoveLegal(state, piece, newRook)) {
                        value += 10;
                    }
                }
            }
        }

        if (piece.getClass() == Queen.class) {
            int[][] queenMoves = generateQueenMoves();

            for (int i = 1; i < 8; i++) {
                for (int[] move : queenMoves) {
                    int y = piece.file + move[0] * i;
                    int x = piece.rank + move[1] * i;

                    Queen newQueen = new Queen(piece.player, y, x);
                    if (isMoveLegal(state, piece, newQueen)) {
                        value += 10;
                    }
                }
            }
        }

        if (piece.getClass() == King.class) {
            int[][] kingMoves = generateQueenMoves();

            for (int[] move : kingMoves) {
                int y = piece.file + move[0];
                int x = piece.rank + move[1];

                King newKing = new King(piece.player, y, x);
                if (isMoveLegal(state, piece, newKing)) {
                    value += 10;
                }
            }
        }

        if (!maximizingPlayer) {
            value *= -1;
        }

        return value;
    }

    private double kingSafetyValue(State state, Piece piece) {
        double value = 0;

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

    private ArrayList<Result> gatherChildren(State state) {
        // build the list manually from possible moves of every
        // one of our pieces on the board

        ArrayList<State> children = new ArrayList<>();

        for (Piece piece : state.board) {
            if (piece.player == state.player) {
                // build possible moves of said piece based on rules of its movement
                if (piece.getClass() == Pawn.class) {
                    int[][] pawnMoves = generatePawnMoves(piece);
                    for (int[] move : pawnMoves) {
                        int y = piece.file + move[0];
                        int x = piece.rank + move[1];

                        Pawn newPawn = new Pawn(piece.player, y, x);
                        if (isMoveLegal(state, piece, newPawn)) {
                            State newMove = state.next(piece, newPawn);
                            long key = newMove.hashCode();

                            if (!visitedStates.containsKey(key)) {
                                children.add(newMove);
                                visitedStates.put(key, newMove);
                            }
                        }
                    }
                }
                if (piece.getClass() == Knight.class) {
                    int[][] knightMoves = generateKnightMoves();

                    for (int[] move : knightMoves) {
                        int y = piece.file + move[0];
                        int x = piece.rank + move[1];

                        Knight newKnight = new Knight(piece.player, y, x);
                        if (isMoveLegal(state, piece, newKnight)) {
                            State newMove = state.next(piece, newKnight);
                            long key = newMove.hashCode();

                            if (!visitedStates.containsKey(key)) {
                                children.add(newMove);
                                visitedStates.put(key, newMove);
                            }
                        }
                    }
                }
                if (piece.getClass() == Bishop.class) {
                    int[][] bishopMoves = generateBishopMoves();

                    for (int i = 1; i < 8; i++) {
                        for (int[] move : bishopMoves) {
                            int y = piece.file + move[0] * i;
                            int x = piece.rank + move[1] * i;

                            Bishop newBishop = new Bishop(piece.player, y, x);
                            if (isMoveLegal(state, piece, newBishop)) {
                                State newMove = state.next(piece, newBishop);
                                long key = newMove.hashCode();

                                if (!visitedStates.containsKey(key)) {
                                    children.add(newMove);
                                    visitedStates.put(key, newMove);
                                }
                            }
                        }
                    }
                }
                if (piece.getClass() == Rook.class) {
                    int[][] rookMoves = generateRookMoves();

                    for (int i = 1; i < 8; i++) {
                        for (int[] move : rookMoves) {
                            int y = piece.file + move[0] * i;
                            int x = piece.rank + move[1] * i;

                            Rook newRook = new Rook(piece.player, y, x);
                            if (isMoveLegal(state, piece, newRook)) {
                                State newMove = state.next(piece, newRook);
                                long key = newMove.hashCode();

                                if (!visitedStates.containsKey(key)) {
                                    children.add(newMove);
                                    visitedStates.put(key, newMove);
                                }
                            }
                        }
                    }
                }
                if (piece.getClass() == Queen.class) {
                    int[][] queenMoves = generateQueenMoves();

                    for (int i = 1; i < 8; i++) {
                        for (int[] move : queenMoves) {
                            int y = piece.file + move[0] * i;
                            int x = piece.rank + move[1] * i;

                            Queen newQueen = new Queen(piece.player, y, x);
                            if (isMoveLegal(state, piece, newQueen)) {
                                State newMove = state.next(piece, newQueen);
                                long key = newMove.hashCode();

                                if (!visitedStates.containsKey(key)) {
                                    children.add(newMove);
                                    visitedStates.put(key, newMove);
                                }
                            }
                        }
                    }
                }
                if (piece.getClass() == King.class) {
                    int[][] kingMoves = generateQueenMoves();

                    for (int[] move : kingMoves) {
                        int y = piece.file + move[0];
                        int x = piece.rank + move[1];

                        King newKing = new King(piece.player, y, x);
                        if (isMoveLegal(state, piece, newKing)) {
                            State newMove = state.next(piece, newKing);
                            long key = newMove.hashCode();

                            if (!visitedStates.containsKey(key)) {
                                children.add(newMove);
                                visitedStates.put(key, newMove);
                            }
                        }
                    }
                }
            }
        }

        ArrayList<Result> sortedChildren = new ArrayList<>();

        for (State child : children) {
            sortedChildren.add(new Result(child, evaluateState(child)));
        }

        sortedChildren.sort(Comparator.comparingDouble(result -> result.value));

        return sortedChildren;
    }

    private ArrayList<Result> gatherQuiescentChildren(State state) {
        ArrayList<State> children = new ArrayList<>();

        for (Piece piece : state.board) {
            if (piece.player == state.player) {
                if (piece.getClass() == Pawn.class) {
                    int[][] pawnMoves = generatePawnMoves(piece);
                    for (int[] move : pawnMoves) {
                        int y = piece.file + move[0];
                        int x = piece.rank + move[1];

                        if (state.board.pieceAt(y, x, state.player.other())) {
                            Pawn newPawn = new Pawn(piece.player, y, x);
                            if (isMoveLegal(state, piece, newPawn)) {
                                State newMove = state.next(piece, newPawn);
                                long key = newMove.hashCode();

                                if (!visitedStates.containsKey(key)) {
                                    children.add(newMove);
                                    visitedStates.put(key, newMove);
                                }

                            }
                        }

                        int [][] kingSurroundingSquares = generateQueenMoves();
                        for (int[] square : kingSurroundingSquares) {
                            int kingY = state.board.getKing(state.player.other()).file  + square[0];
                            int kingX = state.board.getKing(state.player.other()).rank + square[1];

                            if (state.board.pieceAt(kingY, kingX, state.player.other())) {
                                Pawn newPawn = new Pawn(piece.player, kingY, kingX);
                                if (isMoveLegal(state, piece, newPawn)) {
                                    State newMove = state.next(piece, newPawn);
                                    long key = newMove.hashCode();

                                    if (!visitedStates.containsKey(key)) {
                                        children.add(newMove);
                                        visitedStates.put(key, newMove);
                                    }
                                }
                            }
                        }

                    }
                }

                if (piece.getClass() == Knight.class) {
                    int[][] knightMoves = generateKnightMoves();

                    for (int[] move : knightMoves) {
                        int y = piece.file + move[0];
                        int x = piece.rank + move[1];

                        if (state.board.pieceAt(y, x, state.player.other())) {
                            Knight newKnight = new Knight(piece.player, y, x);
                            if (isMoveLegal(state, piece, newKnight)) {
                                State newMove = state.next(piece, newKnight);
                                long key = newMove.hashCode();

                                if (!visitedStates.containsKey(key)) {
                                    children.add(newMove);
                                    visitedStates.put(key, newMove);
                                }
                            }
                        }

                        int [][] kingSurroundingSquares = generateQueenMoves();
                        for (int[] square : kingSurroundingSquares) {
                            int kingY = state.board.getKing(state.player.other()).file  + square[0];
                            int kingX = state.board.getKing(state.player.other()).rank + square[1];

                            if (state.board.pieceAt(kingY, kingX, state.player.other())) {
                                Knight newKnight = new Knight(piece.player, kingY, kingX);
                                if (isMoveLegal(state, piece, newKnight)) {
                                    State newMove = state.next(piece, newKnight);
                                    long key = newMove.hashCode();

                                    if (!visitedStates.containsKey(key)) {
                                        children.add(newMove);
                                        visitedStates.put(key, newMove);
                                    }
                                }
                            }
                        }

                    }
                }

                if (piece.getClass() == Bishop.class) {
                    int[][] bishopMoves = generateBishopMoves();

                    for (int i = 1; i < 8; i++) {
                        for (int[] move : bishopMoves) {
                            int y = piece.file + move[0] * i;
                            int x = piece.rank + move[1] * i;

                            if (state.board.pieceAt(y, x, state.player.other())) {
                                Bishop newBishop = new Bishop(piece.player, y, x);
                                if (isMoveLegal(state, piece, newBishop)) {
                                    State newMove = state.next(piece, newBishop);
                                    long key = newMove.hashCode();

                                    if (!visitedStates.containsKey(key)) {
                                        children.add(newMove);
                                        visitedStates.put(key, newMove);
                                    }
                                }
                            }

                            int [][] kingSurroundingSquares = generateQueenMoves();
                            for (int[] square : kingSurroundingSquares) {
                                int kingY = state.board.getKing(state.player.other()).file  + square[0];
                                int kingX = state.board.getKing(state.player.other()).rank + square[1];

                                if (state.board.pieceAt(kingY, kingX, state.player.other())) {
                                    Bishop newBishop = new Bishop(piece.player, kingY, kingX);
                                    if (isMoveLegal(state, piece, newBishop)) {
                                        State newMove = state.next(piece, newBishop);
                                        long key = newMove.hashCode();

                                        if (!visitedStates.containsKey(key)) {
                                            children.add(newMove);
                                            visitedStates.put(key, newMove);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (piece.getClass() == Rook.class) {
                    int[][] rookMoves = generateRookMoves();

                    for (int i = 1; i < 8; i++) {
                        for (int[] move : rookMoves) {
                            int y = piece.file + move[0] * i;
                            int x = piece.rank + move[1] * i;

                            if (state.board.pieceAt(y, x, state.player.other())) {
                                Rook newRook = new Rook(piece.player, y, x);
                                if (isMoveLegal(state, piece, newRook)) {
                                    State newMove = state.next(piece, newRook);
                                    long key = newMove.hashCode();

                                    if (!visitedStates.containsKey(key)) {
                                        children.add(newMove);
                                        visitedStates.put(key, newMove);
                                    }
                                }
                            }

                            int [][] kingSurroundingSquares = generateQueenMoves();
                            for (int[] square : kingSurroundingSquares) {
                                int kingY = state.board.getKing(state.player.other()).file  + square[0];
                                int kingX = state.board.getKing(state.player.other()).rank + square[1];

                                if (state.board.pieceAt(kingY, kingX, state.player.other())) {
                                    Rook newRook = new Rook(piece.player, kingY, kingX);
                                    if (isMoveLegal(state, piece, newRook)) {
                                        State newMove = state.next(piece, newRook);
                                        long key = newMove.hashCode();

                                        if (!visitedStates.containsKey(key)) {
                                            children.add(newMove);
                                            visitedStates.put(key, newMove);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (piece.getClass() == Queen.class) {
                    int[][] queenMoves = generateQueenMoves();

                    for (int i = 1; i < 8; i++) {
                        for (int[] move : queenMoves) {
                            int y = piece.file + move[0] * i;
                            int x = piece.rank + move[1] * i;

                            if (state.board.pieceAt(y, x, state.player.other())) {
                                Queen newQueen = new Queen(piece.player, y, x);
                                if (isMoveLegal(state, piece, newQueen)) {
                                    State newMove = state.next(piece, newQueen);
                                    long key = newMove.hashCode();

                                    if (!visitedStates.containsKey(key)) {
                                        children.add(newMove);
                                        visitedStates.put(key, newMove);
                                    }
                                }
                            }

                            int [][] kingSurroundingSquares = generateQueenMoves();
                            for (int[] square : kingSurroundingSquares) {
                                int kingY = state.board.getKing(state.player.other()).file  + square[0];
                                int kingX = state.board.getKing(state.player.other()).rank + square[1];

                                if (state.board.pieceAt(kingY, kingX, state.player.other())) {
                                    Queen newQueen = new Queen(piece.player, kingY, kingX);
                                    if (isMoveLegal(state, piece, newQueen)) {
                                        State newMove = state.next(piece, newQueen);
                                        long key = newMove.hashCode();

                                        if (!visitedStates.containsKey(key)) {
                                            children.add(newMove);
                                            visitedStates.put(key, newMove);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (piece.getClass() == King.class) {
                    int[][] kingMoves = generateQueenMoves();

                    for (int[] move : kingMoves) {
                        int y = piece.file + move[0];
                        int x = piece.rank + move[1];

                        if (state.board.pieceAt(y, x, state.player.other())) {
                            King newKing = new King(piece.player, y, x);
                            if (isMoveLegal(state, piece, newKing)) {
                                State newMove = state.next(piece, newKing);
                                long key = newMove.hashCode();

                                if (!visitedStates.containsKey(key)) {
                                    children.add(newMove);
                                    visitedStates.put(key, newMove);
                                }
                            }
                        }

                        int [][] kingSurroundingSquares = generateQueenMoves();
                        for (int[] square : kingSurroundingSquares) {
                            int kingY = state.board.getKing(state.player.other()).file  + square[0];
                            int kingX = state.board.getKing(state.player.other()).rank + square[1];

                            if (state.board.pieceAt(kingY, kingX, state.player.other())) {
                                King newKing = new King(piece.player, kingY, kingX);
                                if (isMoveLegal(state, piece, newKing)) {
                                    State newMove = state.next(piece, newKing);
                                    long key = newMove.hashCode();

                                    if (!visitedStates.containsKey(key)) {
                                        children.add(newMove);
                                        visitedStates.put(key, newMove);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        ArrayList<Result> sortedChildren = new ArrayList<>();
        for (State child : children) {
            sortedChildren.add(new Result(child, evaluateState(child)));
        }

        sortedChildren.sort(Comparator.comparingDouble(result -> result.value));

        return sortedChildren;
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

    private double getPieceSquareValue(State state, Piece piece) {
        // Values for square tables copied from
        // https://www.chessprogramming.org/Simplified_Evaluation_Function

        double value = 0;
        boolean maximizingPlayer = piece.player == Player.WHITE;

        if (piece.getClass() == Pawn.class) {
            value += maximizingPlayer ? whitePawnPieceSquareTable[piece.file][piece.rank]
                    : blackPawnPieceSquareTable[piece.file][piece.rank];
        }
        if (piece.getClass() == Knight.class) {
            value += maximizingPlayer ? whiteKnightPieceSquareTable[piece.file][piece.rank]
                    : blackKnightPieceSquareTable[piece.file][piece.rank];
        }
        if (piece.getClass() == Bishop.class) {
            value += maximizingPlayer ? whiteBishopPieceSquareTable[piece.file][piece.rank]
                    : blackBishopPieceSquareTable[piece.file][piece.rank];
        }
        if (piece.getClass() == Rook.class) {
            value += maximizingPlayer ? whiteRookPieceSquareTable[piece.file][piece.rank]
                    : blackRookPieceSquareTable[piece.file][piece.rank];
        }
        if (piece.getClass() == Queen.class) {
            value += maximizingPlayer ? whiteQueenPieceSquareTable[piece.file][piece.rank]
                    : blackQueenPieceSquareTable[piece.file][piece.rank];
        }
        if (piece.getClass() == King.class) {
            GamePhase gamePhase = determineGamePhase(state);
            if (gamePhase == GamePhase.MIDDLE_GAME) {
                value += maximizingPlayer ? whiteKingMiddleGamePieceSquareTable[piece.file][piece.rank]
                        : blackKingMiddleGamePieceSquareTable[piece.file][piece.rank];
            } else {
                value += maximizingPlayer ? whiteKingEndGamePieceSquareTable[piece.file][piece.rank]
                        : blackKingEndGamePieceSquareTable[piece.file][piece.rank];
            }
        }

        if (!maximizingPlayer) {
            value *= -1;
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

    private class Result {
        public State state;
        public double value;

        public Result(State state, double value) {
            this.state = state;
            this.value = value;
        }
    }
}
