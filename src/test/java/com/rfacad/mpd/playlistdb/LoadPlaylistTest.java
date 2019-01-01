package com.rfacad.mpd.playlistdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
public class LoadPlaylistTest {
	
	public static final String FOLDER="Monty Python";
	public static final String PLNAME1="Monty Python and the Holy Grail";
	public static final String DIR=FOLDER+"/"+PLNAME1+"/";
	public static final String PLID1=FOLDER+PlaylistDBI.SEP+PLNAME1;
	public static final String CACHEKEY1 = PlaylistDBI.FILES_IN_PLAYLIST_PREFIX+PLID1;
	public static final String CMD_LISTFILES = "listfiles \""+FOLDER+"/"+PLNAME1+"\"";
	public static final String CMD_LISTM3U = "listplaylistinfo \""+PLID1+"\"";
	public static final String CMD_UPDATE = "update";
	public static final String CMD_CLEAR = "clear";
	public static final String CMD_CLEARM3U = "clear \""+PLID1+"\"";
	public static final String CMD_LOAD = "load \""+PLID1+"\"";
	public static final String FNAME1="Holy Grail 01.mp3";
	public static final String FNAME2="Holy Grail 02.mp3";
	public static final String FNAME3="Holy Grail 03.mp3";
	public static final String FNAME4="Holy Grail 04.mp3";
	public static final String F = "\tfile: ";
	public static final String S = "\tsize: 2267191";
	public static final String T = "\tLast-Modified: 2018-07-01T19:40:53Z";
	
	public static final List<String> FILELISTING = new ArrayList<>();
	public static final List<String> M3ULISTING = new ArrayList<>();
	
	static {
		FILELISTING.add(F+FNAME1); FILELISTING.add(S); FILELISTING.add(T);
		FILELISTING.add(F+FNAME2); FILELISTING.add(S); FILELISTING.add(T);
		FILELISTING.add(F+FNAME3); FILELISTING.add(S); FILELISTING.add(T);
		
		M3ULISTING.add(F+DIR+FNAME1);
		M3ULISTING.add(F+DIR+FNAME2);
		M3ULISTING.add(F+DIR+FNAME3);
	}

	
	private MockMpdClient mpdc;
	private PlaylistDBI pldb;
	
	@Before
	public void setup()
	{
		 mpdc=new MockMpdClient();
		 pldb=new PlaylistDB(mpdc); 
	}

	@Test
	public void shouldNotLoadNonexistantDir()
	{
		List<String> response1 = new ArrayList<>();
		String listcmd="listfiles \"nonexistant/dir\"";
		mpdc.expectFailRequest(listcmd,"ACK [52@0] {listfiles} No such file or directory", response1); // no files -- empty response
		BState bs1=new MockBState();
		boolean ret = pldb.loadPlaylist(bs1, "nonexistant--dir");
		assertFalse(ret);
		
		List<String> sent = mpdc.getCommandsSent();
		assertEquals(1,sent.size());
		assertEquals(listcmd,sent.get(0));
	}
	
	@Test
	public void shouldNotLoadEmptyDir()
	{
		List<String> response1 = new ArrayList<>();
		mpdc.expectOkRequest(CMD_LISTFILES, response1);
		BState bs1=new MockBState();
		boolean ret = pldb.loadPlaylist(bs1, PLID1);
		assertFalse(ret);

		List<String> sent = mpdc.getCommandsSent();
		assertEquals(1,sent.size());
		assertEquals(CMD_LISTFILES,sent.get(0));
	}

	@Test
	public void shouldNotLoadInvalidPlaylist()
	{
		List<String> response1 = new ArrayList<>();
		mpdc.expectOkRequest(CMD_LISTFILES, response1);
		BState bs1=new MockBState();
		boolean ret1 = pldb.loadPlaylist(bs1, "notaplaylist--");
		assertFalse(ret1);
		boolean ret2 = pldb.loadPlaylist(bs1, "--notaplaylist");
		assertFalse(ret2);
		boolean ret3 = pldb.loadPlaylist(bs1, "");
		assertFalse(ret3);
		boolean ret4 = pldb.loadPlaylist(bs1, "foo");
		assertFalse(ret4);

		List<String> sent = mpdc.getCommandsSent();
		assertEquals(0,sent.size());
	}

	
	@Test
	public void shouldLoadExistingPlaylist()
	{
		// Expected interactions:
		// get files in playlist from db (get files in dir from mpd)
		// get files in m3u from mpd (does not exist; it will be created at this point)
		// update
		// add files to m3u
		// load m3u
		String [] expected=new String [] {
				CMD_LISTFILES,	// 0
				CMD_LISTM3U,	// 1
				CMD_CLEAR,		// 2
				CMD_LOAD		// 3
		};
		
		mpdc.expectOkRequest(expected[0], FILELISTING);
		mpdc.expectOkRequest(expected[1], M3ULISTING);
		mpdc.expectOkRequest(expected[2], Collections.emptyList());
		mpdc.expectOkRequest(expected[3], Collections.emptyList());
		
		BState bs1=new MockBState();
		boolean ret = pldb.loadPlaylist(bs1, PLID1);
		assertTrue(ret);
		assertEquals(PLID1,bs1.getString(PlaylistDBI.PLAYLIST_LOADED));
		
		List<String> sent = mpdc.getCommandsSent();
		assertEquals(expected.length,sent.size());
		for(int i=0;i<expected.length;i++)
		{
			assertEquals(expected[i],sent.get(i));
		}
	}

