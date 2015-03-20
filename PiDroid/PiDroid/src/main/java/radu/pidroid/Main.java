/*
  Main.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;


public class Main extends Activity implements View.OnClickListener {

    public final static String EXTRA_SERVERIP   = "radu.pidroid.SERVER_IP";
    public final static String EXTRA_SERVERPORT = "radu.pidroid.SERVER_PORT";
    public static CheckBox detailsCheckBox;

    EditText ipTextField, portTextField;
    Button connectButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ipTextField   = (EditText) findViewById(R.id.ipTextField);
        portTextField = (EditText) findViewById(R.id.portTextField);
        detailsCheckBox = (CheckBox) findViewById(R.id.detailsCheckBox);

        connectButton = (Button) findViewById(R.id.connectButton);
        connectButton.setOnClickListener(this);

        //
        SharedPreferences preferences = this.getPreferences(Context.MODE_PRIVATE);
        if (preferences.contains(EXTRA_SERVERIP) && preferences.contains(EXTRA_SERVERPORT))
        {
            ipTextField.setText(preferences.getString(EXTRA_SERVERIP, "0.0.0.0"));
            portTextField.setText(preferences.getInt(EXTRA_SERVERPORT, 8090) + "");
        } // if
    } // onCreate


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
            String serverIP = ipTextField.getText().toString();
            int serverPort = Integer.parseInt(portTextField.getText().toString());

            SharedPreferences preferences = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            //
            if (detailsCheckBox.isChecked())
            {
                editor.putString(EXTRA_SERVERIP, serverIP);
                editor.putInt(EXTRA_SERVERPORT, serverPort);
            }
            else
                editor.clear();
            editor.apply();

            // Switch to the PiDroid Controller activity
            Intent intent = new Intent(this, Controller.class);
            intent.putExtra(EXTRA_SERVERIP, serverIP);
            intent.putExtra(EXTRA_SERVERPORT, serverPort);
            startActivity(intent);
            break;
        } // switch
    } // onClick

} // Main
