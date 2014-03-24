
#include <Python.h>     // functions to be called by Python

#include <stdlib.h>     //
#include <stdio.h>      // files, system
#include <math.h>       // sin, cos

#include <wiringPi.h>   // gpio handling library
#include <softPwm.h>    // software pulse width modulation

#include "pidroid.h"    // Boolean, PI


// Driving states for the rover's motors
#define ROVER_FORWARDS   0
#define ROVER_BACKWARDS  1
#define ROVER_SPIN       2

// For testing purposes - will output variables to standard output
static Boolean DEBUG = TRUE ;


/* Rover 5 specifics */
/*****************************************************************************/

// Pulse Width Modulation and Direction pins (indexes correspond to channel)
int motor_pwm[5] ;
int motor_dir[5] ;

// Rover's speed: BACKWARDS = [-100, 0], FORWARDS = [0, 100]
int roverSpeed = 0 ;

// Rover's direction state - includes spin left, right
int roverDirection = ROVER_FORWARDS ;

// Rover's turn angle: LEFT = [90, 180], RIGHT = [0, 90]
int roverTurnAngle = 90 ;

// Influences inner wheels speed during turns
int turnSensitivity = 1 ;


/* 
 *  WiringPi specific
 *      Sets four GPIOs to be digital outputs and
 *      four GPIOs to generate software PWM
 *
 *      The pins have already been read from the
 *      configuration file
 */
void roverSetup (void)
{
  wiringPiSetup () ;

  // Setting the Direction pins as OUTPUTs
  pinMode (motor_dir[1], OUTPUT) ;
  pinMode (motor_dir[2], OUTPUT) ;
  pinMode (motor_dir[3], OUTPUT) ;
  pinMode (motor_dir[4], OUTPUT) ;
  
  // Create and initialise the Software Pulse-Width Modulation pins
  softPwmCreate (motor_pwm[1], 0, 100) ;
  softPwmCreate (motor_pwm[2], 0, 100) ;
  softPwmCreate (motor_pwm[3], 0, 100) ;
  softPwmCreate (motor_pwm[4], 0, 100) ;
} // roverSetup


/* 
 *  WiringPi specific
 *    Sets the left wheels/track to FORWARDS direction
 */
void roverLeftDirectionForwards (void)
{
  digitalWrite (motor_dir[1], 1) ;
  digitalWrite (motor_dir[2], 0) ;  
} // roverLeftDirectionForwards


/* 
 *  WiringPi specific
 *      Sets the right wheels/track to FORWARDS direction
 */
void roverRightDirectionForwards (void)
{
  digitalWrite (motor_dir[3], 1) ;
  digitalWrite (motor_dir[4], 0) ; 
} // roverRightDirectionForwards


/* 
 *  WiringPi specific
 *      Sets the left wheels/track to BACKWARDS direction
 */
void roverLeftDirectionBackwards (void)
{
  digitalWrite (motor_dir[1], 0) ;
  digitalWrite (motor_dir[2], 1) ;
} // roverLeftDirectionBackwards


/* 
 *  WiringPi specific
 *      Sets the right wheels/track to BACKWARDS direction
 */
void roverRightDirectionBackwards (void)
{
  digitalWrite (motor_dir[3], 0) ;
  digitalWrite (motor_dir[4], 1) ; 
} // roverRightDirectionBackwards


/*
 *  WiringPi specific
 *      Sets the left wheels/track speed 
 *      NOTE: the speed may be negative, so set the absolute value
 */
void roverLeftSpeed (int speedToSet)
{
  softPwmWrite (motor_pwm[1], abs (speedToSet)) ;
  softPwmWrite (motor_pwm[2], abs (speedToSet)) ;
} // roverLeftSpeed


/*
 *  WiringPi specific
 *      Sets the right wheels/track speed 
 *      NOTE: the speed may be negative, so set the absolute value
 */
