package com.rfacad.jjmpc;

import org.junit.Before;
import org.junit.Test;

import com.rfacad.buttons.BState;
import com.rfacad.buttons.ButtonCommand;
import com.rfacad.buttons.ChordCommand;
import com.rfacad.buttons.mapper.ButtonMapper;
import com.rfacad.buttons.MockCmd;


@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class TestChordata
{
	private ButtonMapper bm;
	private MockCmd goal;
	private short S0=0;
	private short S1=1;

	// The tests will cover various combinations of A and B;
	// the goal command will only be triggered when both are pressed
	// before one is released.
	private short BTN_A=100;
	private short BTN_B=101;
	private short BTN_C=102;

	@Before
	public void setup()
	{
		bm=new ButtonMapper();
		goal=new MockCmd();
		ChordCommand chordata=new ChordCommand(3,goal);
		// The stop command will prevent notgoal from being called.
		// notgoal should never ever be called!
		
		// PRESS
		bm.map(BTN_A,0,1,S0,chordata.mkSet(1));
		bm.map(BTN_B,0,1,S0,chordata.mkSet(2));
		
		// RELEASE
		bm.map(BTN_A,1,0,S0,chordata.mkCheck(1));
		bm.map(BTN_B,1,0,S0,chordata.mkCheck(2));
	}
	
	@Test
	public void shouldHandleSingleButtonLock() {
		// There's no reason someone couldn't have a lock based on a single button.
		// Pointless, but no reason to prevent it.
		// This test case will also test the return-false.
		MockCmd stop=new MockCmd(false); // this will return false, stopping the commands
		MockCmd notgoal=new MockCmd();

		ChordCommand chordata=new ChordCommand(1,stop,notgoal);
		bm.map(BTN_C,0,1,S0,chordata.mkSet(1));
		bm.map(BTN_C,1,0,S0,chordata.mkCheck(1));

		stop.assertCalls(0);
		bm.button(BTN_C, S0, S1); // press A
		stop.assertCalls(0);
		bm.button(BTN_C, S1, S0); // release A
		stop.assertCalls(1);
		notgoal.assertCalls(0);
	}
	
	@Test
	public void shouldIgnoreNeitherPressed() {
		// sorta an error case; what happens when you
		// release a button when it isn't pressed?
		goal.assertCalls(0);
		bm.button(BTN_A, S0, S1); // release A
		goal.assertCalls(0);
		bm.button(BTN_B, S0, S1); // release B
		goal.assertCalls(0);
	}

	@Test
	public void shouldIgnoreJustA() {
		goal.assertCalls(0);
		bm.button(BTN_A, S0, S1); // press A
		goal.assertCalls(0);
		bm.button(BTN_A, S1, S0); // release A
		goal.assertCalls(0);
	}

	@Test
	public void shouldIgnoreJustB() {
		goal.assertCalls(0);
		bm.button(BTN_B, S0, S1); // press B
		goal.assertCalls(0);
		bm.button(BTN_B, S1, S0); // release B
		goal.assertCalls(0);
	}

	@Test
	public void shouldAcceptBoth_ABAB() {
		goal.assertCalls(0);
		bm.button(BTN_A, S0, S1); // press A
		goal.assertCalls(0);
		bm.button(BTN_B, S0, S1); // press B
		goal.assertCalls(0);
		bm.button(BTN_A, S1, S0); // release A
		goal.assertCalls(1);
		bm.button(BTN_B, S1, S0); // release B
		goal.assertCalls(1);
	}

	@Test
	public void shouldAcceptBoth_ABBA() {
		goal.assertCalls(0);
		bm.button(BTN_A, S0, S1); // press A
		goal.assertCalls(0);
		bm.button(BTN_B, S0, S1); // press B
		goal.assertCalls(0);
		bm.button(BTN_B, S1, S0); // release B
		goal.assertCalls(1);
		bm.button(BTN_A, S1, S0); // release A
		goal.assertCalls(1);
	}
	
	@Test
	public void shouldAcceptBoth_BABA() {
		goal.assertCalls(0);
		bm.button(BTN_B, S0, S1); // press B
		goal.assertCalls(0);
		bm.button(BTN_A, S0, S1); // press A
		goal.assertCalls(0);
		bm.button(BTN_B, S1, S0); // release B
		goal.assertCalls(1);
		bm.button(BTN_A, S1, S0); // release A
		goal.assertCalls(1);
	}

	@Test
	public void shouldAcceptBoth_BAAB() {
		goal.assertCalls(0);
		bm.button(BTN_B, S0, S1); // press B
		goal.assertCalls(0);
		bm.button(BTN_A, S0, S1); // press A
		goal.assertCalls(0);
		bm.button(BTN_A, S1, S0); // release A
		goal.assertCalls(1);
		bm.button(BTN_B, S1, S0); // release B
		goal.assertCalls(1);
	}
}
