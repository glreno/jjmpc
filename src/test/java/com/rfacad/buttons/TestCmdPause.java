package com.rfacad.jjmpc;

import static org.junit.Assert.*;

import org.junit.Test;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class TestCmdPause
{
	@Test
	public void shouldPause() {
		CmdPause cmd1 = new CmdPause(1000);
		long before=System.currentTimeMillis();
		cmd1.button(null);
		long after=System.currentTimeMillis();
		long duration=after-before;
		assertTrue(duration>=1000);
	}
}
