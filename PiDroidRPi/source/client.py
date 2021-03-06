'''
Code taken and adapted from:
The University of Manchester Computer Science
COMP18111 - Lab Exercise 3
version: 2011/2012

ex3.py - Module for ex3 - David Thorne / AIG / 15-01-2009
'''


import sys

from serverutils import Client


class IRCClient(Client):

    def onMessage(self, socket, message):
        # *** process incoming messages here ***
        print message
        return True


# Parse the IP address and port you wish to connect to.
ip = "0.0.0.0"
port = int(sys.argv[1])

# Create an IRC client.
client = IRCClient()

# Start server
client.start(ip, port)

# *** register your client here, e.g. ***
# client.send('/user %s' % screenName)

while client.isRunning():
    try:
        command = raw_input("> ").strip()
        # *** process input from the user in a loop here ***
        # *** use client.send(someMessage) to send messages to the server
        client.send(command)
    except:
        client.stop()

client.stop()
