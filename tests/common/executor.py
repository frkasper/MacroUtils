# -*- coding: utf-8 -*-
"""
This module executes commands.

Created on Fri Apr 27 10:44:34 2018

@author: Fabio Kasper
"""
import glob
import os
import re
import strings


def _flat(commands):
    """Flatten commands while preserving order"""
    flattened = []
    for command in commands:
        if isinstance(command, str):
            flattened.append(command)
        elif isinstance(command, list):
            flattened.extend(command)
        else:
            raise TypeError('Invalid command: %s' % str(command))
    return flattened


def _case_name(command):
    """Demo name based on command supplied"""
    found = re.findall('(\w*)\.java', command)
    assert len(found) == 1, 'Could not parse name from command: %s' % command
    return found[0]


def _log(command):
    """Automatic log file generation based on macro"""
    return '%s.log' % _case_name(command)


def _run(testhome, command):
    os.chdir(testhome)
    os.system('%s > %s' % (command,  _log(command)))


def _will_run(testhome, base_name):
    pictures = glob.glob(os.path.join(testhome, '%s*.png' % base_name))
    log_file = glob.glob(os.path.join(testhome, '%s.log' % base_name))
    return len(pictures) + len(log_file) == 0


def run_multiple(testhome, commands, num_threads=4):
    """Run multiple threads"""
    threads = '(threads = %d)' % num_threads
    print(strings.heading('Running in threaded mode %s' % threads))
    raise NotImplementedError('TODO')
    print('\n')


def run_sequential(testhome, commands):
    """Run invidually"""
    commands = [cmd for cmd in commands if cmd is not None]
    if len(commands) == 0:
        return
    print(strings.heading('Running in sequential mode'))
    for command in _flat(commands):
        base_name = _case_name(command)
        if _will_run(testhome, base_name):
            print('Running: %s' % base_name)
            _run(testhome, command)
        else:
            print('Bypassed due presence of log/picture files: %s' % base_name)
    print('\n')


if __name__ == "__main__":
    pass
