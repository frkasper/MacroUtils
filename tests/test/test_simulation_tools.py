# -*- coding: utf-8 -*-
import os
import re
import test_utils


CONVERGENCE_CHECKER = 'SimToolImplicitUnsteadyConvergenceChecker'
MESH_METRICS = 'SimToolMeshMetrics'

SIM_TOOL = {

    CONVERGENCE_CHECKER: 'SimToolConvergenceChecker',

    MESH_METRICS: MESH_METRICS,

    }


def _contents_sim_tool(key, mesh_metric):

    ref_file = '%s_%s.ref' % (SIM_TOOL[mesh_metric], key)

    return test_utils._contents(ref_file)


def _load_demo(demo_id, macro_name):

    os.chdir(test_utils._test_home())

    sim_file = test_utils.simulation(demo_id)
    assert os.path.exists(sim_file), 'Demo %d needs to be run first' % demo_id

    test_utils._load_sim(sim_file, '%sTest.java' % macro_name)


def test_convergence_checker():

    _load_demo(15, CONVERGENCE_CHECKER)

    original = _contents_sim_tool('0_Original', CONVERGENCE_CHECKER)
    created = _contents_sim_tool('1_Artifacts_Created', CONVERGENCE_CHECKER)
    removed = _contents_sim_tool('2_Artifacts_Removed', CONVERGENCE_CHECKER)

    assert not re.findall('Convergence Checker', removed)
    assert original == removed

    expected = 6 * 2  # Number of Reports x 2
    fmt = '%s -> Convergence Checker'

    assert expected == len(re.findall(fmt % 'Monitor', created))
    assert expected == len(re.findall(fmt % 'Plot', created))


def test_mesh_metrics():

    _load_demo(5, MESH_METRICS)

    metric_example = 'Face Validity Part Surfaces'

    original = _contents_sim_tool('0_Original', MESH_METRICS)
    created = _contents_sim_tool('1_Artifacts_Created', MESH_METRICS)
    removed = _contents_sim_tool('2_Artifacts_Removed', MESH_METRICS)

    assert not re.findall(metric_example, original)
    assert re.findall(metric_example, created)
    assert original == removed

    plots_created = 4
    expected = plots_created * 2 + 2  # The last term is the included ones
    assert expected == len(re.findall('Plot -> ', created))
    assert plots_created == len(re.findall('Function:', created))
    assert plots_created == len(re.findall('Bins:', created))
