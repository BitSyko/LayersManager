package com.lovejoy777.rroandlayersmanager.beans;

import com.lovejoy777.rroandlayersmanager.helper.AndroidXMLDecompress;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

public class FileBean {

    private final String location;
    private final String name;
    private final String fullname;
    private boolean checked;

    private static final String PACKAGE_REGEX = "targetPackage=\"(.*?)\"";

    public FileBean(String location) {
        this.location = location;
        this.name = location.replace(".apk", "").replace("_", " ");
        this.fullname = location;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullname;
    }

    public String getRelatedPackage() {

        // TO-DO: this needs de-hardcoding, possibly use getOverlayFolder?
        // After that, eliminate the two same code instances, here and in layerFile (somehow)
        File file = new File("/system/vendor/overlay/" + location);

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

    public boolean isInstallable() {
        return getFullName().endsWith(".zip") || getFullName().endsWith(".apk");
    }

}
