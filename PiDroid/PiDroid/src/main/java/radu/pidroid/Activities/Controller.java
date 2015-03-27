/*
  Controller.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.Activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;

import java.util.ArrayList;

import radu.pidroid.Connector.MethodInvocation;
import radu.pidroid.Managers.ControlsManager;
import radu.pidroid.Managers.DrawerManager;
import radu.pidroid.Managers.SensorsManager;
import radu.pidroid.Managers.SettingsManager;
import radu.pidroid.Managers.VideoFeedManager;
import radu.pidroid.R;
import radu.pidroid.Tutorials.ControllerTutorial;
import radu.pidroid.Connector.Messenger;
import radu.pidroid.SettingsDrawer.DrawerItem;
import radu.pidroid.SettingsDrawer.DrawerRow;


public class Controller extends Activity {

    //
    public ImageView hudImageView;
    public final int[] hudResources = new int[] { R.drawable.hud_clean_1366x768,
                                                  R.drawable.hud_lines_1366x768 };

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

        hudImageView = (ImageView) findViewById(R.id.hudImageView);

        // get the boolean passed from Main with an Intent
        Intent intent = getIntent();
        rememberDetailsON = intent.getBooleanExtra(Main.EXTRA_REMEMBER_DETAILS_ON, false);

        messenger = new Messenger(this, settings.serverIP, settings.serverPort);
        invocator = new MethodInvocation(this, messenger);

        drawer = new DrawerManager(this, controls, videoFeed, settings, messenger);
        settings = new SettingsManager(this);
        controls = new ControlsManager(this, settings, videoFeed, messenger, invocator);

        sensors = new SensorsManager(this, controls);
        sensors.start();

        settings = new SettingsManager(this);
        settings.load();

        drawer.setup();
        videoFeed.start();

        if (settings.tutorialsON)
            new ControllerTutorial(this).start();
    } // onCreate


    @Override
    protected void onResume() {
        super.onResume();

        //
        sensors.start();
        // TODO: video.start(); ?

        //
        if (rememberDetailsON) settings.load();
        else settings.clear();
    } // onResume


    @Override
    protected void onPause() {
        super.onPause();

        //
        sensors.stop();
        videoFeed.stop();

        //
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


    //**********************************************************************************************
    //                                   MACHINE LEARNING
    //**********************************************************************************************


    public class LearningTask extends AsyncTask<Void, Void, Boolean> {

        public final static int PIDROID_LEARN = 0;
        public final static int PIDROID_RECOGNISE = 1;

        private final Context UIContext;
        private final ProgressDialog dialog;
        private final int taskType;

        private String objects[];
        private int objectType;


        public LearningTask(Context context, int taskType, int objectType) {
            this(context, taskType);

            this.objectType = objectType;
        } // constructor


        public LearningTask(Context context, int taskType) {
            this.UIContext = context;
            this.taskType = taskType;

            objects = getResources().getStringArray(R.array.object_names);

            // Define the behaviour of a reply from PiDroid sent by RecogniserController
            messenger.setRecogniserController(new Messenger.RecogniserController() {
                @Override
                public void onLearnNewObjectReply() {
                    videoFeedOn = true;
                } // onLearnNewObjectReply

                @Override
                public void onRecogniseObjectReply(int objType) {
                    videoFeedOn = true;

                    if (objType != -1)
                        new AlertDialog.Builder(UIContext, AlertDialog.THEME_HOLO_DARK)
                                .setPositiveButton("The object is a " + objects[objType].toUpperCase(), null)
                                .create().show();
                    else
                        new AlertDialog.Builder(UIContext, AlertDialog.THEME_HOLO_DARK)
                                .setPositiveButton("The object could not be recognised.", null)
                                .create().show();
                } // onRecogniseObjectReply
            });

            // Let the user know PiDroid is busy
            dialog = new ProgressDialog(UIContext, AlertDialog.THEME_HOLO_DARK);
            dialog.setMessage("PiDroid is computing...");
            dialog.setCancelable(false);
            dialog.show();

            Log.d("LearningTask:", "Progress dialog is showing...");
        } // constructor


        @Override
        protected Boolean doInBackground(Void... params) {
            // Because PiDroid uses the camera to learn new objects or recognise them, it stops the stream
            videoFeedMjpegView.stopPlayback();
            videoFeedTask.cancel(true);
            videoFeedOn = false;

            if (taskType == PIDROID_LEARN)
                // Tell PiDroid to kick off the learning process
                messenger.learnNewObject(objectType);
            else
            if (taskType == PIDROID_RECOGNISE)
                // Tell PiDroid to kick off the recognition process
                messenger.recogniseObject();

            // Here we wait for a reply from PiDroid meaning that the learning process has finished
            // The variable is set by the onLearnNewObjectReply() defined in the Constructor
            while (!videoFeedOn)
            {
                Log.d("LearningTask:", "Failed to restart video feed!");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException interruptedException) {
                    Log.d("LearningTask:", "Thread got interrupted");
                } // try - catch
            } // while

            setupVideoFeed();

            Log.d("LearningTask:", "Progress dialog is being dismissed...");

            return true;
        } // doInBackground


        @Override
        protected void onPostExecute(Boolean result) {
            dialog.dismiss();
        } // onPostExecute
    } // LearningTask

} // Controller
