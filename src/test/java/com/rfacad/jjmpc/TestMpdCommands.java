package com.rfacad.jjmpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.rfacad.buttons.ButtonState;
import com.rfacad.buttons.interfaces.BState;
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
		BState bstate=new ButtonState(S0,S0,S0);
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
		BState bstate=new ButtonState(S0,S0,S0);
		boolean ret=status.button(bstate);
		assertFalse(ret);
	}
	
	@Test
	public void testPlayPause()
	{
		CmdPlayPause pp=new CmdPlayPause(status);
		
		BState bstate=null;
		assertNull(mpd.getStat("state"));
		
		// First press: no initial state, "PLAY"
		bstate=new ButtonState(S0,S0,S0);
		status.button(bstate);
		assertNull(bstate.get("state"));
		pp.button(bstate);
		assertEquals("play",mpd.getStat("state"));
		
		// Second press: "PAUSE"
		bstate=new ButtonState(S0,S0,S0);
		status.button(bstate);
		assertEquals("play",bstate.get("state"));
		pp.button(bstate);
		assertEquals("pause",mpd.getStat("state"));
		
		// Third press: "PLAY"
		bstate=new ButtonState(S0,S0,S0);
		status.button(bstate);
		assertEquals("pause",bstate.get("state"));
		pp.button(bstate);
		assertEquals("play",mpd.getStat("state"));
	}

	@Test
	public void testVolume()
	{
		mpd.setStat("volume", "50");
		CmdVolume v=new CmdVolume(status,10);
		
		BState bstate=null;
		assertEquals("50",mpd.getStat("volume"));
		
		bstate=new ButtonState(S0,S0,S0);
		status.button(bstate);
		assertEquals("50",bstate.get("volume"));
		v.button(bstate);
		
		assertEquals("60",mpd.getStat("volume"));

		// and a bad volume state
		mpd.setStat("volume", "foo");
		assertEquals("foo",mpd.getStat("volume"));
		bstate=new ButtonState(S0,S0,S0);
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
		BState bstate=null;
		assertEquals("1",mpd.getStat("single"));
		
		// First press: set playlist-once mode
		bstate=new ButtonState(S0,S0,S0);
		status.button(bstate);
		assertEquals("1",bstate.get("single"));
		v.button(bstate);
		assertEquals("0",mpd.getStat("single"));
		assertEquals("Playlist once",bstate.get("mode"));
		
		// Second press: set track-once mode
		bstate=new ButtonState(S0,S0,S0);
		status.button(bstate);
		assertEquals("0",bstate.get("single"));
		v.button(bstate);
		assertEquals("1",mpd.getStat("single"));
		assertEquals("Track once",bstate.get("mode"));

		// and a bad state
		bstate=new ButtonState(S0,S0,S0);
		mpd.setStat("single", "foo");
		assertEquals("foo",mpd.getStat("single"));
		status.button(bstate);
		v.button(bstate);
		assertEquals("foo",mpd.getStat("single"));

		
	}
	
}
