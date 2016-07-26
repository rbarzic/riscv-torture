#!/usr/bin/env python3 

import subprocess
import argparse
import os
import shutil
import sys
import difflib
import datetime
import glob
import signal
from enum import Enum

tmp_tests = "tmp_tests/"
runtest = "../../sim/"
testspath = "../tests/torture/"+tmp_tests # from ./runtest.py pov
torture = "riscv-torture/"

class SimType(Enum):
    pysim = 1
    icarus = 2

timestamp = datetime.datetime.now().strftime("%Y-%m-%d-%H:%M").replace(' ', '')


def start_runtest(sim_type, index, timeout, args):
    cmd_pysim = ['./runtest.py', '-s', 'pysim', testspath+'test'+str(index)+'.S', '-sig', 'signature'+str(index)+'.log', '-t', 'trace'+str(index)+'.log']
    cmd_icarus = ['./runtest.py', '-s', 'icarus', '--logging', testspath+'test'+str(index)+'.S',  '-sig', 'signature'+str(index) + '.rtl.log', '-t' ,'trace'+str(index)+'.rtl.log']
    if sim_type == SimType.pysim:
        cmd = cmd_pysim
    else:
        cmd = cmd_icarus
    if args.rvc:
        cmd.append('--rvc')
        
    p_sim = subprocess.Popen(cmd, cwd=runtest, preexec_fn=os.setsid)
    try:
        p_sim.communicate(timeout)
    except subprocess.TimeoutExpired:
        os.killpg(os.getpgid(p_sim.pid), signal.SIGTERM) # Terminate process group spawned by runtest
        print('Process was killed because it used too long time to finish. \nPlease make sure all processes were properly killed.')
    

def get_args():
    """
    Get command line arguments
    """
    parser = argparse.ArgumentParser(description="""
    Diff the signature files provided in path
                   """)
    parser.add_argument('-p', '--path', action='store', dest='path',
                        help='path for signature files', default=None) # Not used
    parser.add_argument('-n', '--nruns', action='store', dest='nruns',
                        help='number of runs/tests to be tested', default='1')
    parser.add_argument('--rvc', action='store_true', dest='rvc',                        
                        help='enable rvc option in runtest', default=False) # Not used yet
    parser.add_argument('--clean', action='store_true', dest='clean',                    
                        help='cleans up tmp_tests directory', default=False)
    return parser.parse_args()

if __name__ =='__main__':
    args = get_args()
    signatures_match = True
    p_make = subprocess.Popen(['./make_tests.sh', args.nruns], cwd=torture).wait() # Need timeout on this?
    
    i = 0
    while i < int(args.nruns):
        if not (os.path.isfile(tmp_tests + 'test' + str(i) + '.S')):
            if i == 0:
                print('Test files not found at path: ' + tmp_tests + 'test' + str(i) + '.S')
            sys.exit()

        # Run pysim
        start_runtest(SimType.pysim, i, 10, args)

        # Run icarus
        start_runtest(SimType.icarus, i, 10, args)

        # Check diff in signatures
        f_py = open(tmp_tests+'signature' + str(i) + '.log', 'r')
        f_rtl = open(tmp_tests+'signature' + str(i) + '.rtl.log', 'r')

        s = difflib.SequenceMatcher(None, f_py.read(), f_rtl.read())

        f_py.close()
        f_rtl.close()
        
        # Move files needed for debug if difference
        if s.ratio() != 1:
            signatures_match = False
            print('Signatures on test' + str(i) + ' differs')
            if not os.path.exists('testrun-'+timestamp):
                os.makedirs('testrun-'+timestamp)
            os.makedirs('testrun-'+timestamp+'/test'+str(i))
            shutil.copy(tmp_tests+'test'+str(i)+'.S', 'testrun-'+timestamp+'/test'+str(i))
            shutil.copy(tmp_tests+'signature'+str(i)+'.log', 'testrun-'+timestamp+'/test'+str(i))
            shutil.copy(tmp_tests+'signature'+str(i)+'.rtl.log', 'testrun-'+timestamp+'/test'+str(i))
            shutil.copy(tmp_tests+'trace'+str(i)+'.log', 'testrun-'+timestamp+'/test'+str(i))
            shutil.copy(tmp_tests+'trace'+str(i)+'.rtl.log', 'testrun-'+timestamp+'/test'+str(i))
            shutil.copy(tmp_tests+'tb_nanorv32.vcd', 'testrun-'+timestamp+'/test'+str(i))
            shutil.copy('diff_trace.py', 'testrun-'+timestamp+'/test'+str(i)) # Necessary?

        if args.clean:
            # Remove the temporary files, the files with a diff has been copied
            for filename in glob.glob(tmp_tests+'*[!0-9]'+str(i)+'[!0-9]*'):
                os.remove(filename)
            
        i += 1

    # Finished
    if signatures_match == True:
        print('All tests signature files checked and matches')
    else:
        print('Some signatures does not match, check the generated directories under: '+'testrun-'+timestamp)

