package com.lovejoy777.rroandlayersmanager.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.beans.DeveloperBean;

import java.util.HashMap;
import java.util.List;

public class AboutAdapter extends BaseExpandableListAdapter {

    private final Activity context;
    private final List<String> headers;
    private final HashMap<String, List<DeveloperBean>> childData;

    static class ViewHolder {
        public ImageView image;
        public TextView text;
        public TextView text2;
    }


    public AboutAdapter(Activity context, List<String> headers, HashMap<String, List<DeveloperBean>> childData) {
        this.context = context;
        this.headers = headers;
        this.childData = childData;
    }


    @Override
    public int getGroupCount() {
        return headers.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return childData.get(headers.get(i)).size();
    }

    @Override
    public Object getGroup(int i) {
        return headers.get(i);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childData.get(headers.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View rowView, ViewGroup parent) {

        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.adapter_about_group, null);
        }

        TextView lblListHeader = (TextView) rowView
                .findViewById(R.id.header);

        lblListHeader.setText(headers.get(groupPosition));

        return rowView;

    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View rowView, ViewGroup parent) {

        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.adapter_listview_about, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.image = (ImageView) rowView.findViewById(R.id.img);
            viewHolder.text = (TextView) rowView
                    .findViewById(R.id.txt);
            viewHolder.text2 = (TextView) rowView
                    .findViewById(R.id.description);
            rowView.setTag(viewHolder);
        }


        DeveloperBean developer = childData.get(headers.get(groupPosition)).get(childPosition);

        ViewHolder holder = (ViewHolder) rowView.getTag();


        holder.image.setImageDrawable(developer.getImage());
        holder.text.setText(developer.getTitle());
        holder.text2.setText(developer.getDescription());

        return rowView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

}