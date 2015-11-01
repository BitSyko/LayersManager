package com.lovejoy777.rroandlayersmanager.beans;

import java.io.File;

//TODO: Rewrite this
public class FileBean {

    private final String location;
    private final String name;
    private final String fullname;
    private File file;
    private boolean checked;

    public FileBean(File file) {
        this(file.getName());
        this.file = file;
    }

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

    public boolean isInstallable() {
        return getFullName().endsWith(".zip") || getFullName().endsWith(".apk");
    }

    public File getFile() {
        return file;
    }

}
