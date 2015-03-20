/*
  pidroid.h

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


// maths
#define PI 	3.14159265359

// working resolutions for mjpeg-streamer: 640x360, 320x240
#define STREAM_WIDTH	320
#define STREAM_HEIGHT	240

// image size for RaspiStill timelapse capture
// NOTE: Must be divisible by 10!! Otherwise I can't guarantee
// what will not explode ...
#define STILLS_WIDTH	110
#define STILLS_HEIGHT	70


/* For readability purposes */
typedef enum Boolean Boolean ;
enum Boolean {
  FALSE = 0,
  TRUE = 1
} ;
