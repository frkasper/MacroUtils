# -*- coding: utf-8 -*-
import test_utils


DEMO_ID = test_utils.demo_id(__file__)


def _grid_des():
    return _sim_files()[2]


def _grid_ss():
    return _sim_files()[0]


def _sim_files():
    sfs = test_utils.simulations(DEMO_ID)
    assert len(sfs) == 3, 'Invalid files: %s' % str(sfs)
    return sfs


def test_write_summary_ss():
    test_utils.assert_summary_contents_by_sim_file(_grid_ss())


def test_unite_part_surfaces_count():
    test_utils.assert_part_surfaces_count(_grid_ss(), 'Unite', 11)


def test_cell_count_ss():
    test_utils.assert_cell_count(_grid_ss(), 16745)


def test_solution_ss():
    test_utils.assert_iteration(_grid_ss(), 89)


def test_pressure_drop_report_ss():
    test_utils.assert_report(_grid_ss(), 'Pressure Drop', 8.0329)


def test_pressure_scalar_min_ss():
    test_utils.assert_scene_min(_grid_ss(), 'Pressure', 'Scalar', -11.22)


def test_pressure_scalar_max_ss():
    test_utils.assert_scene_max(_grid_ss(), 'Pressure', 'Scalar', 13.61)


def test_vector_min_ss():
    test_utils.assert_scene_min(_grid_ss(), 'Vector', 'Vector', 0.0059)


def test_vector_max_ss():
    test_utils.assert_scene_max(_grid_ss(), 'Vector', 'Vector', 5.3978)


def test_write_summary_des():
    test_utils.assert_summary_contents_by_sim_file(_grid_des())


def test_cell_count_des():
    test_utils.assert_cell_count(_grid_des(), 219109)


def test_solution_des():
    test_utils.assert_iteration(_grid_des(), 30000)
    test_utils.assert_time(_grid_des(), 0.5)


def test_pressure_drop_report_des():
    test_utils.assert_report(_grid_des(), 'Pressure Drop', 47.107)


def test_cfl_avg_report_des():
    test_utils.assert_report(_grid_des(), 'CFL_avg', 0.2062)


def test_cfl_max_report_des():
    test_utils.assert_report(_grid_des(), 'CFL_max', 2.296)


def test_time_report_des():
    test_utils.assert_report(_grid_des(), 'Time', 0.5, tolerance=0.0,
                             relative=False)


def test_p1_report_des():
    test_utils.assert_report(_grid_des(), 'P1', 23.4693)


def test_vector_min_des():
    test_utils.assert_scene_min(_grid_des(), 'Vector', 'Vector', 0.0097)


def test_vector_max_des():
    test_utils.assert_scene_max(_grid_des(), 'Vector', 'Vector', 6.6477)


if __name__ == "__main__":
    pass
