package com.rfacad.jjmpc;

import java.util.*;

/**
* Combine two (or more) button presses so that both buttons must
* be pressed for a command to occur.
* This works as a factory. A single ChordCommand wraps a goal command list.
* But what you give to the ButtonMapper is ChordCommand.mkSet() and
* ChordCommand.mkCheck() -- those two factory methods return ButtonCommands
* that set, check, and clear state in this object. If both mkSet() have been
* called, mkCheck() will pass and run the commands. Otherwise, mkCheck()
* clears the state for its button.
*/
@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class ChordCommand
{
	private List<ButtonCommand> goalCommands;

	// Support for two-button-chord exit command
	private int innerState;
	private int goalState;

	private class setChordState implements ButtonCommand {
		int val;
		public setChordState(int n) { val=n;}
		public boolean button(BState state) {
			innerState|=val;
			return true;
		}
	}
	private class checkChordState implements ButtonCommand {
		int val;
		public checkChordState(int n) { val=n;}
		public boolean button(BState state) {
			if ( innerState==goalState ) {
				return runGoalCommands(state);
			}
			innerState&=~val;
			return true;
		}
	}

	public ChordCommand(int goalFlags,ButtonCommand ... commands) {
		goalState=goalFlags;
		goalCommands = Arrays.asList(commands);
	}

	private boolean runGoalCommands(BState state) {
		for(ButtonCommand cmd : goalCommands) {
			boolean b = cmd.button(state);
			if ( !b ) return false;
		}
		return true;
	}

	public ButtonCommand mkSet(int v) {
		return new setChordState(v);
	}
	public ButtonCommand mkCheck(int v) {
		return new checkChordState(v);
	}
}
