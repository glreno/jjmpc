package com.rfacad.mpd.interfaces;

import java.util.List;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public interface RSMPDListener
{
	/** @param responses Response text, up to but not including 'OK'. Newlines removed. May be empty, but not null */
	public void ok(List<String> responses);
	/** Called on error.
	 * @param completioncode The 'ACK' text
	 * @param responses anything else
	 */
	public void not_ok(String completioncode,List<String> responses);
}
