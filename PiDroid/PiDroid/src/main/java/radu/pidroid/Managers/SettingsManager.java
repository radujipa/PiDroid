/*
  SettingsManager.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.Managers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;

import radu.pidroid.Activities.Controller;
import radu.pidroid.Activities.Main;
import radu.pidroid.Managers.ControlsManager.Controls;


public class SettingsManager {

    //
    public final static String EXTRA_SERVER_IP = "radu.pidroid.SERVERIP";
    public final static String EXTRA_SERVER_PORT = "radu.pidroid.SERVERPORT";

    //
    public final static String EXTRA_CONTROLS_ID             = "radu.pidroid.CONTROLSID";
    public final static String EXTRA_MAX_TILT_ANGLE          = "radu.pidroid.MAXTILTANGLE";
    public final static String EXTRA_ACCELERATION_TIME       = "radu.pidroid.ACCELERATIONTIME";
    public final static String EXTRA_DECELERATION_TIME       = "radu.pidroid.DECELERATIONTIME";
    public final static String EXTRA_TOUCH_RESPONSIVENESS    = "radu.pidroid.TOUCHRESPONSIVNESS";
    public final static String EXTRA_TURN_SENSITIVITY        = "radu.pidroid.TURNSENSITIVITY";
    public final static String EXTRA_HUD_INDEX               = "radu.pidroid.HUDINDEX";
    public final static String EXTRA_LEVEL_INDICATOR_ON      = "radu.pidroid.LEVELINDICATORON";
    public final static String EXTRA_CAMERA_STABILISATION_ON = "radu.pidroid.CAMERASTABILISATIONON";
    public final static String EXTRA_TUTORIALS_ON            = "radu.pidroid.TUTORIALSON";

    //
    public String serverIP;
    public String serverPort;

    //
    public int controlsID;
    public int maximumTiltAngle;
    public int accelerationTime;
    public int decelerationTime;
    public int touchResponsiveness;
    public int turnSensitivity;
    public boolean tutorialsON;
    public boolean cameraStabilisationON;
    public int currentHUDIndex;

    //
    private Activity activity;


    public SettingsManager(Activity activity) {
        this.activity = activity;
    } // constructor


    public void load() {
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);

        if (activity instanceof Main) {
            serverIP = preferences.getString(EXTRA_SERVER_IP, "0.0.0.0");
            serverPort = preferences.getString(EXTRA_SERVER_PORT, "8088");
        }
        else if (activity instanceof Controller) {
            controlsID = preferences.getInt(EXTRA_CONTROLS_ID, Controls.TOUCH_GYRO.getID());
            maximumTiltAngle = preferences.getInt(EXTRA_MAX_TILT_ANGLE, 45);
            accelerationTime = preferences.getInt(EXTRA_ACCELERATION_TIME, 1500);
            decelerationTime = preferences.getInt(EXTRA_DECELERATION_TIME, 1000);
            touchResponsiveness = preferences.getInt(EXTRA_TOUCH_RESPONSIVENESS, 200);
            turnSensitivity = preferences.getInt(EXTRA_TURN_SENSITIVITY, 1);
            tutorialsON = preferences.getBoolean(EXTRA_TUTORIALS_ON, true);

            cameraStabilisationON = preferences.getBoolean(EXTRA_CAMERA_STABILISATION_ON, true);
            ((Controller)activity).videoFeed.videoFeedMjpegView.setCameraStabilisation(cameraStabilisationON);

            currentHUDIndex = preferences.getInt(EXTRA_HUD_INDEX, 0);
            ((Controller)activity).controls.levelIndicatorImageView
                    .setVisibility(preferences.getInt(EXTRA_LEVEL_INDICATOR_ON, View.VISIBLE));

            ((Controller)activity).hudImageView.setImageResource(((Controller)activity).hudResources[currentHUDIndex]);
        }
        else
            Log.d("save():", "not defined for activity");
    } // loadPreferences


    public void save() {
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if (activity instanceof Main) {
            editor.putString(EXTRA_SERVER_IP, serverIP);
            editor.putString(EXTRA_SERVER_PORT, serverPort);
        }
        else if (activity instanceof Controller) {
            editor.putInt(EXTRA_CONTROLS_ID, controlsID);
            editor.putInt(EXTRA_MAX_TILT_ANGLE, maximumTiltAngle);
            editor.putInt(EXTRA_ACCELERATION_TIME, accelerationTime);
            editor.putInt(EXTRA_DECELERATION_TIME, decelerationTime);
            editor.putInt(EXTRA_TOUCH_RESPONSIVENESS, touchResponsiveness);
            editor.putInt(EXTRA_TURN_SENSITIVITY, turnSensitivity);
            editor.putInt(EXTRA_HUD_INDEX, currentHUDIndex);
            editor.putInt(EXTRA_LEVEL_INDICATOR_ON, ((Controller)activity).controls.levelIndicatorImageView.getVisibility());
            editor.putBoolean(EXTRA_CAMERA_STABILISATION_ON, cameraStabilisationON);
            editor.putBoolean(EXTRA_TUTORIALS_ON, tutorialsON);
        }
        else
            Log.e("save():", "not defined for activity");

        editor.apply();
    } // savePreferences


    public void clear() {
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.clear();
        editor.apply();
    } // clearPreferences

} // SettingsManager
