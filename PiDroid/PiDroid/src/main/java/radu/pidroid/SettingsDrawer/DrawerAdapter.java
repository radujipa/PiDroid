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
    private final List<DrawerItem> parentData;
    private final HashMap<DrawerItem, List<DrawerItem>> childData;


    public DrawerAdapter(ExpandableListView listView, List<DrawerItem> parentData, HashMap<DrawerItem, List<DrawerItem>> childData) {
        this.listView = listView;
        this.parentData = parentData;
        this.childData  = childData;
    } // constructor


    //**********************************************************************************************
    //                                   DRAWER PARENT ITEM
    //**********************************************************************************************

    private int lastExpandedGroupPosition;


    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        return parentData.get(groupPosition).getItemView();
    } // getGroupView


    @Override
    public Object getGroup(int groupPosition) {
        return parentData.get(groupPosition);
    } // getGroup


    @Override
    public long getGroupId(int groupPosition) {
        return parentData.get(groupPosition).getItemView().getId();
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

        if (groupPosition != lastExpandedGroupPosition)
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
        return childData.get(parentData.get(groupPosition)).get(childPosition).getItemView();
    } // getChildView


    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childData.get(parentData.get(groupPosition)).get(childPosition);
    } // getChild


    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childData.get(parentData.get(groupPosition)).get(childPosition).getItemView().getId();
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
