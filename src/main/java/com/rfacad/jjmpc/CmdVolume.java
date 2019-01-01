package com.rfacad.jjmpc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rfacad.buttons.interfaces.BState;
import com.rfacad.buttons.ButtonCommand;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdVolume extends CmdMpd implements ButtonCommand
{
	private static final Logger log = LogManager.getLogger(CmdVolume.class);
	private int delta;
	public CmdVolume(CmdMpd status,int delta)
	{
		super(status,"nop");
		this.delta=delta;
	}
	public boolean button(BState state)
	{
		Object volstring=state.get("volume");
		if (volstring!=null)
		{
			try {
				int vol=Integer.parseInt(volstring.toString());
				vol += delta;
				setCommand("setvol "+vol);
				return super.button(state);
			}
			catch (NumberFormatException e) {
				log.warn("Bad volume state: {}",volstring);
			}
		}
		return false;
	}

}
