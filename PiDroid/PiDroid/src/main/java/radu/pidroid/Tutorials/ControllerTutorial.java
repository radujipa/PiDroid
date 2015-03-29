/*
  ControllerTutorial.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.Tutorials;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.WindowManager;

import radu.pidroid.Activities.Controller;
import radu.pidroid.Managers.ControlsManager;
import radu.pidroid.Managers.ControlsManager.Controls;
import radu.pidroid.Managers.DrawerManager;


public class ControllerTutorial {

    // references to modules
    private Controller controller;
    private ControlsManager controls;
    private DrawerManager drawer;


    public ControllerTutorial(Controller controller, ControlsManager controls, DrawerManager drawer) {
        this.controller = controller;
        this.controls = controls;
        this.drawer = drawer;
    } // constructor


    public void start() {
        controller.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //
                final AlertDialog drawerTutorial, voiceTutorial, touchTutorial;
                final AlertDialog tiltTutorial, joystickTutorial, spinTutorial;
                final AlertDialog introTutorial, finalTutorial;

                finalTutorial = new AlertDialog.Builder(controller, AlertDialog.THEME_HOLO_DARK)
                        .setPositiveButton("That concludes the tutorial!\n" +
                                "Have fun!", null)
                        .create();

                voiceTutorial = new AlertDialog.Builder(controller, AlertDialog.THEME_HOLO_DARK)
                        .setPositiveButton("You can also control PiDroid by issuing voice commands.\n" +
                                "Press the mic button and use intuitive commands.", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                controls.speechButton.setBackgroundColor(0); // clear background color
                                finalTutorial.show();
                            }
                        })
                        .create();

                spinTutorial = new AlertDialog.Builder(controller, AlertDialog.THEME_HOLO_DARK)
                        .setPositiveButton("This is a TOGGLE button for SPIN control.\n" +
                                        "Spin LEFT or RIGHT when going BACKWARDS or FORWARDS.",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        controls.speechButton.setBackgroundColor(Color.argb(150, 0, 255, 0)); // green
                                        controls.toggleSpinImageView.setBackgroundColor(0); // clear colour
                                        voiceTutorial.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                                        voiceTutorial.show();
                                    }
                                })
                        .create();

                joystickTutorial = new AlertDialog.Builder(controller, AlertDialog.THEME_HOLO_DARK)
                        .setPositiveButton("You can also use these two joysticks to control PiDroid.\n" +
                                "LEFT one for camera and RIGHT for driving.", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                controls.setControls(Controls.TOUCH_GYRO.getID());
                                controls.toggleSpinImageView.setBackgroundColor(Color.argb(150, 0, 255, 0)); // green
                                controls.largeCameraJoystickView.setBackgroundColor(0); // clear colour
                                controls.directionJoystickView.setBackgroundColor(0);
                                spinTutorial.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                                spinTutorial.show();
                            }
                        })
                        .create();

                tiltTutorial = new AlertDialog.Builder(controller, AlertDialog.THEME_HOLO_DARK)
                        .setPositiveButton("To turn LEFT or RIGHT tilt the device in the \n"
                                + "corresponding direction. The gyro will do the work.", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                controls.setControls(Controls.JOYSTICKS.getID());
                                controls.largeCameraJoystickView.setBackgroundColor(Color.argb(150, 0, 255, 0)); // green
                                controls.directionJoystickView.setBackgroundColor(Color.argb(150, 0, 255, 0));
                                joystickTutorial.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                                joystickTutorial.show();
                            }
                        })
                        .create();

                touchTutorial = new AlertDialog.Builder(controller, AlertDialog.THEME_HOLO_DARK)
                        .setPositiveButton("Touch the LEFT side of the screen to go BACKWARDS\n"
                                + "and the RIGHT side to go FORWARDS.", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                controls.forwardsPowerImageView.setBackgroundColor(0);  // clear background color
                                controls.backwardsPowerImageView.setBackgroundColor(0);
                                tiltTutorial.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                                tiltTutorial.show();
                            }
                        })
                        .create();

                drawerTutorial = new AlertDialog.Builder(controller, AlertDialog.THEME_HOLO_DARK)
                        .setPositiveButton("This is the settings menu.\n"
                                + "Drag to open and quickly make changes.", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                controls.setControls(Controls.TOUCH_GYRO.getID());
                                drawer.closeDrawer();
                                controls.forwardsPowerImageView.setBackgroundColor(Color.argb(150, 0, 255, 0));  // green
                                controls.backwardsPowerImageView.setBackgroundColor(Color.argb(150, 255, 0, 0)); // red
                                touchTutorial.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                                touchTutorial.show();
                            }
                        })
                        .create();

                introTutorial = new AlertDialog.Builder(controller, AlertDialog.THEME_HOLO_DARK)
                        .setPositiveButton("This will be a short tutorial on HOW TO use the app.\n" +
                                "To SKIP it, simply press the back button.", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                drawer.openDrawer();
                                drawerTutorial.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                                drawerTutorial.show();
                            }
                        })
                        .create();

                //
                introTutorial.show();
            } // run()
        });
    } // start()

} // ControllerTutorial
