package com.rfacad.jjmpc;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdPlayPause extends CmdMpd implements ButtonCommand
{
	CmdMpdStatus status;
	public CmdPlayPause(CmdMpdStatus status)
	{
		super(status,"play");
		this.status=status;
	}
	public boolean button(BState state)
	{
		String playstate=status.get("state");
		if ( "play".equals(playstate)) {
			setCommand("pause");
		}
		else {
			setCommand("play");
		}
		return super.button(state);
	}

}
