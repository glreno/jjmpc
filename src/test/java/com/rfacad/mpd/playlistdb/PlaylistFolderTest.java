package com.rfacad.mpd.playlistdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.rfacad.buttons.interfaces.BState;
import com.rfacad.jjmpc.MockBState;
import com.rfacad.jjmpc.MockMpdClient;
import com.rfacad.mpd.interfaces.PlaylistDBI;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class PlaylistFolderTest {
	
	public static final String FOLDER="Monty Python";
	public static final String CACHEKEY = PlaylistDBI.PLAYLISTS_IN_FOLDER_PREFIX+FOLDER;
	public static final String CMD = "listfiles \""+FOLDER+"\"";
	public static final String PLNAME1="Monty Python and the Holy Grail";
	public static final String PLNAME2="Matching Tie and Handkerchief";
	public static final String PLNAME3="Monty Python's Previous Record";
	public static final String PLID1=FOLDER+PlaylistDBI.SEP+PLNAME1;
	public static final String PLID2=FOLDER+PlaylistDBI.SEP+PLNAME2;
	public static final String PLID3=FOLDER+PlaylistDBI.SEP+PLNAME3;
	public static final String D = "\tdirectory: ";
	public static final String T = "\tLast-Modified: 2018-07-01T19:40:53Z";
	
	private MockMpdClient mpdc;
	private PlaylistDBI pldb;
	
	@Before
	public void setup()
	{
		 mpdc=new MockMpdClient();
		 pldb=new PlaylistDB(mpdc); 
	}
	
	@Test
	public void shouldNotListPlaylistsInFolderWhenNoMusic()
	{
		List<String> response=new ArrayList<>();
		mpdc.expectOkRequest(CMD, response); // no files -- empty response
		BState bs=new MockBState();
		List<String> ret1=pldb.listPlaylists(bs,FOLDER);
		assertNull(ret1);
	}
	
	@Test
	public void shouldNotListPlaylistsInFolderThereAreOnlyFiles()
	{
		List<String> response=new ArrayList<>();
		response.add("\tfile: Holy Grail 01.mp3");
		response.add("\tsize: 2482231");
		response.add(T);
		mpdc.expectOkRequest(CMD, response);
		BState bs=new MockBState();
		List<String> ret1=pldb.listPlaylists(bs,FOLDER);
		assertNull(ret1);
	}
	
	@Test
	public void shouldNotListPlaylistsInFolderThatDoesNotExist()
	{
		List<String> response=new ArrayList<>();
		mpdc.expectFailRequest(CMD,"ACK [52@0] {listfiles} No such file or directory", response); // no files -- empty response
		BState bs=new MockBState();
		List<String> ret1=pldb.listPlaylists(bs,FOLDER);
		assertNull(ret1);
	}

	@Test
	public void shouldListPlaylistsInFolderWhenNoChange()
	{
		BState bs1=new MockBState();
		
		// Note: MPD does not SORT its responses.

		List<String> response1=new ArrayList<>();
		response1.add(D+PLNAME1); response1.add(T); // M
		response1.add(D+PLNAME2); response1.add(T); // G
		mpdc.expectOkRequest(CMD, response1); // no files -- empty response

		List<String> ret1=pldb.listPlaylists(bs1,FOLDER);
		assertNotNull(ret1);
		assertEquals(2,ret1.size());
		assertEquals(PLID2,ret1.get(0));
		assertEquals(PLID1,ret1.get(1));
		
		List<String> list1=bs1.getStringList(CACHEKEY);
		assertNotNull(list1);
		assertEquals(2,list1.size());
		assertEquals(PLID2,list1.get(0));
		assertEquals(PLID1,list1.get(1));
		
		BState bs2=new MockBState();

		List<String> ret2=pldb.listPlaylists(bs2,FOLDER);
		assertNotNull(ret2);
		assertEquals(2,ret2.size());
		assertEquals(PLID2,ret2.get(0));
		assertEquals(PLID1,ret2.get(1));

		List<String> list2=bs2.getStringList(CACHEKEY);
		assertNotNull(list2);
		assertEquals(2,list2.size());
		assertEquals(PLID2,list2.get(0));
		assertEquals(PLID1,list2.get(1));


	}
	
	@Test
	public void shouldListPlaylistsInFolderAfterAddingDir()
	{
		BState bs1=new MockBState();
		
		// Note: MPD does not SORT its responses.

		List<String> response1=new ArrayList<>();
		response1.add(D+PLNAME1); response1.add(T); // M
		response1.add(D+PLNAME2); response1.add(T); // G
		mpdc.expectOkRequest(CMD, response1);

		List<String> ret1=pldb.listPlaylists(bs1,FOLDER);
		assertNotNull(ret1);
		assertEquals(2,ret1.size());
		assertEquals(PLID2,ret1.get(0));
		assertEquals(PLID1,ret1.get(1));

		List<String> list1=bs1.getStringList(CACHEKEY);
		assertNotNull(list1);
		assertEquals(2,list1.size());
		assertEquals(PLID2,list1.get(0));
		assertEquals(PLID1,list1.get(1));

		BState bs2=new MockBState();

		List<String> response2=new ArrayList<>();
		response2.add(D+PLNAME1); response1.add(T); // M
		response2.add(D+PLNAME2); response1.add(T); // G
		response2.add(D+PLNAME3); response1.add(T); // S
		mpdc.expectOkRequest(CMD, response2);

		List<String> ret2=pldb.listPlaylists(bs2,FOLDER);
		assertNotNull(ret2);
		assertEquals(3,ret2.size());
		assertEquals(PLID2,ret2.get(0));
		assertEquals(PLID1,ret2.get(1));
		assertEquals(PLID3,ret2.get(2));
		
		List<String> list2=bs2.getStringList(CACHEKEY);
		assertNotNull(list2);
		assertEquals(3,list2.size());
		assertEquals(PLID2,list2.get(0));
		assertEquals(PLID1,list2.get(1));
		assertEquals(PLID3,list2.get(2));

		// but the cache is still in place, if you use bs1
		// (this bit of design internal probably doesn't need to be unit tested)
		List<String> ret3=pldb.listPlaylists(bs1,FOLDER);
		assertNotNull(ret3);
		assertEquals(2,ret3.size());
		assertEquals(PLID2,ret3.get(0));
		assertEquals(PLID1,ret3.get(1));
		
		List<String> list3=bs1.getStringList(CACHEKEY);
		assertNotNull(list3);
		assertEquals(2,list3.size());
		assertEquals(PLID2,list3.get(0));
		assertEquals(PLID1,list3.get(1));


	}
	
	@Test
	public void shouldListPlaylistsInFolderAfterRemovingDir()
	{
		BState bs1=new MockBState();
		
		// Note: MPD does not SORT its responses.

		List<String> response1=new ArrayList<>();
		response1.add(D+PLNAME1); response1.add(T); // M
		response1.add(D+PLNAME2); response1.add(T); // G
		mpdc.expectOkRequest(CMD, response1);

		List<String> ret1=pldb.listPlaylists(bs1,FOLDER);
		assertNotNull(ret1);
		assertEquals(2,ret1.size());
		assertEquals(PLID2,ret1.get(0));
		assertEquals(PLID1,ret1.get(1));


		List<String> list1=bs1.getStringList(CACHEKEY);
		assertNotNull(list1);
		assertEquals(2,list1.size());
		assertEquals(PLID2,list1.get(0));
		assertEquals(PLID1,list1.get(1));

		BState bs2=new MockBState();

		List<String> response2=new ArrayList<>();
		response2.add(D+PLNAME2); response1.add(T); // G
		mpdc.expectOkRequest(CMD, response2);

		List<String> ret2=pldb.listPlaylists(bs2,FOLDER);
		assertNotNull(ret2);
		assertEquals(1,ret2.size());
		assertEquals(PLID2,ret2.get(0));
	
		List<String> list2=bs2.getStringList(CACHEKEY);
		assertNotNull(list2);
		assertEquals(1,list2.size());
		assertEquals(PLID2,list2.get(0));
		
		// but the cache is still in place, if you use bs1
		// (this bit of design internal probably doesn't need to be unit tested)
		List<String> ret3=pldb.listPlaylists(bs1,FOLDER);
		assertNotNull(ret3);
		assertEquals(2,ret3.size());
		assertEquals(PLID2,ret3.get(0));
		assertEquals(PLID1,ret3.get(1));

		List<String> list3=bs1.getStringList(CACHEKEY);
		assertNotNull(list3);
		assertEquals(2,list3.size());
		assertEquals(PLID2,list3.get(0));
		assertEquals(PLID1,list3.get(1));


	}
	
}
