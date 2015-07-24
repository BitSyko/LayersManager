package com.lovejoy777.rroandlayersmanager.beans;

public class FileBean {

    private final String location;
    private final String name;
    private final String fullname;
    private boolean checked;

    public FileBean(String location) {
        this.location = location;
        this.name = location.replace(".apk","").replace("_"," ");
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
}
