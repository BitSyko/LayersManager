package com.bitsyko.libicons.shader;

import android.graphics.Color;

import com.bitsyko.libicons.shader.TargetChannel.Alpha;
import com.bitsyko.libicons.shader.TargetChannel.Blue;
import com.bitsyko.libicons.shader.TargetChannel.Green;
import com.bitsyko.libicons.shader.TargetChannel.Intensity;
import com.bitsyko.libicons.shader.TargetChannel.Red;
import com.bitsyko.libicons.shader.TargetChannel.TargetChannelInterface;

public class Exec {

    String t;
    String m;
    String v;

    public Exec(String t, String m, String v) {
        this.t = t;
        this.m = m;
        this.v = v;
    }

    public String getT() {
        return t;
    }

    public String getM() {
        return m;
    }

    public String getV() {
        return v;
    }

    public void parse(DataHolder holder, int pixelColor) {

        char channel = t.charAt(1);

        TargetChannelInterface output;

        switch (channel) {
            case 'A':
                output = new Alpha();
                break;
            case 'B':
                output = new Blue();
                break;
            case 'R':
                output = new Red();
                break;
            case 'G':
                output = new Green();
                break;
            default:
                throw new RuntimeException("Wrong channel");

        }


        boolean isVValue = !(v.charAt(0) == 'I');

        float value;

        if (isVValue) {

            value = Float.valueOf(v);

        } else {

            TargetChannelInterface input;

            switch (v.charAt(1)) {

                case 'A':
                    value = Color.alpha(pixelColor);
                    ;
                    break;
                case 'B':
                    value = Color.blue(pixelColor);
                    break;
                case 'R':
                    value = Color.red(pixelColor);
                    break;
                case 'G':
                    value = Color.green(pixelColor);
                    break;
                case 'I':
                    value = calculateIntensity(pixelColor);
                    break;
                default:
                    throw new RuntimeException("Wrong input channel");
            }

            // value = input.load(holder);

        }

        int currentValue = output.load(holder);


        switch (m.charAt(0)) {
            case 'W':
                output.save(holder, (int) value);
            case 'S':
                output.save(holder, currentValue - (int) value);
            case 'M':
                output.save(holder, (int) (currentValue * value));
            case 'D':
                output.save(holder, (int) (currentValue / value));
            case 'A':
                output.save(holder, currentValue + (int) value);

        }


    }

    public int calculateIntensity(int color) {
        int R = Color.red(color);
        int G = Color.green(color);
        int B = Color.blue(color);

        return (int) (0.21 * R + 0.72 * G + 0.07 * B);
    }

}


