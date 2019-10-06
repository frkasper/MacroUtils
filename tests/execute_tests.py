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
import glob
import itertools
import optparse
import os
import re
import shutil
import common.executor as executor
import common.set_up as set_up
import common.star as star
import common.strings as strings
import common.timer as timer
import tests_definition as td

from collections import namedtuple


_Options = namedtuple('Options', ['data_home', 'demo_home', 'jar_file',
                                  'star_home', 'test_home', 'test_cases',
                                  'threads', 'pytest_args', 'serial'])
PWD = os.getcwd()


def _call_pytest(options, test_cases):
    print(strings.heading('Calling pytest'))
    commands = ['export PYTHONPATH=%s' % PWD,
                'export STARHOME=%s' % options.star_home,
                'export TESTHOME=%s' % options.test_home,
                'pytest %s %s' % (options.pytest_args,
                                  _test_files(test_cases))]
    os.system('; '.join(commands))
    print('\n')


def _commands_from_demo(options, demo_nt):
    java_files = td.java_files_from_nt(demo_nt, options.demo_home)
    star_commands = []
    for java_file in java_files:
        np = 1 if options.serial else demo_nt.np
        star_cmd = star.new_simulation(options.star_home, java_file, np,
                                       demo_nt.batch)
        star_commands.append(star_cmd)
    return star_commands


def _commands_from_test(options, test_case):
    if td.is_demo(test_case):
        return _commands_from_demo(options, test_case)
    else:
        # Bugs are not run in step1
        return None


def _copy_test_macros(options):
    print(strings.heading('Test macros'))

    # Test macros first
    test_macros_home = os.path.join(PWD, 'macros')
    java_files = glob.glob(os.path.join(test_macros_home, '*Test.java'))
    assert len(java_files) > 0, 'No test macros found in %s' % test_macros_home

    # Then auxiliary macros
    aux_folder = 'common'
    aux_macros_home = os.path.join(test_macros_home, aux_folder)

    aux_in_test_home = os.path.join(options.test_home, aux_folder)
    if os.path.isdir(aux_in_test_home):
        shutil.rmtree(aux_in_test_home)

    # Finally, copy files
    set_up._copy(java_files, options.test_home)
    shutil.copytree(aux_macros_home, aux_in_test_home)

    names = [os.path.split(f)[1] for f in java_files]
    print(strings.itemized(names))
    print('\n')


def _demo(number):
    return td.demo(number)


def _demo_files(options, demo_nt):
    return td.demo_files(demo_nt.id, options.demo_home, options.data_home)


def _demo_home(options):
    """For convenience demo home is in the same folder as jar file"""
    bp, fn = os.path.split(options.jar_file)
    demo_home = os.path.join(bp, 'demo')
    assert os.path.isdir(demo_home), 'Folder does not exist: "%s"' % demo_home
    return demo_home


def _is_error(key, value):
    """Check if key is worth of trowing an optparse error"""
    if re.match('^(demo|threads)$', key):
        return False
    return value is None


def _itemized(tests):
    bugs = [nt.macro_name for nt in tests if td.is_bug(nt)]
    demos = ['Demo%d' % nt.id for nt in tests if td.is_demo(nt)]
    sas = [nt.macro_name for nt in tests if td.is_sa(nt)]
    sts = [nt.macro_name for nt in tests if td.is_simtool(nt)]

    all_tests = itertools.chain.from_iterable([demos, bugs, sas, sts])

    return strings.itemized(list(all_tests))


def _test_cases(parser):

    opts, args = parser.parse_args()

    given_options = [opts.bugs, opts.sas, opts.simtools, opts.demo is not None]
    execute_all = sum(given_options) == 0

    test_cases = []

    if opts.bugs and opts.demo is not None:
            parser.error('--demo and --bugs are mutually exclusive')
    if opts.sas and opts.demo is not None:
            parser.error('--sas and --bugs are mutually exclusive')
    if opts.simtools and opts.demo is not None:
            parser.error('--simtools and --bugs are mutually exclusive')

    if execute_all:

            test_cases.extend(td.DEMOS)
            test_cases.extend(td.BUGS)
            test_cases.extend(td.SAS)
            test_cases.extend(td.SIMTOOLS)

    else:

        if opts.bugs:
            test_cases.extend(td.BUGS)

        if opts.sas:
            test_cases.extend(td.SAS)

        if opts.simtools:
            test_cases.extend(td.SIMTOOLS)

        if opts.demo is not None:

            try:
                demo_id = int(opts.demo)
                test_cases.append(td.demo(demo_id))
            except ValueError:
                parser.error('demo must be an integer: "%s"' % opts.demo)

    return test_cases


