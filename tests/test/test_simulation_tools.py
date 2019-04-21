# -*- coding: utf-8 -*-
import os
import re
import test_utils


CONVERGENCE_CHECKER = 'SimToolImplicitUnsteadyConvergenceChecker'


def _contents_convergence_checker(key):

    return test_utils._contents('SimToolConvergenceChecker_' + key + '.ref')


def _load_demo(demo_id, macro_name):

    sim_file = test_utils.simulation(demo_id)
    assert os.path.exists(sim_file), 'Demo %d needs to be run first' % demo_id

    test_utils._load_sim(sim_file, '%sTest.java' % macro_name)


def test_convergence_checker():

    _load_demo(15, CONVERGENCE_CHECKER)

    original = _contents_convergence_checker('0_Original')
    artifacts_created = _contents_convergence_checker('1_Artifacts_Created')
    artifacts_removed = _contents_convergence_checker('2_Artifacts_Removed')

    assert not re.findall('Convergence Checker', artifacts_removed)
    assert original == artifacts_removed

    expected = 5 * 2  # Number of Reports x 2
    fmt = '%s -> Convergence Checker'

    assert expected == len(re.findall(fmt % 'Monitor', artifacts_created))
    assert expected == len(re.findall(fmt % 'Plot', artifacts_created))
