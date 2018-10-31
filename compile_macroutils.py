#!/usr/bin/python
# -*- coding: utf-8 -*-
"""
Compile the MacroUtils project.

MacroUtils.jar file will be located under "dist" folder.

@author: Fabio Kasper
"""
import collections
import datetime
import os
import optparse
import re


FIELDS = ['clean', 'package', 'javadoc', 'version', 'build']
OPTIONS = collections.namedtuple('Options', FIELDS)
STARCCM = 'STAR-CCM+'


def assert_workspace(parser):
    """Make sure script is working in a valid workspace"""

    # Assert ant is accessible
    if which('ant') is None:
        parser.error('ant must be accessible under PATH')

    # Assert is in the right folder
    pwd = os.getcwd()
    macroutils = re.match('MacroUtils', os.path.split(pwd)[-1])
    demos = os.path.isdir(os.path.join(pwd, 'demos'))
    src = os.path.isdir(os.path.join(pwd, 'src'))

    if not (macroutils and demos and src):
        parser.error('Invalid MacroUtils folder: "%s"' % pwd)


def execute(options):
    """Execute this script"""

    if options.clean:
        run_ant('clean clean')
        return

    update_project_properties(options)

    update_manifest_file(options)

    if options.package:
        run_ant('rebuild clean jar')
        run_ant('javadoc javadoc')
        package_dist(options)
    elif options.javadoc:
        run_ant('javadoc javadoc')
    else:
        run_ant('rebuild clean jar')


def package_dist(options):
    """Package a 7-zip file"""

    say('Packaging "dist" subfolder')
    assert os.path.isdir('dist'), "Folder does not exist"

    os.chdir('dist')
    assert which('7z'), "7-zip tool not found. Packaging is not possible."

    filename = 'MacroUtils_v%s_%s.7z' % (options.version, options.build)
    zip_file = filename.replace('-', '_')
    os.system('7z a -mx5 %s MacroUtils.jar javadoc' % zip_file)
    assert os.path.exists(zip_file), "Could not package 7-zip file"

    say('7-zip file written: "%s"' % zip_file)


def parse_options():
    """Parse argument options and return a namedtuple"""

    usage = '\n'.join([
            '%prog [options]',
            '',
            'Compile MacroUtils project using "ant".'
            ])
    parser = optparse.OptionParser(usage)

    parser.add_option('-c', '--clean',
                      dest='clean',
                      action='store_true',
                      help='clean dist folder',
                      default=False)
    parser.add_option('-j', '--javadoc',
                      dest='javadoc',
                      action='store_true',
                      help='build javadoc',
                      default=False)
    parser.add_option('-p', '--package',
                      dest='package',
                      action='store_true',
                      help='package MacroUtils into a .7z file',
                      default=False)

    opts, args = parser.parse_args()

    clean = opts.clean
    javadoc = opts.javadoc
    package = opts.package

    if sum([clean, javadoc, package]) > 1:
        parser.error('Options are mutually exclusive')

    if package:
        javadoc = True

    if clean:
        javadoc = package = False

    assert_workspace(parser)

    version = starccm_version()
    build = 'build-%s' % datetime.datetime.now().strftime('%Y%m%d')

    return OPTIONS(clean, package, javadoc, version, build)


def run_ant(action):
    """Run default NetBeans build tool"""

    command = ' '.join([
            'ant',
            # '-verbose',
            '-f %s' % os.getcwd(),
            '-Dnb.internal.action.name=%s' % action,
            ])

    say('Executing: "%s"' % command)
    os.system(command)


def say(message):
    """Print a custom message"""

    print('#\n# %s\n#\n' % message)


def starccm_version():
    """Parse and return the current STAR-CCM+ version from MacroUtils.java"""

    src_subfolder = os.path.join('src', 'macroutils')
    src_path = os.path.join(os.getcwd(), src_subfolder)
    macroutils_java = os.path.join(src_path, 'MacroUtils.java')

    with open(macroutils_java, 'r') as f:
        data = f.read()

    found = re.findall('@version\sv(.*)\n', data)
    assert len(found) == 1, "Could not parse %s version" % STARCCM

    return found[0]


def update_manifest_file(options):
    """Update the manifest file before compiling the project"""

    say('Updating Manifest file: "manifest.mf"')

    contents = [
            'Manifest-Version: 1.1',
            'X-COMMENT: Main-Class will be added automatically by build',
            'Specification-Title: MacroUtils',
            'Specification-Version: %s' % options.build,
            'Implementation-Title: %s' % STARCCM,
            'Implementation-Version: %s' % options.version,
            ]

    with open(os.path.join(os.getcwd(), 'manifest.mf'), 'w') as f:
        f.write('%s\n' % '\n'.join(contents))


def update_project_properties(options):
    """Update project.properties in NetBeans, if applicable"""

    nbproject_dir = os.path.join(os.getcwd(), 'nbproject')
    if not os.path.isdir(nbproject_dir):
        return

    say('Updating NetBeans file: "project.properties"')

    keys_and_values = [
            'javadoc.additionalparam=',
            'javadoc.author=true',
            'javadoc.encoding=${source.encoding}',
            'javadoc.noindex=false',
            'javadoc.nonavbar=false',
            'javadoc.notree=false',
            'javadoc.private=false',
            'javadoc.splitindex=true',
            'javadoc.use=true',
            'javadoc.version=true',
            'javadoc.windowtitle=MacroUtils v%s' % options.version,
            ]

    # First read original file
    project_properties = os.path.join(nbproject_dir, 'project.properties')
    with open(project_properties, 'r') as f:
        lines = f.readlines()

    # Then replace keys in list
    wanted_keys = [x.split('=')[0] for x in keys_and_values]
    for i, line in enumerate(lines):
        if not re.search('=', line):
            continue
        this_key = line.split('=')[0]
        if this_key in wanted_keys:
            idx_wanted_key = wanted_keys.index(this_key)
            lines[i] = '%s\n' % keys_and_values[idx_wanted_key]

    # Finally rewrite file
    with open(project_properties, 'w') as g:
        g.writelines(lines)


def which(program):
    """
    Return the path for a command

    Source: stackoverflow.com -- question #377017
    """

    def is_executable(filepath):
        return os.path.isfile(filepath) and os.access(filepath, os.X_OK)

    filepath, filename = os.path.split(program)

    if filepath:

        if is_executable(program):
            return program

    else:
        for path in os.environ["PATH"].split(os.pathsep):

            candidate = os.path.join(path, program)

            if is_executable(candidate):
                return candidate

    return None


if __name__ == "__main__":

    options = parse_options()

    execute(options)
