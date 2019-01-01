package com.rfacad.mpd;

import java.io.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rfacad.mpd.SyncMPDCall.MPDCallDoneListener;
import com.rfacad.mpd.interfaces.RSMPDListener;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class RidiculouslySimpleMPDClient implements MPDCallDoneListener
{
	private static final Logger log = LogManager.getLogger(RidiculouslySimpleMPDClient.class);

	private String address;
	private int port;
	private ExecutorService executor;
	private Queue<SyncMPDCall> jobs;

	public RidiculouslySimpleMPDClient(String address, int port)
	{
		this.address=address;
		this.port=port;
		this.executor=Executors.newCachedThreadPool();
		this.jobs=new ConcurrentLinkedQueue<>();
	}
	
	public void setExecutor(ExecutorService executor)
	{
		this.executor=executor;
	}

	public void sendCommand(String command, RSMPDListener listener) throws IOException
	{
		SyncMPDCall r=new SyncMPDCall(address,port,command,listener,this);
		jobs.add(r);
		executor.execute(r);
	}
	
	public void shutdown()
	{
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
		executor.shutdownNow();
		log.info("executor shut down");
	}

	@Override
	public void callDone(SyncMPDCall c) {
		jobs.remove(c);
	}
}
