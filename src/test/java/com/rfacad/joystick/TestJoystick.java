package com.rfacad.joystick;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.rfacad.joystick.interfaces.RSJDListener;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class TestJoystick
{
	private static final Logger log = LogManager.getLogger(RidiculouslySimpleJoystickDriver.class);

	class TestJDListener implements RSJDListener
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
		private List<byte[]> contentList;
		private int current;
		CountDownLatch threadEnd=new CountDownLatch(1);
		public TestJDriver(List<List<short[]>> callsets)
		{
			super("foo");
			current=0;
			contentList=new ArrayList<>();
			for(List<short[]> calls : callsets)
			{
				byte [] content=new byte[calls.size()*8];
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
				contentList.add(content);
			}
		}
		protected void innerRun() {
			super.innerRun();
			// That will play back all of the mock buttons at once
			// ... and keep going until told to stop!
			threadEnd.countDown();
		}
		protected BufferedInputStream openStream() throws IOException
		{
			// The ByteArrayInputStream will EOF after it finishes,
			// forcing the driver to call openStream() again
			// if anyone tries to read it again.
			if ( current >= contentList.size() )
			{
				throw new IOException("End of content");
			}
			byte[] content = contentList.get(current++);
			return new BufferedInputStream(new ByteArrayInputStream(content));
		}
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
	
	private static void verifyCall(short[] press, short prev, short[] call)
	{
		assertEquals(press[0],call[0]);
		assertEquals(prev,call[1]);
		assertEquals(press[1],call[2]);
	}
	
	@Test
	public void shouldAcceptButtonPress()
	{
		log.info("TEST START shouldAcceptButtonPress()");
		// Happy path, mostly tests to make sure the test code is OK
		List<short[]> presses=new ArrayList<>(1);
		presses.add(new short[]{(short)0x102,(short)0x1234}); // 0x102 is Y axis
		TestJDriver jd=new TestJDriver(Collections.singletonList(presses));
		TestJDListener jl=new TestJDListener(presses.size());
		jd.setListener(jl);

		jd.spawn();
		stopAfterAllButtonPresses(jd,jl);
		
		// jl's calls should match what we sent
		assertEquals(presses.size(),jl.calls.size());
		
		// Each press should have a matching call.
		// calls are {id,prev value,next value}
		// presses are {id,value}
		verifyCall(presses.get(0),(short)0,jl.calls.get(0));

		// Just making sure we're not byte-swapped
		assertEquals((short)0x0102,jl.calls.get(0)[0]);
		assertEquals((short)0x1234,jl.calls.get(0)[2]);
	}

	@Test
	public void shouldRememberLastButtonPress()
	{
		log.info("TEST START shouldRememberLastButtonPress()");
		// Check the prev-value cache.
		// Note that it handles 0 and 1 differently (for buttons)
		List<short[]> presses=new ArrayList<>(1);
		presses.add(new short[]{(short)0x301,(short)0x1});   // 0 0x301 is button X
		presses.add(new short[]{(short)0x102,(short)0x1234});// 1 0x102 is Y axis
		presses.add(new short[]{(short)0x301,(short)0x0});   // 2 X
		presses.add(new short[]{(short)0x102,(short)0x0});   // 3 Y
		presses.add(new short[]{(short)0x301,(short)0x1});   // 4 X
		presses.add(new short[]{(short)0x102,(short)-54});   // 5 Y negative is up
		presses.add(new short[]{(short)0x301,(short)0x0});   // 6 X
		presses.add(new short[]{(short)0x102,(short)0x5678});// 7 Y
		TestJDriver jd=new TestJDriver(Collections.singletonList(presses));
		TestJDListener jl=new TestJDListener(presses.size());
		jd.setListener(jl);

		jd.spawn();
		stopAfterAllButtonPresses(jd,jl);
		
		// jl's calls should match what we sent
		assertEquals(presses.size(),jl.calls.size());
		
		// first call on each button, prev is 0
		verifyCall(presses.get(0),(short)0,jl.calls.get(0)); // button X
		verifyCall(presses.get(1),(short)0,jl.calls.get(1)); // Y axis
		
		// after this, it gets complicated... sort by ID!
		
		// Button X, presses 0,2,4,6
		verifyCall(presses.get(2),presses.get(0)[1],jl.calls.get(2));
		verifyCall(presses.get(4),presses.get(2)[1],jl.calls.get(4));
		verifyCall(presses.get(6),presses.get(4)[1],jl.calls.get(6));
		
		// Y Axis, presses 1,3,5,7
		verifyCall(presses.get(3),presses.get(1)[1],jl.calls.get(3));
		verifyCall(presses.get(5),presses.get(3)[1],jl.calls.get(5));
		verifyCall(presses.get(7),presses.get(5)[1],jl.calls.get(7));
	}
	
	@Test
	public void shouldReopenDevice()
	{
		log.info("TEST START shouldReopenDevice()");
		List<short[]> presses=new ArrayList<>();
		List<Short> prevs=new ArrayList<>();
		List<short[]> presses1=new ArrayList<>();
		List<short[]> presses2=new ArrayList<>();
		List<short[]> presses3=new ArrayList<>();
		short id=(short)0x102;
		short v=(short)0x200;
		int k=0;
		for(int i=0;i<5;i++)
		{
			short [] p=new short[]{id,v};
			prevs.add( new Short( (short) ((i==0)?0:(v-1)) ));
			presses.add(p);
			presses1.add(p);
			++v; ++k;
		}
		for(int i=0;i<6;i++)
		{
			short [] p=new short[]{id,v};
			prevs.add( new Short( (short) ((i==0)?0:(v-1)) ));
			presses.add(p);
			presses2.add(p);
			++v; ++k;
		}
		for(int i=0;i<7;i++)
		{
			short [] p=new short[]{id,v};
			prevs.add( new Short( (short) ((i==0)?0:(v-1)) ));
			presses.add(p);
			presses3.add(p);
			++v; ++k;
		}
		List<List<short[]>> listoflists=new ArrayList<>();
		listoflists.add(presses1);
		listoflists.add(presses2);
		listoflists.add(presses3);
		TestJDriver jd=new TestJDriver(listoflists);
		TestJDListener jl=new TestJDListener(k);
		jd.setListener(jl);

		jd.spawn();
		stopAfterAllButtonPresses(jd,jl);
		
		// jl's calls should match what we sent
		assertEquals(k,jl.calls.size());
		
		// Each press should have a matching call.
		// calls are {id,prev value,next value}
		// presses are {id,value}
		for(int i=0;i<k;i++)
		{
			verifyCall(presses.get(i),prevs.get(i),jl.calls.get(i));
		}
	}
	
	@Test
	public void shouldNotHangOnError() throws InterruptedException
	{
		log.info("TEST START shouldNotHangOnError()");
		// This test uses the real file I/O, pointed at
		// a non-existant file, to generate errors trying to read it
		log.info("TEST START shouldNotHangOnError()");
		String fn="/this/file/does/not/exist";
		final CountDownLatch end=new CountDownLatch(1);
		final RidiculouslySimpleJoystickDriver jd = new RidiculouslySimpleJoystickDriver(fn) {
			protected void innerRun() {
				super.innerRun();
				end.countDown();
			}
		};
		jd.spawn();
		
		// While it's sleeping, it should try to open that file
		// two or three times, waiting 1 second each
		Thread.sleep(3000);
		jd.shutdown();
		log.info("Hang test shutdown sent");
		end.await();
		log.info("Hang test complete");
	}

	@Test
	public void shouldNotHangOnShutdown() throws InterruptedException, IOException
	{
		log.info("TEST START shouldNotHangOnShutdown()");
		// What if shutdown is called when the driver is
		// waiting for I/O? InputStream.read() doesn't time out,
		// after all. I'll have to use a pipe-type input stream here.
		final PipedOutputStream src=new PipedOutputStream();
		final CountDownLatch end=new CountDownLatch(1);
		final RidiculouslySimpleJoystickDriver jd = new RidiculouslySimpleJoystickDriver("") {
			protected BufferedInputStream openStream() throws IOException
			{
				return new BufferedInputStream(new PipedInputStream(src));
			}
			protected void innerRun() {
				try {
					log.info("Hanger thread started");
					super.innerRun();
					log.info("Hanger thread exited cleanly");
				}
				catch (Exception e)
				{
					log.info("Hanger thread threw exception ",e);
				}
				end.countDown();
			}
		};
		// never write to src, and the input stream will block on read.
		jd.spawn();
		Thread.sleep(1000);
		
		jd.shutdown();
		log.info("Hang test shutdown sent");
		end.await();
		log.info("Hang test complete");

	}
	
}
