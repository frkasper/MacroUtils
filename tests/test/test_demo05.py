# -*- coding: utf-8 -*-
import test_utils


DEMO_ID = test_utils.demo_id(__file__)


def test_write_summary():
    test_utils.assert_summary_contents_by_demo(DEMO_ID)


def test_tunnel_part_surfaces_count():
    test_utils.assert_part_surfaces_count(DEMO_ID, 'Tunnel', 4)


def test_kart_wrap_part_surfaces_count():
    test_utils.assert_part_surfaces_count(DEMO_ID, 'Kart Wrap', 91)


def test_cell_count():
    test_utils.assert_cell_count(DEMO_ID, 621000, tolerance=0.01)


def test_solution():
    test_utils.assert_iteration(DEMO_ID, 250, tolerance=100, relative=False)


def test_frontal_area_report():
    test_utils.assert_report(DEMO_ID, 'Frontal Area', 0.002692)


def test_cd_report():
    test_utils.assert_report(DEMO_ID, 'C_d', 0.65,
                             tolerance=0.01, relative=False)


def test_cl_report():
    test_utils.assert_report(DEMO_ID, 'C_l', 0.35,
                             tolerance=0.01, relative=False)


def test_scalar_pressure_kart_min():
    test_utils.assert_scene_min(DEMO_ID, 'Pressure Kart', 'Scalar', -2.0,
                                tolerance=0.2, relative=False)


def test_scalar_pressure_kart_max():
    test_utils.assert_scene_max(DEMO_ID, 'Pressure Kart', 'Scalar', 5.2,
                                tolerance=0.1, relative=True)


def test_scalar_pressure_section_min():
    test_utils.assert_scene_min(DEMO_ID, 'Pressure Section', 'Scalar', -0.8,
                                tolerance=0.1, relative=True)


def test_scalar_pressure_section_max():
    test_utils.assert_scene_max(DEMO_ID, 'Pressure Section', 'Scalar', 5.3,
                                tolerance=0.1, relative=True)


def test_vector_velocity_section_min():
    test_utils.assert_scene_min(DEMO_ID, 'Vector Section', 'Vector', 0.0)


def test_vector_velocity_section_max():
    test_utils.assert_scene_max(DEMO_ID, 'Vector Section', 'Vector', 56.83)
