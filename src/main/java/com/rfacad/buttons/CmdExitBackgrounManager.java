package com.rfacad.buttons;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rfacad.buttons.interfaces.BState;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")

public class CmdExitBackgrounManager implements ButtonCommand {
	private static final Logger log = LogManager.getLogger(CmdExitBackgrounManager.class);
	private BackgroundManager bm;
	
	public CmdExitBackgrounManager(BackgroundManager bm)
	{
		this.bm=bm;
	}
	
	@Override
	public boolean button(BState state)
	{
		log.info("Shutting down.");
		if ( bm!=null )
		{
			log.debug("Shutting down BackgroundManager thread");
			bm.shutdown();
		}
		return true;
	}
}
