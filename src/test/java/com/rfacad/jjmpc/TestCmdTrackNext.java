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
public class TestCmdTrackNext 
{
	public static String [] FOLDERS={ "folder 0", "folder 1" };
	public static String [] PLAYLISTS0 = {
			FOLDERS[0]+PlaylistDBI.SEP+"playlist 00",
			FOLDERS[0]+PlaylistDBI.SEP+"playlist 01"
	};
	public static String [] DIRS0 = {
			FOLDERS[0]+"/"+"playlist 00",
			FOLDERS[0]+"/"+"playlist 01"
	};
	public static String [] PLAYLISTS1 = {
			FOLDERS[1]+PlaylistDBI.SEP+"playlist 10",
			FOLDERS[1]+PlaylistDBI.SEP+"playlist 11"
	};
	public static String [] DIRS1 = {
			FOLDERS[1]+"/"+"playlist 10",
			FOLDERS[1]+"/"+"playlist 11"
	};
	public static String [] FILES00 = { "file 01.mp3", "file 02.mp3", "file 03.mp3"};
	public static String [] FILES01 = { "file 04.mp3", "file 05.mp3", "file 06.mp3"};
	public static String [] FILES10 = { "file 11.mp3", "file 12.mp3", "file 13.mp3"};
	public static String [] FILES11 = { "file 14.mp3", "file 15.mp3", "file 16.mp3"};
	
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
	private CmdTrackNext nextcmd;
	private MockMpdClient driver;
	private PlaylistDB db;
	
