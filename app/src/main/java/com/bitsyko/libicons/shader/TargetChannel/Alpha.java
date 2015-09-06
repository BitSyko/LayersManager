package com.bitsyko.libicons.shader.TargetChannel;

import com.bitsyko.libicons.shader.DataHolder;

public class Alpha implements TargetChannelInterface {

    @Override
    public void save(DataHolder dataHolder, int value) {
        dataHolder.A = value;
    }

    @Override
    public int load(DataHolder dataHolder) {
        return dataHolder.A;
    }
}
