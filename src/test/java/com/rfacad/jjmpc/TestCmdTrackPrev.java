package com.rfacad.jjmpc;


import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.rfacad.buttons.interfaces.BState;
import com.rfacad.mpd.interfaces.PlaylistDBI;
import com.rfacad.mpd.playlistdb.PlaylistDB;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class TestCmdTrackPrev 
{
	public static String [] FOLDERS={ "folder 0", "folder 1" };
	public static String [] PLAYLISTS0 = {
			FOLDERS[0]+PlaylistDBI.SEP+"playlist 00",
			FOLDERS[0]+PlaylistDBI.SEP+"playlist 01"
	};
	public static String [] DIRS0 = {
			FOLDERS[0]+"/"+"playlist 00"+"/",
			FOLDERS[0]+"/"+"playlist 01"+"/"
	};
	public static String [] PLAYLISTS1 = {
			FOLDERS[1]+PlaylistDBI.SEP+"playlist 10",
			FOLDERS[1]+PlaylistDBI.SEP+"playlist 11"
	};
	public static String [] DIRS1 = {
			FOLDERS[1]+"/"+"playlist 10"+"/",
			FOLDERS[1]+"/"+"playlist 11"+"/"
	};
	public static String [] FILES00 = { "file 01.mp3", "file 02.mp3", "file 03.mp3", "file 04.mp3"};
	public static String [] FILES01 = { "file 05.mp3", "file 06.mp3", "file 07.mp3", "file 08.mp3"};
	public static String [] FILES10 = { "file 11.mp3", "file 12.mp3", "file 13.mp3", "file 14.mp3"};
	public static String [] FILES11 = { "file 15.mp3", "file 16.mp3", "file 17.mp3", "file 18.mp3"};
	
	// While playing track    The number of items remaining in the playlist is
	// file 05.mp3            4   (the first track has FILE01.length items remaining when you are playing it!)
	// file 06.mp3            3
	// file 07.mp3            2
	// file 08.mp3            1
	
	public static List<String> PATHS00=new ArrayList<>();
	public static List<String> PATHS01=new ArrayList<>();
	public static List<String> PATHS10=new ArrayList<>();
	public static List<String> PATHS11=new ArrayList<>();
	
	static {
		Arrays.asList(FILES00).forEach(n->PATHS00.add(DIRS0[0]+n));
		Arrays.asList(FILES01).forEach(n->PATHS01.add(DIRS0[1]+n));
		Arrays.asList(FILES10).forEach(n->PATHS10.add(DIRS1[0]+n));
		Arrays.asList(FILES11).forEach(n->PATHS11.add(DIRS1[1]+n));
	}
	
	private CmdMpd statuscmd;
	private CmdTrackPrev prevcmd;
	private MockMpdClient driver;
	private PlaylistDB db;
	
	@Before
	public void setup()
	{
		driver=new MockMpdClient();
		db=new PlaylistDB(driver);
		statuscmd=new CmdMpd(driver,"nop");
		prevcmd=new CmdTrackPrev(statuscmd,db);
	}
	
	public static BState loadPlaylistState()
	{
		BState bs=new MockBState();
		bs.setStringList(PlaylistDBI.FOLDERS, Arrays.asList(FOLDERS));
		bs.setStringList(PlaylistDBI.PLAYLISTS_IN_FOLDER_PREFIX+FOLDERS[0], Arrays.asList(PLAYLISTS0));
		bs.setStringList(PlaylistDBI.PLAYLISTS_IN_FOLDER_PREFIX+FOLDERS[1], Arrays.asList(PLAYLISTS1));
		bs.setStringList(PlaylistDBI.FILES_IN_PLAYLIST_PREFIX+PLAYLISTS0[0], Arrays.asList(FILES00));
		bs.setStringList(PlaylistDBI.FILES_IN_PLAYLIST_PREFIX+PLAYLISTS0[1], Arrays.asList(FILES01));
		bs.setStringList(PlaylistDBI.FILES_IN_PLAYLIST_PREFIX+PLAYLISTS1[0], Arrays.asList(FILES10));
		bs.setStringList(PlaylistDBI.FILES_IN_PLAYLIST_PREFIX+PLAYLISTS1[1], Arrays.asList(FILES11));
		bs.setStringList(PlaylistDBI.FILES_IN_M3U_PREFIX+PLAYLISTS0[0], PATHS00);
		bs.setStringList(PlaylistDBI.FILES_IN_M3U_PREFIX+PLAYLISTS0[1], PATHS01);
		bs.setStringList(PlaylistDBI.FILES_IN_M3U_PREFIX+PLAYLISTS1[0], PATHS10);
		bs.setStringList(PlaylistDBI.FILES_IN_M3U_PREFIX+PLAYLISTS1[1], PATHS11);
		return bs;
	}
	
	public static void setStatus(BState bs, String state, int playlistlength, String elapsed)
	{
		bs.setString("state", state);
		if ( playlistlength>-1) bs.setString("playlistlength", Integer.toString(playlistlength));
		if ( elapsed != null ) bs.setString("elapsed", elapsed);
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
	
	@Test
	public void shouldRestartCurrentTrackWhenPlaying()
	{
		// 1. Restart current track, if not at the beginning
		// State=play, Elapsed: >= 5 seconds, don't care about index in playlist
		// "stop" "play"
		BState bs=loadPlaylistState();
		setStatus(bs,"play",0,"7.013");
		db.setMostRecentPlaylist(bs, PLAYLISTS0[1]);
		
		driver.expectOkRequest("stop");
		driver.expectOkRequest("play");
		
		boolean ok=prevcmd.button(bs);
		assertTrue(ok);
		
		List<String> sent = driver.getCommandsSent();
		assertEquals(2,sent.size());
		assertEquals("stop",sent.get(0));
		assertEquals("play",sent.get(1));
	}

	@Test
	public void shouldRestartCurrentTrackWhenPaused()
	{
		// 2. Restart current track, if not at the beginning
		// State=pause, Elapsed: >= 5 seconds, don't care about index in playlist
		// "stop" "play"
		BState bs=loadPlaylistState();
		setStatus(bs,"pause",1,"7.013");
		db.setMostRecentPlaylist(bs, PLAYLISTS0[1]);

		driver.expectOkRequest("stop");
		driver.expectOkRequest("play");
		
		boolean ok=prevcmd.button(bs);
		assertTrue(ok);
				
		List<String> sent = driver.getCommandsSent();
		assertEquals(2,sent.size());
		assertEquals("stop",sent.get(0));
		assertEquals("play",sent.get(1));
	}
	
	@Test
	public void shouldLoadPreviousPlaylistWhenPlayingAtStart()
	{
		// 3a. At the beginning of the first track in the playlist, so load the last track of the previous playlist
		// State=play, Elapsed: unavailable or < 5 seconds, in first track of playlist
		// load prev, "stop", "play", (len-1)*"next"
		BState bs=loadPlaylistState();
		setStatus(bs,"play",FILES01.length,"1.023"); // 4: A B C D, want d (load prev, 3 nexts)
		db.setMostRecentPlaylist(bs, PLAYLISTS0[1]);
		
		int nexts=FILES00.length-1;
		String load="load \""+PLAYLISTS0[0]+"\"";
		expectLoadStopPlayNexts(load, nexts);
		
		boolean ok=prevcmd.button(bs);
		assertTrue(ok);
		
		List<String> sent = driver.getCommandsSent();
		verifyLoadStopPlayNexts(sent,load,nexts);
	}

	@Test
	public void shouldLoadPreviousPlaylistWhenPausedAtStart()
	{
		// 4a. At the beginning of the first track in the playlist, so load the last track of the previous playlist
		// State=pause, Elapsed: unavailable or < 5 seconds, in first track of playlist
		// load prev, "stop", "play", (len-1)*"next"
		BState bs=loadPlaylistState();
		setStatus(bs,"pause",FILES01.length,"1.023"); // 4: A B C D, want d (load prev, 3 nexts)
		db.setMostRecentPlaylist(bs, PLAYLISTS0[1]);
		
		int nexts=FILES00.length-1;
		String load="load \""+PLAYLISTS0[0]+"\"";
		expectLoadStopPlayNexts(load, nexts);
		
		boolean ok=prevcmd.button(bs);
		assertTrue(ok);
		
		List<String> sent = driver.getCommandsSent();
		verifyLoadStopPlayNexts(sent,load,nexts);
	}

	@Test
	public void shouldLoadPreviousPlaylistWhenStopped()
	{
		// 5a. At the beginning of the first track in the playlist, so load the last track of the previous playlist
		// State=stop, Elapsed: unavailable or < 5 seconds, in first track of playlist
		// load prev, "play", (len-1)*"next"
		BState bs=loadPlaylistState();
		setStatus(bs,"stop",FILES01.length,"1.023"); // 4: A B C D, want d (load prev, 3 nexts)
		db.setMostRecentPlaylist(bs, PLAYLISTS0[1]);
		
		int nexts=FILES00.length-1;
		String load="load \""+PLAYLISTS0[0]+"\"";
		expectLoadPlayNexts(load, nexts);
		
		boolean ok=prevcmd.button(bs);
		assertTrue(ok);
		
		List<String> sent = driver.getCommandsSent();
		verifyLoadPlayNexts(sent,load,nexts);
	}

	@Test
	public void shouldLoadFirstPlaylistWhenPlayingAtStart()
	{
		// 3b. At the beginning of the first track in the playlist, so load the last track of the previous playlist
		// But this is the first playlist, so just start the first playlist.
		// State=play, Elapsed: unavailable or < 5 seconds, no current playlist
		// load prev, "play"
		BState bs=loadPlaylistState();
		setStatus(bs,"play",FILES01.length,null); // 4: A B C D, want a (load first, 0 nexts)
		
		int nexts=0;
		String load="load \""+PLAYLISTS0[0]+"\"";
		expectLoadPlayNexts(load, nexts);
		
		boolean ok=prevcmd.button(bs);
		assertTrue(ok);
		
		List<String> sent = driver.getCommandsSent();
		verifyLoadPlayNexts(sent,load,nexts);
	}
	
	@Test
	public void shouldLoadFirstPlaylistWhenPausedAtStart()
	{
		// 4b. At the beginning of the first track in the playlist, so load the last track of the previous playlist
		// But this is the first playlist, so just start the first playlist.
		// State=pause, Elapsed: unavailable or < 5 seconds, no current playlist
		// load prev, "play"
		BState bs=loadPlaylistState();
		setStatus(bs,"pause",FILES01.length,null); // 4: A B C D, want a (load first, 0 nexts)

		int nexts=0;
		String load="load \""+PLAYLISTS0[0]+"\"";
		expectLoadPlayNexts(load, nexts);
		
		boolean ok=prevcmd.button(bs);
		assertTrue(ok);
		
		List<String> sent = driver.getCommandsSent();
		verifyLoadPlayNexts(sent,load,nexts);
	}
	
	@Test
	public void shouldLoadFirstPlaylistWhenStopped()
	{
		// 5b. At the beginning of the first track in the playlist, so load the last track of the previous playlist
		// But this is the first playlist, so just start the first playlist.
		// State=stop, Elapsed: unavailable or < 5 seconds, no current playlist
		// load prev, "play"
		BState bs=loadPlaylistState();
		setStatus(bs,"stop",FILES01.length,null); // 4: A B C D, want a (load first, 0 nexts)

		int nexts=0;
		String load="load \""+PLAYLISTS0[0]+"\"";
		expectLoadPlayNexts(load, nexts);
		
		boolean ok=prevcmd.button(bs);
		assertTrue(ok);
		
		List<String> sent = driver.getCommandsSent();
		verifyLoadPlayNexts(sent,load,nexts);
	}

	@Test
	public void shouldLoadPreviousTrackWhenPlayingStartOfSecondTrack()
	{
		// 6. At the beginning of the track, go to the previous track
		// State=play, Elapsed: unavailable or < 5 seconds, in any track x of the playlist (x>0)
		// load curr, "stop", "play", (x-1)*"next"
		BState bs=loadPlaylistState();
		setStatus(bs,"play",2,"1.023"); // 2: C D, want B (load curr, 1 next)
		db.setMostRecentPlaylist(bs, PLAYLISTS0[1]);
		
		int nexts=1;
		String load="load \""+PLAYLISTS0[1]+"\"";
		expectLoadStopPlayNexts(load, nexts);
		
		boolean ok=prevcmd.button(bs);
		assertTrue(ok);
		
		List<String> sent = driver.getCommandsSent();
		verifyLoadStopPlayNexts(sent,load,nexts);
	}
	@Test
	public void shouldLoadPreviousTrackWhenPlayingStartOfThirdTrack()
	{
		// 6. At the beginning of the track, go to the previous track
		// State=play, Elapsed: unavailable or < 5 seconds, in any track x of the playlist (x>0)
		// load curr, "stop", "play", (x-1)*"next"
		BState bs=loadPlaylistState();
		setStatus(bs,"play",2,"1.023"); // 2: C D, want B (load curr, 1 next)
		db.setMostRecentPlaylist(bs, PLAYLISTS0[1]);
		
		int nexts=1;
		String load="load \""+PLAYLISTS0[1]+"\"";
		expectLoadStopPlayNexts(load, nexts);
		
		boolean ok=prevcmd.button(bs);
		assertTrue(ok);
		
		List<String> sent = driver.getCommandsSent();
		verifyLoadStopPlayNexts(sent,load,nexts);
	}
	@Test
	public void shouldLoadPreviousTrackWhenPausedAtStartOfSecondTrack()
	{
		// 7. At the beginning of the track, go to the previous track
		// State=pause, Elapsed: unavailable or < 5 seconds, in any track x of the playlist (x>0)
		// load curr, "stop", "play", (x-1)*"next"
		BState bs=loadPlaylistState();
		setStatus(bs,"pause",3,"1.023"); // 3: B C D, want A (load curr, 0 next)
		db.setMostRecentPlaylist(bs, PLAYLISTS0[1]);

		int nexts=0;
		String load="load \""+PLAYLISTS0[1]+"\"";
		expectLoadStopPlayNexts(load, nexts);
		
		boolean ok=prevcmd.button(bs);
		assertTrue(ok);
		
		List<String> sent = driver.getCommandsSent();
		verifyLoadStopPlayNexts(sent,load,nexts);
	}
	@Test
	public void shouldLoadPreviousTrackWhenPausedAtStartOfThirdTrack()
	{
		// 7. At the beginning of the track, go to the previous track
		// State=pause, Elapsed: unavailable or < 5 seconds, in any track x of the playlist (x>0)
		// load curr, "stop", "play", (x-1)*"next"
		BState bs=loadPlaylistState();
		setStatus(bs,"pause",2,"1.023"); // 2: C D, want B (load curr, 1 next)
		db.setMostRecentPlaylist(bs, PLAYLISTS0[1]);

		int nexts=1;
		String load="load \""+PLAYLISTS0[1]+"\"";
		expectLoadStopPlayNexts(load, nexts);
		
		boolean ok=prevcmd.button(bs);
		assertTrue(ok);
		
		List<String> sent = driver.getCommandsSent();
		verifyLoadStopPlayNexts(sent,load,nexts);
	}
	@Test
	public void shouldLoadPreviousTrackWhenStoppedInSecondTrack()
	{
		// 8a. At the beginning of the track, go to the previous track
		// State=stop, Elapsed: don't care, in any track x of the playlist (x>0)
		// load curr, "play", (x-1)*"next"
		BState bs=loadPlaylistState();
		setStatus(bs,"stop",3,"1.023"); // 3: B C D, want A (load curr, 0 next)
		db.setMostRecentPlaylist(bs, PLAYLISTS0[1]);
		
		int nexts=0;
		String load="load \""+PLAYLISTS0[1]+"\"";
		expectLoadPlayNexts(load, nexts);

		boolean ok=prevcmd.button(bs);
		assertTrue(ok);
		
		List<String> sent = driver.getCommandsSent();
		verifyLoadPlayNexts(sent,load,nexts);
	}
	@Test
	public void shouldLoadPreviousTrackWhenStoppedInThirdTrack()
	{
		// 8a. At the beginning of the track, go to the previous track
		// State=stop, Elapsed: don't care, in any track x of the playlist (x>0)
		// load curr, "play", (x-1)*"next"
		BState bs=loadPlaylistState();
		setStatus(bs,"stop",2,"1.023"); // 2: C D, want B (load curr, 1 next)
		db.setMostRecentPlaylist(bs, PLAYLISTS0[1]);
		
		int nexts=1;
		String load="load \""+PLAYLISTS0[1]+"\"";
		expectLoadPlayNexts(load, nexts);
		
		boolean ok=prevcmd.button(bs);
		assertTrue(ok);
		
		List<String> sent = driver.getCommandsSent();
		verifyLoadPlayNexts(sent,load,nexts);
	}
	
	private void expectLoadStopPlayNexts(String load,int nexts)
	{
		driver.expectOkRequest("clear");
		driver.expectOkRequest(load);
		driver.expectOkRequest("play");
		driver.expectOkRequest("stop");
		if ( nexts>0 )
			driver.expectOkRequest("next");
	}

	private void verifyLoadStopPlayNexts(List<String> sent,String load,int nexts)
	{
		assertEquals(4+nexts,sent.size());
		assertEquals("clear",sent.get(0));
		assertEquals(load,sent.get(1));
		assertEquals("stop",sent.get(2));
		assertEquals("play",sent.get(3));
		for(int i=4;i<sent.size();i++)
			assertEquals("next",sent.get(i));
	}

	private void expectLoadPlayNexts(String load,int nexts)
	{
		driver.expectOkRequest("clear");
		driver.expectOkRequest(load);
		driver.expectOkRequest("play");
		driver.expectOkRequest("stop");
		if ( nexts>0 )
			driver.expectOkRequest("next");
	}
	private void verifyLoadPlayNexts(List<String> sent,String load,int nexts)
	{
		assertEquals(3+nexts,sent.size());
		assertEquals("clear",sent.get(0));
		assertEquals(load,sent.get(1));
		assertEquals("play",sent.get(2));
		for(int i=3;i<sent.size();i++)
			assertEquals("next",sent.get(i));
	}

}