	@Test
	public void shouldLoadNewPlaylist()
	{
		// Expected interactions:
		// get files in playlist from db (get files in dir from mpd)
		// get files in m3u from mpd (does not exist; it will be created at this point)
		// update
		// add files to m3u
		// load m3u
		String [] expected=new String [] {
				CMD_LISTFILES,	// 0
				CMD_LISTM3U,	// 1
				CMD_UPDATE,		// 2
				CMD_CLEARM3U,	// 3
				"playlistadd \""+PLID1+"\" \""+DIR+FNAME1+"\"",
				"playlistadd \""+PLID1+"\" \""+DIR+FNAME2+"\"",
				"playlistadd \""+PLID1+"\" \""+DIR+FNAME3+"\"",
				CMD_CLEAR,		// 7
				CMD_LOAD		// 8
		};
		//for(int i=0;i<8;i++) System.err.println(expected[i]);
		mpdc.expectOkRequest(expected[0], FILELISTING);
		mpdc.expectOkRequest(expected[1], Collections.emptyList());
		mpdc.expectOkRequest(expected[2], Collections.singletonList("\tupdating_db: 3"));
		for(int i=3;i<9;i++)
			mpdc.expectOkRequest(expected[i], Collections.emptyList());
		
		BState bs1=new MockBState();
		boolean ret = pldb.loadPlaylist(bs1, PLID1);
		assertTrue(ret);
		assertEquals(PLID1,bs1.getString(PlaylistDBI.PLAYLIST_LOADED));
		
		List<String> sent = mpdc.getCommandsSent();
		assertEquals(expected.length,sent.size());
		for(int i=0;i<expected.length;i++)
		{
			assertEquals(expected[i],sent.get(i));
		}
	}

	@Test
	public void shouldRecreateLongerPlaylist()
	{
		// Expected interactions:
		// get files in playlist from db (get files in dir from mpd)
		// get files in m3u from mpd (does not exist; it will be created at this point)
		// update
		// add files to m3u
		// load m3u
		String [] expected=new String [] {
				CMD_LISTFILES,	// 0
				CMD_LISTM3U,	// 1
				CMD_UPDATE,		// 2
				CMD_CLEARM3U,	// 3
				"playlistadd \""+PLID1+"\" \""+DIR+FNAME1+"\"",
				"playlistadd \""+PLID1+"\" \""+DIR+FNAME2+"\"",
				"playlistadd \""+PLID1+"\" \""+DIR+FNAME3+"\"",
				CMD_CLEAR,		// 7
				CMD_LOAD		// 8
		};
		//for(int i=0;i<expected.length;i++) System.err.println(expected[i]);
		
		List<String> moddedlisting=new ArrayList<>();
		moddedlisting.add(F+DIR+FNAME1);

		mpdc.expectOkRequest(expected[0], FILELISTING);
		mpdc.expectOkRequest(expected[1], moddedlisting);
		mpdc.expectOkRequest(expected[2], Collections.singletonList("\tupdating_db: 3"));
		for(int i=3;i<9;i++)
			mpdc.expectOkRequest(expected[i], Collections.emptyList());
		
		BState bs1=new MockBState();
		boolean ret = pldb.loadPlaylist(bs1, PLID1);
		assertTrue(ret);
		assertEquals(PLID1,bs1.getString(PlaylistDBI.PLAYLIST_LOADED));
		
		List<String> sent = mpdc.getCommandsSent();
		assertEquals(expected.length,sent.size());
		for(int i=0;i<expected.length;i++)
		{
			assertEquals(expected[i],sent.get(i));
		}
	}

	@Test
	public void shouldRecreateModifiedPlaylist()
	{
		// Expected interactions:
		// get files in playlist from db (get files in dir from mpd)
		// get files in m3u from mpd (does not exist; it will be created at this point)
		// update
		// add files to m3u
		// load m3u
		String [] expected=new String [] {
				CMD_LISTFILES,	// 0
				CMD_LISTM3U,	// 1
				CMD_UPDATE,		// 2
				CMD_CLEARM3U,	// 3
				"playlistadd \""+PLID1+"\" \""+DIR+FNAME1+"\"",
				"playlistadd \""+PLID1+"\" \""+DIR+FNAME2+"\"",
				"playlistadd \""+PLID1+"\" \""+DIR+FNAME3+"\"",
				CMD_CLEAR,		// 7
				CMD_LOAD		// 8
		};
		//for(int i=0;i<expected.length;i++) System.err.println(expected[i]);
		
		List<String> moddedlisting=new ArrayList<>();
		moddedlisting.add(F+DIR+FNAME1);
		moddedlisting.add(F+DIR+FNAME4);
		moddedlisting.add(F+DIR+FNAME3);

		mpdc.expectOkRequest(expected[0], FILELISTING);
		mpdc.expectOkRequest(expected[1], moddedlisting);
		mpdc.expectOkRequest(expected[2], Collections.singletonList("\tupdating_db: 3"));
		for(int i=3;i<9;i++)
			mpdc.expectOkRequest(expected[i], Collections.emptyList());
		
		BState bs1=new MockBState();
		boolean ret = pldb.loadPlaylist(bs1, PLID1);
		assertTrue(ret);
		assertEquals(PLID1,bs1.getString(PlaylistDBI.PLAYLIST_LOADED));
		
		List<String> sent = mpdc.getCommandsSent();
		assertEquals(expected.length,sent.size());
		for(int i=0;i<expected.length;i++)
		{
			assertEquals(expected[i],sent.get(i));
		}
	}

}
