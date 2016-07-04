#!/usr/bin/env python

import subprocess
import argparse
import os
import shutil
import sys
import difflib
import datetime
import glob

tmp_tests = "tmp_tests/"
runtest = "../../sim/"
testspath = "../tests/torture/"+tmp_tests # from ./runtest.py pov
torture = "riscv-torture/"

timestamp = datetime.datetime.now().strftime("%Y-%m-%d-%H:%M").replace(' ', '')

def get_args():
    """
    Get command line arguments
    """
    parser = argparse.ArgumentParser(description="""
    Diff the signature files provided in path
                   """)
    parser.add_argument('-p', '--path', action='store', dest='path',
                        help='path for signature files', default=None)
    parser.add_argument('-n', '--nruns', action='store', dest='nruns',
                        help='number of runs/tests to be tested', default=None)
    return parser.parse_args()

if __name__ =='__main__':
    args = get_args()
    signatures_match = True
    subprocess.Popen(['./make_tests.sh', args.nruns], cwd=torture).wait()
    
    i = 0
    while i < args.nruns:
        if not (os.path.isfile(tmp_tests + 'test' + str(i) + '.S')):
            if i == 0:
                print('Test files not found at path: ' + tmp_tests + 'test' + str(i) + '.S')
            else:
                if signatures_match == True:
                    print('All tests signature files checked and matches')
                else:
                    print('Some signatures does not match, check the generated directories under: '+'testrun-'+timestamp)
            sys.exit()
        # try:
        # Run pysim
        subprocess.Popen(['./runtest.py', testspath+'test'+str(i)+'.S', '-s', 'pysim', '-sig', 'signature'+str(i)+'.log', '-t', 'trace'+str(i)+'.log'], cwd=runtest).wait()
        # Run icarus
        subprocess.Popen(['./runtest.py', testspath+'test'+str(i)+'.S', '-s', 'icarus', '-sig', 'signature'+str(i) + '.rtl.log', '-t' ,'trace'+str(i)+'.rtl.log', '--logging'], cwd=runtest).wait()
        # except
        f_py = open(tmp_tests+'signature' + str(i) + '.log', 'r')
        f_rtl = open(tmp_tests+'signature' + str(i) + '.rtl.log', 'r')

        # Check diff
        s = difflib.SequenceMatcher(None, f_py.read(), f_rtl.read())

        # Move files needed for debug if difference
        if s.ratio() != 1:
            signatures_match = False
            if not os.path.exists('testrun-'+timestamp):
                os.makedirs('testrun-'+timestamp)
            os.makedirs('testrun-'+timestamp+'/test'+str(i))
            shutil.copy(tmp_tests+'test'+str(i)+'.S', 'testrun-'+timestamp+'/test'+str(i))
            shutil.copy(tmp_tests+'signature'+str(i)+'.log', 'testrun-'+timestamp+'/test'+str(i))
            shutil.copy(tmp_tests+'signature'+str(i)+'.rtl.log', 'testrun-'+timestamp+'/test'+str(i))
            shutil.copy(tmp_tests+'trace'+str(i)+'.log', 'testrun-'+timestamp+'/test'+str(i))
            shutil.copy(tmp_tests+'trace'+str(i)+'.rtl.log', 'testrun-'+timestamp+'/test'+str(i))
            shutil.copy(tmp_tests+'tb_nanorv32.vcd', +'testrun-'+timestamp+'/test'+str(i))
            shutil.copy('diff_trace.py', 'testrun-'+timestamp+'/test'+str(i)) # Necessary?

        # Remove the temporary files, the files with a diff has been moved
        for filename in glob.glob(tmp_tests+'*[!0-9]'+str(i)+'[!0-9]*'):
            os.remove(filename)
            
        i += 1
