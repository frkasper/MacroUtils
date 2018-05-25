# -*- coding: utf-8 -*-
"""
Created on Fri May 11 16:27:42 2018

@author: fabiok
"""
import glob
import os
import test_utils


def _assert_opacity_bug_14(key, expected):
    re_patt = 'Scene -> \w+ -> \w+ -> %s Opacity:\s(.*)\n' % key
    actual = _float(re_patt, _bug_sim(1))
    test_utils.assert_value(actual, expected, tolerance=0)


def _bug_sim(demo_id):
    return 'Bug_%s' % _sim(demo_id)


def _clean_up(demo_id):
    os.chdir(test_utils._test_home())
    files = glob.glob('%s.*' % os.path.splitext(_bug_sim(demo_id))[0])
    files.append(test_utils._summary_file(_bug_sim(demo_id)))
    test_utils._remove(files)


def _float(re_patt, sim_file):
    contents = test_utils.summary_contents(sim_file=sim_file)
    return test_utils._float_from_contents(re_patt, contents)


def _load_demo(demo_id, macro_name):
    _clean_up(demo_id)
    test_utils._load_sim(_sim(demo_id), '%s.java' % macro_name)


def _new_sim(macro_name):
    test_utils._new_sim('%s.java' % macro_name)


def _sim(demo_id):
    sim_file = test_utils.simulation(demo_id)
    assert os.path.exists(sim_file), 'Demo %d needs to be run first' % demo_id
    return sim_file


def test_bug_14():
    _load_demo(1, 'BugCreateStreamlineSceneTest')
    bug_sim = _bug_sim(1)
    _assert_opacity_bug_14('Part', 0.2)
    _assert_opacity_bug_14('Scalar', 1.0)
    _assert_opacity_bug_14('Streamline', 1.0)
    assert os.path.exists(bug_sim.replace('.sim', '.png'))


def test_bug_19():
    file_name = 'Block 95.2 mm^3.sim'
    scene_name = 'Block 3.4 x 4.0 x 7.0 mm^3.png'
    _new_sim('BugWritingFilenamesTest')
    assert os.path.exists(file_name.replace(' ', '_'))
    assert os.path.exists(scene_name.replace(' ', '_'))


if __name__ == "__main__":
    pass
