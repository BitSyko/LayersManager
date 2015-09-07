package com.lovejoy777.rroandlayersmanager.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bitsyko.ApplicationInfo;
import com.bitsyko.liblayers.Layer;
import com.lovejoy777.rroandlayersmanager.R;

import java.util.List;

public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.ContactViewHolder> {

    private List<? extends ApplicationInfo> layersList;

    public CardViewAdapter(List<? extends ApplicationInfo> layersList) {
        this.layersList = layersList;
    }

    public ApplicationInfo getLayerFromPosition(int position) {
        return layersList.get(position);
    }

    @Override
    public int getItemCount() {
        return layersList.size();
    }

    @Override
    public void onBindViewHolder(ContactViewHolder contactViewHolder, int i) {
        ApplicationInfo layer = layersList.get(i);
        contactViewHolder.name.setText(layer.getName());
        contactViewHolder.developer.setText(layer.getDeveloper());
        contactViewHolder.image.setImageDrawable(layer.getIcon());
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.adapter_cardview, viewGroup, false);

        return new ContactViewHolder(itemView);
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {

        protected TextView name;
        protected TextView developer;
        protected ImageView image;

        public ContactViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.txtName);
            developer = (TextView) v.findViewById(R.id.txtSurname);
            image = (ImageView) v.findViewById(R.id.iv_themeImage);

        }
    }
}

