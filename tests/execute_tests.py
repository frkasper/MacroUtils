#!/usr/bin/python
# -*- coding: utf-8 -*-
"""
Execute MacroUtils tests using pytest.

At the moment, testing is restricted to legacy demos and this framework is
just for consistency checking.


Basic requirements:
    - Python 2 -- not tested with Python 3
    - pytest -- see http://www.pytest.org


Limitations:
    - Restricted to Linux only


@author: Fabio Kasper
"""
import datetime
import demos_definition
import glob
import itertools
import optparse
import os
import re
import common.executor as executor
import common.set_up as set_up
import common.star as star
import common.strings as strings
from collections import namedtuple


_Options = namedtuple('Options', ['datahome', 'demohome', 'jarfile',
                                  'starhome', 'testhome', 'demo',
                                  'pytest_args'])
PWD = os.getcwd()


def _call_pytest(options, demos_to_run):
    print(strings.heading('Calling pytest'))
    commands = ['export PYTHONPATH=%s' % PWD,
                'export STARHOME=%s' % options.starhome,
                'export TESTHOME=%s' % options.testhome,
                'pytest %s %s' % (options.pytest_args,
                                  _test_files(demos_to_run))]
    os.system('; '.join(commands))
    print('\n')


def _commands_from_demo(options, demo_nt):
    java_files = demos_definition.java_files_from_nt(demo_nt, options.demohome)
    star_commands = []
    for java_file in java_files:
        star_cmd = star.new_simulation(options.starhome, java_file,
                                       demo_nt.np, demo_nt.batch)
        star_commands.append(star_cmd)
    return star_commands


def _copy_test_macros(options, demos_to_run):
    print(strings.heading('Test macros'))
    test_macros = _test_macros(options)
    set_up._copy(test_macros, options.testhome)
    names = [os.path.split(f)[1] for f in test_macros]
    print(strings.itemized(names))
    print('\n')


def _demo(number):
    return demos_definition.demo(number)


def _demo_files(options, demo_nt):
        return demos_definition.demo_files(demo_nt.id, options.demohome,
                                           options.datahome)


def _demo_home(options):
    """For convenience demo home is in the same folder as jar file"""
    bp, fn = os.path.split(options.jarfile)
    demo_home = os.path.join(bp, 'demo')
    assert os.path.isdir(demo_home), 'Folder does not exist: "%s"' % demo_home
    return demo_home


def _demos():
    return demos_definition.DEMOS


def _is_error(key, value):
    """Check if key is worth of trowing an optparse error"""
    if re.match('^demo$', key):
        return False
    return value is None


def _test_files(demos_to_run):
    files = ['test/test_demo%02d.py' % nt.id for nt in demos_to_run]
    return ' '.join(files)


def _test_macros(options):
    test_macros_home = os.path.join(PWD, 'macros')
    files = glob.glob(os.path.join(test_macros_home, '*Test.java'))
    assert len(files) > 0, 'No test macros found in %s' % test_macros_home
    return files


