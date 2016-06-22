#!/usr/bin/env python
import sys
import difflib
import os.path
import argparse

def get_args():
    """
    Get command line arguments
    """
    parser = argparse.ArgumentParser(description="""
    Diff the signature files provided in path
                   """)
    parser.add_argument('-p', '--path', action='store', dest='path',
                        help='path for signature files', default=None)
    return parser.parse_args()

if __name__ == '__main__': 
    args = get_args()
    signature_match = True
    f_diffr = open(args.path + "diff_result.txt", 'w')

    idx = 0
    while True:
        if not (os.path.isfile(args.path + "signature" + str(idx) + ".log") and os.path.isfile(args.path + "signature" + str(idx) + ".rtl.log")):
            if idx == 0:
                print("Signature files not found at path: " + args.path)
            else:
                if signature_match == True:
                    print("All signature files checked and matches")
                else:
                    print("Some signatures does not match: See diff_result.txt")
            sys.exit()
        f_py = open(args.path + "signature" + str(idx) + ".log", 'r')
        f_rtl = open(args.path + "signature" + str(idx) + ".rtl.log", 'r')   
    
        s = difflib.SequenceMatcher(None, f_py.read(), f_rtl.read())
        if s.ratio() == 1:
            f_diffr.write("Files signature{}.log and signature{}.rtl.log => OK\n" .format(idx, idx))
        else:
            f_diffr.write("Files signature{}.log and signature{}.rtl.log => DIFFER\n" .format(idx, idx))
            signature_match = False
        
        idx += 1
