/*
  ControlsManager.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.Managers;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import radu.pidroid.Activities.Controller;
import radu.pidroid.Connector.Messenger;
import radu.pidroid.Connector.MethodInvocation;
import radu.pidroid.Controls.GyroscopeControls;
import radu.pidroid.Controls.JoystickControls;
import radu.pidroid.Controls.SliderControls;
import radu.pidroid.Controls.TouchControls;
import radu.pidroid.Controls.VoiceControls;
import radu.pidroid.Joystick.JoystickView;
import radu.pidroid.R;
import radu.pidroid.Robot.RoboticPlatform;


public class ControlsManager implements View.OnClickListener, Controller.ActivityLifecycleListener {

    //
    public enum Controls {
        TOUCH_GYRO(1234), SLIDER_GYRO(1324), JOYSTICKS(4231);

        private int id;

        Controls(int index) {
            this.id = index;
        } // constructor

        public int getID() {
            return this.id;
        } // getID
    } // Controls


    public TouchControls touchControls;
    public RelativeLayout touchControlsLayout;
    public ImageView forwardsPowerImageView, backwardsPowerImageView;
    public ProgressBar forwardsPowerProgressBar, backwardsPowerProgressBar;

    public SliderControls sliderControls;
    public RelativeLayout sliderControlsLayout;
    public SeekBar forwardsPowerSeekBar, backwardsPowerSeekBar;

    public JoystickControls joystickControls;
    public RelativeLayout joystickControlsLayout;
    public JoystickView largeCameraJoystickView, directionJoystickView;
    public JoystickView smallCameraJoystickView;

    public GyroscopeControls gyroscopeControls;
    public ImageView levelIndicatorImageView;

    public ImageView toggleSpinImageView;

    public VoiceControls voiceControls;
    public ImageView speechButton;

    private Controller controller;
    private SettingsManager settings;
    private SensorsManager sensors;
    private VideoFeedManager video;
    private Messenger messenger;

    //
    public boolean tiltControlsON;
    public boolean spinControlON;


    public ControlsManager(Controller controller, SettingsManager settings, SensorsManager sensors,
                           VideoFeedManager video, Messenger messenger, MethodInvocation invocator) {

        controller.addActivityLifecycleListener(this);

        this.controller = controller;
        this.settings = settings;
        this.sensors = sensors;
        this.video = video;
        this.messenger = messenger;

        RoboticPlatform robot = new RoboticPlatform(controller, messenger);

        this.touchControls = new TouchControls(this, robot, settings);
        touchControlsLayout = (RelativeLayout) controller.findViewById(R.id.TouchControlsLayout);
        forwardsPowerImageView = (ImageView) controller.findViewById(R.id.forwardsPowerImageView);
        forwardsPowerImageView.setOnTouchListener(touchControls);
        backwardsPowerImageView = (ImageView) controller.findViewById(R.id.backwardsPowerImageView);
        backwardsPowerImageView.setOnTouchListener(touchControls);
        forwardsPowerProgressBar = (ProgressBar) controller.findViewById(R.id.forwardsPowerProgressBar);
        backwardsPowerProgressBar = (ProgressBar) controller.findViewById(R.id.backwardsPowerProgressBar);

        this.sliderControls = new SliderControls(this, robot);
        sliderControlsLayout = (RelativeLayout) controller.findViewById(R.id.SliderControlsLayout);
        forwardsPowerSeekBar = (SeekBar) controller.findViewById(R.id.forwardsPowerSeekBar);
        forwardsPowerSeekBar.setOnSeekBarChangeListener(sliderControls);
        backwardsPowerSeekBar = (SeekBar) controller.findViewById(R.id.backwardsPowerSeekBar);
        backwardsPowerSeekBar.setOnSeekBarChangeListener(sliderControls);

        this.gyroscopeControls = new GyroscopeControls(this, robot, settings);
        levelIndicatorImageView = (ImageView) controller.findViewById(R.id.levelIndicatorImageView);
        levelIndicatorImageView.setVisibility(settings.levelIndicatorON ? View.VISIBLE : View.INVISIBLE);
        sensors.setTiltListener(gyroscopeControls);

        this.joystickControls = new JoystickControls(robot);
        joystickControlsLayout = (RelativeLayout) controller.findViewById(R.id.JoystickControlsLayout);
        directionJoystickView = (JoystickView) controller.findViewById(R.id.directionJoystickView);
        directionJoystickView.addMoveListener(joystickControls);
        largeCameraJoystickView = (JoystickView) controller.findViewById(R.id.largeCameraJoystickView);
        largeCameraJoystickView.addMoveListener(joystickControls);
        smallCameraJoystickView = (JoystickView) controller.findViewById(R.id.cameraJoystickView);
        smallCameraJoystickView.addMoveListener(joystickControls);
        smallCameraJoystickView.setTag("smallCamera");

        toggleSpinImageView = (ImageView) controller.findViewById(R.id.toggleSpinImageView);
        toggleSpinImageView.setOnClickListener(this);

        this.voiceControls = new VoiceControls(controller, invocator);
        speechButton = (ImageView) controller.findViewById(R.id.speechButton);
        speechButton.setOnClickListener(voiceControls);

        this.tiltControlsON = true;
        this.spinControlON = false;

        setControls(settings.controlsID);
    } // constructor


    public void setControls(int controlsID) {
        if (controlsID == Controls.TOUCH_GYRO.getID()) {
            touchControlsLayout.setVisibility(View.VISIBLE);
            smallCameraJoystickView.setVisibility(View.VISIBLE);
            sliderControlsLayout.setVisibility(View.INVISIBLE);
            joystickControlsLayout.setVisibility(View.INVISIBLE);
            tiltControlsON = true;
            Toast.makeText(controller, "Touch controls have been set", Toast.LENGTH_SHORT).show();
        }
        else
        if (controlsID == Controls.SLIDER_GYRO.getID()) {
            sliderControlsLayout.setVisibility(View.VISIBLE);
            smallCameraJoystickView.setVisibility(View.VISIBLE);
            touchControlsLayout.setVisibility(View.INVISIBLE);
            joystickControlsLayout.setVisibility(View.INVISIBLE);
            tiltControlsON = true;
            Toast.makeText(controller, "Slider controls have been set", Toast.LENGTH_SHORT).show();
        }
        else
        if (controlsID == Controls.JOYSTICKS.getID()) {
            joystickControlsLayout.setVisibility(View.VISIBLE);
            touchControlsLayout.setVisibility(View.INVISIBLE);
            sliderControlsLayout.setVisibility(View.INVISIBLE);
            smallCameraJoystickView.setVisibility(View.INVISIBLE);
            tiltControlsON = false;
            Toast.makeText(controller, "Joystick controls have been set", Toast.LENGTH_SHORT).show();
        }
        else {
            Log.e("ControlsManager", "setControls(): fell through default case " +
                    "with controlsID = " + controlsID);
            return;
        }
        updateSensorsState();
        settings.controlsID = controlsID;
    } // setControls


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.toggleSpinImageView:
                // if spinControl is ON then tiltControls (gyros) are OFF and vice versa
                spinControlON  = !spinControlON;
                tiltControlsON = !spinControlON;

                // change the appearance of the button to
                if (spinControlON)
                    toggleSpinImageView.setImageResource(R.drawable.spin_button_down);  // toggled / on
                else
                    toggleSpinImageView.setImageResource(R.drawable.spin_button_up);  // not toggled / off

                // notify the robot to toggle motor direction inversion
                // i.e. left motors forwards, right motors backwards (for rovers / tanks)
                messenger.toggleSpin(spinControlON);
                break;

            default:
                Log.e("ControlsManager", "onClick(): fell through default case!");
                return;
        } // switch
        updateSensorsState();
    } // onClick


    private void updateSensorsState() {
        if (tiltControlsON) {
            sensors.start();
            video.setCameraStabilisation(settings.cameraStabilisationON);
        }
        else {
            sensors.stop();
            levelIndicatorImageView.setRotation(0);
            video.setCameraStabilisation(false);
        }
    } // updateSensorsState


    @Override
    public void onPause() {
        // when the Controller activity pauses, stop the sensors to save battery
        if (tiltControlsON) sensors.stop();
    } // onPause


    @Override
    public void onResume() {
        // when the Controller activity resumes, turn the sensors back on
        if (tiltControlsON) sensors.start();
    } // onResume

} // ControlsManager
