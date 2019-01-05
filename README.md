# jjmpc, joypad-controlled mp3 player
## What it is
Software for a raspberry pi or similar tiny, low-power unix machine. With a suitable controller, you get a stereo that has no display, and real hardware buttons, so it can be used in the dark. Playlists are defined by the filenames of the mp3 files, and their directory structure. No need for an iTunes knockoff, just use simple Unix file manipulation.
## System Requirements
* a low-power unix machine (this was designed for a Raspberry Pi)
* A standard 12-button USB joypad
* [MPD server software](https://www.musicpd.org/) to do the actual playing of the mp3 files
* (optional) external sound card, since the sound from a Raspberry Pi heaphone jack stinks
## Why?
Because I would rather spend a year writing software than buy a dozen mp3 players trying to find one I like. When I have insomnia, I like to listen to books on tape, or old comedy albums, etc. An mp3 player is great for that, but a smartphone or an iPod is **not**. You can't use a touchscreen without looking at it, which does not help me sleep. My old mp3 player (a Creative Zen Stone) has hardware buttons, and can be used by feel. Mostly. But it is getting old, and will not last forever.
## Main Features
* Hardware buttons
* No complicated software to load new mp3 files
* Can play a single track or a single playlist
* Single button to play next track
## Non-Features
* No display
* No battery
## More Features
* Can shut down the hardware cleanly
* Can announce playlist (directory) names
* Volume controls
* Playlist and Track navigation controls
* Protected buttons (must press two buttons simultaneously to do anything)
