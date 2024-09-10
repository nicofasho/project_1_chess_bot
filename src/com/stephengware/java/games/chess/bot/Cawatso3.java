
package com.stephengware.java.games.chess.bot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import com.stephengware.java.games.chess.state.*;
import com.stephengware.java.games.chess.bot.Result;

public class Cawatso3 extends Bot {
    private HashSet<Queen> queenCount;
    private HashSet<Rook> rookCount;

    private enum GamePhase {
        MIDDLEGAME, THRESHOLD, ENDGAME;
    }

    public Cawatso3() {
        super("Cawatso3 Bot");
        this.rookCount = new HashSet<Rook>();
        this.queenCount = new HashSet<Queen>();
    }

    @Override
    protected State chooseMove(State root) {
    	
        State bestMove = root;

        for (int depth = 1; depth <= 6; depth++) {
            bestMove = min_max(root, root.player == Player.WHITE, depth);

            if (root.searchLimitReached()) {
                break;
            }
        }

        return bestMove;
    }

    private GamePhase findGamePhase(State state) {
        int whiteQueenCount = 0;
        int blackQueenCount = 0;

        for (Piece piece : state.board) {
            if (piece instanceof Queen) {
                if (piece.player == Player.WHITE) {
                    whiteQueenCount++;
                } else {
                    blackQueenCount++;
                }
            }
        }

        if (whiteQueenCount == 1 && blackQueenCount == 1) {
            return GamePhase.MIDDLEGAME;
        } else if (whiteQueenCount != blackQueenCount) {
            return GamePhase.THRESHOLD;
        } else {
            return GamePhase.ENDGAME;
        }
    
    }

