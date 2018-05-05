# -*- coding: utf-8 -*-
import test_utils


DEMO_ID = test_utils.demo_id(__file__)


def test_write_summary():
    test_utils.assert_summary_contents_by_demo(DEMO_ID)


def test_domain_part_surfaces_count():
    test_utils.assert_part_surfaces_count(DEMO_ID, 'Domain', 3)


def test_cell_count():
    test_utils.assert_cell_count(DEMO_ID, 1000, tolerance=0, relative=False)


def test_solution():
    test_utils.assert_iteration(DEMO_ID, 2140)
    test_utils.assert_time(DEMO_ID, 0.0200081)


if __name__ == "__main__":
    pass
