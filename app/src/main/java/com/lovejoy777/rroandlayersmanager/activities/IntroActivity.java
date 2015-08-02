package com.lovejoy777.rroandlayersmanager.activities;

import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro2;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.adapters.SlideAdapter;

public class IntroActivity extends AppIntro2 {

    // Please DO NOT override onCreate. Use init
        @Override
        public void init(Bundle savedInstanceState) {
            addSlide(SlideAdapter.newInstance(R.layout.intro_1));
            addSlide(SlideAdapter.newInstance(R.layout.intro_2));
            addSlide(SlideAdapter.newInstance(R.layout.intro_3));
            addSlide(SlideAdapter.newInstance(R.layout.intro_4));
            addSlide(SlideAdapter.newInstance(R.layout.intro_5));
            setFadeAnimation();
        }


    @Override
    public void onDonePressed() {
        finish();
    }

}