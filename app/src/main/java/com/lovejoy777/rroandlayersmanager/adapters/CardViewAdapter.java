package com.lovejoy777.rroandlayersmanager.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bitsyko.liblayers.Layer;
import com.lovejoy777.rroandlayersmanager.R;

import java.util.List;

import butterknife.ButterKnife;

public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.viewHolder> {

    private List<Layer> layersList;

    public CardViewAdapter(List<Layer> layersList) {
        this.layersList = layersList;
    }

    public Layer getLayerFromPosition(int position) {
        return layersList.get(position);
    }

    @Override
    public int getItemCount() {
        return layersList.size();
    }

    @Override
    public void onBindViewHolder(viewHolder viewHolder, int i) {
        Layer layer = layersList.get(i);
        viewHolder.title.setText(layer.getName());
        viewHolder.subtitle.setText(layer.getDeveloper());
        viewHolder.image.setImageDrawable(layer.getIcon());
    }

    @Override
    public viewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_cardview, viewGroup, false);

        return new viewHolder(itemView);
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        protected TextView title;
        protected TextView subtitle;
        protected ImageView image;

        public viewHolder(View v) {
            super(v);
            title = ButterKnife.findById(v,R.id.tv_cardViewAdapter_title);
            subtitle = ButterKnife.findById(v,R.id.tv_cardViewAdapter_subtitle);
            image = ButterKnife.findById(v,R.id.iv_cardViewAdapter_image);
        }
    }
}

