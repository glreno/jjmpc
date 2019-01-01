package com.rfacad.jjmpc;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.rfacad.buttons.interfaces.BState;

public class MockBState implements BState {

	private Map<String,String> mapString=new ConcurrentHashMap<>();
	private Map<String,List<String>> mapStringList=new ConcurrentHashMap<>();
	
	@Override
	public void setString(String key, String value) {
		mapString.put(key, value);
	}

	@Override
	public String getString(String key) {
		return mapString.get(key);
	}

	@Override
	public void setStringList(String key, List<String> value) {
		mapStringList.put(key, value);
	}

	@Override
	public List<String> getStringList(String key) {
		return mapStringList.get(key);
	}

	@Override
	public String substituteStrings(String s) {
		return s;
	}

}
