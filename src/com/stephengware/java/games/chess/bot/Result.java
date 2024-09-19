package com.stephengware.java.games.chess.bot;

import com.stephengware.java.games.chess.state.State;

public final class Result {
	public State state;
	public double value;
	
	public Result(State state, double value) {
		this.state = state;
		this.value = value;
	}
}