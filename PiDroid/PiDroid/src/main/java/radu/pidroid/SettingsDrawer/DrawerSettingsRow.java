/*
  DrawerSettingsRow.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.SettingsDrawer;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;


public class DrawerSettingsRow extends DrawerToggleRow
        implements SeekBar.OnSeekBarChangeListener, SeekBar.OnTouchListener {

    public interface SliderSettings {
        String calculate(int progress);
    } // Command interface


    private DrawerSlider sliderRow;
    private SliderSettings settingsFunction;


    public DrawerSettingsRow(Activity activity) {
        super(activity);

        this.sliderRow = new DrawerSlider(activity);
        sliderRow.getSeekBar().setOnTouchListener(this);
        sliderRow.getSeekBar().setOnSeekBarChangeListener(this);

        setToggleAction(new ToggleAction() {
            @Override
            public void toggle() {
                if (isToggled()) setIsToggledText("");
                else setIsToggledText(sliderRow.getSliderText());
            } // toggle
        }); // setToggleAction
    } // constructor


    public DrawerSlider getSliderRow() {
        return this.sliderRow;
    } // getSliderRow


    public void setSettingsFunction(SliderSettings function) {
        this.settingsFunction = function;
    } // setSettingsFunction


    public void initialise(int paramInt) {
        sliderRow.getSeekBar().setProgress(paramInt);
        setIsToggledText(this.settingsFunction.calculate(paramInt));
        sliderRow.setSliderText(this.settingsFunction.calculate(paramInt));
    } // initialise


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (settingsFunction != null)
            sliderRow.setSliderText(settingsFunction.calculate(progress));
    } // onProgressChanged

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    } // onStartTrackingTouch

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    } // onStopTrackingTouch


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

} // DrawerSettingsRow