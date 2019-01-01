package com.rfacad.buttons;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rfacad.buttons.interfaces.BState;
import com.rfacad.buttons.mapper.ButtonMapper;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdLog implements ButtonCommand
{
	private static final Logger log = LogManager.getLogger(CmdLog.class);

	private String msg;
	private boolean logstate;
	private Level level;
	
	public static CmdLog info(String s) { return new CmdLog(Level.INFO,s,false);}
	public static CmdLog debug(String s) { return new CmdLog(Level.DEBUG,s,true);}
	
	public CmdLog(Level lvl,String s,boolean state) {
		level=lvl;
		msg=s;
		logstate=state;
	}
	
	public boolean button(BState state)
	{
		if ( state == null && msg == null )
		{
			msg = "null state passed to CmdLog";
		}
		if ( logstate && state != null && state instanceof ButtonState)
		{
			ButtonState bs=(ButtonState)state;
			log.log(level,"ID:"+Integer.toHexString(bs.getButtonId())
			+" Prev:"+bs.getPrevValue()
			+" Value:"+bs.getNewValue()
			+" Shifts:"+ButtonMapper.squash(bs.getStringList(BState.SHIFT)));
		}
		if ( msg != null )
		{
			log.log(level,msg);
		}
		return true;
	}
}

