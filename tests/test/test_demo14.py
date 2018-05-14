# -*- coding: utf-8 -*-
import re
import test_utils


DEMO_ID = test_utils.demo_id(__file__)


def _assert_report(grid_file, report_name, expected, tolerance=0.005,
                   relative=True):
    re_patt = 'Report -> %s:\s(.*)\n' % report_name
    actual = test_utils._float_from_sim_file(re_patt, grid_file)
    print('\n[assert Report] %s: ' % report_name),
    test_utils.assert_value(actual, expected, tolerance, relative)


def _assert_summary_contents(grid_number):
    test_utils.assert_summary_contents_by_sim_file(_grid(grid_number))


def _grid(grid_number):
    sfs = test_utils.simulations(DEMO_ID)
    sf_grid = [sf for sf in sfs if re.search('Grid%03d' % grid_number, sf)]
    assert len(sf_grid) == 1, 'Invalid grid files: %s' % str(sfs)
    return sf_grid[0]


def test_write_summary_grid_001():
    _assert_summary_contents(1)


def test_cylinder_part_surfaces_count():
    test_utils.assert_part_surfaces_count(_grid(1), 'Cylinder', 3)


def test_cell_count_grid_001():
    test_utils.assert_cell_count(_grid(1), 870, relative=False)


def test_vmean_report_grid_001():
    _assert_report(_grid(1), 'Vmean', 9.365380e-02)


def test_vmax_report_grid_001():
    _assert_report(_grid(1), 'Vmax', 1.383828e-01)


def test_write_summary_grid_002():
    _assert_summary_contents(2)


def test_cell_count_grid_002():
    test_utils.assert_cell_count(_grid(2), 2650, relative=False)


def test_vmean_report_grid_002():
    _assert_report(_grid(2), 'Vmean', 1.030953e-01)


def test_vmax_report_grid_002():
    _assert_report(_grid(2), 'Vmax', 1.544657e-01)


def test_write_summary_grid_003():
    _assert_summary_contents(3)


def test_cell_count_grid_003():
    test_utils.assert_cell_count(_grid(3), 9290, relative=False)


def test_solution_grid_003():
    test_utils.assert_iteration(_grid(3), 8352)


def test_vmean_report_grid_003():
    _assert_report(_grid(3), 'Vmean', 1.040143e-01)


def test_vmax_report_grid_003():
    _assert_report(_grid(3), 'Vmax', 1.560768e-01)


if __name__ == "__main__":
    pass
