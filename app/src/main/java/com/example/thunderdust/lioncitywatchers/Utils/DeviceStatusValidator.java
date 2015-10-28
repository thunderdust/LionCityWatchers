package com.example.thunderdust.lioncitywatchers.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;

/**
 * Created by weiran.liu on 10/28/2015.
 * A <Singleton> device status checker to verify if the disk is mounted, the network is connected etc.
 * As such validations shall be conducted universally for all activities, it's best to keep a singleton
 * class and used it for all activities.
 */
public class DeviceStatusValidator {

    private static DeviceStatusValidator mValidator = null;

    private DeviceStatusValidator(){
    }

    public static DeviceStatusValidator getInstance(){
        if (mValidator==null){
            mValidator = new DeviceStatusValidator();
        }
        return mValidator;
    }

    public boolean isDiskMounted(){
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public boolean isNetworkConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork!=null && activeNetwork.isConnectedOrConnecting());
    }

    public int getDeviceAndroidVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }

    // Check if device android version equal or greater than certain version
    public boolean isDeviceUpdatedTo(int androidVersion){
        return Build.VERSION.SDK_INT >= androidVersion;
    }
}
