# -*- coding: utf-8 -*-
import test_utils


DEMO_ID = test_utils.demo_id(__file__)


def test_write_summary():
    test_utils.assert_summary_contents_by_demo(DEMO_ID)


def test_cylinders_part_surfaces_count():
    for cyl in ['Main', 'In', 'Out']:
        test_utils.assert_part_surfaces_count(DEMO_ID, 'Cyl%s' % cyl, 3)


def test_unite_part_surfaces_count():
    test_utils.assert_part_surfaces_count(DEMO_ID, 'Unite', 7)


def test_cell_count():
    test_utils.assert_cell_count(DEMO_ID, 20087)


def test_solution():
    test_utils.assert_iteration(DEMO_ID, 200)


def test_pressure_scalar_min():
    test_utils.assert_scene_min(DEMO_ID, 'Scalar', 'Scalar', -10.137)


def test_pressure_scalar_max():
    test_utils.assert_scene_max(DEMO_ID, 'Scalar', 'Scalar', 14.797)


def test_streamline_min():
    test_utils.assert_scene_min(DEMO_ID, 'Scalar', 'Streamline', 0.0,
                                tolerance=0, relative=False)


def test_streamline_max():
    test_utils.assert_scene_max(DEMO_ID, 'Scalar', 'Streamline', 3.0,
                                tolerance=0, relative=False)


if __name__ == "__main__":
    pass
