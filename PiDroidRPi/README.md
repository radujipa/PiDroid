
PiDroid: RPi package
====================
#### Raspberry Pi driven robot controlled by an Android device



Install
=======

First of all, as mentioned, this project has a few dependencies which need to be install on the Pi.

To install [WiringPi](http://wiringpi.com/download-and-install/) follow the instructions given on the project's website. This is well documented and there is no need for me to go through them.


Next, install [ServoBlaster](https://github.com/richardghirst/PiBits/tree/master/ServoBlaster). Again, the instructions on 
how to do this are well documented there. After you have built ServoBlaster, please move the project to **/opt/ServoBlaster/**.

	$ cd /path/to/repo/PiBits/
	$ mv -r ServoBlaster/* /opt/.


You will also need [MJPEG-Streamer](http://sourceforge.net/projects/mjpg-streamer/).Since the official V4L2 drivers have recently been released, the streamer can now work better. Make sure you copy the project in **/opt/mjpg-streamer**, again.

	$ cd /path/to/repo/MJPG-streamer
	$ mv -r mjpg-streamer/* /opt/.


Finally, you will need to install OpenCV library on the Pi for object recognition. Keep in mind that building OpenCV on the Pi will take a **long** time, roughly 10h. With that in mind, I an not particularly happy about this and I may implement the small number of functions actually used from OpenCV.. For now, decide whether you want / need this functionality and install OpenCV following [these](http://robertcastle.com/2014/02/installing-opencv-on-a-raspberry-pi/) instrunctions.


Now, building the software on the RPi is easy. Simply go to the repo's folder and run the build script:
	
	$ cd /path/to/repo/PiDroid/PiDroidRPi
	$ ./build.sh



Configuration
=============

To configure PiDroid, you must edit **pidroid.conf** for your own build. Please keep in mind that the configuration file is quite fragile - not very robust parser was implemented. Do not exceed 100 characters per line, start comment lines with the **#** symbol, and respect the **begin** and **end** blocks. The current vesion of the configuration file shows how to do this. 


Start
=====

If everything was configured properly and all dependencies installed properly, start PiDroid by running:

	$ sudo ./start.sh <port_number>

e.g. 

	$ sudo ./start.sh 8080


You should notice that ServoBlaster starts with the supplied pins and that MJPG-streamer starts on **port + 1**. If everything was properly installed, then the last printed debug output should be **Server has started...**
