package com.rfacad.jjmpc;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;


@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class MockCmd implements ButtonCommand
{
	boolean ret;
	List<BState> calls;
	
	public MockCmd() { this(true);}
	public MockCmd(boolean b)
	{
		ret=b;
		calls=new ArrayList<>(1);
	}
	public boolean button(BState state)
	{
		calls.add(new BState(state));
		return ret;
	}
	
	public List<BState> getCalls() {
		return calls;
	}
	
	public void assertCalls(int i) {
		assertEquals(i,calls.size());
	}
}

