package com.rfacad.jjmpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileOutputStream;

import org.junit.Test;

import com.rfacad.buttons.CmdSh;
import com.rfacad.jjmpc.JoystickMediaPlayerClient;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class TestJjmpcStartup {
	
	@Test
	public void shouldShutDownTestProgram() throws IOException
	{
		List<String[]> testlog=new ArrayList<>();
		CmdSh.globalTestLog=testlog;
		String fn="/tmp/stop_joystick";
		File f=new File(fn);
		// The shutdown command is L and R shifts, select and start.
		FileOutputStream out=new FileOutputStream(f);
		List<short[]> calls = new ArrayList<>();
		calls.add( new short[] {(short)0x401,(short)1} ); // L
		calls.add( new short[] {(short)0x501,(short)1} ); // R
		calls.add( new short[] {(short)0x801,(short)1} ); // Select
		calls.add( new short[] {(short)0x901,(short)1} ); // Start

		calls.add( new short[] {(short)0x901,(short)0} ); // Start
		calls.add( new short[] {(short)0x801,(short)0} ); // Select
		calls.add( new short[] {(short)0x501,(short)0} ); // R
		calls.add( new short[] {(short)0x401,(short)0} ); // L

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
		
		JoystickMediaPlayerClient.main(new String[] {"-j",fn,"-h","localhost","-p","6600"});
		
		try { Thread.sleep(1000);} catch (InterruptedException e) {}
		
		f.delete();
	}
}
