/*
  DrawerToggleRow.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.SettingsDrawer;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import radu.pidroid.R;


public class DrawerToggleRow extends DrawerItem {

    public interface ToggleAction {
        void toggle();
    } // ToggleAction


    private View rowView;
    private ImageView rowIconImageView;
    private TextView rowTextView;
    private TextView isToggledTextView;

    private boolean isToggled;
    private ToggleAction rowFunction;


    public DrawerToggleRow(Activity activity) {
        super(activity);

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.rowView = inflater.inflate(R.layout.drawer_toggle_row, null);

        this.rowTextView = (TextView) rowView.findViewById(R.id.rowTextView);
        this.isToggledTextView = (TextView) rowView.findViewById(R.id.isToggledText);
        this.rowIconImageView = (ImageView) rowView.findViewById(R.id.rowIconImageView);

        this.isToggled = false;
        this.isToggledTextView.setText("OFF");
    } // constructor


    @Override
    public View getView() {
        return this.rowView;
    } // getView


    public void setRowText(String text) {
        rowTextView.setText(text);
    } // setRowText


    public void setIsToggledText(String text) {
        isToggledTextView.setText(text);
    } // setIsToggledText


    public void setIcon(int resID) {
        rowIconImageView.setImageResource(resID);
    } // setIcon


    public void setToggleAction(ToggleAction function) {
        this.rowFunction = function;
    } // setToggleAction


    public boolean isToggled() {
        return this.isToggled;
    } // isToggled


    public void toggleRow(boolean status) {
        if (isToggled != status) toggleRow();
    } // toggleRow


    public void toggleRow() {
        try {
            isToggled = !isToggled;
            String status = isToggled ? "ON" : "OFF";
            isToggledTextView.setText(status);

            rowFunction.toggle();
        } catch (Exception exception) {
            Log.e("DrawerToggleRow", "toggleRow(): null ToggleAction, call setToggleAction first!");
        } // try-catch
    } // toggleRow

} // DrawerToggleRow
