/*
  Controller.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import radu.pidroid.Connector.PiDroidMessenger;
import radu.pidroid.Joystick.JoystickView;
import radu.pidroid.MjpegViewer.MjpegInputStream;
import radu.pidroid.MjpegViewer.MjpegView;
import radu.pidroid.Recogniser.CommandRecogniser;
import radu.pidroid.SettingsDrawer.DrawerAdapter;
import radu.pidroid.SettingsDrawer.DrawerHeader;
import radu.pidroid.SettingsDrawer.DrawerItem;
import radu.pidroid.SettingsDrawer.DrawerRow;
import radu.pidroid.SettingsDrawer.DrawerSettingsRow;


public class Controller extends Activity
        implements SensorEventListener, View.OnTouchListener, SeekBar.OnSeekBarChangeListener,
                   ExpandableListView.OnGroupClickListener, JoystickView.MoveListener {

    //
    public final static String EXTRA_CONTROLS_ID             = "radu.pidroid.CONTROLSID";
    public final static String EXTRA_MAX_TILT_ANGLE          = "radu.pidroid.MAXTILTANGLE";
    public final static String EXTRA_ACCELERATION_TIME       = "radu.pidroid.ACCELERATIONTIME";
    public final static String EXTRA_DECELERATION_TIME       = "radu.pidroid.DECELERATIONTIME";
    public final static String EXTRA_TOUCH_RESPONSIVENESS    = "radu.pidroid.TOUCHRESPONSIVNESS";
    public final static String EXTRA_TURN_SENSITIVITY        = "radu.pidroid.TURNSENSITIVITY";
    public final static String EXTRA_HUD                     = "radu.pidroid.HUD";
    public final static String EXTRA_LEVEL_INDICATOR         = "radu.pidroid.LEVELINDICATOR";
    public final static String EXTRA_CAMERA_STABILISATION_ON = "radu.pidroid.CAMERASTABILISATIONON";
    public final static String EXTRA_TUTORIALS_ON            = "radu.pidroid.TUTORIALSON";

    //
    public final static int SPEECH_RECOGNITION_REQUEST_CODE = 513365;

    // acceleration states & direction states
    private final static int ROVER_ACCELERATING = 10;
    private final static int ROVER_DECELERATING = 11;
    private final static int ROVER_STOPPED = 12;
    private final static int ROVER_FORWARDS = 20;
    private final static int ROVER_BACKWARDS = 21;

    //
    private final static int TOUCH_CONTROLS = 30;
    private final static int SLIDER_CONTROLS = 31;
    private final static int JOYSTICK_CONTROLS = 32;

    //
    public static int controlsID;
    public static int maxTiltAngle;
    public static int accelerationTime;
    public static int decelerationTime;
    public static int touchResponsiveness;

    public static int turnAngle = 0;
    public static int turnSensitivity;

    //
    private String serverIP;
    private int serverPort;


    //
    ImageView hudImageView, levelIndicatorImageView;

    int currentHudResource;
    final int[] hudResources = new int[] {R.drawable.hud_clean_1366x768, R.drawable.hud_lines_1366x768};

    ImageView speechButton;

    MjpegView videoFeedMjpegView;
    boolean videoFeedOn, cameraStabilisationOn;
    VideoFeedTask videoFeedTask = null;

    RelativeLayout touchControlsLayout;
    ImageView   forwardsPowerImageView, backwardsPowerImageView;
    ProgressBar forwardsPowerProgressBar, backwardsPowerProgressBar;

    RelativeLayout sliderControlsLayout;
    SeekBar forwardsPowerSeekBar, backwardsPowerSeekBar;

    RelativeLayout joystickControlsLayout;
    JoystickView largeCameraJoystickView, directionJoystickView;

    JoystickView smallCameraJoystickView;
    ImageView toggleSpinImageView;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mGeomagnetic;
    private float [] mAccelerometerData = new float[3];
    private float [] mGeomagneticData = new float[3];

    private int lastTurnAngle = 0 ;
    private boolean tiltControlsOn = true;
    private boolean spinControlOn = false;

    Timer timer = new Timer();
    private final int timerPeriod = 50;

    private int roverSpeed, roverLastSpeed = 0;
    private int roverDirection = ROVER_FORWARDS;
    private int roverAccelerationState = ROVER_STOPPED;

    private PiDroidMessenger mPiDroidMessenger;
    private CommandRecogniser mRecogniser;

    private boolean tutorialsOn;


    //**********************************************************************************************
    //                                  ACTIVITY METHODS
    //**********************************************************************************************


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.controller);

        ActionBar actionBar = getActionBar();
        actionBar.hide();

        speechButton = (ImageView) findViewById(R.id.speechButton);
        speechButton.setOnClickListener(new ImageView.OnClickListener() {
            public void onClick(View view) {
                startVoiceRecognition();
            } // onClick
        });

        touchControlsLayout = (RelativeLayout) findViewById(R.id.TouchControlsLayout);
        forwardsPowerImageView = (ImageView) findViewById(R.id.forwardsPowerImageView);
        forwardsPowerImageView.setOnTouchListener(this);
        backwardsPowerImageView = (ImageView) findViewById(R.id.backwardsPowerImageView);
        backwardsPowerImageView.setOnTouchListener(this);

        forwardsPowerProgressBar = (ProgressBar) findViewById(R.id.forwardsPowerProgressBar);
        backwardsPowerProgressBar = (ProgressBar) findViewById(R.id.backwardsPowerProgressBar);

        sliderControlsLayout = (RelativeLayout) findViewById(R.id.SliderControlsLayout);
        forwardsPowerSeekBar = (SeekBar) findViewById(R.id.forwardsPowerSeekBar);
        forwardsPowerSeekBar.setOnSeekBarChangeListener(this);
        backwardsPowerSeekBar = (SeekBar) findViewById(R.id.backwardsPowerSeekBar);
        backwardsPowerSeekBar.setOnSeekBarChangeListener(this);

        joystickControlsLayout = (RelativeLayout) findViewById(R.id.JoystickControlsLayout);
        largeCameraJoystickView = (JoystickView) findViewById(R.id.largeCameraJoystickView);
        largeCameraJoystickView.setMoveListener(this);
        directionJoystickView = (JoystickView) findViewById(R.id.directionJoystickView);
        directionJoystickView.setMoveListener(this);

        videoFeedMjpegView = (MjpegView) findViewById(R.id.videoFeedSurfaceView);
        hudImageView = (ImageView) findViewById(R.id.hudImageView);
        levelIndicatorImageView = (ImageView) findViewById(R.id.levelIndicatorImageView);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGeomagnetic   = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        smallCameraJoystickView = (JoystickView) findViewById(R.id.cameraJoystickView);
        smallCameraJoystickView.setMoveListener(this);
        smallCameraJoystickView.setTag("smallCamera");

        spinControlOn = false;
        toggleSpinImageView = (ImageView) findViewById(R.id.toggleSpinImageView);
        toggleSpinImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // if spinControl is ON then tiltControls are OFF and vice versa
                spinControlOn = !spinControlOn;
                tiltControlsOn = !tiltControlsOn;

                if (spinControlOn)
                    toggleSpinImageView.setImageResource(R.drawable.spin_button_down);
                else
                    toggleSpinImageView.setImageResource(R.drawable.spin_button_up);

                // Let PiDroid know whether to spin
                mPiDroidMessenger.toggleSpin(spinControlOn);
            } // onClick
        });

        // Get the message from the intent
        Intent intent = getIntent();
        serverIP   = intent.getStringExtra(Main.EXTRA_SERVERIP);
        serverPort = intent.getIntExtra(Main.EXTRA_SERVERPORT, 8090);

        mPiDroidMessenger = new PiDroidMessenger(this, serverIP, serverPort);
        mRecogniser = new CommandRecogniser(this, mPiDroidMessenger, videoFeedMjpegView);

        if (Main.detailsCheckBox.isChecked()) loadPreferences();
        else                                  clearPreferences();

        setupNavigationDrawer();
        setupVideoFeed();

        if (tutorialsOn)
            displayTutorial(this);
    } // onCreate


    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGeomagnetic, SensorManager.SENSOR_DELAY_FASTEST);

        //
        if (Main.detailsCheckBox.isChecked())
            loadPreferences();
        else
            clearPreferences();

        //setupVideoFeed();
    } // onResume


    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);

        //
        if (Main.detailsCheckBox.isChecked())
            savePreferences();

        videoFeedMjpegView.stopPlayback();
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
                mPiDroidMessenger.updateRoverSpeed(roverSpeed = 0);
                mPiDroidMessenger.stopMessenger();
                finish();
            } // onClick
        }) // .setPositiveButton
        .setNegativeButton(android.R.string.cancel, null)
        .create().show();

        videoFeedMjpegView.stopPlayback();
    } // onBackPressed


    private void loadPreferences() {
        //
        SharedPreferences preferences = this.getPreferences(Context.MODE_PRIVATE);

        controlsID = preferences.getInt(EXTRA_CONTROLS_ID, TOUCH_CONTROLS);
        maxTiltAngle = preferences.getInt(EXTRA_MAX_TILT_ANGLE, 45);
        accelerationTime = preferences.getInt(EXTRA_ACCELERATION_TIME, 1500);
        decelerationTime = preferences.getInt(EXTRA_DECELERATION_TIME, 1000);
        touchResponsiveness = preferences.getInt(EXTRA_TOUCH_RESPONSIVENESS, 200);
        turnSensitivity = preferences.getInt(EXTRA_TURN_SENSITIVITY, 1);
        tutorialsOn = preferences.getBoolean(EXTRA_TUTORIALS_ON, true);

        cameraStabilisationOn = preferences.getBoolean(EXTRA_CAMERA_STABILISATION_ON, true);
        videoFeedMjpegView.setCameraStabilisation(cameraStabilisationOn);

        currentHudResource = preferences.getInt(EXTRA_HUD, 0);
        levelIndicatorImageView.setVisibility(preferences.getInt(EXTRA_LEVEL_INDICATOR, View.VISIBLE));

        hudImageView.setImageResource(hudResources[currentHudResource]);
        setControls(controlsID);
    } // loadPreferences


    private void savePreferences() {
        //
        SharedPreferences preferences = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(EXTRA_CONTROLS_ID, controlsID);
        editor.putInt(EXTRA_MAX_TILT_ANGLE, maxTiltAngle);
        editor.putInt(EXTRA_ACCELERATION_TIME, accelerationTime);
        editor.putInt(EXTRA_DECELERATION_TIME, decelerationTime);
        editor.putInt(EXTRA_TOUCH_RESPONSIVENESS, touchResponsiveness);
        editor.putInt(EXTRA_TURN_SENSITIVITY, turnSensitivity);
        editor.putInt(EXTRA_HUD, currentHudResource);
        editor.putInt(EXTRA_LEVEL_INDICATOR, levelIndicatorImageView.getVisibility());
        editor.putBoolean(EXTRA_CAMERA_STABILISATION_ON, cameraStabilisationOn);
        editor.putBoolean(EXTRA_TUTORIALS_ON, tutorialsOn);

        editor.commit();
    } // savePreferences


    private void clearPreferences() {
        //
        SharedPreferences preferences = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.clear();
        editor.commit();
    } // clearPreferences


    //**********************************************************************************************
    //                                      APP TUTORIAL
    //**********************************************************************************************


    private void displayTutorial(Context UIContext) {

        //
        final AlertDialog drawerTutorial, voiceTutorial, touchTutorial;
        final AlertDialog tiltTutorial, joystickTutorial, spinTutorial;
        final AlertDialog introTutorial, finalTutorial;

        finalTutorial = new AlertDialog.Builder(UIContext, AlertDialog.THEME_HOLO_DARK)
                .setPositiveButton("That concludes the tutorial!\n" +
                        "Have fun!", null)
                .create();

        voiceTutorial = new AlertDialog.Builder(UIContext, AlertDialog.THEME_HOLO_DARK)
                .setPositiveButton("You can also control PiDroid by issuing voice commands.\n" +
                        "Press the mic button and use intuitive commands.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        speechButton.setBackgroundColor(0); // clear background color
                        finalTutorial.show();
                    }
                })
                .create();

        spinTutorial = new AlertDialog.Builder(UIContext, AlertDialog.THEME_HOLO_DARK)
                .setPositiveButton("This is a TOGGLE button for SPIN control.\n" +
                        "Spin LEFT or RIGHT when going BACKWARDS or FORWARDS.",
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        speechButton.setBackgroundColor(Color.argb(150, 0, 255, 0)); // green
                        toggleSpinImageView.setBackgroundColor(0); // clear colour
                        voiceTutorial.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                        voiceTutorial.show();
                    }
                })
                .create();

        joystickTutorial = new AlertDialog.Builder(UIContext, AlertDialog.THEME_HOLO_DARK)
                .setPositiveButton("You can also use these two joysticks to control PiDroid.\n" +
                        "LEFT one for camera and RIGHT for driving.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setControls(TOUCH_CONTROLS);
                        toggleSpinImageView.setBackgroundColor(Color.argb(150, 0, 255, 0)); // green
                        largeCameraJoystickView.setBackgroundColor(0); // clear colour
                        directionJoystickView.setBackgroundColor(0);
                        spinTutorial.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                        spinTutorial.show();
                    }
                })
                .create();

        tiltTutorial = new AlertDialog.Builder(UIContext, AlertDialog.THEME_HOLO_DARK)
                .setPositiveButton("To turn LEFT or RIGHT tilt the device in the \n"
                        + "corresponding direction. The gyro will do the work.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setControls(JOYSTICK_CONTROLS);
                        largeCameraJoystickView.setBackgroundColor(Color.argb(150, 0,255,0)); // green
                        directionJoystickView.setBackgroundColor(Color.argb(150, 0,255,0));
                        joystickTutorial.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                        joystickTutorial.show();
                    }
                })
                .create();

        touchTutorial = new AlertDialog.Builder(UIContext, AlertDialog.THEME_HOLO_DARK)
                .setPositiveButton("Touch the LEFT side of the screen to go BACKWARDS\n"
                        + "and the RIGHT side to go FORWARDS.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        forwardsPowerImageView.setBackgroundColor(0);  // clear background color
                        backwardsPowerImageView.setBackgroundColor(0);
                        tiltTutorial.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                        tiltTutorial.show();
                    }
                })
                .create();

        drawerTutorial = new AlertDialog.Builder(UIContext, AlertDialog.THEME_HOLO_DARK)
            .setPositiveButton("This is the settings menu.\n"
                    + "Drag to open and quickly make changes.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    setControls(TOUCH_CONTROLS);
                    mDrawerLayout.closeDrawer(mDrawerListView);
                    forwardsPowerImageView.setBackgroundColor(Color.argb(150, 0, 255, 0));  // green
                    backwardsPowerImageView.setBackgroundColor(Color.argb(150, 255, 0, 0)); // red
                    touchTutorial.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    touchTutorial.show();
                }
            })
            .create();

        introTutorial = new AlertDialog.Builder(UIContext, AlertDialog.THEME_HOLO_DARK)
                .setPositiveButton("This will be a short tutorial on HOW TO use the app.\n" +
                        "To SKIP it, simply press the back button.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mDrawerLayout.openDrawer(mDrawerListView);
                        drawerTutorial.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                        drawerTutorial.show();
                    }
                })
                .create();

        //
        introTutorial.show();

    } // displayTutorials


    //**********************************************************************************************
    //                                    NAVIGATION DRAWER
    //**********************************************************************************************


    private DrawerLayout mDrawerLayout;
    private DrawerAdapter mDrawerAdapter;
    private ExpandableListView mDrawerListView;

    private String[] mDrawerItemsList;
    private List<DrawerItem> parentData;
    private HashMap<DrawerItem, List<DrawerItem>> childData;


    private void setupNavigationDrawer() {
        mDrawerLayout   = (DrawerLayout) findViewById(R.id.DrawerLayout);
        mDrawerListView = (ExpandableListView) findViewById(R.id.LeftDrawer);
        mDrawerListView.setOnGroupClickListener(this);

        mDrawerItemsList = getResources().getStringArray(R.array.drawer_items_list);

        createDrawerRows();

        mDrawerAdapter = new DrawerAdapter(mDrawerListView, parentData, childData);
        mDrawerListView.setAdapter(mDrawerAdapter);
    } // setupNavigationDrawer


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

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //
        DrawerItem parent = null, child = null;
        List<DrawerItem> children;

        //
        for (int index = 0; index < mDrawerItemsList.length; index++) {

            //
            String[] parts = mDrawerItemsList[index].split(",");
            children = new ArrayList<DrawerItem>();

            //
            switch (Integer.parseInt(parts[0])) {

            case DRAWER_HEADER:
                parent = new DrawerHeader(this, inflater.inflate(R.layout.drawer_header, null));
                ((DrawerHeader)parent).setHeaderText(parts[1]);
                parentData.add(parent);
                break;

            case DRAWER_ROW:
                parent = new DrawerRow(this, inflater.inflate(R.layout.drawer_row, null));
                ((DrawerRow)parent).setRowText(parts[1]);
                ((DrawerRow)parent).setIconResource(android.R.drawable.ic_menu_always_landscape_portrait);
                parentData.add(parent);
                break;

            case DRAWER_SETTINGS_ROW:
                parent = new DrawerRow(this, inflater.inflate(R.layout.drawer_row, null));
                ((DrawerRow)parent).setRowText(parts[1]);
                ((DrawerRow)parent).setIconResource(android.R.drawable.ic_menu_always_landscape_portrait);
                parentData.add(parent);

                child = new DrawerSettingsRow(this, inflater.inflate(R.layout.drawer_settings_row, null));
                children.add(child);
                break;

            default:
                Log.e("createNavigationDrawerRows(): ", "invalid drawer item type! Check strings.xml");
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
                setControls(TOUCH_CONTROLS);
                mDrawerLayout.closeDrawer(mDrawerListView);
                Toast.makeText(parentData.get(1).getUIContext(), "Touch controls have been set", Toast.LENGTH_SHORT).show();
            } // toggle
        });

        // SLIDER CONTROLS
        ((DrawerRow) parentData.get(2)).setRowFunction(new DrawerRow.ToggleSettings() {
            @Override
            public void toggle() {
                setControls(SLIDER_CONTROLS);
                mDrawerLayout.closeDrawer(mDrawerListView);
                Toast.makeText(parentData.get(2).getUIContext(), "Slider controls have been set", Toast.LENGTH_SHORT).show();
            } // toggle
        });

        // JOYSTICK CONTROLS
        ((DrawerRow) parentData.get(3)).setRowFunction(new DrawerRow.ToggleSettings() {
            @Override
            public void toggle() {
                setControls(JOYSTICK_CONTROLS);
                mDrawerLayout.closeDrawer(mDrawerListView);
                Toast.makeText(parentData.get(3).getUIContext(), "Joystick controls have been set", Toast.LENGTH_SHORT).show();
            } // toggle
        });

        // MAXIMUM TILT ANGLE
        ((DrawerSettingsRow) childData.get(parentData.get(5)).get(0)).setSettingsFunction(new DrawerSettingsRow.SliderSettings() {
            @Override
            public String calculate(int progress) {
                maxTiltAngle = (int) (progress * 0.8 + 10) / 10 * 10;
                return Controller.maxTiltAngle + "°";
            } // calculate
        });
        ((DrawerSettingsRow) childData.get(parentData.get(5)).get(0)).initialise((int) ((maxTiltAngle / 10 * 10 - 10) / 0.8));

        // ACCELERATION TIME
        ((DrawerSettingsRow) childData.get(parentData.get(6)).get(0)).setSettingsFunction(new DrawerSettingsRow.SliderSettings() {
            @Override
            public String calculate(int progress) {
                accelerationTime = progress / 10 * 250;
                return accelerationTime + "ms";
            } // calculate
        });
        ((DrawerSettingsRow) childData.get(parentData.get(6)).get(0)).initialise(accelerationTime / 250 * 10);

        // DECELERATION TIME
        ((DrawerSettingsRow) childData.get(parentData.get(7)).get(0)).setSettingsFunction(new DrawerSettingsRow.SliderSettings() {
            @Override
            public String calculate(int progress) {
                decelerationTime = progress / 10 * 250;
                return decelerationTime + "ms";
            } // calculate
        });
        ((DrawerSettingsRow) childData.get(parentData.get(7)).get(0)).initialise(decelerationTime / 250 * 10);

        // TOUCH RESPONSIVENESS
        ((DrawerSettingsRow) childData.get(parentData.get(8)).get(0)).setSettingsFunction(new DrawerSettingsRow.SliderSettings() {
            @Override
            public String calculate(int progress) {
                touchResponsiveness = progress / 10 * 50;
                return touchResponsiveness + "ms";
            } // calculate
        });
        ((DrawerSettingsRow) childData.get(parentData.get(8)).get(0)).initialise(touchResponsiveness / 50 * 10);

        // TURN SENSITIVITY
        ((DrawerSettingsRow) childData.get(parentData.get(10)).get(0)).setSettingsFunction(new DrawerSettingsRow.SliderSettings() {
            @Override
            public String calculate(int progress) {
                turnSensitivity = progress / 25 + 1;
                mPiDroidMessenger.updateTurnSensitivity(turnSensitivity);
                return turnSensitivity + "";
            } // calculate
        });
        ((DrawerSettingsRow) childData.get(parentData.get(10)).get(0)).initialise((turnSensitivity - 1) * 25);

        // LEARN NEW OBJECT
        ((DrawerRow) parentData.get(11)).setRowFunction(new DrawerRow.ToggleSettings() {
            @Override
            public void toggle() {
                mDrawerLayout.closeDrawer(mDrawerListView);

                // Before we fire of the learning process, ask the user what this new object is
                new AlertDialog.Builder(parentData.get(11).getUIContext(), AlertDialog.THEME_HOLO_DARK)
                    .setTitle("What is this new object?")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setItems(R.array.object_names, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int index) {
                            new LearningTask(parentData.get(11).getUIContext(), LearningTask.PIDROID_LEARN, index).execute();
                        } // onClick
                    })
                    .setCancelable(false)
                    .create().show();
            } // toggle
        });

        // RECOGNISE OBJECT
        ((DrawerRow) parentData.get(12)).setRowFunction(new DrawerRow.ToggleSettings() {
            @Override
            public void toggle() {
                mDrawerLayout.closeDrawer(mDrawerListView);
                new LearningTask(parentData.get(12).getUIContext(), LearningTask.PIDROID_RECOGNISE).execute();
            } // toggle
        });

        // CLEAR LEARNING DATA
        ((DrawerRow) parentData.get(13)).setRowFunction(new DrawerRow.ToggleSettings() {
            @Override
            public void toggle() {
                new AlertDialog.Builder(parentData.get(13).getUIContext(), AlertDialog.THEME_HOLO_DARK)
                .setTitle("Clear Data")
                .setMessage("Are you sure you want to delete all learned data?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mPiDroidMessenger.clearLearningData();
                    } // onClick
                }) // .setPositiveButton
                .setNegativeButton(android.R.string.cancel, null)
                .create().show();
            } // toggle
        });

        // CAMERA STABILISATION
        ((DrawerRow) parentData.get(15)).setRowFunction(new DrawerRow.ToggleSettings() {
            @Override
            public void toggle() {
                mDrawerLayout.closeDrawer(mDrawerListView);
                cameraStabilisationOn = !cameraStabilisationOn;
                videoFeedMjpegView.setCameraStabilisation(cameraStabilisationOn);

                if (cameraStabilisationOn)
                    Toast.makeText(parentData.get(15).getUIContext(), "Camera stabilisation is now ON", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(parentData.get(15).getUIContext(), "Camera stabilisation is now OFF", Toast.LENGTH_LONG).show();
            } // toggle
        });

        // CHANGE HUD
        ((DrawerRow) parentData.get(16)).setRowFunction(new DrawerRow.ToggleSettings() {
            @Override
            public void toggle() {
                if (++currentHudResource == hudResources.length) currentHudResource = 0;
                hudImageView.setImageResource(hudResources[currentHudResource]);
            } // toggle
        });

        // LEVEL INDICATOR
        ((DrawerRow) parentData.get(17)).setRowFunction(new DrawerRow.ToggleSettings() {
            @Override
            public void toggle() {
                if (levelIndicatorImageView.getVisibility() == View.VISIBLE)
                    levelIndicatorImageView.setVisibility(View.INVISIBLE);
                else levelIndicatorImageView.setVisibility(View.VISIBLE);
            } // toggle
        });

        // TUTORIALS
        ((DrawerRow) parentData.get(18)).setRowFunction(new DrawerRow.ToggleSettings() {
            @Override
            public void toggle() {
                tutorialsOn = !tutorialsOn;
                mDrawerLayout.closeDrawer(mDrawerListView);

                if (tutorialsOn)
                    Toast.makeText(parentData.get(18).getUIContext(), "Tutorials are now ON", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(parentData.get(18).getUIContext(), "Tutorials are now OFF", Toast.LENGTH_LONG).show();
            } // toggle
        });
    } // createDrawerRows


    public void setControls(int id) {
        switch (id) {

        case TOUCH_CONTROLS:
            touchControlsLayout.setVisibility(View.VISIBLE);
            smallCameraJoystickView.setVisibility(View.VISIBLE);
            sliderControlsLayout.setVisibility(View.INVISIBLE);
            joystickControlsLayout.setVisibility(View.INVISIBLE);
            tiltControlsOn = true;
            videoFeedMjpegView.setCameraStabilisation(cameraStabilisationOn = true);
            break;

        case SLIDER_CONTROLS:
            sliderControlsLayout.setVisibility(View.VISIBLE);
            smallCameraJoystickView.setVisibility(View.VISIBLE);
            touchControlsLayout.setVisibility(View.INVISIBLE);
            joystickControlsLayout.setVisibility(View.INVISIBLE);
            tiltControlsOn = true;
            videoFeedMjpegView.setCameraStabilisation(cameraStabilisationOn = true);
            break;

        case JOYSTICK_CONTROLS:
            joystickControlsLayout.setVisibility(View.VISIBLE);
            touchControlsLayout.setVisibility(View.INVISIBLE);
            sliderControlsLayout.setVisibility(View.INVISIBLE);
            smallCameraJoystickView.setVisibility(View.INVISIBLE);
            tiltControlsOn = false;
            videoFeedMjpegView.setCameraStabilisation(cameraStabilisationOn = false);
            break;

        default:
            Log.e("setControls(): ", "default triggered with controlsID = " + controlsID);
            return ;
        } // switch

        controlsID = id;
    } // setControls


    public boolean onGroupClick(ExpandableListView parent, View view, int groupPosition, long id) {
        DrawerItem item = ((DrawerItem) mDrawerAdapter.getGroup(groupPosition));

        if (item instanceof DrawerRow)
            ((DrawerRow) item).executeRowFunction();

        return false;
    } // onGroupClick


    //**********************************************************************************************
    //                                    TILT CONTROLS
    //**********************************************************************************************


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    } // onAccuracyChanged


    @Override
    public void onSensorChanged(SensorEvent event) {
        // we received a sensor event. it is a good practice to check
        // that we received the proper event
        switch (event.sensor.getType()) {

        case Sensor.TYPE_ACCELEROMETER:
            mAccelerometerData = event.values;
            break;

        case Sensor.TYPE_MAGNETIC_FIELD:
            mGeomagneticData = event.values;

            if (tiltControlsOn) {
                computeTiltAngle();
                tiltControls();
            } // if
            break;

        default:
            Log.e("onSensorChanged", "wut?!");
            break;
        } // switch
    } // onSensorChanged


    private void computeTiltAngle() {
        float [] mRotationMatrix = new float[9];
        float [] mOrientationMatrix = new float[3];

        //
        if (!SensorManager.getRotationMatrix(mRotationMatrix, null, mAccelerometerData, mGeomagneticData)) return;
        else SensorManager.getOrientation(mRotationMatrix, mOrientationMatrix);

        //
        turnAngle = (int)Math.round(Math.toDegrees((double)mOrientationMatrix[1]) + 90);
    } // computeTiltAngle


    private void tiltControls() {
        //
        if (levelIndicatorImageView.getVisibility() == View.VISIBLE
                && turnAngle < 90 + maxTiltAngle && turnAngle > 90 - maxTiltAngle) {
            levelIndicatorImageView.setRotation(turnAngle - 90);
            videoFeedMjpegView.setTiltAngle(turnAngle - 90);
        } // if

        //
        if (turnAngle > 90 + maxTiltAngle) turnAngle = 90 + maxTiltAngle;
        if (turnAngle < 90 - maxTiltAngle) turnAngle = 90 - maxTiltAngle;

        //
        turnAngle = (int)Math.round((turnAngle - (90 - maxTiltAngle)) * 90.0 / maxTiltAngle) ;

        //
        turnAngle = turnAngle > 90 ? turnAngle / 10 * 10 : (turnAngle + 10) / 10 * 10;
        turnAngle = turnAngle % 20 == 0 ? turnAngle - 10 : turnAngle;

        //
        if (turnAngle != lastTurnAngle) {
            lastTurnAngle = turnAngle;
            mPiDroidMessenger.updateTurnAngle(turnAngle);
            Log.d("Controller: tiltControls():", "here's your tilt angle = " + turnAngle);
        } // if
    } // tiltControls


    //**********************************************************************************************
    //                                TOUCH SCREEN CONTROLS
    //**********************************************************************************************


    private boolean isTouchOnView(View view, MotionEvent motionEvent) {
        //
        int viewPosition[] = new int[4];
        view.getLocationOnScreen(viewPosition);

        viewPosition[2] = viewPosition[0] + view.getWidth();
        viewPosition[3] = viewPosition[1] + view.getHeight();

        //
        return motionEvent.getRawX() >= viewPosition[0] && motionEvent.getRawX() <= viewPosition[2]
            && motionEvent.getRawY() >= viewPosition[1] && motionEvent.getRawY() <= viewPosition[3];
    } // isTouchOnView


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getActionMasked();

        switch (view.getId()) {

        case R.id.forwardsPowerImageView:
            //
            if (action == MotionEvent.ACTION_DOWN) {
                if (roverAccelerationState != ROVER_STOPPED && roverDirection != ROVER_FORWARDS) {
                    roverAccelerationState = ROVER_STOPPED;
                    mPiDroidMessenger.updateRoverSpeed(roverSpeed = 0);
                } // if
                else {
                    roverDirection = ROVER_FORWARDS;
                    roverAccelerationState = ROVER_ACCELERATING;
                    resetTimer(new roverSpeedTask());
                } // else
            } // if
            else
            //
            if ((action == MotionEvent.ACTION_UP || !isTouchOnView(view, motionEvent))
             && roverAccelerationState != ROVER_DECELERATING) {
                roverAccelerationState = ROVER_DECELERATING;
                resetTimer(new roverSpeedTask());
            } // if
            break;


        case R.id.backwardsPowerImageView:
            //
            if (action == MotionEvent.ACTION_DOWN) {
                if (roverAccelerationState != ROVER_STOPPED && roverDirection != ROVER_BACKWARDS) {
                    roverAccelerationState = ROVER_STOPPED;
                    mPiDroidMessenger.updateRoverSpeed(roverSpeed = 0);
                } // if
                else {
                    roverDirection = ROVER_BACKWARDS;
                    roverAccelerationState = ROVER_ACCELERATING;
                    resetTimer(new roverSpeedTask());
                } // else
            } // if
            else
            if ((action == MotionEvent.ACTION_UP || !isTouchOnView(view, motionEvent))
              && roverAccelerationState != ROVER_DECELERATING) {
                roverAccelerationState = ROVER_DECELERATING;
                resetTimer(new roverSpeedTask());
            } // if
            break;

        default:
            Log.e("OnTouch:", "wut?");
        } // switch
        return true;
    } // onClick


    private class roverSpeedTask extends TimerTask {

        @Override
        public void run() {
            //
            if (roverAccelerationState == ROVER_ACCELERATING && roverSpeed < 100
             || roverAccelerationState == ROVER_DECELERATING && roverSpeed > 0)
                roverSpeed = calculateSpeed();

            //
            if (roverSpeed == 0) {
                timer.cancel(); timer.purge();
                roverAccelerationState = ROVER_STOPPED;
            } // if

            //
            if (roverSpeed / 25 * 25 != roverLastSpeed) {
                roverLastSpeed = (int)(roverSpeed / 25.0 * 25);
                if (roverDirection == ROVER_BACKWARDS) roverLastSpeed *= -1;
                mPiDroidMessenger.updateRoverSpeed(roverLastSpeed);
            } // if

            //
            if (roverDirection == ROVER_FORWARDS) {
                forwardsPowerProgressBar.setProgress(roverSpeed);
                backwardsPowerProgressBar.setProgress(0);
            } // if
            else {
                forwardsPowerProgressBar.setProgress(0);
                backwardsPowerProgressBar.setProgress(roverSpeed);
            } // else
        } // run
    } // increasePower


    private void resetTimer(TimerTask task) {
        timer.cancel(); timer.purge();
        timer = new Timer();

        timer.schedule(task, touchResponsiveness, timerPeriod);
    } // resetTimer


    private int calculateSpeed() {
        int speed = 0;

        switch (roverAccelerationState) {

        case ROVER_ACCELERATING:
            speed = (int)(roverSpeed + (double)timerPeriod / accelerationTime * 100);
            break;

        case ROVER_DECELERATING:
            speed = (int)(roverSpeed - (double)timerPeriod / decelerationTime * 100);
            break;

        case ROVER_STOPPED:
            speed = 0;
            break;

        default:
            Log.e("calculateSpeed(): ", "default triggered");
        } // switch

        return speed < 0 ? 0 : speed;
    } // calculateSpeed


    //**********************************************************************************************
    //                                  SEEKBAR CONTROLS
    //**********************************************************************************************


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //
        if (progress <= 20) {
            progress = 0;
            seekBar.setProgress(0);
        } // if

        //
        switch (seekBar.getId()) {

        case R.id.forwardsPowerSeekBar:
            if (backwardsPowerSeekBar.getProgress() > 0)
                forwardsPowerSeekBar.setProgress(0);
            else {
                roverSpeed = progress;
                forwardsPowerProgressBar.setProgress(progress);
            }
            break;

        case R.id.backwardsPowerSeekBar:
            if (forwardsPowerSeekBar.getProgress() > 0)
                backwardsPowerSeekBar.setProgress(0);
            else {
                roverSpeed = -progress;
                backwardsPowerProgressBar.setProgress(progress);
            }
            break;

        default:
            Toast.makeText(this, "onProgressChanged: SeekBar: wut?!", Toast.LENGTH_SHORT).show();
        } // switch
    } // onProgressChanged


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    } // onStartTrackingTouch


    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mPiDroidMessenger.updateRoverSpeed(roverSpeed);
    } // onStopTrackingTouch


    //**********************************************************************************************
    //                                  JOYSTICK CONTROLS
    //**********************************************************************************************


    private int cameraX = 0, cameraY = 0;

    @Override
    public void onMove(JoystickView view, double x, double y) {
        int newX = ((int)(x * 101)) / 20 * 20;
        int newY = ((int)(y * 101)) / 20 * -20;

        switch (view.getId()) {

        case R.id.cameraJoystickView:
        case R.id.largeCameraJoystickView:
            // If any of the pan/tilt positions have changed, tell PiDroid about it.
            if (cameraX != newX || cameraY != newY) {
                cameraX = newX;
                cameraY = newY;
                mPiDroidMessenger.updateCameraPosition(cameraX, cameraY);
                Log.d("Controller: onMove():", "cameraX = " + cameraX + ", cameraY = " + cameraY);
            } // if
            break;

        case R.id.directionJoystickView:
            // If there is a new roverSpeed, tell PiDroid about it.
            if (roverSpeed != newY) {
                roverSpeed = newY;
                mPiDroidMessenger.updateRoverSpeed(roverSpeed);
                //Log.d("Controller: onMove():", "roverSpeed = " + roverSpeed);
            } // if

            // Compute the angle made by the joystick measured with respect to the trigonometric circle.
            if (newX > 0)
                newX = (int)(Math.toDegrees(Math.atan(Math.abs((double)newY / newX))));
            else
                newX = (int)(Math.toDegrees(Math.atan(Math.abs((double)newX / newY))) + 90);

            // If there is a new turnAngle (or turning angle), tell PiDroid about it.
            if (turnAngle != newX) {
                turnAngle = newX;
                //Log.d("Controller: onMove():", "turn angle = " + turnAngle);
                mPiDroidMessenger.updateRoverSpeed(roverSpeed);
            } // if
            break;
        } // switch
    } // onMove


    @Override
    public void onRelease(JoystickView view) {
        switch (view.getId()) {

        case R.id.cameraJoystickView:
        case R.id.largeCameraJoystickView:

            // Tell PiDroid to reset the camera position.
            //mPiDroidMessenger.updateCameraPosition(cameraX = 0, cameraY = 0);
            break;

        case R.id.directionJoystickView:

            roverSpeed = 0;
            mPiDroidMessenger.updateRoverSpeed(roverSpeed);

            turnAngle = 90;
            mPiDroidMessenger.updateTurnAngle(turnAngle);
            break;
        } // switch
    } // onRelease


    //**********************************************************************************************
    //                                  VOICE RECOGNITION
    //**********************************************************************************************


    public void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, SPEECH_RECOGNITION_REQUEST_CODE);
    } // startVoiceRecognition


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SPEECH_RECOGNITION_REQUEST_CODE && intent != null) {
            ArrayList<String> predictions = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            mRecogniser.recogniseExecuteCommand(predictions);
        } // if

        super.onActivityResult(requestCode, resultCode, intent);
    } // onActivityResult


    //**********************************************************************************************
    //                                    VIDEO STREAM
    //**********************************************************************************************


    public boolean setupVideoFeed() {
        String URL = "http://" + serverIP + ":" + (serverPort + 1) + "/?action=stream";

        try {
            videoFeedTask = new VideoFeedTask();
            videoFeedTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, URL);
        } catch (Exception exception) {
            Log.e("Controller: setupVideoFeed():", "Could not connect to host!");
            return false;
        } // try-catch

        return true;
    } // setupVideoFeed


    public class VideoFeedTask extends AsyncTask<String, Void, MjpegInputStream> {

        @Override
        protected MjpegInputStream doInBackground(String... url) {
            HttpResponse res;
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpParams httpParams = httpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 5 * 1000);

            Log.d("Controller: VideoFeedTask: DoRead():", "1. Sending http request");
            try {
                res = httpClient.execute(new HttpGet(URI.create(url[0])));
                Log.e("Controller: VideoFeedTask: DoRead():", "2. Request finished, status = " + res.getStatusLine().getStatusCode());
                if(res.getStatusLine().getStatusCode()==401){
                    // You must turn off camera User Access Control before this will work
                    return null;
                }
                return new MjpegInputStream(res.getEntity().getContent());
            } catch (Exception e) {
                //e.printStackTrace();
                Log.e("Controller: VideoFeedTask: DoRead():", "Caught exception: ", e.getCause());
            } // try-catch
            return null;
        }

        @Override
        protected void onPostExecute(MjpegInputStream result) {
            videoFeedMjpegView.setSource(result);

            if(result!=null) {
                result.setSkip(1);
                //videoFeedOn = true;
            } // if
            //else
                //videoFeedOn = false;

            videoFeedMjpegView.showFps(false);
        } // onPostExecute
    } // VideoFeedTask


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
            mPiDroidMessenger.setRecogniserController(new PiDroidMessenger.RecogniserController() {
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

            Log.d("Controller: LearningTask():", "Progress dialog is showing...");
        } // constructor


        @Override
        protected Boolean doInBackground(Void... params) {
            // Because PiDroid uses the camera to learn new objects or recognise them, it stops the stream
            videoFeedMjpegView.stopPlayback();
            videoFeedTask.cancel(true);
            videoFeedOn = false;

            if (taskType == PIDROID_LEARN)
                // Tell PiDroid to kick off the learning process
                mPiDroidMessenger.learnNewObject(objectType);
            else
            if (taskType == PIDROID_RECOGNISE)
                // Tell PiDroid to kick off the recognition process
                mPiDroidMessenger.recogniseObject();

            // Here we wait for a reply from PiDroid meaning that the learning process has finished
            // The variable is set by the onLearnNewObjectReply() defined in the Constructor
            while (!videoFeedOn)
            {
                Log.d("Controller: LearningTask: doInBackground():", "Failed to restart video feed!");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException interruptedException) {
                    Log.d("Controller: LearningTask: doInBackground():", "Thread got interrupted");
                } // try - catch
            } // while

            setupVideoFeed();

            Log.d("Controller: LearningTask: doInBackground():", "Progress dialog is being dismissed...");

            return true;
        } // doInBackground


        @Override
        protected void onPostExecute(Boolean result) {
            dialog.dismiss();
        } // onPostExecute
    } // LearningTask

} // Controller
