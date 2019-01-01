package com.rfacad.jjmpc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rfacad.buttons.BState;
import com.rfacad.buttons.ButtonCommand;
import com.rfacad.buttons.ButtonMapper;


@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdLog implements ButtonCommand
{
	private static final Logger log = LogManager.getLogger(CmdLog.class);

	private String msg;
	private boolean logstate;
	public CmdLog() { msg=null;logstate=true;}
	public CmdLog(String s) { msg=s;logstate=false;}
	public CmdLog(String s,boolean state) { msg=s;logstate=state;}
	public boolean button(BState state)
	{
		if ( state == null && msg == null )
		{
			msg = "null state passed to CmdLog";
		}
		if ( logstate && state != null )
		{
			log.info("ID:"+Integer.toHexString(state.getButtonId())
			+" Prev:"+state.getPrevValue()
			+" Value:"+state.getNewValue()
			+" Shifts:"+state.get(ButtonMapper.SHIFT));
		}
		if ( msg != null )
		{
			log.info(msg);
		}
		return true;
	}
}

