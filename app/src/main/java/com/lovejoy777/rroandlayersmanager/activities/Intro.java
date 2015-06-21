package com.lovejoy777.rroandlayersmanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.github.paolorotolo.appintro.AppIntro2;
import com.lovejoy777.rroandlayersmanager.slides.FifthSlide;
import com.lovejoy777.rroandlayersmanager.slides.FirstSlide;
import com.lovejoy777.rroandlayersmanager.slides.FourthSlide;
import com.lovejoy777.rroandlayersmanager.slides.SecondSlide;
import com.lovejoy777.rroandlayersmanager.slides.ThirdSlide;

public class Intro extends AppIntro2 {

    // Please DO NOT override onCreate. Use init
        @Override
        public void init(Bundle savedInstanceState) {
            addSlide(new FirstSlide(), getApplicationContext());
            addSlide(new SecondSlide(), getApplicationContext());
            addSlide(new ThirdSlide(), getApplicationContext());
            addSlide(new FourthSlide(), getApplicationContext());
            addSlide(new FifthSlide(), getApplicationContext());
            setFadeAnimation();
        }

    private void loadMainActivity(){
        Intent intent = new Intent(this, menu.class);
        startActivity(intent);
    }

    @Override
    public void onDonePressed() {
        loadMainActivity();
    }

    public void getStarted(View v){
        loadMainActivity();
    }
}