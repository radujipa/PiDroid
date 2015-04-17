/*
  DrawerHeader.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.SettingsDrawer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import radu.pidroid.R;


public class DrawerHeader extends DrawerItem {

    private View headerView;
    private TextView headerTextView;


    public DrawerHeader(Activity activity) {
        super(activity);

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.headerView = inflater.inflate(R.layout.drawer_header, null);

        this.headerTextView = (TextView) headerView.findViewById(R.id.headerText);
    } // constructor


    @Override
    public View getView() {
        return this.headerView;
    } // getView


    public String getHeaderText() {
        return headerTextView.getText().toString();
    } // getHeaderText


    public void setHeaderText(String text) {
        headerTextView.setText(text);
    } // setHeaderText

} // DrawerHeader