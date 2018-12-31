package com.rfacad.jjmpc;

import java.io.IOException;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdSh implements ButtonCommand
{
	private String [] command;
	private boolean waiter;
	public CmdSh(boolean wait,String ... cmd) { command=cmd;waiter=wait;}

	public boolean button(BState state)
	{
		try {
			Process p=Runtime.getRuntime().exec(command);
			while(waiter) {
				// TODO this needs  timeout mechanism
				try {
					int ret=p.waitFor();
					return (ret==0);
				}
				catch (InterruptedException e) {}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return !waiter;
	}
}

