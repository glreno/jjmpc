package com.rfacad.jjmpc;

import com.rfacad.buttons.BState;
import com.rfacad.buttons.ButtonCommand;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdVolume extends CmdMpd implements ButtonCommand
{
	private CmdMpdStatus status;
	private int delta;
	public CmdVolume(CmdMpdStatus status,int delta)
	{
		super(status,"nop");
		this.status=status;
		this.delta=delta;
	}
	public boolean button(BState state)
	{
		String volstring=status.get("volume");
		if (volstring!=null) try {
			int vol=Integer.parseInt(volstring);
			vol += delta;
			setCommand("setvol "+vol);
			return super.button(state);
		}
		catch (NumberFormatException e) {
			System.err.println("Bad volume state: "+volstring);
		}
		return false;
	}

}
