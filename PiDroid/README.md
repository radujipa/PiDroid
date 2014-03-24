
PiDroid: Android App
====================
#### Raspberry Pi driven robot controlled by an Android device


Install
=======

#### Method 1 - Easy

The easiest method is to send to your android device the app apk. It can be found in:

	$ cd build/apk

Once it has been sent, you'll need to have 'Developer Options' enabled to install apps which were not downloaded from Play Store. Then, simply open the apk and follow the instructions.



#### Method 2 - A bit harder

This method involves building the source files yourself and it may take some time to install everything on your machine. To create this app, I have used Android Studio, however, if you'd like to build the app with, say, Eclipse you're free to do so. So, download [Android Studio](http://developer.android.com/sdk/installing/studio.html).

Follow the instructions specific for your machine, and install the necesarry
dependencies with the SDK Manager ![SDK Manager](http://i.imgur.com/LbtaGaH.png)

After all updates and dependencies have been downloaded and installed, you'll need
to import the project. This will be either from the *Welcome Screen*, or 
*File > Import project...*

If everything has worked well until now, great! All that needs to be done now is
to connect your android device to your machine and hit build ![Build](http://i.imgur.com/JgkW4cU.png)

Hopefully, everything worked smoothly and now you have the app installed on your device.