	@Before
	public void setup()
	{
		driver=new MockMpdClient();
		db=new PlaylistDB(driver);
		statuscmd=new CmdMpd(driver,"nop");
		nextcmd=new CmdTrackNext(statuscmd,db);
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
	
	// "Next" commands:
	//	state   elapsed playlistlength
	//
	//	play    ?       0       "stop", load next, "play" (1. we are playing last track)
	//	pause   0       0       "play" (2. we are paused after finishing the penultimate track)
	//	pause   +       0       "stop", load next, "play" (3. we are paused on last track)
	//	stop    ?       0       load next, "play" (4. Startup or after playlist)
	//
	//	play    ?       +       "next" (5. we are playing a track)
	//	pause   0       +       "play" (6. we are paused having finished a track)
	//	pause   +       +       "next" (7. we are paused in a track)
	//	stop    ?       +       "play" "next" (8. we stopped a track instead of pausing)

	@Test
	public void shouldStartNextPlaylistDuringLastTrack()
	{
		// Status: state: play, playlistlength: 0, elapsed: any
		// 1. While playing the last track in a playlist, start the next playlist ( "stop", load next playlist, "play")
		BState bs=loadPlaylistState();
		setStatus(bs,"play",0,"7.013");
		db.setMostRecentPlaylist(bs, PLAYLISTS0[0]);
		String load="loadplaylist \""+PLAYLISTS0[1]+"\"";
		driver.expectOkRequest("stop");
		driver.expectOkRequest(load);
		driver.expectOkRequest("play");
		
		boolean ok=nextcmd.button(bs);
		assertTrue(ok);
		
		List<String> sent = driver.getCommandsSent();
		assertEquals(3,sent.size());
		assertEquals("stop",sent.get(0));
		assertEquals(load,sent.get(1));
		assertEquals("play",sent.get(2));
	}
	
	@Test
	public void shouldStartNextTrackAfterPenultimateTrack()
	{
		// Status: state: pause, playlistlength: 0, elapsed: 0
		// 2. After playing the penultimate track in a playlist, play the last track ( "play" )
		BState bs=loadPlaylistState();
		setStatus(bs,"pause",0,"0.000");
		driver.expectOkRequest("play");
		
		boolean ok=nextcmd.button(bs);
		assertTrue(ok);
		
		List<String> sent = driver.getCommandsSent();
		assertEquals(1,sent.size());
		assertEquals("play",sent.get(0));
	}
	@Test
	public void shouldStartNextPlaylistWhenPausedDuringLastTrack()
	{
		// Status: state: pause, playlistlength: 0, elapsed: >0
		// 3. While playing the last track in a playlist, start the next playlist ( "stop", load next playlist, "play")
		BState bs=loadPlaylistState();
		setStatus(bs,"pause",0,"7.013");
		db.setMostRecentPlaylist(bs, PLAYLISTS0[0]);
		String load="loadplaylist \""+PLAYLISTS0[1]+"\"";
		driver.expectOkRequest("stop");
		driver.expectOkRequest(load);
		driver.expectOkRequest("play");
		
		boolean ok=nextcmd.button(bs);
		assertTrue(ok);
		
		List<String> sent = driver.getCommandsSent();
		assertEquals(3,sent.size());
		assertEquals("stop",sent.get(0));
		assertEquals(load,sent.get(1));
		assertEquals("play",sent.get(2));
	}

	@Test
	public void shouldStartNextPlaylistAfterLastTrack()
	{
		// 4a. When stopped after playing the last track in a playlist, start the next playlist ( load next playlist, "play")
		// Status: state: stop, playlistlength: 0, elapsed:any, there is a current playlist
		BState bs=loadPlaylistState();
		setStatus(bs,"stop",0,null);
		db.setMostRecentPlaylist(bs, PLAYLISTS0[0]);
		String load="loadplaylist \""+PLAYLISTS0[1]+"\"";
		driver.expectOkRequest(load);
		driver.expectOkRequest("play");
		
		boolean ok=nextcmd.button(bs);
		assertTrue(ok);
		
		List<String> sent = driver.getCommandsSent();
		assertEquals(2,sent.size());
		assertEquals(load,sent.get(0));
		assertEquals("play",sent.get(1));
	}

	@Test
	public void shouldStartFirstPlaylistOnStartup()
	{
		// 4b. On startup, start the first playlist ( load first playlist, "play" )
		// Status: state: stop, playlistlength: 0, elapsed:any, there is no current playlist
		BState bs=loadPlaylistState();
		setStatus(bs,"stop",0,null);
		String load="loadplaylist \""+PLAYLISTS0[0]+"\"";
		driver.expectOkRequest(load);
		driver.expectOkRequest("play");
		
		boolean ok=nextcmd.button(bs);
		assertTrue(ok);
		
		List<String> sent = driver.getCommandsSent();
		assertEquals(2,sent.size());
		assertEquals(load,sent.get(0));
		assertEquals("play",sent.get(1));
	}


	@Test
	public void shouldStartNextTrackInPlaylistWhilePlaying()
	{
		// 5. While playing a track in a playlist, start the next track ("next")
		// Status: state: play, playlistlength: >0, elapsed: any
		BState bs=loadPlaylistState();
		setStatus(bs,"play",2,"7.013");
		driver.expectOkRequest("next");
		
		boolean ok=nextcmd.button(bs);
		assertTrue(ok);
		
		List<String> sent = driver.getCommandsSent();
		assertEquals(1,sent.size());
		assertEquals("next",sent.get(0));
	}

	@Test
	public void shouldStartNextTrackAfterTrack()
	{
		// 6. When stopped after playing a track in a playlist, start the next track ("play" -- it'll be the first track in the loaded playlist)
		// Status: state: pause, playlistlength: >0, elapsed: 0
		BState bs=loadPlaylistState();
		setStatus(bs,"pause",2,"0.000");
		driver.expectOkRequest("play");
		
		boolean ok=nextcmd.button(bs);
		assertTrue(ok);
		
		List<String> sent = driver.getCommandsSent();
		assertEquals(1,sent.size());
		assertEquals("play",sent.get(0));
	}

	@Test
	public void shouldStartNextTrackInPlaylistWhilePaused()
	{
		// 7. While paused on a track in a playlist, start the next track ("next" is sufficient)
		// Status: state: pause, playlistlength: >0, elapsed: >0
		BState bs=loadPlaylistState();
		setStatus(bs,"pause",2,"7.013");
		driver.expectOkRequest("next");
		
		boolean ok=nextcmd.button(bs);
		assertTrue(ok);
		
		List<String> sent = driver.getCommandsSent();
		assertEquals(1,sent.size());
		assertEquals("next",sent.get(0));
	}

	@Test
	public void shouldStartCurrentTrackAfterStop()
	{
		// 8. When stopped while playing a track in a playlist, start the next track ("play" "next" )
		// Status: state: stop, playlistlength: >0, elapsed: any
		BState bs=loadPlaylistState();
		setStatus(bs,"stop",2,null);
		driver.expectOkRequest("play");
		driver.expectOkRequest("next");
		
		boolean ok=nextcmd.button(bs);
		assertTrue(ok);
		
		List<String> sent = driver.getCommandsSent();
		assertEquals(2,sent.size());
		assertEquals("play",sent.get(0));
		assertEquals("next",sent.get(1));
	}
}
