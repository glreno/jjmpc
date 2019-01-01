package com.rfacad.mpd.playlistdb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rfacad.buttons.interfaces.BState;
import com.rfacad.mpd.RSMPDSyncCaller;
import com.rfacad.mpd.interfaces.PlaylistDBI;
import com.rfacad.mpd.interfaces.RidiculouslySimpleMPDClientI;

@com.rfacad.Copyright("Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0")
public class PlaylistDB implements PlaylistDBI {

	private static final Logger log = LogManager.getLogger(PlaylistDB.class);
	private RidiculouslySimpleMPDClientI mpdc;
	private String mostRecentlyLoadedPlaylist;

	public PlaylistDB(RidiculouslySimpleMPDClientI mpdc) {
		this.mpdc = mpdc;
		this.mostRecentlyLoadedPlaylist = null;
	}

	@Override
	public List<String> listPlaylistFolders(BState bs) {
		// Send a listfiles command to the MPD. The response will look like this:
		
		// directory: Monty Python
		// Last-Modified: 2018-07-01T19:03:45Z
		// file: X
		// size: 0
		// Last-Modified: 2018-07-04T23:06:15Z

		// for everything in the top-level directory

		log.debug("listPlaylistFolders");
		List<String> dirs=bs.getStringList(FOLDERS);
		if ( dirs != null )
		{
			// We have a cached set, so use it.
			// (cache is in the BState, so there is a new cache for every button press)
			log.debug("Found list in BState");
		}
		else
		{
	
			log.debug("Requesting list of top-level dirs from MPD");
			RSMPDSyncCaller caller=new RSMPDSyncCaller(mpdc);
			boolean b=caller.send("listfiles");
			if ( !b ) {
				log.error("MPD listfiles returned error {}",caller.getResponseCode());
				return null;
			}
			List<String> resp=caller.getResponse();
			if ( resp == null || resp.isEmpty() )
			{
				log.warn("MPD listfiles returned nothing");
				return null;
			}
			
			log.debug("Received dir listing from MPD with {} entries",resp.size());
			Set<String> found=new TreeSet<>();
			for(String line : resp)
			{
				String s=line.trim();
				if ( s.startsWith("directory:"))
				{
					String fn=s.substring(10).trim();
					boolean added=found.add(fn);
					if (added) log.debug("Found dir: {}",fn);
				}
			}
			
			if ( found.isEmpty() )
			{
				log.warn("MPD listfiles returned nothing");
				return null;
			}
			
			dirs=new ArrayList<String>(found);
			bs.setStringList(FOLDERS, new ArrayList<String>(dirs));
		}
		log.debug("Found {} folders",dirs.size());
		return dirs;
	}

	@Override
	public List<String> listPlaylists(BState bs, String folder) {
		log.debug("listPlaylists in folder {}",folder);
		String cachekey = PLAYLISTS_IN_FOLDER_PREFIX+folder;
		List<String> dirs=bs.getStringList(cachekey);
		if ( dirs!=null )
		{
			log.debug("Found list in BState");
		}
		else
		{
			log.debug("Requesting list of subdirs from MPD");
	
			RSMPDSyncCaller caller=new RSMPDSyncCaller(mpdc);
			boolean b=caller.send("listfiles \""+folder+"\"");
			if ( !b ) {
				log.error("MPD listfiles returned error {} for folder {}",caller.getResponseCode(),folder);
				return null;
			}
			List<String> resp=caller.getResponse();
			if ( resp == null || resp.isEmpty() )
			{
				log.warn("MPD listfiles returned nothing for folder {}",folder);
				return null;
			}
			
			log.debug("Received dir listing from MPD with {} entries",resp.size());
			Set<String> found=new TreeSet<>();
			for(String line : resp)
			{
				String s=line.trim();
				if ( s.startsWith("directory:"))
				{
					String playlistname=s.substring(10).trim();
					// playlist id is folder--dirname
					String playlistid=folder+SEP+playlistname;
					boolean added=found.add(playlistid);
					if (added) log.debug("Found playlist: {}",playlistid);
				}
			}
			
			if ( found.isEmpty() )
			{
				log.warn("MPD listfiles returned nothing for folder {}",folder);
				return null;
			}
			
			dirs=new ArrayList<String>(found);
			bs.setStringList(cachekey, new ArrayList<String>(dirs));
		}
		log.debug("Found {} playlists",dirs.size());
		return dirs;
	}