void roverRightSpeed (int speedToSet)
{
  softPwmWrite (motor_pwm[3], abs (speedToSet)) ;
  softPwmWrite (motor_pwm[4], abs (speedToSet)) ;
} // roverRightSpeed


/*
 *  Rover 5 specific
 *      Sets the left and right wheels/track speed when the rover is TURNING
 *      Based on the TURNING ANGLE, the inner wheels lose power
 */
void roverUpdateLeftRightSpeed (void)
{
	//
  int innerWheelsSpeed = abs (roverSpeed) * sin (roverTurnAngle * PI / 180) / turnSensitivity ;  
  
  //
  if (roverTurnAngle > 90)  // Turning LEFT
  {
    roverLeftSpeed  (innerWheelsSpeed) ;
    roverRightSpeed (roverSpeed) ;

    if (DEBUG == TRUE)
    {
      printf ("ROVER:\n roverUpdateLeftRightSpeed():\n") ;
      printf ("   roverLeftSpeed = %d\n", innerWheelsSpeed) ;
      printf ("   roverRightSpeed = %d\n\n", roverSpeed) ;
    } // if
  } // if
  else
  if (roverTurnAngle < 90) // Turning RIGHT
  {
    roverLeftSpeed  (roverSpeed) ;
    roverRightSpeed (innerWheelsSpeed) ;

    if (DEBUG == TRUE)
    {
      printf ("ROVER:\n roverUpdateLeftRightSpeed():\n") ;
      printf ("   roverLeftSpeed = %d\n", roverSpeed) ;
      printf ("   roverRightSpeed = %d\n\n", innerWheelsSpeed) ;
    } // if
  } // if
  else // NO turn i.e. straight FORWARDS or BACKWARDS
  {
    roverLeftSpeed  (roverSpeed) ;
    roverRightSpeed (roverSpeed) ;

    if (DEBUG == TRUE)
    {
      printf ("ROVER:\n roverUpdateLeftRightSpeed():\n") ;
      printf ("   roverLeftSpeed = %d\n", roverSpeed) ;
      printf ("   roverRightSpeed = %d\n\n", roverSpeed) ;
    } // if
  } // else
} // roverUpdateLeftRightSpeed


/*
 * C Extension for Python
 *    This function sets up the ROVER 
 *
 *    It reads the configuration file, pidroid.conf, and gets the
 *    direction and PWM pins the motors have been connected to.
 *
 *    The numbering of the pins is with respect to WiringPi naming!
 *    see http://wiringpi.com/pins/
 ******************************************************************************
 */
static PyObject* setup (PyObject *self, PyObject *args)
{  
  FILE *configFile = fopen ("pidroid.conf", "r") ;
  char buffer[100] ;
  int index = 0, pin ;
  
  // Read line by line from the config file until the end 
  while (fgets (buffer, sizeof (buffer), configFile) != NULL)
  {
    // Keep reading lines until you reach the BEGINING of a block
    // i.e. ignores comment lines
    while (strcmp (buffer, "begin\n") != 0)
      fgets (buffer, sizeof (buffer), configFile) ;
    
    index++ ;

    // Keep reading lines until you reach the END of the block
    while (strcmp (buffer, "end") != 0)
    {
      fscanf (configFile, "%s %d", buffer, &pin) ;
      
      // The PWM pin is set here
      if (strcmp (buffer, "pwm") == 0)
        motor_pwm[index] = pin ;
      else
      // The direction pin is set here
      if (strcmp (buffer, "dir") == 0)
        motor_dir[index] = pin ;      
    } // while
  } // while
  fclose (configFile) ;

  // Setup WiringPi with the configured pins
  roverSetup () ;

  if (DEBUG == TRUE)
  {
    printf ("ROVER:\n setup():\n") ;
    printf ("   pwm: %d, %d, %d, %d\n", motor_pwm[1], motor_pwm[2], motor_pwm[3], motor_pwm[4]) ;
    printf ("   dir: %d, %d, %d, %d\n", motor_dir[1], motor_dir[2], motor_dir[3], motor_dir[4]) ;
    printf ("   roverTurnAngle: %d\n", roverTurnAngle) ;
    printf ("   roverSpeed: %d\n", roverSpeed) ;
    printf ("   roverDirection: %d\n", roverDirection) ;
    printf ("   turnSensitivity: %d\n\n", turnSensitivity) ;    
  } // if

  // Build a tuple and return it back to the Python Controller
  return Py_BuildValue ("(iiiiiiiiiiii)",
    motor_pwm[1], motor_pwm[2], motor_pwm[3], motor_pwm[4],
    motor_dir[1], motor_dir[2], motor_dir[3], motor_dir[4],
    roverSpeed, roverDirection, roverTurnAngle, turnSensitivity) ;
} // setup


