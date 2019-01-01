package com.rfacad.jjmpc;

import com.rfacad.buttons.interfaces.BState;
import com.rfacad.buttons.ButtonCommand;


@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdPlayPause extends AbstractCmdMpd implements ButtonCommand
{
	public static final String STATE="state";
	public CmdPlayPause(CmdMpd status)
	{
		super(status);
	}
	public boolean button(BState state)
	{
		String playstate=state.getString(STATE);
		String cmdToSend;
		if ( "play".equals(playstate)) {
			cmdToSend="pause";
		}
		else {
			cmdToSend="play";
		}
		return sendCommand(state,cmdToSend);
	}

}
