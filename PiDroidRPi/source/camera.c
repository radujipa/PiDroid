
#include <Python.h>     // functions to be called by Python

#include <stdlib.h>     //
#include <stdio.h>      // files, system
#include <unistd.h>     // sleep
#include <sys/types.h>  // pid_t

#include "pidroid.h"    // Boolean


// For testing purposes - will output variables to standard output
static Boolean DEBUG = TRUE ;

// Camera position (X,Y) - these will be used to move the servos
// Pins: camera_pwm[0] = X pin, camera_pwm[1] = Y pin
int camera_pwm[2] ; 
int cameraX = 0, cameraY = 0 ;

// Starting and stoping MJPEG-STREAMER requires the port
int port ;


/*  
 *  ServoBlaster specific
 *      Writes to the servoblaster daemon with the corresponding "API".
 *      The values to be set also need to be remmaped to a smalled interval
 *      since my servos were making a strange noise towards extreme values..
 */
void updatePosition (void)
{
  FILE *fp = fopen ("/dev/servoblaster", "w") ;
  if (fp == NULL)
  {
    printf ("Could not open /dev/servoblaser. Forgot to run servod?\n") ;
    exit (0) ;
  } // if

  // camera pos = [-100,100] -> remap to camera pos = [20,80]
  cameraX = (cameraX + 100.0) / 10 * 3 + 20 ;
  cameraY = (cameraY + 100.0) / 10 * 3 + 20 ;

  fprintf (fp, "0=%d%%\n", cameraX) ;
  fprintf (fp, "1=%d%%\n", cameraY) ;

  fclose (fp) ;
} // updatePosition


/*
 * C Extension for Python
 *    This function sets up the CAMERA
 *
 *    It reads the configuration file, pidroid.conf, and gets the
 *    two PWM pins for the camera SERVOs.
 *
 *    The numbering of the pins is with respect to ServoBlaster naming!
 *    see https://github.com/richardghirst/PiBits/tree/master/ServoBlaster
 ******************************************************************************
 */
static PyObject* setup (PyObject *self, PyObject *args)
{
  // Parse the arguments from python; the port is needed by Mjpeg-Streamer
  PyArg_ParseTuple (args, "i", &port) ;

  FILE *configFile = fopen ("pidroid.conf", "r") ;
  char buffer[100] ;
  int pin ;


  // Read line by line from the config file until the end 
  while (fgets (buffer, sizeof (buffer), configFile) != NULL)
  {
    // Keep reading lines until you reach the BEGINING of a block
    // i.e. ignores comment lines
    while (strcmp (buffer, "begin\n") != 0)
      fgets (buffer, sizeof (buffer), configFile) ;
      
    // Keep reading lines until you reach the END of the block
    while (strcmp (buffer, "end") != 0)
    {
      fscanf (configFile, "%s %d", buffer, &pin) ;
      
      // Here the PWM pin for PAN is set
      if (strcmp (buffer, "pwmX") == 0)
        camera_pwm[0] = pin ;
      else
      // Here the PWM pin for TILT is set
      if (strcmp (buffer, "pwmY") == 0)
        camera_pwm[1] = pin ;
    } // while
  } // for
  fclose (configFile) ;

  if (DEBUG == TRUE)
  {
    printf ("CAMERA:\n setup():\n") ;
    printf ("   pwm: %d, %d\n", camera_pwm[0], camera_pwm[1]) ;
    printf ("   cameraX: %d, cameraY: %d\n", cameraX, cameraY) ;
    printf ("   port: %d\n\n", port) ;
  } // if  

  // Build a tuple and return it back to the Python Controller
  return Py_BuildValue ("(iiii)",
    camera_pwm[0], camera_pwm[1], cameraX, cameraY) ;
} // setup


/*
 * C Extension for Python
 *    This function sets the CAMERA position
 *
 *    Simply updates the pan/tilt SERVOs positions
 *    with the new ones. 
 ******************************************************************************
 */
static PyObject* setPosition (PyObject *self, PyObject *args)
{
  // Parse the arguments from python
  int cameraXtoSet, cameraYtoSet ;

  PyArg_ParseTuple (args, "((iiii)(ii))", 
    &camera_pwm[0], &camera_pwm[1], &cameraX, &cameraY,
    &cameraXtoSet, &cameraYtoSet) ;
  
  //
  cameraX = cameraXtoSet ;
  cameraY = cameraYtoSet ;
  updatePosition () ;

  if (DEBUG == TRUE)
  {
    printf ("CAMERA:\n setPosition():\n") ;
    printf ("   pwm: %d, %d\n", camera_pwm[0], camera_pwm[1]) ;
    printf ("   cameraX: %d, cameraY: %d\n", cameraX, cameraY) ;
    printf ("   cameraXtoSet: %d, cameraYtoSet: %d\n\n", 
      cameraXtoSet, cameraYtoSet) ;
  } // if

  // Build a tuple and return it back to the Python Controller
  return Py_BuildValue ("(iiii)",
    camera_pwm[0], camera_pwm[1], cameraX, cameraY) ;
} // setPosition


/*
 * C Extension for Python
 *    This function 
 *
 ******************************************************************************
 */
