package com.rfacad.jjmpc;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class TestJoystick
{
	private static final Logger log = LogManager.getLogger(RidiculouslySimpleJoystickDriver.class);

	class TestJDListener implements RidiculouslySimpleJoystickDriver.RSJDListener
	{
		public List<short[]> calls=new ArrayList<>();
		public CountDownLatch latch;
		public TestJDListener(int callsToExpect)
		{
			latch=new CountDownLatch(callsToExpect);
		}
		public void button(short id,short prev,short value)
		{
			log.info("Call: "+RidiculouslySimpleJoystickDriver.hex(id)+" "+RidiculouslySimpleJoystickDriver.hex(prev)+" "+RidiculouslySimpleJoystickDriver.hex(value));
			calls.add(new short[] { id,prev,value });
			latch.countDown();
		}
	}
	
	class TestJDriver extends RidiculouslySimpleJoystickDriver
	{
		byte[]content;
		CountDownLatch threadEnd=new CountDownLatch(1);
		public TestJDriver(List<short[]> calls)
		{
			super("foo");
			content=new byte[calls.size()*8];
			for(int i=0;i<calls.size();i++)
			{
				short[]call=calls.get(i);
				log.info("Press: "+hex(call[0])+"  "+hex(call[1]));
				int j=i*8;
				// Sequence number
				int seq= (i|0x40404040);
				content[j++]=(byte)(seq&0xff);
				content[j++]=(byte)((seq>>8)&0xff);
				content[j++]=(byte)((seq>>16)&0xff);
				content[j++]=(byte)((seq>>24)&0xff);
				// Value
				content[j++]=(byte)(call[1]&0xff);
				content[j++]=(byte)((call[1]>>8)&0xff);
				// ID
				content[j++]=(byte)(call[0]&0xff);
				content[j++]=(byte)((call[0]>>8)&0xff);
			}
		}
		protected BufferedInputStream openStream() throws IOException
		{
			return new BufferedInputStream(new ByteArrayInputStream(content));
		}
	}
	
	private void startThread(final TestJDriver jd)
	{
		new Thread(jd) { public void run() {
			jd.run();
			// That will play back all of the mock buttons at once
			// ... and keep going until told to stop!
			jd.threadEnd.countDown();
		}}.start();
	}
	
	private void stopAfterAllButtonPresses(TestJDriver jd,TestJDListener jl)
	{
		try {
			jl.latch.await();
			jd.shutdown();
			jd.threadEnd.await();
		} catch (InterruptedException e) {
			fail("interrupted");
		}
	}
	
	@Test
	public void shouldAcceptButtonPress()
	{
		// Happy path, mostly tests to make sure the test code is OK
		List<short[]> presses=new ArrayList<>(1);
		presses.add(new short[]{(short)0x102,(short)0x1234}); // 0x102 is Y axis
		TestJDriver jd=new TestJDriver(presses);
		TestJDListener jl=new TestJDListener(presses.size());
		jd.setListener(jl);

		startThread(jd);
		stopAfterAllButtonPresses(jd,jl);
		
		// jl's calls should match what we sent
		assertEquals(presses.size(),jl.calls.size());
		
		// Each press should have a matching call.
		// calls are {id,prev value,next value}
		// presses are {id,value}
		assertEquals(presses.get(0)[0],jl.calls.get(0)[0]);
		assertEquals(       (short)0,jl.calls.get(0)[1]);
		assertEquals(presses.get(0)[1],jl.calls.get(0)[2]);

		// Just making sure we're not byte-swapped
		assertEquals((short)0x0102,jl.calls.get(0)[0]);
		assertEquals((short)0x1234,jl.calls.get(0)[2]);

	}
}
