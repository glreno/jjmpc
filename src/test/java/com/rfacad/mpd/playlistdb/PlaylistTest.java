package com.rfacad.mpd.playlistdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.rfacad.buttons.interfaces.BState;
import com.rfacad.jjmpc.MockBState;
import com.rfacad.jjmpc.MockMpdClient;
import com.rfacad.mpd.interfaces.PlaylistDBI;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class PlaylistTest {
	
	public static final String FOLDER="Monty Python";
	public static final String PLNAME1="Monty Python and the Holy Grail";
	public static final String PLID1=FOLDER+PlaylistDBI.SEP+PLNAME1;
	public static final String CACHEKEY1 = PlaylistDBI.FILES_IN_PLAYLIST_PREFIX+PLID1;
	public static final String CMD = "listfiles \""+FOLDER+"/"+PLNAME1+"\"";
	public static final String FNAME1="Holy Grail 01.mp3";
	public static final String FNAME2="Holy Grail 02.mp3";
	public static final String FNAME3="Holy Grail 03.mp3";
	public static final String F = "\tfile: ";
	public static final String S = "\tsize: 2267191";
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
	public void shouldNotListFilesInPlaylistWhenNoMusic()
	{
		List<String> response=new ArrayList<>();
		mpdc.expectOkRequest(CMD, response); // no files -- empty response
		BState bs=new MockBState();
		List<String> ret1=pldb.listFiles(bs,PLID1);
		assertNull(ret1);
	}
	
	@Test
	public void shouldNotListFilesInPlaylistWhenPlaylistIdHasNoDash()
	{
		List<String> response=new ArrayList<>();
		BState bs=new MockBState();
		List<String> ret1=pldb.listFiles(bs,"not a valid playlist name");
		assertNull(ret1);
	}

	@Test
	public void shouldNotListFilesInPlaylistWhenPlaylistIdHasNoName()
	{
		List<String> response=new ArrayList<>();
		BState bs=new MockBState();
		List<String> ret1=pldb.listFiles(bs,"not a valid playlist name--");
		assertNull(ret1);
	}
	
	@Test
	public void shouldNotListFilesInPlaylistWhenPlaylistidHasNoFolder()
	{
		List<String> response=new ArrayList<>();
		BState bs=new MockBState();
		List<String> ret1=pldb.listFiles(bs,"--not a valid playlist name");
		assertNull(ret1);
	}

	@Test
	public void shouldNotListFilesInPlaylistWhenPlaylistDoesNotExist()
	{
		List<String> response=new ArrayList<>();
		mpdc.expectFailRequest(CMD,"ACK [52@0] {listfiles} No such file or directory", response); // no files -- empty response
		BState bs=new MockBState();
		List<String> ret1=pldb.listFiles(bs,PLID1);
		assertNull(ret1);
	}
	
	@Test
	public void shouldListNoFilesInPlaylistWhenDirIsEmpty()
	{
		List<String> response=new ArrayList<>();
		mpdc.expectOkRequest(CMD, response); // no files -- empty response
		BState bs=new MockBState();
		List<String> ret1=pldb.listFiles(bs,PLID1);
		assertNull(ret1);
	}
	
	@Test
	public void shouldListFilesInPlaylistWhenNoChange()
	{
		BState bs1=new MockBState();
		
		// Note: MPD does not SORT its responses.

		List<String> response1=new ArrayList<>();
		response1.add(F+FNAME2); response1.add(S); response1.add(T);
		response1.add(F+FNAME1); response1.add(S); response1.add(T);
		mpdc.expectOkRequest(CMD, response1); // no files -- empty response

		List<String> ret1=pldb.listFiles(bs1,PLID1);
		assertNotNull(ret1);
		assertEquals(2,ret1.size());
		assertEquals(FNAME1,ret1.get(0));
		assertEquals(FNAME2,ret1.get(1));
		
		List<String> list1=(List<String>)bs1.get(CACHEKEY1);
		assertNotNull(list1);
		assertEquals(2,list1.size());
		assertEquals(FNAME1,list1.get(0));
		assertEquals(FNAME2,list1.get(1));
		
		BState bs2=new MockBState();

		List<String> ret2=pldb.listFiles(bs2,PLID1);
		assertNotNull(ret2);
		assertEquals(2,ret2.size());
		assertEquals(FNAME1,ret2.get(0));
		assertEquals(FNAME2,ret2.get(1));

		List<String> list2=(List<String>)bs2.get(CACHEKEY1);
		assertNotNull(list2);
		assertEquals(2,list2.size());
		assertEquals(FNAME1,list2.get(0));
		assertEquals(FNAME2,list2.get(1));

	}
	
	@Test
	public void shouldListFilesInPlaylistAfterAddingFile()
	{
		BState bs1=new MockBState();
		
		// Note: MPD does not SORT its responses.

		List<String> response1=new ArrayList<>();
		response1.add(F+FNAME2); response1.add(S); response1.add(T);
		response1.add(F+FNAME1); response1.add(S); response1.add(T);
		mpdc.expectOkRequest(CMD, response1);

		List<String> ret1=pldb.listFiles(bs1,PLID1);
		assertNotNull(ret1);
		assertEquals(2,ret1.size());
		assertEquals(FNAME1,ret1.get(0));
		assertEquals(FNAME2,ret1.get(1));
		
		List<String> list1=(List<String>)bs1.get(CACHEKEY1);
		assertNotNull(list1);
		assertEquals(2,list1.size());
		assertEquals(FNAME1,list1.get(0));
		assertEquals(FNAME2,list1.get(1));
		
		BState bs2=new MockBState();
		List<String> response2=new ArrayList<>();
		response2.add(F+FNAME2); response2.add(S); response2.add(T);
		response2.add(F+FNAME3); response2.add(S); response2.add(T);
		response2.add(F+FNAME1); response2.add(S); response2.add(T);
		mpdc.expectOkRequest(CMD, response2);

		List<String> ret2=pldb.listFiles(bs2,PLID1);
		assertNotNull(ret2);
		assertEquals(3,ret2.size());
		assertEquals(FNAME1,ret2.get(0));
		assertEquals(FNAME2,ret2.get(1));
		assertEquals(FNAME3,ret2.get(2));

		List<String> list2=(List<String>)bs2.get(CACHEKEY1);
		assertNotNull(list2);
		assertEquals(3,list2.size());
		assertEquals(FNAME1,list2.get(0));
		assertEquals(FNAME2,list2.get(1));
		assertEquals(FNAME3,list2.get(2));


		// but the cache is still in place, if you use bs1
		// (this bit of design internal probably doesn't need to be unit tested)
		List<String> list3=(List<String>)bs1.get(CACHEKEY1);
		assertNotNull(list3);
		assertEquals(2,list3.size());
		assertEquals(FNAME1,list3.get(0));
		assertEquals(FNAME2,list3.get(1));
	
	}
	
	@Test
	public void shouldListFilesInPlaylistAfterRemovingFile()
	{
		BState bs1=new MockBState();
		
		// Note: MPD does not SORT its responses.

		List<String> response1=new ArrayList<>();
		response1.add(F+FNAME2); response1.add(S); response1.add(T);
		response1.add(F+FNAME3); response1.add(S); response1.add(T);
		response1.add(F+FNAME1); response1.add(S); response1.add(T);
		mpdc.expectOkRequest(CMD, response1);

		List<String> ret1=pldb.listFiles(bs1,PLID1);
		assertNotNull(ret1);
		assertEquals(3,ret1.size());
		assertEquals(FNAME1,ret1.get(0));
		assertEquals(FNAME2,ret1.get(1));
		assertEquals(FNAME3,ret1.get(2));

		List<String> list1=(List<String>)bs1.get(CACHEKEY1);
		assertNotNull(list1);
		assertEquals(3,list1.size());
		assertEquals(FNAME1,list1.get(0));
		assertEquals(FNAME2,list1.get(1));
		assertEquals(FNAME3,list1.get(2));

		BState bs2=new MockBState();
		List<String> response2=new ArrayList<>();
		response2.add(F+FNAME3); response2.add(S); response2.add(T);
		response2.add(F+FNAME1); response2.add(S); response2.add(T);
		mpdc.expectOkRequest(CMD, response2);

		List<String> ret2=pldb.listFiles(bs2,PLID1);
		assertNotNull(ret2);
		assertEquals(2,ret2.size());
		assertEquals(FNAME1,ret2.get(0));
		assertEquals(FNAME3,ret2.get(1));

		List<String> list2=(List<String>)bs2.get(CACHEKEY1);
		assertNotNull(list2);
		assertEquals(2,list2.size());
		assertEquals(FNAME1,list2.get(0));
		assertEquals(FNAME3,list2.get(1));


		// but the cache is still in place, if you use bs1
		// (this bit of design internal probably doesn't need to be unit tested)
		List<String> list3=(List<String>)bs1.get(CACHEKEY1);
		assertNotNull(list3);
		assertEquals(3,list3.size());
		assertEquals(FNAME1,list3.get(0));
		assertEquals(FNAME2,list3.get(1));
		assertEquals(FNAME3,list3.get(2));

	}

}
