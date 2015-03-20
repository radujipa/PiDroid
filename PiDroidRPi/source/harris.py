#!/usr/bin/env python

# harris.py
#
# Copyright (C) 2015 Radu Traian Jipa
# License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
#


import cv2

# read the image extracting the gray scale and do a binary threshold
image = cv2.imread('img0001.jpg', cv2.CV_LOAD_IMAGE_GRAYSCALE)
retval, threshold_image = cv2.threshold(image, 128, 255, cv2.THRESH_BINARY)

# detectop parameters
blockSize = 2
apertureSize = 3
k = 0.04

#
corner_map = cv2.cornerHarris(threshold_image, blockSize, apertureSize, k)
#threshold_image[corner_map>0.01*corner_map.max()]=[0,0,255]

#
#corner_map_norm = cv2.normalize(corner_map, None, 0, 255, cv2.NORM_MINMAX)

#
#corner_map_norm = cv2.convertScaleAbs(corner_map_norm)

# experiment with threshold to get the right no of corners
threshold = 64
corners = 0

corner_map.max()

"""
# for each row and column in the thresholded image
for y in range(0, len(corner_map) - 1):
	for x in range(0, len(corner_map[0]) - 1):
		print corner_map[y][x]
		if corner_map[y][x] > threshold:
			cv2.circle(threshold_image, (y,x), 10, (255,0,0), -1)
			corners += 1

print corners
"""

# show the result
cv2.namedWindow('window')
cv2.imshow('window', threshold_image)
cv2.waitKey(0)

cv2.destroyWindow('window')
