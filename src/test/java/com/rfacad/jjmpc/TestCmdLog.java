package com.rfacad.jjmpc;

import org.junit.Test;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class TestCmdLog
{
	@Test
	public void shouldHandleNullState() {
		CmdLog cmd1 = new CmdLog();
		cmd1.button(null);

		CmdLog cmd2 = new CmdLog("Message,logstate=false but no state");
		cmd2.button(null);

		CmdLog cmd3 = new CmdLog("Message,logstate=true but no state",true);
		cmd3.button(null);

		CmdLog cmd4 = new CmdLog(null,false);
		cmd4.button(null);
	}
	
	@Test
	public void shouldLogState() {
		CmdLog cmd1 = new CmdLog();
		short s0=(short)0;
		BState state=new BState(s0, s0, s0);
		cmd1.button(state);
	}
}
