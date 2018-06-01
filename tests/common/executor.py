# -*- coding: utf-8 -*-
"""
This module executes commands.

Created on Fri Apr 27 10:44:34 2018

@author: Fabio Kasper
"""
import glob
import os
import Queue
import re
import strings
import threading
import time
import timer


def _flat(commands):
    """Flatten commands while preserving order"""
    commands = [cmd for cmd in commands if cmd is not None]
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


def run_commands(testhome, commands, num_threads):
    """General runner"""
    star_time = timer.ExecutionTime(key='STAR-CCM+ macros')
    print('')
    if num_threads > 0:
        run_multiple(testhome, commands, num_threads)
    else:
        run_sequential(testhome, commands)
    star_time.finalize(extra_info='STAR-CCM+ macros')
    print('\n')


def run_multiple(testhome, commands, num_threads):
    """Run multiple threads"""
    threads = '(threads = %d)' % num_threads
    print(strings.heading('Running in threaded mode %s' % threads))

    commands_to_run = _flat(commands)
    if len(commands_to_run) == 0:
        return

    queue = Queue.Queue()
    for i in range(num_threads):
        thread = TesterThread(queue=queue)
        thread.start()

    for command in commands_to_run:
        base_name = _case_name(command)
        if _will_run(testhome, base_name):
            queue.put([testhome, command])
            time.sleep(1)
        else:
            print('Bypassed due presence of log/picture files: %s' % base_name)

    queue.join()


def run_sequential(testhome, commands):
    """Run invidually"""
    print(strings.heading('Running in sequential mode'))

    commands_to_run = _flat(commands)
    if len(commands_to_run) == 0:
        return

    for command in commands_to_run:
        base_name = _case_name(command)
        if _will_run(testhome, base_name):
            print('Running: %s' % base_name)
            _run(testhome, command)
        else:
            print('Bypassed due presence of log/picture files: %s' % base_name)


class TesterThread(threading.Thread):
    def __init__(self, queue=None):
        threading.Thread.__init__(self)
        self.queue = queue
        self.setDaemon(True)

    def run(self):
        while True:
            self.run_command()
            self.queue.task_done()

    def run_command(self):
        et = timer.ExecutionTime(verbose=False)
        testhome, command = self.queue.get()
        name = _case_name(command)
        print('  |--> Started: %s (at %s)' % (name, et.s_t0))
        _run(testhome, command)
        et.finalize()
        print '   -->| Finished: %s (duration: %s)' % (name, et.s_tf)


if __name__ == "__main__":
    pass
