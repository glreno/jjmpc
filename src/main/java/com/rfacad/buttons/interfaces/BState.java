package com.rfacad.buttons.interfaces;

import java.util.List;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public interface BState 
{
	public static String SHIFT="SHIFTS"; // a Byte, stored as a String

	public void setString(String key,String value);
	public String getString(String key);
	public void setStringList(String key,List<String> value);
	public List<String> getStringList(String key);

	public String substituteStrings(String s);
}
