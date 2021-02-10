# -*- coding: utf-8 -*-
"""
Generate movies from pictures with FFMPEG.

Created on Wed Jun  6 14:54:06 2018

@author: Fabio Kasper
"""
import glob
import os
import re
import shutil


_FMT_PICS = 'pic%08d.png'


def _rename_pictures(folder):
    pwd = os.getcwd()
    os.chdir(folder)
    files = sorted(glob.glob("*"), key=os.path.getmtime)
    print('Renaming %d pictures ... ' % len(files)),
    for n, filename in enumerate(files[::2]):
        os.rename(filename, _FMT_PICS % (n+1))
    print('OK!')
    os.chdir(pwd)


def _remove(folder):
    if os.path.isdir(folder):
        print('Removing "%s" ... ' % folder),
        shutil.rmtree(folder)
        print('OK!')


def name(folder):
    assert re.match('^pics_.*', folder), 'Invalid folder: %s' % folder
    return '_%s.mp4' % folder.replace('pics_', '')


def write(folder):
    movie_file = name(folder)

    print('')
    print('This folder: "%s"' % os.getcwd())
    print('Movie file: "%s"' % movie_file)
    print('From Folder: "%s"' % folder)

    if os.path.isfile(movie_file):
        print('Movie already exists.')
        return

    temp_folder = 'tmp_%s' % folder
    _remove(temp_folder)

    print('Duplicating to "%s"...' % temp_folder)
    shutil.copytree(folder, temp_folder)

    _rename_pictures(temp_folder)

    command = 'ffmpeg -i %s/%s -framerate 60 -c:v libx264 ' \
        '-pix_fmt yuv420p %s; rm -fr %s' % (temp_folder, _FMT_PICS,
                                            movie_file, temp_folder)
    print('\nCommand: "%s"\n' % command)
    os.system(command)
    _remove(temp_folder)


if __name__ == "__main__":
    pass
