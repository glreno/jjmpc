package com.rfacad.buttons;

import static org.junit.Assert.*;

import org.junit.Test;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class TestBState
{
	@Test
	public void shouldStoreButtonState() {
		BState state=new BState((short)1, (short)2, (short)3);
		assertEquals((short)1,state.getButtonId());
		assertEquals((short)2,state.getPrevValue());
		assertEquals((short)3,state.getNewValue());
	}
	
	@Test
	public void shouldUnescapePercents() {
		BState state=new BState((short)1, (short)2, (short)3);
		
		String test0="%";
		String s0 = state.substitute(test0);
		assertEquals("%",s0);

		String test1="%% This is a %% Test %%";
		String s1 = state.substitute(test1);
		assertEquals("% This is a % Test %",s1);
		
		String test2="100%";
		String s2 = state.substitute(test2);
		assertEquals("100%",s2);

		String test3="%5";
		String s3 = state.substitute(test3);
		assertEquals("%5",s3);

	}

	@Test
	public void shouldHandleUnsetValue() {
		BState state=new BState((short)1, (short)2, (short)3);
		String test="There is no %foo% value here";
		String s = state.substitute(test);
		assertEquals("There is no  value here",s);
	}

	@Test
	public void shouldSubstituteValues() {
		BState state=new BState((short)1, (short)2, (short)3);
		String test1="foo is %foo%, bar is %bar%.";
		state.set("foo", "xyzzy");
		state.set("bar", "quux");
		String s1 = state.substitute(test1);
		assertEquals("foo is xyzzy, bar is quux.",s1);
		
		// Now change the values
		state.set("foo", "ace");
		state.set("bar", "deuce");
		String s2 = state.substitute(test1);
		assertEquals("foo is ace, bar is deuce.",s2);
		
		// Check the ends
		String test3="%bar%%foo%";
		String s3 = state.substitute(test3);
		assertEquals("deuceace",s3);
	}

	
}
