/*
  DrawerAdapter.java

  Copyright (C) 2015 Radu Traian Jipa
  License: http://www.gnu.org/licenses/gpl-2.0.txt GNU General Public License v2
*/


package radu.pidroid.SettingsDrawer;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

import java.util.HashMap;
import java.util.List;


public class DrawerAdapter extends BaseExpandableListAdapter {

    /* Navigation drawer specifics */
    private final ExpandableListView listView;
    private boolean singleItemExpandable;

    private final List<DrawerItem> parentData;
    private final HashMap<DrawerItem, List<DrawerItem>> childData;


    public DrawerAdapter(ExpandableListView listView, List<DrawerItem> parentData,
                         HashMap<DrawerItem, List<DrawerItem>> childData) {

        this.listView = listView;
        this.singleItemExpandable = true;
        this.parentData = parentData;
        this.childData  = childData;
    } // constructor


    public void setSingleItemExpandable(boolean singleItemExpandable) {
        this.singleItemExpandable = singleItemExpandable;
    } // setSingleItemExpandable


    //**********************************************************************************************
    //                                   DRAWER PARENT ITEM
    //**********************************************************************************************


    private int lastExpandedGroupPosition;


    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        return parentData.get(groupPosition).getView();
    } // getGroupView


    @Override
    public Object getGroup(int groupPosition) {
        return parentData.get(groupPosition);
    } // getGroup


    @Override
    public long getGroupId(int groupPosition) {
        return parentData.get(groupPosition).getView().getId();
    } // getGroupId


    @Override
    public int getGroupCount() {
        return parentData.size();
    } // getGroupCount


    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    } // onGroupCollapsed


    @Override
    public void onGroupExpanded(int groupPosition) {

        if (groupPosition != lastExpandedGroupPosition && singleItemExpandable)
            listView.collapseGroup(lastExpandedGroupPosition);

        super.onGroupExpanded(groupPosition);
        lastExpandedGroupPosition = groupPosition;
    } // onGroupExpanded


    @Override
    public boolean hasStableIds() {
        return false;
    } // hasStableIds


    //**********************************************************************************************
    //                                  DRAWER CHILD ITEM
    //**********************************************************************************************


    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        return childData.get(parentData.get(groupPosition)).get(childPosition).getView();
    } // getChildView


    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childData.get(parentData.get(groupPosition)).get(childPosition);
    } // getChild


    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childData.get(parentData.get(groupPosition)).get(childPosition).getView().getId();
    } // getChildId


    @Override
    public int getChildrenCount(int groupPosition) {
        return childData.get(parentData.get(groupPosition)).size();
    } // getChildrenCount


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    } // getChildrenCount

} // DrawerAdapter
