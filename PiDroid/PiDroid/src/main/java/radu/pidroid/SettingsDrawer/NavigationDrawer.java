/*
  NavigationDrawer.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.SettingsDrawer;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import radu.pidroid.R;


public class NavigationDrawer implements ExpandableListView.OnGroupClickListener {

    private DrawerLayout mDrawerLayout;
    private DrawerAdapter mDrawerAdapter;
    private ExpandableListView mDrawerListView;

    private List<DrawerItem> parentData;
    private HashMap<DrawerItem, List<DrawerItem>> childData;


    public NavigationDrawer(Activity activity) {
        this.mDrawerLayout = (DrawerLayout) activity.findViewById(R.id.DrawerLayout);
        this.mDrawerListView = (ExpandableListView) activity.findViewById(R.id.LeftDrawer);
        this.mDrawerListView.setOnGroupClickListener(this);

        this.parentData = new ArrayList<DrawerItem>();
        this.childData = new HashMap<DrawerItem, List<DrawerItem>>();

        this.mDrawerAdapter = new DrawerAdapter(mDrawerListView, parentData, childData);
        this.mDrawerListView.setAdapter(mDrawerAdapter);
    } // constructor


    public void openDrawer() {
        mDrawerLayout.openDrawer(this.mDrawerListView);
    } // openDrawer


    public void closeDrawer() {
        mDrawerLayout.closeDrawer(this.mDrawerListView);
    } // closeDrawer


    public void addCategory(DrawerCategory categoryRow) {
        parentData.addAll(categoryRow.getParentData());
        childData.putAll(categoryRow.getChildData());
    } // addCategory


    @Override
    public boolean onGroupClick(ExpandableListView parent, View view, int groupPosition, long id) {
        DrawerItem item = ((DrawerItem) mDrawerAdapter.getGroup(groupPosition));

        if (item instanceof DrawerToggleRow)
            ((DrawerToggleRow)item).toggleRow();

        return false;
    } // onGroupClick

} // NavigationDrawer
