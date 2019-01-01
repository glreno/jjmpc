package com.rfacad.buttons;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class CmdSh implements ButtonCommand
{
	private static final Logger log = LogManager.getLogger(CmdSh.class);

	private String [] command;
	private String [] lastRunJob;
	private boolean waiter;
	private boolean testmode=false;
	
	public CmdSh(boolean wait,String ... cmd) {
		command=cmd;
		waiter=wait;
	}

	public boolean button(BState state)
	{
		if ( command==null ) {
			log.error("Null command sent to CmdSh");
			return false;
		}

		// Substitute variables into the command string
		String [] cmd=command;
		if ( state != null ) {
			cmd = substituteStateVars(state,cmd); 
		} 
		if (( cmd==null ) || (cmd.length==0 )) {
			log.error("Empty command sent to CmdSh");
			return false;
		}
		
		// log for future reference
		lastRunJob=cmd;
		if ( log.isDebugEnabled() )
		{
			StringBuilder b=new StringBuilder();
			b.append("Executing:");
			for(String s:cmd) {
				b.append(' ');
				b.append(s);
			}
			log.debug(b);
		}
		
		try {
			int ret = executeCommand(cmd);
			if ( ret != -1 )
			{
				// We waited for the command to execute.
				return (ret==0);
			}
			
		}
		catch (IOException e) {
			log.error("Problem executing script. "+cmd[0],e);
		}
		
		// Success if we weren't supposed to wait.
		// If we were supposed to wait, and we got here, something is wrong.
		// (possible the script returned -1, which is an error anyway)
		return !waiter;
	}
	
	public void setTestMode(boolean b) {
		testmode=b;
	}

	protected int executeCommand(String[] cmd) throws IOException {

		if ( testmode )
		{
			return 0;
		}
		
		Process p=Runtime.getRuntime().exec(cmd);
		
		// If this CmdSh is supposed to wait,
		// then wait until the command returns.
		// If we're not waiting, return a flag of -1.
		
		while(waiter) {
			// TODO this needs  timeout mechanism
			try {
				int ret=p.waitFor();
				return ret;
			}
			catch (InterruptedException e) {}
		}
		return -1;
	}

	private String[] substituteStateVars(BState state,String [] cmd) {
		String[] ret;
		ret=new String[cmd.length];
		for(int i=0;i<cmd.length;i++) {
			ret[i]=state.substitute(cmd[i]);
		}
		return ret;
	}

	public String[] getLastRunJob() {
		return lastRunJob;
	}

}

