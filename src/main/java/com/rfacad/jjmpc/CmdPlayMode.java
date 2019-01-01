package com.rfacad.jjmpc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rfacad.buttons.BState;
import com.rfacad.buttons.ButtonCommand;


@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdPlayMode extends CmdMpd implements ButtonCommand
{
	private static final Logger log = LogManager.getLogger(CmdPlayMode.class);
	
	private CmdMpdStatus status;
	public CmdPlayMode(CmdMpdStatus status)
	{
		super(status,"nop");
		this.status=status;
	}
	public boolean button(BState state)
	{
		String s=status.get("single");
		if (s!=null) try {
			int mode=Integer.parseInt(s);
			mode = 1 - mode;
			setCommand("single "+mode);
			log.debug("single - {}",mode);
			boolean ret = super.button(state);
			if ( ret ) {
				// Success. Update state variable
				if ( mode == 1 ) {
					state.set("mode","Track once");
				}
				else {
					state.set("mode","Playlist once");
				}
			}
			return ret;
		}
		catch (NumberFormatException e) {
			log.warn("Bad single state: {}",s);
		}
		return false;
	}

}
