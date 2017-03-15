#!/bin/sh

pid_file=feedss_pid.file

[ -f $pid_file ] && pid=$(cat $pid_file)
if [ -n "$pid" ]; then
  ps -p $pid|grep $pid >/dev/null
  if [ $? -eq 0 ]; then
    echo "kill process by pid match."
    kill $pid
    rm $pid_file
    exit
  fi
fi

echo "Can not find process(pid:$pid)."
