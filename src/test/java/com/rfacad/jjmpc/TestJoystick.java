package com.rfacad.jjmpc;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class TestJoystick
{
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
			calls.add(new short[] { id,prev,value });
			latch.countDown();
		}
	}
	
	class TestJDriver extends RidiculouslySimpleJoystickDriver
	{
		byte[]content;
		public TestJDriver(List<short[]> calls)
		{
			super("foo");
			content=new byte[calls.size()*8];
			for(int i=0;i<calls.size();i++)
			{
				short[]call=calls.get(i);
				int j=i*8;
				// Sequence number
				int seq= (i|0x40404040);
				content[j++]=(byte)(i&0xff);
				content[j++]=(byte)((i>>8)&0xff);
				content[j++]=(byte)((i>>16)&0xff);
				content[j++]=(byte)((i>>24)&0xff);
				// ID
				content[j++]=(byte)(call[0]&0xff);
				content[j++]=(byte)((call[0]>>8)&0xff);
				// Value
				content[j++]=(byte)(call[1]&0xff);
				content[j++]=(byte)((call[1]>>8)&0xff);
			}
		}
		protected BufferedInputStream openStream() throws IOException
		{
			return new BufferedInputStream(new ByteArrayInputStream(content));
		}
	}
	
	@Test
	public void shouldAcceptButtonPress()
	{
		// Happy path, mostly tests to make sure the test code is OK
		List<short[]> calls=new ArrayList<>(1);
		calls.add(new short[]{(short)0x102,(short)0x304});
		final TestJDriver jd=new TestJDriver(calls);
		final CountDownLatch threadEnd=new CountDownLatch(1);
		TestJDListener jl=new TestJDListener(calls.size());
		jd.setListener(jl);
		
		new Thread(jd) { public void run() {
			jd.run();
			// That will play back all of the mock buttons at once
			// ... and keep going until told to stop!
			threadEnd.countDown();
		}}.start();
		
		try {
			jl.latch.await();
			jd.shutdown();
			threadEnd.await();
		} catch (InterruptedException e) {
			fail("interrupted");
		}
		
		// jl's calls should match what we sent
		assertEquals(1,jl.calls.size());
		assertEquals(calls.get(0)[1],jl.calls.get(0)[0]);
		assertEquals(       (short)0,jl.calls.get(0)[1]);
		assertEquals(calls.get(0)[0],jl.calls.get(0)[2]);
	}
}
