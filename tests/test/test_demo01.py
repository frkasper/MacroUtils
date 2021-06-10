# -*- coding: utf-8 -*-
import test_utils


DEMO_ID = test_utils.demo_id(__file__)


def test_write_summary():
    test_utils.assert_summary_contents_by_demo(DEMO_ID)


def test_part_surfaces_count():
    test_utils.assert_part_surfaces_count(DEMO_ID, 'Cylinder', 3)


def test_cell_count():
    test_utils.assert_cell_count(DEMO_ID, 55200, tolerance=0.01, relative=True)


def test_solution():
    test_utils.assert_iteration(DEMO_ID, 120, tolerance=0.1, relative=True)


def test_report():
    test_utils.assert_report(DEMO_ID, 'Pressure Inlet', 3.845553)


def test_scalar_min():
    test_utils.assert_scene_min(DEMO_ID, 'Scalar', 'Scalar', 0, relative=False)


def test_scalar_max():
    test_utils.assert_scene_max(DEMO_ID, 'Scalar', 'Scalar', 0.129)
