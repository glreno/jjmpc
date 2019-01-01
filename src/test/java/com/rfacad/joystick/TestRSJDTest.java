package com.rfacad.joystick;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileOutputStream;

import org.junit.Test;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class TestRSJDTest {
	
	@Test
	public void shouldShutDownTestProgram() throws IOException
	{
		String fn="/tmp/stop_joystick";
		File f=new File(fn);
		// Create a file containing button 0x109 being pressed.
		FileOutputStream out=new FileOutputStream(f);
		List<short[]> calls = new ArrayList<>();
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
		
		RSJDTest.main(new String[] {fn});
		
		f.delete();
	}
}
