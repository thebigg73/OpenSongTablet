package com.garethevans.church.opensongtablet.appdata;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class PedalAccessibilityService extends AccessibilityService {

    private final String TAG = "PedalService";
    private MainActivityInterface mainActivityInterface;

    public void initialise(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
    }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Not used for this purpose
    }

    @Override
    public void onInterrupt() {
        // Not used
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        Log.d(TAG,"event.getAction():"+event.getAction());
        if (mainActivityInterface!=null) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                mainActivityInterface.onKeyDown(event.getKeyCode(),event);

            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                mainActivityInterface.onKeyUp(event.getKeyCode(),event);

            }
        }
        return false; // consume event
    }

}
