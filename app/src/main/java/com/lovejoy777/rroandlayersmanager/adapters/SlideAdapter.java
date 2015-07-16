package com.lovejoy777.rroandlayersmanager.adapters;

/**
 * Created by Niklas on 16.07.2015.
 */
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SlideAdapter extends Fragment {

    private static final String ARG_LAYOUT_RES_ID = "layoutResId";

    public static SlideAdapter newInstance(int layoutResId) {
        SlideAdapter sampleSlideAdapter = new SlideAdapter();

        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
        sampleSlideAdapter.setArguments(args);

        return sampleSlideAdapter;
    }

    private int layoutResId;

    public SlideAdapter() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null && getArguments().containsKey(ARG_LAYOUT_RES_ID))
            layoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(layoutResId, container, false);
    }

}