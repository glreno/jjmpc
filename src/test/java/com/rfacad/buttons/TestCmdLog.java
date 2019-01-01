package com.rfacad.buttons;

import org.apache.logging.log4j.Level;
import org.junit.Test;

import com.rfacad.buttons.interfaces.BState;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class TestCmdLog
{
	@Test
	public void shouldHandleNullState() {
		CmdLog cmd1 = CmdLog.info(null);
		cmd1.button(null);

		CmdLog cmd2 = new CmdLog(Level.INFO,"Message,logstate=false but no state",false);
		cmd2.button(null);

		CmdLog cmd3 = new CmdLog(Level.INFO,"Message,logstate=true but no state",true);
		cmd3.button(null);

		CmdLog cmd4 = new CmdLog(Level.INFO,null,false);
		cmd4.button(null);

		CmdLog cmd5 = CmdLog.debug(null);
		cmd5.button(null);
	}
	
	@Test
	public void shouldLogState() {
		CmdLog cmd1 = new CmdLog(Level.INFO,null,true);
		short s0=(short)0;
		BState state=new ButtonState(s0, s0, s0);
		cmd1.button(state);
	}
}
