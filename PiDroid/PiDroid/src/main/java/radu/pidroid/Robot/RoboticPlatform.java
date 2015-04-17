/*
  RoboticPlatform.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.Robot;

import android.util.Log;

import radu.pidroid.Activities.Controller;
import radu.pidroid.Connector.Messenger;
import radu.pidroid.Robot.MotorsController.Direction;
import radu.pidroid.Robot.MotorsController.State;


public class RoboticPlatform {

    //
    private MotorsController motors;
    private CameraController camera;


    public RoboticPlatform(Controller controller, Messenger messenger) {
        this.motors = new MotorsController(controller, messenger);
        this.camera = new CameraController(controller, messenger);
    } // constructor


    public void notifyMotors(int power, Direction direction, State state) {
        notifyMotors(power, direction);

        if (power == 0 && state != State.STOPPED) {
            Log.e("RoboticPlatform", "notifyMotors(): power is 0 but state != STOPPED!");
            return;
        }
        motors.updateMotorsState(state);
    } // notifyMotors


    public void notifyMotors(int power, Direction direction) {
        notifyMotors(power);
        motors.updateMotorsDirection(direction);
    } // notifyMotors


    public void notifyMotors(int power) {
        if (power < 0 || power > 100) {
            Log.e("RoboticPlatform", "notifyMotors(): power: either < 0 or > 100!");
            return;
        }
        motors.updateMotorsPower(power);
    } // notifyMotors


    public void notifyMotorsTurnAngle(int angle) {
        if (angle < 0 || angle > 180) {
            Log.e("RoboticPlatform", "notifyMotorsTurnAngle(): angle: either < 0 or > 180!");
            return;
        }
        motors.updateMotorsTurnAngle(angle);
    } // notifyMotorsTurnAngle

    public void notifyCamera(int cameraX, int cameraY) {
        if (cameraX < 0 || cameraX > 100) {
            Log.e("RoboticPlatform", "notifyCamera(): cameraX: either < 0 or > 100!");
            return;
        }
        if (cameraY < 0 || cameraY > 100) {
            Log.e("RoboticPlatform", "notifyCamera() cameraY: either < 0 or > 100!");
            return;
        }
        camera.updateCameraPosition(cameraX, cameraY);
    } // notifyCamera

} // RoboticPlatform
