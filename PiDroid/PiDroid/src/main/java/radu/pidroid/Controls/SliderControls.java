/*
  SliderControls.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.Controls;

import android.util.Log;
import android.widget.SeekBar;

import radu.pidroid.Managers.ControlsManager;
import radu.pidroid.R;
import radu.pidroid.Robot.MotorsController.Direction;
import radu.pidroid.Robot.RoboticPlatform;


public class SliderControls implements SeekBar.OnSeekBarChangeListener {

    // references to modules
    private ControlsManager controls;
    private RoboticPlatform robot;

    // slider controls state variables
    private int progress;
    private Direction direction;


    public SliderControls(ControlsManager controls, RoboticPlatform robot) {
        this.controls = controls;
        this.robot = robot;

        this.progress = 0;
        this.direction = Direction.FORWARDS;
    } // constructor


    private void notifyRobot() {
        //
        robot.notifyMotors(progress, direction);
    } // notifyRobot


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // TODO: invert action if statements
         switch (seekBar.getId()) {

            case R.id.forwardsPowerSeekBar:
                if (controls.backwardsPowerSeekBar.getProgress() > 0) {
                    controls.forwardsPowerSeekBar.setProgress(0);
                }
                else {
                    controls.forwardsPowerProgressBar.setProgress(progress);
                    direction = Direction.FORWARDS;
                }
                break;

            case R.id.backwardsPowerSeekBar:
                if (controls.forwardsPowerSeekBar.getProgress() > 0) {
                    controls.backwardsPowerSeekBar.setProgress(0);
                }
                else {
                    controls.backwardsPowerProgressBar.setProgress(progress);
                    direction = Direction.BACKWARDS;
                }
                break;

            default:
                Log.e("onProgressChanged():", "fell through default case!");
        } // switch

        this.progress = progress;
        notifyRobot();
    } // onProgressChanged


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    } // onStartTrackingTouch


    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO: set the progress of the seekBar be set to the notified value
    } // onStopTrackingTouch

} // SliderControls
