package com.example.sayid.myapplication.common.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import android.content.Context;
import android.util.DisplayMetrics;

public class PhoneInfoUtil {
    private final static String TAG = "PhoneInfoUtil";

    // 获取CPU名字
    public static String getCpuName() {
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = null;
            while (true) {
                text = br.readLine();
                if (text != null) {
                    if (text.indexOf("Hardware") != -1) {
                        break;
                    }
                }
            }
            if (text != null) {
                String[] array = text.split(":\\s+", 2);
                return array[1];
            }
        } catch (IOException e) {
            //.e(TAG, e, "Error:");
        }
        return null;
    }

    public static int getDensityDpi(Context cxt) {
        DisplayMetrics dm = cxt.getResources().getDisplayMetrics();
        int densityDPI = dm.densityDpi;
        return densityDPI;
    }

}
