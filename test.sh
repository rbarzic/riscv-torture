#!/bin/bash

TESTRUNS=10
SIMPATH=~/olsk/nanorv32-ik/sim
TESTFILESPATH=../tests/imported/riscv-tests/isa/rv32ui
TORTUREPATH=~/olsk/nanorv32imc-torture/riscv-torture

#Generate test
java -Xmx1G -Xss8M -XX:MaxPermSize=128M -jar sbt-launch.jar "generator/run -C config/nanorv32IM.config -n $TESTRUNS"

cd $SIMPATH
for ((i=0; i < $TESTRUNS; i++)) do 
	#Run on pysim
	./runtest.py $TESTFILESPATH/test_00$i.S -s pysim -t trace_00$i.log

	#Run on icarus
	./runtest.py $TESTFILESPATH/test_00$i.S -s icarus -t trace_00$i.rtl.log
done
cd $TORTUREPATH
