package com.rfacad.buttons;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class BackgroundManager
{
	private static final Logger log = LogManager.getLogger(BackgroundManager.class);
	private ExecutorService executor;
	private boolean shuttingdown;
	
	public BackgroundManager()
	{
		this.executor=Executors.newCachedThreadPool();
		this.shuttingdown=false;
	}
	
	public void execute(Runnable r)
	{
		if ( ! shuttingdown )
		{
			executor.execute(r);
		}
	}
	
	public void shutdown()
	{
		shuttingdown=true;
		log.debug("Shutting down executor");
		// stop processing new jobs
		executor.shutdown();
		// interrupt any jobs that are still running
		log.debug("Final force of executor shutdown");
		executor.shutdownNow();
		log.info("executor shut down");
	}

}
