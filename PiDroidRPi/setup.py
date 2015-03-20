#!/usr/bin/env python

# setup.py
#
# Copyright (C) 2015 Radu Traian Jipa
# License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
#


from distutils.core import setup, Extension


module1 = Extension('rover',
                    include_dirs = ['./source/'],
                    libraries = ['wiringPi', 'm'],
                    #library_dirs = [''],
                    sources = ['./source/rover.c'],
                    extra_compile_args = ['-Wall', '-std=c99'])

module2 = Extension('camera',
                    include_dirs = ['./source/'],
                    libraries = ['wiringPi', 'pthread'],
                    #library_dirs = [''],
                    sources = ['./source/camera.c', './source/camera_handler.c'],
                    extra_compile_args = ['-Wall', '-std=c99'])
"""
module3 = Extension('recogniser',
                    include_dirs = ['./source/', '/usr/local/include'],
                    libraries = ['pthread', 'jpeg', 'opencv_core', 'opencv_highgui', 'm'],
                    library_dirs = ['/usr/local/lib'],
                    sources = ['./source/recogniser.c', './source/camera_handler.c', './source/jpegread.c'],
                    extra_compile_args = ['-Wall', '-std=c99'])
"""

setup (name = 'PackageName',
       version = '1.0',
       description = 'TODO',
       author = 'Radu T. Jipa',
       author_email = 'radu.t.jipa@gmail.com',
       url = 'https://github.com/radujipa/PiDroid',
       ext_modules = [module1, module2])
