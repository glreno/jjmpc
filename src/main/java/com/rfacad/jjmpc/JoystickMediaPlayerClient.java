package com.rfacad.jjmpc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rfacad.buttons.ButtonCommand;
import com.rfacad.buttons.ChordCommand;
import com.rfacad.buttons.CmdLog;
import com.rfacad.buttons.mapper.ButtonMapper;
import com.rfacad.joystick.RidiculouslySimpleJoystickDriver;
import com.rfacad.mpd.RidiculouslySimpleMPDClient;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class JoystickMediaPlayerClient
{
	private static final Logger log = LogManager.getLogger(JoystickMediaPlayerClient.class);

	private static String PLUCK="/usr/lib/libreoffice/share/gallery/sounds/pluck.wav";
	private RidiculouslySimpleJoystickDriver jdriver;
	private RidiculouslySimpleMPDClient mdriver;
	private ButtonMapper jbm;
	private CmdExit exit;
	private CmdMpdStatus status;

	public static void main(String [] args) {
		log.info("Starting up.");
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
		status=new CmdMpdStatus(mdriver);
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
		jbm.map(0x401,0,1,ButtonMapper.ANY_SHIFT_STATE, jbm.mkCmdShift(1));
		jbm.map(0x401,1,0,ButtonMapper.ANY_SHIFT_STATE,jbm.mkCmdUnshift(1));
		// right
		jbm.map(0x501,0,1,ButtonMapper.ANY_SHIFT_STATE,jbm.mkCmdShift(2));
		jbm.map(0x501,1,0,ButtonMapper.ANY_SHIFT_STATE,jbm.mkCmdUnshift(2));

		// Error sound if any button is pressed without a shift.
		ButtonCommand fail=new CmdSound(PLUCK);
		jbm.map(0x0001,1,0,ButtonMapper.NO_SHIFT,fail); // X
		jbm.map(0x0101,1,0,ButtonMapper.NO_SHIFT,fail); // A
		jbm.map(0x0201,1,0,ButtonMapper.NO_SHIFT,fail); // B
		jbm.map(0x0301,1,0,ButtonMapper.NO_SHIFT,fail); // Y
		jbm.map(0x0801,1,0,ButtonMapper.NO_SHIFT,fail); // SELECT
		jbm.map(0x0901,1,0,ButtonMapper.NO_SHIFT,fail); // START
		jbm.map(0x0002,ButtonMapper.ANY,0,ButtonMapper.NO_SHIFT,fail); // H
		jbm.map(0x0102,ButtonMapper.ANY,0,ButtonMapper.NO_SHIFT,fail); // V


		// Exit command: Both shifts, select, and start
		ChordCommand exCmd=new ChordCommand(3,new CmdLog("exiting"),exit);
		jbm.map(0x0801,0,1,(short)3,exCmd.mkSet(1)); // SELECT - press
		jbm.map(0x0901,0,1,(short)3,exCmd.mkSet(2)); // START - press
		// the 'real' select & start commands will also call
		// cmdMaybeExit, which will either exit, or
		// unset the exState

		//
		// Select - change play mode (maybe exit)
		// (Modes are track-once and playlist-once)
		//
		jbm.map(0x0801,1,0,(short)3,exCmd.mkCheck(1),status,new CmdLog("select_1",true),new CmdPlayMode(status),new CmdSay("%mode% mode")); // SELECT - exit mode
		jbm.map(0x0801,1,0,ButtonMapper.AT_LEAST_ONE_SHIFT,status,new CmdLog("select_2",true),new CmdPlayMode(status),new CmdSay("%mode% mode")); // SELECT - exit mode

		//
		// Start - play/pause (maybe exit)
		//
		jbm.map(0x0901,1,0,(short)3,new CmdLog("start_1",true),exCmd.mkCheck(2),status,new CmdPlayPause(status)); // START - exit mode
		jbm.map(0x0901,1,0,ButtonMapper.AT_LEAST_ONE_SHIFT,status,new CmdLog("start_2",true),new CmdPlayPause(status));

		//
		// DPad Vert - volume up & down
		//
		jbm.map(0x0102,ButtonMapper.NEGATIVE,0,ButtonMapper.ANY_SHIFT_STATE,status,new CmdVolume(status,10));
		jbm.map(0x0102,ButtonMapper.POSITIVE,0,ButtonMapper.ANY_SHIFT_STATE,status,new CmdVolume(status,-10));

		// Buttons from test program

		// Map most of the buttons

		jbm.map(0x0001,1,0,ButtonMapper.AT_LEAST_ONE_SHIFT,new CmdLog("X",true));
		jbm.map(0x0001,1,0,ButtonMapper.NO_SHIFT,new CmdSound(PLUCK));
		jbm.map(0x0101,1,0,ButtonMapper.ANY_SHIFT_STATE,new CmdLog("A",true));
		jbm.map(0x0201,1,0,(short)3,new CmdLog("B",true));
		jbm.map(0x0301,1,0,ButtonMapper.AT_LEAST_ONE_SHIFT,new CmdLog("Y",true));

		// joystick LEFT
		//jbm.map(0x0002,ButtonMapper.NEGATIVE,0,ButtonMapper.ANY_SHIFT_STATE,new CmdSay("shift to the left"),new SuperSecretCmd(1));
		// joystick RIGHT
		//jbm.map(0x0002,ButtonMapper.POSITIVE,0,ButtonMapper.ANY_SHIFT_STATE,new CmdSay("shift to the right"),new SuperSecretCmd(2));
		// joystick UP
		// joystick DOWN
		//jbm.map(0x0102,ButtonMapper.POSITIVE,0,ButtonMapper.ANY_SHIFT_STATE,new CmdSay("push down"),new SuperSecretCmd(4));
	}

}

