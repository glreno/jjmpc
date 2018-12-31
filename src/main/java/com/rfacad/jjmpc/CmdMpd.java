package com.rfacad.jjmpc;

import java.util.*;
import java.util.concurrent.*;
import java.io.IOException;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdMpd implements ButtonCommand, RidiculouslySimpleMPDClient.RSMPDListener
{
	protected RidiculouslySimpleMPDClient mpdDriver;
	protected String command;
	private CountDownLatch latch;
	private boolean retval;
	

	public CmdMpd(RidiculouslySimpleMPDClient md,String cmd)
	{
		mpdDriver=md;
		command=cmd;
		latch=null;
	}
	public boolean button(BState state)
	{
		try {
			retval=false;
			mpdDriver.setListener(this);
			latch=new CountDownLatch(1);
			mpdDriver.sendCommand(command);
			try {
				latch.await();
			}
			catch (InterruptedException e) {
				// retval is false
			}
			mpdDriver.setListener(null);
			return retval;
		}
		catch (IOException e) {
			e.printStackTrace();
			mpdDriver.closeSocket();
			return false;
		}
	}

	public void ok(List<String> response)
	{
		retval=true;
		latch.countDown();
	}

	public void not_ok(String code,List<String> response)
	{
		System.err.println(code);
		System.err.println(response);
		retval=false;
		latch.countDown();
	}
}

