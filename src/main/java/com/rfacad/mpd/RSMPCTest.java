package com.rfacad.mpd;

import java.util.*;
import java.io.IOException;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class RSMPCTest implements RidiculouslySimpleMPDClient.RSMPDListener
{
	private RidiculouslySimpleMPDClient driver;

	public static void main(String [] args)
	{
		RSMPCTest test=new RSMPCTest();
		new Thread( test.driver ).start();
		StringBuilder buf=new StringBuilder();
		for(String s: args)
		{
			buf.append(s);
			buf.append(' ');
		}
		try
		{
			System.out.println("Sending: "+buf.toString());
			test.driver.sendCommand(buf.toString());
		}
		catch (IOException e) {
			e.printStackTrace();
			test.driver.closeSocket();
			test.driver.shutdown();
		}
	}

	public RSMPCTest()
	{
		driver=new RidiculouslySimpleMPDClient("localhost",6600);
		driver.setListener(this);
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

