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
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;

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
    ActivityResultListener activityResultListener;


    //**********************************************************************************************
    //                                  ACTIVITY METHODS
    //**********************************************************************************************


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.controller);

        ActionBar actionBar = getActionBar();
        actionBar.hide();

        // get the boolean passed from Main with an Intent
        Intent intent = getIntent();
        rememberDetailsON = intent.getBooleanExtra(Main.EXTRA_REMEMBER_DETAILS_ON, false);

        settings = new SettingsManager(this);
        settings.load();

        hudImageView = (ImageView) findViewById(R.id.hudImageView);
        hudResources = new int[] { R.drawable.hud_clean_1366x768, R.drawable.hud_lines_1366x768 };
        hudImageView.setImageResource(hudResources[settings.currentHUDIndex]);

        messenger = new Messenger(this, settings);
        invocator = new MethodInvocation(this, messenger);

        sensors = new SensorsManager(this);
        sensors.start();

        videoFeed = new VideoFeedManager(this, settings, sensors);
        videoFeed.start();

        controls = new ControlsManager(this, settings, sensors, videoFeed, messenger, invocator);

        drawer = new DrawerManager(this, settings, controls, videoFeed, messenger);
        drawer.setup();

        if (settings.tutorialsON)
            new ControllerTutorial(this, controls, drawer).start();
    } // onCreate


    @Override
    protected void onResume() {
        super.onResume();

        //
        sensors.start();
        // TODO: video.start(); ?

        //
        if (rememberDetailsON)  settings.load();
        else                    settings.clear();
    } // onResume


    @Override
    protected void onPause() {
        super.onPause();

        //
        sensors.stop();
        videoFeed.stop();

        //
        if (rememberDetailsON)  settings.save();
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
                //TODO: messenger.updateRoverSpeed(roverSpeed = 0);
                messenger.updateRoverSpeed(0);
                messenger.stopMessenger();
                finish();
            } // onClick
        }) // .setPositiveButton
        .setNegativeButton(android.R.string.cancel, null)
        .create().show();

        videoFeed.stop();
    } // onBackPressed


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        this.activityResultListener.onActivityResult(requestCode, resultCode, intent);
        super.onActivityResult(requestCode, resultCode, intent);
    } // onActivityResult


    public interface ActivityResultListener {
        void onActivityResult(int requestCode, int resultCode, Intent intent);
    } // ActivityResultListener

    public void setActivityResultListener(ActivityResultListener listener) {
        this.activityResultListener = listener;
    } // setActivityResultListener

} // Controller
