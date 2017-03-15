#!/bin/bash

_pgrep="/usr/bin/pgrep"
_process="mysql-stream"

result=`$_pgrep $_process`

if [ "${result}" = "" ] 
then
	echo `date` $_process' not exist.' >> watchdog.log

# 	/etc/init.d/srs start >> watchdog.log
	/data/soft/elasticsearch-jdbc/bin/mysql-stream.sh
	
	echo `date` $_process' restarted.' >> watchdog.log
else
	echo `date` $_process' exist' >> watchdog.log
fi

