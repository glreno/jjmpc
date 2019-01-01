package com.rfacad.mpd;

import java.util.*;
import java.util.concurrent.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import com.rfacad.mpd.interfaces.RSMPDListener;
import com.rfacad.mpd.interfaces.RidiculouslySimpleMPDClientI;


@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class RSMPDSyncCaller implements RSMPDListener
{
	private static final Logger log = LogManager.getLogger(RSMPDSyncCaller.class);
	
	protected RidiculouslySimpleMPDClientI mpdDriver;
	private CountDownLatch latch;
	private boolean retval;
	protected String cmdSent;
	protected List<String> response;

	private String responseCode;
	

	public RSMPDSyncCaller(RidiculouslySimpleMPDClientI md)
	{
		mpdDriver=md;
		latch=null;
	}
	
	/**
	 * Send the command, and wait for a response.
	 * If there is an error, return false, with no response stored.
	 * If MPD returns an ACK, return false, and record the response and message.
	 * If MPD returns OK, return true, and record the response.
	 * @param command
	 * @return
	 */
	public boolean send(String command)
	{
		try {
			cmdSent=command;
			retval=false;
			response=null;
			latch=new CountDownLatch(1);
			mpdDriver.sendCommand(command,this);
			try {
				latch.await(10,TimeUnit.SECONDS);
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

	public void ok(List<String> response)
	{
		this.retval=true;
		this.responseCode="OK";
		this.response=response;
		this.latch.countDown();
	}

	public void not_ok(String code,List<String> response)
	{
		log.warn(code);
		log.warn(response);
		this.retval=false;
		this.responseCode=code;
		this.response=response;
		this.latch.countDown();
	}

	public List<String> getResponse() {
		return this.response;
	}

	public String getResponseCode() {
		return this.responseCode;
	}
}

