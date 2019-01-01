package com.rfacad.jjmpc;

import java.util.*;
import java.util.concurrent.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import com.rfacad.buttons.BState;
import com.rfacad.buttons.ButtonCommand;
import com.rfacad.mpd.interfaces.RSMPDListener;
import com.rfacad.mpd.RidiculouslySimpleMPDClient;


@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdMpd implements ButtonCommand, RSMPDListener
{
	private static final Logger log = LogManager.getLogger(CmdMpd.class);
	
	protected RidiculouslySimpleMPDClient mpdDriver;
	protected String command;
	private CountDownLatch latch;
	private boolean retval;
	protected List<String> response;
	

	public CmdMpd(RidiculouslySimpleMPDClient md,String cmd)
	{
		mpdDriver=md;
		command=cmd;
		latch=null;
	}
	public CmdMpd(CmdMpd another,String cmd)
	{
		mpdDriver=another.mpdDriver;
		command=cmd;
		latch=null;
	}
	public boolean button(BState state)
	{
		try {
			retval=false;
			response=null;
			latch=new CountDownLatch(1);
			mpdDriver.sendCommand(command,this);
			try {
				latch.await();
			}
			catch (InterruptedException e) {
				// retval is false
			}
			return retval;
		}
		catch (IOException e) {
			log.error("Exception contacting MPD",e);
			return false;
		}
	}

	public void ok(List<String> response)
	{
		this.retval=true;
		this.response=response;
		this.latch.countDown();
	}

	public void not_ok(String code,List<String> response)
	{
		log.warn(code);
		log.warn(response);
		this.retval=false;
		this.response=response;
		this.latch.countDown();
	}

	protected List<String> getResponse() {
		return this.response;
	}

	protected void setCommand(String s) {
		this.command=s;
	}
}

