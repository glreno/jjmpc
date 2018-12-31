package com.rfacad.jjmpc;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdSay extends CmdSh
{
	private static String ESPEAK="/usr/bin/espeak";
	private static String LANG="-ven-rp+f5";
	private static String SPEED="-s150";
	public CmdSay(String phrase) {
		super(false,ESPEAK,LANG,SPEED,phrase);
	}
}

