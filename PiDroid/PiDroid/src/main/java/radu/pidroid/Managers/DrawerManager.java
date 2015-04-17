/*
  DrawerManager.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.Managers;

import android.view.View;

import radu.pidroid.Activities.Controller;
import radu.pidroid.Connector.Messenger;
import radu.pidroid.R;
import radu.pidroid.SettingsDrawer.DrawerCategory;
import radu.pidroid.SettingsDrawer.DrawerSettingsRow;
import radu.pidroid.SettingsDrawer.DrawerToggleRow;
import radu.pidroid.SettingsDrawer.NavigationDrawer;


public class DrawerManager {

    // references to modules
    private Controller controller;
    private ControlsManager controls;
    private VideoFeedManager videoFeed;
    private SettingsManager settings;
    private Messenger messenger;

    // our settings drawer
    private NavigationDrawer drawer;

    // categories which will be added to our drawer
    private DrawerCategory controlsCategory;
    private DrawerCategory settingsCategory;
    private DrawerCategory robotCategory;
    private DrawerCategory optionsCategory;


    public DrawerManager(Controller controller, SettingsManager settings, ControlsManager controls,
                         VideoFeedManager videoFeed, Messenger messenger) {

        this.controller = controller;
        this.controls = controls;
        this.settings = settings;
        this.messenger = messenger;
        this.videoFeed = videoFeed;

        this.drawer = new NavigationDrawer(controller);

        /*  The next part defines the behaviour of the drawer items.

            Because of the different types of rows, hard-coding these is the easiest
             approach as opposed to further complicating things to simply create them.
         */
        createControlsCategory();
        createSettingsCategory();
        createRobotCategory();
        createOptionsCategory();
    } // constructor


    public NavigationDrawer getDrawer() {
        return this.drawer;
    } // getDrawer


    private void createControlsCategory() {
        this.controlsCategory = new DrawerCategory(controller, R.array.drawer_controls);
        drawer.addCategory(controlsCategory);

        // TOUCH CONTROLS
        int index = 1;
        ((DrawerToggleRow) controlsCategory.getParentData().get(index))
            .toggleRow(settings.controlsID == ControlsManager.Controls.TOUCH_GYRO.getID());
        ((DrawerToggleRow) controlsCategory.getParentData().get(index))
            .setToggleAction(new DrawerToggleRow.ToggleAction() {
            @Override
            public void toggle() {
                drawer.closeDrawer();
                controls.setControls(ControlsManager.Controls.TOUCH_GYRO.getID());
                ((DrawerToggleRow) controlsCategory.getParentData().get(2)).setIsToggledText("OFF");
                ((DrawerToggleRow) controlsCategory.getParentData().get(3)).setIsToggledText("OFF");
            } // toggle
        });

        // SLIDER CONTROLS
        index = 2;
        ((DrawerToggleRow) controlsCategory.getParentData().get(index))
            .toggleRow(settings.controlsID == ControlsManager.Controls.SLIDER_GYRO.getID());
        ((DrawerToggleRow) controlsCategory.getParentData().get(index))
            .setToggleAction(new DrawerToggleRow.ToggleAction() {
            @Override
            public void toggle() {
                drawer.closeDrawer();
                controls.setControls(ControlsManager.Controls.SLIDER_GYRO.getID());
                ((DrawerToggleRow) controlsCategory.getParentData().get(1)).setIsToggledText("OFF");
                ((DrawerToggleRow) controlsCategory.getParentData().get(3)).setIsToggledText("OFF");
            } // toggle
        });

        // JOYSTICK CONTROLS
        index = 3;
        ((DrawerToggleRow) controlsCategory.getParentData().get(index))
            .toggleRow(settings.controlsID == ControlsManager.Controls.JOYSTICKS.getID());
        ((DrawerToggleRow) controlsCategory.getParentData().get(index))
            .setToggleAction(new DrawerToggleRow.ToggleAction() {
            @Override
            public void toggle() {
                drawer.closeDrawer();
                controls.setControls(ControlsManager.Controls.JOYSTICKS.getID());
                ((DrawerToggleRow) controlsCategory.getParentData().get(1)).setIsToggledText("OFF");
                ((DrawerToggleRow) controlsCategory.getParentData().get(2)).setIsToggledText("OFF");
            } // toggle
        });
    } // createControlsCategory


    private void createSettingsCategory() {
        this.settingsCategory = new DrawerCategory(controller, R.array.drawer_touch_settings);
        drawer.addCategory(settingsCategory);

        // MAXIMUM TILT ANGLE
        int index = 1;
        ((DrawerSettingsRow) settingsCategory.getParentData().get(index))
            .setSettingsFunction(new DrawerSettingsRow.SliderSettings() {
            @Override
            public String calculate(int progress) {
                settings.maximumTiltAngle = (int) (progress * 0.8 + 10) / 10 * 10;
                return settings.maximumTiltAngle + "°";
            } // calculate
        });
        ((DrawerSettingsRow) settingsCategory.getParentData().get(index))
            .initialise((int) ((settings.maximumTiltAngle / 10 * 10 - 10) / 0.8));

        // ACCELERATION TIME
        index = 2;
        ((DrawerSettingsRow) settingsCategory.getParentData().get(index))
            .setSettingsFunction(new DrawerSettingsRow.SliderSettings() {
            @Override
            public String calculate(int progress) {
                settings.accelerationTime = progress / 10 * 250;
                return settings.accelerationTime + "ms";
            } // calculate
        });
        ((DrawerSettingsRow) settingsCategory.getParentData().get(index))
            .initialise(settings.accelerationTime / 250 * 10);

        // DECELERATION TIME
        index = 3;
        ((DrawerSettingsRow) settingsCategory.getParentData().get(index))
            .setSettingsFunction(new DrawerSettingsRow.SliderSettings() {
            @Override
            public String calculate(int progress) {
                settings.decelerationTime = progress / 10 * 250;
                return settings.decelerationTime + "ms";
            } // calculate
        });
        ((DrawerSettingsRow) settingsCategory.getParentData().get(index))
            .initialise(settings.decelerationTime / 250 * 10);

        // TOUCH RESPONSIVENESS
        index = 4;
        ((DrawerSettingsRow) settingsCategory.getParentData().get(index))
            .setSettingsFunction(new DrawerSettingsRow.SliderSettings() {
            @Override
            public String calculate(int progress) {
                settings.touchResponsiveness = progress / 10 * 50;
                return settings.touchResponsiveness + "ms";
            } // calculate
        });
        ((DrawerSettingsRow) settingsCategory.getParentData().get(index))
            .initialise(settings.touchResponsiveness / 50 * 10);
    } // createSettingsCategory


    private void createRobotCategory() {
        this.robotCategory = new DrawerCategory(controller, R.array.drawer_robot_settings);
        drawer.addCategory(robotCategory);

        // TURN SENSITIVITY
        int index = 1;
        ((DrawerSettingsRow) robotCategory.getParentData().get(index))
            .setSettingsFunction(new DrawerSettingsRow.SliderSettings() {
            @Override
            public String calculate(int progress) {
                settings.turnSensitivity = progress / 25 + 1;
                messenger.updateTurnSensitivity(settings.turnSensitivity);
                return settings.turnSensitivity + "";
            } // calculate
        });
        ((DrawerSettingsRow) robotCategory.getParentData().get(index))
            .initialise((settings.turnSensitivity - 1) * 25);
    } // createRobotCategory


    private void createOptionsCategory() {
        this.optionsCategory = new DrawerCategory(controller, R.array.drawer_options);
        drawer.addCategory(optionsCategory);

        // CAMERA STABILISATION
        int index = 1;
        ((DrawerToggleRow) optionsCategory.getParentData().get(index))
            .toggleRow(settings.cameraStabilisationON);
        ((DrawerToggleRow) optionsCategory.getParentData().get(index))
            .setToggleAction(new DrawerToggleRow.ToggleAction() {
            @Override
            public void toggle() {
                settings.cameraStabilisationON = !settings.cameraStabilisationON;
                videoFeed.setCameraStabilisation(settings.cameraStabilisationON);
            } // toggle
        });

        // CHANGE HUD
        index = 2;
        ((DrawerToggleRow) optionsCategory.getParentData().get(index))
            .setToggleAction(new DrawerToggleRow.ToggleAction() {
            @Override
            public void toggle() {
                if (++settings.currentHUDIndex == controller.hudResources.length)
                    settings.currentHUDIndex = 0;
                controller.hudImageView.setImageResource(controller.hudResources[settings.currentHUDIndex]);
                ((DrawerToggleRow) optionsCategory.getParentData().get(2)).setIsToggledText(settings.currentHUDIndex + "");
            } // toggle
        });
        ((DrawerToggleRow) optionsCategory.getParentData().get(index))
            .setIsToggledText(settings.currentHUDIndex + "");

        // LEVEL INDICATOR
        index = 3;
        ((DrawerToggleRow) optionsCategory.getParentData().get(index))
            .toggleRow(settings.levelIndicatorON);
        ((DrawerToggleRow) optionsCategory.getParentData().get(index))
            .setToggleAction(new DrawerToggleRow.ToggleAction() {
            @Override
            public void toggle() {
                settings.levelIndicatorON = !settings.levelIndicatorON;
                controls.levelIndicatorImageView.setVisibility(settings.levelIndicatorON ? View.VISIBLE : View.INVISIBLE);
            } // toggle
        });

        // TUTORIALS
        index = 4;
        ((DrawerToggleRow) optionsCategory.getParentData().get(index))
            .toggleRow(settings.tutorialsON);
        ((DrawerToggleRow) optionsCategory.getParentData().get(index))
            .setToggleAction(new DrawerToggleRow.ToggleAction() {
            @Override
            public void toggle() {
                settings.tutorialsON = !settings.tutorialsON;
            } // toggle
        });
    } // createOptionsCategory

} // NavigationDrawerManager
