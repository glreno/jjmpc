package com.rfacad.mpd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class TestRSMPDSyncCaller extends AbstractMPDClientTest {
	
	private RidiculouslySimpleMPDClient driver;
	private CountDownLatch clientThreadDone;
	private boolean clientThreadShutDown;
	private RSMPDSyncCaller caller;
	
	@Before
	public void setup() throws IOException
	{
		clientThreadDone = new CountDownLatch(1);
		clientThreadShutDown=false;
		driver = new RidiculouslySimpleMPDClient(LOCALHOST, port);
		driver.setExecutor(Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(final Runnable r) {
				Thread driverThread = new Thread() {
					public void run() {
						r.run();
						clientThreadShutDown=true;
						clientThreadDone.countDown();
					}
				};
				return driverThread;
			}
		}));
		caller=new RSMPDSyncCaller(driver);
	}

	@Test
	public void shouldRespondToOK() throws IOException, InterruptedException
	{
		log.info("TEST START shouldRespondToOK()");

		startMockServer(new MockServerHandler() {
			@Override
			public void handle(BufferedReader in, PrintWriter out) {
				try {
					String s=in.readLine();
					log.info("Handling request "+s);
					assertEquals("This is a test",s);
					out.println("foo");
					out.println("bar");
					out.println("OK");
					out.println();
					out.flush();
					log.info("Sent response");
				} catch (IOException e) {
					log.warn("caught exception",e);
				}
			}
		});
		log.info("Sending test command");
		boolean b=caller.send("This is a test");
		assertTrue(b);
		final List<String> collectedResponses=caller.getResponse();
		assertNotNull(collectedResponses);
		assertEquals(2,collectedResponses.size());
		assertEquals("foo",collectedResponses.get(0));
		assertEquals("bar",collectedResponses.get(1));
		assertEquals("OK",caller.getResponseCode());
		driver.shutdown();
		clientThreadDone.await(10,TimeUnit.SECONDS);
		assertTrue("Client thread failed to shut down",clientThreadShutDown);
	}

	@Test
	public void shouldRespondToNotOK() throws IOException, InterruptedException
	{
		log.info("TEST START shouldRespondToNotOK()");
		startMockServer(new MockServerHandler() {
			@Override
			public void handle(BufferedReader in, PrintWriter out) {
				try {
					String s=in.readLine();
					log.info("Handling request "+s);
					assertEquals("This is a test",s);
					out.println("foo");
					out.println("bar");
					out.println("ACK up yours");
					out.println();
					out.flush();
					log.info("Sent response");
				} catch (IOException e) {
					log.warn("caught exception",e);
				}
			}
		});
		log.info("Sending test command");
		boolean b=caller.send("This is a test");
		assertFalse(b);
		final List<String> collectedResponses=caller.getResponse();
		assertNotNull(collectedResponses);
		assertEquals(2,collectedResponses.size());
		assertEquals("foo",collectedResponses.get(0));
		assertEquals("bar",collectedResponses.get(1));
		assertEquals("ACK up yours",caller.getResponseCode());
		driver.shutdown();
		clientThreadDone.await(10,TimeUnit.SECONDS);
		assertTrue("Client thread failed to shut down",clientThreadShutDown);
	}
}
