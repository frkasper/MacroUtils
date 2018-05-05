# -*- coding: utf-8 -*-
"""
Created on Fri Apr 27 08:47:26 2018

@author: Fabio Kasper
"""
import os
import sys


def _assert_exists(f):
    assert os.path.exists(f), 'File does not exist: %s' % f


def _executable(starhome):
    """Return full path to STAR-CCM+"""
    if _is_linux():
        star_exe = os.path.join(starhome, 'star/bin/starccm+')
        _assert_exists(star_exe)
        return star_exe
    else:
        raise NotImplementedError('Have not tested in non Linux platforms')


def _no_path(macro_file):
    """Everything is run in the same folder so no full path is needed"""
    bp, fn = os.path.split(macro_file)
    return fn


def _is_linux():
    platform = sys.platform
    return platform == "linux" or platform == "linux2"


def _basic_syntax(macro_file, np, is_batch, sim_file=None):
    """Return the full syntax to run STAR-CCM+"""
    macro = '%s %s' % ('-batch' if is_batch else '-m', _no_path(macro_file))
    cores = '-np %d' % np if np > 1 else ''
    sim = '' if sim_file is None else sim_file
    return '-classpath . %s %s %s' % (macro, cores, sim)


def load_simulation(starhome, sim_file, macro_file, np, is_batch):
    """Return the full syntax to run STAR-CCM+"""
    basic_syntax = _basic_syntax(macro_file, np, is_batch)
    return '%s %s %s' % (_executable(starhome), basic_syntax, sim_file)


def new_simulation(starhome, macro_file, np, is_batch):
    """Return the full syntax to run STAR-CCM+"""
    basic_syntax = _basic_syntax(macro_file, np, is_batch)
    return '%s -new %s' % (_executable(starhome), basic_syntax)


if __name__ == "__main__":
    pass
