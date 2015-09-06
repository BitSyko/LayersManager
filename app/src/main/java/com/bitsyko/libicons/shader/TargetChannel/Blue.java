package com.bitsyko.libicons.shader.TargetChannel;

import com.bitsyko.libicons.shader.DataHolder;

public class Blue implements TargetChannelInterface {

    @Override
    public void save(DataHolder dataHolder, int value) {
        dataHolder.B = value;
    }

    @Override
    public int load(DataHolder dataHolder) {
        return dataHolder.B;
    }

}