/*
  Main.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.Activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import radu.pidroid.Managers.SettingsManager;
import radu.pidroid.R;


public class Main extends Activity implements View.OnClickListener {

    //
    public static final String EXTRA_REMEMBER_DETAILS_ON = "radu.pidroid.REMEMBERDETAILSON";


    //
    private CheckBox detailsCheckBox;
    private EditText ipTextField, portTextField;

    //
    private SettingsManager settings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) actionBar.hide();

        ipTextField   = (EditText) findViewById(R.id.ipTextField);
        portTextField = (EditText) findViewById(R.id.portTextField);
        detailsCheckBox = (CheckBox) findViewById(R.id.detailsCheckBox);

        Button connectButton;
        connectButton = (Button) findViewById(R.id.connectButton);
        connectButton.setOnClickListener(this);

        settings = new SettingsManager(this);
        settings.load();

        ipTextField.setText(settings.serverIP);
        portTextField.setText(settings.serverPort);
    } // onCreate


    @Override
    protected void onResume() {
        super.onResume();
        settings.load();
    } // onResume


    @Override
    protected void onPause() {
        super.onPause();
        settings.save();
    } // onPause


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    } // onCreateOptionsMenu


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // Gets executed when connectButton was pressed
            case R.id.connectButton:
                boolean rememberDetailsON = detailsCheckBox.isChecked();

                settings.serverIP = ipTextField.getText().toString();
                settings.serverPort = portTextField.getText().toString();
                //settings.save();

                // pass the boolean to the Controller activity with an Intent and start the Controller
                Intent intent = new Intent(this, Controller.class);
                intent.putExtra(EXTRA_REMEMBER_DETAILS_ON, rememberDetailsON);
                startActivity(intent);
                break;

            default:
                Log.e("Main", "onClick(): fell through default case!");
        } // switch
    } // onClick

} // Main
