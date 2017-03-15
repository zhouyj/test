#!/bin/sh

svn up
mvn clean
mvn package -Dmaven.test.skip=true
docker build -t feedss/zplive .
sudo docker login -u 13810163257 -p tangjun49  hub.c.163.com

sudo docker tag feedss/zplive hub.c.163.com/tangjun/zplive
sudo docker push hub.c.163.com/tangjun/zplive

