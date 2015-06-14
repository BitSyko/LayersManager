package com.lovejoy777.rroandlayersmanager.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lovejoy777.rroandlayersmanager.R;

public class CustomListAdapter extends ArrayAdapter<String>{
    private final Activity context;
    private final String[] web;
    private final Integer[] imageId;
    public CustomListAdapter(Activity context,
                             String[] web, Integer[] imageId) {
        super(context, R.layout.adapter_listview_about, web);
        this.context = context;
        this.web = web;
        this.imageId = imageId;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.adapter_listview_about, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        txtTitle.setText(web[position]);
        imageView.setImageResource(imageId[position]);
        return rowView;
    }
}