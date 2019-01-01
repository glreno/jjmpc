package com.rfacad.buttons;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rfacad.buttons.interfaces.BState;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")

/**
 * Runs a command sequence on a background thread.
 * Usually, the buttonmapper runs commands on the same thread that
 * handles the buttons, which can be a bit inconvenient.
 */
public class CmdBackground implements ButtonCommand
{
	private static final Logger log = LogManager.getLogger(CmdBackground.class);

	protected ButtonCommand [] commands;
	protected BackgroundManager bm;

	
	public CmdBackground(BackgroundManager bm,ButtonCommand ... cmds)
	{
		this.bm=bm;
		this.commands = cmds;
	}

	@Override
	public boolean button(final BState state) {
		bm.execute(new Runnable() {
			public void run() {
				doRunRun(state);
			}
		});
		return true;
	}
	
	protected void doRunRun(BState state)
	{
		for(ButtonCommand cmd : commands)
		{
			boolean ret = cmd.button(state);
			if ( !ret )
			{
				return;
			}
		}
	}
}

