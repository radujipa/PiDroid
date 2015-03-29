/*
  GyroscopeControls.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.Controls;

import android.util.Log;
import android.view.View;

import radu.pidroid.Managers.ControlsManager;
import radu.pidroid.Managers.SensorsManager;
import radu.pidroid.Managers.SettingsManager;
import radu.pidroid.Robot.RoboticPlatform;


public class GyroscopeControls implements SensorsManager.TiltListener {

    // references to modules
    private ControlsManager controls;
    private RoboticPlatform robot;
    private SettingsManager settings;

    //
    private int tiltAngle;


    public GyroscopeControls(ControlsManager controls, RoboticPlatform robot, SettingsManager settings) {
        this.controls = controls;
        this.robot = robot;
        this.settings = settings;

        this.tiltAngle = 90;  // the normal landscape view
    } // constructor


    private void notifyRobot() {
        //
        robot.notifyMotorsTurnAngle(tiltAngle);
    } // notifyRobot


    @Override
    public void onTilt(int tiltAngle) {
        //
        if (controls.levelIndicatorImageView.getVisibility() == View.VISIBLE
                && tiltAngle <= 90 + settings.maximumTiltAngle
                && tiltAngle >= 90 - settings.maximumTiltAngle) {
            controls.levelIndicatorImageView.setRotation(tiltAngle - 90);
        } // if

        //
        if (tiltAngle > 90 + settings.maximumTiltAngle) tiltAngle = 90 + settings.maximumTiltAngle;
        if (tiltAngle < 90 - settings.maximumTiltAngle) tiltAngle = 90 - settings.maximumTiltAngle;

        //
        tiltAngle = (int)Math.round((tiltAngle - (90 - settings.maximumTiltAngle)) * 90.0 / settings.maximumTiltAngle) ;

        this.tiltAngle = tiltAngle;
        notifyRobot();
        Log.d("GyroscopeControls:", "tiltControls(): tiltAngle = " + tiltAngle);
    } // onTilt

} // GyroscopeControls
