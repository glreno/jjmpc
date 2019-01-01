package com.rfacad.buttons;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.rfacad.buttons.mapper.ButtonMapper;
import com.rfacad.buttons.MockCmd;
import com.rfacad.buttons.interfaces.BState;


@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class TestCmdBackground
{
	private static final short S0=0;
	private static final short S1=1;
	private static final String B0=ButtonMapper.NO_SHIFT;

	@Test
	public void shouldRunInBackground()
	{
		BackgroundManager bman=new BackgroundManager();
		ButtonMapper bm=new ButtonMapper();
		MockCmd c1=new MockCmd();
		MockCmd c2=new MockCmd();
		CountDownLatch latch1=new CountDownLatch(1);
		CountDownLatch latch2=new CountDownLatch(1);
		CmdBackground back=new CmdBackground(bman,new CmdLatchWait(latch1,1,TimeUnit.SECONDS),c1,new CmdLatchCountdown(latch2));

		// c1 will be called on a background thread
		// c2 will be called after the thread is spawned.
		// Thus c2 will complete first.
		bm.map(0,1,0,B0,back,c2);
		
		// First press: call c2 (but not c1 or err)
		bm.button(S0,S1,S0);
		c1.assertCalls(0);
		c2.assertCalls(1);
		
		// Now wait a second.
		latch1.countDown();
		try {
			latch2.await(1,TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		}
		c1.assertCalls(1);
		c2.assertCalls(1);

		new CmdExitBackgrounManager(bman).button(null);
	}

	@Test
	public void shouldSRunInBackgroundUntilFalse()
	{
		BackgroundManager bman=new BackgroundManager();
		ButtonMapper bm=new ButtonMapper();
		MockCmd c1=new MockCmd();
		MockCmd c2=new MockCmd();
		MockCmd c3=new MockCmd(false);
		MockCmd c4=new MockCmd();
		CountDownLatch latch1=new CountDownLatch(1);
		CountDownLatch latch2=new CountDownLatch(1);
		CmdBackground back=new CmdBackground(bman,new CmdLatchWait(latch1,1,TimeUnit.SECONDS),c1,new CmdLatchCountdown(latch2),c3,c4);

		// c1 will be called on a background thread
		// c2 will be called after the thread is spawned.
		// Thus c2 will complete first.
		bm.map(0,1,0,B0,back,c2);
		
		// First press: call c2 (but not c1 or err)
		bm.button(S0,S1,S0);
		c1.assertCalls(0);
		c2.assertCalls(1);
		c3.assertCalls(0);
		c4.assertCalls(0);
		
		// Now wait a second.
		latch1.countDown();
		try {
			latch2.await(1,TimeUnit.SECONDS);
			Thread.sleep(1000);
			// two more commands after the latch: c3 and c4
			// c3 will return false, so c4 will not run.
			// And they should take way less than a second.
		} catch (InterruptedException e) {
		}
		c1.assertCalls(1);
		c2.assertCalls(1);
		c3.assertCalls(1);
		c4.assertCalls(0);
		
		new CmdExitBackgrounManager(bman).button(null);
	}

	
}
