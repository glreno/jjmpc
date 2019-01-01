package com.rfacad.audioCommands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.Test;

import com.rfacad.buttons.CmdSh;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class TestJbmTest {
	
	@Test
	public void shouldRunSecretCommand() throws IOException
	{
		String fn="/tmp/stop_joystick";
		File f=new File(fn);
		// Create a file containing button 0x109 being pressed.
		FileOutputStream out=new FileOutputStream(f);
		List<short[]> calls = new ArrayList<>();
		calls.add( new short[] {(short)0x105,(short)0} );
		calls.add( new short[] {(short)0x200,(short)-1} ); // shift to the left
		calls.add( new short[] {(short)0x200,(short)0} );
		calls.add( new short[] {(short)0x105,(short)1} );
		calls.add( new short[] {(short)0x200,(short)-1} ); // shift to the left (but shift isn't pressed, so oops.
		calls.add( new short[] {(short)0x200,(short)0} );
		calls.add( new short[] {(short)0x105,(short)0} );
		calls.add( new short[] {(short)0x200,(short)-1} ); // shift to the left
		calls.add( new short[] {(short)0x200,(short)0} );
		calls.add( new short[] {(short)0x200,(short)+1} ); // shift to the right
		calls.add( new short[] {(short)0x200,(short)0} );
		calls.add( new short[] {(short)0x201,(short)-1} ); // pop up
		calls.add( new short[] {(short)0x201,(short)0} );
		calls.add( new short[] {(short)0x201,(short)+1} ); // push down
		calls.add( new short[] {(short)0x201,(short)0} );
		calls.add( new short[] {(short)0x105,(short)1} );
		calls.add( new short[] {(short)0x109,(short)0} );
		calls.add( new short[] {(short)0x109,(short)1} );
		calls.add( new short[] {(short)0x109,(short)0} );
		calls.add( new short[] {(short)0x109,(short)1} );
		byte [] content=new byte[calls.size()*8];
		for(int i=0;i<calls.size();i++)
		{
			short[]call=calls.get(i);
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
		out.write(content);
		out.close();
		
		// Set all the commands in JBMTest into test mode.
		List<String[]> record=new ArrayList<>();
		CmdSh.globalTestLog=record;
		
		JBMTest.main(new String[] {fn});
		
		try { Thread.sleep(5000);} catch (InterruptedException e) {}
		
		f.delete();
		CmdSh.globalTestLog=null; // just in case there are other tests!
		
		// We should verify that all of the commands expected were run,
		// but need to hack some kind of recording into CmdSay/CmdSh for it
		assertEquals(9,record.size());
		for(int i=0;i<9;i++)
			assertEquals("/usr/bin/espeak",record.get(i)[0]);
		assertEquals("Greetings",record.get(0)[3]);
		assertEquals("shift to the left",record.get(1)[3]);
		assertEquals("shift to the left",record.get(2)[3]);
		assertEquals("oops",record.get(3)[3]);
		assertEquals("shift to the left",record.get(4)[3]);
		assertEquals("shift to the right",record.get(5)[3]);
		assertEquals("pop up",record.get(6)[3]);
		assertEquals("push down",record.get(7)[3]);
		assertEquals("Byte! Byte! Byte!",record.get(8)[3]);
	}
}
