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

tmp_tests = "tmp_tests/"
runtest = "../../sim/"
testspath = "../tests/torture/"+tmp_tests # from ./runtest.py pov
torture = "riscv-torture/"

cmd_pysim = ['./runtest.py', testspath+'test'+str(i)+'.S', '--rvc', '-s', 'pysim', '-sig', 'signature'+str(i)+'.log', '-t', 'trace'+str(i)+'.log']
cmd_icarus = ['./runtest.py', testspath+'test'+str(i)+'.S', '--rvc', '-s', 'icarus', '-sig', 'signature'+str(i) + '.rtl.log', '-t' ,'trace'+str(i)+'.rtl.log', '--logging']

timestamp = datetime.datetime.now().strftime("%Y-%m-%d-%H:%M").replace(' ', '')

def start_runtest(cmd, timeout):
    p_pysim = subprocess.Popen(cmd, cwd=runtest, preexec_fn=os.setsid)
    try:
        p_pysim.communicate(timeout)
    except subprocess.TimeoutExpired:
        os.killpg(os.getpgid(p_pysim.pid), signal.SIGTERM) # Terminate process group spawned by runtest
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
                        help='number of runs/tests to be tested', default=None)
    parser.add_argument('--rvc', action='store', dest='rvc',
                        help='enable rvc option in runtest', default=None) # Not used yet
    return parser.parse_args()

if __name__ =='__main__':
    args = get_args()
    signatures_match = True
    p_make = subprocess.Popen(['./make_tests.sh', args.nruns], cwd=torture).wait() # Need timeout on this?
    
    i = 0
    while i < int(args.nruns) + 1:
        if not (os.path.isfile(tmp_tests + 'test' + str(i) + '.S')):
            if i == 0:
                print('Test files not found at path: ' + tmp_tests + 'test' + str(i) + '.S')
            else:
                if signatures_match == True:
                    print('All tests signature files checked and matches')
                else:
                    print('Some signatures does not match, check the generated directories under: '+'testrun-'+timestamp)
            sys.exit()

        # Run pysim
#        start_runtest(cmd_pysim, 10)
        p_pysim = subprocess.Popen(['./runtest.py', testspath+'test'+str(i)+'.S', '--rvc', '-s', 'pysim', '-sig', 'signature'+str(i)+'.log', '-t', 'trace'+str(i)+'.log'], cwd=runtest, preexec_fn=os.setsid)
        try:
           p_pysim.communicate(timeout=10)
        except subprocess.TimeoutExpired:
           os.killpg(os.getpgid(p_pysim.pid), signal.SIGTERM) # Terminate process group spawned by runtest
           print('Process was killed because it used too long time to finish. \nPlease make sure all processes were properly killed.')

        # Run icarus
#        start_runtest(cmd_icarus, 10)
        p_icarus = subprocess.Popen(['./runtest.py', testspath+'test'+str(i)+'.S', '--rvc', '-s', 'icarus', '-sig', 'signature'+str(i) + '.rtl.log', '-t' ,'trace'+str(i)+'.rtl.log', '--logging'], cwd=runtest, preexec_fn=os.setsid)
        try:
            p_icarus.communicate(timeout=10)
        except subprocess.TimeoutExpired:
            os.killpg(os.getpgid(p_icarus.pid), signal.SIGTERM) # Terminate process group spawned by runtest
            print('Process was killed because it used too long time to finish. \nPlease make sure all processes were properly killed.')
            
        #try:
        f_py = open(tmp_tests+'signature' + str(i) + '.log', 'r')
        f_rtl = open(tmp_tests+'signature' + str(i) + '.rtl.log', 'r')
        #except IOerror as e:
        #    print(e)
        #    s = 0
        #else:
        # Check diff
        s = difflib.SequenceMatcher(None, f_py.read(), f_rtl.read())

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

        # Remove the temporary files, the files with a diff has been copied
#        for filename in glob.glob(tmp_tests+'*[!0-9]'+str(i)+'[!0-9]*'):
#            os.remove(filename)
            
        i += 1