	@Override
	public List<String> listFiles(BState bs, String playlistid) {
		log.debug("listFiles in playlist {}",playlistid);
		String cachekey = FILES_IN_PLAYLIST_PREFIX+playlistid;
		List<String> files=bs.getStringList(cachekey);
		if ( files != null )
		{
			// We have a cached set, so use it.
			// (cache is in the BState, so there is a new cache for every button press)
			log.debug("Found list in BState");
		}
		else
		{
			int dash=playlistid.indexOf("--");
			if ( (dash < 1) || (dash==playlistid.length()-2) )
			{
				log.error("Invalid playlist id: {}",playlistid);
				return null;
			}
			String f=playlistid.substring(0, dash); // folder
			String p=playlistid.substring(dash+2); // playlist name
			log.debug("Folder: {} Playlist name: {}",f,p);
			
			log.debug("Requesting list of files from MPD");
			RSMPDSyncCaller caller=new RSMPDSyncCaller(mpdc);
			boolean b=caller.send("listfiles \""+f+"/"+p+"\"");
			if ( !b ) {
				log.error("MPD listfiles returned error {} for playlist {} / {}",caller.getResponseCode(),f,p);
				return null;
			}
			List<String> resp=caller.getResponse();
			if ( resp == null || resp.isEmpty() )
			{
				log.warn("MPD listfiles returned nothing for playlist {} / {}",f,p);
				return null;
			}
			
			log.debug("Received dir listing from MPD with {} entries",resp.size());
			Set<String> found=new TreeSet<>();
			for(String line : resp)
			{
				String s=line.trim();
				if ( s.startsWith("file:"))
				{
					String fn=s.substring(5).trim();
					
					// Reject all files except .mp3
					int dot=fn.lastIndexOf('.');
					if ( dot > 0 )
					{
						String ext=fn.substring(dot);
						if ( ".mp3".equalsIgnoreCase(ext) )
						{
							boolean added=found.add(fn);
							if (added) log.debug("Found file: {}",fn);
						}
					}
				}
			}
			
			if ( found.isEmpty() )
			{
				log.warn("MPD listfiles returned nothing for playlist {} / {}",f,p);
				return null;
			}

			files=new ArrayList<String>(found);
			bs.setStringList(cachekey, new ArrayList<String>(files));
		}
		log.debug("Found {} files",files.size());
		return files;
	
	}