/*
 * C Extension for Python
 *    This function sets the TURN ANGLE of the ROVER
 *
 *    When the TURN ANGLE is updated, the speed of the INNER WHEELS
 *    needs to be updated as well.
 ******************************************************************************
 */
static PyObject* setTurnAngle (PyObject *self, PyObject *args)
{
  // Parse the tuple arguments from python
  int turnAngleToSet ;

  PyArg_ParseTuple (args, "((iiiiiiiiiiii)(i))", 
    &motor_pwm[1], &motor_pwm[2], &motor_pwm[3], &motor_pwm[4],
    &motor_dir[1], &motor_dir[2], &motor_dir[3], &motor_dir[4],
    &roverSpeed, &roverDirection, &roverTurnAngle, &turnSensitivity,
    &turnAngleToSet) ; 
  
  // Set the new turn angle ad update the speeds
  roverTurnAngle = turnAngleToSet ;
  roverUpdateLeftRightSpeed () ;

  if (DEBUG == TRUE)
  {
    printf ("ROVER:\n setTurnAngle():\n") ;
    printf ("   pwm: %d, %d, %d, %d\n", motor_pwm[1], motor_pwm[2], motor_pwm[3], motor_pwm[4]) ;
    printf ("   dir: %d, %d, %d, %d\n", motor_dir[1], motor_dir[2], motor_dir[3], motor_dir[4]) ;
    printf ("   roverTurnAngle: %d\n", roverTurnAngle) ;
    printf ("   roverSpeed: %d\n", roverSpeed) ;
    printf ("   roverDirection: %d\n", roverDirection) ;
    printf ("   turnSensitivity: %d\n", turnSensitivity) ;
    printf ("   turnAngleToSet: %d\n\n", turnAngleToSet) ;
  } // if
  
  // Build a tuple and return it back to the Python Controller
  return Py_BuildValue ("(iiiiiiiiiiii)",
    motor_pwm[1], motor_pwm[2], motor_pwm[3], motor_pwm[4],
    motor_dir[1], motor_dir[2], motor_dir[3], motor_dir[4],
    roverSpeed, roverDirection, roverTurnAngle, turnSensitivity) ;
} // setTurnAngle


/*
 * C Extension for Python
 *    This function sets the SPEED of the ROVER
 *
 *    The SPEED is an integer in the interval [-100, 100].
 *    A negative speed implies BACKWARDS direction whilst 
 *    a positive speed implies FORWARDS direction.
 *
 *    If the SPIN option is toggled, then positive speed triggers
 *    a RIGHT turn, whilst a negative speed turns LEFT.
 ******************************************************************************
 */
