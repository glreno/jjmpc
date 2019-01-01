package com.rfacad.audioCommands;

import org.apache.logging.log4j.Level;

import com.rfacad.buttons.BState;
import com.rfacad.buttons.ButtonCommand;
import com.rfacad.buttons.CmdLog;
import com.rfacad.buttons.CmdPause;
import com.rfacad.buttons.CmdSh;
import com.rfacad.buttons.mapper.ButtonMapper;
import com.rfacad.joystick.CmdExitJoystickDriver;
import com.rfacad.joystick.RidiculouslySimpleJoystickDriver;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class JBMTest
{
	private static String APLAY="/usr/bin/aplay";
	private static String PLUCK="/usr/lib/libreoffice/share/gallery/sounds/pluck.wav";

	private static int lastssc=0;
	private static class SuperSecretCmd implements ButtonCommand {
		private int me;
		SuperSecretCmd(int i) { me=i;}
		public boolean button(BState state) {
			boolean sayit=false;
			if ( me==lastssc+1 ) {
				if ( me==4 ) {
					lastssc=0;
					sayit=true;
				}
				else {
					lastssc=me;
				}
			}
			else {
				lastssc=0;
				return false;
			}
			if ( sayit ) {
				new CmdPause(2000).button(state);
				return new CmdSay("Byte! Byte! Byte!").button(state);
			}
			return true;
		}
	}

	public static void main(String [] args)
	{
		String device="/dev/input/js0";
		if ( args.length > 0 ) device=args[0];

		RidiculouslySimpleJoystickDriver jdriver;
		jdriver=new RidiculouslySimpleJoystickDriver(device);

		ButtonMapper jbm=new ButtonMapper();
		jdriver.setListener(jbm);

		CmdExitJoystickDriver exitj=new CmdExitJoystickDriver(jdriver);
	
		// on error, say oops
		jbm.onError(new CmdSay("oops"));


		// Map most of the buttons
		jbm.map(0x0901,1,0,ButtonMapper.ANY_SHIFT_STATE,exitj);

		jbm.map(0x0001,1,0,ButtonMapper.AT_LEAST_ONE_SHIFT,new CmdLog(Level.INFO,"X",true));
		jbm.map(0x0001,1,0,ButtonMapper.NO_SHIFT,new CmdSh(false,APLAY,PLUCK));
		jbm.map(0x0101,1,0,ButtonMapper.ANY_SHIFT_STATE,new CmdLog(Level.INFO,"A",true));
		jbm.map(0x0201,1,0,(short)3,new CmdLog(Level.INFO,"B",true));
		jbm.map(0x0301,1,0,ButtonMapper.AT_LEAST_ONE_SHIFT,new CmdLog(Level.INFO,"Y",true));

		// left
		jbm.map(0x0401,0,1,ButtonMapper.ANY_SHIFT_STATE,
			jbm.mkCmdShift(1)
		);
		jbm.map(0x0401,1,0,ButtonMapper.ANY_SHIFT_STATE,jbm.mkCmdUnshift(1));
		// right
		jbm.map(0x0501,0,1,ButtonMapper.ANY_SHIFT_STATE,jbm.mkCmdShift(2));
		jbm.map(0x0501,1,0,ButtonMapper.ANY_SHIFT_STATE,jbm.mkCmdUnshift(2));

		// joystick LEFT
		jbm.map(0x0002,ButtonMapper.NEGATIVE,0,ButtonMapper.ANY_SHIFT_STATE,new CmdSay("shift to the left"),new SuperSecretCmd(1));
		// joystick RIGHT
		jbm.map(0x0002,ButtonMapper.POSITIVE,0,ButtonMapper.ANY_SHIFT_STATE,new CmdSay("shift to the right"),new SuperSecretCmd(2));
		// joystick UP
		jbm.map(0x0102,ButtonMapper.NEGATIVE,0,ButtonMapper.ANY_SHIFT_STATE,new CmdSay("pop up"),new SuperSecretCmd(3));
		// joystick DOWN
		jbm.map(0x0102,ButtonMapper.POSITIVE,0,ButtonMapper.ANY_SHIFT_STATE,new CmdSay("push down"),new SuperSecretCmd(4));

		// Init code
		new CmdSay("Greetings").button(null);

		// Main loop
		jdriver.spawn();

		//exitj.button(new BState((short)0,(short)0,(short)0));
	}
}

