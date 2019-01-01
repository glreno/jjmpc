package com.rfacad.mpd;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rfacad.buttons.ButtonState;
import com.rfacad.buttons.interfaces.BState;
import com.rfacad.jjmpc.CmdMpdStatus;
import com.rfacad.mpd.interfaces.PlaylistDBI;
import com.rfacad.mpd.interfaces.RSMPDListener;
import com.rfacad.mpd.playlistdb.PlaylistDB;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class RSMPCTest implements RSMPDListener
{
	private static final Logger log = LogManager.getLogger(RSMPCTest.class);
	private RidiculouslySimpleMPDClient driver;
	private PlaylistDBI playlistdb;

	public static void main(String [] args)
	{
		//new Thread( test.driver ).start();
		StringBuilder buf=new StringBuilder();
		int state=0;
		String host="localhost";
		String port="6600";
		String cmd=null;
		for(String s: args)
		{
			if ("-p".equals(s)) {
				state=1;
			}
			else if ( "-h".equals(s) ) {
				state=2;
			}
			else if ( s.startsWith("-l") || "-getMostRecentPlaylist".equals(s))
			{
				cmd=s;
			}
			else if ( "--help".equals(s))
			{
				usage();
				return;
			}
			else {
				if (state==1) {
					port=s;
					state=0;
				}
				else if ( state==2) {
					host=s;
					state=0;
				}
				else {
					buf.append(s);
					buf.append(' ');
				}
			}
		}
		int portnum=Integer.parseInt(port);
		RSMPCTest test=new RSMPCTest(host,portnum);
			
		try
		{
			if ( cmd != null )
			{
				test.playlist(cmd,buf.toString().trim());
			}
			else
			{
				System.out.println("Sending: "+buf.toString());
				test.sendCommand(buf.toString());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			//test.driver.closeSocket();
			test.driver.shutdown();
		}
	}
	
	public static void usage() {
		System.err.println("RSMPCTest - RidiculouslySimpleMPDClient tester");
		System.err.println("");
		System.err.println("Usage:");
		System.err.println("\tmpctest.sh [-h host] [-p port] mpccmd [args...]");
		System.err.println("\tmpctest.sh [-h host] [-p port] -listPlaylistFolders");
		System.err.println("\tmpctest.sh [-h host] [-p port] -listPlaylists foldername");
		System.err.println("\tmpctest.sh [-h host] [-p port] -listFiles playlistid");
		System.err.println("\tmpctest.sh [-h host] [-p port] -loadPlaylist playlistid");
		System.err.println("\tmpctest.sh [-h host] [-p port] -getMostRecentPlaylist");
	}

	public RSMPCTest(String host,int port)
	{
		log.info("Connecting to {}:{}",host,port);
		driver=new RidiculouslySimpleMPDClient(host,port);
		playlistdb=new PlaylistDB(driver);
	}
	
	public void sendCommand(String cmdbuf)
	{
		driver.sendCommand(cmdbuf, this);
	}

	public void ok(List<String> response)
	{
		System.out.println("OK!");
		for(String s : response ) System.out.println("\t"+s);
		driver.shutdown();
	}

	public void not_ok(String code,List<String> response)
	{
		System.out.println("not ok");
		System.out.println(code);
		for(String s : response ) System.out.println("\t"+s);
		driver.shutdown();
	}

	public void playlist(String cmd, String arg) {
		List<String> ret=null;
		short S0=(short)0;
		BState bs=new ButtonState(S0, S0, S0);
		log.info("Command: {} {}", cmd, arg);
		switch(cmd) {
			case "-listPlaylistFolders":
				ret = playlistdb.listPlaylistFolders(bs);
				break;
			case "-listPlaylists":
				ret = playlistdb.listPlaylists(bs,arg);
				break;
			case "-listFiles":
				ret = playlistdb.listFiles(bs,arg);
				break;
			case "-loadPlaylist":
				boolean ok = playlistdb.loadPlaylist(bs,arg);
				if ( ok ) ret=Collections.singletonList("ok");
				else ret=Collections.singletonList("failed");
				break;
			case "-getMostRecentPlaylist":
				new CmdMpdStatus(driver).button(bs);
				String s=playlistdb.getMostRecentPlaylist(bs);
				ret=Collections.singletonList(s);
				break;
			default:
				log.error("Unknown command: {}",cmd);
		}
		if ( ret!=null )
		{
			for(String s : ret )
			{
				log.info(s);
			}
		}
		driver.shutdown();
	}


}

