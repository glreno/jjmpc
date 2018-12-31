package com.rfacad.jjmpc;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class JBMTest
{
	public static void main(String [] args)
	{
		RidiculouslySimpleJoystickDriver jdriver;
		jdriver=new RidiculouslySimpleJoystickDriver("/dev/input/js0");

		ButtonMapper jbm=new ButtonMapper();
		jdriver.setListener(jbm);

		CmdExit exit=new CmdExit(jdriver,null);
		CmdLog log=new CmdLog();

		jbm.map(0x0109,1,0,ButtonMapper.ANY_SHIFT_STATE,exit);
		jbm.map(0x0100,1,0,ButtonMapper.AT_LEAST_ONE_SHIFT,new CmdLog("X",true));
		jbm.map(0x0101,1,0,ButtonMapper.ANY_SHIFT_STATE,new CmdLog("A",true));
		jbm.map(0x0102,1,0,(short)3,new CmdLog("B",true));
		jbm.map(0x0103,1,0,ButtonMapper.AT_LEAST_ONE_SHIFT,new CmdLog("Y",true));

		// left
		jbm.map(0x104,0,1,ButtonMapper.ANY_SHIFT_STATE,jbm.mkCmdShift(1));
		jbm.map(0x104,1,0,ButtonMapper.ANY_SHIFT_STATE,jbm.mkCmdUnshift(1));
		// right
		jbm.map(0x105,0,1,ButtonMapper.ANY_SHIFT_STATE,jbm.mkCmdShift(2));
		jbm.map(0x105,1,0,ButtonMapper.ANY_SHIFT_STATE,jbm.mkCmdUnshift(2));

		jdriver.run();

		exit.button(new BState((short)0,(short)0,(short)0));
	}
}

