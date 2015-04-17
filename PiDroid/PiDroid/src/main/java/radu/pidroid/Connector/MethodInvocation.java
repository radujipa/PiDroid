/*
  MethodInvocation.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.Connector;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import radu.pidroid.R;


public class MethodInvocation {

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
    private final Messenger mMessenger;


    public MethodInvocation(Context context, Messenger messenger) {
        this.UIContext = context;
        this.mMessenger = messenger;

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
                    mMessenger.updateRoverSpeed(50);
                    mMessenger.updateTurnAngle(90);
                    break;

                case BACKWARD_COMMAND:
                    mMessenger.updateRoverSpeed(-50);
                    mMessenger.updateTurnAngle(90);
                    break;

                case TURN_LEFT_COMMAND:
                    mMessenger.updateTurnAngle(180);
                    break;

                case TURN_RIGHT_COMMAND:
                    mMessenger.updateTurnAngle(0);
                    break;

                case CAMERA_UP_COMMAND:
                    mMessenger.updateCameraPosition(0,100);
                    break;

                case CAMERA_DOWN_COMMAND:
                    mMessenger.updateCameraPosition(0,-100);
                    break;

                case CAMERA_LEFT_COMMAND:
                    mMessenger.updateCameraPosition(-100,0);
                    break;

                case CAMERA_RIGHT_COMMAND:
                    mMessenger.updateCameraPosition(100,0);
                    break;

                case STOP_RESET_COMMAND:
                    mMessenger.updateRoverSpeed(0);
                    mMessenger.updateCameraPosition(0,0);
                    mMessenger.updateTurnAngle(90);
                    break;

                case LEARN_OBJECT_COMMAND:
                    break;

                default:
                    Log.e("MethodInvocation", "recogniseExecuteCommand(): fell through default case " +
                            "with id = " + id);
            } // switch
    } // recogniseExecuteCommand

} // MethodInvocation
