#!/bin/bash

TESTRUNS=250
SIMPATH=~/olsk/nanorv32-ik/sim
OUTPUTFILE=../../../nanorv32-ik/tests/torture/test #Path and name
TESTFILESPATH=../tests/torture
TORTUREPATH=~/olsk/nanorv32imc-torture/riscv-torture

#Generate test
java -Xmx1G -Xss8M -XX:MaxPermSize=128M -jar sbt-launch.jar "generator/run -C config/nanorv32IM.config -o $OUTPUTFILE -n $TESTRUNS"

cd $SIMPATH
for ((i=0; i < $TESTRUNS; i++)) do 
	#Run on pysim
	./runtest.py $TESTFILESPATH/test$i.S -s pysim -sig signature$i.log -t trace$i.log 

	#Run on icarus
	./runtest.py $TESTFILESPATH/test$i.S -s icarus -sig signature$i.rtl.log -t trace$i.rtl.log 
done
cd $TORTUREPATH  
./diff_signature.py -p $SIMPATH/$TESTFILESPATH/