static PyObject* setSpeed (PyObject *self, PyObject *args)
{
  // Parse the tuple arguments from python
  int speedToSet ;

  PyArg_ParseTuple (args, "((iiiiiiiiiiii)(i))", 
    &motor_pwm[1], &motor_pwm[2], &motor_pwm[3], &motor_pwm[4],
    &motor_dir[1], &motor_dir[2], &motor_dir[3], &motor_dir[4],
    &roverSpeed, &roverDirection, &roverTurnAngle, &turnSensitivity,
    &speedToSet) ;
  
  // Check the new speed against the old one to verify any direction 
  // change whilst taking into account the SPIN toggle.
  if (speedToSet > 0)
  {
    if (roverDirection == ROVER_BACKWARDS)
    {
      roverLeftDirectionForwards  () ;
      roverRightDirectionForwards () ;
      roverDirection = ROVER_FORWARDS ;
    } // if
    else
    if (roverDirection == ROVER_SPIN)
    {
      roverLeftDirectionForwards () ;
      roverRightDirectionBackwards () ;
    } // if
  } // if
  else
  if (speedToSet < 0)
  {
    if (roverDirection == ROVER_FORWARDS)
    {
      roverLeftDirectionBackwards  () ;
      roverRightDirectionBackwards () ;
      roverDirection = ROVER_BACKWARDS ;
    } // if
    else
    if (roverDirection == ROVER_SPIN)
    {
      roverLeftDirectionBackwards () ;
      roverRightDirectionForwards () ;
    } // if
  } // if
  
  // Set the new speed and update the ROVER speeds
  roverSpeed = speedToSet ;
  roverUpdateLeftRightSpeed () ;

  if (DEBUG == TRUE)
  {
    printf ("ROVER:\n setSpeed():\n") ;
    printf ("   pwm: %d, %d, %d, %d\n", motor_pwm[1], motor_pwm[2], motor_pwm[3], motor_pwm[4]) ;
    printf ("   dir: %d, %d, %d, %d\n", motor_dir[1], motor_dir[2], motor_dir[3], motor_dir[4]) ;
    printf ("   roverTurnAngle: %d\n", roverTurnAngle) ;
    printf ("   roverSpeed: %d\n", roverSpeed) ;
    printf ("   roverDirection: %d\n", roverDirection) ;
    printf ("   turnSensitivity: %d\n", turnSensitivity) ;
    printf ("   speedToSet: %d\n\n", speedToSet) ;
  } // if

  // Build a tuple and return it back to the Python Controller
  return Py_BuildValue ("(iiiiiiiiiiii)",
    motor_pwm[1], motor_pwm[2], motor_pwm[3], motor_pwm[4],
    motor_dir[1], motor_dir[2], motor_dir[3], motor_dir[4],
    roverSpeed, roverDirection, roverTurnAngle, turnSensitivity) ;
} // setSpeed


/*
 * C Extension for Python
 *    This function sets the TURN SENSITIVITY of the ROVER
 *
 *    If the turning is not satisfactory, i.e. too slow to turn,
 *    then increasing this parameter will have a bigger effect on the
 *    inner wheel's turn to speed ratio.
 ******************************************************************************
 */
static PyObject* setTurnSensitivity (PyObject *self, PyObject *args)
{
  // Parse the tuple arguments from python
  int turnSensitivityToSet ;

  PyArg_ParseTuple (args, "((iiiiiiiiiiii)(i))", 
    &motor_pwm[1], &motor_pwm[2], &motor_pwm[3], &motor_pwm[4],
    &motor_dir[1], &motor_dir[2], &motor_dir[3], &motor_dir[4],
    &roverSpeed, &roverDirection, &roverTurnAngle, &turnSensitivity,
    &turnSensitivityToSet) ;
  
  // Set the new sensitivity; nothing more to update
  turnSensitivity = turnSensitivityToSet ;

  if (DEBUG == TRUE)
  {
    printf ("ROVER:\n setTurnSensitivity():\n") ;
    printf ("   pwm: %d, %d, %d, %d\n", motor_pwm[1], motor_pwm[2], motor_pwm[3], motor_pwm[4]) ;
    printf ("   dir: %d, %d, %d, %d\n", motor_dir[1], motor_dir[2], motor_dir[3], motor_dir[4]) ;
    printf ("   roverTurnAngle: %d\n", roverTurnAngle) ;
    printf ("   roverSpeed: %d\n", roverSpeed) ;
    printf ("   roverDirection: %d\n", roverDirection) ;
    printf ("   turnSensitivity: %d\n", turnSensitivity) ;
    printf ("   turnSensitivityToSet: %d\n\n", turnSensitivityToSet) ;
  } // if

  // Build a tuple and return it back to the Python Controller
  return Py_BuildValue ("(iiiiiiiiiiii)",
    motor_pwm[1], motor_pwm[2], motor_pwm[3], motor_pwm[4],
    motor_dir[1], motor_dir[2], motor_dir[3], motor_dir[4],
    roverSpeed, roverDirection, roverTurnAngle, turnSensitivity) ;
} // setTurnSensitivity


