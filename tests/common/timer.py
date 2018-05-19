# -*- coding: utf-8 -*-
"""
A few useful timer tools.

@author: Fabio Kasper
"""
import datetime


class ExecutionTime():

    """A very useful information to have"""

    def __init__(self, t0=None, verbose=True):
        """Initialize with no argument as a start"""
        self.verbose = verbose
        if t0 is None:
            t0 = datetime.datetime.now()
        self.t0 = t0
        self.s_t0 = t0.strftime('%H:%M:%S')
        if verbose:
            print('Execution started at %s.' % self.s_t0)

    def __delta(self, t1):
        return int((t1 - self.t0).total_seconds())

    def finalize(self, extra_info=None):
        dt = self.__delta(datetime.datetime.now())
        h = dt // 3600
        m = (dt % 3600) // 60
        s = (dt % 3600) % 60
        sh = '%dh' % h if h > 0 else ''
        sm = '%dmin' % m if m > 0 else ''
        ss = '%ds' % s if s > 0 else ''
        self.s_tf = '%s%s%s' % (sh, sm, ss) if dt > 0 else '0s'
        ei = '' if extra_info is None else ' -- %s' % extra_info
        if self.verbose:
            print('Execution time%s: %s.' % (ei, self.s_tf))


if __name__ == "__main__":
    pass
