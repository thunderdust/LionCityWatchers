package com.example.thunderdust.lioncitywatchers.Utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by weiran.liu on 10/28/2015.
 * A <Singleton> class to display toast based on context and contents.
 * As such toaster shall be utilized universally for all activities,
 * it's best to keep a singleton class and used it for all activities.
 */
public class Toaster {

    private static Toaster mToaster = null;

    private Toaster(){

    }

    public static Toaster getInstance() {
        if (mToaster==null){
            mToaster = new Toaster();
        }
        return mToaster;
    }

    // Long toast at (default) bottom of screen
    public void ToastLongBottom(Context context, String message){
        Toast longToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        longToast.show();
    }

    // Short toast at (default) bottom of screen
    public void ToastShortBottom(Context context, String message){
        Toast shortToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        shortToast.show();
    }

    // Long toast at center of screen
    public void ToastLongCenter(Context context, String message){
        Toast longToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        longToast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
        longToast.show();
    }

    // Short toast at center of screen
    public void ToastShortCenter(Context context, String message){
        Toast shortToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        shortToast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
        shortToast.show();
    }
}
