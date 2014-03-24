package radu.pidroid.Connector;


import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class PiDroidMessenger {

    public final static int PIDROID_ROVER_CONTROLLER = 0;
    public final static int PIDROID_SET_TURN_ANGLE = 0;
    public final static int PIDROID_SET_SPEED = 1;
    public final static int PIDROID_SET_TURN_SENSITIVITY = 2;
    public final static int PIDROID_TOGGLE_SPIN = 3;

    public final static int PIDROID_CAMERA_CONTROLLER = 1;
    public final static int PIDROID_SET_CAMERA_POSITION = 0;

    public final static int PIDROID_RECOGNISER_CONTROLLER = 2;
    public final static int PIDROID_LEARN_NEW_OBJECT = 0;
    public final static int PIDROID_RECOGNISE_OBJECT = 1;
    public final static int PIDROID_CLEAR_LEARNING_DATA = 2;


    private TCPClient mTCPClient;
    private String serverIP;
    private int serverPort;

    private RecogniserController recogniser;


    public PiDroidMessenger(Context UIContext, String ip, int port) {
        this.serverIP = ip;
        this.serverPort = port;

        new MessagingTask(UIContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
    } // constructor


    //**********************************************************************************************
    //                                  PUBLIC METHODS
    //**********************************************************************************************


    public void updateTurnAngle(int turnAngle) {
        mTCPClient.sendMessage(PIDROID_ROVER_CONTROLLER + ","
                             + PIDROID_SET_TURN_ANGLE + ","
                             + turnAngle);
    } // updateTurnAngle


    public void updateRoverSpeed(int roverSpeed) {
        mTCPClient.sendMessage(PIDROID_ROVER_CONTROLLER + ","
                             + PIDROID_SET_SPEED + ","
                             + roverSpeed);
    } // updateRoverSpeed


    public void updateTurnSensitivity(int turnSensitivity) {
        mTCPClient.sendMessage(PIDROID_ROVER_CONTROLLER + ","
                             + PIDROID_SET_TURN_SENSITIVITY + ","
                             + turnSensitivity);
    } // updateTurnSensitivity


    public void toggleSpin(boolean spin) {
        int onOff = (spin) ? 1 : 0;
        mTCPClient.sendMessage(PIDROID_ROVER_CONTROLLER + ","
                             + PIDROID_TOGGLE_SPIN + ","
                             + onOff);
    } // updateTurnSensitivity


    public void updateCameraPosition(int cameraX, int cameraY) {
        mTCPClient.sendMessage(PIDROID_CAMERA_CONTROLLER + ","
                             + PIDROID_SET_CAMERA_POSITION + ","
                             + cameraX + "," + cameraY);
    } // updateCameraPosition


    public void learnNewObject(int type) {
        mTCPClient.sendMessage(PIDROID_RECOGNISER_CONTROLLER + ","
                             + PIDROID_LEARN_NEW_OBJECT + ","
                             + type);
    } // learnNewObject


    public void recogniseObject() {
        mTCPClient.sendMessage(PIDROID_RECOGNISER_CONTROLLER + ","
                             + PIDROID_RECOGNISE_OBJECT + ",0");
    } // recogniseObject


    public void clearLearningData() {
        mTCPClient.sendMessage(PIDROID_RECOGNISER_CONTROLLER + ","
                + PIDROID_CLEAR_LEARNING_DATA + ",0");
    } // clearLearningData


    public void stopMessenger() {
        mTCPClient.stopClient();
    } // stopMessenger


    //**********************************************************************************************
    //                                      DISPATCHER
    //**********************************************************************************************


    public void dispatch(String reply) {
        String[] parts = reply.split(",");

        int controller = Integer.parseInt(parts[0]);
        int method = Integer.parseInt(parts[1]);
        int param = Integer.parseInt(parts[2]);

        switch (controller) {

        case PIDROID_CAMERA_CONTROLLER:
            Log.d("MessagingTask: dispatch():", "PIDROID_CAMERA_CONTROLLER nothing to do here");
            break;

        case PIDROID_ROVER_CONTROLLER:
            Log.d("MessagingTask: dispatch():", "PIDROID_ROVER_CONTROLLER nothing to do here");
            break;

        case PIDROID_RECOGNISER_CONTROLLER:
            //

            /*  */ if (method == 0) recogniser.onLearnNewObjectReply();
            else   if (method == 1) recogniser.onRecogniseObjectReply(param);
            break;

        default:
            Log.e("MessagingTask: dispatch():", "Switch fell through default case " +
                    "with controller = " + controller);
        } // switch

    } // dispatch


    //**********************************************************************************************
    //                                CONTROLLER INTERFACES
    //**********************************************************************************************


    public interface RecogniserController {
        public void onLearnNewObjectReply();
        public void onRecogniseObjectReply(int objType);
    } // RecogniserController


    public void setRecogniserController (RecogniserController controller) {
        this.recogniser = controller ;
    } // setRecogniserController


    //**********************************************************************************************
    //                                  CLIENT CONNECTOR
    //**********************************************************************************************


    public class MessagingTask extends AsyncTask<String,String,TCPClient> {

        private Context UIContext;

        public MessagingTask(Context context) {
            this.UIContext = context;
        } // constructor


        @Override
        protected TCPClient doInBackground(String... message) {
            try {
                // Create a TCPClient object and set a MessageReceivedListener
                mTCPClient = new TCPClient(serverIP, serverPort);
                mTCPClient.setMessageReceivedListener(new TCPClient.MessageReceivedListener() {
                    @Override
                    public void OnMessageReceived(String message) {
                        //this method calls the onProgressUpdate
                        publishProgress(message);
                    } // OnMessageReceived
                });

                // Start the client an start receiving those wonderful messages.
                mTCPClient.startClient();
            } // try
            catch(TCPClient.TCPClientException exception) {
                Log.e("MessagingTask: doInBackground():", "Failed to start client.");
                publishProgress(null);
            } // catch

            return null;
        } // TCPClient


        // This method runs on the UI thread!!
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            if (values == null)
                new AlertDialog.Builder(UIContext, AlertDialog.THEME_HOLO_DARK)
                .setTitle("Error")
                .setMessage("Could not connect to " + TCPClient.SERVER_IP + ":" + TCPClient.SERVER_PORT
                            + "\nYou can continue using the app.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, null)
                .create().show();
            else
                dispatch(values[0]) ;
        } // onProgressUpdate
    } // connectTask


} // PiDroidMessenger
