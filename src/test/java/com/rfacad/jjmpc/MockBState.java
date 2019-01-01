package com.rfacad.jjmpc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.rfacad.buttons.interfaces.BState;

public class MockBState implements BState {

	private Map<String,Object> map=new ConcurrentHashMap<>();
	
	@Override
	public void set(String key, Object value) {
		map.put(key, value);
	}

	@Override
	public Object get(String key) {
		return map.get(key);
	}

	@Override
	public String substitute(String s) {
		return s;
	}

}