static PyObject* startServoBlaster (PyObject *self, PyObject *args)
{
  // Now that we have the P1 pins for the servos,
  char command[60] ;
  sprintf (command, "sudo /opt/ServoBlaster/user/servod --p1pins=\"%d,%d\"",
    camera_pwm[0], camera_pwm[1]) ;

  if (DEBUG == TRUE)
  {
    printf ("CAMERA:\n startServoBlaster():\n") ;
    printf ("   command: %s\n", command) ;
  } // if

  // let's start the servoblaster and wait for everything to settle down.
  system (command) ;
  sleep (1) ;

  // A function that returns "void" for python
  Py_INCREF (Py_None) ;
  return Py_None ;
} // startServoBlaster


/*
 * C Extension for Python
 *    This function 
 *
 ******************************************************************************
 */
static PyObject* stopServoBlaster (PyObject *self, PyObject *args)
{
  const char *command = "sudo killall servod" ;

  if (DEBUG == TRUE)
  {
    printf ("CAMERA:\n stopServoBlaster():\n") ;
    printf ("   command: %s\n", command) ;
  } // if

  system (command);
  sleep (1) ;

  // A function that returns "void" for python
  Py_INCREF (Py_None) ;
  return Py_None ;
} // stopServoBlaster


/*
 * C Extension for Python
 *    This function 
 *
 ******************************************************************************
 */
static PyObject* startMjpegStreamer (PyObject *self, PyObject *args)
{
  char command[250];
  sprintf (command, "LD_LIBRARY_PATH=/opt/mjpg-streamer/ /opt/mjpg-streamer/mjpg_streamer -i \"input_raspicam.so -fps 30 -q 100 -vf -hf --width %d --height %d\" -o \"output_http.so -p %d -w /opt/mjpg-streamer/www\" &",
    STREAM_WIDTH, STREAM_HEIGHT, port + 1) ;

  if (DEBUG == TRUE)
  {
    printf ("CAMERA:\n startMjpgStreamer():\n") ;
    printf ("   command: %s\n", command) ;
  } // if

  system (command) ;
  sleep (2) ;

  // A function that returns "void" for python
  Py_INCREF (Py_None) ;
  return Py_None ;
} // startMjpegStreamer


/*
 * C Extension for Python
 *    This function 
 *
 ******************************************************************************
 */
static PyObject* stopMjpegStreamer (PyObject *self, PyObject *args)
{
  const char *command = "sudo killall mjpg_streamer" ;

  if (DEBUG == TRUE)
  {
    printf ("CAMERA:\n stopMjpgStreamer():\n") ;
    printf ("   command: %s\n\n", command) ;
  } // if

  system (command) ;
  sleep (1) ;

  // A function that returns "void" for python
  Py_INCREF (Py_None) ;
  return Py_None ;
} // stopMjpegStreamer


/*
 * C Extension for Python
 *    This function 
 *
 ******************************************************************************
 */
static PyObject* startRaspiStill (PyObject *self, PyObject *args)
{
  //
  const char *path ;
  int frames ;
  PyArg_ParseTuple (args, "si", &path, &frames) ;

  // Set the timelapse parameters
  int timelapse = 1500 ; // ms
  int timeout = timelapse * (frames - 1) ;

  // Apparently, if you run RaspiStill on the main thread it kills pidroid...
  // so create a new thread and run it there
  pid_t raspistill_pid ;

  if ((raspistill_pid = fork ()) < 0)
  {
    perror ("CAMERA: startRaspiStill(): fork failed") ;
    exit (1) ;
  } // 

  if (raspistill_pid == 0)
  {
    // RaspiStill process here
    // The command with which we start a timelapse capturing JPEGs -cfx 128:128
    char command[50] ;

    if (frames == 1)
      sprintf (command, "sudo raspistill -w %d -h %d -vf -hf -t 1 -o %s/image.jpg",
        STILLS_WIDTH, STILLS_HEIGHT, path) ;
    else
      sprintf (command, "sudo raspistill -w %d -h %d -vf -hf -t %d -tl %d -o %s/img%%04d.jpg", 
        STILLS_WIDTH, STILLS_HEIGHT, timeout, timelapse, path) ;

    if (DEBUG == TRUE)
    {
      printf ("CAMERA:\n startRaspiStill():\n") ;
      printf ("   command: %s\n\n", command) ;
    } // if

    // Start the image captures and wait 1s after it finishes
    system (command) ;
  } // if
  else
  {
    // Main process here waits for RaspiStill process to finish
    wait (NULL) ;
  } // else

  // A function that returns "void" for python
  Py_INCREF (Py_None) ;
  return Py_None ;
} // startRaspiStill


/*
 * Binding python module function names with the C ones
 ******************************************************************************
 */
static PyMethodDef camera_methods[] = {
	{"setup",   	          setup,                METH_VARARGS},
 	{"setPosition",         setPosition,          METH_VARARGS},
  {"startServoBlaster",   startServoBlaster,    METH_VARARGS},
  {"stopServoBlaster",    stopServoBlaster,     METH_VARARGS},
  {"startMjpegStreamer",  startMjpegStreamer,   METH_VARARGS},
  {"stopMjpegStreamer",   stopMjpegStreamer,    METH_VARARGS},
  {"startRaspiStill",     startRaspiStill,      METH_VARARGS}
} ; // camera_methods


/*
 * The module's initialisation function as required by the Python API.
 ******************************************************************************
 */
void initcamera ()
{  
  (void) Py_InitModule("camera", camera_methods) ;
} // initCamera
