# -*- coding: utf-8 -*-
import movie
import test_utils


DEMO_ID = test_utils.demo_id(__file__)
MOVIE_FOLDER = 'pics_Demo13_Streamlines'


def test_write_summary():
    test_utils.assert_summary_contents_by_demo(DEMO_ID)


def test_cylinders_part_surfaces_count():
    for cyl in ['Main', 'In', 'Out']:
        test_utils.assert_part_surfaces_count(DEMO_ID, 'Cyl%s' % cyl, 3)


def test_unite_part_surfaces_count():
    test_utils.assert_part_surfaces_count(DEMO_ID, 'Unite', 7)


def test_cell_count():
    test_utils.assert_cell_count(DEMO_ID, 14400, tolerance=0.025)


def test_solution():
    test_utils.assert_iteration(DEMO_ID, 200)


def test_pressure_scalar_min():
    test_utils.assert_scene_min(DEMO_ID, 'Scalar', 'Scalar', -9.5,
                                tolerance=1.0, relative=False)


def test_pressure_scalar_max():
    test_utils.assert_scene_max(DEMO_ID, 'Scalar', 'Scalar', 14.5,
                                tolerance=0.5, relative=False)


def test_streamline_min():
    test_utils.assert_scene_min(DEMO_ID, 'Scalar', 'Streamline', 0.0,
                                tolerance=0, relative=False)


def test_streamline_max():
    test_utils.assert_scene_max(DEMO_ID, 'Scalar', 'Streamline', 3.0,
                                tolerance=0, relative=False)


def test_pictures_count():
    folder = '%s/*.png' % MOVIE_FOLDER
    test_utils.assert_pictures_count_in_folder(folder, 1548)


def test_write_movie():
    movie.write(MOVIE_FOLDER)
    test_utils.assert_file_size(movie.name(MOVIE_FOLDER), 9799470,
                                tolerance=0.1, relative=True)
