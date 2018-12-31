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
		// Substitute variables into the command string
		String [] cmd=command;
		if ( state != null ) {
			cmd=new String[command.length];
			for(int i=0;i<command.length;i++) {
				cmd[i]=state.substitute(command[i]);
			} 
		} 
		try {
			Process p=Runtime.getRuntime().exec(cmd);
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

