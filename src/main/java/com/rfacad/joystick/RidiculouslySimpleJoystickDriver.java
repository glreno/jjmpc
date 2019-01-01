package com.rfacad.joystick;

import java.io.*;
import java.util.Map;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rfacad.joystick.interfaces.RSJDListener;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class RidiculouslySimpleJoystickDriver
{
	private static final Logger log = LogManager.getLogger(RidiculouslySimpleJoystickDriver.class);

	protected String filename;
	protected boolean pauseForRetry;
	protected boolean keepgoing;
	protected RSJDListener listener;
	protected Map<Short,Short> cache;
	private static final Short S_0 = new Short((short)0);
	private static final Short S_1 = new Short((short)1);
	private Thread thr=null;
	private BufferedInputStream in=null;

	public RidiculouslySimpleJoystickDriver(String filename)
	{
		this.filename=filename;
		this.keepgoing=true;
		this.pauseForRetry=false;
	}

	public void setListener(RSJDListener listener)
	{
		this.listener=listener;
	}

	public void shutdown()
	{
		this.keepgoing=false;
		try
		{
			InputStream in2=in;
			in=null;
			if ( in2 != null )
			{
				in2.close();
			}
		}
		catch (IOException e)
		{
			;
		}
		Thread t=thr;
		thr=null;
		if ( t!=null ) {
			t.interrupt();
		}
	}
	
	public void spawn()
	{
		if ( thr==null )
		{
			thr=new Thread() {
				public void run() {
					innerRun();
				}
			};
			thr.start();
		}
	}
	
	protected void innerRun()
	{
		log.debug("Joystick thread starting.");
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
				log.error(e);
			}
		}
		log.info("Joystick thread ending.");
	}
	
	protected BufferedInputStream openStream() throws IOException
	{
		return new BufferedInputStream(new FileInputStream(filename));
	}

	protected void mainloop()
	{
		// Flush cache on re-open
		cache=null;
		
		byte [] buf=new byte[8];
		try
		{
			in=openStream();
		}
		catch(IOException e)
		{
			log.error("Error connecting to joystick "+filename+": "+e.getMessage(),e);
			pauseForRetry=true;
			return;
		}
		try
		{
			while (keepgoing)
			{
				int read=in.read(buf);
				if ( read == 8 )
				{
					// The first four bytes are a sequence number,
					// and I'm going to ignore it.

					if ( log.isTraceEnabled() )
					{
						StringBuilder s=new StringBuilder(26);
						s.append("Btn: ");
						for(int i=0;i<8;i++) {
							if ( (buf[i]&0xff) < 16 ) s.append('0');
							s.append(Integer.toHexString(buf[i]&0xff));
							if ( i%2 != 0 ) s.append(' ');
						}
						log.trace(s);
					}
					
					short id=mkshort(buf[7],buf[6]);
					short value=mkshort(buf[5],buf[4]);
					report(id,value);
				}
				else
				{
					log.error("Error reading joystick, got {} bytes",read);
					return;
				}
			}
		}
		catch (Exception e)
		{
			if ( !keepgoing )
			{
				log.error("Exception thrown by joystick driver",e);
			}
			return;
		}
		finally
		{
			try
			{
				InputStream in2=in;
				in=null;
				if ( in2 != null )
				{
					in2.close();
				}
			}
			catch (IOException e)
			{
				;
			}
		}
	}

	protected void report(short id,short value)
	{
		if ( log.isDebugEnabled() )
		{
			log.debug("Button pressed: {} value={}",()->hex(id),()->hex(value));
		}
		
		if ( cache == null )
		{
			cache=new HashMap<Short,Short>();
		}
		Short prev=cache.get(id);

		// To avoid storing every button in the cache,
		// assume that if the button is NOT in the cache,
		// then the prev value is 0.
		// Unless the new value is 0, in which case assume
		// that the prev value is one.
		// Only store the new value if there is a prev value,
		// or the new value is not 1 or 0.
		//
		// Note that there is a bad assumption there:
		// if an item starts with 1 and then not-zero,
		// the 1 will not appear as a previous. (the not-zero press
		// will assume that the previous value is zero)
		// If this is a problem for any particular button,
		// then you just need to pre-load the cache.
		// Not that there is a way to do that.
		boolean doCacheIt=false;
		if ( prev==null ) {
			if ( value == 0 )
			{
				prev=S_1;
			}
			else if ( value == 1 )
			{
				prev=S_0;
			}
			else
			{
				prev=S_0;
				doCacheIt = true;
			}
		}
		else {
			doCacheIt=true;
		}
		short prevd=prev; // guaranteed not null

		if ( log.isDebugEnabled() )
		{
			log.debug("Button: {} prev={} value={}",()->hex(id),()->hex(prevd),()->hex(value));
		}
		if ( listener != null )
		{
			listener.button(id,prevd,value);
		}
		
		if ( doCacheIt )
		{
			cache.put(id,value);
		}
	}

	private static final short mkshort(byte hi,byte lo)
	{
		return (short) ( ( (hi&0xff)<<8 ) | (lo&0xff) );
	}
	
	public static String hex(short i)
	{
		StringBuilder ret=new StringBuilder(4);
		ret.append(Integer.toHexString(i));
		while(ret.length()<4) ret.insert(0, '0');
		return ret.toString();
	}
	

}

