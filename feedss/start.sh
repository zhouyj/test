#!/bin/sh

nohup mvn spring-boot:run >> log/feedss.log >&1 &

echo $! > feedss_pid.file
