package com.lovejoy777.rroandlayersmanager.adapters;

/**
 * Created by Niklas on 06.06.2015.
 */

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.lovejoy777.rroandlayersmanager.helper.CardViewContent;
import com.lovejoy777.rroandlayersmanager.R;

import java.util.List;

public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.ContactViewHolder> {

    private List<CardViewContent> contactList;

    public CardViewAdapter(List<CardViewContent> contactList) {
        this.contactList = contactList;
    }


    @Override
    public int getItemCount() {
        return contactList.size();
    }

    @Override
    public void onBindViewHolder(ContactViewHolder contactViewHolder, int i) {
        CardViewContent ci = contactList.get(i);
        contactViewHolder.vName.setText(ci.themeName);
        contactViewHolder.vSurname.setText(ci.themeDeveloper);
        contactViewHolder.vImage.setImageDrawable(ci.themeImage);
        contactViewHolder.vMessage.setText(ci.message1);
        contactViewHolder.vMessage2.setText(ci.message2);

    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.adapter_card_layout, viewGroup, false);

        return new ContactViewHolder(itemView);
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder{

        protected TextView vName;
        protected TextView vSurname;
        protected ImageView vImage;
        protected TextView vMessage;
        protected TextView vMessage2;

        public ContactViewHolder(View v) {
            super(v);
            vName =  (TextView) v.findViewById(R.id.txtName);
            vSurname = (TextView)  v.findViewById(R.id.txtSurname);
            vImage = (ImageView) v.findViewById(R.id.iv_themeImage);
            vMessage = (TextView) v.findViewById(R.id.textView);
            vMessage2 = (TextView) v.findViewById(R.id.textView2);

        }
    }





    }

