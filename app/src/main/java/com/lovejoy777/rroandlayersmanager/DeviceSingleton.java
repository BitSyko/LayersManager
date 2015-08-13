package com.lovejoy777.rroandlayersmanager;

import android.os.Build;
import android.util.Log;

public class DeviceSingleton {

    private static Singleton device;

    public static Singleton getInstance() {

        if (device == null) {

            if (Build.DEVICE.equals("flounder") || Build.DEVICE.equals("flounder_lte")) {
                device = new N9();
                Log.d("Manager", "N9 detected");
            } else {
                device = new Other();
            }

        }

        return device;
    }


    public interface Singleton {
        String getOverlayFolder();

        String getMountFolder();
    }


    private static class N9 implements Singleton {


        @Override
        public String getOverlayFolder() {
            return "/vendor/overlay";
        }

        @Override
        public String getMountFolder() {
            return "/vendor";
        }
    }


    private static class Other implements Singleton {


        @Override
        public String getOverlayFolder() {
            return "/system/vendor/overlay";
        }

        @Override
        public String getMountFolder() {
            return "/system";
        }
    }


}