def parse_options():
    """Parse argument options and return a namedtuple."""
    usage = [
            '%prog [options]',
            '',
            'Execute MacroUtils tests using pytest. This is an optional step',
            'and only serves for consistency checks while maintaining this',
            'library. It is encouraged though that everyone using MacroUtils',
            'try to execute the tests themselves, specially when changing the',
            'source code.',
            ]

    parser = optparse.OptionParser('\n'.join(usage))

    #
    # Runtime Group
    gr_r = optparse.OptionGroup(parser, 'Runtime Options', 'These options '
                                'control the required inputs for launching '
                                'the tests.')
    parser.add_option_group(gr_r)

    gr_r.add_option('--datahome', dest='datahome', action='store',
                    help='path to supporting files -- e.g.: geometries, etc',
                    default=None)
    gr_r.add_option('--demohome', dest='demohome', action='store',
                    help='path to demo source files',
                    default=None)
    gr_r.add_option('--demo', dest='demo', action='store',
                    help='run a specific demo number (default = all)',
                    default=None)
    gr_r.add_option('--jarhome', dest='jarhome', action='store',
                    help='path to MacroUtils.jar file',
                    default=None)
    gr_r.add_option('--starhome', dest='starhome', action='store',
                    help='path to STAR-CCM+ installation',
                    default=None)
    gr_r.add_option('--testhome', dest='testhome', action='store',
                    help='path to where testing will be conducted',
                    default=None)

    #
    # pytest Group
    gr_p = optparse.OptionGroup(parser, 'pytest Options', 'These options '
                                'control pytest utility.')
    parser.add_option_group(gr_p)

    gr_p.add_option('-s', dest='capture_no', action='store_true',
                    help='print captured output to console',
                    default=False)
    gr_p.add_option('-v', dest='verbose', action='store_true',
                    help='extra verbosity for each test_case()',
                    default=False)
    gr_p.add_option('-x', dest='stop', action='store_true',
                    help='stop at first failure',
                    default=False)

    (opts, args) = parser.parse_args()

    #
    # Assert all arguments are in place
    items = opts.__dict__.items()
    errors = ['%s not informed' % k for k, v in items if _is_error(k, v)]
    if len(errors) > 0:
        errors.append('add --help for a detailed list of options')
        parser.error('\n\n- %s\n' % '\n- '.join(errors))

    datahome = os.path.abspath(opts.datahome)
    demohome = os.path.abspath(opts.demohome)
    jarhome = os.path.abspath(opts.jarhome)
    if os.path.isfile(jarhome):
        jarfile = jarhome
    else:
        jarfile = os.path.join(jarhome, 'MacroUtils.jar')
    starhome = os.path.abspath(opts.starhome)
    testhome = os.path.abspath(opts.testhome)

    for folder in [datahome, demohome, starhome, testhome]:
        if not os.path.isdir(folder):
            parser.error('folder does not exist: "%s"' % folder)
    if not os.path.exists(jarfile):
        parser.error('MacroUtils.jar file does not exist in "%s"' % jarhome)

    # Now assign a TESTHOME
    today = datetime.datetime.now().strftime('%Y%m%d')
    testhome = os.path.join(testhome, 'tests_%s' % today)

    if opts.demo is None:
        demo = opts.demo
    else:
        try:
            demo = int(opts.demo)
        except ValueError:
            parser.error('demo must be an integer: "%s"' % opts.demo)

    pytest_args = []
    if opts.capture_no:
        pytest_args.append('-s')
    if opts.verbose:
        pytest_args.append('-v')
    if opts.stop:
        pytest_args.append('-x')

    return _Options(datahome, demohome, jarfile, starhome, testhome, demo,
                    ' '.join(pytest_args))


def print_overview(options, demos_to_run):
    fmt = '%s: %s'
    print(strings.frame('MacroUtils tester'))
    print('')
    print(strings.heading('Important information'))
    print(fmt % ('DATAHOME', options.datahome))
    print(fmt % ('DEMOHOME', options.demohome))
    print(fmt % ('STARHOME', options.starhome))
    print(fmt % ('JAR FILE', options.jarfile))
    print(fmt % ('TESTHOME', options.testhome))
    print('\n')
    print(strings.heading('Basic Procedure'))
    print('1) Run demos in STAR-CCM+ -- functional tests;')
    print('2) Perform unit testing with pytest.')
    print('\n')
    print(strings.heading('Note'))
    print('- In case of a test repetition, only step (2) will be performed;')
    print('- If one wants to repeat step (1), just remove corresponding')
    print('  STAR-CCM+ files associated with the demo(s) in TESTHOME.')
    print('\n')
    print(strings.heading('Demos to run: %d' % len(demos_to_run)))
    print(strings.itemized(['Demo%d' % demo.id for demo in demos_to_run]))
    print('\n')


def run_step1(options, demos_to_run):
    star_commands = [_commands_from_demo(options, nt) for nt in demos_to_run]
    executor.run_sequential(options.testhome, star_commands)


def run_step2(options, demos_to_run):
    os.chdir(PWD)
    _copy_test_macros(options, demos_to_run)
    _call_pytest(options, demos_to_run)


def run_tests(options):
    demos_to_run = []
    if options.demo is None:
        demos_to_run.extend(_demos())
    else:
        demo = demos_definition.demo(options.demo)
        demos_to_run.append(demo)
    print_overview(options, demos_to_run)
    files_2d = [_demo_files(options, demo_nt) for demo_nt in demos_to_run]
    files = list(itertools.chain.from_iterable(files_2d))

    set_up.environment(options.datahome, options.demohome,
                       options.jarfile, options.starhome,
                       options.testhome, files)

    run_step1(options, demos_to_run)
    run_step2(options, demos_to_run)


if __name__ == "__main__":
    options = parse_options()
    run_tests(options)
