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
import tests_definition
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

from collections import namedtuple


_Options = namedtuple('Options', ['datahome', 'demohome', 'jarfile',
                                  'starhome', 'testhome', 'test_cases',
                                  'threads', 'pytest_args'])
PWD = os.getcwd()


def _call_pytest(options, test_cases):
    print(strings.heading('Calling pytest'))
    commands = ['export PYTHONPATH=%s' % PWD,
                'export STARHOME=%s' % options.starhome,
                'export TESTHOME=%s' % options.testhome,
                'pytest %s %s' % (options.pytest_args,
                                  _test_files(test_cases))]
    os.system('; '.join(commands))
    print('\n')


def _commands_from_demo(options, demo_nt):
    java_files = tests_definition.java_files_from_nt(demo_nt, options.demohome)
    star_commands = []
    for java_file in java_files:
        star_cmd = star.new_simulation(options.starhome, java_file,
                                       demo_nt.np, demo_nt.batch)
        star_commands.append(star_cmd)
    return star_commands


def _commands_from_test(options, test_case):
    if tests_definition.is_demo(test_case):
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
    java_files.extend(glob.glob(os.path.join(aux_macros_home, '*.java')))

    aux_in_testhome = os.path.join(options.testhome, aux_folder)
    if os.path.isdir(aux_in_testhome):
        shutil.rmtree(aux_in_testhome)

    # Finally, copy files
    set_up._copy(java_files, options.testhome)
    shutil.copytree(aux_macros_home, aux_in_testhome)

    names = [os.path.split(f)[1] for f in java_files]
    print(strings.itemized(names))
    print('\n')


def _demo(number):
    return tests_definition.demo(number)


def _demo_files(options, demo_nt):
        return tests_definition.demo_files(demo_nt.id, options.demohome,
                                           options.datahome)


def _demo_home(options):
    """For convenience demo home is in the same folder as jar file"""
    bp, fn = os.path.split(options.jarfile)
    demo_home = os.path.join(bp, 'demo')
    assert os.path.isdir(demo_home), 'Folder does not exist: "%s"' % demo_home
    return demo_home


def _is_error(key, value):
    """Check if key is worth of trowing an optparse error"""
    if re.match('^(demo|threads)$', key):
        return False
    return value is None


def _itemized(tests):
    bugs = [nt.macro_name for nt in tests if tests_definition.is_bug(nt)]
    demos = ['Demo%d' % nt.id for nt in tests if tests_definition.is_demo(nt)]
    all_tests = itertools.chain.from_iterable([demos, bugs])
    return strings.itemized(list(all_tests))


def _test_files(test_cases):
    bugs = [nt for nt in test_cases if tests_definition.is_bug(nt)]
    demos = [nt for nt in test_cases if tests_definition.is_demo(nt)]
    files = ['test/test_demo%02d.py' % nt.id for nt in demos]
    if len(bugs) > 0:
        files.append('test/test_bugs.py')
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
                    help='path to MacroUtils.jar file',
                    default=None)
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

    if opts.bugs and opts.demo is not None:
            parser.error('--demo and --bugs are mutually exclusive')

    test_cases = []
    if opts.demo is None:
        if not opts.bugs:
            test_cases.extend(tests_definition.DEMOS)
        test_cases.extend(tests_definition.BUGS)
    else:
        try:
            demo_id = int(opts.demo)
            test_cases.append(tests_definition.demo(demo_id))
        except ValueError:
            parser.error('demo must be an integer: "%s"' % opts.demo)

    pytest_args = []
    if opts.capture_no:
        pytest_args.append('-s')
    if opts.verbose:
        pytest_args.append('-v')
    if opts.stop:
        pytest_args.append('-x')

    nt = opts.threads
    threads = max(int(nt) if isinstance(nt, str) else nt, 0)

    return _Options(datahome, demohome, jarfile, starhome, testhome,
                    test_cases, threads, ' '.join(pytest_args))


def print_overview(options):
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
    print(strings.heading('Tests to run: %d' % len(options.test_cases)))
    print(_itemized(options.test_cases))
    print('\n')


def run_step1(options, test_cases):
    star_commands = [_commands_from_test(options, nt) for nt in test_cases]
    executor.run_commands(options.testhome, star_commands, options.threads)


def run_step2(options, test_cases):
    os.chdir(PWD)
    _copy_test_macros(options)
    _call_pytest(options, test_cases)


def run_tests(options):
    print_overview(options)

    # Bug files are not copied at this time.
    demos = [nt for nt in options.test_cases if tests_definition.is_demo(nt)]
    demo_files_2d = [_demo_files(options, nt) for nt in demos]
    files = list(itertools.chain.from_iterable(demo_files_2d))

    set_up.environment(options.datahome, options.demohome,
                       options.jarfile, options.starhome,
                       options.testhome, files)

    run_step1(options, options.test_cases)
    run_step2(options, options.test_cases)


if __name__ == "__main__":
    options = parse_options()
#    for tc in options.test_cases:
#        print tc
    run_tests(options)
