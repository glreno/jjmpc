package com.rfacad.jjmpc;

import com.rfacad.buttons.interfaces.BState;
import com.rfacad.buttons.ButtonCommand;


@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdPlayPause extends CmdMpd implements ButtonCommand
{
	public CmdPlayPause(CmdMpd status)
	{
		super(status,"play");
	}
	public boolean button(BState state)
	{
		Object playstate=state.get("state");
		if ( "play".equals(playstate)) {
			setCommand("pause");
		}
		else {
			setCommand("play");
		}
		return super.button(state);
	}

}
