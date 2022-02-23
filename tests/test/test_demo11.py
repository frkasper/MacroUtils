# -*- coding: utf-8 -*-
import test_utils


DEMO_ID = test_utils.demo_id(__file__)


def test_write_summary():
    test_utils.assert_summary_contents_by_demo(DEMO_ID)


def test_block_part_surfaces_count():
    test_utils.assert_part_surfaces_count(DEMO_ID, 'Block', 6)


def test_cylinders_part_surfaces_count():
    for i in range(1, 10):
        test_utils.assert_part_surfaces_count(DEMO_ID, 'Cylinder%d' % i, 3)


def test_subtract_part_surfaces_count():
    test_utils.assert_part_surfaces_count(DEMO_ID, 'Subtract', 15)


def test_cell_count():
    test_utils.assert_cell_count(DEMO_ID, 165000, tolerance=0.025)


def test_scalar_min():
    test_utils.assert_scene_min(DEMO_ID, 'Scalar', 'Scalar', 0.0)


def test_scalar_max():
    test_utils.assert_scene_max(DEMO_ID, 'Scalar', 'Scalar', 9.0,
                                relative=False)


def test_solution():
    test_utils.assert_iteration(DEMO_ID, 0)
