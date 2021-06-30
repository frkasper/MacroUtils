# -*- coding: utf-8 -*-
import movie
import test_utils


DEMO_ID = test_utils.demo_id(__file__)
MOVIE_FOLDERS = {
        'pics_Demo15_Run_DES_Structures': 1088207,
        'pics_Demo15_Run_DES_Turbulent_Viscosity_Ratio': 3326595,
        'pics_Demo15_Run_DES_Velocity': 2622274,
        'pics_Demo15_Run_DES_Wall_Y+': 1933390,
        }


def _grid_des():
    return _sim_files()[2]


def _grid_ss():
    return _sim_files()[0]


def _sim_files():
    sfs = test_utils.simulations(DEMO_ID)
    assert len(sfs) == 3, 'Invalid files: %s' % str(sfs)
    return sfs


def _write_movies():
    """Write all movies first irrespective of possible failures"""
    for movie_folder in MOVIE_FOLDERS:
        movie.write(movie_folder)


def test_write_summary_ss():
    test_utils.assert_summary_contents_by_sim_file(_grid_ss())


def test_unite_part_surfaces_count():
    test_utils.assert_part_surfaces_count(_grid_ss(), 'Unite', 11)


def test_cell_count_ss():
    test_utils.assert_cell_count(_grid_ss(), 16500,
                                 tolerance=500, relative=False)


def test_solution_ss():
    test_utils.assert_iteration(_grid_ss(), 100, tolerance=0.2, relative=True)


def test_pressure_drop_report_ss():
    test_utils.assert_report(_grid_ss(), 'Pressure Drop', 8.1,
                             tolerance=0.1, relative=False)


def test_pressure_scalar_min_ss():
    test_utils.assert_scene_min(_grid_ss(), 'Pressure', 'Scalar', -11.1,
                                tolerance=0.1, relative=True)


def test_pressure_scalar_max_ss():
    test_utils.assert_scene_max(_grid_ss(), 'Pressure', 'Scalar', 13.7,
                                tolerance=0.02, relative=True)


def test_vector_min_ss():
    test_utils.assert_scene_min(_grid_ss(), 'Vector', 'Vector', 0.0,
                                tolerance=0.015)


def test_vector_max_ss():
    test_utils.assert_scene_max(_grid_ss(), 'Vector', 'Vector', 5.4,
                                tolerance=0.02)


def test_write_summary_des():
    test_utils.assert_summary_contents_by_sim_file(_grid_des())


def test_cell_count_des():
    test_utils.assert_cell_count(_grid_des(), 97500,
                                 tolerance=0.02, relative=True)


def test_solution_des():
    test_utils.assert_iteration(_grid_des(), 18000)
    test_utils.assert_time(_grid_des(), 0.3)


# Too sensitive
# def test_pressure_drop_report_des():
#     test_utils.assert_report(_grid_des(), 'Pressure Drop', 47.0,
#                              tolerance=0.05, relative=True)


def test_cfl_avg_report_des():
    test_utils.assert_report(_grid_des(), 'CFL_avg', 0.2,
                             tolerance=0.1, relative=False)


def test_cfl_max_report_des():
    test_utils.assert_report(_grid_des(), 'CFL_max', 1.6,
                             tolerance=0.50, relative=False)


def test_time_report_des():
    test_utils.assert_report(_grid_des(), 'Time', 0.3, tolerance=0.0,
                             relative=False)


# Too sensitive
# def test_p1_report_des():
#     test_utils.assert_report(_grid_des(), 'P1', 23.5,
#                              tolerance=0.05, relative=True)


def test_vector_min_des():
    test_utils.assert_scene_min(_grid_des(), 'Vector', 'Vector', 0.0,
                                tolerance=0.015, relative=False)


def test_vector_max_des():
    test_utils.assert_scene_max(_grid_des(), 'Vector', 'Vector', 6.0,
                                tolerance=0.05, relative=True)


def test_pictures_count():
    for movie_folder in MOVIE_FOLDERS:
        folder = '%s/*.png' % movie_folder
        test_utils.assert_pictures_count_in_folder(folder, 599)


def test_write_movies():
    _write_movies()
    for movie_folder in MOVIE_FOLDERS:
        file_size = MOVIE_FOLDERS[movie_folder]
        print('Movie folder: %s' % movie_folder)
        test_utils.assert_file_size(movie.name(movie_folder), file_size,
                                    tolerance=0.1)