/*
 * C Extension for Python
 *    This function toggles SPIN on/off on the ROVER
 *
 *    For the ROVER to SPIN, the LEFT and RIGHT wheels/tracks
 *    need to have OPPOSITE directions.
 *      i.e. to SPIN LEFT, LEFT track BACKWARDS and RIGHT track FORWARDS
 ******************************************************************************
 */
static PyObject* toggleSpin (PyObject *self, PyObject *args)
{
  // Parse the tuple arguments from python
  int spinToSet ;

  PyArg_ParseTuple (args, "((iiiiiiiiiiii)(i))", 
    &motor_pwm[1], &motor_pwm[2], &motor_pwm[3], &motor_pwm[4],
    &motor_dir[1], &motor_dir[2], &motor_dir[3], &motor_dir[4],
    &roverSpeed, &roverDirection, &roverTurnAngle, &turnSensitivity,
    &spinToSet) ;
  
  // Here we toggle SPIN on/off; when off, set the direction.. FORWARDS
  if (spinToSet == TRUE)
    roverDirection = ROVER_SPIN ;
  else
    roverDirection = ROVER_FORWARDS ;

  if (DEBUG == TRUE)
  {
    printf ("ROVER:\n setTurnSensitivity():\n") ;
    printf ("   pwm: %d, %d, %d, %d\n", motor_pwm[1], motor_pwm[2], motor_pwm[3], motor_pwm[4]) ;
    printf ("   dir: %d, %d, %d, %d\n", motor_dir[1], motor_dir[2], motor_dir[3], motor_dir[4]) ;
    printf ("   roverTurnAngle: %d\n", roverTurnAngle) ;
    printf ("   roverSpeed: %d\n", roverSpeed) ;
    printf ("   roverDirection: %d\n", roverDirection) ;
    printf ("   turnSensitivity: %d\n", turnSensitivity) ;
    printf ("   spinToSet: %d\n\n", spinToSet) ;
  } // if

  // Build a tuple and return it back to the Python Controller
  return Py_BuildValue ("(iiiiiiiiiiii)",
    motor_pwm[1], motor_pwm[2], motor_pwm[3], motor_pwm[4],
    motor_dir[1], motor_dir[2], motor_dir[3], motor_dir[4],
    roverSpeed, roverDirection, roverTurnAngle, turnSensitivity) ;
} // spinToSet


/*
 * Binding python module function names with the C ones
 ******************************************************************************
 */
static PyMethodDef rover_methods[] = {
  {"setup",               setup,                METH_VARARGS},
  {"setTurnAngle",        setTurnAngle,         METH_VARARGS},
  {"setSpeed",            setSpeed,             METH_VARARGS},
  {"setTurnSensitivity",  setTurnSensitivity,   METH_VARARGS},
  {"toggleSpin",          toggleSpin,           METH_VARARGS}  
} ; // Rover_methods


/*
 * The module's initialisation function as required by the Python API.
 ******************************************************************************
 */
void initrover ()
{  
  (void) Py_InitModule("rover", rover_methods) ;
} // initRover