	protected List<String> listM3U(BState bs, String playlistid) {
		log.debug("listFiles in playlist.m3u {}",playlistid);
		String cachekey = FILES_IN_M3U_PREFIX+playlistid;
		List<String> files=bs.getStringList(cachekey);
		if ( files != null )
		{
			// We have a cached set, so use it.
			// (cache is in the BState, so there is a new cache for every button press)
			log.debug("Found list in BState");
		}
		else
		{
			int dash=playlistid.indexOf("--");
			if ( (dash < 1) || (dash==playlistid.length()-2) )
			{
				log.error("Invalid playlist id: {}",playlistid);
				return null;
			}
			
			log.debug("Requesting list of files in m3u from MPD");
			RSMPDSyncCaller caller=new RSMPDSyncCaller(mpdc);
			boolean b=caller.send("listplaylistinfo \""+playlistid+"\"");
			if ( !b ) {
				log.error("MPD listplaylistinfo returned error {} for playlist {}",playlistid);
				return null;
			}
			List<String> resp=caller.getResponse();
			if ( resp == null || resp.isEmpty() )
			{
				log.warn("MPD listplaylistinfo returned nothing for playlist {}",playlistid);
				return null;
			}
			
			log.debug("Received m3u listing from MPD with {} entries",resp.size());
			Set<String> found=new TreeSet<>();
			for(String line : resp)
			{
				String s=line.trim();
				log.debug(s);
				if ( s.startsWith("file:"))
				{
					String fn=s.substring(5).trim();
					boolean added=found.add(fn);
					if (added) log.debug("Found file: {}",fn);
				}
			}
			
			if ( found.isEmpty() )
			{
				log.warn("MPD listplaylistinfo returned nothing for playlist {}",playlistid);
				return null;
			}

			files=new ArrayList<String>(found);
			bs.setStringList(cachekey, new ArrayList<String>(files));
		}
		log.debug("Found {} files",files.size());
		return files;
	
	}

	
	@Override
	public boolean loadPlaylist(BState bs,String playlistid)
	{
		log.debug("load playlist {}",playlistid);

		int dash=playlistid.indexOf("--");
		if ( (dash < 1) || (dash==playlistid.length()-2) )
		{
			log.error("Invalid playlist id: {}",playlistid);
			return false;
		}
		String f=playlistid.substring(0, dash); // folder
		String p=playlistid.substring(dash+2); // playlist name
		log.debug("Folder: {} Playlist name: {}",f,p);

		
		List<String> diskFileNames = listFiles(bs,playlistid);
		if ( diskFileNames==null || diskFileNames.isEmpty() )
		{
			log.error("No files in playlist.");
			return false;
		}
		int nfiles = diskFileNames.size();
		List<String> diskFilesFullPath=new ArrayList<>(nfiles);
		diskFileNames.forEach(fn->diskFilesFullPath.add(f+"/"+p+"/"+fn));

		boolean doBuildM3u=false;
		List<String> m3ufiles = listM3U(bs,playlistid);
		if ( m3ufiles==null || m3ufiles.isEmpty() )
		{
			log.debug("M3U file is empty, so must be built");
			doBuildM3u=true;
		}
		else
		{
			// Compare M3U to files on disk
			if ( nfiles != m3ufiles.size() )
			{
				log.debug("Size mismatch; rebuilding playlist");
				doBuildM3u=true;
			}
			else
			{
				for(int i=0;i<nfiles && !doBuildM3u;i++)
				{
					if ( !diskFilesFullPath.get(i).equals(m3ufiles.get(i)) )
					{
						doBuildM3u=true;

					}
				}
			}
		}
		
		if (doBuildM3u)
		{
			log.debug("Building playlist.");
			RSMPDSyncCaller caller=new RSMPDSyncCaller(mpdc);
			caller.send("update");
			caller.send("playlistclear \""+playlistid+"\"");
			
			for(String fn : diskFilesFullPath)
			{
				caller.send("playlistadd \""+playlistid+"\" \""+fn+"\"");
			}
		}

		// And load it.
		{
			log.debug("Issuing load command to MPD");
			RSMPDSyncCaller caller=new RSMPDSyncCaller(mpdc);
			caller.send("clear");
			boolean b=caller.send("load \""+playlistid+"\"");
			if ( !b ) {
				log.error("MPD load returned error {} for playlist {}",playlistid);
				return false;
			}
			log.debug("MPD load of {} succeeded",playlistid);
		}
		
		bs.setString(PLAYLIST_LOADED, playlistid);
		mostRecentlyLoadedPlaylist = playlistid;
		
		return true;
	}
	
	// Unit test only!!!
	public void setMostRecentPlaylist(BState bs,String playlistid)
	{
		bs.setString(PLAYLIST_LOADED, playlistid);
		mostRecentlyLoadedPlaylist = playlistid;
	}
	
