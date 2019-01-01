package com.rfacad.buttons.mapper;

import java.util.*;

import com.rfacad.buttons.interfaces.BState;
import com.rfacad.buttons.ButtonCommand;
import com.rfacad.buttons.ButtonState;
import com.rfacad.joystick.interfaces.RSJDListener;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class ButtonMapper implements RSJDListener
{
	// Constants for generic button values.
	public static Integer ZERO=0;
	public static Integer NEGATIVE=0x10000;
	public static Integer POSITIVE=0x20000;
	public static Integer ANY=0x7777777;

	// Constants for comparing Shift flags
	public static String NO_SHIFT=""; // kind of obvious, really
	public static String AT_LEAST_ONE_SHIFT="AT LEAST ONE SHIFT";
	public static String ANY_SHIFT_STATE="ANY_SHIFT";

	// What to do when there is an error
	private ButtonCommand errHandler;

	// Map<buttonid, prevvaluemap>
	// prevvaluemap is SortedMap<Integer,nextvaluemap>
	// nextvaluemap is SortedMap<Integer,shiftmap>
	// shiftmap is Map<csv,List<ButtonCommand>
	// The value maps are sorted because they are tested in order:
	// real values, any-neg, any-pos, any

	private Map<Short,SortedMap<Integer,SortedMap<Integer,Map<String,List<ButtonCommand>>>>> mapping;


	// The shift commands are builtin, they need access here.
	// The shifting is an 'and'. If shift state is (binary) 10
	// and a command is mapped to 11, it will not pass.
	// So if you want a command to trip on 10, 01, and 11, you need to
	// register it three times, which is a bit of a pain.
	// Which is why there is a constant for 'any shift state'
	// and another for 'at least one shift'.
	private List<String> shifts=new ArrayList<>();
	
	class CmdShift implements ButtonCommand {
		String flag;
		CmdShift(String f) { flag=f;}
		public boolean button(BState state) {
			if ( ! shifts.contains(flag) )
			{
				shifts.add(flag);
			}
			state.setStringList(BState.SHIFT,shifts);
			return true;
		}
	}
	class CmdUnshift implements ButtonCommand {
		String flag;
		CmdUnshift(String f) { flag=f;}
		public boolean button(BState state) {
			shifts.remove(flag);
			state.setStringList(BState.SHIFT,shifts);
			return true;
		}
	}

	public ButtonMapper()
	{
		mapping=new HashMap<>();
		errHandler=null;
	}

	public void button(short id,short prev,short value)
	{
		Integer iprev=(int)prev;
		Integer inext=(int)value;
		SortedMap<Integer,SortedMap<Integer,Map<String,List<ButtonCommand>>>> prevmap=mapping.get(id);
		if ( prevmap==null ) return;

		SortedMap<Integer,Map<String,List<ButtonCommand>>> nextmap=prevmap.get(iprev);
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

		Map<String,List<ButtonCommand>> shiftmap=nextmap.get(inext);
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

		String squash=squash(shifts);
		// Prefer an exact match. "No Shift" is an exact match of an empty string.
		List<ButtonCommand> commands=shiftmap.get(squash);
		if (commands==null && ! shifts.isEmpty() ) {
			// Next priority is the "at least one"
			commands=shiftmap.get(AT_LEAST_ONE_SHIFT);
		}
		if (commands==null) {
			// Last priority is "any".
			commands=shiftmap.get(ANY_SHIFT_STATE);
		}
		
		// Execute the commands, if there are any
		if ( commands != null )
		{
			BState state=new ButtonState(id,prev,value);
			state.setStringList(BState.SHIFT,shifts);
			execute(commands,state);
		}
	}

	public synchronized void map(int id,int prev,int value,String[] shiftstate,ButtonCommand ... cmd)
	{
		map(id,prev,value,squash(Arrays.asList(shiftstate)),Arrays.asList(cmd));
	}

	public synchronized void map(int id,int prev,int value,String[] shiftstate,List<ButtonCommand> commands)
	{
		map(id,prev,value,squash(Arrays.asList(shiftstate)),commands);
	}
	public synchronized void map(int id,int prev,int value,String shiftstate,ButtonCommand ... cmd)
	{
		map(id,prev,value,shiftstate,Arrays.asList(cmd));
	}
	public synchronized void map(int id,int prev,int value,String shiftstate,List<ButtonCommand> commands)
	{
		Short sid=(short)(id&0xffff);
		SortedMap<Integer,SortedMap<Integer,Map<String,List<ButtonCommand>>>> prevmap=mapping.get(sid);
		if ( prevmap==null ) {
			prevmap=new TreeMap<>();
			mapping.put(sid,prevmap);
		}
		SortedMap<Integer,Map<String,List<ButtonCommand>>> nextmap=prevmap.get(prev);
		if ( nextmap==null ) {
			nextmap=new TreeMap<>();
			prevmap.put(prev,nextmap);
		}
		Map<String,List<ButtonCommand>> shiftmap=nextmap.get(value);
		if ( shiftmap==null ) {
			shiftmap=new TreeMap<>();
			nextmap.put(value,shiftmap);
		}
		shiftmap.put(shiftstate,commands);
	}

	public void onError(ButtonCommand c) {
		errHandler=c;
	}

	protected void execute(List<ButtonCommand> commands,BState state)
	{
		for(ButtonCommand cmd:commands)
		{
			if ( cmd != null )
			{
				boolean ok = cmd.button(state);
				if ( ! ok )
				{
					if (errHandler!=null) {
						errHandler.button(state);
					}
					return;
				}
			}
		}
	}
	
	public static String squash(List<String > list)
	{
		if ( list==null )
		{
			return NO_SHIFT;
		}
		int nshifts=list.size();
		if ( nshifts==0 )
		{
			return NO_SHIFT;
		}
		else if ( nshifts==1 )
		{
			return list.get(0);
		}
		else
		{
			list.sort(null);
			StringBuilder buf=new StringBuilder();
			boolean first=true;
			for(String s : list )
			{
				if ( !first ) 
				{
					buf.append(","); 
				}
				else first=false;
				buf.append(s);
			}
			return buf.toString();
		}
	}

	public CmdShift mkCmdShift(String f) { return new CmdShift(f);}
	public CmdUnshift mkCmdUnshift(String f) { return new CmdUnshift(f);}
}

