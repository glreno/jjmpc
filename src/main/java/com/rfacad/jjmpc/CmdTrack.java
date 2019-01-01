package com.rfacad.jjmpc;

import com.rfacad.buttons.interfaces.BState;
import com.rfacad.buttons.ButtonCommand;


@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdTrack extends CmdMpd implements ButtonCommand
{
	private boolean isprev;
	
	public CmdTrack(CmdMpd status,String cmd)
	{
		super(status,cmd);
		this.isprev="previous".equals(cmd);
	}
	public boolean button(BState state)
	{
		Object playstate=state.get("state");
		if ( isprev ) {
			// "previous" doesn't actually work in MPD, because the playlist is consumed as it is played.
			// So to go to the previous track, we need to reload the playlist!
			boolean ok;
			setCommand("clear"); // this does stop playback
			ok=super.button(state);
			setCommand("load "+"hga");
			ok=super.button(state);
			setCommand("play");
			ok=super.button(state);
		}
		
		return super.button(state);
	}
}
