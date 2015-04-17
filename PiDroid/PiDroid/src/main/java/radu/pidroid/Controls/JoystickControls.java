/*
  JoystickControls.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.Controls;

import android.util.Log;

import radu.pidroid.Joystick.JoystickView;
import radu.pidroid.R;
import radu.pidroid.Robot.RoboticPlatform;


public class JoystickControls implements JoystickView.MoveListener {

    // references to modules
    private RoboticPlatform robot;

    //
    private int power;
    private int tiltAngle;
    private int cameraX, cameraY;


    public JoystickControls(RoboticPlatform robot) {
        this.robot = robot;

        this.power = 0;
        this.tiltAngle = 90;  // device is normal landscape mode
        this.cameraX = this.cameraY = 0;
    } // constructor


    private void notifyRobot() {
        //
        robot.notifyMotors(power);
        robot.notifyCamera(cameraX, cameraY);
    } // notifyRobot


    @Override
    public void onMove(JoystickView view, double x, double y) {
        boolean shouldNotify = false;
        // TODO: why *101 for percentage conversion? remove modulo?
        int newX = ((int)(x * 101)) / 20 * 20;
        int newY = ((int)(y * 101)) / 20 * -20;  // TODO: why invert?
        Log.d("JoystickControls", "onMove(): x = " + x + ", y = " + y);

        switch (view.getId()) {

            case R.id.cameraJoystickView:
            case R.id.largeCameraJoystickView:
                // update any new
                if (cameraX != newX || cameraY != newY) {
                    cameraX = newX; cameraY = newY;
                    shouldNotify = true;
                } // if
                break;

            case R.id.directionJoystickView:
                int newTiltAngle;
                // TODO: recheck this to make sure it's correct
                // compute the angle made by the joystick measured with respect to the trigonometric circle
                if (newX > 0)
                    newTiltAngle = (int)(Math.toDegrees(Math.atan(Math.abs((double)newY / newX))));
                else
                    newTiltAngle = (int)(Math.toDegrees(Math.atan(Math.abs((double)newX / newY))) + 90);
                Log.d("JoystickControls", "onMove(): newTiltAngle = " + newTiltAngle);

                // If there is a new turnAngle (or turning angle), tell PiDroid about it.
                if (power != newY || tiltAngle != newTiltAngle) {
                    power = newY; tiltAngle = newTiltAngle;
                    shouldNotify = true;
                } // if
                break;

            default:
                Log.e("JoystickControls", "onMove(): fell through default case!");
                return;
        } // switch

        if (shouldNotify) notifyRobot();
    } // onMove


    @Override
    public void onRelease(JoystickView view) {
        switch (view.getId()) {

            case R.id.cameraJoystickView:
            case R.id.largeCameraJoystickView:
                // TODO: have this as an option for the user to decide
                // notifyRobot();
                break;

            case R.id.directionJoystickView:
                // when the user lets go of the joystick, we stop the robot immediately
                power = 0;
                tiltAngle = 90;
                notifyRobot();
                break;

            default:
                Log.e("JoystickControls", "onRelease():  fell through default case!");
        } // switch
    } // onRelease

} // JoystickControls
