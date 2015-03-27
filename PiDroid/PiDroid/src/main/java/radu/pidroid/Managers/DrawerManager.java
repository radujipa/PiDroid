/*
  DrawerManager.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.Managers;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import radu.pidroid.Activities.Controller;
import radu.pidroid.Connector.Messenger;
import radu.pidroid.Managers.ControlsManager.Controls;
import radu.pidroid.R;
import radu.pidroid.SettingsDrawer.DrawerAdapter;
import radu.pidroid.SettingsDrawer.DrawerHeader;
import radu.pidroid.SettingsDrawer.DrawerItem;
import radu.pidroid.SettingsDrawer.DrawerRow;
import radu.pidroid.SettingsDrawer.DrawerSettingsRow;


public class DrawerManager implements ExpandableListView.OnGroupClickListener {

    // references to modules
    private Controller controller;
    private ControlsManager controls;
    private VideoFeedManager videoFeed;
    private SettingsManager settings;
    private Messenger messenger;

    private DrawerLayout mDrawerLayout;
    private DrawerAdapter mDrawerAdapter;
    private ExpandableListView mDrawerListView;

    private String[] mDrawerItemsList;
    private List<DrawerItem> parentData;
    private HashMap<DrawerItem, List<DrawerItem>> childData;


    public DrawerManager(Controller controller, ControlsManager controls, VideoFeedManager videoFeed,
                         SettingsManager settings, Messenger messenger) {
        this.controller = controller;
        this.controls = controls;
        this.settings = settings;
        this.messenger = messenger;
        this.videoFeed = videoFeed;
    } // constructor


    public void setup() {
        mDrawerLayout   = (DrawerLayout) controller.findViewById(R.id.DrawerLayout);
        mDrawerListView = (ExpandableListView) controller.findViewById(R.id.LeftDrawer);
        mDrawerListView.setOnGroupClickListener(this);

        mDrawerItemsList = controller.getResources().getStringArray(R.array.drawer_items_list);

        createDrawerRows();

        mDrawerAdapter = new DrawerAdapter(mDrawerListView, parentData, childData);
        mDrawerListView.setAdapter(mDrawerAdapter);
    } // setup


    public void openDrawer() {
        mDrawerLayout.openDrawer(mDrawerListView);
    } // openDrawer


    public void closeDrawer() {
        mDrawerLayout.closeDrawer(mDrawerListView);
    } // closeDrawer


    private void createDrawerRows() {
        /*
         NOTE:  1. These constants must be in accordance with StringArray
                    "drawer_items_list" in res/values/strings.xml
                2. Used only to help automate the creation process of drawer items
        */
        final int DRAWER_HEADER = 0, DRAWER_ROW = 1, DRAWER_SETTINGS_ROW = 2;

        //
        parentData = new ArrayList<DrawerItem>();
        childData = new HashMap<DrawerItem, List<DrawerItem>>();

        LayoutInflater inflater = (LayoutInflater) controller.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //
        DrawerItem parent = null, child = null;
        List<DrawerItem> children;

        //
        for (String drawerItemText : mDrawerItemsList) {

            //
            String[] parts = drawerItemText.split(",");
            children = new ArrayList<DrawerItem>();

            //
            switch (Integer.parseInt(parts[0])) {

                case DRAWER_HEADER:
                    parent = new DrawerHeader(controller, inflater.inflate(R.layout.drawer_header, null));
                    ((DrawerHeader)parent).setHeaderText(parts[1]);
                    parentData.add(parent);
                    break;

                case DRAWER_ROW:
                    parent = new DrawerRow(controller, inflater.inflate(R.layout.drawer_row, null));
                    ((DrawerRow)parent).setRowText(parts[1]);
                    ((DrawerRow)parent).setIconResource(android.R.drawable.ic_menu_always_landscape_portrait);
                    parentData.add(parent);
                    break;

                case DRAWER_SETTINGS_ROW:
                    parent = new DrawerRow(controller, inflater.inflate(R.layout.drawer_row, null));
                    ((DrawerRow)parent).setRowText(parts[1]);
                    ((DrawerRow)parent).setIconResource(android.R.drawable.ic_menu_always_landscape_portrait);
                    parentData.add(parent);

                    child = new DrawerSettingsRow(controller, inflater.inflate(R.layout.drawer_settings_row, null));
                    children.add(child);
                    break;

                default:
                    // invalid drawer item type
                    Log.e("createDrawerRows(): ", "Check strings.xml");
            } // switch

            //
            childData.put(parent, children);
        } // for


        /*  The next part defines the behaviour of the drawer items.

            Because of the different types of rows, hard-coding these is the easiest
             approach as opposed to further complicating things to simply create them.
         */
        // TOUCH CONTROLS
        ((DrawerRow) parentData.get(1)).setRowFunction(new DrawerRow.ToggleSettings() {
            @Override
            public void toggle() {
                controls.setControls(Controls.TOUCH_GYRO);
                mDrawerLayout.closeDrawer(mDrawerListView);
                Toast.makeText(parentData.get(1).getUIContext(), "Touch controls have been set", Toast.LENGTH_SHORT).show();
            } // toggle
        });

        // SLIDER CONTROLS
        ((DrawerRow) parentData.get(2)).setRowFunction(new DrawerRow.ToggleSettings() {
            @Override
            public void toggle() {
                controls.setControls(Controls.SLIDER_GYRO);
                mDrawerLayout.closeDrawer(mDrawerListView);
                Toast.makeText(parentData.get(2).getUIContext(), "Slider controls have been set", Toast.LENGTH_SHORT).show();
            } // toggle
        });

        // JOYSTICK CONTROLS
        ((DrawerRow) parentData.get(3)).setRowFunction(new DrawerRow.ToggleSettings() {
            @Override
            public void toggle() {
                controls.setControls(Controls.JOYSTICKS);
                mDrawerLayout.closeDrawer(mDrawerListView);
                Toast.makeText(parentData.get(3).getUIContext(), "Joystick controls have been set", Toast.LENGTH_SHORT).show();
            } // toggle
        });

        // MAXIMUM TILT ANGLE
        ((DrawerSettingsRow) childData.get(parentData.get(5)).get(0)).setSettingsFunction(new DrawerSettingsRow.SliderSettings() {
            @Override
            public String calculate(int progress) {
                settings.maximumTiltAngle = (int) (progress * 0.8 + 10) / 10 * 10;
                return settings.maximumTiltAngle + "°";
            } // calculate
        });
        ((DrawerSettingsRow) childData.get(parentData.get(5)).get(0)).initialise((int) ((settings.maximumTiltAngle / 10 * 10 - 10) / 0.8));

        // ACCELERATION TIME
        ((DrawerSettingsRow) childData.get(parentData.get(6)).get(0)).setSettingsFunction(new DrawerSettingsRow.SliderSettings() {
            @Override
            public String calculate(int progress) {
                settings.accelerationTime = progress / 10 * 250;
                return settings.accelerationTime + "ms";
            } // calculate
        });
        ((DrawerSettingsRow) childData.get(parentData.get(6)).get(0)).initialise(settings.accelerationTime / 250 * 10);

        // DECELERATION TIME
        ((DrawerSettingsRow) childData.get(parentData.get(7)).get(0)).setSettingsFunction(new DrawerSettingsRow.SliderSettings() {
            @Override
            public String calculate(int progress) {
                settings.decelerationTime = progress / 10 * 250;
                return settings.decelerationTime + "ms";
            } // calculate
        });
        ((DrawerSettingsRow) childData.get(parentData.get(7)).get(0)).initialise(settings.decelerationTime / 250 * 10);

        // TOUCH RESPONSIVENESS
        ((DrawerSettingsRow) childData.get(parentData.get(8)).get(0)).setSettingsFunction(new DrawerSettingsRow.SliderSettings() {
            @Override
            public String calculate(int progress) {
                settings.touchResponsiveness = progress / 10 * 50;
                return settings.touchResponsiveness + "ms";
            } // calculate
        });
        ((DrawerSettingsRow) childData.get(parentData.get(8)).get(0)).initialise(settings.touchResponsiveness / 50 * 10);

        // TURN SENSITIVITY
        ((DrawerSettingsRow) childData.get(parentData.get(10)).get(0)).setSettingsFunction(new DrawerSettingsRow.SliderSettings() {
            @Override
            public String calculate(int progress) {
                settings.turnSensitivity = progress / 25 + 1;
                messenger.updateTurnSensitivity(settings.turnSensitivity);
                return settings.turnSensitivity + "";
            } // calculate
        });
        ((DrawerSettingsRow) childData.get(parentData.get(10)).get(0)).initialise((settings.turnSensitivity - 1) * 25);

        // CAMERA STABILISATION
        ((DrawerRow) parentData.get(12)).setRowFunction(new DrawerRow.ToggleSettings() {
            @Override
            public void toggle() {
                mDrawerLayout.closeDrawer(mDrawerListView);
                settings.cameraStabilisationON = !settings.cameraStabilisationON;
                videoFeed.setCameraStabilisation(settings.cameraStabilisationON);

                if (settings.cameraStabilisationON)
                    Toast.makeText(parentData.get(15).getUIContext(), "Camera stabilisation is now ON", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(parentData.get(15).getUIContext(), "Camera stabilisation is now OFF", Toast.LENGTH_LONG).show();
            } // toggle
        });

        // CHANGE HUD
        ((DrawerRow) parentData.get(13)).setRowFunction(new DrawerRow.ToggleSettings() {
            @Override
            public void toggle() {
                if (++settings.currentHUDIndex == controller.hudResources.length) settings.currentHUDIndex = 0;
                controller.hudImageView.setImageResource(controller.hudResources[settings.currentHUDIndex]);
            } // toggle
        });

        // LEVEL INDICATOR
        ((DrawerRow) parentData.get(14)).setRowFunction(new DrawerRow.ToggleSettings() {
            @Override
            public void toggle() {
                if (controls.levelIndicatorImageView.getVisibility() == View.VISIBLE)
                    controls.levelIndicatorImageView.setVisibility(View.INVISIBLE);
                else controls.levelIndicatorImageView.setVisibility(View.VISIBLE);
            } // toggle
        });

        // TUTORIALS
        ((DrawerRow) parentData.get(15)).setRowFunction(new DrawerRow.ToggleSettings() {
            @Override
            public void toggle() {
                settings.tutorialsON = !settings.tutorialsON;
                mDrawerLayout.closeDrawer(mDrawerListView);

                if (settings.tutorialsON)
                    Toast.makeText(parentData.get(18).getUIContext(), "Tutorials are now ON", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(parentData.get(18).getUIContext(), "Tutorials are now OFF", Toast.LENGTH_LONG).show();
            } // toggle
        });
    } // createDrawerRows


    public boolean onGroupClick(ExpandableListView parent, View view, int groupPosition, long id) {
        DrawerItem item = ((DrawerItem) mDrawerAdapter.getGroup(groupPosition));

        if (item instanceof DrawerRow)
            ((DrawerRow) item).executeRowFunction();

        return false;
    } // onGroupClick

} // DrawerManager