    private State min_max(State state, boolean maximizingPlayer, int depth) {

        return maximizingPlayer ? find_max(state, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY) 
        		: find_min(state, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    private boolean isMoveLegal(State state) {
        if (state.turn <= 1) {
            return true;
        }

        if (state.previous.previous.check && state.check) {
            return false;
        } else {
            return true;
        }
    }

    private double assignCaptureOrCheckScores(State state, boolean maximizingPlayer) {
        double value = 0.0;

        if (state.board.countPieces(maximizingPlayer ? Player.WHITE : Player.BLACK)
                - state.board.countPieces(maximizingPlayer ? Player.BLACK : Player.WHITE) > 0) {
            value += 10.0;
        } else if (state.board.countPieces(maximizingPlayer ? Player.WHITE : Player.BLACK)
                - state.board.countPieces(maximizingPlayer ? Player.BLACK : Player.WHITE) < 0) {
            value -= 10.0;
        } else {
            value = 0.0;
        }

        if (state.check) {
            value += maximizingPlayer ? 20.0 : -20.0;
        }

        if (state.over) {
            value += maximizingPlayer ? 1000.0 : -1000.0;
        }

        return value;
    }

    private State find_max(State state, int depth, double alpha, double beta) {
        if (state.searchLimitReached() || depth == 0 || state.over) {
            return state;
        }
        
        queenCount.clear();
        rookCount.clear();

        double bestValueSoFar = Double.NEGATIVE_INFINITY;

        ArrayList<State> children = new ArrayList<>();
        Iterator<State> iterator = state.next().iterator();

        while (!state.searchLimitReached() && iterator.hasNext()) {
            State move = iterator.next();
//            if (isMoveLegal(move)) {
                children.add(move);
//            }
        }

//        children.sort((a, b) -> Double.compare(assignCaptureOrCheckScores(b, true), assignCaptureOrCheckScores(a, true)));

        State value = state;
        for (State child : children) {
            value = find_min(child, depth - 1, alpha, beta);
            
            bestValueSoFar = Math.max(bestValueSoFar, materialValue(value).value);
            alpha = Math.max(alpha, bestValueSoFar);
            if (beta <= alpha) {
                break;
            }
        }

        return value;
    }

    private State find_min(State state, int depth, double alpha, double beta) {
        if (state.searchLimitReached() || depth == 0 || state.over) {
            return state;
        }
        
        queenCount.clear();
        rookCount.clear();

        double bestSoFar = Double.POSITIVE_INFINITY;

        ArrayList<State> children = new ArrayList<>();
        Iterator<State> iterator = state.next().iterator();

        while (!state.searchLimitReached() && iterator.hasNext()) {
            State move = iterator.next();
//            if (isMoveLegal(move)) {
                children.add(move);
//            }
        }

//        children.sort((a, b) -> Double.compare(assignCaptureOrCheckScores(a, false), assignCaptureOrCheckScores(b, false)));

        State value = state;
        for (State child : children) {
            value = find_max(child, depth - 1, alpha, beta);

            bestSoFar = Math.min(bestSoFar, materialValue(value).value);
            beta = Math.min(beta, bestSoFar);
            if (beta <= alpha) {
                break;
            }
        }

        return value;
    }

    private Result materialValue(State state) {
        GamePhase phase = findGamePhase(state);
        double value = 0.0;

        
    	for (Piece piece : state.board) {
            if (piece.getClass() == Pawn.class) {
                value += calculatePawn(piece, phase);
            } else if (piece.getClass() == Knight.class) {
                value += calculateKnight(piece);
            } else if (piece.getClass() == Bishop.class) {
                value += calculateBishop(state, piece, phase);
            } else if (piece.getClass() == Rook.class) {
                value += calculateRook(piece, phase);
            } else if (piece.getClass() == Queen.class) {
                value += calculateQueen(piece, phase);
            } else if (piece.getClass() == King.class) {
                value += calculateKing(piece);
            } else {
                continue;
            }
    	}
        	
        System.out.println("Value: " + value);
        System.out.println("White pieces: " + state.board.countPieces(Player.WHITE));
        System.out.println("Black pieces: " + state.board.countPieces(Player.BLACK));
        
        
        return new Result(state, value);
    }

    private double calculateKing(Piece piece) {
        return piece.player == Player.WHITE ? 1000.0 : -1000.0;
    }

    private double calculateQueen(Piece piece, GamePhase phase) {
        double value = 0.0;

        queenCount.add((Queen) piece);

        int whiteQueenCount = 0;
        int blackQueenCount = 0;

        Iterator<Queen> iterator = queenCount.iterator();
        while (iterator.hasNext()) {
            Queen queen = iterator.next();
            if (queen.player == Player.WHITE) {
                whiteQueenCount++;
            } else {
                blackQueenCount++;
            }
        }

        switch (phase) {
            case MIDDLEGAME:
            case ENDGAME:
                break;
            case THRESHOLD:
                if (piece.player == Player.WHITE) {
                    value += (whiteQueenCount == 2) ? 8.7 : 9.4;
                } else {
                    value -= (blackQueenCount == 2) ? -8.7 : -9.4;
                }
                break;
        }
        
        System.out.println("Queen value: " + value);

        return value;
    }

    private double calculateRook(Piece piece, GamePhase phase) {
        double value = 0.0;
		if (rookCount.size() < 4) {
			rookCount.add((Rook) piece);
		}
        

        int whiteRookCount = 0;
        int blackRookCount = 0;
        
        System.out.println("Rook count: " + rookCount.size());

        Iterator<Rook> iterator = rookCount.iterator();
        while (iterator.hasNext()) {
            Rook rook = iterator.next();
            if (rook.player == Player.WHITE) {
                whiteRookCount++;
            } else {
                blackRookCount++;
            }
        }

        switch (phase) {
            case MIDDLEGAME:
                if (piece.player == Player.WHITE) {
                    value += (whiteRookCount == 2) ? 4.5 : 4.7;
                } else {
                    value += (blackRookCount == 2) ? -4.5 : -4.7;
                }
                break;
            case THRESHOLD:
                if (piece.player == Player.WHITE) {
                    value += (whiteRookCount == 2) ? 4.9 : 4.8;
                } else {
                    value += (blackRookCount == 2) ? -4.9 : -4.8;
                }
                break;
            case ENDGAME:
                if (piece.player == Player.WHITE) {
                    value += (whiteRookCount == 2) ? 5.0 : 5.3;
                } else {
                    value += (blackRookCount == 2) ? -5.0 : -5.3;
                }
                break;
            default:
                break;
        }
        
        System.out.println("Rook value: " + value);

        return value;
    }

    private double calculateBishop(State state, Piece piece, GamePhase phase) {
        int whiteBishopCount = 0;
        int blackBishopCount = 0;

        double value = 0.0;
        
        for (Piece currentPiece : state.board) {
        	if (currentPiece.getClass() == Bishop.class) {
				if (currentPiece.player == Player.WHITE) {
					whiteBishopCount++;
				} else {
					blackBishopCount++;
				}
        	}
        }

        switch (phase) {
            case MIDDLEGAME:
                if (piece.player == Player.WHITE) {
                    value += (whiteBishopCount == 2) ? 3.6 : 3.3;
                } else {
                    value += (blackBishopCount == 2) ? -3.6 : -3.3;
                }
                break;
            case THRESHOLD:
                if (piece.player == Player.WHITE) {
                    value += (whiteBishopCount == 2) ? 3.7 : 3.3;
                } else {
                    value += (blackBishopCount == 2) ? -3.7 : -3.3;
                }
                break;
            case ENDGAME:
                if (piece.player == Player.WHITE) {
                    value += (whiteBishopCount == 2) ? 3.8 : 3.3;
                } else {
                    value += (blackBishopCount == 2) ? -3.8 : -3.3;
                }
                break;
            default:
                return value;
        }
        
        System.out.println("Bishop value: " + value);

        return value;
    }

    private double calculateKnight(Piece piece) {
    	
    	System.out.println("Knight value: " + (piece.player == Player.WHITE ? 3.2 : -3.2));
    	
        return piece.player == Player.WHITE ? 3.2 : -3.2;
    }

    private double calculatePawn(Piece piece, GamePhase phase) {
        double value = 0.0;

        switch (phase) {
            case MIDDLEGAME:
                switch (piece.rank) {
                    case 0:
                    case 7:
                        value = piece.player == Player.WHITE ? 0.7 : -0.7;
                        break;
                    case 1:
                    case 6:
                        value = piece.player == Player.WHITE ? 0.8 : -0.8;
                        break;
                    case 2:
                    case 5:
                        value = piece.player == Player.WHITE ? 0.95 : -0.95;
                        break;
                    case 3:
                    case 4:
                        value = piece.player == Player.WHITE ? 1.0 : -1.0;
                        break;
                    default:
                        break;
                }
                break;
            case THRESHOLD:
                value = piece.player == Player.WHITE ? 0.9 : -0.9;
                break;
            case ENDGAME:
                value = piece.player == Player.WHITE ? 1.0 : -1.0;
                break;
            default:
                break;
        }
        
        System.out.println("Pawn value: " + value);

        return value;
    }
}