#!/usr/bin/env python

# Controllers.py
#
# Copyright (C) 2015 Radu Traian Jipa
# License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
#


"""
---------------------------------------------------------
|					PIDROID CONTROLLERS					|
---------------------------------------------------------
"""

import os
import cv2
import collections

import rover
import camera


"""
"""
class RoverController:

	def __init__(self):
		self.configuration = rover.setup()
		self.DEBUG = True


	# args = (turnAngleToSet)
	def setTurnAngle(self, args):

		if self.DEBUG:
			print "ROVER CONTROLLER:\n setTurnAngle():\n",
			print "   configuration: ", self.configuration
			print "   args", args, "\n"

		#
		self.configuration = rover.setTurnAngle((self.configuration, args))


	# args = (speedToSet)
	def setSpeed(self, args):

		if self.DEBUG:
			print "ROVER CONTROLLER:\n setSpeed():\n",
			print "   configuration: ", self.configuration
			print "   args", args, "\n"

		#
		self.configuration = rover.setSpeed((self.configuration, args))


	# args = (turnSensitivityToSet)
	def setTurnSensitivity(self, args):

		if self.DEBUG:
			print "ROVER CONTROLLER:\n setTurnSensitivity():\n",
			print "   configuration: ", self.configuration
			print "   args", args, "\n"

		#
		self.configuration = rover.setTurnSensitivity((self.configuration, args))


	# args = (toggleSpin)
	def toggleSpin(self, args):

		if self.DEBUG:
			print "ROVER CONTROLLER:\n toggleSpin():\n",
			print "   configuration: ", self.configuration
			print "   args", args, "\n"

		#
		self.configuration = rover.toggleSpin((self.configuration, args))


	# a list of the functions in this controller
	functionList = [setTurnAngle, setSpeed, setTurnSensitivity, toggleSpin]



"""
"""
class CameraController:

	def __init__(self, port):
		self.position = camera.setup(port)
		self.DEBUG = True

		# start servoblaster and mjpeg-streamer
		camera.startServoBlaster()
		camera.startMjpegStreamer()


	# args = (CameraX, CameraY)
	def setPosition(self, args):

		if self.DEBUG:
			print "CAMERA CONTROLLER:\n setPosition():\n"
			print "   configuration: ", self.position
			print "   args", args, "\n"

		#
		self.position = camera.setPosition((self.position, args))


	# a list of the functions in this controller
	functionList = [setPosition]



