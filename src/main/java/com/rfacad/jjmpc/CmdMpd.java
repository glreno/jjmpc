package com.rfacad.jjmpc;

import java.util.*;
import java.util.concurrent.*;

import com.rfacad.buttons.ButtonCommand;
import com.rfacad.buttons.interfaces.BState;
import com.rfacad.mpd.interfaces.RidiculouslySimpleMPDClientI;


@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdMpd extends AbstractCmdMpd implements ButtonCommand
{
	protected String command;
	public CmdMpd(RidiculouslySimpleMPDClientI md,String cmd)
	{
		super(md);
		command=cmd;
	}
	public CmdMpd(CmdMpd another,String cmd)
	{
		super(another);
		command=cmd;
	}
	public boolean button(BState state)
	{
		return sendCommand(state,command);
	}
	
	protected void setCommand(String s) {
		this.command=s;
	}
	
}

