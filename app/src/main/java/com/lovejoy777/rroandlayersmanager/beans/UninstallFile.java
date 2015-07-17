package com.lovejoy777.rroandlayersmanager.beans;

public class UninstallFile {

    private final String location;
    private final String name;
    private boolean checked;

    public UninstallFile(String location) {
        this.location = location;
        this.name = location.replace(".apk","").replace("_"," ");
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
}
