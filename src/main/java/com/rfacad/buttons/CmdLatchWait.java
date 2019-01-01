package com.rfacad.buttons;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.rfacad.buttons.interfaces.BState;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdLatchWait implements ButtonCommand
{
	protected CountDownLatch latch;
	protected long timeout;
	protected TimeUnit unit;
	public CmdLatchWait(CountDownLatch latch,long timeout,TimeUnit unit)
	{
		this.latch=latch;
		this.timeout=timeout;
		this.unit=unit;
	}
	public boolean button(BState state)
	{
		boolean ret;
		try {
			ret = latch.await(timeout, unit);
		} catch (InterruptedException e) {
			ret=false;
		}
		return ret;
	}
}

