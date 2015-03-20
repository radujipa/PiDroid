/*
  DrawerItem.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


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