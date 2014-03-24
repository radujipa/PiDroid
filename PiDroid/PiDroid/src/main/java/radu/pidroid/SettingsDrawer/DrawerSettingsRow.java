package radu.pidroid.SettingsDrawer;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SeekBar;
import android.widget.TextView;

import radu.pidroid.R;


public class DrawerSettingsRow extends DrawerItem
        implements SeekBar.OnTouchListener, SeekBar.OnSeekBarChangeListener {

    public interface SliderSettings {
        String calculate(int progress);
    } // Command interface

    private SeekBar  settingsSeekBar;
    private TextView settingsTextView;

    private SliderSettings settingsFunction;


    public DrawerSettingsRow(Context context, View itemView) {
        super(context, itemView);
        this.settingsTextView = (TextView) itemView.findViewById(R.id.settingsTextView);
        this.settingsSeekBar = (SeekBar) itemView.findViewById(R.id.settingsSeekBar);

        this.settingsSeekBar.setOnTouchListener(this);
        this.settingsSeekBar.setOnSeekBarChangeListener(this);
    } // constructor


    public void setSettingsFunction(SliderSettings function) {
        this.settingsFunction = function;
    } // setSettingsFunction


    public void initialise(int paramInt)
    {
        this.settingsSeekBar.setProgress(paramInt);
        this.settingsTextView.setText(this.settingsFunction.calculate(paramInt));
    } // initialise


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {

        // When the SeekBar is touched, disable Drawer touch event
        case MotionEvent.ACTION_DOWN:
            view.getParent().requestDisallowInterceptTouchEvent(true);
            break;

        // When the SeekBar is released, restore the Drawer touch event
        case MotionEvent.ACTION_UP:
            view.getParent().requestDisallowInterceptTouchEvent(false);
            break;
        } // switch

        // Handle SeekBar touch events
        view.onTouchEvent(motionEvent);
        return true;
    } // onTouch


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        settingsTextView.setText(settingsFunction.calculate(progress));
    } // onProgressChanged

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    } // onStartTrackingTouch

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    } // onStopTrackingTouch

} // DrawerSettingsRow