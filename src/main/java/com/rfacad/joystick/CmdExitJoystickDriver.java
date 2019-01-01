package com.rfacad.joystick;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rfacad.buttons.interfaces.BState;
import com.rfacad.buttons.ButtonCommand;


@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdExitJoystickDriver implements ButtonCommand
{
	private static final Logger log = LogManager.getLogger(CmdExitJoystickDriver.class);

	RidiculouslySimpleJoystickDriver joystickDriver;

	public CmdExitJoystickDriver(RidiculouslySimpleJoystickDriver jd)
	{
		joystickDriver=jd;
	}
	public boolean button(BState state)
	{
		log.info("Shutting down.");
		if (joystickDriver!=null) {
			log.debug("Shutting down joystick driver thread");
			joystickDriver.shutdown();
		}
		return true;
	}
}

