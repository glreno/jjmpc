package com.rfacad.jjmpc;

import java.util.*;
import java.util.concurrent.*;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class ButtonMapper implements RidiculouslySimpleJoystickDriver.RSJDListener
{
	public static Integer NEGATIVE=0x10000;
	public static Integer POSITIVE=0x20000;
	public static Integer ANY=0x7777777;

	public static short AT_LEAST_ONE_SHIFT=0x100;
	public static short ANY_SHIFT_STATE=0x200;

	public static String SHIFT="SHIFTS";

	// Map<buttonid, prevvaluemap>
	// prevvaluemap is SortedMap<Integer,nextvaluemap>
	// nextvaluemap is SortedMap<Integer,shiftmap>
	// shiftmap is Map<byte,List<ButtonCommand>
	// The value maps are sorted because they are tested in order:
	// real values, any-neg, any-pos, any

	Map<Short,SortedMap<Integer,SortedMap<Integer,SortedMap<Short,List<ButtonCommand>>>>> mapping;


	// The shift commands are builtin, they need access here.
	// The shifting is an 'and'. If shift state is (binary) 10
	// and a command is mapped to 11, it will not pass.
	// So if you want a command to trip on 10, 01, and 11, you need to
	// register it three times, which is a bit of a pain.
	// Which is why there is a constant for 'any shift state'
	// and another for 'at least one shift'.
	private byte shifts=0;
	class CmdShift implements ButtonCommand {
		byte flag;
		CmdShift(byte f) { flag=f;}
		public boolean button(BState state) {
			shifts |= flag;
			state.set(SHIFT,shifts);
			return true;
		}
	}
	class CmdUnshift implements ButtonCommand {
		byte flag;
		CmdUnshift(byte f) { flag=f;}
		public boolean button(BState state) {
			shifts &= ~flag;
			state.set(SHIFT,shifts);
			return true;
		}
	}

	public ButtonMapper()
	{
		mapping=new HashMap<>();
	}

	public void button(short id,short prev,short value)
	{
		Integer iprev=(int)prev;
		Integer inext=(int)value;
		SortedMap<Integer,SortedMap<Integer,SortedMap<Short,List<ButtonCommand>>>> prevmap=mapping.get(id);
		if ( prevmap==null ) return;

		SortedMap<Integer,SortedMap<Short,List<ButtonCommand>>> nextmap=prevmap.get(iprev);
		if ( nextmap==null && prev<0 ) {
			nextmap=prevmap.get(NEGATIVE);
		}
		if ( nextmap==null && prev>0 ) {
			nextmap=prevmap.get(POSITIVE);
		}
		if ( nextmap==null ) {
			nextmap=prevmap.get(ANY);
		}
		if ( nextmap==null ) return;

		SortedMap<Short,List<ButtonCommand>> shiftmap=nextmap.get(inext);
		if ( shiftmap==null && value<0 ) {
			shiftmap=nextmap.get(NEGATIVE);
		}
		if ( shiftmap==null && value>0 ) {
			shiftmap=nextmap.get(POSITIVE);
		}
		if ( shiftmap==null ) {
			shiftmap=nextmap.get(ANY);
		}
		if ( shiftmap==null ) return;

		List<ButtonCommand> commands=shiftmap.get((short)shifts);
		if (commands==null && shifts > 0 ) {
			commands=shiftmap.get(AT_LEAST_ONE_SHIFT);
		}
		if (commands==null) {
			commands=shiftmap.get(ANY_SHIFT_STATE);
		}
		if ( commands != null )
		{
			BState state=new BState(id,prev,value);
			state.set(SHIFT,shifts);
			execute(commands,state);
		}
	}

	public void map(int id,int prev,int value,short shiftstate,ButtonCommand cmd)
	{
		map(id,prev,value,shiftstate,Collections.singletonList(cmd));
	}

	public synchronized void map(int id,int prev,int value,short shiftstate,List<ButtonCommand> commands)
	{
		Short sid=(short)(id&0xffff);
		SortedMap<Integer,SortedMap<Integer,SortedMap<Short,List<ButtonCommand>>>> prevmap=mapping.get(sid);
		if ( prevmap==null ) {
			prevmap=new TreeMap<>();
			mapping.put(sid,prevmap);
		}
		SortedMap<Integer,SortedMap<Short,List<ButtonCommand>>> nextmap=prevmap.get(prev);
		if ( nextmap==null ) {
			nextmap=new TreeMap<>();
			prevmap.put(prev,nextmap);
		}
		SortedMap<Short,List<ButtonCommand>> shiftmap=nextmap.get(value);
		if ( shiftmap==null ) {
			shiftmap=new TreeMap<>();
			nextmap.put(value,shiftmap);
		}
		shiftmap.put(shiftstate,commands);
	}

	protected void execute(List<ButtonCommand> commands,BState state)
	{
		for(ButtonCommand cmd:commands)
		{
			if ( cmd != null )
			{
				cmd.button(state);
			}
		}
	}

	public CmdShift mkCmdShift(int f) { return new CmdShift((byte)f);}
	public CmdUnshift mkCmdUnshift(int f) { return new CmdUnshift((byte)f);}
}

