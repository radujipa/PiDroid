#!/bin/sh

# build.sh
#
# Copyright (C) 2015 Radu Traian Jipa
# License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
#


# Compile all the C extension modules
python setup.py build

# And copy them to the source file where all
# the python scripts are
cp build/lib.*/* source/.

# TODO: better way of doing this would be nice..
