package com.rfacad.mpd.interfaces;

import java.util.List;

import com.rfacad.buttons.interfaces.BState;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public interface PlaylistDBI {
	static String SEP="--";
	
	// Keys for storing these things in the BState
	static String FOLDERS = "PlaylistFolderList";
	static String PLAYLISTS_IN_FOLDER_PREFIX = "PlaylistsInFolder:";
	static String FILES_IN_PLAYLIST_PREFIX = "FilesInPlaylist:";
	static String FILES_IN_M3U_PREFIX = "FilesInM3U:";
	static String PLAYLIST_LOADED = "PlaylistLoaded";
	
	/**
	 * @param bs
	 * @return sorted list of all top-level playlist folders, may be empty
	 */
	List<String> listPlaylistFolders(BState bs);

	/**
	 * @param bs
	 * @param folder name of playlist folder
	 * @return sorted list of playlist ids. Note that playlist ids include the folder name and playlist name, separated by '--'. Returns null if folder does not exist.
	 */
	List<String> listPlaylists(BState bs,String folder);
	
	/** @param playlist Note that playlist id INCLUDES the name of the folder and the playlist name, e.g. "Monty Python--Monty Python and the Holy Grail"
	 * @param playlistid
	 * @return sorted list of all files in the playlist. Returns null if dir does not exist.
	 */
	List<String> listFiles(BState bs,String playlistid);
	
	/**
	 * Clears the current playlist, and loads the specified playlist. May create/update m3u files.
	 * Checks db to see what files SHOULD be in the playlist. Stores loaded id in bstate,
	 * and in most-recently-loaded.
	 * @param bs
	 * @param playlistid playlist to load
	 * @return true if load succeeded
	 */
	boolean loadPlaylist(BState bs, String playlistid);
	
	/**
	 * @return ID of the most recently loaded playlist; if nothing loaded, then the ID of the first playlist.
	 */
	String getMostRecentPlaylist(BState bs);
}
