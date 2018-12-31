package com.rfacad.jjmpc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdExit implements ButtonCommand
{
	private static final Logger log = LogManager.getLogger(CmdExit.class);

	RidiculouslySimpleJoystickDriver joystickDriver;
	RidiculouslySimpleMPDClient mpdDriver;

	public CmdExit(RidiculouslySimpleJoystickDriver jd, RidiculouslySimpleMPDClient md)
	{
		joystickDriver=jd;
		mpdDriver=md;
	}
	public boolean button(BState state)
	{
		log.info("Shutting down.");
		if (joystickDriver!=null) {
			log.info("Shutting down joystick driver thread");
			joystickDriver.shutdown();
		}
		if (mpdDriver!=null){
			log.info("Shutting down MPD driver thread");
			 mpdDriver.shutdown();
		}
		return true;
	}
}

