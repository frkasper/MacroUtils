# -*- coding: utf-8 -*-
"""
Created on Fri Apr 27 08:25:53 2018

@author: Fabio Kasper
"""
import os
import shutil
import strings


def _copy(files, testhome):
    plural = 's' if len(files) > 1 else ''
    print('Copying %d file%s to TESTHOME:' % (len(files), plural)),

    only_strings = [isinstance(f, str) for f in files]
    assert len(only_strings) == sum(only_strings), 'Must be a list of strings'
    for f in files:
        shutil.copy(f, testhome)

    print('OK!')


def _setup_testhome(testhome):
    print('Setting up TESTHOME:'),
    if os.path.isdir(testhome):
        print('folder already exists')
    else:
        os.mkdir(testhome)
        print('folder created')


def environment(datahome, demohome, jarfile, starhome, testhome, files):
    """Set up the environment to run the demos"""
    print(strings.heading('Environment preparation'))
    _setup_testhome(testhome)
    files.append(jarfile)
    _copy(files, testhome)
    print('\n')


if __name__ == "__main__":
    pass