"""
"""
class RecogniserController:

	def __init__(self, port):
		self.PATH = 'learning'
		self.DEBUG = True


	#
	def clearLearningData(self, args):

		# get the file names in ./learning/
		# these will be directories for each learned object
		objects = os.listdir(self.PATH)

		for object in objects:

			# get the file names in each object folder
			# these will either be imgXXXX or features.data
			files = os.listdir(self.PATH + '/' + object)

			for file in files:

				# delete all the files in this folder
				os.remove(self.PATH + '/' + object + '/' + file)

			# finally, delete the object folder
			os.rmdir(self.PATH + '/' + object)

		if self.DEBUG:
			print "RECOGNISER CONTROLLER:\n clearLearningData():"
			print "   deleted all data from ", objects, "\n"


	#
	def collectData(self, path, frames):

		# Stop the mjpg-streamer to free the camera
		camera.stopMjpegStreamer()

		# Take JPEG images of the new object
		camera.startRaspiStill(path, frames)

		# Now that we've taken the images, let's start the stream again
		camera.startMjpegStreamer()


	# args = (objType)
	def learnNewObject(self, args):

		# Compute the new object index and create a new objX folder in PATH
		objIndex = int(len(os.listdir(self.PATH)) + 1)
		os.mkdir(self.PATH + '/obj' + str(objIndex))

		# Use RaspiStill to capture X frames - JPEG images and grab them in a list
		self.collectData(self.PATH + '/obj' + str(objIndex), 5)
		images = os.listdir(self.PATH + '/obj' + str(objIndex))

		# Create the feature file for this object
		featureFile = open(self.PATH + '/obj' + str(objIndex) + '/features.data', 'w')
		featureFile.write("%s\n" % args)

		# For all the images we have taken previously
		for image in images:

			# We've got the data now, let's extract some useful features
			imageFilePath = self.PATH + '/obj' + str(objIndex) + '/' + str(image)
			features = self.extractFeatures(imageFilePath)

			# Let's put all these features in the file
			for feature in features:
				featureFile.write("%s\n" % feature)

		# Neatly close the file before returning
		featureFile.close()

		if self.DEBUG:
			print "RECOGNISER CONTROLLER:\n learnNewObject():"
			print "   objIndex: ", objIndex
			print "   images: ", images, "\n"

		# Send a reply back to the client: lets him know the function finished
		return "2,0,0"


	# NOTE: Naturally, if the features for each image are to be expanded,
	#		this function will no longer be valid!
	# args = (0)
	def recogniseObject(self, args):

		result = -1

		# Take one picture with RaspiStill and extract its features
		self.collectData('resource', 2)
		featuresToMatch = self.extractFeatures('resource/img0001.jpg')

		# Get the number of objects learned and get the features of each one
		objects = os.listdir(self.PATH)

		for object in objects:

			# Open the feature file and read the learned data for each object
			with open(self.PATH + '/' + object + '/features.data', 'r') as featureFile:
				objFeatures = [int(featureFile.readline().split()[0])]
				lines = featureFile.readlines()

				# read the rest of the lines from the file
				features = []
				for line in lines:
					features.append(line.split()[0])

				# add to this object's feature list the most common feature
				objFeatures.append(int(collections.Counter(features).most_common()[0][0]))
			featureFile.close()

			# finally, match the features to the feature set
			if featuresToMatch[0] == objFeatures[1]:

				if self.DEBUG:
					print "RECOGNISER CONTROLLER:\n recogniseObject():"
					print "   objects: ", objects
					print "   featuresToMatch: ", featuresToMatch
					print "   result: ", objFeatures[0], "\n"

				# Success! PiDroid has recognised an object!
				#return "2,1," + str(objFeatures[0])
				result = objFeatures[0]

		# notify the user that the object was not recognised
		return "2,1," + str(result)


	# Made with further improvements in mind. Add more features!
	def extractFeatures(self, imageFilePath):

		features = []

		# get the number of detected corners in the image - Harris Corner detector
		corners = self.getNumberOfCorners(imageFilePath)
		features.append(corners)

		# Return our list of features
		return features


	#
	def getNumberOfCorners(self, imageFilePath):

		# read the image extracting the gray scale and do a binary threshold
		image = cv2.imread(imageFilePath)
		gray = cv2.cvtColor(image, cv2.COLOR_RGB2GRAY)
		#retval, threshold_image = cv2.threshold(gray, 170, 255, cv2.THRESH_BINARY)

		# detectop parameters
		blockSize = 2
		apertureSize = 3
		k = 0.04

		# Harris Corner detector
		#corner_map = cv2.cornerHarris(threshold_image, blockSize, apertureSize, k)
		corner_map = cv2.cornerHarris(gray, blockSize, apertureSize, k)

		# experiment with threshold to get the right no of corners
		# fragile threshold .. careful changing this value!
		threshold = 0.001 * corner_map.max()
		corners = 0

		# Convolution: when a corner has been found, do a local maximum
		# to filter other close by corners which can be approximated to
		# a single interesting one
		HEIGHT = len(corner_map) - 1
		WIDTH = len(corner_map[0]) - 1

		for y in range(10, HEIGHT - 10):
			for x in range(10, WIDTH - 10):
				if corner_map[y][x] > threshold:

					# initialise the local maximum
					max = [y, x, corner_map[y][x]]

					# check if any of the surrounding pixels is also a corner
					for y1 in range(y - 10, y + 10):
						for x1 in range(x - 10, x + 10):

							# new local maximum
							if corner_map[y1][y1] > max[2]:
								max = [x1, y1, corner_map[y1][x1]]

							# otherwise this corner might be counted multiple times
							if corner_map[y1][x1] > threshold:
								corner_map[y1][x1] = 0
								cv2.circle(image, (x1, y1), 2, (255,0,0), -1)

					# only count this local corner once and draw a circle on top
					cv2.circle(image, (max[1], max[0]), 2, (0,0,255), -1)
					corners += 1

		if self.DEBUG:
			print "RECOGNISER CONTROLLER:\n getNumberOfCorners():"
			print "   corners: ", corners, "\n"

		# show the result
		#cv2.namedWindow('Color Image')
		#cv2.imshow('Color Image', image)
		#cv2.namedWindow('Grayscale Image')
		#cv2.imshow('Grayscale Image', gray)
		#cv2.namedWindow('Thresholded Image')
		#cv2.imshow('Thresholded Image', threshold_image)
		#cv2.waitKey(0)
		#cv2.destroyAllWindows()

		return corners

	# a list of the functions in this controller
	functionList = [learnNewObject, recogniseObject, clearLearningData]



"""
---------------------------------------------------------
|			  PIDROID CONTROLLERS TEST CODE 			|
---------------------------------------------------------

>>OUTDATED<<

"""
