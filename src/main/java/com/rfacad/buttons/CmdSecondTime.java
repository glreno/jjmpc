package com.rfacad.buttons;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rfacad.buttons.interfaces.BState;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")

/**
 * Runs a command the SECOND time it is called.
 * The first call returns true, the second call returns false.
 * It then resets, so the command gets run every other time this is called.
 * 
 *  This means that you can put it in a call sequence like:
 *  CmdSecondTime(new CmdSh("/sbin/shutdown")), CmdSay("Press again to shut down")
 *  and it won't say anything the second time.
 */
public class CmdSecondTime implements ButtonCommand
{
	private static final Logger log = LogManager.getLogger(CmdSecondTime.class);

	protected boolean doRunRun;
	protected ButtonCommand [] commands;
	
	public CmdSecondTime(ButtonCommand ... cmds) {
		doRunRun=false;
		commands = cmds;
	}

	@Override
	public boolean button(BState state) {
		if (doRunRun)
		{
			log.debug("Second call: Executing command.");
			boolean ret=true;
			for(ButtonCommand cmd : commands )
			{
				if(ret)
				{
					ret=cmd.button(state);
				}
			}
		}
		else
		{
			log.debug("First call.");
		}
		doRunRun = ! doRunRun;
		return doRunRun;
	}
}

