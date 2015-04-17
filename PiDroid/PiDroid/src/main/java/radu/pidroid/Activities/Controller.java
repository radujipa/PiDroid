/*
  Controller.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.Activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import radu.pidroid.Connector.Messenger;
import radu.pidroid.Connector.MethodInvocation;
import radu.pidroid.Managers.ControlsManager;
import radu.pidroid.Managers.DrawerManager;
import radu.pidroid.Managers.SensorsManager;
import radu.pidroid.Managers.SettingsManager;
import radu.pidroid.Managers.VideoFeedManager;
import radu.pidroid.R;
import radu.pidroid.Tutorials.ControllerTutorial;


public class Controller extends Activity {

    //
    public ImageView hudImageView;
    public int[] hudResources;

    //
    public DrawerManager drawer;

    //
    private boolean rememberDetailsON;
    private SettingsManager settings;

    //
    public SensorsManager sensors;
    public ControlsManager controls;
    public VideoFeedManager videoFeed;

    //
    private Messenger messenger;
    private MethodInvocation invocator;


    //
    private List<ActivityLifecycleListener> activityLifecycleListeners;
    private List<ActivityResultListener> activityResultListeners;


    //**********************************************************************************************
    //                                  ACTIVITY METHODS
    //**********************************************************************************************


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.controller);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) actionBar.hide();

        // get the boolean passed from Main with an Intent
        Intent intent = getIntent();
        rememberDetailsON = intent.getBooleanExtra(Main.EXTRA_REMEMBER_DETAILS_ON, false);

        // initialising listeners
        activityLifecycleListeners = new ArrayList<ActivityLifecycleListener>();
        activityResultListeners = new ArrayList<ActivityResultListener>();

        // loading the shared settings for the whole app
        settings = new SettingsManager(this);
        settings.load();

        // loading the HUD overlay
        hudImageView = (ImageView) findViewById(R.id.hudImageView);
        hudResources = new int[] { R.drawable.hud_clean_1366x768, R.drawable.hud_lines_1366x768 };
        hudImageView.setImageResource(hudResources[settings.currentHUDIndex]);

        // instantiating the networking layer
        messenger = new Messenger(this, settings);
        invocator = new MethodInvocation(this, messenger);

        // creating the gyro sensor manager (it is managed by ControlsManager)
        sensors = new SensorsManager(this);

        // starting the video feed from the robot (it registers with ActivityLifecycleListener)
        videoFeed = new VideoFeedManager(this, settings, sensors);

        // creating the manager for all types of controls and associated UI elements
        controls = new ControlsManager(this, settings, sensors, videoFeed, messenger, invocator);

        // setting up the NavigationDrawer used for quick settings
        drawer = new DrawerManager(this, settings, controls, videoFeed, messenger);

        // showing a tutorial to get new users up to speed with the features
        if (settings.tutorialsON)
            new ControllerTutorial(this, controls, drawer).start();
    } // onCreate


    @Override
    protected void onResume() {
        super.onResume();

        // calling all listeners (SensorsManager, VideoFeedManager are registered here)
        for (ActivityLifecycleListener listener : this.activityLifecycleListeners)
            listener.onResume();

        // if the user ticked 'Remember details' we load the settings, otherwise clear
        if (rememberDetailsON) settings.load();
        else                   settings.clear();
    } // onResume


    @Override
    protected void onPause() {
        super.onPause();

        // calling all listeners (SensorsManager, VideoFeedManager are registered here)
        for (ActivityLifecycleListener listener : this.activityLifecycleListeners)
            listener.onPause();

        // if the user ticked 'Remember details' we save the settings
        if (rememberDetailsON) settings.save();
    } // onPause


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.controller, menu);
        return true;
    } // onCreateOptionsMenu


    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK)
        .setTitle("Disconnect")
        .setMessage("Are you sure you want to disconnect?")
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            } // onClick
        }) // .setPositiveButton
        .setNegativeButton(android.R.string.cancel, null)
        .create().show();
    } // onBackPressed


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // calling all listeners (VoiceControls is registered here)
        for (ActivityResultListener listener : this.activityResultListeners)
            listener.onActivityResult(requestCode, resultCode, intent);

        super.onActivityResult(requestCode, resultCode, intent);
    } // onActivityResult


    public interface ActivityLifecycleListener {
        void onPause();
        void onResume();
    } // ActivityLifecycleListener

    public void addActivityLifecycleListener(ActivityLifecycleListener listener) {
        this.activityLifecycleListeners.add(listener);
    } // addActivityLifecycleListener


    public interface ActivityResultListener {
        void onActivityResult(int requestCode, int resultCode, Intent intent);
    } // ActivityResultListener

    public void addActivityResultListener(ActivityResultListener listener) {
        this.activityResultListeners.add(listener);
    } // addActivityResultListener

} // Controller
