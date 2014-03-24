package radu.pidroid.SettingsDrawer;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import radu.pidroid.R;


public class DrawerHeader extends DrawerItem {

    public TextView headerTextView;


    public DrawerHeader(Context context, View itemView) {
        super(context, itemView);
        this.headerTextView = (TextView) itemView.findViewById(R.id.headerText);
    } // constructor


    public void setHeaderText(String text) {
        headerTextView.setText(text);
    } // setHeaderText


    public String getHeaderText() {
        return headerTextView.getText().toString();
    } // getHeaderText

} // DrawerHeader