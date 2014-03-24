package radu.pidroid.SettingsDrawer;

import radu.pidroid.SettingsDrawer.DrawerItem;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import radu.pidroid.R;


public class DrawerRow extends DrawerItem {

    public interface ToggleSettings {
        void toggle();
    } // Toggle


    private TextView rowTextView;
    private ImageView rowIconImageView;

    private ToggleSettings rowFunction;


    public DrawerRow(Context context, View itemView) {
        super(context, itemView);
        this.rowTextView = (TextView) itemView.findViewById(R.id.rowTextView);
        this.rowIconImageView = (ImageView) itemView.findViewById(R.id.rowIconImageView);
    } // constructor


    public void setRowText(String text) {
        rowTextView.setText(text);
    } // setText


    public void setIconResource(int resId) {
        rowIconImageView.setImageResource(resId);
    } // setIconResource


    public void setRowFunction(ToggleSettings function) {
        this.rowFunction = function;
    } // setRowFunction


    public void executeRowFunction() {
        if (rowFunction != null) rowFunction.toggle();
    } // executeRowFunction

} // DrawerRow
