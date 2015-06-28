package com.lovejoy777.rroandlayersmanager.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.lovejoy777.rroandlayersmanager.R;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Niklas on 28.06.2015.
 */

public class CardViewAdapter2 extends RecyclerView.Adapter<CardViewAdapter2.ViewHolder>{

    private ArrayList<String> themes;
    private int rowLayout;
    private Context mContext;

    public CardViewAdapter2(ArrayList<String> themes, int rowLayout, Context context) {
        this.themes = themes;
        this.rowLayout = rowLayout;
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {

        viewHolder.themeName.setText(themes.get(i));
        viewHolder.themeName.setTag(i);
        //viewHolder.themeName.setOnCheckedChangeListener(mListener); // set the listener

    }

    @Override
    public int getItemCount() {
        return themes == null ? 0 : themes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CheckBox themeName;


        public ViewHolder(View itemView) {
            super(itemView);
            themeName = (CheckBox) itemView.findViewById(R.id.deletecheckbox);
        }

    }


   /* CompoundButton.OnCheckedChangeListener mListener = new CompoundButton.OnCheckedChangeListener() {

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            System.out.println(buttonView.getTag());
            for (int c=0; c<themes.size(); c++) {
                if (buttonView.getTag().equals(c)) {
                    if (buttonView.isChecked()){
                        InstallOverlayList.set(c, 1);

                        atleastOneIsClicked = atleastOneIsClicked + 1;

                    }
                    else {

                        InstallOverlayList.set(c, 0);
                        //InstallOverlay[c] = 0;
                        atleastOneIsClicked = atleastOneIsClicked -1;
                    }
                    if (atleastOneIsClicked> 0) {
                        fab2.setVisibility(View.VISIBLE);
                        fab2.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();


                    } else {

                        fab2.animate().translationY(fab2.getHeight()+48).setInterpolator(new AccelerateInterpolator(2)).start();
                        //fab2.setVisibility(View.INVISIBLE);

                    }
                }
            }
        }
    }; */
}
