# -*- coding: utf-8 -*-
import test_utils


DEMO_ID = test_utils.demo_id(__file__)


def test_write_summary():
    test_utils.assert_summary_contents_by_demo(DEMO_ID)


def test_block1_part_surfaces_count():
    test_utils.assert_part_surfaces_count(DEMO_ID, 'Block1', 5)


def test_block2_part_surfaces_count():
    test_utils.assert_part_surfaces_count(DEMO_ID, 'Block2', 5)


def test_channel_part_surfaces_count():
    test_utils.assert_part_surfaces_count(DEMO_ID, 'Channel', 6)


def test_cell_count():
    test_utils.assert_cell_count(DEMO_ID, 11354, relative=False)


def test_solution():
    test_utils.assert_iteration(DEMO_ID, 1000)


def test_report():
    test_utils.assert_report(DEMO_ID, 'P_in', 0.0015419)


def test_scalar_min():
    test_utils.assert_scene_min(DEMO_ID, 'Scalar', 'Scalar', 0.0,
                                tolerance=1e-6, relative=False)


def test_scalar_max():
    test_utils.assert_scene_max(DEMO_ID, 'Scalar', 'Scalar', 6.0563)


def test_vector_min():
    test_utils.assert_scene_min(DEMO_ID, 'Vector', 'Vector', 0.0)


def test_vector_max():
    test_utils.assert_scene_max(DEMO_ID, 'Vector', 'Vector', 0.0821)


if __name__ == "__main__":
    pass
