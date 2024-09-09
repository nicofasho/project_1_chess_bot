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

	
	/**
	 * Constructs a new chess bot named "Cawatso3 Bot"
	 */
	public Cawatso3() {
		super("Cawatso3 Bot");
	}
	
	public enum GamePhase {
		MIDDLEGAME, THRESHOLD, ENDGAME;
	}

	@Override
	protected State chooseMove(State root) {
		State bestMove = root;
		
		return bestMove;
	}
	
	private State min_max(State state, boolean maximizingPlayer) {
        
		State bestMove = (maximizingPlayer) ? find_max(state, 10) : find_min(state, 10);
		
		return bestMove;
	}
	
	private State find_max(State state, int depth) {
		
		if (state.searchLimitReached() || state.over || depth == 0)
			return state;
		
		double bestValueSoFar = Double.NEGATIVE_INFINITY;
		
		ArrayList<State> children = new ArrayList<State>();
		
		Iterator<State> iterator = state.next().iterator();
		
		while (!state.searchLimitReached() && iterator.hasNext()) {
			State child = iterator.next();
			children.add(child);
		}
		
		State value = state;
		
		for (State child : children) {
			value = find_min(child, depth - 1);
			value = (materialValue(value) > bestValueSoFar) ? value : state;
			
		}
		return value;
	}
	
	private State find_min(State state, int depth) {
		if (state.searchLimitReached() || state.over || depth == 0)
			return state;
		
        double best = Double.POSITIVE_INFINITY;
        
        ArrayList<State> children = new ArrayList<State>();
        
        Iterator<State> iterator = state.next().iterator();
        
		while (!state.searchLimitReached() && iterator.hasNext()) {
			State child = iterator.next();
			children.add(child);
		}
        
        State value = state;
        
        for (State child : children) {
            value = find_max(child, depth - 1);
            value = (materialValue(value) < best) ? value : state;
        }
        
        return value;
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
