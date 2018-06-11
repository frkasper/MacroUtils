# -*- coding: utf-8 -*-
"""
Basic definitions for the demos.


namedtuple fields:
    - id: Demo number;
    - np: number of cores to perform testing; np=0 is serial;
    - batch: a few demos are run in interactive mode;
    - files: the support files to be copied in order for the demo to execute.


@author: Fabio Kasper
"""
import glob
import itertools
import os
import re
from collections import namedtuple


Bug = namedtuple('Bug', ['load_demo', 'macro_name'])
Demo = namedtuple('Demo', ['id', 'np', 'batch', 'files'])
SimAssistant = namedtuple('SimAssistant', ['macro_name'])


# Declare individual demos here
DEMO_01 = Demo(id=1, np=2, batch=True, files=[])
DEMO_02 = Demo(id=2, np=2, batch=True, files=[])
DEMO_03 = Demo(id=3, np=2, batch=True, files=[])
DEMO_04 = Demo(id=4, np=1, batch=True, files=['radial_impeller.stp'])
DEMO_05 = Demo(id=5, np=6, batch=True, files=['LegoKart.x_b'])
# Demo 6 is bypassed
DEMO_07 = Demo(id=7, np=4, batch=True, files=[])
DEMO_08 = Demo(id=8, np=6, batch=True, files=['WING.x_b', 'wingCams.txt'])
# Demo 9 is bypassed
DEMO_10 = Demo(id=10, np=1, batch=True, files=[])
DEMO_11 = Demo(id=11, np=1, batch=True, files=[])
DEMO_12 = Demo(id=12, np=2, batch=True, files=[])
DEMO_13 = Demo(id=13, np=2, batch=True, files=[])
DEMO_14 = Demo(id=14, np=1, batch=True, files=[])
DEMO_15 = Demo(id=15, np=16, batch=True, files=['TableFromPeriodicRun.csv'])
DEMO_16 = Demo(id=16, np=4, batch=True, files=[])


# Declare individual bugs here
BUG_014 = Bug(load_demo=1, macro_name='BugCreateStreamlineSceneTest')


# Declare individual simulation assistants here
SA_01 = SimAssistant(macro_name='SimAssistantBlockMesherTest')


# Then collect them all using Python magic
BUGS = [v for k, v in sorted(vars().items()) if re.match('BUG_\d{3}', k)]
DEMOS = [v for k, v in sorted(vars().items()) if re.match('DEMO_\d{2}', k)]
SAS = [v for k, v in sorted(vars().items()) if re.match('SA_\d{2}', k)]


def _is_nt(nt, key):
    assert isinstance(nt, tuple), 'Must be a namedtuple'
    return bool(re.match('^%s\(.*\)$' % key, nt.__doc__))


def bug_file(macro_name, testhome):
    """Return a file necessary to run a particular bug"""
    macro_file = os.path.join(testhome, '%s.java' % macro_name)
    assert os.path.exists(macro_file), 'File does not exist: %s' % macro_file
    return macro_file


def demo(number):
    """Return a namedtuple"""
    assert isinstance(number, int), 'Demo number must be an integer'
    chosen = [d for d in DEMOS if d.id == number]
    assert len(chosen) > 0, 'Demo %d not declared in setup' % number
    assert len(chosen) == 1, 'Invalid result: "%s"' % str(chosen)
    return chosen[0]


def demo_files(number, demohome, datahome):
    """Return a list of files necessary to run a particular demo"""
    demo_nt = demo(number)
    support_files = [os.path.join(datahome, f) for f in demo_nt.files]
    for sf in support_files:
        assert os.path.exists(sf), 'File does not exist: %s' % sf
    all_files = [java_files(number, demohome), support_files]
    return sorted(list(itertools.chain.from_iterable(all_files)))


def is_bug(nt):
    return _is_nt(nt, 'Bug')


def is_demo(nt):
    return _is_nt(nt, 'Demo')


def is_sa(nt):
    return _is_nt(nt, 'SimAssistant')


def java_files(number, demohome):
    """Return a list of java files associated with the demo number"""
    java_files = glob.glob(os.path.join(demohome, 'Demo%d_*.java' % number))
    assert len(java_files) > 0, 'No JAVA files found for demo %d' % number
    return sorted(java_files)


def java_files_from_nt(demo_nt, demohome):
    """Return a list of java files associated with the demo"""
    files = java_files(demo_nt.id, demohome)
    return [f for f in files if re.match('.*\.java$', f)]


if __name__ == "__main__":
    pass
