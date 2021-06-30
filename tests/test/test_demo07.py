# -*- coding: utf-8 -*-
import movie
import test_utils


DEMO_ID = test_utils.demo_id(__file__)
MOVIE_FOLDER = 'pics_Demo7_VOF'


def test_write_summary():
    test_utils.assert_summary_contents_by_demo(DEMO_ID)


def test_tunnel_part_surfaces_count():
    test_utils.assert_part_surfaces_count(DEMO_ID, 'Block', 2)


def test_cell_count():
    test_utils.assert_cell_count(DEMO_ID, 5000, tolerance=0, relative=False)


def test_solution():
    test_utils.assert_iteration(DEMO_ID, 31200)
    test_utils.assert_time(DEMO_ID, 8.0, tolerance=0.0)


def test_cfl_avg_report():
    test_utils.assert_report(DEMO_ID, 'CFL_avg', 0.011,
                             tolerance=0.001, relative=False)


def test_cfl_max_report():
    """Fluid should be at rest by the end of simulation"""
    test_utils.assert_report(DEMO_ID, 'CFL_max', 0.0,
                             tolerance=0.05, relative=False)


def test_time_report():
    test_utils.assert_report(DEMO_ID, 'Time', 8.0, tolerance=0, relative=False)


def test_motion_reports():
    test_utils.assert_report(DEMO_ID, 'MotionDispl', 0.0)
    test_utils.assert_report(DEMO_ID, 'MotionVel', 0.0)


def test_scalar_min():
    test_utils.assert_scene_min(DEMO_ID, 'Demo7_VOF', 'Scalar', 0.0)


def test_scalar_max():
    test_utils.assert_scene_max(DEMO_ID, 'Demo7_VOF', 'Scalar', 1.0,
                                relative=False)


def test_pictures_count():
    folder = '%s/*.png' % MOVIE_FOLDER
    test_utils.assert_pictures_count_in_folder(folder, 1599)


def test_write_movie():
    movie.write(MOVIE_FOLDER)
    test_utils.assert_file_size(movie.name(MOVIE_FOLDER), 3097666,
                                tolerance=0.015, relative=True)
