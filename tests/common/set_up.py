# -*- coding: utf-8 -*-
"""
Created on Fri Apr 27 08:25:53 2018

@author: Fabio Kasper
"""
import os
from pathlib import Path
import shutil
import common.strings as strings


def _copy(files: list[Path], testhome: Path):
    n = len(files)
    print('Copying {} file{} to TESTHOME:'.format(n, 's' if n > 1 else ''),
          end=' ')
    print(strings.itemized([repr(file.name) for file in files]))
    for file in files:
        shutil.copy(file, testhome)


def _setup_testhome(testhome: Path):
    print('Setting up TESTHOME:', end=' ')
    if testhome.is_dir():
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
