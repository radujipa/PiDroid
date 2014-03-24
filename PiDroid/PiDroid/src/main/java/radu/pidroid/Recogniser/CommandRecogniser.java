package radu.pidroid.Recogniser;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import radu.pidroid.Connector.PiDroidMessenger;
import radu.pidroid.MjpegViewer.MjpegView;
import radu.pidroid.R;

public class CommandRecogniser {

    //
    public final static int FORWARD_COMMAND      = 0;
    public final static int BACKWARD_COMMAND     = 1;
    public final static int TURN_LEFT_COMMAND    = 2;
    public final static int TURN_RIGHT_COMMAND   = 3;
    public final static int CAMERA_UP_COMMAND    = 4;
    public final static int CAMERA_DOWN_COMMAND  = 5;
    public final static int CAMERA_LEFT_COMMAND  = 6;
    public final static int CAMERA_RIGHT_COMMAND = 7;
    public final static int STOP_RESET_COMMAND   = 8;

    //
    public final static int LEARN_OBJECT_COMMAND = 9;

    //
    private final int[] resources = {R.array.forward_commands, R.array.backward_commands,
            R.array.turn_left_commands, R.array.turn_right_commands, R.array.camera_up_commands,
            R.array.camera_down_commands, R.array.camera_left_commands, R.array.camera_right_commands,
            R.array.stop_reset_commands, R.array.object_recognition_commands};

    //
    private final String[][] commandsWithID = new String[resources.length][];

    private final Context UIContext;
    private final PiDroidMessenger mPiDroidMessenger;
    private final MjpegView mjpegView;


    public CommandRecogniser(Context context, PiDroidMessenger messenger, MjpegView mjpegView) {
        this.UIContext = context;
        this.mPiDroidMessenger = messenger;
        this.mjpegView = mjpegView;

        // Initialise the data structure with commands from /values/voice_commands
        for (int index = 0; index < resources.length; index++)
            commandsWithID[index] = context.getResources().getStringArray(resources[index]);
    } // constructor


    private boolean arrayContains(String[] array, ArrayList<String> predictions) {
        for (String string : array)
            for (String prediction : predictions)
                if (string.contains(prediction))
                    return true;
        return false;
    } // arrayContains


    public void recogniseExecuteCommand(ArrayList<String> predictions) {

        //
        for (int id = 0; id < resources.length; id++)
            if (arrayContains(commandsWithID[id], predictions))
                switch (id) {

                case FORWARD_COMMAND:
                    mPiDroidMessenger.updateRoverSpeed(50);
                    mPiDroidMessenger.updateTurnAngle(90);
                    break;

                case BACKWARD_COMMAND:
                    mPiDroidMessenger.updateRoverSpeed(-50);
                    mPiDroidMessenger.updateTurnAngle(90);
                    break;

                case TURN_LEFT_COMMAND:
                    mPiDroidMessenger.updateTurnAngle(180);
                    break;

                case TURN_RIGHT_COMMAND:
                    mPiDroidMessenger.updateTurnAngle(0);
                    break;

                case CAMERA_UP_COMMAND:
                    mPiDroidMessenger.updateCameraPosition(0,100);
                    break;

                case CAMERA_DOWN_COMMAND:
                    mPiDroidMessenger.updateCameraPosition(0,-100);
                    break;

                case CAMERA_LEFT_COMMAND:
                    mPiDroidMessenger.updateCameraPosition(-100,0);
                    break;

                case CAMERA_RIGHT_COMMAND:
                    mPiDroidMessenger.updateCameraPosition(100,0);
                    break;

                case STOP_RESET_COMMAND:
                    mPiDroidMessenger.updateRoverSpeed(0);
                    mPiDroidMessenger.updateCameraPosition(0,0);
                    mPiDroidMessenger.updateTurnAngle(90);
                    break;

                case LEARN_OBJECT_COMMAND:

                    break;

                default:
                    Log.e("CommandRecogniser: recogniseCommand(): ", "switch fell through default");
            } // switch
    } // recogniseExecuteCommand

} // CommandRecogniser
