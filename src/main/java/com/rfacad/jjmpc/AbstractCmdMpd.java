package com.rfacad.jjmpc;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rfacad.buttons.ButtonCommand;
import com.rfacad.buttons.interfaces.BState;
import com.rfacad.mpd.interfaces.RSMPDListener;
import com.rfacad.mpd.interfaces.RidiculouslySimpleMPDClientI;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public abstract class AbstractCmdMpd implements ButtonCommand, RSMPDListener
{

	public static final Logger log = LogManager.getLogger(CmdMpd.class);
	protected RidiculouslySimpleMPDClientI mpdDriver;
	private CountDownLatch latch;
	private boolean retval;
	private List<String> response;

	public AbstractCmdMpd(RidiculouslySimpleMPDClientI md)
	{
		mpdDriver=md;
		latch=null;
	}
	public AbstractCmdMpd(AbstractCmdMpd another)
	{
		mpdDriver=another.mpdDriver;
		latch=null;
	}

	protected boolean sendCommand(BState state, String cmdToSend)
	{
		try {
			retval=false;
			response=null;
			latch=new CountDownLatch(1);
			mpdDriver.sendCommand(cmdToSend,this);
			try {
				latch.await();
			}
			catch (InterruptedException e) {
				// retval is false
			}
			return retval;
		}
		catch (IOException|RejectedExecutionException e) {
			log.error("Exception contacting MPD",e);
			return false;
		}
	}

	protected boolean sendCommands(BState state, String ... cmdsToSend)
	{
		for(String cmdToSend : cmdsToSend )
		{
			boolean ok = sendCommand(state,cmdToSend);
			if ( !ok )
			{
				return false;
			}
		}
		return true;
	}
	
	public void ok(List<String> response) {
		this.retval=true;
		this.response=response;
		this.latch.countDown();
	}

	public void not_ok(String code, List<String> response)
	{
		log.warn(code);
		log.warn(response);
		this.retval=false;
		this.response=response;
		this.latch.countDown();
	}

	protected List<String> getResponse()
	{
		return this.response;
	}

}