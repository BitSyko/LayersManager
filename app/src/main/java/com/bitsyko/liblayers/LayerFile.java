package com.bitsyko.liblayers;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import com.lovejoy777.rroandlayersmanager.commands.Commands;
import com.lovejoy777.rroandlayersmanager.helper.AndroidXMLDecompress;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

public class LayerFile {

    private Layer layer;
    private String name;
    private boolean color;
    private String colorName;
    private File file;
    private List Styles;
    private String fullName;
    private String selectedStyle;
    private int pluginVersion;

    private static final String PACKAGE_REGEX = "targetPackage=\"(.*?)\"";

    public LayerFile(Layer layer, String name, boolean color, int pluginVersion) {
        this.layer = layer;
        this.pluginVersion = pluginVersion;
        //Plugin version 3
        if (pluginVersion==3){
            this.fullName = name;
            this.name = name.replaceAll("\\[.*?\\]","");
        } else {
            this.name = name;
        }
        this.color = color;
    }

    public LayerFile(File file) {
        this.file = file;
        this.colorName = "General";
    }

    public String getRelatedPackage() {

        if (file == null) {
            throw new RuntimeException("No file to work on");
        }

        ZipFile zip;
        InputStream manifestInputStream;
        byte[] array;

        try {
            zip = new ZipFile(file);
            manifestInputStream = zip.getInputStream(zip.getEntry("AndroidManifest.xml"));
            array = IOUtils.toByteArray(manifestInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }


        Pattern pattern = Pattern.compile(PACKAGE_REGEX, Pattern.DOTALL);

        Matcher matcher = pattern.matcher(AndroidXMLDecompress.decompressXML(array));

        matcher.find();

        return matcher.group(1);

    }

    public File getFile(String color) throws IOException {

        if (file != null && file.exists() && color.equals(colorName)) {
            return file;
        }

        this.colorName = color;


        String fileName = name.replaceAll(" ", "");

        String tempDir = layer.getCacheDir() + File.separator + layer.getName();

        //We're creating cache directory
        File cacheFolder = new File(tempDir);

        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs();
        }

        //Plugin Version 3 // One zip for each Overlay
        File zipFile;
        if (pluginVersion==3){
            //PackName_OverlayName.zip
            zipFile = new File(tempDir + File.separator + layer.getName() + "_" + name + ".zip");

            if (!zipFile.exists()) {
                AssetManager assetManager = layer.getResources().getAssets();

                InputStream in = assetManager.open(("Files" + File.separator + layer.getName() + "_" + name + ".zip").replaceAll(" ", ""));
                FileUtils.copyInputStreamToFile(in, zipFile);
                in.close();

            }
        }
        //Plugin 1 // One zip for each color
        else{
            //We're checking if zip with apk is in temp
            zipFile = new File(tempDir + File.separator + layer.getName() + "_" + color + ".zip");

            if (!zipFile.exists()) {
                AssetManager assetManager = layer.getResources().getAssets();

                InputStream in = assetManager.open(("Files" + File.separator + layer.getName() + "_" + color + ".zip").replaceAll(" ", ""));
                FileUtils.copyInputStreamToFile(in, zipFile);
                in.close();

            }



        }

        ZipFile zipFile1;

        if (!layer.mapHasFile(zipFile.getName())) {
            zipFile1 = new ZipFile(zipFile);
            layer.putFileToMap(zipFile1, zipFile.getName());
        } else {
            zipFile1 = layer.getFileFromMap(zipFile.getName());
        }


        File zipTempDir;
        File destFile;
        System.out.println(layer.getName());
        //Plugin Version 3 // One zip for each Overlay
        if (pluginVersion==3){
            //We're extracting apk from zip
            zipTempDir = new File(tempDir + File.separator + layer.getName() + "_" + name);

            if (!zipTempDir.exists()) {
                zipTempDir.mkdirs();
            }

            destFile = new File(tempDir +
                    File.separator + layer.getName() + "_" + name + File.separator+ selectedStyle+
                    File.separator + (layer.getName() + "_" + fileName + ".apk").replaceAll(" ", ""));

            if (!destFile.exists()) {
                FileUtils.copyInputStreamToFile(Commands.fileFromZip(zipFile1, getSelectedStyle()+ File.separator +layer.getName() + "_" + fileName + ".apk"), destFile);
            }

        }
        //Plugin 1
        else {
            //We're extracting apk from zip
            zipTempDir = new File(tempDir + File.separator + layer.getName() + "_" + color);

            if (!zipTempDir.exists()) {
                zipTempDir.mkdirs();
            }

            destFile = new File(tempDir +
                    File.separator + layer.getName() + "_" + color + File.separator + (layer.getName() + "_" + fileName + ".apk").replaceAll(" ", ""));

            if (!destFile.exists()) {
                FileUtils.copyInputStreamToFile(Commands.fileFromZip(zipFile1, layer.getName() + "_" + fileName + ".apk"), destFile);
            }
        }
        System.out.println("ZipFile: "+zipFile);
        System.out.println("ZipFile: "+zipFile1);
        System.out.println("zipTempDir: "+zipTempDir);
        System.out.println("destFile: "+destFile);



        file = destFile;



        return destFile;

    }

    public File getFile() throws IOException {
        if (color) {
            throw new RuntimeException("Don't use getFile() for color layers");
        }
        return getFile("General");
    }

    public String getName() {
        return name;

    }

    public String getNiceName() {
        return name.replaceAll("_", " ");
    }

    public boolean isColor() {
        return color;
    }

    public Layer getLayer() {
        return layer;
    }

    public List<String> getColors() {
        List colors = new ArrayList();
        Matcher m = Pattern.compile("\\[([^\\]]+)").matcher(fullName);
        String test;
        while (m.find()){
            test = m.group(1);
            colors = Arrays.asList(test.split(";"));
        }


        return colors;
    }

    public boolean hasStyles(){
        boolean mhasStyles = fullName.contains("[");
        return mhasStyles;
    }

    public void setSelectedStyle(String color){
        this.selectedStyle = color;
    }

    public String getSelectedStyle(){
        return selectedStyle;
    }
}
