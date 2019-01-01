package com.rfacad.mpd;

import java.io.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rfacad.mpd.SyncMPDCall.MPDCallDoneListener;
import com.rfacad.mpd.interfaces.RSMPDListener;
import com.rfacad.mpd.interfaces.RidiculouslySimpleMPDClientI;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class RidiculouslySimpleMPDClient implements MPDCallDoneListener, RidiculouslySimpleMPDClientI
{
	private static final Logger log = LogManager.getLogger(RidiculouslySimpleMPDClient.class);

	private String address;
	private int port;
	private ExecutorService executor;
	private Queue<SyncMPDCall> jobs;
	private boolean shuttingdown;

	public RidiculouslySimpleMPDClient(String address, int port)
	{
		this.address=address;
		this.port=port;
		this.executor=Executors.newCachedThreadPool();
		this.jobs=new ConcurrentLinkedQueue<>();
		this.shuttingdown=false;
	}
	
	public void setExecutor(ExecutorService executor)
	{
		this.executor=executor;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public void sendCommand(String command, RSMPDListener listener)
	{
		SyncMPDCall r=new SyncMPDCall(address,port,command,listener,this);
		jobs.add(r);
		if ( shuttingdown )
		{
			log.debug("Shutting down, not executing command {}",command);
		}
		else
		{
			try
			{
				executor.execute(r);
			}
			catch (RejectedExecutionException e)
			{
				// this can happen, most likely if
				// a command got queued during shutdown
			}
		}
	}
	
	public void shutdown()
	{
		shuttingdown=true;
		int nj=jobs.size();
		if ( nj > 0 )
		{
			log.warn("Shutting down executor, there are {} jobs",jobs.size());
		}
		else
		{
			log.debug("Shutting down executor");
		}
		// stop processing new jobs
		executor.shutdown();
		// Warn the jobs
		for(SyncMPDCall r:jobs)
		{
			r.shutdown();
		}
		// interrupt any jobs that are still running
		log.debug("Final force of executor shutdown");
		executor.shutdownNow();
		log.info("executor shut down");
	}

	@Override
	public void callDone(SyncMPDCall c) {
		jobs.remove(c);
	}
}
