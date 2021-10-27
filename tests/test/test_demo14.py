# -*- coding: utf-8 -*-
import re
import test_utils


DEMO_ID = test_utils.demo_id(__file__)


def _assert_report(grid_file, report_name, expected, tolerance=0.005,
                   relative=True):
    re_patt = r'Report -> %s:\s(.*)\n' % report_name
    actual = test_utils._float_from_sim_file(re_patt, grid_file)
    print('\n[assert Report] %s: ' % report_name),
    test_utils.assert_value(actual, expected, tolerance, relative)


def _assert_summary_contents(grid_number):
    test_utils.assert_summary_contents_by_sim_file(_grid(grid_number))


def _gci_coefficients():
    """Reconstruct GCI23 values from Plot"""
    num_vals = _values_from_plot(r'.*Numerical .. Y values:\s\[(.*)\]')
    gci_all_vals = _values_from_plot(r'.*GCI23 .. Y values:\s\[(.*)\]')
    gci_vals = gci_all_vals[::2]
    assert len(num_vals) == len(gci_vals)
    return [xs / x - 1.0 for xs, x in zip(gci_vals, num_vals)]


def _grid(grid_number):
    sfs = test_utils.simulations(DEMO_ID)
    sf_grid = [sf for sf in sfs if re.search('Grid%03d' % grid_number, sf)]
    assert len(sf_grid) == 1, 'Invalid grid files: %s' % str(sfs)
    return sf_grid[0]


def _ref_file_gci():
    sim_file = test_utils.simulation(DEMO_ID)
    return test_utils._summary_file(sim_file)


def _values_from_plot(re_patt):
    contents = test_utils._contents(_ref_file_gci())
    s_vals = re.findall('Plot -> %s' % re_patt, contents)
    assert len(s_vals) < 2, 'Parsed too many chunk of values: %d' % len(s_vals)
    assert len(s_vals) > 0, 'Could not parse values. Found: %s' % str(s_vals)
    vals = [float(x) for x in s_vals[0].split(',')]
    return vals


def _vmax():
    """Analytical solution; Vmax = 0.15625 m/s"""
    dPdL = 1.0
    R = 0.025
    visc = 0.001
    return dPdL / (4 * visc) * pow(R, 2)


def test_write_summary_grid_001():
    _assert_summary_contents(1)


def test_cylinder_part_surfaces_count():
    test_utils.assert_part_surfaces_count(_grid(1), 'Cylinder', 3)


def test_cell_count_grid_001():
    test_utils.assert_cell_count(_grid(1), 900,
                                 tolerance=50, relative=False)


def test_vmean_report_grid_001():
    _assert_report(_grid(1), 'Vmean', 0.09, tolerance=0.1, relative=True)


def test_vmax_report_grid_001():
    _assert_report(_grid(1), 'Vmax', 0.14, tolerance=0.1, relative=True)


def test_write_summary_grid_002():
    _assert_summary_contents(2)


def test_cell_count_grid_002():
    test_utils.assert_cell_count(_grid(2), 2625,
                                 tolerance=25, relative=False)


def test_vmean_report_grid_002():
    _assert_report(_grid(2), 'Vmean', 0.1, tolerance=0.05, relative=True)


def test_vmax_report_grid_002():
    _assert_report(_grid(2), 'Vmax', 0.15, tolerance=0.05, relative=True)


def test_write_summary_grid_003():
    _assert_summary_contents(3)


def test_cell_count_grid_003():
    test_utils.assert_cell_count(_grid(3), 9275,
                                 tolerance=25, relative=False)


def test_solution_grid_003():
    test_utils.assert_iteration(_grid(3), 8700, tolerance=0.02, relative=True)


def test_vmean_report_grid_003():
    _assert_report(_grid(3), 'Vmean', 0.104, tolerance=0.02, relative=True)


def test_vmax_report_grid_003():
    _assert_report(_grid(3), 'Vmax', 0.156, tolerance=0.02, relative=True)


def test_evaluate_gci_summary():
    test_utils._remove(_ref_file_gci())
    test_utils._load_sim(_grid(3), 'Demo14_EvalGCITest.java')


def test_maximum_velocity_from_plot():
    num_vals = _values_from_plot(r'.*Numerical .. Y values:\s\[(.*)\]')
    assert len(num_vals) == 21
    test_utils.assert_value(max(num_vals), _vmax())


def test_gci23_coefficients():
    gci23_coeffs = _gci_coefficients()
    test_utils.assert_value(min(gci23_coeffs), 0.0005,
                            tolerance=0.0005, relative=False)
    test_utils.assert_value(max(gci23_coeffs), 0.06,
                            tolerance=0.02, relative=False)
