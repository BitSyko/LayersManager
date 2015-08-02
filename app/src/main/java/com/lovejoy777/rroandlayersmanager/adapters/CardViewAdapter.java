package com.lovejoy777.rroandlayersmanager.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.beans.CardBean;

import java.util.List;

public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.ContactViewHolder> {

    private List<CardBean> contactList;

    public CardViewAdapter(List<CardBean> contactList) {
        this.contactList = contactList;
    }


    @Override
    public int getItemCount() {
        return contactList.size();
    }

    @Override
    public void onBindViewHolder(ContactViewHolder contactViewHolder, int i) {
        CardBean ci = contactList.get(i);
        contactViewHolder.name.setText(ci.getTitle());
        contactViewHolder.developer.setText(ci.getDescription());
        contactViewHolder.image.setImageDrawable(ci.getImage());
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

