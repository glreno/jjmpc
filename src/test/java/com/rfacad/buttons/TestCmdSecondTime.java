package com.rfacad.buttons;

import org.junit.Test;

import com.rfacad.buttons.mapper.ButtonMapper;
import com.rfacad.buttons.MockCmd;


@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class TestCmdSecondTime
{
	private static final short S0=0;
	private static final short S1=1;
	private static final String B0=ButtonMapper.NO_SHIFT;

	@Test
	public void shouldRunOnSecondCall()
	{
		ButtonMapper bm=new ButtonMapper();
		MockCmd c1=new MockCmd();
		MockCmd c2=new MockCmd();
		MockCmd err=new MockCmd();
		CmdSecondTime sec=new CmdSecondTime(c1);

		// c1 will be called the SECOND time sec is called (so, even numbered calls)
		// c2 will be called when sec does not call c1 (so, odd numbered calls)
		// sec returns false when it calls c1, so err will be called.
		bm.map(0,1,0,B0,sec,c2);
		bm.onError(err);
		
		// First press: call c2 (but not c1 or err)
		bm.button(S0,S1,S0);
		c1.assertCalls(0);
		c2.assertCalls(1);
		err.assertCalls(0);
		
		// Second press: call c1 and err (but not c2)
		bm.button(S0,S1,S0);
		c1.assertCalls(1);
		c2.assertCalls(1);
		err.assertCalls(1);

		// Third press: call c2 (but not c1 or err)
		bm.button(S0,S1,S0);
		c1.assertCalls(1);
		c2.assertCalls(2);
		err.assertCalls(1);
		
		// Fourth press: call c1 and err (but not c2)
		bm.button(S0,S1,S0);
		c1.assertCalls(2);
		c2.assertCalls(2);
		err.assertCalls(2);
	}

}
