#!/bin/sh

# start.sh
#
# Copyright (C) 2015 Radu Traian Jipa
# License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
#


# Start the PiDroid server
# expects you give the port address you wish to connect to
python source/PiDroidServer.py $1
