package com.rfacad.jjmpc;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdLog implements ButtonCommand
{
	private String msg;
	private boolean logstate;
	public CmdLog() { msg=null;logstate=true;}
	public CmdLog(String s) { msg=s;logstate=false;}
	public CmdLog(String s,boolean state) { msg=s;logstate=state;}
	public boolean button(BState state)
	{
		if ( logstate )
		{
			System.out.println("ID:"+Integer.toHexString(state.getButtonId())
			+" Prev:"+state.getPrevValue()
			+" Value:"+state.getNewValue()
			+" Shifts:"+state.get(ButtonMapper.SHIFT));
		}
		if ( msg != null )
		{
			System.out.println(msg);
		}
		return true;
	}
}