def _test_files(test_cases):

    bugs = [nt for nt in test_cases if td.is_bug(nt)]
    demos = [nt for nt in test_cases if td.is_demo(nt)]
    sas = [nt for nt in test_cases if td.is_sa(nt)]
    sts = [nt for nt in test_cases if td.is_simtool(nt)]
    files = ['test/test_demo%02d.py' % nt.id for nt in demos]

    if len(bugs) > 0:
        files.append('test/test_bugs.py')

    if len(sas) > 0:
        files.append('test/test_simulation_assistants.py')

    if len(sts) > 0:
        files.append('test/test_simulation_tools.py')

    assert len(files) > 0, 'No test file retrieved'

    return ' '.join(files)


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
            '',
            'There are essentially two type of tests:',
            '  - demos: tests legacy MacroUtils demos;',
            '  - bugs: tests created over bugs filed over GitHub.',
            '',
            'If no custom syntax is given both types will be tested.',
            ]

    parser = optparse.OptionParser('\n'.join(usage))

    #
    # Runtime Group
    gr_r = optparse.OptionGroup(parser, 'Runtime Options', 'These options '
                                'control the required inputs for launching '
                                'the tests.')
    parser.add_option_group(gr_r)

    gr_r.add_option('--bugs', dest='bugs', action='store_true',
                    help='execute tests related to bugs only',
                    default=False)
    gr_r.add_option('--datahome', dest='datahome', action='store',
                    help='path to supporting files -- e.g.: geometries, etc',
                    default=None)
    gr_r.add_option('--demohome', dest='demohome', action='store',
                    help='path to demo source files',
                    default=None)
    gr_r.add_option('--demo', dest='demo', action='store',
                    help='run a specific demo number (default = all tests)',
                    default=None)
    gr_r.add_option('--jarhome', dest='jarhome', action='store',
                    help='path to MacroUtils compiled jar file',
                    default=None)
    gr_r.add_option('--sas', dest='sas', action='store_true',
                    help='execute simulation assistant tests only',
                    default=False)
    gr_r.add_option('--serial', dest='serial', action='store_true',
                    help='override STAR-CCM+ runs to serial (default = False)',
                    default=False)
    gr_r.add_option('--simtools', dest='simtools', action='store_true',
                    help='execute simulation tools tests only',
                    default=False)
    gr_r.add_option('--starhome', dest='starhome', action='store',
                    help='path to STAR-CCM+ installation',
                    default=None)
    gr_r.add_option('--testhome', dest='testhome', action='store',
                    help='path to where testing will be conducted',
                    default=None)
    gr_r.add_option('--threads', dest='threads', action='store',
                    help='how many multiple instances of STAR-CCM+ will be '
                    'run (default = 4)', default=4)
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

    opts, args = parser.parse_args()

    #
    # Assert all arguments are in place
    items = opts.__dict__.items()
    errors = ['%s not informed' % k for k, v in items if _is_error(k, v)]
    if len(errors) > 0:
        errors.append('add --help for a detailed list of options')
        parser.error('\n\n- %s\n' % '\n- '.join(errors))

    data_home = os.path.abspath(opts.datahome)
    demo_home = os.path.abspath(opts.demohome)
    jar_home = os.path.abspath(opts.jarhome)

    no_jars_msg = 'MacroUtils jar file does not exist in "%s"' % jar_home

    if os.path.isfile(jar_home):
        jar_file = jar_home
    else:
        jar_files = glob.glob(jar_home + os.sep + 'macroutils*.jar')

        if not jar_files:
            parser.error(no_jars_msg)

        jar_file = jar_files[0] if jar_files else None

    if not os.path.exists(jar_file):
        parser.error(no_jars_msg)

    star_home = os.path.abspath(opts.starhome)
    test_home = os.path.abspath(opts.testhome)

    for folder in [data_home, demo_home, star_home, test_home]:
        if not os.path.isdir(folder):
            parser.error('folder does not exist: "%s"' % folder)

    # Now assign a TESTHOME
    today = datetime.datetime.now().strftime('%Y%m%d')
    test_home = os.path.join(test_home, 'tests_%s' % today)

    test_cases = _test_cases(parser)

    pytest_args = []
    if opts.capture_no:
        pytest_args.append('-s')
    if opts.verbose:
        pytest_args.append('-v')
    if opts.stop:
        pytest_args.append('-x')

    serial = opts.serial
    nt = opts.threads
    threads = max(int(nt) if isinstance(nt, str) else nt, 0)

    return _Options(data_home, demo_home, jar_file, star_home, test_home,
                    test_cases, threads, ' '.join(pytest_args), serial)


def print_overview(options):
    fmt = '%s: %s'
    print(strings.frame('MacroUtils tester'))
    print('')
    print(strings.heading('Important information'))
    print(fmt % ('DATAHOME', options.data_home))
    print(fmt % ('DEMOHOME', options.demo_home))
    print(fmt % ('STARHOME', options.star_home))
    print(fmt % ('JAR FILE', options.jar_file))
    print(fmt % ('TESTHOME', options.test_home))
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
    print(strings.heading('Tests to run: %d' % len(options.test_cases)))
    print(_itemized(options.test_cases))
    print('\n')


def run_step1(options, test_cases):
    star_commands = [_commands_from_test(options, nt) for nt in test_cases]
    executor.run_commands(options.test_home, star_commands, options.threads)


def run_step2(options, test_cases):
    os.chdir(PWD)
    _copy_test_macros(options)
    _call_pytest(options, test_cases)


def run_tests(options):
    print_overview(options)

    print(strings.line())
    overall_time = timer.ExecutionTime(key='All Tests')
    print(strings.line() + '\n')

    # Bug and SimAssistant files are not copied at this time.
    demos = [nt for nt in options.test_cases if td.is_demo(nt)]
    demo_files_2d = [_demo_files(options, nt) for nt in demos]
    files = list(itertools.chain.from_iterable(demo_files_2d))

    set_up.environment(options.data_home, options.demo_home,
                       options.jar_file, options.star_home,
                       options.test_home, files)

    run_step1(options, options.test_cases)
    run_step2(options, options.test_cases)

    overall_time.finalize(extra_info='All Tests')


if __name__ == "__main__":
    options = parse_options()
    run_tests(options)
