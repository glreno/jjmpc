package com.rfacad.mpd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class AbstractMPDClientTest {
	
	protected static final Logger log = LogManager.getLogger(AbstractMPDClientTest.class);
	protected static final String WELCOME_TO_THE_MOCK_SERVER = "Welcome to the mock server";
	protected static final String LOCALHOST = "localhost";
	private ServerSocket mockServer;
	protected boolean letMockServerRun;
	protected int port=0;
	
	public static interface MockServerHandler {
		public void handle(BufferedReader in, PrintWriter out) throws IOException;
	}
	
	@Before
	public void setupMockServer() throws IOException
	{
		mockServer=new ServerSocket(0);
		port = mockServer.getLocalPort();
		log.info("Testing on port:"+port);
	}


	
	@After
	public void stopMockServer() throws IOException
	{
		letMockServerRun=false;
		if ( mockServer != null )
		{
			log.info("Test over, shutting down mock server");
			mockServer.close();
		}
	}
	
	protected void startMockServer(final MockServerHandler handler)
	{
		final CountDownLatch startupLatch=new CountDownLatch(1);
		letMockServerRun=true;
		new Thread() {
			@Override
			public void run() {
				Thread.yield();
				boolean frist=true;
				while ( letMockServerRun )
				{
					Socket s=null;
					OutputStream out;
					InputStream in;
					try
					{
						log.info("Opening mockServer socket");
						if (frist) {
							startupLatch.countDown();
							frist=false;
						}
						s=mockServer.accept();
						log.info("Accepted mockServer socket");
						out = s.getOutputStream();
						in = s.getInputStream();
						PrintWriter pout = new PrintWriter(out);
						log.info(WELCOME_TO_THE_MOCK_SERVER);
						pout.println(WELCOME_TO_THE_MOCK_SERVER);
						pout.flush();
						log.info("Starting handler");
						handler.handle(new BufferedReader(new InputStreamReader(in)),pout);
					}
					catch (IOException e) {
						if ( letMockServerRun )
						{
							log.warn("caught exception",e);
						}
					}
					finally {
						log.info("handler ended, closing socket");
						try
						{
							if ( s!=null ) s.close();
							s=null;
						}
						catch (IOException e) {}
					}
				}
			}
		}.start();
		try {
			startupLatch.await();
		} catch (InterruptedException e) {
			log.warn("caught exception",e);
		}
		log.info("Mock server started");
	}


}
