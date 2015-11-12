package com.lovejoy777.rroandlayersmanager.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bitsyko.liblayers.Color;

import java.util.Arrays;
import java.util.List;

public class ColorSpinnerAdapter extends ArrayAdapter<Color> {

    List<Color> colors;

    public ColorSpinnerAdapter(Context context, int resource) {
        super(context, resource);
    }

    public ColorSpinnerAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public ColorSpinnerAdapter(Context context, int resource, Color[] objects) {
        super(context, resource, objects);
        this.colors = Arrays.asList(objects);
    }

    public ColorSpinnerAdapter(Context context, int resource, int textViewResourceId, Color[] objects) {
        super(context, resource, textViewResourceId, objects);
        this.colors = Arrays.asList(objects);
    }

    public ColorSpinnerAdapter(Context context, int resource, List<Color> objects) {
        super(context, resource, objects);
        this.colors = objects;
    }

    public ColorSpinnerAdapter(Context context, int resource, int textViewResourceId, List<Color> objects) {
        super(context, resource, textViewResourceId, objects);
        this.colors = objects;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        ((TextView) view).setText(colors.get(position).getNiceName());
        return view;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        ((TextView) view).setText(colors.get(position).getNiceName());
        return view;
    }
}
