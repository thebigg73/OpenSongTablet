package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.textview.MaterialTextView;

public class ShowToast {

    private final View anchor;
    private final PopupWindow popupWindow;
    private final MaterialTextView textToast;

    public ShowToast(Context c, View anchor) {
        this.anchor = anchor;
        popupWindow = new PopupWindow(c);
        LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_toast,null,false);
        popupWindow.setContentView(view);
        popupWindow.setFocusable(false);
        popupWindow.setBackgroundDrawable(null);
        textToast = view.findViewById(R.id.textToast);
    }

    public void doIt(String message) {
        try {

            // Toasts with custom layouts are deprecated and look ugly!
            //Toast toast = Toast.makeText(c, message, Toast.LENGTH_SHORT);
            //toast.show();

            // Use a more customisable popup window
            //LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //View view = inflater.inflate(R.layout.view_toast,null,false);
            textToast.setText(message);
            popupWindow.showAtLocation(anchor, Gravity.CENTER,0,0);
            Runnable r = popupWindow::dismiss;
            new Handler().postDelayed(r, 2000);

        } catch (Exception e) {
            Log.d("d","Error showing toast message");
            e.printStackTrace();
        }
    }
}
