package com.rfacad.jjmpc;

import com.rfacad.buttons.interfaces.BState;
import com.rfacad.mpd.interfaces.PlaylistDBI;
import com.rfacad.buttons.ButtonCommand;


@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdTrackNext extends AbstractCmdMpd implements ButtonCommand
{
	private PlaylistDBI db;
	public CmdTrackNext(CmdMpd status,PlaylistDBI db)
	{
		super(status);
		this.db=db;
	}
	public boolean button(BState state)
	{
		String playstate=state.getString(CmdPlayPause.STATE);
		String plistlenO=state.getString("playlistlength");
		String elapsedO=state.getString("elapsed");
		
		int plistlen=0;
		try {
			if ( plistlenO != null)
				plistlen=Integer.parseInt(plistlenO);
		}
		catch (NumberFormatException e)
		{
			log.debug("Could not parse playlistlength={}",plistlenO);
		}
		double elapsed=0;
		try {
			if ( elapsedO != null )
				elapsed=Double.parseDouble(elapsedO);
		}
		catch (NumberFormatException e)
		{
			log.debug("Could not parse elapsed={}",elapsedO);
		}
		
		
		// "Next" commands:
		//	state   elapsed playlistlength
		//
		//	play    ?       0,1  X   "stop", load next, "play" (1. we are playing last track)
		//	pause   0       0,1  X   "play" (2. we are paused after finishing the penultimate track)
		//	pause   +       0,1  X   "stop", load next, "play" (3. we are paused on last track)
		//	stop    ?       0,1  X   load first|next, "play" (4. Startup or after playlist)
		//
		//	play    ?       +    X   "next" (5. we are playing a track)
		//	pause   0       +    X   "play" (6. we are paused having finished a track)
		//	pause   +       +    X   "next" (7. we are paused in a track)
		//	stop    ?       +    X   "play" "next" (8. we stopped a track instead of pausing)
		
		if ("play".equals(playstate))
		{
			if ( plistlen>1 )
			{
				return sendCommand(state, "next"); // case 5
			}
			else
			{
				String playlistid=db.getNextPlaylist(state);
				sendCommand(state, "stop");
				if (db.loadPlaylist(state, playlistid))
				{
					return sendCommand(state, "play"); // case 1
				}
			}
		}
		else if ("pause".equals(playstate))
		{
			if ( elapsed<0.001 && plistlen<2 )
			{
				return sendCommand(state, "play"); // case 2
			}
			else if ( elapsed<0.001 && plistlen>1 )
			{
				return sendCommand(state, "play"); // case 6
			}
			else if ( elapsed>0 && plistlen>1 )
			{
				return sendCommand(state, "next"); // case 7
			}
			else if ( elapsed>0 && plistlen<2 )
			{
				String playlistid=db.getNextPlaylist(state);
				sendCommand(state, "stop");
				if (db.loadPlaylist(state, playlistid))
				{
					return sendCommand(state, "play"); // case 3
				}
			}
		}
		else if ("stop".equals(playstate))
		{
			if ( plistlen>1 )
			{
				return sendCommands(state, "play", "next"); // case 8
			}
			String playlistid=db.getNextPlaylist(state);
			if (db.loadPlaylist(state, playlistid))
			{
				return sendCommand(state, "play"); // case 4
			}
		}
		
		return false;
	}
}
