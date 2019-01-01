package com.rfacad.jjmpc;

import com.rfacad.buttons.interfaces.BState;
import com.rfacad.mpd.interfaces.PlaylistDBI;
import com.rfacad.buttons.ButtonCommand;


@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdPlaylistPrev extends AbstractCmdMpd implements ButtonCommand
{
	private PlaylistDBI db;
	public CmdPlaylistPrev(CmdMpd status,PlaylistDBI db)
	{
		super(status);
		this.db=db;
	}
	public boolean button(BState state)
	{
		String playstate=state.getString(CmdPlayPause.STATE);

		String playlistid = db.getPrevPlaylist(state);
		
		if ( playlistid==null)
		{
			// Can't go before first playlist
			return false;
		}

		// stop playing, and clear the current playlist 
		if (playstate!=null && !"stop".equals(playstate))
		{
			sendCommands(state, "stop" );
		}
		
		boolean ret=db.loadPlaylist(state, playlistid);
		
		return ret;
	}
}
