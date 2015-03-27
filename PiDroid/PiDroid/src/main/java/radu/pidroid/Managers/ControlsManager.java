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


public class ControlsManager implements View.OnClickListener {

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


    // references to modules
    private RoboticPlatform robot;
    private SettingsManager settings;
    private VideoFeedManager video;
    private Messenger messenger;

    //
    public boolean tiltControlsON;
    public boolean spinControlON;


    public ControlsManager(Controller controller, SettingsManager settings, VideoFeedManager video,
                           Messenger messenger, MethodInvocation invocator) {
        this.settings = settings;
        this.video = video;
        this.messenger = messenger;

        this.robot = new RoboticPlatform(messenger);

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

        this.joystickControls = new JoystickControls(robot);
        joystickControlsLayout = (RelativeLayout) controller.findViewById(R.id.JoystickControlsLayout);
        directionJoystickView = (JoystickView) controller.findViewById(R.id.directionJoystickView);
        directionJoystickView.setMoveListener(joystickControls);
        largeCameraJoystickView = (JoystickView) controller.findViewById(R.id.largeCameraJoystickView);
        largeCameraJoystickView.setMoveListener(joystickControls);
        smallCameraJoystickView = (JoystickView) controller.findViewById(R.id.cameraJoystickView);
        smallCameraJoystickView.setMoveListener(joystickControls);
        smallCameraJoystickView.setTag("smallCamera");

        toggleSpinImageView = (ImageView) controller.findViewById(R.id.toggleSpinImageView);
        toggleSpinImageView.setOnClickListener(this);

        this.voiceControls = new VoiceControls(controller, invocator);
        speechButton = (ImageView) controller.findViewById(R.id.speechButton);
        speechButton.setOnClickListener(voiceControls);

        this.spinControlON = false;
    } // constructor


    public void setControls(Controls controls) {
        if (controls.getID() == Controls.TOUCH_GYRO.getID()) {
            touchControlsLayout.setVisibility(View.VISIBLE);
            smallCameraJoystickView.setVisibility(View.VISIBLE);
            sliderControlsLayout.setVisibility(View.INVISIBLE);
            joystickControlsLayout.setVisibility(View.INVISIBLE);
            video.setCameraStabilisation(settings.cameraStabilisationON);
            tiltControlsON = true;
        }
        else
        if (controls.getID() == Controls.SLIDER_GYRO.getID()) {
            sliderControlsLayout.setVisibility(View.VISIBLE);
            smallCameraJoystickView.setVisibility(View.VISIBLE);
            touchControlsLayout.setVisibility(View.INVISIBLE);
            joystickControlsLayout.setVisibility(View.INVISIBLE);
            video.setCameraStabilisation(settings.cameraStabilisationON);
            tiltControlsON = true;
        }
        else
        if (controls.getID() == Controls.JOYSTICKS.getID()) {
            joystickControlsLayout.setVisibility(View.VISIBLE);
            touchControlsLayout.setVisibility(View.INVISIBLE);
            sliderControlsLayout.setVisibility(View.INVISIBLE);
            smallCameraJoystickView.setVisibility(View.INVISIBLE);
            video.setCameraStabilisation(false);
            tiltControlsON = false;
        }
        else {
            Log.e("setControls():", "fell through default case controlsID = " + settings.controlsID);
            return;
        }
        settings.controlsID = controls.getID();
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

            default:
                Log.e("onClick()", "fell through default case!");
        } // switch
    } // onClick

} // ControlsManager
