# -*- coding: utf-8 -*-
"""
Some string utilities.

Created on Fri Apr 27 08:06:06 2018

@author: Fabio Kasper
"""


N_CHARS = 80


def _assert_is_list(items):
    assert isinstance(items, list), _reason('Must be a list', items)


def _assert_is_list_of_strings(items):
    _assert_is_list(items)
    assert len(items) == sum([isinstance(item, str) for item in items]), \
        _reason('Must be a list of strings', items)


def _reason(reason, obj):
    return '%s: "%s"' % (reason, obj)


def frame(message):
    """Create a fancy frame"""
    line = N_CHARS * '='
    string = '  '.join(message.upper()).center(N_CHARS)
    return '\n'.join([line, string, line])


def heading(message):
    """Return a heading message"""
    return '%s\n%s' % (message, line())


def itemized(items):
    """Return a fancy list of strings"""
    _assert_is_list_of_strings(items)
    if len(items) < 2:
        return ' '.join(items)
    else:
        flat_items = ', '.join(items[:-1])
        return '%s and %s' % (flat_items, items[-1])


def line():
    """Return a simple line"""
    return N_CHARS * '-'


if __name__ == "__main__":
    pass
