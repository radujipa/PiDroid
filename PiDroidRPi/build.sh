#!/bin/sh

# Compile all the C extension modules
python setup.py build

# And copy them to the source file where all
# the python scripts are
cp build/lib.*/* source/.

# TODO: better way of doing this would be nice..
