package com.rfacad.jjmpc;

import java.util.*;
import java.util.concurrent.*;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class BState 
{
	private short id;
	private short prev;
	private short value;
	private Map<String,Object> map;

	public BState(short id,short prev,short value) {
		this.id=id;
		this.prev=prev;
		this.value=value;
		this.map=new ConcurrentHashMap<>();
	}

	public short getButtonId() { return id;}
	public short getPrevValue() { return prev;}
	public short getNewValue() { return value;}

	public void set(String key,Object value) { map.put(key,value);}
	public Object get(String key) { return map.get(key);}
}
