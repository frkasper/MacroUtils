# -*- coding: utf-8 -*-
"""
Common methods w.r.t. testing.

Quantitative tolerances policy:
    - Relative tolerances starts at:
        - 0.5% for mesh count, i.e., volume or triangulated data
    - Absolute tolerance of zero for integer count

Created on Fri Apr 27 12:47:46 2018

@author: Fabio Kasper
"""
import glob
import os
import re
import common.star as star


def _assert_content_count(key, argument, expected, tolerance, relative):
    actual = _float_from_argument(r'%s Count:\s(.*)\n' % key, argument)
    print('\n[assert %s Count]: ' % key),
    assert_value(actual, expected, tolerance, relative)


def _assert_exists(filename):
    assert os.path.exists(filename), 'File not found: "%s"' % filename


def _assert_report(argument, report_name, expected, tolerance, relative):
    re_patt = r'Report -> %s:\s(.*)\n' % report_name
    actual = _float_from_argument(re_patt, argument)
    print('\n[assert Report] %s: ' % report_name),
    assert_value(actual, expected, tolerance, relative)


def _assert_scene(argument, scene, displayer, expected, tol, relative, key):
    re_patt = r'Scene -> %s -> \w+ -> %s %s:\s(.*)\n' % (scene, displayer, key)
    actual = _float_from_argument(re_patt, argument)
    print('\n[assert Scene] %s -> %s: ' % (scene, displayer)),
    assert_value(actual, expected, tol, relative)


def _base_name(filename):
    return os.path.splitext(filename)[0]


def _contents(filename):
    with open(filename, 'r') as f:
        data = f.read()
    return data


def _env_var(value):
    env_var = os.getenv(value)
    assert env_var is not None, '%s environemntal variable not defined' % value
    return env_var


def _float_from_argument(re_patt, argument):
    if isinstance(argument, int):
        return _float_from_demo(re_patt, argument)
    elif isinstance(argument, str):
        return _float_from_sim_file(re_patt, argument)
    assert False, 'Invalid argument: "%s"' % str(argument)


def _float_from_contents(re_patt, contents):
    found = re.findall(re_patt, contents)
    assert len(found) > 0, 'Pattern not found: "%s"' % re_patt
    return float(found[0])


def _float_from_demo(re_patt, demo_number):
    contents = summary_contents(demo_number=demo_number, overwrite=False)
    return _float_from_contents(re_patt, contents)


def _float_from_sim_file(re_patt, sim_file):
    contents = summary_contents(sim_file=sim_file, overwrite=False)
    return _float_from_contents(re_patt, contents)


def _glob(pattern, validate=True):
    """Make sure glob is always done at TESTHOME"""
    os.chdir(_test_home())
    found = glob.glob(pattern)
    if validate:
        assert len(found) > 0, 'File not found: "%s"' % pattern
    return found


def _int_from_argument(re_patt, argument):
    return int(_float_from_argument(re_patt, argument))


def _load_sim(sim_file, macro_file):
    _run_sim(sim_file=sim_file, macro_file=macro_file)


def _new_sim(macro_file):
    _run_sim(sim_file=None, macro_file=macro_file)


def _pair(key, value, fmt='%g', multiplier=1.0):
    return '%s = %s' % (key, fmt % (multiplier * value))


def _percentage(key, value):
    if value < 1e-6:
        fmt = '%.2e%%'
    elif value < 1e-4:
        fmt = '%.2f%%'
    else:
        fmt = '%.1f%%'
    return _pair(key, value, fmt, multiplier=100.0)


def _remove(filename):
    if isinstance(filename, list):
        for f in filename:
            _remove(f)
    else:
        if os.path.exists(filename):
            os.remove(filename)


def _run_sim(sim_file=None, macro_file=None):
    os.chdir(_test_home())
    sh, np, is_batch = _star_home(), 1, True
    if sim_file is None:
        star_cmd = star.new_simulation(sh, macro_file, np, is_batch)
    else:
        star_cmd = star.load_simulation(sh, sim_file, macro_file, np, is_batch)
    os.system(star_cmd)


def _star_home():
    return _env_var('STARHOME')


def _summary_file(sim_file):
    return 'Summary_%s.ref' % _base_name(sim_file)


def _summary_macro():
    macro = 'WriteSummaryTest.java'
    assert os.path.exists(macro), 'Macro not found: "%s"' % macro
    return macro


def _test_home():
    return _env_var('TESTHOME')


def _write_summary(sim_file, ref_file):
    _remove(ref_file)
    _load_sim(sim_file, _summary_macro())
    _assert_exists(ref_file)


def assert_cell_count(argument, expected, tolerance=0.005, relative=True):
    _assert_content_count('Cell', argument, expected, tolerance, relative)


