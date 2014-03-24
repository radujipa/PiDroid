package radu.pidroid.SettingsDrawer;

import android.content.Context;
import android.view.View;


public abstract class DrawerItem {

    private final Context UIContext;
    private final View itemView;


    public DrawerItem(Context context, View itemView) {
        this.UIContext = context;
        this.itemView = itemView;
    } // constructor


    public View getItemView() {
        return itemView;
    } // getView


    public Context getUIContext() {
        return UIContext;
    } // getUIContext

} // DrawerItem