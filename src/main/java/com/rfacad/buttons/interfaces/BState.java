package com.rfacad.buttons.interfaces;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public interface BState 
{
	public static String SHIFT="SHIFTS";

	public void set(String key,Object value);
	public Object get(String key);

	public String substitute(String s);
}
