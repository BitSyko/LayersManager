package com.bitsyko.libicons.shader.TargetChannel;

import com.bitsyko.libicons.shader.DataHolder;

public class Green implements TargetChannelInterface {

    @Override
    public void save(DataHolder dataHolder, int value) {
        dataHolder.G = value;
    }

    @Override
    public int load(DataHolder dataHolder) {
        return dataHolder.G;
    }

}