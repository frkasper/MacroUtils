# -*- coding: utf-8 -*-
import glob
import os
import test_utils


BLOCK_MESHER = 'SimAssistantBlockMesher'


def _assert_cell_count(sim_file, expected):
    test_utils.assert_cell_count(argument=sim_file, expected=expected,
                                 tolerance=0, relative=False)


def _clean_up(base_name):

    os.chdir(test_utils._test_home())

    files = glob.glob('%s*.*' % os.path.splitext(base_name)[0])
    files.append(test_utils._summary_file(base_name))
    test_utils._remove([f for f in files if not _is_java(f)])


def _is_java(f):
    return os.path.splitext(f)[-1] == '.java'


def _new_sim(base_name):
    _clean_up(base_name)
    test_utils._new_sim('%sTest.java' % base_name)


def test_block_mesher_macro_play():
    _new_sim(BLOCK_MESHER)
    assert os.path.exists('%s.sim' % BLOCK_MESHER)
    assert os.path.exists('%s_2D.sim' % BLOCK_MESHER)


def test_block_mesher_cell_count():
    i, j, k = 4, 7, 8
    _assert_cell_count('%s.sim' % BLOCK_MESHER, i * j * k)
    _assert_cell_count('%s_2D.sim' % BLOCK_MESHER, i * j)
