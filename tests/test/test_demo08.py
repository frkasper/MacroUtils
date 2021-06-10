# -*- coding: utf-8 -*-
import test_utils


DEMO_ID = test_utils.demo_id(__file__)


def test_write_summary():
    test_utils.assert_summary_contents_by_demo(DEMO_ID)


def test_wing_part_surfaces_count():
    test_utils.assert_part_surfaces_count(DEMO_ID, 'WING', 4)


def test_domain_part_surfaces_count():
    test_utils.assert_part_surfaces_count(DEMO_ID, 'Subtract', 5)


def test_cell_count():
    test_utils.assert_cell_count(DEMO_ID, 726000,
                                 tolerance=0.015, relative=True)


def test_solution():
    test_utils.assert_iteration(DEMO_ID, 200, tolerance=40, relative=False)


def test_frontal_area_report():
    test_utils.assert_report(DEMO_ID, 'Frontal Area', 0.216328)


def test_upper_area_report():
    test_utils.assert_report(DEMO_ID, 'Upper Area', 0.956046)


def test_cd_report():
    test_utils.assert_report(DEMO_ID, 'C_d', 0.190, tolerance=0.02)


def test_cl_report():
    test_utils.assert_report(DEMO_ID, 'C_l', 0.357, tolerance=0.02)


def test_scalar_pressure_coefficient_min():
    test_utils.assert_scene_min(DEMO_ID, 'Cp Wing', 'Scalar', -1.5444)


def test_scalar_pressure_kart_max():
    test_utils.assert_scene_max(DEMO_ID, 'Cp Wing', 'Scalar', 0.97297)


def test_vector_pressure_section_min():
    test_utils.assert_scene_min(DEMO_ID, 'Vector Wing', 'Vector', 0.0)


def test_vector_pressure_section_max():
    test_utils.assert_scene_max(DEMO_ID, 'Vector Wing', 'Vector', 14.64)
