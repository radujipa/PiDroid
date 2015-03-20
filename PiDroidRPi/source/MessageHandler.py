#!/usr/bin/env python

# MessageHandler.py
#
# Copyright (C) 2015 Radu Traian Jipa
# License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
#


"""
---------------------------------------------------------
|					 MESSAGE HANDLER					|
---------------------------------------------------------
"""

from Controllers import *


"""
"""
class MessageHandler:

	# Message type constants
	ROVER_CONTROLLER = "0"
	CAMERA_CONTROLLER = "1"
	RECOGNISER_CONTROLLER = "2"


	def __init__(self, port):

		# initialisation stuff
		self.DEBUG = True

		# Different wrappers for C programs
		self.rover = RoverController()
		self.camera = CameraController(port)
		self.recogniser = RecogniserController(port)


	def dispatch(self, message):

		#
		(controller, sep, command) = message.partition(",")
		(method, sep, parameters)  = command.partition(",")

		#
		method = int(method)
		parameters = parameters.split(",")

		#
		for i in range(0, len(parameters)):
			parameters[i] = int(parameters[i])
		parameters = tuple(parameters)

		#
		if self.DEBUG:
			print "MESSAGE HANDLER:\n dispatch:\n",
			print "   message:", message
			print "   controller:", controller
			print "   method:", method
			print "   parameters:", parameters, "\n"

		#
		if controller == self.ROVER_CONTROLLER:
			reply = self.rover.functionList[method](self.rover, parameters)

		elif controller == self.CAMERA_CONTROLLER:
			reply = self.camera.functionList[method](self.camera, parameters)

		elif controller == self.RECOGNISER_CONTROLLER:
			reply = self.recogniser.functionList[method](self.recogniser, parameters)

		# If the called function has something to tell the client
		# it does so through this reply message
		return reply



"""
---------------------------------------------------------
|					HANDLER TEST CODE					|
---------------------------------------------------------

>>OUTDATED<<

print '''messenger = MessageHandler()'''
messenger = MessageHandler()

print '''messenger.dispatch("0,2,-100")'''
messenger.dispatch("0,2,-100")

print '''messenger.dispatch("0,0,120")'''
messenger.dispatch("0,0,120")

print '''messenger.dispatch("0,1,60")'''
messenger.dispatch("0,1,60")

print '''messenger.dispatch("0,2,100")'''
messenger.dispatch("0,2,100")

print '''messenger.dispatch("0,0,120")'''
messenger.dispatch("0,0,120")

print '''messenger.dispatch("0,1,60")'''
messenger.dispatch("0,1,60")

print '''messenger.dispatch("0,3,50,-50")'''
messenger.dispatch("0,3,50,-50")

print '''messenger.dispatch("0,3,0,0")'''
messenger.dispatch("0,3,0,0")

messenger = MessageHandler(5000)
messenger.recogniser.clearDataType("img")
messenger.dispatch("2,0,0")
"""