def assert_face_count(argument, expected, tolerance=0.005, relative=True):
    _assert_content_count('Face', argument, expected, tolerance, relative)


def assert_file_size(filename, expected, tolerance=0.001, relative=True):
    actual = os.path.getsize(filename)
    print('\n[assert on file size "%g"]: ' % actual),
    assert_value(actual, expected, tolerance, relative)


def assert_files_count(glob_pattern, expected, tolerance=0, relative=False):
    actual = len(_glob(glob_pattern, validate=False))
    print('\n[assert on glob pattern "%s"]: ' % glob_pattern),
    assert_value(actual, expected, tolerance, relative)


def assert_pictures_count(demo_number, expected):
    assert_files_count('Demo%d_*.png' % demo_number, expected)


def assert_pictures_count_in_folder(glob_pattern, expected):
    assert_files_count(glob_pattern, expected)


def assert_part_surfaces_count(argument, part_name, expected, tolerance=0,
                               relative=False):
    actual = _float_from_argument(r'Part -> %s:\s(\d+) Part' % part_name,
                                  argument)
    print('\n[assert Part Surfaces Count]: '),
    assert_value(actual, expected, tolerance, relative)


def assert_iteration(argument, expected, tolerance=0.0, relative=False):
    actual = _int_from_argument(r'Iteration:\s(\d+)', argument)
    assert_value(actual, expected, tolerance, relative)


def assert_report(argument, report_name, expected, tolerance=0.01,
                  relative=True):
    _assert_report(argument, report_name, expected, tolerance, relative)


def assert_scene_min(argument, scene_name, displayer_name, expected,
                     tolerance=0.01, relative=True):
    _assert_scene(argument, scene_name, displayer_name, expected, tolerance,
                  relative, 'MIN')


def assert_scene_max(argument, scene_name, displayer_name, expected,
                     tolerance=0.01, relative=True):
    _assert_scene(argument, scene_name, displayer_name, expected, tolerance,
                  relative, 'MAX')


def assert_summary_contents_by_demo(number):
    contents = summary_contents(demo_number=number, overwrite=True)
    assert len(contents) > 0


def assert_summary_contents_by_sim_file(filename):
    contents = summary_contents(sim_file=filename, overwrite=True)
    assert len(contents) > 0


def assert_time(argument, expected, tolerance=0.0001, relative=True):
    actual = _float_from_argument(r'Time:\s(.*)\n', argument)
    assert_value(actual, expected, tolerance, relative)


def assert_value(actual, expected, tolerance=0.01, relative=True):
    '''Assert a quantitative value, relative or absolute'''
    error = actual - expected
    if (relative and expected == 0.0) or error == 0.0:
        relative = False  # Revert to absolute
    if relative:
        assert tolerance > 0, 'Relative tolerance must be positive'
        error /= float(expected)
        s_error = _percentage('error', error)
        s_tol = _percentage('tolerance', tolerance)
    else:
        assert tolerance >= 0, 'Tolerance must be equal or higher than zero'
        s_error = _pair('error', error)
        s_tol = 'tolerance = %g' % tolerance

    message = '%s; %s; %s; %s' % (_pair('actual', actual),
                                  _pair('expected', expected),
                                  s_error, s_tol)
    print(message)
    assert abs(error) <= tolerance, message


def assert_vertex_count(argument, expected, tolerance=0.005, relative=True):
    _assert_content_count('Vertex', argument, expected, tolerance, relative)


def demo_id(filename):
    """Get the demo_number from a test macro"""
    b1 = re.match(r'.*/test_demo\d{2}\.py', filename)
    found = re.findall(r'.*/test_demo(\d{2})\.py', filename)
    b2 = len(found) == 1
    is_test_file = b1 and b2
    assert is_test_file, 'Not a test file: "%s"' % filename
    return int(found[0])


def simulation(demo_number):
    """Get the latest/newest sim file related to a demo number"""
    return simulations(demo_number)[-1]


def simulations(demo_number):
    """Get a list of sim files related to a demo ordered by creation date"""
    return sorted(_glob('Demo%d_*.sim' % demo_number), key=os.path.getctime)


def summary_contents(demo_number=None, sim_file=None, overwrite=False):
    """Return a list of strings"""
    check_syntax = sum([bool(demo_number), bool(sim_file)])
    if check_syntax == 0:
        raise SyntaxError('Provide at least a demo_number or sim_file')
    elif check_syntax == 2:
        raise SyntaxError('Provide a demo_number or sim_file not both')
    os.chdir(_test_home())
    if demo_number is not None:
        sim_file = simulation(demo_number)
    ref_file = _summary_file(sim_file)
    if overwrite:
        _write_summary(sim_file, ref_file)
    contents = _contents(ref_file)
    return contents


if __name__ == "__main__":
    pass
