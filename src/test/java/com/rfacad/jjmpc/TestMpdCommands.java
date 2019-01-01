package com.rfacad.jjmpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.rfacad.buttons.BState;
import com.rfacad.mpd.interfaces.RSMPDListener;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class TestMpdCommands {
	
	private static final short S0=(short)0;
	
	private MockMpdClient mpd;
	private CmdMpdStatus status;
	
	@Before
	public void setup()
	{
		mpd=new MockMpdClient();
		status=new CmdMpdStatus(mpd);
	}

	@Test
	public void shouldFailOnCannotConnect()
	{
		mpd=new MockMpdClient() {
			@Override
			public void sendCommand(String command, RSMPDListener listener) throws IOException {
				throw new IOException("Cannot contact");
			}
		};
		status=new CmdMpdStatus(mpd);
		BState bstate=new BState(S0,S0,S0);
		boolean ret=status.button(bstate);
		assertFalse(ret);
	}
	
	@Test
	public void shouldFailOnBadStatus()
	{
		mpd=new MockMpdClient() {
			@Override
			public void sendCommand(String command, RSMPDListener listener) throws IOException {
				List<String> resp=new ArrayList<>();
				resp.add("foo:bar");
				listener.not_ok("ACK", resp);
			}
		};
		status=new CmdMpdStatus(mpd);
		BState bstate=new BState(S0,S0,S0);
		boolean ret=status.button(bstate);
		assertFalse(ret);
	}
	
	@Test
	public void testPlayPause()
	{
		CmdPlayPause pp=new CmdPlayPause(status);
		
		BState bstate=new BState(S0,S0,S0);
		assertNull(status.get("state"));
		assertNull(mpd.getStat("state"));
		
		// First press: no initial state, "PLAY"
		status.button(bstate);
		assertNull(status.get("state"));
		pp.button(bstate);
		assertEquals("play",mpd.getStat("state"));
		
		// Second press: "PAUSE"
		status.button(bstate);
		assertEquals("play",status.get("state"));
		pp.button(bstate);
		assertEquals("pause",mpd.getStat("state"));
		
		// Third press: "PLAY"
		status.button(bstate);
		assertEquals("pause",status.get("state"));
		pp.button(bstate);
		assertEquals("play",mpd.getStat("state"));
	}

	@Test
	public void testVolume()
	{
		mpd.setStat("volume", "50");
		CmdVolume v=new CmdVolume(status,10);
		
		BState bstate=new BState(S0,S0,S0);
		assertNull(status.get("volume"));
		assertEquals("50",mpd.getStat("volume"));
		
		status.button(bstate);
		assertEquals("50",status.get("volume"));
		v.button(bstate);
		
		assertEquals("60",mpd.getStat("volume"));

		// and a bad volume state
		mpd.setStat("volume", "foo");
		assertEquals("foo",mpd.getStat("volume"));
		status.button(bstate);
		v.button(bstate);
		assertEquals("foo",mpd.getStat("volume"));

	}

	@Test
	public void testMode()
	{
		mpd.setStat("single", "1");
		CmdPlayMode v=new CmdPlayMode(status);
		
		// Initial state - Track Once
		BState bstate=new BState(S0,S0,S0);
		assertNull(status.get("single"));
		assertEquals("1",mpd.getStat("single"));
		
		// First press: set playlist-once mode
		status.button(bstate);
		assertEquals("1",status.get("single"));
		v.button(bstate);
		assertEquals("0",mpd.getStat("single"));
		assertEquals("Playlist once",bstate.get("mode"));
		
		// Second press: set track-once mode
		status.button(bstate);
		assertEquals("0",status.get("single"));
		v.button(bstate);
		assertEquals("1",mpd.getStat("single"));
		assertEquals("Track once",bstate.get("mode"));

		// and a bad state
		mpd.setStat("single", "foo");
		assertEquals("foo",mpd.getStat("single"));
		status.button(bstate);
		v.button(bstate);
		assertEquals("foo",mpd.getStat("single"));

		
	}
	
}
