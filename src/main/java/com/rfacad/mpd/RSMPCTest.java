package com.rfacad.mpd;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rfacad.mpd.interfaces.RSMPDListener;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class RSMPCTest implements RSMPDListener
{
	private static final Logger log = LogManager.getLogger(RSMPCTest.class);
	private RidiculouslySimpleMPDClient driver;

	public static void main(String [] args)
	{
		//new Thread( test.driver ).start();
		StringBuilder buf=new StringBuilder();
		int state=0;
		String host="localhost";
		String port="6600";
		for(String s: args)
		{
			if ("-p".equals(s)) {
				state=1;
			}
			else if ( "-h".equals(s) ) {
				state=2;
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
			System.out.println("Sending: "+buf.toString());
			test.driver.sendCommand(buf.toString(),test);
		}
		catch (Exception e) {
			e.printStackTrace();
			//test.driver.closeSocket();
			test.driver.shutdown();
		}
	}

	public RSMPCTest(String host,int port)
	{
		log.info("Connecting to {}:{}",host,port);
		driver=new RidiculouslySimpleMPDClient(host,port);
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

}

