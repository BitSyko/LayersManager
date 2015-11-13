package com.lovejoy777.rroandlayersmanager.loadingpackages;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TableRow;

import com.bitsyko.liblayers.Color;
import com.bitsyko.liblayers.Layer;
import com.bitsyko.liblayers.layerfiles.CustomStyleOverlay;
import com.bitsyko.liblayers.layerfiles.LayerFile;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.interfaces.Callback;
import com.lovejoy777.rroandlayersmanager.views.CheckBoxHolder;

import java.util.List;

//We don't hide/check anything
//This is just ShowPackagesFromList when list contains all overlays
public class ShowAllPackagesFromLayer extends ShowPackagesWithFilter {

    public ShowAllPackagesFromLayer(Context context, CoordinatorLayout cordLayout, Layer layer, Callback<CheckBox> checkBoxCallback, CheckBoxHolder.CheckBoxHolderCallback checkBoxHolderCallback) {
        super(context, cordLayout, layer, checkBoxCallback, checkBoxHolderCallback);
    }

    @Override
    boolean isEnabled(LayerFile layerFile) {
        //True for everything
        return true;
    }
}