	@Override
	public String getMostRecentPlaylist(BState bs)
	{
		if ( mostRecentlyLoadedPlaylist != null )
		{
			return mostRecentlyLoadedPlaylist;
		}
		
		log.debug("No playlist loaded since restart, checking MPD status");
		// So much for the easy way out.
		// Is there a current song being played/paused?
		String songid=bs.getString("songid");
		if ( songid != null )
		{
			log.debug("Current track is songid={}, getting playlistinfo",songid);
			
			// This SHOULD be in the currently loaded playlist, whatever it is
			RSMPDSyncCaller caller=new RSMPDSyncCaller(mpdc);
			boolean b=caller.send("playlistinfo");
			if ( !b ) {
				log.error("MPD playlistinfo returned error {}",caller.getResponseCode());
			}
			else
			{
				List<String> resp=caller.getResponse();
				if ( resp == null || resp.isEmpty() )
				{
					log.warn("MPD playlistinfo returned nothing");
				}
				else
				{
					String playlist=extractPlaylistNameFromPlaylistInfo(songid,resp);
					if ( playlist != null )
					{
						return playlist;
					}
				}
			}
		}
		
		// Give up. This is probably at startup, nothing is loaded.
		return null;
	}
	
	public String getFirstPlaylist(BState bs)
	{
		List<String> folders = listPlaylistFolders(bs);
		if ( folders!=null && !folders.isEmpty() )
		{
			List<String> playlists = listPlaylists(bs, folders.get(0));
			if ( playlists!=null && !playlists.isEmpty() )
			{
				return playlists.get(0);
			}
		}
		return null;
	}

	@Override
	public String getPrevPlaylist(BState bs)
	{
		String curr=getMostRecentPlaylist(bs);
		if ( curr==null )
		{
			return null;
		}

		// Get the list of playlists in this folder
		int sep=curr.indexOf(SEP);
		String folder=curr.substring(0,sep);
		List<String> playlistsInCurrFolder = listPlaylists(bs, folder);
		for(int i=1;i<playlistsInCurrFolder.size();i++)
		{
			if ( curr.equals(playlistsInCurrFolder.get(i)))
			{
				// found it, return the prev folder.
				return playlistsInCurrFolder.get(i-1);
			}
		}
		// curr is either the first playlist in the folder, or it's not there at all.
		// So go to prev folder.
		
		List<String> folders = listPlaylistFolders(bs);
		for(int i=1;i<folders.size();i++)
		{
			if ( folder.equals(folders.get(i)))
			{
				// found it. Return last playlist in the prev folder.
				List<String> playlistsInPrevFolder = listPlaylists(bs, folders.get(i-1));
				if ( playlistsInPrevFolder != null && !playlistsInPrevFolder.isEmpty() )
				{
					return playlistsInPrevFolder.get(playlistsInPrevFolder.size()-1);
				}
			}
		}
		// curr folder is the first one, or does not exist!
		return null;
	}
	
	@Override
	public String getPrevPlaylistFolderFirstPlaylist(BState bs) {
		String curr=getMostRecentPlaylist(bs);
		if ( curr==null )
		{
			return null;
		}

		// Identify the current folder
		int sep=curr.indexOf(SEP);
		String folder=curr.substring(0,sep);

		// Go to prev folder.
		
		List<String> folders = listPlaylistFolders(bs);
		for(int i=1;i<folders.size();i++)
		{
			if ( folder.equals(folders.get(i)))
			{
				// found it. Return first playlist in the prev folder.
				List<String> playlistsInPrevFolder = listPlaylists(bs, folders.get(i-1));
				if ( playlistsInPrevFolder != null && !playlistsInPrevFolder.isEmpty() )
				{
					return playlistsInPrevFolder.get(0);
				}
			}
		}

		// curr folder is the first one, or does not exist!
		return null;
	}

