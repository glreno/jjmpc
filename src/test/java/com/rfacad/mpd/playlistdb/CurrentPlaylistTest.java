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
	public static final String FOLDER3="Steven Wright";
	public static final String PLNAME1A="A Place for my Stuff";
	public static final String PLNAME2A="Monty Python and the Holy Grail";
	public static final String PLNAME2B="Monty Python's Life of Brian";
	public static final String PLNAME3A="I Have a Pony";
	public static final String DIR1A=FOLDER1+"/"+PLNAME1A;
	public static final String DIR2A=FOLDER2+"/"+PLNAME2A;
	public static final String DIR2B=FOLDER2+"/"+PLNAME2B;
	public static final String DIR3A=FOLDER3+"/"+PLNAME3A;
	public static final String PLID1A=FOLDER1+PlaylistDBI.SEP+PLNAME1A;
	public static final String PLID2A=FOLDER2+PlaylistDBI.SEP+PLNAME2A;
	public static final String PLID2B=FOLDER2+PlaylistDBI.SEP+PLNAME2B;
	public static final String PLID3A=FOLDER3+PlaylistDBI.SEP+PLNAME3A;
	public static final String CMD_CLEAR = "clear";
	public static final String CMD_LOAD1A = "load \""+PLID1A+"\"";
	public static final String CMD_LOAD2A = "load \""+PLID2A+"\"";
	public static final String CMD_LOAD2B = "load \""+PLID2B+"\"";
	public static final String CMD_LOAD3A = "load \""+PLID3A+"\"";
	public static final String CMD_LISTFOLDERS = "listfiles";
	public static final String CMD_LISTFILES1 = "listfiles \""+FOLDER1+"\"";
	public static final String CMD_LISTFILES1_A = "listfiles \""+FOLDER1+"/"+PLNAME1A+"\"";
	public static final String CMD_LISTFILES2 = "listfiles \""+FOLDER2+"\"";
	public static final String CMD_LISTFILES2_A = "listfiles \""+FOLDER2+"/"+PLNAME2A+"\"";
	public static final String CMD_LISTFILES2_B = "listfiles \""+FOLDER2+"/"+PLNAME2B+"\"";
	public static final String CMD_LISTFILES3 = "listfiles \""+FOLDER3+"\"";
	public static final String CMD_LISTFILES3_A = "listfiles \""+FOLDER3+"/"+PLNAME3A+"\"";
	public static final String CMD_LISTM3U1_A = "listplaylistinfo \""+PLID1A+"\"";
	public static final String CMD_LISTM3U2_A = "listplaylistinfo \""+PLID2A+"\"";
	public static final String CMD_LISTM3U2_B = "listplaylistinfo \""+PLID2B+"\"";
	public static final String CMD_LISTM3U3_A = "listplaylistinfo \""+PLID3A+"\"";
	public static final String CMD_PLAYLISTINFO = "playlistinfo";
	public static final String D = "\tdirectory: ";
	public static final String T = "\tLast-Modified: 2018-07-01T19:40:53Z";
	public static final String FNAME1A_1="Stuff 01.mp3";
	public static final String FNAME1A_2="Stuff 02.mp3";
	public static final String FNAME1A_3="Stuff 03.mp3";
	public static final String FNAME2A_1="Holy Grail 01.mp3";
	public static final String FNAME2A_2="Holy Grail 02.mp3";
	public static final String FNAME2A_3="Holy Grail 03.mp3";
	public static final String FNAME2B_1="Brian 01.mp3";
	public static final String FNAME2B_2="Brian 02.mp3";
	public static final String FNAME2B_3="Brian 03.mp3";
	public static final String FNAME3A_1="Pony 01.mp3";
	public static final String FNAME3A_2="Pony 01.mp3";
	public static final String FNAME3A_3="Pony 01.mp3";
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
		response0.add(D+FOLDER3); response0.add(T);
		mpdc.expectOkRequest(CMD_LISTFOLDERS, response0);

		List<String> response1 = new ArrayList<>();
		response1.add(D+PLNAME1A); response1.add(T);
		mpdc.expectOkRequest(CMD_LISTFILES1, response1);

		List<String> response1_Af = new ArrayList<>();
		response1_Af.add(F+FNAME1A_1); response1_Af.add(T); response1_Af.add(S);
		response1_Af.add(F+FNAME1A_2); response1_Af.add(T); response1_Af.add(S);
		response1_Af.add(F+FNAME1A_3); response1_Af.add(T); response1_Af.add(S);
		mpdc.expectOkRequest(CMD_LISTFILES1_A, response1_Af);

		List<String> response1_Au = new ArrayList<>();
		response1_Au.add(F+DIR1A+"/"+FNAME1A_1); response1_Au.add(T);
		response1_Au.add(F+DIR1A+"/"+FNAME1A_2); response1_Au.add(T);
		response1_Au.add(F+DIR1A+"/"+FNAME1A_3); response1_Au.add(T);
		mpdc.expectOkRequest(CMD_LISTM3U1_A, response1_Au);

		List<String> response2 = new ArrayList<>();
		response2.add(D+PLNAME2A); response2.add(T);
		response2.add(D+PLNAME2B); response2.add(T);
		mpdc.expectOkRequest(CMD_LISTFILES2, response2);

		List<String> response2_Af = new ArrayList<>();
		response2_Af.add(F+FNAME2A_1); response2_Af.add(T); response2_Af.add(S);
		response2_Af.add(F+FNAME2A_2); response2_Af.add(T); response2_Af.add(S);
		response2_Af.add(F+FNAME2A_3); response2_Af.add(T); response2_Af.add(S);
		mpdc.expectOkRequest(CMD_LISTFILES2_A, response2_Af);
		
		List<String> response2_Au = new ArrayList<>();
		response2_Au.add(F+DIR2A+"/"+FNAME2A_1); response2_Au.add(T);
		response2_Au.add(F+DIR2A+"/"+FNAME2A_2); response2_Au.add(T);
		response2_Au.add(F+DIR2A+"/"+FNAME2A_3); response2_Au.add(T);
		mpdc.expectOkRequest(CMD_LISTM3U2_A, response2_Au);

		List<String> response2_Bf = new ArrayList<>();
		response2_Bf.add(F+FNAME2B_1); response2_Bf.add(T); response2_Bf.add(S);
		response2_Bf.add(F+FNAME2B_2); response2_Bf.add(T); response2_Bf.add(S);
		response2_Bf.add(F+FNAME2B_3); response2_Bf.add(T); response2_Bf.add(S);
		mpdc.expectOkRequest(CMD_LISTFILES2_B, response2_Bf);
		
		List<String> response2_Bu = new ArrayList<>();
		response2_Bu.add(F+DIR2B+"/"+FNAME2B_1); response2_Bu.add(T);
		response2_Bu.add(F+DIR2B+"/"+FNAME2B_2); response2_Bu.add(T);
		response2_Bu.add(F+DIR2B+"/"+FNAME2B_3); response2_Bu.add(T);
		mpdc.expectOkRequest(CMD_LISTM3U2_B, response2_Bu);

		List<String> response3 = new ArrayList<>();
		response3.add(D+PLNAME3A); response3.add(T);
		mpdc.expectOkRequest(CMD_LISTFILES3, response3);

		List<String> response3_Af = new ArrayList<>();
		response3_Af.add(F+FNAME3A_1); response3_Af.add(T); response3_Af.add(S);
		response3_Af.add(F+FNAME3A_2); response3_Af.add(T); response3_Af.add(S);
		response3_Af.add(F+FNAME3A_3); response3_Af.add(T); response3_Af.add(S);
		mpdc.expectOkRequest(CMD_LISTFILES3_A, response3_Af);

		List<String> response3_Au = new ArrayList<>();
		response3_Au.add(F+DIR3A+"/"+FNAME3A_1); response3_Au.add(T);
		response3_Au.add(F+DIR3A+"/"+FNAME3A_2); response3_Au.add(T);
		response3_Au.add(F+DIR3A+"/"+FNAME3A_3); response3_Au.add(T);
		mpdc.expectOkRequest(CMD_LISTM3U3_A, response3_Au);
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
		assertEquals(PLID1A,ret2);

		BState bs3=new MockBState();
		String ret3 = pldb.getPrevPlaylistFolderFirstPlaylist(bs3);
		assertNull(ret3);

		BState bs4=new MockBState();
		String ret4 = pldb.getNextPlaylistFolderFirstPlaylist(bs4);
		assertEquals(PLID1A,ret4);
	}
	
	@Test
	public void shouldReturnCurrentPlaylistWhenPlayingAfterStartup()
	{
		// press play
		MockBState bs0=new MockBState();
		bs0.setString("state", "play");
		bs0.setString("songid", "16");
		BState bs2=new MockBState(bs0);
		BState bs3=new MockBState(bs0);
		BState bs4=new MockBState(bs0);
		BState bs5=new MockBState(bs0);

		// the DB is going to see 'songid 16',
		// find out what the filename is from playlistinfo
		// and build the playlistid from that
		List<String> response2_2 = new ArrayList<>();
		response2_2.add(F+DIR2A+"/"+FNAME2A_1); response2_2.add(T); response2_2.add(I+"15");
		response2_2.add(F+DIR2A+"/"+FNAME2A_2); response2_2.add(T); response2_2.add(I+"16");
		response2_2.add(F+DIR2A+"/"+FNAME2A_3); response2_2.add(T); response2_2.add(I+"17");
		mpdc.expectOkRequest(CMD_PLAYLISTINFO, response2_2);
		

		// check previous playlist
		String ret0 = pldb.getPrevPlaylist(bs0);
		assertEquals(PLID1A,ret0);

		// check current playlist
		String ret2 = pldb.getMostRecentPlaylist(bs2);
		assertEquals(PLID2A,ret2);
		
		// check next playlist
		String ret3 = pldb.getNextPlaylist(bs3);
		assertEquals(PLID2B,ret3);
		
		// check previous playlist folder
		String ret4 = pldb.getPrevPlaylistFolderFirstPlaylist(bs4);
		assertEquals(PLID1A,ret4);

		// check next playlist folder
		String ret5 = pldb.getNextPlaylistFolderFirstPlaylist(bs5);
		assertEquals(PLID3A,ret5);

	}

	@Test
	public void shouldReturnCurrentPlaylistWhenPausedAfterStartup()
	{
		// press pause
		MockBState bs0=new MockBState();
		bs0.setString("state", "pause");
		bs0.setString("songid", "16");
		BState bs2=new MockBState(bs0);
		BState bs3=new MockBState(bs0);
		BState bs4=new MockBState(bs0);
		BState bs5=new MockBState(bs0);

		
		// the DB is going to see 'songid 16',
		// find out what the filename is from playlistinfo
		// and build the playlistid from that
		List<String> response2_2 = new ArrayList<>();
		response2_2.add(F+DIR2A+"/"+FNAME2A_1); response2_2.add(T); response2_2.add(I+"15");
		response2_2.add(F+DIR2A+"/"+FNAME2A_2); response2_2.add(T); response2_2.add(I+"16");
		response2_2.add(F+DIR2A+"/"+FNAME2A_3); response2_2.add(T); response2_2.add(I+"17");
		mpdc.expectOkRequest(CMD_PLAYLISTINFO, response2_2);

		// check previous playlist
		String ret0 = pldb.getPrevPlaylist(bs0);
		assertEquals(PLID1A,ret0);

		// check current playlist
		String ret2 = pldb.getMostRecentPlaylist(bs2);
		assertEquals(PLID2A,ret2);

		// check next playlist
		String ret3 = pldb.getNextPlaylist(bs3);
		assertEquals(PLID2B,ret3);

		// check previous playlist folder
		String ret4 = pldb.getPrevPlaylistFolderFirstPlaylist(bs0);
		assertEquals(PLID1A,ret4);

		// check next playlist folder
		String ret5 = pldb.getNextPlaylistFolderFirstPlaylist(bs3);
		assertEquals(PLID3A,ret5);

	}

	@Test
	public void shouldReturnCurrentPlaylistWhenPlayingAfterLoad1A()
	{
		// load a playlist
		mpdc.expectOkRequest(CMD_CLEAR, Collections.emptyList());
		mpdc.expectOkRequest(CMD_LOAD1A, Collections.emptyList());
		BState bs1=new MockBState();
		boolean ret1 = pldb.loadPlaylist(bs1, PLID1A);
		assertTrue(ret1);
		assertEquals(PLID1A,bs1.getString(PlaylistDBI.PLAYLIST_LOADED));

		// press play
		MockBState bs0=new MockBState();
		bs0.setString("state", "play");
		bs0.setString("songid", "16");
		BState bs2=new MockBState(bs0);
		BState bs3=new MockBState(bs0);
		BState bs4=new MockBState(bs0);
		BState bs5=new MockBState(bs0);
		
		// check previous playlist
		String ret0 = pldb.getPrevPlaylist(bs0);
		assertNull(ret0);

		// check current playlist
		String ret2 = pldb.getMostRecentPlaylist(bs2);
		assertEquals(PLID1A,ret2);

		// check next playlist
		String ret3 = pldb.getNextPlaylist(bs3);
		assertEquals(PLID2A,ret3);

		// check previous playlist folder
		String ret4 = pldb.getPrevPlaylistFolderFirstPlaylist(bs0);
		assertNull(ret4);

		// check next playlist folder
		String ret5 = pldb.getNextPlaylistFolderFirstPlaylist(bs3);
		assertEquals(PLID2A,ret5);
}

	@Test
	public void shouldReturnCurrentPlaylistWhenPlayingAfterLoad2A()
	{
		// load a playlist
		mpdc.expectOkRequest(CMD_CLEAR, Collections.emptyList());
		mpdc.expectOkRequest(CMD_LOAD2A, Collections.emptyList());
		BState bs1=new MockBState();
		boolean ret1 = pldb.loadPlaylist(bs1, PLID2A);
		assertTrue(ret1);
		assertEquals(PLID2A,bs1.getString(PlaylistDBI.PLAYLIST_LOADED));

		// press play
		MockBState bs0=new MockBState();
		bs0.setString("state", "play");
		bs0.setString("songid", "16");
		BState bs2=new MockBState(bs0);
		BState bs3=new MockBState(bs0);
		BState bs4=new MockBState(bs0);
		BState bs5=new MockBState(bs0);
		
		// check previous playlist
		String ret0 = pldb.getPrevPlaylist(bs0);
		assertEquals(PLID1A,ret0);

		// check current playlist
		String ret2 = pldb.getMostRecentPlaylist(bs2);
		assertEquals(PLID2A,ret2);

		// check next playlist
		String ret3 = pldb.getNextPlaylist(bs3);
		assertEquals(PLID2B,ret3);

		// check previous playlist folder
		String ret4 = pldb.getPrevPlaylistFolderFirstPlaylist(bs0);
		assertEquals(PLID1A,ret4);

		// check next playlist folder
		String ret5 = pldb.getNextPlaylistFolderFirstPlaylist(bs3);
		assertEquals(PLID3A,ret5);
	}

	@Test
	public void shouldReturnCurrentPlaylistWhenPlayingAfterLoad2B()
	{
		// load a playlist
		mpdc.expectOkRequest(CMD_CLEAR, Collections.emptyList());
		mpdc.expectOkRequest(CMD_LOAD2B, Collections.emptyList());
		BState bs1=new MockBState();
		boolean ret1 = pldb.loadPlaylist(bs1, PLID2B);
		assertTrue(ret1);
		assertEquals(PLID2B,bs1.getString(PlaylistDBI.PLAYLIST_LOADED));

		// press play
		MockBState bs0=new MockBState();
		bs0.setString("state", "play");
		bs0.setString("songid", "16");
		BState bs2=new MockBState(bs0);
		BState bs3=new MockBState(bs0);
		BState bs4=new MockBState(bs0);
		BState bs5=new MockBState(bs0);
		
		// check previous playlist
		String ret0 = pldb.getPrevPlaylist(bs0);
		assertEquals(PLID2A,ret0);

		// check current playlist
		String ret2 = pldb.getMostRecentPlaylist(bs2);
		assertEquals(PLID2B,ret2);

		// check next playlist
		String ret3 = pldb.getNextPlaylist(bs3);
		assertEquals(PLID3A,ret3);

		// check previous playlist folder
		String ret4 = pldb.getPrevPlaylistFolderFirstPlaylist(bs0);
		assertEquals(PLID1A,ret4);

		// check next playlist folder
		String ret5 = pldb.getNextPlaylistFolderFirstPlaylist(bs3);
		assertEquals(PLID3A,ret5);

	}

	@Test
	public void shouldReturnCurrentPlaylistWhenPlayingAfterLoad3A()
	{
		// load a playlist
		mpdc.expectOkRequest(CMD_CLEAR, Collections.emptyList());
		mpdc.expectOkRequest(CMD_LOAD3A, Collections.emptyList());
		BState bs1=new MockBState();
		boolean ret1 = pldb.loadPlaylist(bs1, PLID3A);
		assertTrue(ret1);
		assertEquals(PLID3A,bs1.getString(PlaylistDBI.PLAYLIST_LOADED));

		// press play
		MockBState bs0=new MockBState();
		bs0.setString("state", "play");
		bs0.setString("songid", "16");
		BState bs2=new MockBState(bs0);
		BState bs3=new MockBState(bs0);
		BState bs4=new MockBState(bs0);
		BState bs5=new MockBState(bs0);
		
		// check previous playlist
		String ret0 = pldb.getPrevPlaylist(bs0);
		assertEquals(PLID2B,ret0);

		// check current playlist
		String ret2 = pldb.getMostRecentPlaylist(bs2);
		assertEquals(PLID3A,ret2);

		// check next playlist
		String ret3 = pldb.getNextPlaylist(bs3);
		assertEquals(PLID1A,ret3);

		// check previous playlist folder
		String ret4 = pldb.getPrevPlaylistFolderFirstPlaylist(bs0);
		assertEquals(PLID2A,ret4);

		// check next playlist folder
		String ret5 = pldb.getNextPlaylistFolderFirstPlaylist(bs3);
		assertEquals(PLID1A,ret5);

	}

	@Test
	public void shouldReturnCurrentPlaylistWhenPausedAfterLoad()
	{
		// load a playlist
		mpdc.expectOkRequest(CMD_CLEAR, Collections.emptyList());
		mpdc.expectOkRequest(CMD_LOAD2A, Collections.emptyList());
		BState bs1=new MockBState();
		boolean ret1 = pldb.loadPlaylist(bs1, PLID2A);
		assertTrue(ret1);
		assertEquals(PLID2A,bs1.getString(PlaylistDBI.PLAYLIST_LOADED));

		// press pause
		MockBState bs0=new MockBState();
		bs0.setString("state", "pause");
		bs0.setString("songid", "16");
		BState bs2=new MockBState(bs0);
		BState bs3=new MockBState(bs0);
		BState bs4=new MockBState(bs0);
		BState bs5=new MockBState(bs0);
		
		// check previous playlist
		String ret0 = pldb.getPrevPlaylist(bs0);
		assertEquals(PLID1A,ret0);

		// check current playlist
		String ret2 = pldb.getMostRecentPlaylist(bs2);
		assertEquals(PLID2A,ret2);
		
		// check next playlist
		String ret3 = pldb.getNextPlaylist(bs3);
		assertEquals(PLID2B,ret3);

		// check previous playlist folder
		String ret4 = pldb.getPrevPlaylistFolderFirstPlaylist(bs0);
		assertEquals(PLID1A,ret4);

		// check next playlist folder
		String ret5 = pldb.getNextPlaylistFolderFirstPlaylist(bs3);
		assertEquals(PLID3A,ret5);
	}
	

	public void shouldReturnNextPlaylistWhenCurrentDirDeleted()
	{
		// not sure if even possible,
		// or why we would bother
	}
}
