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

import butterknife.ButterKnife;

public abstract class AbsLoadPackagesAsyncTask<A, B, Void> extends StoppableAsyncTask<A, B, Void> {

    protected Context context;
    protected CoordinatorLayout cordLayout;
    protected LinearLayout ll_category1, ll_category2;
    protected CardView cv_category1, cv_category2;
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
        ll_category1 = ButterKnife.findById(cordLayout, R.id.ll_plugindetail_general);
        ll_category2 = ButterKnife.findById(cordLayout, R.id.ll_plugindetail_style);
        cv_category1 = ButterKnife.findById(cordLayout, R.id.cv_plugindetail_category1);
        cv_category2 = ButterKnife.findById(cordLayout, R.id.cv_plugindetail_category2);
    }

    @Override
    public void stop() {
        stop = true;
    }

    @Override
    protected void onPostExecute(Void nothing) {

        //No styleSpecific Overlays
        if (ll_category2.getChildCount() == 0) {
            cv_category2.setVisibility(View.GONE);
        }
        //No normal Overlays
        if (ll_category1.getChildCount() == 0) {
            cv_category1.setVisibility(View.GONE);
        }

        ll_category1.invalidate();
        ll_category2.invalidate();
    }
}
