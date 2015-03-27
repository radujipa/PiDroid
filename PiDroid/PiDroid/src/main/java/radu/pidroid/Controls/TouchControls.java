/*
  TouchControls.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.Controls;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

import radu.pidroid.Managers.ControlsManager;
import radu.pidroid.Managers.SettingsManager;
import radu.pidroid.R;
import radu.pidroid.Robot.MotorsController.Direction;
import radu.pidroid.Robot.MotorsController.State;
import radu.pidroid.Robot.RoboticPlatform;


public class TouchControls implements View.OnTouchListener {

    // update rate of forwards/backwards progressbar
    private static final int TIMER_PERIOD = 50; // (ms) ~ 20fps


    // references to modules
    private ControlsManager controls;
    private RoboticPlatform robot;
    private SettingsManager settings;

    // a timer object for measuring and coordinating acceleration/deceleration rates
    private Timer timer;

    // touch controls state variables
    private int progress;
    private Direction direction;
    private State accelerationState;


    public TouchControls(ControlsManager controls, RoboticPlatform robot, SettingsManager settings) {
        this.controls = controls;
        this.robot = robot;
        this.settings = settings;

        this.timer = new Timer();
        this.progress = 0;
    } // constructor


    private void notifyRobot() {
        //
        robot.notifyMotors(progress, direction, accelerationState);
    } // notifyRobot


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // TODO: invert action if statements
        // TODO: think of a better way of doing this
        int action = motionEvent.getActionMasked();
        switch (view.getId()) {

            case R.id.forwardsPowerImageView:
                //
                if (action == MotionEvent.ACTION_DOWN) {
                    if (accelerationState != State.STOPPED && direction != Direction.FORWARDS) {
                        accelerationState = State.STOPPED;
                        progress = 0;
                        notifyRobot();
                    } // if
                    else {
                        direction = Direction.FORWARDS;
                        accelerationState = State.ACCELERATING;
                        resetTimer();
                    } // else
                } // if
                break;

            case R.id.backwardsPowerImageView:
                //
                if (action == MotionEvent.ACTION_DOWN) {
                    if (accelerationState != State.STOPPED && direction != Direction.BACKWARDS) {
                        accelerationState = State.STOPPED;
                        progress = 0;
                        notifyRobot();
                    } // if
                    else {
                        direction = Direction.BACKWARDS;
                        accelerationState = State.ACCELERATING;
                        resetTimer();
                    } // else
                } // if
                break;

            default:
                Log.e("onTouch():", "fell through default case!");
        } // switch

        //
        if ((action == MotionEvent.ACTION_UP || !isTouchOnView(view, motionEvent))
          && accelerationState != State.DECELERATING) {
            accelerationState = State.DECELERATING;
            resetTimer();
        } // if

        return true;
    } // onTouch


    private boolean isTouchOnView(View view, MotionEvent motionEvent) {
        int viewPosition[] = new int[4];
        view.getLocationOnScreen(viewPosition);

        viewPosition[2] = viewPosition[0] + view.getWidth();
        viewPosition[3] = viewPosition[1] + view.getHeight();

        return motionEvent.getRawX() >= viewPosition[0] && motionEvent.getRawX() <= viewPosition[2]
            && motionEvent.getRawY() >= viewPosition[1] && motionEvent.getRawY() <= viewPosition[3];
    } // isTouchOnView


    private void resetTimer() {
        timer.cancel(); timer.purge();
        timer = new Timer();

        timer.schedule(new UpdateProgressTask(), settings.touchResponsiveness, TIMER_PERIOD);
    } // resetTimer


    private class UpdateProgressTask extends TimerTask {
        @Override
        public void run() {
            //
            if (accelerationState == State.ACCELERATING && progress < 100
             || accelerationState == State.DECELERATING && progress > 0)
                progress = calculateProgress();

            //
            if (progress == 0) {
                timer.cancel(); timer.purge();
                accelerationState = State.STOPPED;
            } // if

            //
            notifyRobot();

            //
            if (direction == Direction.FORWARDS) {
                controls.forwardsPowerProgressBar.setProgress(progress);
                controls.backwardsPowerProgressBar.setProgress(0);
            } // if
            else {
                controls.forwardsPowerProgressBar.setProgress(0);
                controls.backwardsPowerProgressBar.setProgress(progress);
            } // else
        } // run


        private int calculateProgress() {
            if (accelerationState == State.ACCELERATING) {
                progress = (int) (progress + (double) TIMER_PERIOD / settings.accelerationTime * 100);
                progress = progress > 100 ? 100 : progress;
            }
            else
            if (accelerationState == State.DECELERATING) {
                progress = (int) (progress - (double) TIMER_PERIOD / settings.decelerationTime * 100);
                progress = progress < 0 ? 0 : progress;
            }
            return progress;
        } // calculateProgress
    } // UpdateProgressTask

} // TouchControls
