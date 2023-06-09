# -*- coding: utf-8 -*-
import movie
import pytest
import test_utils


DEMO_ID = test_utils.demo_id(__file__)
MOVIES_FOLDERS = {
        'pics_Demo15_Run_DES_Structures': 1179880,
        'pics_Demo15_Run_DES_Turbulent_Viscosity_Ratio': 2871033,
        'pics_Demo15_Run_DES_Velocity': 2997786,
        'pics_Demo15_Run_DES_Wall_Y+': 2408223,
        }
MOVIES_IDS = ['-'.join(x.split('_')[4:]) for x in MOVIES_FOLDERS]


def des_sim():
    return sim_files()[2]


def ss_sim():
    return sim_files()[0]


def sim_files():
    sfs = test_utils.simulations(DEMO_ID)
    assert len(sfs) == 3, 'Invalid files: %s' % str(sfs)
    return sfs


def test_write_summary_ss():
    test_utils.assert_summary_contents_by_sim_file(ss_sim())


def test_unite_part_surfaces_count():
    test_utils.assert_part_surfaces_count(ss_sim(), 'Unite', 11)


def test_cell_count_ss():
    test_utils.assert_cell_count(ss_sim(), 17000,
                                 tolerance=2000, relative=False)


def test_solution_ss():
    test_utils.assert_iteration(ss_sim(), 100, tolerance=0.2, relative=True)


def test_pressure_drop_report_ss():
    test_utils.assert_report(ss_sim(), 'Pressure Drop', 8.1,
                             tolerance=0.1, relative=False)


def test_pressure_scalar_min_ss():
    test_utils.assert_scene_min(ss_sim(), 'Pressure', 'Scalar', -11.1,
                                tolerance=0.1, relative=True)


def test_pressure_scalar_max_ss():
    test_utils.assert_scene_max(ss_sim(), 'Pressure', 'Scalar', 13.7,
                                tolerance=0.02, relative=True)


def test_vector_min_ss():
    test_utils.assert_scene_min(ss_sim(), 'Vector', 'Vector', 0.0,
                                tolerance=0.015)


def test_vector_max_ss():
    test_utils.assert_scene_max(ss_sim(), 'Vector', 'Vector', 5.4,
                                tolerance=0.02)


def test_write_summary_des():
    test_utils.assert_summary_contents_by_sim_file(des_sim())


def test_cell_count_des():
    test_utils.assert_cell_count(des_sim(), 97500,
                                  tolerance=0.02, relative=True)


def test_solution_des():
    test_utils.assert_iteration(des_sim(), 18000)
    test_utils.assert_time(des_sim(), 0.3)


# Too sensitive
# def test_pressure_drop_report_des():
#     test_utils.assert_report(des_sim(), 'Pressure Drop', 47.0,
#                              tolerance=0.05, relative=True)


def test_cfl_avg_report_des():
    test_utils.assert_report(des_sim(), 'CFL_avg', 0.2,
                             tolerance=0.1, relative=False)


def test_cfl_max_report_des():
    test_utils.assert_report(des_sim(), 'CFL_max', 1.6,
                             tolerance=0.50, relative=False)


def test_time_report_des():
    test_utils.assert_report(des_sim(), 'Time', 0.3, tolerance=0.0,
                             relative=False)


# Too sensitive
# def test_p1_report_des():
#     test_utils.assert_report(des_sim(), 'P1', 23.5,
#                              tolerance=0.05, relative=True)


def test_vector_min_des():
    test_utils.assert_scene_min(des_sim(), 'Vector', 'Vector', 0.0,
                                tolerance=0.015, relative=False)


def test_vector_max_des():
    test_utils.assert_scene_max(des_sim(), 'Vector', 'Vector', 6.0,
                                tolerance=0.05, relative=True)


def test_pictures_count():
    for movie_folder in MOVIES_FOLDERS:
        folder = '%s/*.png' % movie_folder
        test_utils.assert_pictures_count_in_folder(folder, 599)


def test_write_movies():
    for movie_folder in MOVIES_FOLDERS:
        movie.write(movie_folder)


@pytest.mark.parametrize('movie_folder', MOVIES_FOLDERS.keys(), ids=MOVIES_IDS)
def test_movie_size(movie_folder):
    file_size = MOVIES_FOLDERS[movie_folder]
    print('Movie folder: %s' % movie_folder)
    test_utils.assert_file_size(movie.name(movie_folder), file_size,
                                tolerance=0.1)
