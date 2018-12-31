package com.rfacad.jjmpc;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdPlayMode extends CmdMpd implements ButtonCommand
{
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
			//System.err.println("single - "+mode);
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
			System.err.println("Bad single state: "+s);
		}
		return false;
	}

}
