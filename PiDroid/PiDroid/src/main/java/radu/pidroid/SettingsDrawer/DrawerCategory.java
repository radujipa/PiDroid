/*
  DrawerCategory.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.SettingsDrawer;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DrawerCategory {

    private List<DrawerItem> parentData;
    private HashMap<DrawerItem, List<DrawerItem>> childData;


    public DrawerCategory(Activity activity, int id) {
        this.parentData = new ArrayList<DrawerItem>();
        this.childData = new HashMap<DrawerItem, List<DrawerItem>>();

        loadResources(activity, parentData, childData, id);
    } // constructor


    public void loadResources(Activity activity, List<DrawerItem> parentData,
                              HashMap<DrawerItem,List<DrawerItem>> childData, int id) {

        String[] mCategoryItemsList = activity.getResources().getStringArray(id);

        /*
         NOTE:  1. These constants must be in accordance with StringArray
                    "drawer_*" in res/values/drawer_*.xml
                2. Used only to help automate the creation process of drawer items
        */
        final int DRAWER_HEADER = 0, DRAWER_TOGGLE_ROW = 1, DRAWER_SETTINGS_ROW = 2;

        //
        DrawerItem parent, child;
        List<DrawerItem> children = new ArrayList<DrawerItem>();

        //
        for (String drawerItemText : mCategoryItemsList) {
            String[] parts = drawerItemText.split(",");

            switch (Integer.parseInt(parts[0])) {

                // For each case, we reinitialise the children ArrayList in order to make it
                // easy for future updates if we ever need to add a certain drawer_item
                // designed to always be a child - then no reinitialisation would be needed
                // since we reset it for each new parent

                case DRAWER_HEADER:
                    parent = new DrawerHeader(activity);
                    children = new ArrayList<DrawerItem>();
                    ((DrawerHeader) parent).setHeaderText(parts[1]);
                    parentData.add(parent);
                    break;

                case DRAWER_TOGGLE_ROW:
                    parent = new DrawerToggleRow(activity);
                    children = new ArrayList<DrawerItem>();
                    ((DrawerToggleRow) parent).setRowText(parts[1]);
                    ((DrawerToggleRow) parent).setIcon(android.R.drawable.ic_menu_always_landscape_portrait);
                    parentData.add(parent);
                    break;

                case DRAWER_SETTINGS_ROW:
                    parent = new DrawerSettingsRow(activity);
                    children = new ArrayList<DrawerItem>();
                    ((DrawerSettingsRow) parent).setRowText(parts[1]);
                    ((DrawerSettingsRow) parent).setIcon(android.R.drawable.ic_menu_always_landscape_portrait);
                    parentData.add(parent);
                    children.add(((DrawerSettingsRow) parent).getSliderRow());
                    break;

                default:
                    Log.e("DrawerCategory", "loadResources(): fell through default case! Check strings.xml");
                    return;
            } // switch

            //
            childData.put(parent, children);
        } // for
    } // loadResources


    public List<DrawerItem> getParentData() {
        return this.parentData;
    } // getParentData


    public HashMap<DrawerItem, List<DrawerItem>> getChildData() {
        return this.childData;
    } // getChildData

} // DrawerCategory
