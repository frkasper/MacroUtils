# -*- coding: utf-8 -*-
import test_utils


DEMO_ID = test_utils.demo_id(__file__)


def test_write_summary():
    test_utils.assert_summary_contents_by_demo(DEMO_ID)


def test_sphere_part_surfaces_count():
    test_utils.assert_part_surfaces_count(DEMO_ID, 'Sphere', 1)


def test_cell_count():
    test_utils.assert_cell_count(DEMO_ID, 7000, tolerance=0.025)


def test_scalar_min():
    test_utils.assert_scene_min(DEMO_ID, 'Scalar', 'Scalar', 99.63,
                                tolerance=0.01, relative=True)


def test_scalar_max():
    test_utils.assert_scene_max(DEMO_ID, 'Scalar', 'Scalar', 99.82,
                                tolerance=0.01, relative=True)


def test_solution():
    test_utils.assert_iteration(DEMO_ID, 0)
