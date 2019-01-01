package com.rfacad.buttons;

import java.util.*;
import java.util.concurrent.*;

import com.rfacad.buttons.interfaces.BState;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class ButtonState implements BState
{
	private short id;
	private short prev;
	private short value;
	private Map<String,String> mapString;
	private Map<String,List<String>> mapStringList;

	public ButtonState(short id,short prev,short value) {
		this.id=id;
		this.prev=prev;
		this.value=value;
		this.mapString=new ConcurrentHashMap<>();
		this.mapStringList=new ConcurrentHashMap<>();
	}
	
	/*pkg*/ ButtonState(ButtonState o) {
		this.id=o.id;
		this.prev=o.prev;
		this.value=o.value;
		this.mapString=new ConcurrentHashMap<>(o.mapString);
		this.mapStringList=new ConcurrentHashMap<>(o.mapStringList);
	}

	public short getButtonId() { return id;}
	public short getPrevValue() { return prev;}
	public short getNewValue() { return value;}

	@Override
	public void setString(String key,String value) { mapString.put(key,value);}
	@Override
	public String getString(String key) { return mapString.get(key);}
	@Override
	public void setStringList(String key,List<String> value) { mapStringList.put(key,value);}
	@Override
	public List<String> getStringList(String key) { return mapStringList.get(key);}

	@Override
	public String substituteStrings(String s) {
		int pct1=s.indexOf('%');
		if ( pct1 >= 0 ) {
			int pct2=s.indexOf('%',pct1+1);
			if ( pct2 > 0 ) {
				// There is a substitution!
				StringBuilder ret=new StringBuilder();
				ret.append(s.substring(0,pct1));
				ret.append(lookup(s.substring(pct1+1,pct2)));
				ret.append(substituteStrings(s.substring(pct2+1)));
				return ret.toString();
			}
		}
		return s;
	}

	private String lookup(String s) {
		String ret="";
		if (s.length()==0) ret="%";
		else {
			Object o=getString(s);
			if ( o!=null ) ret=o.toString();
		}
		return ret;
	}
	
}
