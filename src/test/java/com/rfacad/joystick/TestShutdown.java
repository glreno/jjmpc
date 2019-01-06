package com.rfacad.joystick;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

@com.rfacad.Copyright("Copyright (c) 2019 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class TestShutdown {
	
	private static final Logger log = LogManager.getLogger(RidiculouslySimpleJoystickDriver.class);
	
	@Test
	public void shouldShutDownTestProgram() throws IOException
	{
		String fn="/tmp/stop_joystick_pipe";
		String cmd="/usr/bin/mkfifo";
		if ( ! new File(cmd).canExecute() )
		{
			log.info("Cannot find {}",cmd);
			log.info("This test can only run on Unix. Exiting.");
			return;
		}
		try{Runtime.getRuntime().exec(new String[] {cmd,fn}).waitFor(1, TimeUnit.SECONDS);}catch(InterruptedException e) {}
		log.info("Created named pipe {}",fn);
		
		// Create a button press
		List<short[]> calls = new ArrayList<>();
		calls.add( new short[] {(short)0x901,(short)0} );
//		calls.add( new short[] {(short)0x901,(short)1} );
		byte [][] presses=new byte[calls.size()][];
		for(int i=0;i<calls.size();i++)
		{
			short[]call=calls.get(i);
			byte [] content=new byte[8];
			presses[i]=content;
			int j=0;
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

		// Create a joystick driver that can report that it is done running
		CountDownLatch latch=new CountDownLatch(1);
		RidiculouslySimpleJoystickDriver driver=new RidiculouslySimpleJoystickDriver(fn) {
			@Override
			protected void innerRun()
			{
				super.innerRun();
				latch.countDown();
			}
		};
		
		// Attach it to a test program that will shut down the
		// driver when a button is pressed.
		new RSJDTest(driver);
		driver.spawn();
		log.info("Started RSJDTest.");

//		try { Thread.sleep(1000);}catch(InterruptedException e) {}
		log.info("Opening the file.");
		File f=new File(fn);
		FileOutputStream out=new FileOutputStream(f,true);
		log.info("Opened the file.");

//		try { Thread.sleep(1000);}catch(InterruptedException e) {}
		log.info("Writing the button press.");
		out.write(presses[0]);
		log.info("Wrote the button press. Waiting for driver to exit.");
		
		// That button press SHOULD cause the driver to exit.
		try {
			latch.await(5, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
		}
		long count1 = latch.getCount();
		if ( count1 > 0 )
		{
			log.error("Joystick driver thread did not shut down on command.");
		}
		// Not going to assert that until we have shut down everything else!!

//		try { Thread.sleep(1000);}catch(InterruptedException e) {}
		log.info("Closing the file.");
		out.close();
		log.info("Closed the file.");
		if ( count1 > 0 )
		{
			try {
				latch.await(5, TimeUnit.SECONDS);
			} catch (InterruptedException e1) {
			}
		}
		long count2 = latch.getCount();
		
//		try { Thread.sleep(1000);}catch(InterruptedException e) {}
		log.info("Deleting the file.");
		f.delete();
		log.info("Deleted the file.");
		
		assertEquals("Thread did NOT shut down, even after closing pipe.",0L,count2);
		assertEquals("Driver did not shut down on command.",0L,count1);
	}
}
