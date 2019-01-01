package com.rfacad.buttons;

import java.util.concurrent.CountDownLatch;

import com.rfacad.buttons.interfaces.BState;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdLatchCountdown implements ButtonCommand
{
	protected CountDownLatch latch;
	public CmdLatchCountdown(CountDownLatch latch)
	{
		this.latch=latch;
	}
	public boolean button(BState state)
	{
		latch.countDown();
		return true;
	}
}

