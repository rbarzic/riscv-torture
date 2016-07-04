#!/bin/bash

#TESTRUNS=5
OUTPUTFILE=../../tmp_tests/test #Path and name

#Generate test
java -Xmx1G -Xss8M -XX:MaxPermSize=128M -jar sbt-launch.jar "generator/run -C config/nanorv32IM.config -o $OUTPUTFILE -n $1"
