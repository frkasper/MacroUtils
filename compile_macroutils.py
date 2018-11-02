#!/usr/bin/python
# -*- coding: utf-8 -*-
"""
Compile the MacroUtils project.

MacroUtils.jar file will be located under "dist" folder.

@author: Fabio Kasper
"""
import collections
import datetime
import glob
import os
import optparse
import re
import shutil


STARCCM = 'STAR-CCM+'


FIELDS = ['assistants', 'clean', 'package', 'javadoc', 'version', 'build']
OPTIONS = collections.namedtuple('Options', FIELDS)


PWD = os.getcwd()
LIBS_FOLDER = os.path.realpath(os.path.join(PWD, '../lib_release'))
assert os.path.isdir(LIBS_FOLDER), 'Not a valid lib folder: %s' % LIBS_FOLDER


def all_assistants_source_files():
    return list(filter(lambda x: is_assistant(x), all_source_files()))


def all_source_files():
    """Return a list of files in current folder, recursively"""

    except_files = '.*(LICENSE|\.(7z|class|gitignore|jar|md|mf|py|xml))$'
    except_folders = '^(.git|build|demos|dist|nbproject|temp_|tests|wiki).*'

    valid_files = []
    for root, dirs, files in os.walk(".", topdown=False):

        if re.match(except_folders, os.path.normpath(root)):
            continue

        for name in files:

            relative_path = os.path.join(root, name)

            if not re.match(except_files, relative_path):
                valid_files.append(relative_path)

    return valid_files


def assert_workspace(parser):
    """Make sure script is working in a valid workspace"""

    # Assert ant is accessible
    if which('ant') is None:
        parser.error('ant must be accessible under PATH')

    # Assert is in the right folder
    is_repository = re.match('MacroUtils', os.path.split(PWD)[-1])
    have_demos = os.path.isdir(os.path.join(PWD, 'demos'))
    has_source = os.path.isdir(macroutils_source_folder())

    if not (is_repository and have_demos and has_source):
        parser.error('Invalid MacroUtils folder: "%s"' % PWD)


def assistant_demo16_files():
    """Return a list of files corresponding to this Simulation Assistant"""
    valid_files = all_assistants_source_files()
    all_demo16 = [f for f in valid_files if re.search('emo16', f)]
    return [f for f in all_demo16 if not re.search('templates', f)]


def assistant_name(java_file):
    """Return the class name for the Simulation Assistant java file"""

    assert os.path.isfile(java_file), "File does not exist: %s" % java_file

    _, file_name = os.path.split(java_file)
    return os.path.splitext(file_name)[0]


def assistant_temp_folder(class_name):
    """Refresh Simulation Assistant temporary folder"""

    assistant_folder = os.path.join(PWD, 'temp_%s' % class_name)

    if os.path.isdir(assistant_folder):
        shutil.rmtree(assistant_folder)

    if not os.path.isdir(assistant_folder):
        os.mkdir(assistant_folder)

    return assistant_folder


def assistant_simple_hexa_mesher_files():
    """Return a list of files corresponding to this Simulation Assistant"""

    demo16_files = assistant_demo16_files()
    return [f for f in all_assistants_source_files() if f not in demo16_files]


def compile_and_package_assistant(assistant_name, options):
    """Compile the Simulation Assistant"""

    assistant_folder = assistant_temp_folder(assistant_name)

    if assistant_name == 'Demo16':
        included_files = assistant_demo16_files()
    elif assistant_name == 'SimpleHexaMesher':
        included_files = assistant_simple_hexa_mesher_files()
    else:
        raise NotImplementedError('Invalid class: %s' % assistant_name)

    for included_file in included_files:

        dropped_one_folder = os.path.relpath(included_file, 'simassistants')
        destination = os.path.join(assistant_folder, dropped_one_folder)

        print included_file
        print destination
        print ''

        try:
            shutil.copy(included_file, destination)
        except IOError:
            base_folder, _ = os.path.split(destination)
            os.makedirs(base_folder)
            shutil.copy(included_file, destination)

    os.chdir(assistant_folder)

    os.symlink(os.path.join(PWD, 'manifest.mf'), 'manifest.mf')
    os.symlink(macroutils_source_folder(base_folder=True), 'src')
    os.symlink(LIBS_FOLDER, 'libs')

    write_build_xml(assistant_name)

    run_ant()

    packaged_files = ['%s.jar' % assistant_name]
    package_dist_folder(assistant_folder, assistant_name, packaged_files,
                        options, prefix='SimulationAssistant_')

    os.chdir(PWD)

    shutil.rmtree(assistant_folder)


