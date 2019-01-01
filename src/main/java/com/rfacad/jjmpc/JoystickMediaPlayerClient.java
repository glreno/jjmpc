package com.rfacad.jjmpc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rfacad.License;
import com.rfacad.audioCommands.CmdSay;
import com.rfacad.audioCommands.CmdSound;
import com.rfacad.buttons.BackgroundManager;
import com.rfacad.buttons.ButtonCommand;
import com.rfacad.buttons.ChordCommand;
import com.rfacad.buttons.CmdBackground;
import com.rfacad.buttons.CmdExitBackgrounManager;
import com.rfacad.buttons.CmdLog;
import com.rfacad.buttons.CmdPause;
import com.rfacad.buttons.CmdSecondTime;
import com.rfacad.buttons.CmdSh;
import com.rfacad.buttons.mapper.ButtonMapper;
import com.rfacad.joystick.CmdExitJoystickDriver;
import com.rfacad.joystick.RidiculouslySimpleJoystickDriver;
import com.rfacad.mpd.CmdExitMpdDriver;
import com.rfacad.mpd.RidiculouslySimpleMPDClient;
import com.rfacad.mpd.interfaces.PlaylistDBI;
import com.rfacad.mpd.playlistdb.PlaylistDB;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class JoystickMediaPlayerClient
{
	private static final Logger log = LogManager.getLogger(JoystickMediaPlayerClient.class);

	private static final String L = "L";
	private static final String R = "R";
	private static String [] BOTH=new String[] { L, R };

	private static String PLUCK="/usr/lib/libreoffice/share/gallery/sounds/pluck.wav";
	private RidiculouslySimpleJoystickDriver jdriver;
	private RidiculouslySimpleMPDClient mdriver;
	private PlaylistDB db;
	private ButtonMapper jbm;
	private CmdExitJoystickDriver exitj;
	private CmdExitMpdDriver exitm;
	private CmdMpdStatus status;

	private CmdExitBackgrounManager exitb;

	private BackgroundManager bm;

	public static void main(String [] args) {
		// Really should be getting this from a resource file
		int state=0;
		String device="/dev/input/js0";
		String host="localhost";
		String port="6600";
		for(String s: args)
		{
			if ("-p".equals(s)) {
				state=1;
			}
			else if ( "-h".equals(s) ) {
				state=2;
			}
			else if ( "-j".equals(s) ) {
				state=3;
			}
			else {
				if (state==1) {
					port=s;
					state=0;
				}
				else if ( state==2) {
					host=s;
					state=0;
				}
				else if ( state==3) {
					device=s;
					state=0;
				}
			}
		}
		int portnum=Integer.parseInt(port);
		log.info(License.LICENSE);
		log.info("Starting up.");
		JoystickMediaPlayerClient jjmpc = new JoystickMediaPlayerClient(device,host,portnum);
		jjmpc.loadCommands();
		jjmpc.startupScript();
		jjmpc.start();
	}

	public JoystickMediaPlayerClient(String device,String host,int port) {
		// Really bad idea to use hardcoded paths
		jdriver=new RidiculouslySimpleJoystickDriver(device);
		jbm=new ButtonMapper();
		jdriver.setListener(jbm);
		bm=new BackgroundManager();
		mdriver=new RidiculouslySimpleMPDClient(host,port);
		db=new PlaylistDB(mdriver);
		exitj=new CmdExitJoystickDriver(jdriver);
		exitm=new CmdExitMpdDriver(mdriver);
		exitb=new CmdExitBackgrounManager(bm);
		status=new CmdMpdStatus(mdriver);
	}

	public void start() {
		//new Thread(mdriver).start();
		jdriver.spawn();
	}

	protected void startupScript() {
		log.debug("Running startup script");
		new CmdMpd(mdriver,"setvol 70").button(null);
		new CmdSay("Greetings").button(null);
		log.debug("startup script complete");
	}

	protected void loadCommands() {
		// on error, say oops
		jbm.onError(new CmdSay("oops"));

		// Shift buttons (the shoulder buttons)
		// All command buttons require that a shift button be pressed.

		// left
		jbm.map(0x401,0,1,ButtonMapper.ANY_SHIFT_STATE,CmdLog.debug("401 left shift down"), jbm.mkCmdShift(L));
		jbm.map(0x401,1,0,ButtonMapper.ANY_SHIFT_STATE,CmdLog.debug("401 left shift up"), jbm.mkCmdUnshift(L));
		// right
		jbm.map(0x501,0,1,ButtonMapper.ANY_SHIFT_STATE,CmdLog.debug("501 right shift down"), jbm.mkCmdShift(R));
		jbm.map(0x501,1,0,ButtonMapper.ANY_SHIFT_STATE,CmdLog.debug("501 right shift up"), jbm.mkCmdUnshift(R));

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
		ChordCommand exCmd=new ChordCommand(3,
				CmdLog.info("preparing to exit"),
				jbm.mkCmdOnError(null),
				new CmdSecondTime(
					new CmdSay("shutting down"),
					new CmdSh(false,"/usr/bin/sudo","/sbin/shutdown","-h","now")
				),
				new CmdBackground(bm,
					new CmdSay("press again to shut down pi"),
					new CmdPause(5000),
					CmdLog.info("exiting"),
					exitj,exitm,exitb
				));
		jbm.map(0x0801,0,1,BOTH,CmdLog.debug("801 select down"), exCmd.mkSet(1)); // SELECT - press
		jbm.map(0x0901,0,1,BOTH,CmdLog.debug("901 start down"), exCmd.mkSet(2)); // START - press
		// the 'real' select & start commands will also call
		// cmdMaybeExit, which will either exit, or
		// unset the exState

		//
		// Select - change play mode (maybe exit)
		// (Modes are track-once and playlist-once)
		//
		jbm.map(0x0801,1,0,BOTH,CmdLog.debug("801 select_1"),exCmd.mkCheck(1)); // SELECT - exit mode
		jbm.map(0x0801,1,0,L,CmdLog.debug("801 select_2"),status,new CmdPlayMode(status),new CmdSay("%mode% mode"));
		jbm.map(0x0801,1,0,R,CmdLog.debug("801 select_3"),status,new CmdPlayMode(status),new CmdSay("%mode% mode"));

		//
		// Start - play/pause (maybe exit)
		// The 'play' command starts the next track, if the last one ended.
		//
		jbm.map(0x0901,1,0,BOTH,CmdLog.debug("901 start_1"),exCmd.mkCheck(2)); // START - exit mode
		jbm.map(0x0901,1,0,L,CmdLog.debug("901 start_2"),status,new CmdPlayPause(status));
		jbm.map(0x0901,1,0,R,CmdLog.debug("901 start_3"),status,new CmdPlayPause(status));

		//
		// DPad Vert - volume up & down
		//
		jbm.map(0x0102,ButtonMapper.NEGATIVE,0,ButtonMapper.AT_LEAST_ONE_SHIFT,CmdLog.debug("102 Up"),status,new CmdVolume(status,10));
		jbm.map(0x0102,ButtonMapper.POSITIVE,0,ButtonMapper.AT_LEAST_ONE_SHIFT,CmdLog.debug("102 Down"),status,new CmdVolume(status,-10));

		//
		// DPad Horz - ffwd/rev, prev/next playlist folder (announce name) when both shifts are pressed
		//
		
		//
		// Y/A - track next/prev
		//
		jbm.map(0x0301,1,0,ButtonMapper.AT_LEAST_ONE_SHIFT,CmdLog.debug("301 Y track prev"),status, new CmdTrackPrev(status,db)); // Y
		jbm.map(0x0101,1,0,ButtonMapper.AT_LEAST_ONE_SHIFT,CmdLog.debug("101 A track next"),status, new CmdTrackNext(status,db)); // A

		
		// X/B - playlist next/prev (announce name)
		jbm.map(0x0001,1,0,ButtonMapper.AT_LEAST_ONE_SHIFT,CmdLog.debug("001 X playlist next"),status, new CmdPlaylistNext(status,db), new CmdMpd(status,"pause"), new CmdSay("Next %"+PlaylistDBI.PLAYLIST_LOADED+"%"), new CmdPause(5000), new CmdMpd(status,"play")); // X
		jbm.map(0x0201,1,0,ButtonMapper.AT_LEAST_ONE_SHIFT,CmdLog.debug("201 B playlist prev"),status, new CmdPlaylistPrev(status,db), new CmdMpd(status,"pause"), new CmdSay("Previous %"+PlaylistDBI.PLAYLIST_LOADED+"%"), new CmdPause(5000), new CmdMpd(status,"play")); // B
		
		
		// Buttons from test program
		// Map most of the buttons

//		jbm.map(0x0001,1,0,ButtonMapper.AT_LEAST_ONE_SHIFT,new CmdLog("X",true));
//		jbm.map(0x0001,1,0,ButtonMapper.NO_SHIFT,new CmdSound(PLUCK));
//		jbm.map(0x0101,1,0,ButtonMapper.ANY_SHIFT_STATE,new CmdLog("A",true));
//		jbm.map(0x0201,1,0,(short)3,new CmdLog("B",true));
//		jbm.map(0x0301,1,0,ButtonMapper.AT_LEAST_ONE_SHIFT,new CmdLog("Y",true));

		// joystick LEFT
		//jbm.map(0x0002,ButtonMapper.NEGATIVE,0,ButtonMapper.ANY_SHIFT_STATE,new CmdSay("shift to the left"),new SuperSecretCmd(1));
		// joystick RIGHT
		//jbm.map(0x0002,ButtonMapper.POSITIVE,0,ButtonMapper.ANY_SHIFT_STATE,new CmdSay("shift to the right"),new SuperSecretCmd(2));
		// joystick UP
		// joystick DOWN
		//jbm.map(0x0102,ButtonMapper.POSITIVE,0,ButtonMapper.ANY_SHIFT_STATE,new CmdSay("push down"),new SuperSecretCmd(4));
	}

}

