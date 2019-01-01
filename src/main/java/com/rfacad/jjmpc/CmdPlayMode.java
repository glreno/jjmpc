package com.rfacad.jjmpc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rfacad.buttons.interfaces.BState;
import com.rfacad.buttons.ButtonCommand;


@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdPlayMode extends CmdMpd implements ButtonCommand
{
	private static final Logger log = LogManager.getLogger(CmdPlayMode.class);
	
	public CmdPlayMode(CmdMpd status)
	{
		super(status,"nop");
	}
	public boolean button(BState state)
	{
		Object o=state.get("single");
		if (o!=null)
		{
			String s=o.toString();
			try {
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
		}
		return false;
	}

}
