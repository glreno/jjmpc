package com.rfacad.jjmpc;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.rfacad.mpd.interfaces.RSMPDListener;
import com.rfacad.mpd.interfaces.RidiculouslySimpleMPDClientI;

/**
 * A very simple mock mpd client.
 * Commands permitted:
 * <ol>
 * <li>status
 * <li>single
 * <li>setvol
 * <li>play
 * <li>pause
 * </ol>
 * State is reported to the status, all other commands change state in some way.
 * State is also accessible directly from this class.
 * the ok() and not_ok() calls to the listener are called on another thread after a 100ms delay.
 */
@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class MockMpdClient implements RidiculouslySimpleMPDClientI {
	
	private Executor ex=Executors.newSingleThreadExecutor();
	
	private Map<String,String> stats = new ConcurrentHashMap<>();
	
	private Map<String,List<String>> okResponses = new ConcurrentHashMap<>();
	private Map<String,List<String>> failResponses = new ConcurrentHashMap<>();
	
	private List<String> commandsSent = new LinkedList<>();

	public void setStat(String key,String value)
	{
		stats.put(key, value);
	}
	
	public String getStat(String key)
	{
		return stats.get(key);
	}
	
	public void expectOkRequest(String request,List<String> response)
	{
		okResponses.put(request, new ArrayList<>(response));
	}
	public void expectOkRequest(String request)
	{
		okResponses.put(request, Collections.emptyList());
	}
	public void removeExpectedOkRequest(String request)
	{
		okResponses.remove(request);
	}

	public void expectFailRequest(String request,String ack,List<String> response)
	{
		List<String> resp=new ArrayList<>();
		resp.add(ack);
		resp.addAll(response);
		failResponses.put(request, resp);
	}
	public void removeExpectedFailRequest(String request)
	{
		failResponses.remove(request);
	}

	
	protected void send_ok(final RSMPDListener listener,final List<String> resp)
	{
		ex.execute(new Runnable() { public void run() {
			try { Thread.sleep(100); } catch (InterruptedException e) {}
			listener.ok(resp);
		}});
	}

	protected void send_not_ok(final RSMPDListener listener,final String ack,final List<String> resp)
	{
		ex.execute(new Runnable() { public void run() {
			try { Thread.sleep(100); } catch (InterruptedException e) {}
			listener.not_ok(ack, resp);
		}});
	}

	public List<String> getCommandsSent()
	{
		return new ArrayList<>(commandsSent);
	}
	
	public void clearCommandsSent()
	{
		commandsSent.clear();
	}
	
	@Override
	public void sendCommand(String command, RSMPDListener listener) throws IOException
	{
		if ( command != null ) commandsSent.add(command);
		
		List<String> resp=new ArrayList<>();
		if ( command==null )
		{
			resp.add("Null command");
			send_not_ok(listener,"ACK", resp);
			return;
		}
		else if ( "status".equals(command))
		{
			for(Map.Entry<String,String> e:stats.entrySet())
			{
				resp.add(e.getKey()+":"+e.getValue());
			}
		}
		else if ( "play".equals(command))
		{
			stats.put("state", "play");
		}
		else if ( "pause".equals(command))
		{
			stats.put("state", "pause");
		}
		else if ( command.startsWith("setvol"))
		{
			String n = command.substring(6).trim();
			stats.put("volume", n);
		}
		else if ( command.startsWith("single"))
		{
			String n = command.substring(6).trim();
			stats.put("single", n);
		}
		else if ( okResponses.containsKey(command))
		{
			resp.addAll(okResponses.get(command));
		}
		else if ( failResponses.containsKey(command))
		{
			List<String> response=new ArrayList<>(failResponses.get(command));
			String ack=response.remove(0);
			resp.addAll(response);
			send_not_ok(listener,ack, resp);
			return;
		}
		else
		{
			resp.add("Unknown command "+command);
			send_not_ok(listener,"ACK", resp);
			fail("Unexpected command "+command);
			return;
		}
		send_ok(listener,resp);
	}

}
