package com.rfacad.joystick;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class RSJDTest implements RidiculouslySimpleJoystickDriver.RSJDListener
{
	private RidiculouslySimpleJoystickDriver driver;
	public void button(short id,short prev,short value)
	{
		System.out.println("ID:"+Integer.toHexString(id)+" Prev:"+prev+" Value:"+value);
		if ( id==0x0109 ) {
			// That's the 'start' button
			// This will be called on the press (value==1)
			// but if you exit immediately, the release (value==0)
			// will still be in /dev/input/js0 buffer next time you start!
			// So run it on a thread. This will cause the driver to
			// stop on the NEXT event, which is the release.
			// That will spin off another copy of this thread, but that doesn't matter.
			new Thread() { public void run() {
			driver.shutdown();
			}}.start();
		}
	}

	public static void main(String [] args)
	{
		RSJDTest test=new RSJDTest();
		test.driver.run();
	}

	public RSJDTest()
	{
		driver=new RidiculouslySimpleJoystickDriver("/dev/input/js0");
		driver.setListener(this);
	}
}

