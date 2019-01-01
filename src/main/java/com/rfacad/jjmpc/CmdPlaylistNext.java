package com.rfacad.jjmpc;

import com.rfacad.buttons.interfaces.BState;
import com.rfacad.mpd.interfaces.PlaylistDBI;

import java.util.List;

import com.rfacad.buttons.ButtonCommand;
import com.rfacad.buttons.ButtonState;


@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdPlaylistNext extends AbstractCmdMpd implements ButtonCommand
{
	private PlaylistDBI db;
	public CmdPlaylistNext(CmdMpd status,PlaylistDBI db)
	{
		super(status);
		this.db=db;
	}
	public boolean button(BState state)
	{
		String playstate=state.getString(CmdPlayPause.STATE);
		
		// stop playing, and clear the current playlist 
		if (playstate!=null && !"stop".equals(playstate))
		{
			sendCommands(state, "stop" );
		}
		
		List<String> shifts = state.getStringList(ButtonState.SHIFT);
		int nshifts=0;
		if ( shifts!=null )
		{
			nshifts=shifts.size();
		}

		String playlistid;
		
		if (nshifts<2)
		{
			playlistid = db.getNextPlaylist(state);
		}
		else
		{
			playlistid = db.getNextPlaylistFolderFirstPlaylist(state);
		}
		boolean ret=db.loadPlaylist(state, playlistid);
		
		return ret;
	}
}
