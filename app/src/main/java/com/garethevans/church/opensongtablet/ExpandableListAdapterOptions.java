/*
 * Copyright (c) 2015.
 * The code is provided free of charge.  You can use, modify, contribute and improve it as long as this source is referenced.
 * Commercial use should seek permission.
 */

package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapterOptions extends BaseExpandableListAdapter implements SectionIndexer, AbsListView.OnScrollListener {

    private Context _context;
    public final ExpandableListView expandableListView;
    private boolean manualScroll;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<String>> _listDataChild;
    private String[] groups;

    public ExpandableListAdapterOptions(ExpandableListView expandableListView, Context context, List<String> listDataHeader,
                                        HashMap<String, List<String>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this.expandableListView = expandableListView;
        this.expandableListView.setOnScrollListener(this);
        this.expandableListView.setFastScrollEnabled(false);
        this.expandableListView.setFastScrollEnabled(true);
    }

    @Override
    public Object[] getSections() {
        Log.d("getSections_options", new StringBuilder().append("groups = ").append(_listDataHeader).toString());
        return new String[0];
    }

    @Override
    public Object getChild(int groupPosition, int childPositition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPositition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
 
        final String childText = (String) getChild(groupPosition, childPosition);
 
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item, null);
        }
 
        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.lblListItem);
 
        txtListChild.setText(childText);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }
 
        TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);
 
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.manualScroll = scrollState == SCROLL_STATE_TOUCH_SCROLL;
    }

    @Override
    public void onScroll(AbsListView view,
                         int firstVisibleItem,
                         int visibleItemCount,
                         int totalItemCount) {
    }

    @Override
    public int getPositionForSection(int section) {
        Log.d("getPositionForSection","manualScroll="+manualScroll);
        if (manualScroll) {
            return section;
        } else {
            Log.d("getPositionForSection", "trying..."+ expandableListView.getFlatListPosition(
                    ExpandableListView.getPackedPositionForGroup(section)));
            return expandableListView.getFlatListPosition(
                    ExpandableListView.getPackedPositionForGroup(section));
        }
    }

    // Gets called when scrolling the list manually
    @Override
    public int getSectionForPosition(int position) {
        return ExpandableListView.getPackedPositionGroup(
                expandableListView
                        .getExpandableListPosition(position));
    }
}