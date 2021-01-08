package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class ShowToast {

    // If we need to build a message before displaying
    private String message;
    public void setMessage(String message) {
        this.message = message;
    }

    public void doIt(Context c, String message) {
        try {
            Toast toast = Toast.makeText(c, message, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } catch (Exception e) {
            Log.d("d","Error showing toast message");
            e.printStackTrace();
        }
    }

    public void useMessage(Context c) {
        doIt(c,message);
    }

    public static void showToast(Context c, String message) {
        try {
            Toast toast = Toast.makeText(c, message, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } catch (Exception e) {
            Log.d("d","Error showing toast message");
            e.printStackTrace();
        }
    }
}
