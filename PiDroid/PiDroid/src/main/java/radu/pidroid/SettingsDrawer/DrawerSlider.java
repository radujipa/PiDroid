/*
  DrawerSlider.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.SettingsDrawer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import radu.pidroid.R;


public class DrawerSlider extends DrawerItem {

    private View sliderRowView;
    private SeekBar seekBar;
    private TextView textView;


    public DrawerSlider(Activity activity) {
        super(activity);

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.sliderRowView = inflater.inflate(R.layout.drawer_seekbar_row, null);

        this.seekBar = (SeekBar) sliderRowView.findViewById(R.id.settingsSeekBar);
        this.textView = (TextView) sliderRowView.findViewById(R.id.settingsTextView);
    } // constructor


    @Override
    public View getView() {
        return this.sliderRowView;
    } // getView


    public SeekBar getSeekBar() {
        return this.seekBar;
    } // getSeekBar


    public String getSliderText() {
        return this.textView.getText().toString();
    } // getSliderText


    public void setSliderText(String text) {
        textView.setText(text);
    } // setSliderText

} // DrawerSlider
