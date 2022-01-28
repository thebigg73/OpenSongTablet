package com.garethevans.church.opensongtablet.appdata;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class SoftKeyboard {

    public void showSoftKeyboard(Context c, View view){
        if(view.requestFocus()){
            InputMethodManager imm = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view,InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void hideSoftKeyboard(Context c, View view){
        InputMethodManager imm =(InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