def execute(options):
    """Execute this script"""

    assistant_files = glob.glob('simassistants/*.java')

    if options.clean:

        run_ant('clean clean')
        packages_folder(remove=True)

        for af in assistant_files:
            folder = assistant_temp_folder(assistant_name(af))
            shutil.rmtree(folder)

        return

    update_project_properties(options)
    write_manifest_file(options)

    if options.assistants:

        for i, af in enumerate(assistant_files):

            compile_and_package_assistant(assistant_name(af), options)

    elif options.package:

        run_ant('rebuild clean jar')
        run_ant('javadoc javadoc')

        packaged_files = ['MacroUtils.jar', 'javadoc']
        package_dist_folder(PWD, 'MacroUtils', packaged_files, options)

    elif options.javadoc:

        run_ant('javadoc javadoc')

    else:

        run_ant('rebuild clean jar')


def is_assistant(java_file):
    return bool(re.match('^simassistants.*', os.path.relpath(java_file)))


def macroutils_source_folder(base_folder=False):
    """Return the path for the MacroUtils source folder"""
    source_folder = os.path.join(PWD, 'src')

    if base_folder:
        return source_folder

    return os.path.join(source_folder, 'macroutils')


def package_dist_folder(base_folder, zip_base_name, zipped_files, options,
                        prefix=''):
    """Package a 7-zip file"""

    assert isinstance(zipped_files, list), "Must be a list of files"
    os.chdir(base_folder)

    say('Packaging "dist" subfolder')
    assert os.path.isdir('dist'), "Review compilation for errors"

    os.chdir('dist')
    assert which('7z'), "7-zip tool not found. Packaging is not possible."

    for item in zipped_files:
        assert os.path.exists(item), 'File/Folder does not exist: %s' % item

    name = '%s_v%s_%s.7z' % (zip_base_name, options.version, options.build)
    zip_file = '%s%s' % (prefix, name.replace('-', '_'))

    os.system('7z a -mx5 %s %s' % (zip_file, ' '.join(zipped_files)))
    assert os.path.exists(zip_file), "Could not package 7-zip file"

    say('7-zip file written: "%s"' % zip_file)

    package_folder = os.path.realpath(packages_folder(create=True))
    shutil.move(zip_file, os.path.join(package_folder, zip_file))

    os.chdir(PWD)


def packages_folder(create=False, remove=False):
    """Return the path to compiled/compressed files"""

    folder = os.path.join(PWD, 'packages')

    if create and not os.path.isdir(folder):
        os.mkdir(folder)

    if remove and os.path.isdir(folder):
        shutil.rmtree(folder)

    return folder


