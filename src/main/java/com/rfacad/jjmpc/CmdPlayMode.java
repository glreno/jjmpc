package com.rfacad.jjmpc;

import com.rfacad.buttons.interfaces.BState;
import com.rfacad.buttons.ButtonCommand;


@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdPlayMode extends AbstractCmdMpd implements ButtonCommand
{
	public CmdPlayMode(CmdMpd status)
	{
		super(status);
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
				log.debug("single - {}",mode);
				boolean ret = sendCommand(state,"single "+mode);
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
