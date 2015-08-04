package com.bitsyko.liblayers;

import android.content.res.AssetManager;

import com.lovejoy777.rroandlayersmanager.commands.Commands;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class LayerFile {

    private Layer layer;
    private String name;
    private boolean color;
    private String colorName;

    public LayerFile(Layer layer, String name, boolean color) {
        this.layer = layer;
        this.name = name;
        this.color = color;
    }

    public File getFile(String color) throws IOException {

        String fileName = name.replaceAll(" ", "");

        String tempDir = layer.getCacheDir() + File.separator + layer.getName();

        //We're creating cache directory
        File cacheFolder = new File(tempDir);

        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs();
        }


        //We're checking if zip with apk is in temp
        File zipFile = new File(tempDir + File.separator + layer.getName() + "_" + color + ".zip");

        if (!zipFile.exists()) {
            AssetManager assetManager = layer.getResources().getAssets();

            InputStream in = assetManager.open(("Files" + File.separator + layer.getName() + "_" + color + ".zip").replaceAll(" ", ""));
            FileUtils.copyInputStreamToFile(in, zipFile);
            in.close();

        }


        //We're extracting apk from zip
        File zipTempDir = new File(tempDir + File.separator + layer.getName() + "_" + color);

        if (!zipTempDir.exists()) {
            zipTempDir.mkdirs();
        }


        File destFile = new File(tempDir +
                File.separator + layer.getName() + "_" + color +
                File.separator + layer.getName() + "_" + fileName + ".apk");


        if (!destFile.exists()) {
            FileUtils.copyInputStreamToFile(Commands.fileFromZip(zipFile, layer.getName() + "_" + fileName + ".apk"), destFile);
        }


        return destFile;

    }

    public File getFile() throws IOException {
        return getFile("General");
    }

    public String getName() {
        return name;
    }

    public boolean isColor() {
        return color;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

}