def parse_options():
    """Parse argument options and return a namedtuple"""

    usage = '\n'.join([
            '%prog [options]',
            '',
            'Compile MacroUtils project using "ant".'
            ])
    parser = optparse.OptionParser(usage)

    parser.add_option('-a', '--assistants',
                      dest='assistants',
                      action='store_true',
                      help='compile simulation assistants only',
                      default=False)
    parser.add_option('-c', '--clean',
                      dest='clean',
                      action='store_true',
                      help='clean compiled folders only',
                      default=False)
    parser.add_option('-j', '--javadoc',
                      dest='javadoc',
                      action='store_true',
                      help='build javadoc',
                      default=False)
    parser.add_option('-p', '--package',
                      dest='package',
                      action='store_true',
                      help='package MacroUtils contents into .7z files',
                      default=False)

    opts, args = parser.parse_args()

    assistants = opts.assistants
    clean = opts.clean
    javadoc = opts.javadoc
    package = opts.package

    if sum([assistants, clean, javadoc, package]) > 1:
        parser.error('Options are mutually exclusive')

    if package:
        javadoc = True

    assert_workspace(parser)

    version = starccm_version()
    build = 'build-%s' % datetime.datetime.now().strftime('%Y%m%d')

    return OPTIONS(assistants, clean, package, javadoc, version, build)


def run_ant(action=None):
    """Run default NetBeans build tool"""

    nb_action = '-Dnb.internal.action.name=%s' % action
    command = ' '.join([
            'ant',
            # '-verbose',
            nb_action if action is not None else '',
            ])

    say('Executing: "%s"' % command)
    os.system(command)


def say(message):
    """Print a custom message"""

    print('#\n# %s\n#\n' % message)


def starccm_version():
    """Parse and return the current STAR-CCM+ version from MacroUtils.java"""

    main_java = os.path.join(macroutils_source_folder(), 'MacroUtils.java')

    with open(main_java, 'r') as f:
        data = f.read()

    found = re.findall('@version\sv(.*)\n', data)
    assert len(found) == 1, "Could not parse %s version" % STARCCM

    return found[0]


def update_project_properties(options):
    """Update project.properties in NetBeans, if applicable"""

    nbproject_dir = os.path.join(PWD, 'nbproject')
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


def write_build_xml(class_name):
    contents = '''
    <project name="__CLASS_NAME__" default="package" basedir=".">

        <description>

            Scripted ant build file

        </description>

        <property name="build.dir" location="build"/>

        <property name="dist.dir" location="dist"/>

        <path id="classpath">

            <fileset dir="${basedir}/" includes="**/*.jar"/>

        </path>

        <target name="init">

            <mkdir dir="${build.dir}"/>

        </target>

        <target name="compile" depends="init">

            <javac includeantruntime="false"
                   srcdir="${basedir}"
                   destdir="${build.dir}"
                   classpathref="classpath"
                   />

        </target>

        <target name="package" depends="compile">

            <mkdir dir="${dist.dir}"/>

            <jar destfile="${dist.dir}/__CLASS_NAME__.jar"
                 manifest="manifest.mf">

                <fileset dir="${basedir}" casesensitive="yes">

                    <patternset id="resources">

                        <include name="**/*.jpg"/>
                        <include name="**/*.xhtml"/>

                    </patternset>

                </fileset>

                <fileset dir="${build.dir}" casesensitive="yes"/>

            </jar>

        </target>

        <target name="clean" description="clean up">

            <delete dir="${build.dir}"/>

            <delete dir="${dist.dir}"/>

        </target>

    </project>
    '''.replace('__CLASS_NAME__', class_name)

    this_folder = os.path.basename(os.getcwd().replace('temp_', ''))
    assert this_folder == class_name, "Not on right folder: %s" % os.getcwd()

    with open('build.xml', 'w') as f:
        f.write('%s\n' % contents)


def write_manifest_file(options):
    """Write the manifest file before compiling the project"""

    say('Writing Manifest file: "manifest.mf"')

    contents = [
            'Manifest-Version: 1.1',
            'X-COMMENT: Main-Class will be added automatically by build',
            'Specification-Title: MacroUtils',
            'Specification-Version: %s' % options.build,
            'Implementation-Title: %s' % STARCCM,
            'Implementation-Version: %s' % options.version,
            ]

    with open(os.path.join(PWD, 'manifest.mf'), 'w') as f:
        f.write('%s\n' % '\n'.join(contents))


if __name__ == "__main__":

    options = parse_options()

    execute(options)
