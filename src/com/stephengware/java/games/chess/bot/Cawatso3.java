package com.stephengware.java.games.chess.bot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.stephengware.java.games.chess.state.*;

import java.util.Iterator;

public class Cawatso3 extends Bot {

    public Cawatso3() {
        super("Cawatso3 Bot");
    }

    private HashMap<Long, Result> visitedStates = new HashMap<>();

    @Override
    protected State chooseMove(State root) {

        visitedStates.clear();

        boolean maximizingPlayer = root.player == Player.WHITE;

        if (root.turn == 0) {
            return openingBook(root);
        }
        List<Result> results = new ArrayList<>();

        for (int depth = 1; depth < 5; depth++) {
            if (root.searchLimitReached()) {
                break;
            }
            Result qResult = min_max_ab(root, depth, SearchType.QUIESCENT);
            if (qResult.state != root) {
                results.add(qResult);
            }

            Result result = min_max_ab(root, depth, SearchType.MINIMAX);
            if (result.state != root) {
                results.add(result);
            }
        }

        results.sort(Comparator.comparingDouble(r -> r.value));

        for (Result r : results) {
            System.out.println("value: " + r.value);
        }

        int position = 1;
        Result bestMove = results.get(results.size() - position);

        if (maximizingPlayer) {
            bestMove = results.get(results.size() - position);
        } else {
            bestMove = results.get(position - 1);
        }

        while (bestMove.state == root) {
            if (position > results.size()) {
                position = 1;
            }

            if (maximizingPlayer) {
                bestMove = results.get(results.size() - position);
            } else {
                bestMove = results.get(position - 1);
            }
            position++;
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

        System.out.println("bestMove.value: " + bestMove.value);

        return bestMove.state;
    }

    private State openingBook(State root) {
        ArrayList<State> whiteOpeningMoves = new ArrayList<>();
        ArrayList<State> blackOpeningMoves = new ArrayList<>();
        ArrayList<Result> blackCalculatedMoves = new ArrayList<>();
        if (root.player == Player.WHITE) {
            State e4 = root.next(root.board.getPieceAt(4, 1), new Pawn(Player.WHITE, 4,
                    3));
            State d4 = root.next(root.board.getPieceAt(3, 1), new Pawn(Player.WHITE, 3,
                    3));
            State c4 = root.next(root.board.getPieceAt(2, 1), new Pawn(Player.WHITE, 2,
                    3));
            whiteOpeningMoves.add(e4);
            whiteOpeningMoves.add(d4);
            whiteOpeningMoves.add(c4);
        } else {
            if (root.board.pieceAt(3, 4, Player.WHITE)) {
                State e6 = root.next(root.board.getPieceAt(4, 6), new Pawn(Player.BLACK, 4, 5));
                State c5 = root.next(root.board.getPieceAt(6, 2), new Pawn(Player.BLACK, 2, 4));
                State c6 = root.next(root.board.getPieceAt(2, 6), new Pawn(Player.BLACK, 2, 5));
                blackOpeningMoves = new ArrayList<>();
                blackOpeningMoves.add(e6);
                blackOpeningMoves.add(c5);
                blackOpeningMoves.add(c6);
            } else if (root.board.pieceAt(3, 3, Player.WHITE)) {
                State d5 = root.next(root.board.getPieceAt(3, 6), new Pawn(Player.BLACK, 3, 4));
                blackOpeningMoves.add(d5);
            } else {
                blackCalculatedMoves = gatherChildren(root);
            }
        }

        if (root.player == Player.WHITE) {
            return whiteOpeningMoves.get((int) (Math.random() * whiteOpeningMoves.size()));
        } else {
            if (blackOpeningMoves.size() > 0) {
                return blackOpeningMoves.get((int) (Math.random() * blackOpeningMoves.size()));
            } else {
                return blackCalculatedMoves.get((int) (Math.random() * blackCalculatedMoves.size())).state;
            }
        }
    }

    private enum SearchType {
        MINIMAX, QUIESCENT
    }

    private Result min_max_ab(State state, int depth, SearchType searchType) {
        // visitedStates.clear();
        if (state.player == Player.WHITE) {
            return max_ab(state, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, searchType);
        } else {
            return min_ab(state, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, searchType);
        }
    }

    private Result max_ab(State state, int depth, double alpha, double beta, SearchType searchType) {
        if (depth == 0 || state.countDescendants() <= 1) {
            long key = state.board.toString().hashCode();
            if (!visitedStates.containsKey(key)) {
                visitedStates.put(key, new Result(state, evaluateState(state)));
            }
            return visitedStates.get(key);
        }

        if (state.over) {
            if (state.check) {
                long key = state.board.toString().hashCode();
                if (!visitedStates.containsKey(key)) {
                    visitedStates.put(key, new Result(state, Double.NEGATIVE_INFINITY));
                }
                return visitedStates.get(key);
            }
        }

        ArrayList<Result> children = new ArrayList<>();

        if (searchType == SearchType.MINIMAX) {
            children = gatherChildren(state);
        } else {
            children = gatherQuiescentChildren(state);
        }

        if (children.size() == 0) {
            long key = state.board.toString().hashCode();
            if (!visitedStates.containsKey(key)) {
                visitedStates.put(key, new Result(state, evaluateState(state)));
            }
            return visitedStates.get(key);
        }

        Result best = children.get(0);

        depth--;
        for (int i = children.size() - 1; i >= 0; i--) {
            Result result = min_ab(children.get(i).state, depth, alpha, beta, searchType);

            if (result.value > best.value) {
                best = result;
            }

            if (best.value >= beta) {
                return best;
            }

            alpha = Math.max(alpha, best.value);
        }
        return best;
    }

    private Result min_ab(State state, int depth, double alpha, double beta, SearchType searchType) {
        if (depth == 0 || state.countDescendants() <= 1) {
            long key = state.board.toString().hashCode();
            if (!visitedStates.containsKey(key)) {
                visitedStates.put(key, new Result(state, evaluateState(state)));
            }
            return visitedStates.get(key);
        }

        if (state.over) {
            if (state.check) {
                long key = state.board.toString().hashCode();
                if (!visitedStates.containsKey(key)) {
                    visitedStates.put(key, new Result(state, Double.POSITIVE_INFINITY));
                }
                return visitedStates.get(key);
            }
        }

        ArrayList<Result> children = new ArrayList<>();

        if (searchType == SearchType.MINIMAX) {
            children = gatherChildren(state);
        } else {
            children = gatherQuiescentChildren(state);
        }

        if (children.size() == 0) {
            long key = state.board.toString().hashCode();
            if (!visitedStates.containsKey(key)) {
                visitedStates.put(key, new Result(state, evaluateState(state)));
            }
            return visitedStates.get(key);
        }

        Result best = children.get(0);

        depth--;
        for (int i = 0; i < children.size(); i++) {
            Result result = max_ab(children.get(i).state, depth, alpha, beta, searchType);

            if (result.value < best.value) {
                best = result;
            }

            if (best.value <= alpha) {
                return best;
            }

            beta = Math.min(beta, best.value);
        }
        return best;
    }

    private double evaluateState(State state) {
        double value = 0;

        // value += kingSafetyValue(state);
        // value += squareControl(state);

        for (Piece piece : state.board) {
            value += rawMaterialValue(piece, state);
            // value += getPieceSquareValue(state, piece);
            value += mobilityValue(state, piece);

        }

        if (state.check) {
            if (state.player == Player.WHITE) {
                value -= 100;
            } else {
                value += 100;
            }
        }

        return value;
    }

    private double squareControl(State state) {
        double value = 0;

        for (int r = 0; r < 8; r++) {
            for (int f = 0; f < 8; f++) {
                for (Piece piece : state.board) {
                    if (piece.getClass() == Pawn.class) {
                        if (isMoveLegal(state, piece, new Pawn(piece.player, f, r))) {
                            value += piece.player == Player.WHITE ? 10 : -10;
                        }
                    }
                    if (piece.getClass() == Knight.class) {
                        if (isMoveLegal(state, piece, new Knight(piece.player, f, r))) {
                            value += piece.player == Player.WHITE ? 30 : -30;
                        }
                    }
                    if (piece.getClass() == Bishop.class) {
                        if (isMoveLegal(state, piece, new Bishop(piece.player, f, r))) {
                            value += piece.player == Player.WHITE ? 30 : -30;
                        }
                    }
                    if (piece.getClass() == Rook.class) {
                        if (isMoveLegal(state, piece, new Rook(piece.player, f, r))) {
                            value += piece.player == Player.WHITE ? 50 : -50;
                        }
                    }
                    if (piece.getClass() == Queen.class) {
                        if (isMoveLegal(state, piece, new Queen(piece.player, f, r))) {
                            value += piece.player == Player.WHITE ? 90 : -90;
                        }
                    }
                    // if (piece.getClass() == King.class) {
                    // if (isMoveLegal(state, piece, new King(piece.player, f, r))) {
                    // value += piece.player == Player.WHITE ? 10 : -10;
                    // }
                    // }
                }
            }
        }

        return value;
    }

    private double mobilityValue(State state, Piece piece) {
        double value = 0;

        boolean maximizingPlayer = piece.player == Player.WHITE;

        // Iterator<State> potentialMoves = state.next(piece).iterator();
        // while (potentialMoves.hasNext() && !state.searchLimitReached()) {
        // value += 10;
        // potentialMoves.next();
        // }

        if (piece.getClass() == Pawn.class) {
            int[][] pawnMoves = generatePawnMoves(piece);
            for (int[] move : pawnMoves) {
                int x = piece.file + move[0];
                int y = piece.rank + move[1];

                Pawn newPawn = new Pawn(piece.player, x, y);
                if (isMoveLegal(state, piece, newPawn)) {
                    value += 10;
                }
            }
        }

        if (piece.getClass() == Knight.class) {
            int[][] knightMoves = generateKnightMoves();

            for (int[] move : knightMoves) {
                int x = piece.file + move[0];
                int y = piece.rank + move[1];

                Knight newKnight = new Knight(piece.player, x, y);
                if (isMoveLegal(state, piece, newKnight)) {
                    value += 10;
                }
            }
        }

        if (piece.getClass() == Bishop.class) {
            int[][] bishopMoves = generateBishopMoves();

            for (int i = 1; i < 8; i++) {
                for (int[] move : bishopMoves) {
                    int x = piece.file + move[0] * i;
                    int y = piece.rank + move[1] * i;

                    Bishop newBishop = new Bishop(piece.player, x, y);
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
                    int x = piece.file + move[0] * i;
                    int y = piece.rank + move[1] * i;

                    Rook newRook = new Rook(piece.player, x, y);
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
                    int x = piece.file + move[0] * i;
                    int y = piece.rank + move[1] * i;

                    Queen newQueen = new Queen(piece.player, x, y);
                    if (isMoveLegal(state, piece, newQueen)) {
                        value += 10;
                    }
                }
            }
        }

        if (piece.getClass() == King.class) {
            int[][] kingMoves = generateQueenMoves();

            for (int[] move : kingMoves) {
                int x = piece.file + move[0];
                int y = piece.rank + move[1];

                King newKing = new King(piece.player, x, y);
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

    private class Position {
        int file;
        int rank;

        public Position(int file, int rank) {
            this.file = file;
            this.rank = rank;
        }
    }

    private List<Position> getSafetyZone(Position king) {
        List<Position> safetyZone = new ArrayList<>();
        int[][] surroundingSquares = generateQueenMoves();

        for (int[] square : surroundingSquares) {
            int x = king.file + square[0];
            int y = king.rank + square[1];

            safetyZone.add(new Position(x, y));
        }

        return safetyZone;
    }

    private double kingSafetyValue(State state) {
        double value = 0;

        King whiteKing = state.board.getKing(Player.WHITE);
        King blackKing = state.board.getKing(Player.BLACK);

        List<Position> whiteSafetyZone = getSafetyZone(new Position(whiteKing.file, whiteKing.rank));
        List<Position> blackSafetyZone = getSafetyZone(new Position(blackKing.file, blackKing.rank));

        double whiteSafetyValue = 0;
        double blackSafetyValue = 0;

        for (Position position : whiteSafetyZone) {
            if (state.board.pieceAt(position.file, position.rank, Player.WHITE, Pawn.class)) {
                whiteSafetyValue += 50;
            } else if (state.board.pieceAt(position.file, position.rank, Player.WHITE)) {
                whiteSafetyValue += 10;
            } else if (state.board.pieceAt(position.file, position.rank, Player.BLACK)) {
                whiteSafetyValue -= 50;
            }
        }

        for (Position position : blackSafetyZone) {
            if (state.board.pieceAt(position.file, position.rank, Player.BLACK, Pawn.class)) {
                blackSafetyValue += 50;
            } else if (state.board.pieceAt(position.file, position.rank, Player.BLACK)) {
                blackSafetyValue += 10;
            } else if (state.board.pieceAt(position.file, position.rank, Player.WHITE)) {
                blackSafetyValue -= 50;
            }
        }

        value += whiteSafetyValue;
        value -= blackSafetyValue;

        return value;
    }

    private double rawMaterialValue(Piece piece, State state) {
        double value = 0;

        boolean maximizingPlayer = piece.player == Player.WHITE;

        if (piece.getClass() == Pawn.class) {
            switch (determineGamePhase(state)) {
                case MIDDLEGAME:
                    if (piece.file == 3 || piece.file == 4) {
                        value += 100;
                    } else if (piece.file == 2 || piece.file == 5) {
                        value += 95;
                    } else if (piece.file == 1 || piece.file == 6) {
                        value += 85;
                    } else if (piece.file == 0 || piece.file == 7) {
                        value += 70;
                    }
                    break;
                case THRESHOLD:
                    value += 90;
                    break;
                case ENDGAME:
                    value += 100;
                    break;
            }
        } else if (piece.getClass() == Knight.class) {
            switch (determineGamePhase(state)) {
                case MIDDLEGAME:
                    value += 320;
                    break;
                case THRESHOLD:
                    value += 320;
                    break;
                case ENDGAME:
                    value += 320;
                    break;
            }
        } else if (piece.getClass() == Bishop.class) {
            boolean pairedBishops = countBishops(piece, state);
            switch (determineGamePhase(state)) {
                case MIDDLEGAME:
                    value += 330;
                    if (pairedBishops) {
                        value += 15;
                    }
                    break;
                case THRESHOLD:
                    value += 340;
                    if (pairedBishops) {
                        value += 20;
                    }
                    break;
                case ENDGAME:
                    value += 330;
                    if (pairedBishops) {
                        value += 25;
                    }
                    break;
            }
        } else if (piece.getClass() == Rook.class) {
            switch (determineGamePhase(state)) {
                case MIDDLEGAME:
                    value += 460;
                    break;
                case THRESHOLD:
                    value += 485;
                    break;
                case ENDGAME:
                    value += 515;
                    break;
            }
        } else if (piece.getClass() == Queen.class) {
            switch (determineGamePhase(state)) {
                case MIDDLEGAME:
                    value += 900;
                    break;
                case THRESHOLD:
                    value += 910;
                    break;
                case ENDGAME:
                    value += 900;
                    break;
            }
        } else if (piece.getClass() == King.class) {
            switch (determineGamePhase(state)) {
                case MIDDLEGAME:
                    value += 20000;
                    break;
                case THRESHOLD:
                    value += 20000;
                    break;
                case ENDGAME:
                    value += 20000;
                    break;
            }
        }

        if (!maximizingPlayer) {
            value *= -1;
        }

        return value;
    }

    private boolean countBishops(Piece piece, State state) {
        int count = 0;
        for (Piece p : state.board) {
            if (p.getClass() == Bishop.class) {
                if (p.player == piece.player) {
                    count++;
                }
            }
        }
        return count == 2;
    }

    private ArrayList<Result> gatherChildren(State state) {

        ArrayList<Result> children = new ArrayList<>();

        Iterator<State> it = state.next().iterator();

        while (it.hasNext() && !state.searchLimitReached()) {
            State newState = it.next();
            while (newState == state) {
                if (it.hasNext()) {
                    newState = it.next();
                } else {
                    break;
                }
            }
            long key = newState.board.hashCode();

            if (!visitedStates.containsKey(key)) {
                visitedStates.put(key, new Result(newState, evaluateState(newState)));
            }
            children.add(visitedStates.get(key));
        }

        // for (Piece piece : state.board) {
        // if (piece.player == state.player) {
        // if (piece.getClass() == Pawn.class) {
        // int[][] pawnMoves = generatePawnMoves(piece);
        // for (int[] move : pawnMoves) {
        // int x = piece.file + move[0];
        // int y = piece.rank + move[1];

        // Pawn newPawn = new Pawn(piece.player, x, y);
        // if (isMoveLegal(state, piece, newPawn)) {
        // State newMove = state.next(piece, newPawn);
        // long key = newMove.board.toString().hashCode();

        // if (!visitedStates.containsKey(key)) {
        // visitedStates.put(key, new Result(newMove, evaluateState(newMove)));
        // }
        // children.add(visitedStates.get(key));

        // }
        // }
        // }

        // if (piece.getClass() == Knight.class) {
        // int[][] knightMoves = generateKnightMoves();

        // for (int[] move : knightMoves) {
        // int x = piece.file + move[0];
        // int y = piece.rank + move[1];
        // if (x < 0 || x > 7 || y < 0 || y > 7) {
        // continue;
        // }

        // Knight newKnight = new Knight(piece.player, x, y);
        // if (isMoveLegal(state, piece, newKnight)) {
        // State newMove = state.next(piece, newKnight);
        // long key = newMove.board.toString().hashCode();

        // if (!visitedStates.containsKey(key)) {
        // visitedStates.put(key, new Result(newMove, evaluateState(newMove)));
        // }
        // children.add(visitedStates.get(key));
        // }
        // }
        // }

        // if (piece.getClass() == Bishop.class) {
        // int[][] bishopMoves = generateBishopMoves();

        // for (int i = 1; i < 8; i++) {
        // for (int[] move : bishopMoves) {
        // int x = piece.file + move[0] * i;
        // int y = piece.rank + move[1] * i;
        // if (x < 0 || x > 7 || y < 0 || y > 7) {
        // continue;
        // }

        // Bishop newBishop = new Bishop(piece.player, x, y);
        // if (isMoveLegal(state, piece, newBishop)) {
        // State newMove = state.next(piece, newBishop);
        // long key = newMove.board.toString().hashCode();

        // if (!visitedStates.containsKey(key)) {
        // visitedStates.put(key, new Result(newMove, evaluateState(newMove)));
        // }
        // children.add(visitedStates.get(key));
        // }
        // }
        // }
        // }

        // if (piece.getClass() == Rook.class) {
        // int[][] rookMoves = generateRookMoves();

        // for (int i = 1; i < 8; i++) {
        // for (int[] move : rookMoves) {
        // int x = piece.file + move[0] * i;
        // int y = piece.rank + move[1] * i;
        // if (x < 0 || x > 7 || y < 0 || y > 7) {
        // continue;
        // }

        // Rook newRook = new Rook(piece.player, x, y);
        // if (isMoveLegal(state, piece, newRook)) {
        // State newMove = state.next(piece, newRook);
        // long key = newMove.board.toString().hashCode();

        // if (!visitedStates.containsKey(key)) {
        // visitedStates.put(key, new Result(newMove, evaluateState(newMove)));
        // }
        // children.add(visitedStates.get(key));
        // }
        // }
        // }
        // }

        // if (piece.getClass() == Queen.class) {
        // int[][] queenMoves = generateQueenMoves();

        // for (int i = 1; i < 8; i++) {
        // for (int[] move : queenMoves) {
        // int x = piece.file + move[0] * i;
        // int y = piece.rank + move[1] * i;
        // if (x < 0 || x > 7 || y < 0 || y > 7) {
        // continue;
        // }

        // Queen newQueen = new Queen(piece.player, x, y);
        // if (isMoveLegal(state, piece, newQueen)) {
        // State newMove = state.next(piece, newQueen);
        // long key = newMove.board.toString().hashCode();

        // if (!visitedStates.containsKey(key)) {
        // visitedStates.put(key, new Result(newMove, evaluateState(newMove)));
        // }
        // children.add(visitedStates.get(key));
        // }
        // }
        // }
        // }

        // if (piece.getClass() == King.class) {
        // int[][] kingMoves = generateQueenMoves();

        // for (int[] move : kingMoves) {
        // int x = piece.file + move[0];
        // int y = piece.rank + move[1];
        // if (x < 0 || x > 7 || y < 0 || y > 7) {
        // continue;
        // }

        // King newKing = new King(piece.player, x, y);
        // if (isMoveLegal(state, piece, newKing)) {
        // State newMove = state.next(piece, newKing);
        // long key = newMove.board.toString().hashCode();

        // if (!visitedStates.containsKey(key)) {
        // visitedStates.put(key, new Result(newMove, evaluateState(newMove)));
        // }
        // children.add(visitedStates.get(key));
        // }
        // }
        // }
        // }
        // }

        children.sort(Comparator.comparingDouble(result -> result.value));

        return children;
    }

    private ArrayList<Result> gatherQuiescentChildren(State state) {
        ArrayList<Result> children = new ArrayList<>();

        for (Piece piece : state.board) {
            Iterator<State> it = state.next(piece).iterator();
            while (it.hasNext() && !state.searchLimitReached()) {
                State newState = it.next();
                if (newState.board.countPieces() == newState.previous.board.countPieces() -
                        1) {
                    // if (state.player == Player.WHITE) {
                    //     if (value > 250) {
                    //         value *= 1.1;
                    //     }
                    // } else {
                    //     if (value < -250) {
                    //         value *= 1.1;
                    //     }
                    // }

                    long key = newState.board.toString().hashCode();

                    if (!visitedStates.containsKey(key)) {
                        visitedStates.put(key, new Result(newState, evaluateState(newState)));
                    }
                    children.add(visitedStates.get(key));
                }
            }

            // if (piece.player == state.player) {
            // if (piece.getClass() == Pawn.class) {
            // int[][] pawnMoves = generatePawnMoves(piece);
            // for (int[] move : pawnMoves) {
            // int x = piece.file + move[0];
            // int y = piece.rank + move[1];

            // if (state.board.pieceAt(x, y, state.player.other())) {
            // Pawn newPawn = new Pawn(piece.player, x, y);
            // if (isMoveLegal(state, piece, newPawn)) {
            // State newMove = state.next(piece, newPawn);
            // long key = newMove.board.toString().hashCode();

            // if (!visitedStates.containsKey(key)) {
            // visitedStates.put(key, new Result(newMove, evaluateState(newMove)));
            // }
            // children.add(visitedStates.get(key));

            // }
            // }

            // int[][] kingSurroundingSquares = generateQueenMoves();
            // for (int[] square : kingSurroundingSquares) {
            // int kingX = state.board.getKing(state.player.other()).file + square[0];
            // int kingY = state.board.getKing(state.player.other()).rank + square[1];

            // if (state.board.pieceAt(kingX, kingY, state.player.other())) {
            // Pawn newPawn = new Pawn(piece.player, kingX, kingY);
            // if (isMoveLegal(state, piece, newPawn)) {
            // State newMove = state.next(piece, newPawn);
            // long key = newMove.board.toString().hashCode();

            // if (!visitedStates.containsKey(key)) {
            // visitedStates.put(key, new Result(newMove, evaluateState(newMove)));
            // }
            // children.add(visitedStates.get(key));
            // }
            // }
            // }

            // }
            // }

            // if (piece.getClass() == Knight.class) {
            // int[][] knightMoves = generateKnightMoves();

            // for (int[] move : knightMoves) {
            // int x = piece.file + move[0];
            // int y = piece.rank + move[1];
            // if (x < 0 || x > 7 || y < 0 || y > 7) {
            // continue;
            // }

            // if (state.board.pieceAt(x, y, state.player.other())) {
            // Knight newKnight = new Knight(piece.player, x, y);
            // if (isMoveLegal(state, piece, newKnight)) {
            // State newMove = state.next(piece, newKnight);
            // long key = newMove.board.toString().hashCode();

            // if (!visitedStates.containsKey(key)) {
            // visitedStates.put(key, new Result(newMove, evaluateState(newMove)));
            // }
            // children.add(visitedStates.get(key));
            // }
            // }

            // int[][] kingSurroundingSquares = generateQueenMoves();
            // for (int[] square : kingSurroundingSquares) {
            // int kingX = state.board.getKing(state.player.other()).file + square[0];
            // int kingY = state.board.getKing(state.player.other()).rank + square[1];

            // if (state.board.pieceAt(kingX, kingY, state.player.other())) {
            // Knight newKnight = new Knight(piece.player, kingX, kingY);
            // if (isMoveLegal(state, piece, newKnight)) {
            // State newMove = state.next(piece, newKnight);
            // long key = newMove.board.toString().hashCode();

            // if (!visitedStates.containsKey(key)) {
            // visitedStates.put(key, new Result(newMove, evaluateState(newMove)));
            // }
            // children.add(visitedStates.get(key));
            // }
            // }
            // }

            // }
            // }

            // if (piece.getClass() == Bishop.class) {
            // int[][] bishopMoves = generateBishopMoves();

            // for (int i = 1; i < 8; i++) {
            // for (int[] move : bishopMoves) {
            // int x = piece.file + move[0] * i;
            // int y = piece.rank + move[1] * i;
            // if (x < 0 || x > 7 || y < 0 || y > 7) {
            // continue;
            // }

            // if (state.board.pieceAt(x, y, state.player.other())) {
            // Bishop newBishop = new Bishop(piece.player, x, y);
            // if (isMoveLegal(state, piece, newBishop)) {
            // State newMove = state.next(piece, newBishop);
            // long key = newMove.board.toString().hashCode();

            // if (!visitedStates.containsKey(key)) {
            // visitedStates.put(key, new Result(newMove, evaluateState(newMove)));
            // }
            // children.add(visitedStates.get(key));
            // }
            // }

            // int[][] kingSurroundingSquares = generateQueenMoves();
            // for (int[] square : kingSurroundingSquares) {
            // int kingX = state.board.getKing(state.player.other()).file + square[0];
            // int kingY = state.board.getKing(state.player.other()).rank + square[1];

            // if (state.board.pieceAt(kingX, kingY, state.player.other())) {
            // Bishop newBishop = new Bishop(piece.player, kingX, kingY);
            // if (isMoveLegal(state, piece, newBishop)) {
            // State newMove = state.next(piece, newBishop);
            // long key = newMove.board.toString().hashCode();

            // if (!visitedStates.containsKey(key)) {
            // visitedStates.put(key, new Result(newMove, evaluateState(newMove)));
            // }
            // children.add(visitedStates.get(key));
            // }
            // }
            // }
            // }
            // }
            // }

            // if (piece.getClass() == Rook.class) {
            // int[][] rookMoves = generateRookMoves();

            // for (int i = 1; i < 8; i++) {
            // for (int[] move : rookMoves) {
            // int x = piece.file + move[0] * i;
            // int y = piece.rank + move[1] * i;
            // if (x < 0 || x > 7 || y < 0 || y > 7) {
            // continue;
            // }

            // if (state.board.pieceAt(x, y, state.player.other())) {
            // Rook newRook = new Rook(piece.player, x, y);
            // if (isMoveLegal(state, piece, newRook)) {
            // State newMove = state.next(piece, newRook);
            // long key = newMove.board.toString().hashCode();

            // if (!visitedStates.containsKey(key)) {
            // visitedStates.put(key, new Result(newMove, evaluateState(newMove)));
            // }
            // children.add(visitedStates.get(key));
            // }
            // }

            // int[][] kingSurroundingSquares = generateQueenMoves();
            // for (int[] square : kingSurroundingSquares) {
            // int kingX = state.board.getKing(state.player.other()).file + square[0];
            // int kingY = state.board.getKing(state.player.other()).rank + square[1];

            // if (state.board.pieceAt(kingX, kingY, state.player.other())) {
            // Rook newRook = new Rook(piece.player, kingX, kingY);
            // if (isMoveLegal(state, piece, newRook)) {
            // State newMove = state.next(piece, newRook);
            // long key = newMove.board.toString().hashCode();

            // if (!visitedStates.containsKey(key)) {
            // visitedStates.put(key, new Result(newMove, evaluateState(newMove)));
            // }
            // children.add(visitedStates.get(key));
            // }
            // }
            // }
            // }
            // }
            // }

            // if (piece.getClass() == Queen.class) {
            // int[][] queenMoves = generateQueenMoves();

            // for (int i = 1; i < 8; i++) {
            // for (int[] move : queenMoves) {
            // int x = piece.file + move[0] * i;
            // int y = piece.rank + move[1] * i;
            // if (x < 0 || x > 7 || y < 0 || y > 7) {
            // continue;
            // }

            // if (state.board.pieceAt(x, y, state.player.other())) {
            // Queen newQueen = new Queen(piece.player, x, y);
            // if (isMoveLegal(state, piece, newQueen)) {
            // State newMove = state.next(piece, newQueen);
            // long key = newMove.board.toString().hashCode();

            // if (!visitedStates.containsKey(key)) {
            // visitedStates.put(key, new Result(newMove, evaluateState(newMove)));
            // }
            // children.add(visitedStates.get(key));
            // }
            // }

            // int[][] kingSurroundingSquares = generateQueenMoves();
            // for (int[] square : kingSurroundingSquares) {
            // int kingX = state.board.getKing(state.player.other()).file + square[0];
            // int kingY = state.board.getKing(state.player.other()).rank + square[1];

            // if (state.board.pieceAt(kingX, kingY, state.player.other())) {
            // Queen newQueen = new Queen(piece.player, kingX, kingY);
            // if (isMoveLegal(state, piece, newQueen)) {
            // State newMove = state.next(piece, newQueen);
            // long key = newMove.board.toString().hashCode();

            // if (!visitedStates.containsKey(key)) {
            // visitedStates.put(key, new Result(newMove, evaluateState(newMove)));
            // }
            // children.add(visitedStates.get(key));
            // }
            // }
            // }
            // }
            // }
            // }

            // if (piece.getClass() == King.class) {
            // int[][] kingMoves = generateQueenMoves();

            // for (int[] move : kingMoves) {
            // int x = piece.file + move[0];
            // int y = piece.rank + move[1];
            // if (x < 0 || x > 7 || y < 0 || y > 7) {
            // continue;
            // }

            // if (state.board.pieceAt(x, y, state.player.other())) {
            // King newKing = new King(piece.player, x, y);
            // if (isMoveLegal(state, piece, newKing)) {
            // State newMove = state.next(piece, newKing);
            // long key = newMove.board.toString().hashCode();

            // if (!visitedStates.containsKey(key)) {
            // visitedStates.put(key, new Result(newMove, evaluateState(newMove)));
            // }
            // children.add(visitedStates.get(key));
            // }
            // }

            // int[][] kingSurroundingSquares = generateQueenMoves();
            // for (int[] square : kingSurroundingSquares) {
            // int kingX = state.board.getKing(state.player.other()).file + square[0];
            // int kingY = state.board.getKing(state.player.other()).rank + square[1];

            // if (state.board.pieceAt(kingX, kingY, state.player.other())) {
            // King newKing = new King(piece.player, kingX, kingY);
            // if (isMoveLegal(state, piece, newKing)) {
            // State newMove = state.next(piece, newKing);
            // long key = newMove.board.toString().hashCode();

            // if (!visitedStates.containsKey(key)) {
            // visitedStates.put(key, new Result(newMove, evaluateState(newMove)));
            // }
            // children.add(visitedStates.get(key));
            // }
            // }
            // }
        }

        children.sort(Comparator.comparingDouble(result -> result.value));

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

    private double getPieceSquareValue(State state, Piece piece) {
        // Values for square tables copied from
        // https://www.chessprogramming.org/Simplified_Evaluation_Function

        double value = 0;
        boolean maximizingPlayer = piece.player == Player.WHITE;

        if (piece.getClass() == Pawn.class) {
            value += maximizingPlayer ? whitePawnPieceSquareTable[piece.rank][piece.file]
                    : blackPawnPieceSquareTable[piece.rank][piece.file];
        }
        if (piece.getClass() == Knight.class) {
            value += maximizingPlayer ? whiteKnightPieceSquareTable[piece.rank][piece.file]
                    : blackKnightPieceSquareTable[piece.rank][piece.file];
        }
        if (piece.getClass() == Bishop.class) {
            value += maximizingPlayer ? whiteBishopPieceSquareTable[piece.rank][piece.file]
                    : blackBishopPieceSquareTable[piece.rank][piece.file];
        }
        if (piece.getClass() == Rook.class) {
            value += maximizingPlayer ? whiteRookPieceSquareTable[piece.rank][piece.file]
                    : blackRookPieceSquareTable[piece.rank][piece.file];
        }
        if (piece.getClass() == Queen.class) {
            value += maximizingPlayer ? whiteQueenPieceSquareTable[piece.rank][piece.file]
                    : blackQueenPieceSquareTable[piece.rank][piece.file];
        }
        if (piece.getClass() == King.class) {
            GamePhase gamePhase = determineGamePhase(state);
            if (gamePhase == GamePhase.MIDDLEGAME) {
                value += maximizingPlayer ? whiteKingMiddleGamePieceSquareTable[piece.rank][piece.file]
                        : blackKingMiddleGamePieceSquareTable[piece.rank][piece.file];
            } else {
                value += maximizingPlayer ? whiteKingEndGamePieceSquareTable[piece.rank][piece.file]
                        : blackKingEndGamePieceSquareTable[piece.rank][piece.file];
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
        MIDDLEGAME, THRESHOLD, ENDGAME
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

        boolean whiteThreshold = (whiteQueenCount == 1 && whiteOtherPiecesCount > 1 && whiteOtherPiecesCount <= 3);
        boolean blackThreshold = (blackQueenCount == 1 && blackOtherPiecesCount > 1 && blackOtherPiecesCount <= 3);

        if (whiteEndGame && blackEndGame) {
            return GamePhase.ENDGAME;
        } else if (whiteThreshold || blackThreshold) {
            return GamePhase.THRESHOLD;
        } else {
            return GamePhase.MIDDLEGAME;
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
