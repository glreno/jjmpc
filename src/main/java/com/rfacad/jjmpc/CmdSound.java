package com.rfacad.jjmpc;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdSound extends CmdSh
{
	private static String APLAY="/usr/bin/aplay";

	public CmdSound(String filename) {
		super(false,APLAY,filename);
	}
}

