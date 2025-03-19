#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Execute MacroUtils tests using pytest.

At the moment, testing is restricted to legacy demos and this framework is
just for consistency checking.


Basic requirements:
    - Python 3 -- tested with Python 3.8
    - pytest -- see http://www.pytest.org


Limitations:
    - Restricted to Linux only


@author: Fabio Kasper
"""
import datetime
import os
import re
import shutil
from dataclasses import dataclass
from optparse import OptionParser, OptionGroup
from pathlib import Path
from common import executor, set_up, star, strings, timer
from tests_definition import Bug, Case, CASES, Demo, SimAssistant, SimTool, \
                             filtered_cases, get_demo


WORKDIR = Path(__file__).parent


@dataclass
class Options:
    serial: bool
    threads: int
    data_home: Path
    demo_home: Path
    jar_file: Path
    star_home: Path
    test_home: Path
    test_cases: list[Case]
    pytest_args: list[str]

    @property
    def bugs(self) -> list[Bug]:
        return filtered_cases(self.test_cases, Bug)

    @property
    def demos(self) -> list[Demo]:
        return filtered_cases(self.test_cases, Demo)

    @property
    def sim_assistants(self) -> list[SimAssistant]:
        return filtered_cases(self.test_cases, SimAssistant)

    @property
    def sim_tools(self) -> list[SimTool]:
        return filtered_cases(self.test_cases, SimTool)

    @property
    def pytest_command(self) -> str:
        command = ['pytest'] + self.pytest_args
        command.extend(f'test/test_demo{d.id:02d}.py' for d in self.demos)
        if self.bugs:
            command.append('test/test_bugs.py')
        if self.sim_assistants:
            command.append('test/test_simulation_assistants.py')
        if self.sim_tools:
            command.append('test/test_simulation_tools.py')
        return ' '.join(command)


    def print_overview(self):
        print(strings.frame('MacroUtils tester'))
        print('')
        print(strings.heading('Important information'))
        print(f'DATAHOME: {self.data_home}')
        print(f'DEMOHOME: {self.demo_home}')
        print(f'STARHOME: {self.star_home}')
        print(f'JAR_FILE: {self.jar_file}')
        print(f'TESTHOME: {self.test_home}')
        print(f'PYTESTS : {WORKDIR}')
        print('\n')
        print(strings.heading('Basic Procedure'))
        print('1) Run some demos in STAR-CCM+')
        print('2) Test the results achieved above with pytest')
        print('\n')
        print(strings.heading('Notes'))
        print('- In case of a test repetition, only step (2) is performed')
        print('- To repeat step (1), remove the corresponding STAR-CCM+ ')
        print('  files associated with the demo(s) in TESTHOME')
        print('\n')
        print(strings.heading(f'Tests to run: {len(self.test_cases)}'))
        print(strings.itemized([case.name for case in self.test_cases]))
        print('\n')


def call_pytest(options: Options):
    print(strings.heading('Calling pytest'))
    commands = ['export PYTHONPATH=%s' % WORKDIR,
                'export STARHOME=%s' % options.star_home,
                'export TESTHOME=%s' % options.test_home,
                options.pytest_command]
    os.chdir(WORKDIR)
    os.system('; '.join(commands))
    print('\n')


def copy_test_macros(options: Options):
    print(strings.heading('Test macros'))

    # Test macros first
    macros_folder = WORKDIR.joinpath('macros')
    java_files = list(macros_folder.glob('*Test.java'))

    # Then auxiliary macros
    macros_subfolder = macros_folder.joinpath('common')

    # Refresh subfolder if needed
    test_home_subfolder = options.test_home.joinpath(macros_subfolder.name)
    if test_home_subfolder.is_dir():
        shutil.rmtree(test_home_subfolder)

    # Finally, copy the files
    set_up._copy(java_files, options.test_home)
    shutil.copytree(macros_subfolder, test_home_subfolder)
    print('\n')


def parse_options() -> Options:

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

    parser = OptionParser('\n'.join(usage))

    #
    # Runtime Group
    gr_r = OptionGroup(parser, 'Runtime Options', 'Options related to the '
                       'being evaluated.')
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
    gr_p = OptionGroup(parser, 'pytest Options', 'Options related to pytest.')
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

    def informed(key, value) -> bool:
        if re.match('^(demo|threads)$', key):
            return False
        return value is None

    #
    # Assert that all arguments are informed
    items = opts.__dict__.items()
    errors = ['%s not informed' % k for k, v in items if informed(k, v)]
    if len(errors) > 0:
        errors.append('add --help for a detailed list of options')
        parser.error('\n\n- %s\n' % '\n- '.join(errors))

    data_home = Path(opts.datahome).resolve()
    demo_home = Path(opts.demohome).resolve()
    jar_home = Path(opts.jarhome).resolve()

    if jar_home.is_file():
        jar_file = jar_home
    else:
        jar_files = jar_home.glob('macroutils*.jar')
        if not jar_files:
            parser.error(f'No MacroUtils jar files in {jar_home}')
        jar_file = next(jar_files)

    jar_file = jar_file.resolve()
    star_home = Path(opts.starhome).resolve()
    test_home = Path(opts.testhome).resolve()

    for folder in [data_home, demo_home, star_home, test_home]:
        if not folder.is_dir():
            parser.error(f'folder does not exist: {folder!r}')

    # Now assign a TESTHOME
    today = datetime.datetime.now().strftime('%Y%m%d')
    test_home = test_home.joinpath(f'tests_{today}')


    if opts.bugs and opts.demo is not None:
            parser.error('--demo and --bugs are mutually exclusive')
    if opts.sas and opts.demo is not None:
            parser.error('--sas and --bugs are mutually exclusive')
    if opts.simtools and opts.demo is not None:
            parser.error('--simtools and --bugs are mutually exclusive')

    # Parse the test cases to be run
    test_cases = []
    if opts.demo is None and not any([opts.bugs, opts.sas, opts.simtools]):
        opts.bugs = opts.sas = opts.simtools = True
        test_cases.extend(filtered_cases(CASES, Demo))
    elif opts.demo is None:
        pass
    else:
        try:
            test_cases.append(get_demo(opts.demo))
        except ValueError as ve:
            parser.error(ve)
    if opts.bugs:
        test_cases.extend(filtered_cases(CASES, Bug))
    if opts.sas:
        test_cases.extend(filtered_cases(CASES, SimAssistant))
    if opts.simtools:
        test_cases.extend(filtered_cases(CASES, SimTool))

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

    return Options(serial, threads, data_home, demo_home, jar_file, star_home,
                   test_home, test_cases, pytest_args)


def run_starccm_plus(options: Options):
    star_commands = []
    for demo in options.demos:
        for java_file in demo.java_files(options.demo_home):
            np = 1 if options.serial else demo.np  # Override from command line
            star_cmd = star.new_simulation(options.star_home, java_file, np,
                                           demo.batch)
            star_commands.append(star_cmd)
    executor.run_commands(options.test_home, star_commands, options.threads)


def run_pytest(options: Options):
    copy_test_macros(options)
    call_pytest(options)


def run_tests(options: Options):

    options.print_overview()
    print(strings.line())
    overall_time = timer.ExecutionTime(key='All Tests')
    print(strings.line() + '\n')

    def running_files(demo: Demo) -> list[Path]:
        return demo.running_files(options.demo_home, options.data_home)

    # Bug and SimAssistant files are not copied at this time.
    files = [running_files(demo) for demo in options.demos]

    set_up.environment(options.data_home, options.demo_home, options.jar_file,
                       options.star_home, options.test_home,
                       [file for group in files for file in group])

    run_starccm_plus(options)
    run_pytest(options)

    overall_time.finalize(extra_info='All Tests')


def main():
    options = parse_options()
    run_tests(options)


if __name__ == "__main__":
    main()
