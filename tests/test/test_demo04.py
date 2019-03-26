# -*- coding: utf-8 -*-
import test_utils


DEMO_ID = test_utils.demo_id(__file__)


def test_write_summary():
    test_utils.assert_summary_contents_by_demo(DEMO_ID)


def test_cell_count():
    test_utils.assert_face_count(DEMO_ID, 95652)


def test_vertex_count():
    test_utils.assert_vertex_count(DEMO_ID, 47826)


def test_part_surfaces_count():
    test_utils.assert_part_surfaces_count(DEMO_ID, 'radial_impeller', 5)


def test_pictures_count():
    test_utils.assert_pictures_count(DEMO_ID, 1)


def test_solution():
    test_utils.assert_iteration(DEMO_ID, 0)
