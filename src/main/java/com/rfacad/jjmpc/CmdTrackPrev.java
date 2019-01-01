package com.rfacad.jjmpc;

import com.rfacad.buttons.interfaces.BState;
import com.rfacad.mpd.interfaces.PlaylistDBI;

import java.util.List;

import com.rfacad.buttons.ButtonCommand;


@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdTrackPrev extends AbstractCmdMpd implements ButtonCommand
{
	private PlaylistDBI db;
	public CmdTrackPrev(CmdMpd status,PlaylistDBI db)
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
		
		
		// "Prev" commands:
		//	state	elapsed index in current playlist
		//	play	>5      ?       "stop", "play"								(1. Restart current track, if not at the beginning)
		//	pause	>5      ?       "stop", "play"								(2. Restart current track, if not at the beginning)
		//	play	<5|N/A  0       load prev, "stop", "play", (len-1)*"next"	(3. At the beginning of the first track in the playlist, so load the last track of the previous playlist)
		//	pause	<5|N/A  0       load prev, "stop", "play", (len-1)*"next"	(4. At the beginning of the first track in the playlist, so load the last track of the previous playlist)
		//	stop	?		0       load prev, "play", (len-1)*"next"			(5. At the beginning of the first track in the playlist, so load the last track of the previous playlist)
		//	play	<5|N/A  x       load curr, "stop", "play", (x-1)*"next"		(6. At the beginning of the track, go to the previous track)
		//	pause	<5|N/A  x       load curr, "stop", "play", (x-1)*"next",	(7. At the beginning of the track, go to the previous track)
		//	stop	?		x       load curr, "play", (x-1)*"next",			(8. At the beginning of the track, go to the previous track)

		// There are plistlen items remaining in the loaded playlist.
		// So if the files that were originally in the playlist are
		// A B C D E F
		// ...and plistlen==0, then nothing is left, nothing is playing. currIndex meaningless.
		// ...and plistlen==1, then the loaded playlist is F, and F is playing. currIndex is (6-1)=5.
		// ...and plistlen==2, then the loaded playlist is E F, and E is playing. currIndex is (6-2)=4.
		// ...and plistlen==5, then the loaded playlist is B C D E F, and B is playing. currIndex is (6-5)=1.
		// ,,,and plistlen==6, then the loaded playlist is A B C D E F, and A is playing. currIndex is (6-6)=0.
		
		String playlistid=db.getMostRecentPlaylist(state);
		if ( playlistid==null )
		{
			// Nothing is loaded.
			// Go ahead and load the first playlist, and start it.
			playlistid=db.getNextPlaylist(state);
			if (db.loadPlaylist(state, playlistid))
			{
				return sendCommand(state, "play");
			}
		}
		List<String> playlist = db.listFiles(state, playlistid);
		int currIndex=playlist.size()-plistlen;
		// Note that currIndex can be less than 1, when stopped with a fully-loaded playlist
		
		if ("play".equals(playstate) || "pause".equals(playstate))
		{
			if ( elapsed>5 )
			{
				//	play	>5      ?       "stop", "play"						(1. Restart current track, if not at the beginning)
				//	pause	>5      ?       "stop", "play"						(2. Restart current track, if not at the beginning)
				return sendCommands(state, "stop", "play");
			}
			else
			{
				if ( currIndex>0)
				{
					//	play	<5|N/A  x       load curr, (x-1)*"next", "play"		(6. At the beginning of the track, go to the previous track)
					//	pause	<5|N/A  x       load curr, (x-1)*"next", "play"		(7. At the beginning of the track, go to the previous track)
					boolean ok=db.loadPlaylist(state, playlistid);
					if ( ok )
					{
						List<String> files = db.listFiles(state, playlistid);
						if ( files != null && !files.isEmpty() )
						{
							return sendStopPlayNexts(state, currIndex-1);
						}
					}
				}
				else
				{
					//	play	<5|N/A  0       load prev, (len-1)*"next", "play"	(3. At the beginning of the first track in the playlist, so load the last track of the previous playlist)
					//	pause	<5|N/A  0       load prev, (len-1)*"next", "play"	(4. At the beginning of the first track in the playlist, so load the last track of the previous playlist)
					String prevplaylistid=db.getPrevPlaylist(state);
					boolean ok=db.loadPlaylist(state, prevplaylistid);
					if ( ok )
					{
						List<String> files = db.listFiles(state, prevplaylistid);
						if ( files != null && !files.isEmpty() )
						{
							return sendStopPlayNexts(state, files.size()-1);
						}
					}
				}
			}
		}
		else if ("stop".equals(playstate))
		{
			if ( currIndex<=0 )
			{
				//	stop	?		0       load prev, "play", (len-1)*"next"			(5. At the beginning of the first track in the playlist, so load the last track of the previous playlist)
				String prevplaylistid=db.getPrevPlaylist(state);
				boolean ok=db.loadPlaylist(state, prevplaylistid);
				if ( ok )
				{
					List<String> files = db.listFiles(state, prevplaylistid);
					if ( files != null && !files.isEmpty() )
					{
						return sendPlayNexts(state, files.size()-1);
					}
				}
			}
			else
			{
				//	stop	?		x       load curr, "play", (x-1)*"next",			(8. At the beginning of the track, go to the previous track)
				boolean ok=db.loadPlaylist(state, playlistid);
				if ( ok )
				{
					List<String> files = db.listFiles(state, playlistid);
					if ( files != null && !files.isEmpty() )
					{
						return sendPlayNexts(state, currIndex-1);
					}
				}
			}
		}
		
		return false;
	}
	
	protected boolean sendStopPlayNexts(BState state,int nexts)
	{
		String [] cmds=new String[nexts+2];
		cmds[0]="stop";
		cmds[1]="play";
		for(int i=0;i<nexts;i++)
		{
			cmds[i+2]="next";
		}
		return sendCommands(state,cmds);
	}

	protected boolean sendPlayNexts(BState state,int nexts)
	{
		String [] cmds=new String[nexts+1];
		cmds[0]="play";
		for(int i=0;i<nexts;i++)
		{
			cmds[i+1]="next";
		}
		return sendCommands(state,cmds);
	}

}
