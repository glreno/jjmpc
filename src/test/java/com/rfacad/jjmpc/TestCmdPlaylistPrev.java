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
public class TestCmdPlaylistPrev 
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
	private CmdPlaylistPrev prevcmd;
	private MockMpdClient driver;
	private PlaylistDB db;
	
	@Before
	public void setup()
	{
		driver=new MockMpdClient();
		db=new PlaylistDB(driver);
		statuscmd=new CmdMpd(driver,"nop");
		prevcmd=new CmdPlaylistPrev(statuscmd,db);
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
	
	public void setStatusStop(BState bs)
	{
		bs.setString("state", "stop");
	}

	public void setStatusPlay(BState bs, String state, String playlist)
	{
		bs.setString("state", state);
		bs.setString("elapsed", "1.043");
		db.setMostRecentPlaylist(bs, playlist);
	}

	@Test
	public void shouldDoNothingOnStartup()
	{
		// On startup, do nothing
		BState bs=loadPlaylistState();
		// set no status!!!
		
		boolean ok=prevcmd.button(bs);
		assertFalse(ok);
		
	}

	@Test
	public void shouldDoNothingWhilePlayingFirst()
	{
		// While playing first playlist, try to load the prev playlist
		BState bs=loadPlaylistState();
		setStatusPlay(bs,"play",PLAYLISTS0[0]);
		
		boolean ok1=prevcmd.button(bs);
		assertFalse(ok1);
	}

	@Test
	public void shouldStartPrevPlaylistWhilePlaying()
	{
		// While playing, load the prev playlist
		BState bs=loadPlaylistState();
		setStatusPlay(bs,"pause",PLAYLISTS1[0]);
		String load1="load \""+PLAYLISTS0[1]+"\"";
		String load2="load \""+PLAYLISTS0[0]+"\"";
		driver.expectOkRequest("stop");
		driver.expectOkRequest("clear");
		driver.expectOkRequest(load1);
		driver.expectOkRequest(load2);
		
		boolean ok1=prevcmd.button(bs);
		assertTrue(ok1);
		assertEquals(PLAYLISTS0[1],bs.getString(PlaylistDBI.PLAYLIST_LOADED));
		
		List<String> sent1 = driver.getCommandsSent();
		assertEquals(3,sent1.size());
		assertEquals("stop",sent1.get(0));
		assertEquals("clear",sent1.get(1));
		assertEquals(load1,sent1.get(2));

		// Press it again. We're stopped now, so don't expect another stop
		driver.clearCommandsSent();
		setStatusStop(bs);
		boolean ok2=prevcmd.button(bs);
		assertTrue(ok2);
		assertEquals(PLAYLISTS0[0],bs.getString(PlaylistDBI.PLAYLIST_LOADED));
		
		List<String> sent2 = driver.getCommandsSent();
		assertEquals(2,sent2.size());
		assertEquals("clear",sent2.get(0));
		assertEquals(load2,sent2.get(1));
	}

	@Test
	public void shouldStartPrevPlaylistWhilePaused()
	{
		// While paused, load the next playlist
		BState bs=loadPlaylistState();
		setStatusPlay(bs,"pause",PLAYLISTS1[0]);
		String load1="load \""+PLAYLISTS0[1]+"\"";
		String load2="load \""+PLAYLISTS0[0]+"\"";
		driver.expectOkRequest("stop");
		driver.expectOkRequest("clear");
		driver.expectOkRequest(load1);
		driver.expectOkRequest(load2);
		
		boolean ok1=prevcmd.button(bs);
		assertTrue(ok1);
		assertEquals(PLAYLISTS0[1],bs.getString(PlaylistDBI.PLAYLIST_LOADED));
		
		List<String> sent1 = driver.getCommandsSent();
		assertEquals(3,sent1.size());
		assertEquals("stop",sent1.get(0));
		assertEquals("clear",sent1.get(1));
		assertEquals(load1,sent1.get(2));

		// Press it again. We're stopped now, so don't expect another stop
		driver.clearCommandsSent();
		setStatusStop(bs);
		boolean ok2=prevcmd.button(bs);
		assertTrue(ok2);
		assertEquals(PLAYLISTS0[0],bs.getString(PlaylistDBI.PLAYLIST_LOADED));
		
		List<String> sent2 = driver.getCommandsSent();
		assertEquals(2,sent2.size());
		assertEquals("clear",sent2.get(0));
		assertEquals(load2,sent2.get(1));
	}

	
}
