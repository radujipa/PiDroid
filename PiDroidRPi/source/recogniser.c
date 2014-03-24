
#include <Python.h>     // functions to be called by Python

#include <stdlib.h>
#include <stdio.h>

#include <opencv/cv.h>				// 
#include <opencv/highgui.h>		// 
#include <math.h>							// 

#include "pidroid.h"


// For testing purposes - will output variables to standard output
static Boolean DEBUG = TRUE ;

//
int number_of_objects ;

// Starting and stoping MJPEG-STREAMER requires the port
int port ;


/*  
 *
 */
void decode_image (int objIndex, int imgIndex,
	unsigned char decoded_image[STILLS_HEIGHT][STILLS_WIDTH])
{
	//
	char filename[50] ;
	sprintf (filename, "learning/obj%d/img%04d.jpg", objIndex, imgIndex) ;
	
	int width, height, alpha = -1 ;
	int num_of_pixels = STILLS_HEIGHT * STILLS_WIDTH * 3 ;
	unsigned char pixels[num_of_pixels] ;
	
	//
	if (jpegread (filename, alpha, &width, &height, pixels) != 0)
	{
		printf ("\n\nSomething was wrong with jpegread!\n\n") ;
		exit (1) ;
	} // if

	//
	if (DEBUG == TRUE)
	{
		printf ("RECOGNISER:\n decompress_data():\n") ;
		printf ("   decompressing: %s\n\n", filename) ;
	} // if

	//
	for (int pixelIndex = 0; pixelIndex < num_of_pixels; pixelIndex+=3)
	{
		int row = STILLS_HEIGHT - 1 - pixelIndex / (3 * STILLS_WIDTH) ;
		int col = (pixelIndex / 3) % STILLS_WIDTH ;
		
		decoded_image[row][col] = 0.2126 * pixels[pixelIndex + 0]    // R
														+ 0.7152 * pixels[pixelIndex + 1]		 // G
														+ 0.0722 * pixels[pixelIndex + 2] ;	 // B
	} // for
} // decode_image


/*  
 *
 */
void extract_features (int frames, int objIndex, int objType)
{
	char filename[50] ;
	sprintf (filename, "learning/obj%d/feature_data", objIndex) ;
	FILE *data = fopen (filename, "w") ;

	if (data == NULL)
	{
		printf ("Could not open file! Wrong path maybe? Exiting...\n") ;
		exit (1) ;
	} // if

	// The first line of the features data file is the type of the object
	fprintf (data, "%d\n", objType) ;

	// Create the image 2D array to hold the decoded grayscale of a capture
	unsigned char image[STILLS_HEIGHT][STILLS_WIDTH] ;

	for (int imgIndex = 1; imgIndex <= 1/*frames*/; imgIndex++)
	{		
		// Here we decode the JPEG previously taken and extract the grayscale
		//decode_image (objIndex, imgIndex, image) ;
		
		char filename[50] ;
		sprintf (filename, "learning/obj%d/img%04d.jpg", objIndex, imgIndex) ;
	
		IplImage *image = cvLoadImage ("/home/radu/image.jpg", CV_LOAD_IMAGE_COLOR) ;
		cvNamedWindow ("my jpg", CV_WINDOW_NORMAL) ;
		cvShowImage ("my jpg", image) ;
		cvWaitKey (0) ;
		cvDestroyWindow ("my jpg") ;
		cvReleaseImage (&image) ;

		//printf ("\n\nShowing OpenCV Window!\n\n") ;
		//sleep (10) ;

		/*
		for (int colIndex = 0; colIndex < STILLS_HEIGHT; colIndex++)
			fprintf (data, "%s\n", image[colIndex]) ; */
	} // for

	fclose (data) ;
} // extract_features


/*
 * 
 ******************************************************************************
 */
static PyObject* setup (PyObject *self, PyObject *args)
{  
	//
  PyArg_ParseTuple (args, "i", &port) ;

  //
	if (DEBUG == TRUE)
	{
		printf ("RECOGNISER:\n setup():\n") ;
		printf ("   port: %d\n\n", port) ;	
	} // if

  // A function that returns "void" for python
	Py_INCREF (Py_None) ;
	return Py_None ;
} // setup


/*
 * 
 ******************************************************************************
 */
static PyObject* learnNewObject (PyObject *self, PyObject *args)
{
	//
	int objIndex, objType ;
  PyArg_ParseTuple (args, "(ii)", &objIndex, &objType) ;

  //
	if (DEBUG == TRUE)
	{
		printf ("RECOGNISER:\n learnNewObject():\n") ;
		printf ("   objIndex: %d\n", objIndex) ;
		printf ("   objType: %d\n\n", objType) ;
	} // if

	// Set the number of captures of the object to take
  int frames = 3 ;

	// Stop the mjpg-streamer to free the camera
	stopMjpgStreamer () ;

	// Take JPEG images of the new object
	startRaspiStill (STILLS_WIDTH, STILLS_HEIGHT, frames, objIndex) ;

	// Now that we've taken the images, let's start the stream again
	startMjpgStreamer (STREAM_WIDTH, STREAM_HEIGHT, port + 1) ;

	// We've got the data now, let's extract some useful features
	extract_features (frames, objIndex, objType) ;

	// A function that returns "void" for python
	Py_INCREF (Py_None) ;
	return Py_None ;
} // learnNewObject


/*
 * Binding python module function names with the C ones
 ******************************************************************************
 */
static PyMethodDef recogniser_methods[] = {
	{"setup",   	  			setup,						METH_VARARGS},
	{"learnNewObject",		learnNewObject, 	METH_VARARGS}	
} ; // recogniser_methods


/*
 * The module's initialisation function as required by the Python API.
 ******************************************************************************
 */
void initrecogniser ()
{  
  (void) Py_InitModule("recogniser", recogniser_methods) ;
} // initRecogniser
