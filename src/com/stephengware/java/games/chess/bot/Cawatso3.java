package com.stephengware.java.games.chess.bot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import com.stephengware.java.games.chess.bot.Bot;
import com.stephengware.java.games.chess.state.Knight;
import com.stephengware.java.games.chess.state.Piece;
import com.stephengware.java.games.chess.state.Player;
import com.stephengware.java.games.chess.state.State;

/**
 * A chess bot
 * 
 * @author Christian Watson
 */
public class Cawatso3 extends Bot {

	/** A random number generator */
	private final Random random;
	
	/**
	 * Constructs a new chess bot named "Cawatso3 Bot"
	 */
	public Cawatso3() {
		super("Cawatso3 Bot");
		this.random = new Random(0);
	}
	
	public enum GamePhase {
		MIDDLEGAME, THRESHOLD, ENDGAME;
	}

	@Override
	protected State chooseMove(State root) {
//		// This list will hold all the children nodes of the root.
//		ArrayList<State> children = new ArrayList<>();
//		// Generate all the children nodes of the root (that is, all the
//		// possible next states of the game.  Make sure that we do not exceed
//		// the number of GameTree nodes that we are allowed to generate.
//		Iterator<State> iterator = root.next().iterator();
//		while(!root.searchLimitReached() && iterator.hasNext())
//			children.add(iterator.next());
//		// Choose one of the children at random.
//		return children.get(random.nextInt(children.size()));
		
		State bestMove = null;
		double bestScore = root.player == Player.WHITE ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
		
		for (int depth = 1; depth <= 10; depth++) {
			if (root.searchLimitReached()) {
				break;
			}
			
			for (State state : root.next()) {
				double score = minimax(state, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, root.player == Player.WHITE);
				if (score > bestScore) {
					bestScore = score;
					bestMove = state;
				}
			}
		}
		
		return bestMove;
	}
	
	private double minimax(State state, int depth, double alpha, double beta, boolean maximizingPlayer) {
        if (depth == 0 || state.over) {
            return materialValue(state);
        }
        
        if (maximizingPlayer) {
        	double maxScore = Double.NEGATIVE_INFINITY;
			for (State child : state.next()) {
				double score = minimax(child, depth - 1, alpha, beta, maximizingPlayer);
				maxScore = Math.max(maxScore, score);
				alpha = Math.max(alpha, score);
				if (beta <= alpha) {
					break;
				}
			}
			return maxScore;
        } else {
        	double minScore = Double.POSITIVE_INFINITY;
        	for (State child : state.next()) {
        		double score = minimax(child, depth - 1, alpha, beta, maximizingPlayer);
        		minScore = Math.min(minScore,  score);
        		beta = Math.min(beta,  minScore);
        		if (beta <= alpha) {
        			break;
        		}
        	}
        	return minScore;
        }
	}
	
	private double materialValue (State state) {
		
		for (int rank = 0; rank < 8; rank++) {
			for (int file = 0; file < 8; file++) {
				Piece piece = state.board.getPieceAt(rank, file);
				double value = 0.0;
				
				switch (piece.getClass()) {
				
				case Pawn.class:
					value = calculatePawn(state, piece);
				    break;
				case Knight.class:
					value = calculateKnight(state, piece);
					break;
				case Bishop.class:
					value = calculateBishop(state, piece);
					break;
				case Rook.class:
					value = calculateRook(state, piece);
					break;
				case Queen.class:
					value = calculateQueen(state, piece);
					break;
				case King.class:
					value = calculateKing(state, piece);
					break;
				}
			}
		}
		return value;
	}
	
	private double calculatePawn(State state, Piece piece) {
		
	}
}
