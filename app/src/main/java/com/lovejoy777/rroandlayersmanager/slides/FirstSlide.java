package com.lovejoy777.rroandlayersmanager.slides;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.lovejoy777.rroandlayersmanager.R;

/**
 * Created by Niklas on 21.06.2015.
 */
public class FirstSlide extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.intro_1, container, false);
        return v;
    }
}