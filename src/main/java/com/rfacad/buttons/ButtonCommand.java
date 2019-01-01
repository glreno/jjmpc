package com.rfacad.buttons;

import com.rfacad.buttons.interfaces.BState;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public interface ButtonCommand
{
	public boolean button(BState state);
}

