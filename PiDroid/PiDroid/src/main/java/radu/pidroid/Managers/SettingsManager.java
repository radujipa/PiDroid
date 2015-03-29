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
    private final static String EXTRA_SERVER_IP = "radu.pidroid.SERVERIP";
    private final static String EXTRA_SERVER_PORT = "radu.pidroid.SERVERPORT";

    //
    private final static String EXTRA_CONTROLS_ID             = "radu.pidroid.CONTROLSID";
    private final static String EXTRA_MAX_TILT_ANGLE          = "radu.pidroid.MAXTILTANGLE";
    private final static String EXTRA_ACCELERATION_TIME       = "radu.pidroid.ACCELERATIONTIME";
    private final static String EXTRA_DECELERATION_TIME       = "radu.pidroid.DECELERATIONTIME";
    private final static String EXTRA_TOUCH_RESPONSIVENESS    = "radu.pidroid.TOUCHRESPONSIVNESS";
    private final static String EXTRA_TURN_SENSITIVITY        = "radu.pidroid.TURNSENSITIVITY";
    private final static String EXTRA_HUD_INDEX               = "radu.pidroid.HUDINDEX";
    private final static String EXTRA_LEVEL_INDICATOR_ON      = "radu.pidroid.LEVELINDICATORON";
    private final static String EXTRA_CAMERA_STABILISATION_ON = "radu.pidroid.CAMERASTABILISATIONON";
    private final static String EXTRA_TUTORIALS_ON            = "radu.pidroid.TUTORIALSON";


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
    public int currentHUDIndex;
    public boolean levelIndicatorON;
    public boolean cameraStabilisationON;
    public boolean tutorialsON;

    //
    private SharedPreferences preferences;


    public SettingsManager(Activity activity) {
        preferences = activity.getSharedPreferences("PiDroid", Context.MODE_PRIVATE);
    } // constructor


    public void load() {
        // Main activity settings
        serverIP = preferences.getString(EXTRA_SERVER_IP, "0.0.0.0");
        serverPort = preferences.getString(EXTRA_SERVER_PORT, "8088");

        // Controller activity settings
        controlsID = preferences.getInt(EXTRA_CONTROLS_ID, Controls.TOUCH_GYRO.getID());
        maximumTiltAngle = preferences.getInt(EXTRA_MAX_TILT_ANGLE, 45);
        accelerationTime = preferences.getInt(EXTRA_ACCELERATION_TIME, 1500);
        decelerationTime = preferences.getInt(EXTRA_DECELERATION_TIME, 1000);
        touchResponsiveness = preferences.getInt(EXTRA_TOUCH_RESPONSIVENESS, 200);
        turnSensitivity = preferences.getInt(EXTRA_TURN_SENSITIVITY, 1);
        currentHUDIndex = preferences.getInt(EXTRA_HUD_INDEX, 0);
        levelIndicatorON = preferences.getBoolean(EXTRA_LEVEL_INDICATOR_ON, true);
        cameraStabilisationON = preferences.getBoolean(EXTRA_CAMERA_STABILISATION_ON, true);
        tutorialsON = preferences.getBoolean(EXTRA_TUTORIALS_ON, true);

        Log.d("SettingsManager:", "load(): all settings loaded");
    } // load


    public void save() {
        SharedPreferences.Editor editor = preferences.edit();

        // Main activity settings
        editor.putString(EXTRA_SERVER_IP, serverIP);
        editor.putString(EXTRA_SERVER_PORT, serverPort);

        // Controller activity settings
        editor.putInt(EXTRA_CONTROLS_ID, controlsID);
        editor.putInt(EXTRA_MAX_TILT_ANGLE, maximumTiltAngle);
        editor.putInt(EXTRA_ACCELERATION_TIME, accelerationTime);
        editor.putInt(EXTRA_DECELERATION_TIME, decelerationTime);
        editor.putInt(EXTRA_TOUCH_RESPONSIVENESS, touchResponsiveness);
        editor.putInt(EXTRA_TURN_SENSITIVITY, turnSensitivity);
        editor.putInt(EXTRA_HUD_INDEX, currentHUDIndex);
        editor.putBoolean(EXTRA_LEVEL_INDICATOR_ON, levelIndicatorON);
        editor.putBoolean(EXTRA_CAMERA_STABILISATION_ON, cameraStabilisationON);
        editor.putBoolean(EXTRA_TUTORIALS_ON, tutorialsON);

        editor.apply();
        Log.d("SettingsManager:", "save(): all settings saved");
    } // save


    public void clear() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
        Log.d("SettingsManager:", "clear(): all settings cleared");
    } // clear

} // SettingsManager
