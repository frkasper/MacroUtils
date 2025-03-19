# -*- coding: utf-8 -*-
"""
This module executes commands.

Created on Fri Apr 27 10:44:34 2018

@author: Fabio Kasper
"""
import glob
import os
import queue
import re
import threading
import time
import common.timer as timer
import common.strings as strings


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


def _number_of_cores(command):
    """Retrieve np info"""
    found = re.findall('-np (\d+)', command)
    assert len(found) < 2, 'Could not parse np from command: %s' % command
    return int(found[0]) if found else 1


def _run(testhome, command):
    os.chdir(testhome)
    os.system('%s > %s' % (command,  _log(command)))


def _will_run(testhome, base_name):
    pictures = glob.glob(os.path.join(testhome, '%s*.png' % base_name))
    log_file = glob.glob(os.path.join(testhome, '%s.log' % base_name))
    return len(pictures) + len(log_file) == 0


def run_commands(testhome, commands, threads):
    """General runner"""
    star_time = timer.ExecutionTime(key='STAR-CCM+ macros')
    print('')
    if threads > 1:
        run_multiple(testhome, commands, threads)
    else:
        run_sequential(testhome, commands)
    star_time.finalize(extra_info='STAR-CCM+ macros')
    print('\n')


def run_multiple(testhome, commands, threads):
    """Run multiple threads"""
    print(strings.heading(f'Running in threaded mode ({threads = })'))

    commands_to_run = _flat(commands)
    if len(commands_to_run) == 0:
        return

    run_queue = queue.Queue()
    for i in range(threads):
        thread = TesterThread(queue=run_queue)
        thread.start()

    for command in commands_to_run:
        base_name = _case_name(command)
        if _will_run(testhome, base_name):
            run_queue.put([testhome, command])
            time.sleep(1)
        else:
            print(f'Bypassed due presence of log/picture files: {base_name}')

    run_queue.join()


def run_sequential(testhome, commands):
    """Run invidually"""
    print(strings.heading('Running in sequential mode'))

    commands_to_run = _flat(commands)
    if len(commands_to_run) == 0:
        return

    for command in commands_to_run:
        base_name = _case_name(command)
        np = _number_of_cores(command)
        snp = 'np=%d' % np if np > 1 else 'serial'
        if _will_run(testhome, base_name):
            print('Running: %s (running %s)' % (base_name, snp))
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
        np = _number_of_cores(command)
        snp = 'np=%d' % np if np > 1 else 'serial'
        print('  |--> Started: %s (at %s running %s)' % (name, et.s_t0, snp))
        _run(testhome, command)
        et.finalize()
        print('   -->| Finished: %s (duration: %s)' % (name, et.s_tf))


if __name__ == "__main__":
    pass
