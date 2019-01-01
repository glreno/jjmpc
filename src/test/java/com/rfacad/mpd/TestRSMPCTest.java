package com.rfacad.mpd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class TestRSMPCTest extends AbstractMPDClientTest {
	private static final Logger log = LogManager.getLogger(TestRSMPCTest.class);

	@Test
	public void shouldHandleOk() throws IOException, InterruptedException
	{
		final CountDownLatch sent=new CountDownLatch(1);
		startMockServer(new MockServerHandler() {
			@Override
			public void handle(BufferedReader in, PrintWriter out) {
				try {
					String s=in.readLine();
					log.info("Handling request "+s);
					//assertEquals("This is a test ",s);
					out.println("foo");
					out.println("bar");
					out.println("OK");
					out.println();
					out.flush();
					sent.countDown();
					log.info("Sent response");
				} catch (IOException e) {
					log.warn("caught exception",e);
				}
			}
		});
		String [] args = new String [] {
			"-h",LOCALHOST,"-p",Integer.toString(port),
			"This","is","a","test"
		};
		RSMPCTest.main(args);
		sent.await(10,TimeUnit.SECONDS);
	}
	
	@Test
	public void shouldHandleNotOk() throws IOException, InterruptedException
	{
		final CountDownLatch sent=new CountDownLatch(1);
		startMockServer(new MockServerHandler() {
			@Override
			public void handle(BufferedReader in, PrintWriter out) {
				try {
					String s=in.readLine();
					log.info("Handling request "+s);
					//assertEquals("This is a test ",s);
					out.println("foo");
					out.println("bar");
					out.println("ACK");
					out.println();
					out.flush();
					sent.countDown();
					log.info("Sent response");
				} catch (IOException e) {
					log.warn("caught exception",e);
				}
			}
		});
		String [] args = new String [] {
			"-h",LOCALHOST,"-p",Integer.toString(port),
			"This","is","a","test"
		};
		RSMPCTest.main(args);
		sent.await(10,TimeUnit.SECONDS);
	}

}
