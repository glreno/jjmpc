package com.rfacad.jjmpc;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdExit implements ButtonCommand
{
	RidiculouslySimpleJoystickDriver joystickDriver;
	RidiculouslySimpleMPDClient mpdDriver;

	public CmdExit(RidiculouslySimpleJoystickDriver jd, RidiculouslySimpleMPDClient md)
	{
		joystickDriver=jd;
		mpdDriver=md;
	}
	public boolean button(BState state)
	{
		if (joystickDriver!=null) joystickDriver.shutdown();
		if (mpdDriver!=null) mpdDriver.shutdown();
		return true;
	}
}

