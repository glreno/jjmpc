cd /etc/init.d
ln -s /home/pi/jjmpc/jjmpc_daemon.sh jjmpc
cd /etc/rc3.d
ln -s ../init.d/jjmpc S99jjmpc
cd /etc/rc4.d
ln -s ../init.d/jjmpc S99jjmpc
cd /etc/rc5.d
ln -s ../init.d/jjmpc S99jjmpc
