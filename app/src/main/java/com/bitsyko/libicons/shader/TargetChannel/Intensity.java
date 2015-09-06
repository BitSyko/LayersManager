package com.bitsyko.libicons.shader.TargetChannel;

import com.bitsyko.libicons.shader.DataHolder;

public class Intensity implements TargetChannelInterface {
    @Override
    public void save(DataHolder dataHolder, int value) {
        throw new RuntimeException();
    }

    @Override
    public int load(DataHolder dataHolder) {
        int R = dataHolder.R;
        int G = dataHolder.G;
        int B = dataHolder.B;

        return (int) (0.21 * R + 0.72 * G + 0.07 * B);
    }
}
