#!/bin/sh
# Ridiculously simple /etc/init.d/jjmpcd script
# Makes ludicrous assumptions.
cd /home/pi/jjmpc
sudo --user=pi ./jjmpcd.sh > /var/log/jjmpc.log 2>&1 &
