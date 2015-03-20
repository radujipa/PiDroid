'''
Code taken and adapted from:
The University of Manchester Computer Science
COMP18111 - Lab Exercise 3
version: 2011/2012

ex3.py - Module for ex3 - David Thorne / AIG / 15-01-2009


Network server skeleton.

This shows how you can create a server that listens on a given network socket, dealing
with incoming messages as and when they arrive. To start the server simply call its
start() method passing the IP address on which to listen (most likely 127.0.0.1) and
the TCP port number (greater than 1024). The Server class should be subclassed here,
implementing some or all of the following five events.

  onStart(self)
      This is called when the server starts - i.e. shortly after the start() method is
      executed. Any server-wide variables should be created here.

  onStop(self)
      This is called just before the server stops, allowing you to clean up any server-
      wide variables you may still have set.

  onConnect(self, socket)
      This is called when a client starts a new connection with the server, with that
      connection"s socket being provided as a parameter. You may store connection-
      specific variables directly in this socket object. You can do this as follows:
          socket.myNewVariableName = myNewVariableValue
      e.g. to remember the time a specific connection was made you can store it thus:
          socket.connectionTime = time.time()
      Such connection-specific variables are then available in the following two
      events.

  onMessage(self, socket, message)
      This is called when a client sends a new-line delimited message to the server.
      The message paramater DOES NOT include the new-line character.

  onDisconnect(self, socket)
      This is called when a client"s connection is terminated. As with onConnect(),
      the connection"s socket is provided as a parameter. This is called regardless of
      who closed the connection.
'''


import sys

from serverutils import Server
from MessageHandler import *


class PiDroidServer(Server):
    '''
    '''

    def onStart(self, ip, port):
        self.noOfClients = 0
        self.clients = []
        self.messenger = MessageHandler(port)
        print "Server has started.."

    def onStop(self):
        print "Server is stopping.."

    def onConnect(self, socket):
        self.noOfClients += 1
        self.clients.append(socket)
        print "Client connected to server.."
        print self.noOfClients, "clients are connected"

        # if not socket.username == "guest" + str(self.noOfClients):
        socket.username = "guest" + str(self.noOfClients)
        '''
        socket.msgtype = "/all"
        socket.param = ""
        socket.ignBroadcast = False
        socket.invisible = False
        socket.send("To change it, use command /user")
        socket.send("For more commands, type /help")
        socket.send("Your username is " + socket.username)
        '''

    def onMessage(self, socket, message):
        print "Client ", socket.username, " sent: ", message

        reply = self.messenger.dispatch(message)

        if reply != None:
            socket.send(reply)

        # Signify all is well
        return True

    def onDisconnect(self, socket):
        self.noOfClients -= 1
        for index in range(0, len(self.clients)):
            if self.clients[index] == socket:
                del self.clients[index]
                break
        print socket.username + " has disconnected from server..",
        print self.noOfClients, " clients are connected"


# Parse the IP address and port you wish to listen on.
ip = "0.0.0.0"
port = int(sys.argv[1])

# Create a server.
PiDroid = PiDroidServer()

# Start server
PiDroid.start(ip, port)
