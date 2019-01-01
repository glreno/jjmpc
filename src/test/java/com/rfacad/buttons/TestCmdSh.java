package com.rfacad.buttons;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class TestCmdSh
{
	@Test
	public void shouldFailNullCommand() {
		CmdSh cmd1 = new CmdSh(true,(String[])null);
		boolean ret;
		ret=cmd1.button(null);
		assertFalse(ret);

		CmdSh cmd2 = new CmdSh(false,(String[])null);
		ret=cmd2.button(null);
		assertFalse(ret);

		CmdSh cmd3 = new CmdSh(false);
		ret=cmd3.button(null);
		assertFalse(ret);
	}
	
	@Test
	public void shouldSkipWhenInTestMode() {
		CmdSh cmd1 = new CmdSh(true,"/bin/false");
		cmd1.setTestMode(true);
		boolean ret;
		ret=cmd1.button(null);
		assertTrue(ret);
	}
	
	@Test
	public void shouldSubstituteVars() {
		short s0=(short)0;
		BState state=new BState(s0, s0, s0);
		state.set("foo", "bar");

		CmdSh cmd1 = new CmdSh(true,"/bin/false","%foo%");
		cmd1.setTestMode(true);

		boolean ret;
		ret=cmd1.button(state);
		assertTrue(ret);
		
		String[] submitted=cmd1.getLastRunJob();
		assertEquals(2,submitted.length);
		assertEquals("/bin/false",submitted[0]);
		assertEquals("bar",submitted[1]);
	}
	
	/** Test firing off a command, and not waiting for it.
	 * To see if the command actually DOES anything, we'll
	 * actually be waiting for it, and checking for a
	 * file to be created.
	 */
	@Test
	public void shouldRunAsyncJob()
	{
		String fn="cmdshtest."+System.currentTimeMillis()+".txt";
		File f=new File(fn);
		f.delete();
		
		CmdSh cmd1 = new CmdSh(false,"/usr/bin/touch",fn);
		boolean ret;
		ret=cmd1.button(null);
		
		// The command has been started. Wait for the file to be created
		// (or ten seconds)
		int tries=100;
		while ( tries>0 && ! f.exists() )
		{
			try { Thread.sleep(100);} catch (InterruptedException e){}
			tries--;
		}
		assertTrue("Test file was not created",f.exists());
		f.delete();
		
		assertTrue(ret);
		
	}

	/** Test firing off a command, and waiting for it.
	 * The command is "sleep 5", which takes 5 seconds to run.
	 */
	@Test
	public void shouldRunSyncJob()
	{
		CmdSh cmd1 = new CmdSh(true,"/bin/sleep","5");
		boolean ret;

		long before=System.currentTimeMillis();
		ret=cmd1.button(null);
		long after=System.currentTimeMillis();
		long duration=after-before;
		assertTrue(duration>=5000);

		assertTrue(ret);
	}

	/** Test firing off a command that generates output, and waiting for it.
	 * We don't read the output, just want to make sure it doesn't hang. 
	 */
	@Test
	public void shouldRunSyncJobWithOutput()
	{
		CmdSh cmd1 = new CmdSh(true,"/bin/echo","foo");
		boolean ret;
		ret=cmd1.button(null);
		assertTrue(ret);
	}

	/** Test firing off a command that generates output, and waiting for it.
	 * We don't read the output, just want to make sure it doesn't hang. 
	 */
	@Test
	public void shouldRunASyncJobWithOutput()
	{
		CmdSh cmd1 = new CmdSh(false,"/bin/echo","bar");
		boolean ret;
		ret=cmd1.button(null);
		assertTrue(ret);
	}

}
