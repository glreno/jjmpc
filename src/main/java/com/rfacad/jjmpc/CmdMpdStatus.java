package com.rfacad.jjmpc;

import java.util.*;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdMpdStatus extends CmdMpd implements ButtonCommand
{
	private Map<String,String> stats;
	public CmdMpdStatus(RidiculouslySimpleMPDClient md)
	{
		super(md,"status");
		stats=Collections.emptyMap();
	}
	public boolean button(BState state)
	{
		boolean ret=super.button(state);
		List<String> resp=getResponse();
		if ( !ret || resp==null ) {
			return false;
		}
		stats=new HashMap<>();
		for(String s:resp) {
			int colon = s.indexOf(':');
			if ( colon > 0 ) {
				String key = s.substring(0,colon).trim();
				String val = s.substring(colon+1).trim();
				stats.put(key,val);
			}
		}
		return true;
	}

	public String get(String key) { return stats.get(key);}
}