	@Override
	public String getNextPlaylist(BState bs)
	{
		String curr=getMostRecentPlaylist(bs);
		if ( curr==null )
		{
			return getFirstPlaylist(bs);
		}
		
		// Get the list of playlists in this folder
		int sep=curr.indexOf(SEP);
		String folder=curr.substring(0,sep);
		List<String> playlistsInCurrFolder = listPlaylists(bs, folder);
		for(int i=0;i<playlistsInCurrFolder.size()-1;i++)
		{
			if ( curr.equals(playlistsInCurrFolder.get(i)))
			{
				// found it, return the next folder.
				return playlistsInCurrFolder.get(i+1);
			}
		}
		// curr is either the last playlist in the folder, or it's not there at all.
		// So go to next folder.
		
		List<String> folders = listPlaylistFolders(bs);
		for(int i=0;i<folders.size()-1;i++)
		{
			if ( folder.equals(folders.get(i)))
			{
				// found it. Return first playlist in the next folder.
				List<String> playlistsInNextFolder = listPlaylists(bs, folders.get(i+1));
				if ( playlistsInNextFolder != null && !playlistsInNextFolder.isEmpty() )
				{
					return playlistsInNextFolder.get(0);
				}
			}
		}
		// curr folder is the last one, or does not exist!
		return getFirstPlaylist(bs);
	}


	@Override
	public String getNextPlaylistFolderFirstPlaylist(BState bs) {
		String curr=getMostRecentPlaylist(bs);
		if ( curr==null )
		{
			return getFirstPlaylist(bs);
		}
		
		// Identify the current folder
		int sep=curr.indexOf(SEP);
		String folder=curr.substring(0,sep);

		// Go to the next folder.
		
		List<String> folders = listPlaylistFolders(bs);
		for(int i=0;i<folders.size()-1;i++)
		{
			if ( folder.equals(folders.get(i)))
			{
				// found it. Return first playlist in the next folder.
				List<String> playlistsInNextFolder = listPlaylists(bs, folders.get(i+1));
				if ( playlistsInNextFolder != null && !playlistsInNextFolder.isEmpty() )
				{
					return playlistsInNextFolder.get(0);
				}
			}
		}
		// curr folder is the last one, or does not exist!
		return getFirstPlaylist(bs);
	}

	protected String extractPlaylistNameFromPlaylistInfo(String currId,List<String> info)
	{
		// There's lots of stuff in playlistinfo, but all I care about are these two lines:
		// file: Monty Python/Monty Python and The Holy Grail/Holy Grail 01.mp3
		// Id: 23
		// They occur in that order (with other stuff between them), so
		// look for a matching id, and use the previous file
		String filename=null;
		String lastfilename=null;
		for(Iterator<String> i=info.iterator();filename==null && i.hasNext();)
		{
			String s = i.next().trim();
			if ( s.startsWith("file:"))
			{
				lastfilename=s.substring(5).trim();
			}
			else if ( s.startsWith("Id:"))
			{
				String id = s.substring(3).trim();
				log.debug("ID:{} File:{}",id,lastfilename);
				if ( currId.equals(id))
				{
					filename=lastfilename;
				}
			}
		}
		
		if ( filename!=null )
		{
			log.debug("Extracting playlistid from filename {}",filename);
			int slash1=filename.indexOf("/");
			if ( (slash1 < 1) || (slash1==filename.length()-1) )
			{
				log.error("File is not in a playlist folder: {}",filename);
			}
			else
			{
				String f=filename.substring(0, slash1); // folder
				int slash2=filename.indexOf("/",slash1+1);
				if ( (slash2 < 1) || (slash2==filename.length()-1) )
				{
					log.error("File is not in a playlist: {}",filename);
				}
				else
				{
					String p=filename.substring(slash1+1,slash2);
					String found=f+SEP+p;
					log.debug("Extracted playlist id {}",found);
					return found;
				}
			}
		}

		return null;
	}
	
}
