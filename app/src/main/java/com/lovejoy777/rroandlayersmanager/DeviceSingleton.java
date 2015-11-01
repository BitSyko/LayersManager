package com.lovejoy777.rroandlayersmanager;

import android.os.Build;
import android.util.Log;

import com.lovejoy777.rroandlayersmanager.helper.Helpers;

import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

public class DeviceSingleton {

    private static Singleton device;

    public static Singleton getInstance() {

        if (device == null) {

            String mountData = Helpers.commandToString("mount");

            if (StringUtils.isEmpty(mountData)) {
                mountData = Helpers.commandToString("busybox mount");
            }

            Log.d("mount", mountData);

            String[] mountDataArray = StringUtils.split(mountData, "\n");

            boolean vendorDevice = false;

            for (String mountDataLine : mountDataArray) {

                String[] anotherStringArray = StringUtils.split(mountDataLine);

                if (anotherStringArray != null && anotherStringArray.length > 2 && anotherStringArray[1].equals("/vendor")) {
                    vendorDevice = true;
                    break;
                }

            }

            if (vendorDevice) {
                device = new VendorDevice();
                Log.d("Manager", "VendorDevice detected");
            } else {
                device = new NormalDevice();
                Log.d("Manager", "NormalDevice detected");
            }

            /*

            if (Build.DEVICE.equals("flounder") || Build.DEVICE.equals("flounder_lte")) {
                device = new VendorDevice();
                Log.d("Manager", "VendorDevice detected");
            } else {
                device = new NormalDevice();
            }
*/
        }

        return device;
    }


    public interface Singleton {
        String getOverlayFolder();

        String getMountFolder();
    }


    private static class VendorDevice implements Singleton {


        @Override
        public String getOverlayFolder() {
            return "/vendor/overlay";
        }

        @Override
        public String getMountFolder() {
            return "/vendor";
        }
    }


    private static class NormalDevice implements Singleton {


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
