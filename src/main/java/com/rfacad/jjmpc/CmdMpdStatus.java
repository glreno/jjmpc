package com.rfacad.jjmpc;

import java.util.*;

import com.rfacad.buttons.interfaces.BState;
import com.rfacad.buttons.ButtonCommand;
import com.rfacad.mpd.interfaces.RidiculouslySimpleMPDClientI;


@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdMpdStatus extends CmdMpd implements ButtonCommand
{
	public CmdMpdStatus(RidiculouslySimpleMPDClientI md)
	{
		super(md,"status");
	}
	public boolean button(BState state)
	{
		boolean ret=super.button(state);
		List<String> resp=getResponse();
		if ( !ret || resp==null ) {
			return false;
		}
		for(String s:resp) {
			int colon = s.indexOf(':');
			if ( colon > 0 ) {
				String key = s.substring(0,colon).trim();
				String val = s.substring(colon+1).trim();
				state.setString(key,val);
			}
		}
		return true;
	}
}
