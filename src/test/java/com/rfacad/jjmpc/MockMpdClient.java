package com.rfacad.jjmpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.rfacad.mpd.interfaces.RSMPDListener;
import com.rfacad.mpd.interfaces.RidiculouslySimpleMPDClientI;

/**
 * A very simple mock mpd client.
 * Commands permitted:
 * <ol>
 * <li>status
 * <li>single
 * <li>setvol
 * <li>play
 * <li>pause
 * </ol>
 * State is reported to the status, all other commands change state in some way.
 * State is also accessible directly from this class.
 * the ok() and not_ok() calls to the listener are called on the calling thread -- this needs to be changed in future
 */
@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class MockMpdClient implements RidiculouslySimpleMPDClientI {
	
	private Map<String,String> stats = new ConcurrentHashMap<>();

	public void setStat(String key,String value)
	{
		stats.put(key, value);
	}
	
	public String getStat(String key)
	{
		return stats.get(key);
	}
	

	@Override
	public void sendCommand(String command, RSMPDListener listener) throws IOException {
		List<String> resp=new ArrayList<>();
		if ( command==null )
		{
			resp.add("Null command");
			listener.not_ok("ACK", resp);
		}
		else if ( "status".equals(command))
		{
			for(Map.Entry<String,String> e:stats.entrySet())
			{
				resp.add(e.getKey()+":"+e.getValue());
			}
		}
		else if ( "play".equals(command))
		{
			stats.put("state", "play");
		}
		else if ( "pause".equals(command))
		{
			stats.put("state", "pause");
		}
		else if ( command.startsWith("setvol"))
		{
			String n = command.substring(6).trim();
			stats.put("volume", n);
		}
		else if ( command.startsWith("single"))
		{
			String n = command.substring(6).trim();
			stats.put("single", n);
		}
		else
		{
			resp.add("Unknown command "+command);
			listener.not_ok("ACK", resp);
		}
		listener.ok(resp);
	}

}
