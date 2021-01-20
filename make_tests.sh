#!/bin/bash

#TESTRUNS=5
OUTPUTFILE=tmp_tests/test #Path and name
CONFIGFILE=config/nanorv32IM.config

mkdir -p output/$OUTPUTFILE
#Generate test
java -Xmx1G -Xss8M -XX:MaxPermSize=128M -jar sbt-launch.jar "generator/run -C $CONFIGFILE -o $OUTPUTFILE -n $1"
