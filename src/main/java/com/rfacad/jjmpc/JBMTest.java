package com.rfacad.jjmpc;

import com.rfacad.buttons.BState;
import com.rfacad.buttons.ButtonCommand;
import com.rfacad.buttons.CmdLog;
import com.rfacad.buttons.CmdPause;
import com.rfacad.buttons.CmdSh;
import com.rfacad.buttons.mapper.ButtonMapper;
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
		RidiculouslySimpleJoystickDriver jdriver;
		jdriver=new RidiculouslySimpleJoystickDriver("/dev/input/js0");

		ButtonMapper jbm=new ButtonMapper();
		jdriver.setListener(jbm);

		CmdExit exit=new CmdExit(jdriver,null);
		CmdLog log=new CmdLog();
	
		// on error, say oops
		jbm.onError(new CmdSay("oops"));


		// Map most of the buttons
		jbm.map(0x0109,1,0,ButtonMapper.ANY_SHIFT_STATE,exit);

		jbm.map(0x0100,1,0,ButtonMapper.AT_LEAST_ONE_SHIFT,new CmdLog("X",true));
		jbm.map(0x0100,1,0,ButtonMapper.NO_SHIFT,new CmdSh(false,APLAY,PLUCK));
		jbm.map(0x0101,1,0,ButtonMapper.ANY_SHIFT_STATE,new CmdLog("A",true));
		jbm.map(0x0102,1,0,(short)3,new CmdLog("B",true));
		jbm.map(0x0103,1,0,ButtonMapper.AT_LEAST_ONE_SHIFT,new CmdLog("Y",true));

		// left
		jbm.map(0x104,0,1,ButtonMapper.ANY_SHIFT_STATE,
			jbm.mkCmdShift(1)
		);
		jbm.map(0x104,1,0,ButtonMapper.ANY_SHIFT_STATE,jbm.mkCmdUnshift(1));
		// right
		jbm.map(0x105,0,1,ButtonMapper.ANY_SHIFT_STATE,jbm.mkCmdShift(2));
		jbm.map(0x105,1,0,ButtonMapper.ANY_SHIFT_STATE,jbm.mkCmdUnshift(2));

		// joystick LEFT
		jbm.map(0x200,ButtonMapper.NEGATIVE,0,ButtonMapper.ANY_SHIFT_STATE,new CmdSay("shift to the left"),new SuperSecretCmd(1));
		// joystick RIGHT
		jbm.map(0x200,ButtonMapper.POSITIVE,0,ButtonMapper.ANY_SHIFT_STATE,new CmdSay("shift to the right"),new SuperSecretCmd(2));
		// joystick UP
		jbm.map(0x201,ButtonMapper.NEGATIVE,0,ButtonMapper.ANY_SHIFT_STATE,new CmdSay("pop up"),new SuperSecretCmd(3));
		// joystick DOWN
		jbm.map(0x201,ButtonMapper.POSITIVE,0,ButtonMapper.ANY_SHIFT_STATE,new CmdSay("push down"),new SuperSecretCmd(4));

		// Init code
		new CmdSay("Greetings").button(null);

		// Main loop
		jdriver.run();

		exit.button(new BState((short)0,(short)0,(short)0));
	}
}

