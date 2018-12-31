package com.rfacad.jjmpc;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdPause implements ButtonCommand
{
	private int millis;
	public CmdPause(int millis) { this.millis=millis;}
	public boolean button(BState state)
	{
		try {
			Thread.sleep(millis);
		}
		catch (InterruptedException e) {}
		return true;
	}
}

