package com.rfacad.buttons.mapper;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.rfacad.buttons.interfaces.BState;
import com.rfacad.buttons.ButtonCommand;
import com.rfacad.buttons.ButtonState;
import com.rfacad.buttons.MockCmd;
import com.rfacad.buttons.mapper.ButtonMapper;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class TestButtonMapper
{
	private static final short S0=(short)0;
	private static final short S1=(short)1;
	private static final short S2=(short)2;
	private static final String B0=ButtonMapper.NO_SHIFT;
	private static final String B1="Button 1";
	private static final String B2="Button 2";
	private static final String [] B1_AND_B2 = new String[] { B1,B2};
	private static final String B4="button 4";
	
	@Test
	public void shouldDoNothing()
	{
		ButtonMapper bm=new ButtonMapper();
		bm.button(S0,S0,S0);
	}
	
	@Test
	public void shouldCallACommand()
	{
		ButtonMapper bm=new ButtonMapper();
		MockCmd c1=new MockCmd();
		bm.map(0,1,0,B0,c1);
		bm.button(S0,S1,S0);
		List<BState> calls1 = c1.getCalls();
		assertEquals(1,calls1.size());
		ButtonState state1=(ButtonState)calls1.get(0);
		assertEquals(S0,state1.getButtonId());
		assertEquals(S1,state1.getPrevValue());
		assertEquals(S0,state1.getNewValue());
		List<String> ss1=state1.getStringList(BState.SHIFT);
		assertNotNull(ss1);
		assertTrue(ss1.isEmpty());
	}
	
	@Test
	public void shouldCallErrorHandler()
	{
		ButtonMapper bm=new ButtonMapper();
		MockCmd c1=new MockCmd(false); // return false, indicating error
		MockCmd c2=new MockCmd();
		bm.map(0,1,0,B0,c1);
		bm.onError(c2);
		bm.button(S0,S1,S0);
		c1.assertCalls(1);
		c2.assertCalls(1);
	}

	@Test
	public void shouldNotCallNoErrorHandler()
	{
		ButtonMapper bm=new ButtonMapper();
		MockCmd c1=new MockCmd(false); // return false, indicating error
		bm.map(0,1,0,B0,c1);
		bm.onError(null);
		bm.button(S0,S1,S0);
		c1.assertCalls(1);
	}

	@Test
	public void shouldChangeErrorHandler()
	{
		ButtonMapper bm=new ButtonMapper();
		MockCmd c1=new MockCmd(false); // return false, indicating error
		MockCmd c2=new MockCmd();
		MockCmd c3=new MockCmd();
		ButtonCommand handlerChanger1=bm.mkCmdOnError(c3);
		ButtonCommand handlerChanger2=bm.mkCmdOnError(null);
		
		bm.map(0,1,0,B0,c1);
		bm.map(1,1,0,B0,handlerChanger1);
		bm.map(2,1,0,B0,handlerChanger2);
		
		// Initial state: call c2 on error
		bm.onError(c2);
		bm.button(S0,S1,S0);
		c1.assertCalls(1);
		c2.assertCalls(1);
		c3.assertCalls(0);
		
		// Change state: set new error handler
		bm.button(S1, S1, S0);
		
		// Trigger a second error, call c3
		bm.button(S0,S1,S0);
		c1.assertCalls(2);
		c2.assertCalls(1);
		c3.assertCalls(1);
		
		// Change state: set no error handler
		bm.button(S2, S1, S0);

		// Trigger a third error, call nothing
		bm.button(S0,S1,S0);
		c1.assertCalls(3);
		c2.assertCalls(1);
		c3.assertCalls(1);

	}

	
	@Test
	public void shouldMatchPrevValue()
	{
		// Test these prev value mappings: ANY NEGATIVE POSITIVE 1 -1 0
		// Note that each press finds BEST FIT, so ANY will never be called
		// since NEGATIVE and POSITIVE and ZERO are all defined.
		// The test presses will be: -2 -1 0 +1 +2
		
		ButtonMapper bm=new ButtonMapper();
		MockCmd c_any=new MockCmd();
		MockCmd c_neg=new MockCmd();
		MockCmd c_pos=new MockCmd();
		MockCmd c_m1=new MockCmd();
		MockCmd c_zero=new MockCmd();
		MockCmd c_p1=new MockCmd();
		c_any.assertCalls(  0);
		c_neg.assertCalls(  0);
		c_m1.assertCalls(   0);
		c_zero.assertCalls( 0);
		c_p1.assertCalls(   0);
		c_pos.assertCalls(  0);
		
		bm.map(0,ButtonMapper.ANY,0,B0,c_any);
		// ANY should work, since there is nothing else defined
		bm.button(S0,S0,S0);
		c_any.assertCalls(  1);
		c_neg.assertCalls(  0);
		c_m1.assertCalls(   0);
		c_zero.assertCalls( 0);
		c_p1.assertCalls(   0);
		c_pos.assertCalls(  0);
		
		bm.map(0,ButtonMapper.NEGATIVE,0,B0,c_neg);
		bm.map(0,ButtonMapper.POSITIVE,0,B0,c_pos);
		bm.map(0,(short)-1,0,B0,c_m1);
		bm.map(0,(short)0,0,B0,c_zero);
		bm.map(0,(short)1,0,B0,c_p1);
		
		// -2: should fire NEG
		bm.button(S0,(short)-2,S0);
		c_any.assertCalls(  1);
		c_neg.assertCalls(  1);
		c_m1.assertCalls(   0);
		c_zero.assertCalls( 0);
		c_p1.assertCalls(   0);
		c_pos.assertCalls(  0);

		// -1: should fire -1
		bm.button(S0,(short)-1,S0);
		c_any.assertCalls(  1);
		c_neg.assertCalls(  1);
		c_m1.assertCalls(   1);
		c_zero.assertCalls( 0);
		c_p1.assertCalls(   0);
		c_pos.assertCalls(  0);

		// 0: should fire 0
		bm.button(S0,(short)0,S0);
		c_any.assertCalls(  1);
		c_neg.assertCalls(  1);
		c_m1.assertCalls(   1);
		c_zero.assertCalls( 1);
		c_p1.assertCalls(   0);
		c_pos.assertCalls(  0);
		
		// 1: should fire 1
		bm.button(S0,(short)1,S0);
		c_any.assertCalls(  1);
		c_neg.assertCalls(  1);
		c_m1.assertCalls(   1);
		c_zero.assertCalls( 1);
		c_p1.assertCalls(   1);
		c_pos.assertCalls(  0);

		// 2: should fire POS
		bm.button(S0,(short)2,S0);
		c_any.assertCalls(  1);
		c_neg.assertCalls(  1);
		c_m1.assertCalls(   1);
		c_zero.assertCalls( 1);
		c_p1.assertCalls(   1);
		c_pos.assertCalls(  1);

	}

	@Test
	public void shouldMatchNewValue()
	{
		// Test these next value mappings: ANY NEGATIVE POSITIVE 1 -1 0
		// Note that each press finds BEST FIT, so ANY will never be called
		// since NEGATIVE and POSITIVE and ZERO are all defined.
		// The test presses will be: -2 -1 0 +1 +2
		
		ButtonMapper bm=new ButtonMapper();
		MockCmd c_any=new MockCmd();
		MockCmd c_neg=new MockCmd();
		MockCmd c_pos=new MockCmd();
		MockCmd c_m1=new MockCmd();
		MockCmd c_zero=new MockCmd();
		MockCmd c_p1=new MockCmd();
		c_any.assertCalls(  0);
		c_neg.assertCalls(  0);
		c_m1.assertCalls(   0);
		c_zero.assertCalls( 0);
		c_p1.assertCalls(   0);
		c_pos.assertCalls(  0);
		
		bm.map(0,0,ButtonMapper.ANY,B0,c_any);
		// ANY should work, since there is nothing else defined
		bm.button(S0,S0,S0);
		c_any.assertCalls(  1);
		c_neg.assertCalls(  0);
		c_m1.assertCalls(   0);
		c_zero.assertCalls( 0);
		c_p1.assertCalls(   0);
		c_pos.assertCalls(  0);
		
		bm.map(0,0,ButtonMapper.NEGATIVE,B0,c_neg);
		bm.map(0,0,ButtonMapper.POSITIVE,B0,c_pos);
		bm.map(0,0,(short)-1,B0,c_m1);
		bm.map(0,0,(short)0,B0,c_zero);
		bm.map(0,0,(short)1,B0,c_p1);
		
		// -2: should fire NEG
		bm.button(S0,S0,(short)-2);
		c_any.assertCalls(  1);
		c_neg.assertCalls(  1);
		c_m1.assertCalls(   0);
		c_zero.assertCalls( 0);
		c_p1.assertCalls(   0);
		c_pos.assertCalls(  0);

		// -1: should fire -1
		bm.button(S0,S0,(short)-1);
		c_any.assertCalls(  1);
		c_neg.assertCalls(  1);
		c_m1.assertCalls(   1);
		c_zero.assertCalls( 0);
		c_p1.assertCalls(   0);
		c_pos.assertCalls(  0);

		// 0: should fire 0
		bm.button(S0,S0,(short)0);
		c_any.assertCalls(  1);
		c_neg.assertCalls(  1);
		c_m1.assertCalls(   1);
		c_zero.assertCalls( 1);
		c_p1.assertCalls(   0);
		c_pos.assertCalls(  0);
		
		// 1: should fire 1
		bm.button(S0,S0,(short)1);
		c_any.assertCalls(  1);
		c_neg.assertCalls(  1);
		c_m1.assertCalls(   1);
		c_zero.assertCalls( 1);
		c_p1.assertCalls(   1);
		c_pos.assertCalls(  0);

		// 2: should fire POS
		bm.button(S0,S0,(short)2);
		c_any.assertCalls(  1);
		c_neg.assertCalls(  1);
		c_m1.assertCalls(   1);
		c_zero.assertCalls( 1);
		c_p1.assertCalls(   1);
		c_pos.assertCalls(  1);

	}


	@Test
	public void shouldMatchShiftState()
	{
		// Test these shift value mappings: ANY_SHIFT_STATE AT_LEAST_ONE_SHIFT 3 2 1 NO_SHIFT
		// (NO_SHIFT is zero. Shifts are a bitmap, so valid values are OR of 1,2,4 etc)
		// Note that each press finds BEST FIT, so ANY will never be called
		// after AT_LEAST_ONE_SHIFT is defined.
		// With three shifts, test all eight combinations.
		// (ANY will be tested before any other tests are mapped)
		// 000 => NO_SHIFT
		// 001 => 1
		// 010 => 2
		// 011 => 3
		// 100 through 111 => AT_LEAST_ONE
		
		// Shifts are set by specialized commands, so each of the tests
		// will be triggered by a specified button press, which sets some shifts,
		// and then button 100 to test, then button 101 to clear all shifts.
		
		ButtonMapper bm=new ButtonMapper();
		MockCmd c_n=new MockCmd(); // any
		MockCmd c_a=new MockCmd(); // all
		MockCmd c_3=new MockCmd();
		MockCmd c_2=new MockCmd();
		MockCmd c_1=new MockCmd();
		MockCmd c_0=new MockCmd();
		ButtonCommand s1=bm.mkCmdShift(B1);
		ButtonCommand s2=bm.mkCmdShift(B2);
		ButtonCommand s4=bm.mkCmdShift(B4);
		c_n.assertCalls( 0);
		c_a.assertCalls( 0);
		c_3.assertCalls( 0);
		c_2.assertCalls( 0);
		c_1.assertCalls( 0);
		c_0.assertCalls( 0);
		
		// These are the button presses that set the shifts
		// They only work in NO_SHIFT_STATE!
		bm.map(1,1,0,B0,      s1);
		bm.map(2,1,0,B0,   s2);
		bm.map(3,1,0,B0,   s2,s1);
		bm.map(4,1,0,B0,s4);
		bm.map(5,1,0,B0,s4,   s1);
		bm.map(6,1,0,B0,s4,s2);
		bm.map(7,1,0,B0,s4,s2,s1);
		
		// This one clears the shifts
		bm.map(101,1,0,ButtonMapper.ANY_SHIFT_STATE,bm.mkCmdUnshift(B1),bm.mkCmdUnshift(B2),bm.mkCmdUnshift(B4));
		
		bm.map(100,1,0,ButtonMapper.ANY_SHIFT_STATE,c_n);
		bm.button((short)100,S1,S0);
		c_n.assertCalls( 1);
		c_a.assertCalls( 0);
		c_3.assertCalls( 0);
		c_2.assertCalls( 0);
		c_1.assertCalls( 0);
		c_0.assertCalls( 0);
		// Now shift and then press a button
		bm.button((short)101,S1,S0);
		bm.button((short)3,S1,S0);
		bm.button((short)100,S1,S0);
		c_n.assertCalls( 2);
		c_a.assertCalls( 0);
		c_3.assertCalls( 0);
		c_2.assertCalls( 0);
		c_1.assertCalls( 0);
		c_0.assertCalls( 0);
		
		// Any works, so register the other calls
		bm.map(100,1,0,ButtonMapper.AT_LEAST_ONE_SHIFT,c_a);
		bm.map(100,1,0,B1_AND_B2,c_3);
		bm.map(100,1,0,B2,c_2);
		bm.map(100,1,0,B1,c_1);
		bm.map(100,1,0,ButtonMapper.NO_SHIFT,c_0);

		// And now the real tests.

		// 0 0 0 -> c_0
		bm.button((short)101,S1,S0);
		bm.button((short)100,S1,S0);
		c_n.assertCalls( 2);
		c_a.assertCalls( 0);
		c_3.assertCalls( 0);
		c_2.assertCalls( 0);
		c_1.assertCalls( 0);
		c_0.assertCalls( 1);

		// 0 0 1 -> c_1
		bm.button((short)101,S1,S0);
		bm.button((short)1,S1,S0);
		bm.button((short)100,S1,S0);
		c_n.assertCalls( 2);
		c_a.assertCalls( 0);
		c_3.assertCalls( 0);
		c_2.assertCalls( 0);
		c_1.assertCalls( 1);
		c_0.assertCalls( 1);

		// 0 1 0 -> c_2
		bm.button((short)101,S1,S0);
		bm.button((short)2,S1,S0);
		bm.button((short)100,S1,S0);
		c_n.assertCalls( 2);
		c_a.assertCalls( 0);
		c_3.assertCalls( 0);
		c_2.assertCalls( 1);
		c_1.assertCalls( 1);
		c_0.assertCalls( 1);

		// 0 1 1 -> c_3
		bm.button((short)101,S1,S0);
		bm.button((short)3,S1,S0);
		bm.button((short)100,S1,S0);
		c_n.assertCalls( 2);
		c_a.assertCalls( 0);
		c_3.assertCalls( 1);
		c_2.assertCalls( 1);
		c_1.assertCalls( 1);
		c_0.assertCalls( 1);

		// 1 0 0 through 111 -> c_a (at least one)
		int k=0;
		for(int i=4;i<8;i++)
		{
			++k;
			bm.button((short)101,S1,S0);
			bm.button((short)i,S1,S0);
			bm.button((short)100,S1,S0);
			c_n.assertCalls( 2);
			c_a.assertCalls( k);
			c_3.assertCalls( 1);
			c_2.assertCalls( 1);
			c_1.assertCalls( 1);
			c_0.assertCalls( 1);
		}
	}
	
}
