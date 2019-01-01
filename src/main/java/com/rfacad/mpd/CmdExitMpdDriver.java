package com.rfacad.mpd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rfacad.buttons.interfaces.BState;
import com.rfacad.buttons.ButtonCommand;


@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdExitMpdDriver implements ButtonCommand
{
	private static final Logger log = LogManager.getLogger(CmdExitMpdDriver.class);

	RidiculouslySimpleMPDClient mpdDriver;

	public CmdExitMpdDriver(RidiculouslySimpleMPDClient md)
	{
		mpdDriver=md;
	}
	public boolean button(BState state)
	{
		log.info("Shutting down.");
		if (mpdDriver!=null){
			log.debug("Shutting down MPD driver thread");
			 mpdDriver.shutdown();
		}
		return true;
	}
}

