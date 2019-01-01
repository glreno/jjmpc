package com.rfacad.mpd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rfacad.mpd.interfaces.RSMPDListener;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class SyncMPDCall implements Runnable
{
	public static interface MPDCallDoneListener {
		public void callDone(SyncMPDCall c);
	}
	
	private static final Logger log = LogManager.getLogger(SyncMPDCall.class);
	private boolean keepgoing=true;
	private RSMPDListener listener;
	private Socket socket=null;
	private PrintWriter out=null;
	private BufferedReader in=null;
	private String address;
	private int port;
	private String command;
	private MPDCallDoneListener parent;

	public SyncMPDCall(String address, int port, String command, RSMPDListener listener,MPDCallDoneListener parent) {
		this.address=address;
		this.port=port;
		this.command=command;
		this.listener=listener;
		this.parent=parent;
	}

	public void closeSocket()
	{
		if ( socket != null  && ! socket.isClosed() )
		{
			try {
				socket.close();
			}
			catch (IOException e) {
				log.warn("Exception closing socket");
			}
		}
		socket=null;
		in=null;
		out=null;
	}

	private void sendCommand() throws IOException
	{
		log.debug("Sending: {}",command);
		out.println(command);
		out.flush();
	}
	
	private void openSocket() throws IOException
	{
			socket=new Socket(address,port);
			out=new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			log.debug("Socket opened to {} : {}",address,port);
			// First line is a welcome message of some kind
			String s=in.readLine();
			log.debug(s);
	}

	public void shutdown()
	{
		log.debug("Sync call received shutdown");
		this.keepgoing=false;
		closeSocket();
	}

	public void run()
	{
		log.debug("MPD Client thread starting.");
		keepgoing=true;
		try
		{
				openSocket();
				sendCommand();
				mainloop();
		}
		catch (Exception e)
		{
			log.error("Uncaught exception in MPD client",e);
		}
		finally
		{
			closeSocket();
		}
		log.debug("MPD Client thread ending.");
		parent.callDone(this);
	}


	protected void mainloop()
	{
		// Listen for text on the socket
		// Read everything up to OK or ACK
		// Send to the listener
		int pauseForRetry=0;
		while (keepgoing)
		{
			if ( pauseForRetry > 0 )
			{
				try { Thread.sleep(1000);} catch (InterruptedException e) {
					log.trace("Sleep interrupted");
				}
			}
			if ( in == null )
			{
				pauseForRetry++;
				log.trace("wee paws");
			}
			else
			{
				List<String> response=new ArrayList<String>();
				String s;
				try
				{
					log.trace("Waiting...");
					while ( (s = in.readLine()) != null )
					{
						pauseForRetry=0;
						log.debug("Received: {}",s);
						if ( s.startsWith("OK")) {
							sendok(response);
							return;
						}
						else if ( s.startsWith("ACK")) {
							senderr(s,response);
							return;
						}
						else
						{
							response.add(s);
						}
					}
					// Got a null from readLine(), but no exception
					// After five seconds of pauses, close the socket
					pauseForRetry++;
					log.trace("wee paws {}",pauseForRetry);
					if ( pauseForRetry > 5 ) {
						log.debug("Waited long enough, closing socket");
						senderr("MPD timed out",response);
						return;
					}
				}
				catch (SocketException e) {
					log.warn("Caught socket exception {}",e.getMessage());
					if ("Socket closed".equals(e.getMessage())||"Connection reset".equals(e.getMessage())) {
						log.debug("Socket closed");
						response.add(e.getMessage());
						senderr("Closed socket - SocketException thrown:"+e.getMessage(),response);
						return;
					}
					else {
						log.warn("Socket Exception in MPD client",e);
						response.add(e.getMessage());
						senderr("SocketException thrown",response);
						return;
					}
				}
				catch (IOException e) {
					log.error("IO Exception in MPD client",e);
					response.add(e.getMessage());
					senderr("Exception thrown",response);
					return;
				}
				log.trace("Done waiting.");
			}
		}
	}

	private void sendok(List<String> response)
	{
		log.debug("Received OK. Args: {}",response);
		try{
			listener.ok(response);
		}
		catch (Exception e) {
			log.error("Exception thrown by MPD listener ok()",e);
		}
	}

	private void senderr(String code,List<String> response)
	{
		log.debug("Received Error. Args: {}",response);
		try{
			listener.not_ok(code,response);
		}
		catch (Exception e) {
			log.error("Exception thrown by MPD listener not_ok()",e);
		}
	}

}
