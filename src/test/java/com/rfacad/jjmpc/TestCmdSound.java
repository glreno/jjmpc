package com.rfacad.jjmpc;

import static org.junit.Assert.*;

import org.junit.Test;

import com.rfacad.buttons.BState;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class TestCmdSound
{

	@Test
	public void shouldPlaySound() {
		short s0=(short)0;
		BState state=new BState(s0, s0, s0);
		state.set("foo", "bar");

		CmdSh cmd1 = new CmdSound("%foo%");
		cmd1.setTestMode(true);

		boolean ret;
		ret=cmd1.button(state);
		assertTrue(ret);
		
		String[] submitted=cmd1.getLastRunJob();
		assertEquals(2,submitted.length);
		assertEquals("/usr/bin/aplay",submitted[0]);
		assertEquals("bar",submitted[1]);
	}

	@Test
	public void shouldSayPhrase() {
		short s0=(short)0;
		BState state=new BState(s0, s0, s0);
		state.set("foo", "bar");

		CmdSh cmd1 = new CmdSay("%foo%");
		cmd1.setTestMode(true);

		boolean ret;
		ret=cmd1.button(state);
		assertTrue(ret);
		
		String[] submitted=cmd1.getLastRunJob();
		assertEquals(4,submitted.length);
		assertEquals("/usr/bin/espeak",submitted[0]);
		assertEquals("bar",submitted[3]);
	}

}
