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
    test_utils.assert_cell_count(DEMO_ID, 593235)


def test_solution():
    test_utils.assert_iteration(DEMO_ID, 209)


def test_frontal_area_report():
    test_utils.assert_report(DEMO_ID, 'Frontal Area', 0.002692)


def test_cd_report():
    test_utils.assert_report(DEMO_ID, 'C_d', 0.6425)


def test_cl_report():
    test_utils.assert_report(DEMO_ID, 'C_l', 0.3443)


def test_scalar_pressure_kart_min():
    test_utils.assert_scene_min(DEMO_ID, 'Pressure Kart', 'Scalar', -1.9607)


def test_scalar_pressure_kart_max():
    test_utils.assert_scene_max(DEMO_ID, 'Pressure Kart', 'Scalar', 1.1189)


def test_scalar_pressure_section_min():
    test_utils.assert_scene_min(DEMO_ID, 'Pressure Section', 'Scalar', -0.7279)


def test_scalar_pressure_section_max():
    test_utils.assert_scene_max(DEMO_ID, 'Pressure Section', 'Scalar', 1.0527)


def test_vector_pressure_section_min():
    test_utils.assert_scene_min(DEMO_ID, 'Vector Section', 'Vector', 0.0)


def test_vector_pressure_section_max():
    test_utils.assert_scene_max(DEMO_ID, 'Vector Section', 'Vector', 56.83)


if __name__ == "__main__":
    pass
