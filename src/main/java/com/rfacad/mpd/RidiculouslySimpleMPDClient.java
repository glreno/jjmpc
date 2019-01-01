package com.rfacad.mpd;
import java.io.*;
import java.util.*;
import java.net.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class RidiculouslySimpleMPDClient implements Runnable
{
	private static final Logger log = LogManager.getLogger(RidiculouslySimpleMPDClient.class);

	public interface RSMPDListener
	{
		/** @param responses Response text, up to but not including 'OK'. Newlines removed. May be empty, but not null */
		public void ok(List<String> responses);
		/** Called on error.
		 * @param completioncode The 'ACK' text
		 * @param responses anything else
		 */
		public void not_ok(String completioncode,List<String> responses);
	}

	private boolean keepgoing=true;
	private boolean pauseForRetry=false;
	private RSMPDListener listener=null;
	private Socket socket=null;
	private PrintWriter out=null;
	private BufferedReader in=null;
	private String address;
	private int port;

	public RidiculouslySimpleMPDClient(String address, int port)
	{
		this.address=address;
		this.port=port;
	}

	public void setListener(RSMPDListener listener)
	{
		this.listener=listener;
	}

	public void closeSocket()
	{
		if ( socket != null  && ! socket.isClosed() )
		{
			try {
				socket.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		socket=null;
	}

	public void openSocket() throws IOException
	{
		socket=new Socket(address,port);
		out=new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
		//System.out.println("Socket opened to "+address+" port "+port);
		String s=in.readLine();
		//System.out.println(s);
	}


	public void shutdown()
	{
		closeSocket();
		this.keepgoing=false;
	}

	public void run()
	{
		log.info("MPD Client thread starting.");
		while(keepgoing)
		{
			if ( pauseForRetry )
			{
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					;
				}
			}
			try
			{
				mainloop();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		log.info("MPD Client thread ending.");
	}

	public void sendCommand(String command) throws IOException
	{
		if ( out == null )
		{
			closeSocket();
		}
		if ( socket == null )
		{
			openSocket();
		}
		if ( out == null )
		{
			throw new IOException("Unable to open socket");
		}
		//System.out.println("Sending:"+command);
		out.println(command);
		out.flush();
	}

	protected void mainloop()
	{
		// Listen for text on the socket
		// Read everything up to OK or ACK
		// Send to the listener
		if ( in == null )
		{
			pauseForRetry=true;
			//System.out.println("wee paws");
		}
		else
		{
			List<String> response=new ArrayList<String>();
			String s;
			try
			{
				//System.out.println("Waiting...");
				while ( (s = in.readLine()) != null )
				{
					//System.out.println(s);
					if ( s.startsWith("OK")) {
						sendok(response);
						response=new ArrayList<String>();
					}
					else if ( s.startsWith("ACK")) {
						senderr(s,response);
						response=new ArrayList<String>();
					}
					else
					{
						response.add(s);
					}
				}
			}
			catch (SocketException e) {
				if ("Socket closed".equals(e.getMessage())) {
					closeSocket();
					//System.out.println("Socket closed");
				}
				else {
				closeSocket();
				e.printStackTrace();
				response.add(e.getMessage());
				senderr("Exception thrown",response);
				}
			}
			catch (IOException e) {
				e.printStackTrace();
				response.add(e.getMessage());
				senderr("Exception thrown",response);
			}
			//System.out.println("Done waiting.");
		}
	}

	private void sendok(List<String> response)
	{
		try{
			listener.ok(response);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void senderr(String code,List<String> response)
	{
		try{
			listener.not_ok(code,response);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
