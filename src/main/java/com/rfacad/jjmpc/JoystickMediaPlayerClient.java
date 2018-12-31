package com.rfacad.jjmpc;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class JoystickMediaPlayerClient
{
	private static String PLUCK="/usr/lib/libreoffice/share/gallery/sounds/pluck.wav";
	private RidiculouslySimpleJoystickDriver jdriver;
	private RidiculouslySimpleMPDClient mdriver;
	private ButtonMapper jbm;
	private CmdExit exit;

	// Support for two-button-chord exit command
	private int exitstate=0;
	private class setExState implements ButtonCommand {
		int val;
		public setExState(int n) { val=n;}
		public boolean button(BState state) {
			exitstate|=val;
			return true;
		}
	}
	private class maybeExit implements ButtonCommand {
		int val;
		public maybeExit(int n) { val=n;}
		public boolean button(BState state) {
			if ( exitstate==3 ) {
				// shut down!
				return exit.button(state);
			}
			exitstate&=~val;
			return true;
		}
	}


	public static void main(String [] args) {
		JoystickMediaPlayerClient jjmpc = new JoystickMediaPlayerClient();
		jjmpc.loadCommands();
		jjmpc.start();
		jjmpc.startupScript();
	}

	public JoystickMediaPlayerClient() {
		// Really bad idea to use hardcoded paths
		jdriver=new RidiculouslySimpleJoystickDriver("/dev/input/js0");
		jbm=new ButtonMapper();
		jdriver.setListener(jbm);
		mdriver=new RidiculouslySimpleMPDClient("localhost",6600);
		exit=new CmdExit(jdriver,mdriver);
	}

	public void start() {
		new Thread(mdriver).start();
		new Thread(jdriver).start();
	}

	protected void startupScript() {
		new CmdMpd(mdriver,"setvol 70").button(null);
		new CmdSay("Greetings").button(null);
	}

	protected void loadCommands() {
		// on error, say oops
		jbm.onError(new CmdSay("oops"));

		// Shift buttons (the shoulder buttons)
		// All command buttons require that a shift button be pressed.

		// left
		jbm.map(0x104,0,1,ButtonMapper.ANY_SHIFT_STATE, jbm.mkCmdShift(1));
		jbm.map(0x104,1,0,ButtonMapper.ANY_SHIFT_STATE,jbm.mkCmdUnshift(1));
		// right
		jbm.map(0x105,0,1,ButtonMapper.ANY_SHIFT_STATE,jbm.mkCmdShift(2));
		jbm.map(0x105,1,0,ButtonMapper.ANY_SHIFT_STATE,jbm.mkCmdUnshift(2));

		// Error sound if any button is pressed without a shift.
		ButtonCommand fail=new CmdSound(PLUCK);
		jbm.map(0x0100,1,0,ButtonMapper.NO_SHIFT,fail); // X
		jbm.map(0x0101,1,0,ButtonMapper.NO_SHIFT,fail); // A
		jbm.map(0x0102,1,0,ButtonMapper.NO_SHIFT,fail); // B
		jbm.map(0x0103,1,0,ButtonMapper.NO_SHIFT,fail); // Y
		jbm.map(0x0108,1,0,ButtonMapper.NO_SHIFT,fail); // SELECT
		jbm.map(0x0109,1,0,ButtonMapper.NO_SHIFT,fail); // START
		jbm.map(0x200,ButtonMapper.ANY,0,ButtonMapper.NO_SHIFT,fail); // H
		jbm.map(0x201,ButtonMapper.ANY,0,ButtonMapper.NO_SHIFT,fail); // V


		// Exit command: Both shifts, select, and start
		jbm.map(0x0108,0,1,(short)3,new setExState(1)); // SELECT - press
		jbm.map(0x0109,0,1,(short)3,new setExState(2)); // START - press
		// the 'real' select & start commands will also call
		// cmdMaybeExit, which will either exit, or
		// unset the exState

		// Select - change play mode (maybe exit)
		jbm.map(0x0108,1,0,(short)3,new maybeExit(1)); // SELECT - exit mode
		// Start - play/pause (maybe exit)
		jbm.map(0x0109,1,0,(short)3,new maybeExit(2)); // START - exit mode

		// Buttons from test program

		// Map most of the buttons

		jbm.map(0x0100,1,0,ButtonMapper.AT_LEAST_ONE_SHIFT,new CmdLog("X",true));
		jbm.map(0x0100,1,0,ButtonMapper.NO_SHIFT,new CmdSound(PLUCK));
		jbm.map(0x0101,1,0,ButtonMapper.ANY_SHIFT_STATE,new CmdLog("A",true));
		jbm.map(0x0102,1,0,(short)3,new CmdLog("B",true));
		jbm.map(0x0103,1,0,ButtonMapper.AT_LEAST_ONE_SHIFT,new CmdLog("Y",true));

		// joystick LEFT
		//jbm.map(0x200,ButtonMapper.NEGATIVE,0,ButtonMapper.ANY_SHIFT_STATE,new CmdSay("shift to the left"),new SuperSecretCmd(1));
		// joystick RIGHT
		//jbm.map(0x200,ButtonMapper.POSITIVE,0,ButtonMapper.ANY_SHIFT_STATE,new CmdSay("shift to the right"),new SuperSecretCmd(2));
		// joystick UP
		//jbm.map(0x201,ButtonMapper.NEGATIVE,0,ButtonMapper.ANY_SHIFT_STATE,new CmdSay("pop up"),new SuperSecretCmd(3));
		// joystick DOWN
		//jbm.map(0x201,ButtonMapper.POSITIVE,0,ButtonMapper.ANY_SHIFT_STATE,new CmdSay("push down"),new SuperSecretCmd(4));
	}

}

