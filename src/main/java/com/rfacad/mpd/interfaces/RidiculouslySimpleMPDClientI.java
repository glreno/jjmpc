package com.rfacad.mpd.interfaces;

import java.io.IOException;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public interface RidiculouslySimpleMPDClientI
{
	public void sendCommand(String command, RSMPDListener listener) throws IOException;
}
