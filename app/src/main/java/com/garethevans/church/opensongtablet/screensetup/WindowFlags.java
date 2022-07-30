package com.garethevans.church.opensongtablet.screensetup;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class WindowFlags {

    private final Window w;
    private final WindowInsetsControllerCompat windowInsetsController;
    private final String TAG = "WindowFlags";
    private boolean currentImmersive = false;
    private final int insetTypes = WindowInsetsCompat.Type.systemBars() |
            WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars();

    public WindowFlags(Window w) {
        this.w = w;
        windowInsetsController = WindowCompat.getInsetsController(w,w.getDecorView());
        // Configure the behavior of the hidden system bars
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        setFlags(true);
    }

    private void setFlags(boolean immersiveOn) {
        if (immersiveOn!=currentImmersive) {
            currentImmersive = immersiveOn;
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            w.addFlags(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            WindowCompat.setDecorFitsSystemWindows(w, false);
            // Configure the behavior of the hidden system bars
            windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

            w.getDecorView().setSystemUiVisibility(getUiOptions(immersiveOn));
        }
    }

    public void setImmersive(boolean immersiveOn) {
        //Log.d(TAG,"immersiveOn: "+immersiveOn+"   currentImmersive:"+currentImmersive);
        setFlags(immersiveOn);

        if (immersiveOn) {
            windowInsetsController.hide(insetTypes);
        } else {
            windowInsetsController.show(insetTypes);
        }

        /*if (immersiveOn!=currentImmersive) {
            currentImmersive = immersiveOn;
        }*/





        /*w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        int uiOptions = setUiOptions(immersiveOn);
        if (Build.VERSION.SDK_INT<30) {
            v.setSystemUiVisibility(uiOptions);
        }

        if (immersiveOn) {
            addFlags();
            hideInsets();
        } else {
            showInsets();
        }

        w.getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
            Log.d(TAG,"visibility="+visibility);
            Log.d(TAG,"View.SYSTEM_UI_FLAG_VISIBLE="+View.SYSTEM_UI_FLAG_VISIBLE);

            int newuiOptions;
            if (visibility == 0) {
                // Do this after a pause
                new Handler()
                Log.d(TAG,"Setting fullscreen");
                hideInsets();
                newuiOptions= setUiOptions(true);
            } else {
                Log.d(TAG,"Leaving fullscreen");
                showInsets();
                newuiOptions= setUiOptions(false);
            }
            w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            if (Build.VERSION.SDK_INT<30) {
                v.setSystemUiVisibility(newuiOptions);
            }
        });*/
    }

    private int getUiOptions(boolean immersiveOn) {
        int uiOptions;
        if (immersiveOn) {
            uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_IMMERSIVE |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        } else {
            uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        }
        return uiOptions;
    }

    /*private void addFlags() {
        w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        w.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        w.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }*/

    /*private void hideInsets() {
        if (Build.VERSION.SDK_INT>=30) {
            w.getDecorView().getWindowInsetsController().hide(
                    WindowInsetsCompat.Type.systemBars());
        }
    }*/

    /*private void showInsets() {
        if (Build.VERSION.SDK_INT>=30) {
            w.getDecorView().getWindowInsetsController().show(
                    WindowInsetsCompat.Type.systemBars());
        } else {
            if (ViewCompat.getWindowInsetsController(w.getDecorView())!=null) {
                ViewCompat.getWindowInsetsController(w.getDecorView()).show(
                        WindowInsetsCompat.Type.systemBars());
            }
        }
    }*/


    public void forceImmersive() {
        try {
            windowInsetsController.hide(WindowInsetsCompat.Type.ime());
            currentImmersive = false;
            setImmersive(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void adjustViewPadding(MainActivityInterface mainActivityInterface, View view) {
        if (mainActivityInterface.getSoftKeyboardHeight()>0) {
            view.setPadding(0,0,0,mainActivityInterface.getSoftKeyboardHeight());
        } else {
            view.setPadding(0,0,0,mainActivityInterface.getDisplayMetrics()[1]/2);
        }
    }
}

