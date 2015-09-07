package com.lovejoy777.rroandlayersmanager.loadingpackages;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.bitsyko.liblayers.Layer;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.interfaces.Callback;
import com.lovejoy777.rroandlayersmanager.interfaces.StoppableAsyncTask;
import com.lovejoy777.rroandlayersmanager.views.CheckBoxHolder;

//C extends Void is hack(?) to put Void there
public abstract class AbsLoadPackagesAsyncTask<A, B, C extends Void> extends StoppableAsyncTask<A, B, C> {

    protected Context context;
    protected CoordinatorLayout cordLayout;
    protected LinearLayout linearLayoutCategory1, linearLayoutCategory2;
    protected CardView cardViewCategory1, cardViewCategory2;
    protected Layer layer;
    protected boolean stop;
    protected Callback<CheckBox> checkBoxCallback;
    protected CheckBoxHolder.CheckBoxHolderCallback checkBoxHolderCallback;

    public AbsLoadPackagesAsyncTask(Context context, CoordinatorLayout cordLayout, Layer layer,
                                    Callback<CheckBox> checkBoxCallback,
                                    CheckBoxHolder.CheckBoxHolderCallback checkBoxHolderCallback) {
        this.context = context;
        this.cordLayout = cordLayout;
        this.layer = layer;
        this.checkBoxCallback = checkBoxCallback;
        this.checkBoxHolderCallback = checkBoxHolderCallback;
        this.stop = false;
    }

    @Override
    protected void onPreExecute() {
        linearLayoutCategory1 = (LinearLayout) cordLayout.findViewById(R.id.LinearLayoutCategory1);
        linearLayoutCategory2 = (LinearLayout) cordLayout.findViewById(R.id.LinearLayoutCategory2);
        cardViewCategory1 = (CardView) cordLayout.findViewById(R.id.CardViewCategory1);
        cardViewCategory2 = (CardView) cordLayout.findViewById(R.id.CardViewCategory2);
    }



    @Override
    public void stop() {
        stop = true;
    }

    @Override
    protected void onPostExecute(Void nothing) {

        //No styleSpecific Overlays
        if (linearLayoutCategory2.getChildCount() == 0) {
            cardViewCategory2.setVisibility(View.GONE);
        }
        //No normal Overlays
        if (linearLayoutCategory1.getChildCount() == 0) {
            cardViewCategory1.setVisibility(View.GONE);
        }

        linearLayoutCategory1.invalidate();
        linearLayoutCategory2.invalidate();

    }

}
