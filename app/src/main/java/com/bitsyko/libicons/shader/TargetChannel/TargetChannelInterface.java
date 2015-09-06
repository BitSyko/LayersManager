package com.bitsyko.libicons.shader.TargetChannel;


import com.bitsyko.libicons.shader.DataHolder;

public interface TargetChannelInterface {
    void save(DataHolder dataHolder, int value);
    int load(DataHolder dataHolder);
}
