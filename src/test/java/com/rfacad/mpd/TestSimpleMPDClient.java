package com.rfacad.mpd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;

import com.rfacad.mpd.interfaces.RSMPDListener;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class TestSimpleMPDClient extends AbstractMPDClientTest {
	
	private RidiculouslySimpleMPDClient driver;
	private CountDownLatch clientThreadDone;
	private boolean clientThreadShutDown;
	
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
	}

	@Test
	public void testMockServer() throws UnknownHostException, IOException, InterruptedException
	{
		log.info("TEST START testMockServer()");
		startMockServer(new MockServerHandler() {
			@Override
			public void handle(BufferedReader in, PrintWriter out) {
				try {
					log.info("Handling request");
					String s=in.readLine();
					log.info("Request:"+s);
					StringBuilder buf=new StringBuilder();
					char c=s.charAt(0);
					++c;
					buf.append(c);
					log.info("Responding:"+buf.toString());
					out.println(buf.toString());
					out.flush();
				} catch (IOException e) {
					log.warn("caught exception",e);
				}
			}
		});
		log.info("Opening socket");
		Socket s = new Socket(LOCALHOST,port);
		OutputStream out = s.getOutputStream();
		BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		log.info("Writing 'A'");
		out.write("A\n".getBytes());
		out.flush();
		String welcome=in.readLine();
		log.info("Reading: "+welcome);
		assertEquals(WELCOME_TO_THE_MOCK_SERVER, welcome);
		String ret=in.readLine();
		log.info("Reading: "+ret);
		assertEquals("B",ret);
		s.close();
	}
	
	@Test
	public void shouldRespondToOK() throws IOException, InterruptedException
	{
		log.info("TEST START shouldRespondToOK()");
		final CountDownLatch latch=new CountDownLatch(1);
		final List<String> collectedResponses=new ArrayList<>();
		RSMPDListener listener=new RSMPDListener() {
			@Override
			public void ok(List<String> responses) {
				log.info("Got OK");
				collectedResponses.addAll(responses);
				latch.countDown();
			}
			
			@Override
			public void not_ok(String completioncode, List<String> responses) {
				log.info("Got NOT OK");
				fail("Got NOT OK");
				latch.countDown();
			}
		};
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
		driver.sendCommand("This is a test",listener);
		latch.await();
		assertNotNull(collectedResponses);
		assertEquals(2,collectedResponses.size());
		assertEquals("foo",collectedResponses.get(0));
		assertEquals("bar",collectedResponses.get(1));
		driver.shutdown();
		clientThreadDone.await(10,TimeUnit.SECONDS);
		assertTrue("Client thread failed to shut down",clientThreadShutDown);
	}

	@Test
	public void shouldRespondToNotOK() throws IOException, InterruptedException
	{
		log.info("TEST START shouldRespondToNotOK()");
		final CountDownLatch latch=new CountDownLatch(1);
		final List<String> collectedResponses=new ArrayList<>();
		RSMPDListener listener=new RSMPDListener() {
			@Override
			public void ok(List<String> responses) {
				log.info("Got OK");
				fail("Got OK");
				latch.countDown();
			}
			
			@Override
			public void not_ok(String completioncode, List<String> responses) {
				log.info("Got NOT OK");
				collectedResponses.addAll(responses);
				latch.countDown();
			}
		};
		startMockServer(new MockServerHandler() {
			@Override
			public void handle(BufferedReader in, PrintWriter out) {
				try {
					String s=in.readLine();
					log.info("Handling request "+s);
					assertEquals("This is a test",s);
					out.println("foo");
					out.println("bar");
					out.println("ACK");
					out.println();
					out.flush();
					log.info("Sent response");
				} catch (IOException e) {
					log.warn("caught exception",e);
				}
			}
		});
		log.info("Sending test command");
		driver.sendCommand("This is a test",listener);
		latch.await();
		assertNotNull(collectedResponses);
		assertEquals(2,collectedResponses.size());
		assertEquals("foo",collectedResponses.get(0));
		assertEquals("bar",collectedResponses.get(1));
		driver.shutdown();
		clientThreadDone.await(10,TimeUnit.SECONDS);
		assertTrue("Client thread failed to shut down",clientThreadShutDown);
	}


	@Test
	public void shouldNotHangOnError() throws InterruptedException, IOException
	{
		log.info("TEST START shouldNotHangOnError()");
		RSMPDListener listener=new RSMPDListener() {
			@Override
			public void ok(List<String> responses) {
				log.info("Got OK");
				fail("Got OK");
			}
			
			@Override
			public void not_ok(String completioncode, List<String> responses) {
				log.info("Got NOT OK");
			}
		};
		startMockServer(new MockServerHandler() {
			@Override
			public void handle(BufferedReader in, PrintWriter out) throws IOException {
				throw new SocketException("Socket closed"); // this string is magic
			}
		});
		log.info("Sending test command");
		driver.sendCommand("This is a test",listener);
		
		// While it's sleeping, the client will contact the driver,
		// fail, and exit. Unless something is horribly wrong,
		// the clientThread will exit before shutdown is called.
		log.info("Hang test sleeping");
		Thread.sleep(3000);
		log.info("Hang test sending shutdown");
		driver.shutdown();
		clientThreadDone.await(10,TimeUnit.SECONDS);
		assertTrue("Client thread failed to shut down",clientThreadShutDown);
		log.info("Hang test complete");
	}

	@Test
	public void shouldNotHangOnCannotConnect() throws InterruptedException, IOException
	{
		log.info("TEST START shouldNotHangOnError()");
		RSMPDListener listener=new RSMPDListener() {
			@Override
			public void ok(List<String> responses) {
				log.info("Got OK");
				fail("Got OK");
			}
			
			@Override
			public void not_ok(String completioncode, List<String> responses) {
				log.info("Got NOT OK");
			}
		};
		startMockServer(new MockServerHandler() {
			@Override
			public void handle(BufferedReader in, PrintWriter out) throws IOException {
				throw new SocketException("Socket closed"); // this string is magic
			}
		});
		log.info("Sending test command");
		driver.setPort(driver.getPort()-1);
		driver.sendCommand("This is a test",listener);
		
		// While it's sleeping, the client will contact the driver,
		// fail, and exit. Unless something is horribly wrong,
		// the clientThread will exit before shutdown is called.
		log.info("Hang test sleeping");
		Thread.sleep(10000);
		log.info("Hang test sending shutdown");
		driver.shutdown();
		clientThreadDone.await(10,TimeUnit.SECONDS);
		assertTrue("Client thread failed to shut down",clientThreadShutDown);
		log.info("Hang test complete");
	}

	
	@Test
	public void shouldNotHangOnClose() throws InterruptedException, IOException
	{
		log.info("TEST START shouldNotHangOnClose()");
		RSMPDListener listener=new RSMPDListener() {
			@Override
			public void ok(List<String> responses) {
				log.info("Got OK");
				fail("Got OK");
			}
			
			@Override
			public void not_ok(String completioncode, List<String> responses) {
				log.info("Got NOT OK");
			}
		};
		startMockServer(new MockServerHandler() {
			@Override
			public void handle(BufferedReader in, PrintWriter out) throws IOException {
				in.close();
				out.close();
			}
		});
		log.info("Sending test command");
		driver.sendCommand("This is a test",listener);
		
		// While it's sleeping, the client will contact the driver,
		// fail, and exit. Unless something is horribly wrong,
		// the clientThread will exit before shutdown is called.
		log.info("Hang test sleeping");
		Thread.sleep(10000);
		log.info("Hang test sending shutdown");
		driver.shutdown();
		clientThreadDone.await(10,TimeUnit.SECONDS);
		assertTrue("Client thread failed to shut down",clientThreadShutDown);
		log.info("Hang test complete");
	}

	
	@Test
	public void shouldNotHangOnShutdown() throws InterruptedException, IOException
	{
		log.info("TEST START shouldNotHangOnShutdown()");
		// What if shutdown is called when the driver is
		// waiting for I/O? InputStream.read() doesn't time out,
		// after all. The handler here will just wait forever.
		final CountDownLatch end=new CountDownLatch(1);
		final AtomicBoolean alldone=new AtomicBoolean(false);
		RSMPDListener listener=new RSMPDListener() {
			@Override
			public void ok(List<String> responses) {
				log.info("Got OK");
			}
			
			@Override
			public void not_ok(String completioncode, List<String> responses) {
				log.info("Got NOT OK");
			}
		};
		startMockServer(new MockServerHandler() {
			@Override
			public void handle(BufferedReader in, PrintWriter out) throws IOException {
				log.info("Hanging");
				try { Thread.sleep(20000); } // 20 seconds
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				log.info("Done hanging");
				alldone.set(true);
				end.countDown();
			}
		});
		log.info("Sending test command");
		driver.sendCommand("This is a test",listener);
		
		// While it's sleeping, the client will be trying to
		// read from the server, but the server is hung for 20 seconds.
		Thread.sleep(3000);
		
		// Shut down the client, it's still got seventeen seconds to go before it would get a response
		log.info("Hang test sending shutdown");
		driver.shutdown();
		log.info("shutdown sent");
		clientThreadDone.await(10,TimeUnit.SECONDS);
		log.info("done waiting for shutdown");
		assertTrue("Client thread failed to shut down",clientThreadShutDown);
		// Even if it took ten seconds (it shouldn't) that thread still has seven seconds of hang time.
		
		log.info("Hang test complete");
		assertFalse("Hanging thread ended prematurely",alldone.get());
		log.info("Test passed, waiting for hanging thread to end, expect seven to seventeen seconds, allowing 60");
		end.await(60,TimeUnit.SECONDS);
		assertTrue("Hanging thread failed to end, something horrible is wrong",alldone.get());
		log.info("Hanging thread ended");
		
	}

}
