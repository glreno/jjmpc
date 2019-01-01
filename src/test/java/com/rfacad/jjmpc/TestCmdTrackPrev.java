package com.rfacad.jjmpc;


import static org.junit.Assert.*;

import org.junit.Test;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class TestCmdTrackPrev 
{
	// "Prev" commands:
	//	elapsed index in current playlist
	//	>5      ?       "stop", "play"						(1. Restart current track, if not at the beginning)
	//	<5|N/A  0       load prev, (len-1)*"next", "play"	(2. At the beginning of the first track in the playlist, so load the last track of the previous playlist)
	//	<5|N/A  x       load curr, (x-1)*"next", "play"		(3. At the beginning of the track, go to the previous track)
	
	@Test
	public void shouldRestartCurrentTrack()
	{
		// 1. Restart current track, if not at the beginning
		// Elapsed: >= 5 seconds, don't care about index in playlist
		// "stop" "play"
	}
	
	@Test
	public void shouldLoadPreviousPlaylist()
	{
		// 2a. At the beginning of the first track in the playlist, so load the last track of the previous playlist
		// Elapsed: unavailable or < 5 seconds, in first track of playlist
		// load prev, (len-1)*"next", "play"
	}

	@Test
	public void shouldLoadFirstPlaylist()
	{
		// 2b. At the beginning of the first track in the playlist, so load the last track of the previous playlist
		// Elapsed: unavailable or < 5 seconds, no current playlist
		// load prev, (len-1)*"next", "play"
	}

	@Test
	public void shouldLoadPreviousTrack()
	{
		// 3. At the beginning of the track, go to the previous track
		// Elapsed: unavailable or < 5 seconds, in any track x of the playlist (x>0)
		// load curr, (x-1)*"next", "play"
	}
}
