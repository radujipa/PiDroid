/*
  VoiceControls.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.Controls;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.view.View;

import java.util.ArrayList;

import radu.pidroid.Activities.Controller;
import radu.pidroid.Connector.MethodInvocation;


public class VoiceControls implements View.OnClickListener, Controller.ActivityResultListener {

    //
    public final static int SPEECH_RECOGNITION_REQUEST_CODE = 513365;


    // references to modules
    private Controller controller;
    private MethodInvocation invocator;


    public VoiceControls(Controller controller, MethodInvocation invocator) {
        this.controller = controller;
        this.invocator = invocator;

        controller.addActivityResultListener(this);
    } // constructor


    @Override
    public void onClick(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        controller.startActivityForResult(intent, SPEECH_RECOGNITION_REQUEST_CODE);
    } // onClick


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SPEECH_RECOGNITION_REQUEST_CODE && intent != null) {
            ArrayList<String> predictions = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            invocator.recogniseExecuteCommand(predictions);
        } // if
    } // onActivityResult

} // VoiceControls
