package com.rfacad.jjmpc;

import com.rfacad.buttons.interfaces.BState;
import com.rfacad.buttons.ButtonCommand;


@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdTrackPrev extends CmdMpd implements ButtonCommand
{
	public CmdTrackPrev(CmdMpd status)
	{
		super(status,"prev");
	}
	public boolean button(BState state)
	{
		Object playstate=state.get("state");
		return super.button(state);
	}
}
