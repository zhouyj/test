#!/bin/sh

source /etc/profile
svn up
sh -x stop.sh
sh -x start.sh
echo 'restart feedss over'
tail -f log/feedss.log
