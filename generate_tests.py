#!/usr/bin/env python

import subprocess
import argparse
import os
import math
import glob

root_dir = 'generated_tests' 
leaf_dir_n_tests = 100
leaf_parent_dir_n = 10*leaf_dir_n_tests
children_n = 10


#def number_of_subfolders(num_tests):


def number_of_levels(num_tests):
    return math.ceil(math.log10(num_tests/100)) # num_tests = number of leaf nodes

# Seems to work well
def create_dir_tree_df(rootpath, height): # start_test, num_tests):
    path = rootpath
    stack = [(1,9),(1,8),(1,7),(1,6),(1,5),(1,4),(1,3),(1,2),(1,1),(1,0)]
    last_lvl = 0
    lvl = 0
    while stack:
        dir = stack.pop()
        last_lvl = lvl
        lvl = dir[0]
        num = dir[1]
        if last_lvl < lvl:
            path += '/level'+str(lvl)+'-'+str(num)
        elif last_lvl >= lvl:
            path = path[:-1]
            path += str(num)
        if not os.path.exists(path):
            os.makedirs(path)
        if lvl < height:
            for i in range(0, 10):
                stack.append((lvl+1, 9-i))
                #if lvl == height - 1: # Do something like this to not make a perfect tree
                #    num_leaf_dirs += 10
                #    return #? enough testfolders/tests
        #else
        #    subprocess.Popen(['./make_tests.sh', 100], cwd=torture).wait() # Make tests aswell 
        if num == 9 and last_lvl <= lvl and stack:
            pos = path.rfind('/')
            path = path[:pos]
            next_lvl = stack[-1][0]
            if lvl > next_lvl:
                for i in range(1, lvl-next_lvl):
                    pos = path.rfind('/')
                    path = path[:pos]

def get_args():
    """
    Get command line arguments
    """
    parser = argparse.ArgumentParser(description="""
    Diff the signature files provided in path
                   """)
    parser.add_argument('-p', '--path', action='store', dest='path',
                        help='path for signature files', default=None)
    parser.add_argument('-n', '--ntests', action='store', dest='ntests',
                        help='number of tests', default=None)
    return parser.parse_args()

if __name__ =='__main__':
    args = get_args()

    height = number_of_levels(int(args.ntests))
    create_dir_tree_df(root_dir, height)
#    for lvl in range(1, height):
#        for dir in range(0, children_n**lvl):
#            if not os.path.exists('level'+str(lvl)+'-'+str(dir)):
#                os.makedirs('level'+str(lvl)+'-'+str(dir))
#
#            if lvl == 
            
