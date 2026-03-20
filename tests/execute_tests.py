#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Execute MacroUtils tests using pytest.

At the moment, testing is restricted to legacy demos and this framework is
just for consistency checking.

Basic requirements:
    - Python 3
    - pytest -- see http://www.pytest.org

Limitations:
    - Restricted to Linux only

@author: Fabio Kasper
"""
import datetime
import os
import re
import shutil
from argparse import ArgumentParser, RawDescriptionHelpFormatter
from dataclasses import dataclass
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

    description = (
        'Execute MacroUtils tests using pytest. This is an optional step\n'
        'and only serves for consistency checks while maintaining this\n'
        'library. It is encouraged to execute these tests when changing the\n'
        'source code.\n'
        '\n'
        'There are essentially two type of tests:\n'
        '  - demos: tests legacy MacroUtils demos;\n'
        '  - bugs: tests created over bugs filed over GitHub.\n'
        '\n'
        'If no custom syntax is given both types will be tested.'
    )

    parser = ArgumentParser(description=description,
                            formatter_class=RawDescriptionHelpFormatter,
                            add_help=True)

    gr_r = parser.add_argument_group('Runtime Options', 'Options related to '
                                     'the runtime being evaluated.')
    gr_r.add_argument('--bugs', action='store_true', default=False,
                      help='execute tests related to bugs only')
    gr_r.add_argument('--datahome', action='store', type=Path,
                      required=True, metavar='PATH', help='path to '
                      'supporting files -- e.g.: geometries, etc')
    gr_r.add_argument('--demohome', action='store', type=Path,
                      required=True, metavar='PATH', help='path to demo '
                      'source files')
    gr_r.add_argument('--demo', action='store', type=int, default=-1,
                      metavar='N', help='run a specific demo number '
                      '(default = all tests)')
    gr_r.add_argument('--jarhome', action='store', type=Path,
                      required=True, metavar='PATH', help='path to '
                      'MacroUtils compiled jar file')
    gr_r.add_argument('--sas', action='store_true', default=False,
                      help='execute simulation assistant tests only')
    gr_r.add_argument('--serial', action='store_true', default=False,
                      help='override STAR-CCM+ runs to serial '
                      '(default = False)')
    gr_r.add_argument('--simtools', action='store_true', default=False,
                      help='execute simulation tools tests only')
    gr_r.add_argument('--starhome', action='store', type=Path,
                      required=True, metavar='PATH', help='path to '
                      'STAR-CCM+ installation')
    gr_r.add_argument('--testhome', action='store', type=Path,
                      required=True, metavar='PATH', help='path to where '
                      'testing will be conducted')
    gr_r.add_argument('--threads', action='store', default=4, metavar='N',
                      help='how many multiple instances of STAR-CCM+ will '
                      'be run (default = 4)')

    gr_p = parser.add_argument_group('pytest Options', 'Options related to '
                                     'pytest.')
    gr_p.add_argument('-s', dest='capture_no', action='store_true',
                      default=False, help='print captured output to console')
    gr_p.add_argument('-v', dest='verbose', action='store_true', default=False,
                      help='extra verbosity for each test')
    gr_p.add_argument('-x', dest='stop', action='store_true', default=False,
                      help='stop at first failure')

    args = parser.parse_args()

    def informed(key, value) -> bool:
        if re.match('^(demo|threads)$', key):
            return False
        return value is None

    # Assert that all required arguments are informed
    items = vars(args).items()
    errors = ['%s not informed' % k for k, v in items if informed(k, v)]
    if errors:
        errors.append('add --help for a detailed list of options')
        parser.error('\n\n- %s\n' % '\n- '.join(errors))

    data_home: Path = args.datahome.resolve()
    demo_home: Path = args.demohome.resolve()
    star_home: Path = args.starhome.resolve()
    test_home: Path = args.testhome.resolve()

    for folder in [data_home, demo_home, star_home, test_home]:
        if not folder.is_dir():
            parser.error(f'invalid folder: {folder.as_posix()}')

    jar_home: Path = args.jarhome.resolve()
    jar_file = jar_home
    if not jar_file.is_file():
        jar_files = jar_home.glob('macroutils*.jar')
        if not jar_files:
            parser.error(f'No MacroUtils jar files in {jar_home}')
        jar_file = next(jar_files).resolve()

    # Now assign a TESTHOME
    today = datetime.datetime.now().strftime('%Y%m%d')
    test_home = test_home.joinpath(f'tests_{today}')

    if args.bugs and args.demo > 0:
        parser.error('--demo and --bugs are mutually exclusive')
    if args.sas and args.demo > 0:
        parser.error('--sas and --demo are mutually exclusive')
    if args.simtools and args.demo > 0:
        parser.error('--simtools and --demo are mutually exclusive')

    # Parse the test cases to be run
    test_cases = []
    if args.demo < 0 and not any([args.bugs, args.sas, args.simtools]):
        args.bugs = args.sas = args.simtools = True
        test_cases.extend(filtered_cases(CASES, Demo))
    elif args.demo < 0:
        pass
    else:
        try:
            test_cases.append(get_demo(args.demo))
        except ValueError as ve:
            parser.error(ve)
    if args.bugs:
        test_cases.extend(filtered_cases(CASES, Bug))
    if args.sas:
        test_cases.extend(filtered_cases(CASES, SimAssistant))
    if args.simtools:
        test_cases.extend(filtered_cases(CASES, SimTool))

    pytest_args = []
    if args.capture_no:
        pytest_args.append('-s')
    if args.verbose:
        pytest_args.append('-v')
    if args.stop:
        pytest_args.append('-x')

    serial = args.serial
    nt = args.threads
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
