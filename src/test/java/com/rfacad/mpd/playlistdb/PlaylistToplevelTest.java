package com.rfacad.mpd.playlistdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.rfacad.buttons.interfaces.BState;
import com.rfacad.jjmpc.MockMpdClient;
import com.rfacad.jjmpc.MockBState;
import com.rfacad.mpd.interfaces.PlaylistDBI;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class PlaylistToplevelTest {
	
	private static final String CACHEKEY = PlaylistDBI.FOLDERS;
	private static final String CMD = "listfiles";
	public static final String FOLDER1="Monty Python";
	public static final String FOLDER2="George Carlin";
	public static final String FOLDER3="Steven Wright";
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
	public void shouldNotListFoldersWhenNoMusic()
	{
		List<String> response=new ArrayList<>();
		mpdc.expectOkRequest(CMD, response); // no files -- empty response
		BState bs=new MockBState();
		List<String> ret1=pldb.listPlaylistFolders(bs);
		assertNull(ret1);
	}

	@Test
	public void shouldNotListFoldersThereAreOnlyFiles()
	{
		List<String> response=new ArrayList<>();
		response.add("\tfile: Holy Grail 01.mp3");
		response.add("\tsize: 2482231");
		response.add(T);
		mpdc.expectOkRequest(CMD, response); // no files -- empty response
		BState bs=new MockBState();
		List<String> ret1=pldb.listPlaylistFolders(bs);
		assertNull(ret1);
	}
	

	@Test
	public void shouldListFoldersWhenNoChange()
	{
		BState bs1=new MockBState();
		
		// Note: MPD does not SORT its responses.

		List<String> response1=new ArrayList<>();
		response1.add(D+FOLDER1); response1.add(T); // M
		response1.add(D+FOLDER2); response1.add(T); // G
		mpdc.expectOkRequest(CMD, response1); // no files -- empty response

		List<String> ret1=pldb.listPlaylistFolders(bs1);
		assertNotNull(ret1);
		assertEquals(2,ret1.size());
		assertEquals(FOLDER2,ret1.get(0));
		assertEquals(FOLDER1,ret1.get(1));
		
		List<String> list1=(List<String>)bs1.get(CACHEKEY);
		assertNotNull(list1);
		assertEquals(2,list1.size());
		assertEquals(FOLDER2,list1.get(0));
		assertEquals(FOLDER1,list1.get(1));
		
		BState bs2=new MockBState();

		List<String> ret2=pldb.listPlaylistFolders(bs2);
		assertNotNull(ret2);
		assertEquals(2,ret2.size());
		assertEquals(FOLDER2,ret2.get(0));
		assertEquals(FOLDER1,ret2.get(1));

		List<String> list2=(List<String>)bs2.get(CACHEKEY);
		assertNotNull(list2);
		assertEquals(2,list2.size());
		assertEquals(FOLDER2,list2.get(0));
		assertEquals(FOLDER1,list2.get(1));

	}
	
	@Test
	public void shouldListFoldersAfterAddingDir()
	{
		BState bs1=new MockBState();
		
		// Note: MPD does not SORT its responses.

		List<String> response1=new ArrayList<>();
		response1.add(D+FOLDER1); response1.add(T); // M
		response1.add(D+FOLDER2); response1.add(T); // G
		mpdc.expectOkRequest(CMD, response1);

		List<String> ret1=pldb.listPlaylistFolders(bs1);
		assertNotNull(ret1);
		assertEquals(2,ret1.size());
		assertEquals(FOLDER2,ret1.get(0));
		assertEquals(FOLDER1,ret1.get(1));

		List<String> list1=(List<String>)bs1.get(CACHEKEY);
		assertNotNull(list1);
		assertEquals(2,list1.size());
		assertEquals(FOLDER2,list1.get(0));
		assertEquals(FOLDER1,list1.get(1));

		BState bs2=new MockBState();

		List<String> response2=new ArrayList<>();
		response2.add(D+FOLDER1); response1.add(T); // M
		response2.add(D+FOLDER2); response1.add(T); // G
		response2.add(D+FOLDER3); response1.add(T); // S
		mpdc.expectOkRequest(CMD, response2);

		List<String> ret2=pldb.listPlaylistFolders(bs2);
		assertNotNull(ret2);
		assertEquals(3,ret2.size());
		assertEquals(FOLDER2,ret2.get(0));
		assertEquals(FOLDER1,ret2.get(1));
		assertEquals(FOLDER3,ret2.get(2));
		
		List<String> list2=(List<String>)bs2.get(CACHEKEY);
		assertNotNull(list2);
		assertEquals(3,list2.size());
		assertEquals(FOLDER2,list2.get(0));
		assertEquals(FOLDER1,list2.get(1));
		assertEquals(FOLDER3,list2.get(2));

		// but the cache is still in place, if you use bs1
		// (this bit of design internal probably doesn't need to be unit tested)
		List<String> ret3=pldb.listPlaylistFolders(bs1);
		assertNotNull(ret3);
		assertEquals(2,ret3.size());
		assertEquals(FOLDER2,ret3.get(0));
		assertEquals(FOLDER1,ret3.get(1));
		
		List<String> list3=(List<String>)bs1.get(CACHEKEY);
		assertNotNull(list3);
		assertEquals(2,list3.size());
		assertEquals(FOLDER2,list3.get(0));
		assertEquals(FOLDER1,list3.get(1));

	}
	
	@Test
	public void shouldListFoldersAfterRemovingDir()
	{
		BState bs1=new MockBState();
		
		// Note: MPD does not SORT its responses.

		List<String> response1=new ArrayList<>();
		response1.add(D+FOLDER1); response1.add(T); // M
		response1.add(D+FOLDER2); response1.add(T); // G
		mpdc.expectOkRequest(CMD, response1);

		List<String> ret1=pldb.listPlaylistFolders(bs1);
		assertNotNull(ret1);
		assertEquals(2,ret1.size());
		assertEquals(FOLDER2,ret1.get(0));
		assertEquals(FOLDER1,ret1.get(1));


		List<String> list1=(List<String>)bs1.get(CACHEKEY);
		assertNotNull(list1);
		assertEquals(2,list1.size());
		assertEquals(FOLDER2,list1.get(0));
		assertEquals(FOLDER1,list1.get(1));

		BState bs2=new MockBState();

		List<String> response2=new ArrayList<>();
		response2.add(D+FOLDER2); response1.add(T); // G
		mpdc.expectOkRequest(CMD, response2);

		List<String> ret2=pldb.listPlaylistFolders(bs2);
		assertNotNull(ret2);
		assertEquals(1,ret2.size());
		assertEquals(FOLDER2,ret2.get(0));
	
		List<String> list2=(List<String>)bs2.get(CACHEKEY);
		assertNotNull(list2);
		assertEquals(1,list2.size());
		assertEquals(FOLDER2,list2.get(0));
		
		// but the cache is still in place, if you use bs1
		// (this bit of design internal probably doesn't need to be unit tested)
		List<String> ret3=pldb.listPlaylistFolders(bs1);
		assertNotNull(ret3);
		assertEquals(2,ret3.size());
		assertEquals(FOLDER2,ret3.get(0));
		assertEquals(FOLDER1,ret3.get(1));

		List<String> list3=(List<String>)bs1.get(CACHEKEY);
		assertNotNull(list3);
		assertEquals(2,list3.size());
		assertEquals(FOLDER2,list3.get(0));
		assertEquals(FOLDER1,list3.get(1));

	}

}
