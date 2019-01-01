package com.rfacad.mpd.playlistdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.rfacad.buttons.interfaces.BState;
import com.rfacad.jjmpc.MockBState;
import com.rfacad.jjmpc.MockMpdClient;
import com.rfacad.mpd.interfaces.PlaylistDBI;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CurrentPlaylistTest {

	public static final String FOLDER1="George Carlin";
	public static final String FOLDER2="Monty Python";
	public static final String PLNAME1="A Place for my Stuff";
	public static final String PLNAME2="Monty Python and the Holy Grail";
	public static final String PLNAME3="Monty Python's Life of Brian";
	public static final String DIR1=FOLDER1+"/"+PLNAME1;
	public static final String DIR2=FOLDER2+"/"+PLNAME2;
	public static final String DIR3=FOLDER2+"/"+PLNAME3;
	public static final String PLID1=FOLDER1+PlaylistDBI.SEP+PLNAME1;
	public static final String PLID2=FOLDER2+PlaylistDBI.SEP+PLNAME2;
	public static final String PLID3=FOLDER2+PlaylistDBI.SEP+PLNAME3;
	public static final String CMD_CLEAR = "clear";
	public static final String CMD_LOAD1 = "load \""+PLID1+"\"";
	public static final String CMD_LOAD2 = "load \""+PLID2+"\"";
	public static final String CMD_LOAD3 = "load \""+PLID3+"\"";
	public static final String CMD_LISTFOLDERS = "listfiles";
	public static final String CMD_LISTFILES1 = "listfiles \""+FOLDER1+"\"";
	public static final String CMD_LISTFILES1_1 = "listfiles \""+FOLDER1+"/"+PLNAME1+"\"";
	public static final String CMD_LISTFILES2 = "listfiles \""+FOLDER2+"\"";
	public static final String CMD_LISTFILES2_1 = "listfiles \""+FOLDER2+"/"+PLNAME2+"\"";
	public static final String CMD_LISTFILES3_1 = "listfiles \""+FOLDER2+"/"+PLNAME3+"\"";
	public static final String CMD_LISTM3U1_1 = "listplaylistinfo \""+PLID1+"\"";
	public static final String CMD_LISTM3U2_1 = "listplaylistinfo \""+PLID2+"\"";
	public static final String CMD_LISTM3U3_1 = "listplaylistinfo \""+PLID3+"\"";
	public static final String CMD_PLAYLISTINFO = "playlistinfo";
	public static final String D = "\tdirectory: ";
	public static final String T = "\tLast-Modified: 2018-07-01T19:40:53Z";
	public static final String FNAME1_1="Stuff 01.mp3";
	public static final String FNAME1_2="Stuff 02.mp3";
	public static final String FNAME1_3="Stuff 03.mp3";
	public static final String FNAME2_1="Holy Grail 01.mp3";
	public static final String FNAME2_2="Holy Grail 02.mp3";
	public static final String FNAME2_3="Holy Grail 03.mp3";
	public static final String FNAME3_1="Brian 01.mp3";
	public static final String FNAME3_2="Brian 02.mp3";
	public static final String FNAME3_3="Brian 03.mp3";
	public static final String F = "\tfile: ";
	public static final String S = "\tsize: 2267191";
	public static final String I = "\tId: ";

	private MockMpdClient mpdc;
	private PlaylistDBI pldb;
	
	@Before
	public void setup()
	{
		mpdc=new MockMpdClient();
		pldb=new PlaylistDB(mpdc); 

		List<String> response0 = new ArrayList<>();
		response0.add(D+FOLDER1); response0.add(T);
		response0.add(D+FOLDER2); response0.add(T);
		mpdc.expectOkRequest(CMD_LISTFOLDERS, response0);

		List<String> response1 = new ArrayList<>();
		response1.add(D+PLNAME1); response1.add(T);
		mpdc.expectOkRequest(CMD_LISTFILES1, response1);

		List<String> response1_1 = new ArrayList<>();
		response1_1.add(F+FNAME1_1); response1_1.add(T); response1_1.add(S);
		response1_1.add(F+FNAME1_2); response1_1.add(T); response1_1.add(S);
		response1_1.add(F+FNAME1_3); response1_1.add(T); response1_1.add(S);
		mpdc.expectOkRequest(CMD_LISTFILES1_1, response1_1);

		List<String> response1_2 = new ArrayList<>();
		response1_2.add(F+DIR1+"/"+FNAME1_1); response1_2.add(T);
		response1_2.add(F+DIR1+"/"+FNAME1_2); response1_2.add(T);
		response1_2.add(F+DIR1+"/"+FNAME1_3); response1_2.add(T);
		mpdc.expectOkRequest(CMD_LISTM3U1_1, response1_2);

		
		List<String> response2 = new ArrayList<>();
		response2.add(D+PLNAME2); response2.add(T);
		response2.add(D+PLNAME3); response2.add(T);
		mpdc.expectOkRequest(CMD_LISTFILES2, response2);

		List<String> response2_1 = new ArrayList<>();
		response2_1.add(F+FNAME2_1); response2_1.add(T); response2_1.add(S);
		response2_1.add(F+FNAME2_2); response2_1.add(T); response2_1.add(S);
		response2_1.add(F+FNAME2_3); response2_1.add(T); response2_1.add(S);
		mpdc.expectOkRequest(CMD_LISTFILES2_1, response2_1);
		
		List<String> response2_2 = new ArrayList<>();
		response2_2.add(F+DIR2+"/"+FNAME2_1); response2_2.add(T);
		response2_2.add(F+DIR2+"/"+FNAME2_2); response2_2.add(T);
		response2_2.add(F+DIR2+"/"+FNAME2_3); response2_2.add(T);
		mpdc.expectOkRequest(CMD_LISTM3U2_1, response2_2);

		List<String> response3_1 = new ArrayList<>();
		response3_1.add(F+FNAME3_1); response3_1.add(T); response3_1.add(S);
		response3_1.add(F+FNAME3_2); response3_1.add(T); response3_1.add(S);
		response3_1.add(F+FNAME3_3); response3_1.add(T); response3_1.add(S);
		mpdc.expectOkRequest(CMD_LISTFILES3_1, response3_1);
		
		List<String> response3_2 = new ArrayList<>();
		response3_2.add(F+DIR3+"/"+FNAME3_1); response3_2.add(T);
		response3_2.add(F+DIR3+"/"+FNAME3_2); response3_2.add(T);
		response3_2.add(F+DIR3+"/"+FNAME3_3); response3_2.add(T);
		mpdc.expectOkRequest(CMD_LISTM3U3_1, response3_2);

	}
	
	@Test
	public void shouldReturnNullOnStartup()
	{
		BState bs0=new MockBState();
		String ret0 = pldb.getPrevPlaylist(bs0);
		assertNull(ret0);

		BState bs1=new MockBState();
		String ret1 = pldb.getMostRecentPlaylist(bs1);
		assertNull(ret1);

		BState bs2=new MockBState();
		String ret2 = pldb.getNextPlaylist(bs2);
		assertEquals(PLID1,ret2);
	}
	
	@Test
	public void shouldReturnCurrentPlaylistWhenPlayingAfterStartup()
	{
		// press play
		BState bs0=new MockBState();
		bs0.setString("state", "play");
		bs0.setString("songid", "16");
		BState bs2=new MockBState();
		bs2.setString("state", "play");
		bs2.setString("songid", "16");
		BState bs3=new MockBState();
		bs3.setString("state", "play");
		bs3.setString("songid", "16");

		// the DB is going to see 'songid 16',
		// find out what the filename is from playlistinfo
		// and build the playlistid from that
		List<String> response2_2 = new ArrayList<>();
		response2_2.add(F+DIR2+"/"+FNAME2_1); response2_2.add(T); response2_2.add(I+"15");
		response2_2.add(F+DIR2+"/"+FNAME2_2); response2_2.add(T); response2_2.add(I+"16");
		response2_2.add(F+DIR2+"/"+FNAME2_3); response2_2.add(T); response2_2.add(I+"17");
		mpdc.expectOkRequest(CMD_PLAYLISTINFO, response2_2);
		
		// check previous playlist
		String ret0 = pldb.getPrevPlaylist(bs0);
		assertEquals(PLID1,ret0);

		// check current playlist
		String ret2 = pldb.getMostRecentPlaylist(bs2);
		assertEquals(PLID2,ret2);
		
		// check next playlist
		String ret3 = pldb.getNextPlaylist(bs3);
		assertEquals(PLID3,ret3);
	}

	@Test
	public void shouldReturnCurrentPlaylistWhenPausedAfterStartup()
	{
		// press pause
		BState bs0=new MockBState();
		bs0.setString("state", "pause");
		bs0.setString("songid", "16");
		BState bs2=new MockBState();
		bs2.setString("state", "pause");
		bs2.setString("songid", "16");
		BState bs3=new MockBState();
		bs3.setString("state", "pause");
		bs3.setString("songid", "16");

		
		// the DB is going to see 'songid 16',
		// find out what the filename is from playlistinfo
		// and build the playlistid from that
		List<String> response2_2 = new ArrayList<>();
		response2_2.add(F+DIR2+"/"+FNAME2_1); response2_2.add(T); response2_2.add(I+"15");
		response2_2.add(F+DIR2+"/"+FNAME2_2); response2_2.add(T); response2_2.add(I+"16");
		response2_2.add(F+DIR2+"/"+FNAME2_3); response2_2.add(T); response2_2.add(I+"17");
		mpdc.expectOkRequest(CMD_PLAYLISTINFO, response2_2);

		// check previous playlist
		String ret0 = pldb.getPrevPlaylist(bs0);
		assertEquals(PLID1,ret0);

		// check current playlist
		String ret2 = pldb.getMostRecentPlaylist(bs2);
		assertEquals(PLID2,ret2);

		// check next playlist
		String ret3 = pldb.getNextPlaylist(bs3);
		assertEquals(PLID3,ret3);

	}

	@Test
	public void shouldReturnCurrentPlaylistWhenPlayingAfterLoad1()
	{
		// load a playlist
		mpdc.expectOkRequest(CMD_CLEAR, Collections.emptyList());
		mpdc.expectOkRequest(CMD_LOAD1, Collections.emptyList());
		BState bs1=new MockBState();
		boolean ret1 = pldb.loadPlaylist(bs1, PLID1);
		assertTrue(ret1);
		assertEquals(PLID1,bs1.getString(PlaylistDBI.PLAYLIST_LOADED));

		// press play
		BState bs0=new MockBState();
		bs0.setString("state", "play");
		bs0.setString("songid", "16");
		BState bs2=new MockBState();
		bs2.setString("state", "play");
		bs2.setString("songid", "16");
		BState bs3=new MockBState();
		bs3.setString("state", "play");
		bs3.setString("songid", "16");
		
		// check previous playlist
		String ret0 = pldb.getPrevPlaylist(bs0);
		assertNull(ret0);

		// check current playlist
		String ret2 = pldb.getMostRecentPlaylist(bs2);
		assertEquals(PLID1,ret2);

		// check next playlist
		String ret3 = pldb.getNextPlaylist(bs3);
		assertEquals(PLID2,ret3);
	}

	@Test
	public void shouldReturnCurrentPlaylistWhenPlayingAfterLoad2()
	{
		// load a playlist
		mpdc.expectOkRequest(CMD_CLEAR, Collections.emptyList());
		mpdc.expectOkRequest(CMD_LOAD2, Collections.emptyList());
		BState bs1=new MockBState();
		boolean ret1 = pldb.loadPlaylist(bs1, PLID2);
		assertTrue(ret1);
		assertEquals(PLID2,bs1.getString(PlaylistDBI.PLAYLIST_LOADED));

		// press play
		BState bs0=new MockBState();
		bs0.setString("state", "play");
		bs0.setString("songid", "16");
		BState bs2=new MockBState();
		bs2.setString("state", "play");
		bs2.setString("songid", "16");
		BState bs3=new MockBState();
		bs3.setString("state", "play");
		bs3.setString("songid", "16");
		
		// check previous playlist
		String ret0 = pldb.getPrevPlaylist(bs0);
		assertEquals(PLID1,ret0);

		// check current playlist
		String ret2 = pldb.getMostRecentPlaylist(bs2);
		assertEquals(PLID2,ret2);

		// check next playlist
		String ret3 = pldb.getNextPlaylist(bs3);
		assertEquals(PLID3,ret3);
	}

	@Test
	public void shouldReturnCurrentPlaylistWhenPlayingAfterLoad3()
	{
		// load a playlist
		mpdc.expectOkRequest(CMD_CLEAR, Collections.emptyList());
		mpdc.expectOkRequest(CMD_LOAD3, Collections.emptyList());
		BState bs1=new MockBState();
		boolean ret1 = pldb.loadPlaylist(bs1, PLID3);
		assertTrue(ret1);
		assertEquals(PLID3,bs1.getString(PlaylistDBI.PLAYLIST_LOADED));

		// press play
		BState bs0=new MockBState();
		bs0.setString("state", "play");
		bs0.setString("songid", "16");
		BState bs2=new MockBState();
		bs2.setString("state", "play");
		bs2.setString("songid", "16");
		BState bs3=new MockBState();
		bs3.setString("state", "play");
		bs3.setString("songid", "16");
		
		// check previous playlist
		String ret0 = pldb.getPrevPlaylist(bs0);
		assertEquals(PLID2,ret0);

		// check current playlist
		String ret2 = pldb.getMostRecentPlaylist(bs2);
		assertEquals(PLID3,ret2);

		// check next playlist
		String ret3 = pldb.getNextPlaylist(bs3);
		assertEquals(PLID1,ret3);
	}

	@Test
	public void shouldReturnCurrentPlaylistWhenPausedAfterLoad()
	{
		// load a playlist
		mpdc.expectOkRequest(CMD_CLEAR, Collections.emptyList());
		mpdc.expectOkRequest(CMD_LOAD2, Collections.emptyList());
		BState bs1=new MockBState();
		boolean ret1 = pldb.loadPlaylist(bs1, PLID2);
		assertTrue(ret1);
		assertEquals(PLID2,bs1.getString(PlaylistDBI.PLAYLIST_LOADED));

		// press pause
		BState bs0=new MockBState();
		bs0.setString("state", "pause");
		bs0.setString("songid", "16");
		BState bs2=new MockBState();
		bs2.setString("state", "pause");
		bs2.setString("songid", "16");
		BState bs3=new MockBState();
		bs3.setString("state", "pause");
		bs3.setString("songid", "16");
		
		// check previous playlist
		String ret0 = pldb.getPrevPlaylist(bs0);
		assertEquals(PLID1,ret0);

		// check current playlist
		String ret2 = pldb.getMostRecentPlaylist(bs2);
		assertEquals(PLID2,ret2);
		
		// check next playlist
		String ret3 = pldb.getNextPlaylist(bs3);
		assertEquals(PLID3,ret3);
	}
	

	public void shouldReturnNextPlaylistWhenCurrentDirDeleted()
	{
		// not sure if even possible,
		// or why we would bother
	}
}
