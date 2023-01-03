package com.garethevans.church.opensongtablet.screensetup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.view.RoundedCorner;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.core.graphics.Insets;
import androidx.core.view.DisplayCutoutCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class WindowFlags {

    /*
    Different options of window flags depending on user settings:
    * If the device has notches (camera, etc.) at the top of the screen,
      we can't draw the action bar over that.  Instead, we pad the actionbar
      down and then show the status bar since it is wasted space anyway.

    * If the user has selected immersive mode (by default), the navbar is hidden
      unless swiped in and the status bar is hidden (unless we have notches).
      If this is switched off, the activity view is set to display in system window
      and the status bar and navigation bar are always shown above the toolbar and
      below the song content.  The keepNavBarSpace variable is switched on.

    * If the user overrides the ignoreCutouts, the toolbar is shown right at the
      top, however, it may become unusable if the cutouts overlap the toolbar
      menu or settings icons, so pad these in to the left/right


     On boot up, the device cutouts are based on the chosen orientation
     * These are stored as safeCutoutL/R/T/B
     * On rotation of the device, the hasTopNotches checks if the temp top matches

     */

    private final Window w;
    private final MainActivityInterface mainActivityInterface;
    private final WindowInsetsControllerCompat windowInsetsController;
    private WindowInsetsCompat insetsCompat;
    private DisplayCutoutCompat displayCutoutCompat;
    private Insets systemGestures, navBars, statusBars;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String TAG = "WindowFlags";
    private final float density;
    private int customMarginLeft, customMarginRight, customMarginBottom, customMarginTop,
            statusHeight = 0, navHeight = 0, roundedLeft = 0, roundedRight = 0, roundedBottom = 0, roundedTop = 0,
            navLeft, navRight, currentRotation, firstBootRotation, cutoutTop, cutoutBottom,
            cutoutLeft, cutoutRight, softKeyboardHeight = 0, currentTopCutoutHeight = 0;
    private String navBarPosition = "b";
    private int[] totalMargins = new int[4];
    private boolean immersiveMode, ignoreCutouts, navBarKeepSpace,
            showStatus, showStatusInCutout, showNav, currentTopHasCutout,
            isNavAtBottom;
    private final int typeStatusBars = WindowInsetsCompat.Type.statusBars(),
            typeNavBars = WindowInsetsCompat.Type.navigationBars(),
            typeIme = WindowInsetsCompat.Type.ime(),
            typeGestures = WindowInsetsCompat.Type.systemGestures(),
            smallestScreenWidthDp;


    // This is called once when the class is instantiated.
    public WindowFlags(Context c, Window w) {
        this.w = w;
        mainActivityInterface = (MainActivityInterface) c;
        windowInsetsController = WindowCompat.getInsetsController(w, w.getDecorView());

        // Get the display density, smallest width and current orientation
        density = c.getResources().getDisplayMetrics().density;
        smallestScreenWidthDp = c.getResources().getConfiguration().smallestScreenWidthDp;

        // Get the preferences
        boolean defaultKeepNavSpace = false;
        try {
            @SuppressLint("DiscouragedApi") int resourceId = c.getResources().getIdentifier("config_navBarInteractionMode", "integer", "android");
            if (resourceId > 0) {
                if (c.getResources().getInteger(resourceId) == 2) {
                    defaultKeepNavSpace = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        navBarKeepSpace = mainActivityInterface.getPreferences().getMyPreferenceBoolean("navBarKeepSpace", defaultKeepNavSpace);
        customMarginLeft = mainActivityInterface.getPreferences().getMyPreferenceInt("marginLeft", 0);
        customMarginRight = mainActivityInterface.getPreferences().getMyPreferenceInt("marginRight", 0);
        customMarginTop = mainActivityInterface.getPreferences().getMyPreferenceInt("marginTop", 0);
        customMarginBottom = mainActivityInterface.getPreferences().getMyPreferenceInt("marginBottom", 0);
        immersiveMode = mainActivityInterface.getPreferences().getMyPreferenceBoolean("immersiveMode", true);
        ignoreCutouts = mainActivityInterface.getPreferences().getMyPreferenceBoolean("ignoreCutouts", false);
    }

    // Initialise the WindowInsetsCompat from MainActivity (once it is ready)
    public void setInsetsCompat(WindowInsetsCompat insetsCompat) {
        // This is sent from the main activity when the window is created and accessible
        this.insetsCompat = insetsCompat;
        firstBootRotation = w.getDecorView().getDisplay().getRotation();
        displayCutoutCompat = insetsCompat.getDisplayCutout();
        systemGestures = insetsCompat.getInsetsIgnoringVisibility(typeGestures);
        navBars = insetsCompat.getInsetsIgnoringVisibility(typeNavBars);
        statusBars = insetsCompat.getInsetsIgnoringVisibility(typeStatusBars);

        // Set the defaults that don't change on rotation/actions
        setFlags();
        setSystemBarHeights();
        setDeviceCutouts();
        setRoundedCorners();

        // Check if the top view has the cutout
        setCurrentTopHasCutout();

        // Decide if we should hide or show the system bars
        hideOrShowSystemBars();

        // Work out the margins for this orientation
        setMargins();
    }

    public WindowInsetsCompat getInsetsCompat() {
        return insetsCompat;
    }

    // Set the screen as edge to edge and decide if we are hiding the nav bar, etc.
    // This is only called once the WindowInsetCompat has been set or if changed
    public void setFlags() {
        // This sets the app as edge to edge (better than fullscreen)
        WindowCompat.setDecorFitsSystemWindows(w, false);
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        w.addFlags(View.SYSTEM_UI_FLAG_IMMERSIVE);
        w.addFlags(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        // Stop the screensaver kicking in
        w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Set the method for the soft keyboard/ime
        w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Hide the nav bars for immersive mode (if we don't want to keep the nav bar space),
        if (immersiveMode && !navBarKeepSpace) {
            windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            w.addFlags(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    private void setSystemBarHeights() {
        navLeft = systemGestures.left;
        navRight = systemGestures.right;

        statusHeight = statusBars.top;

        // The nav bar position depends on current rotation
        checkNavBarHeight();
    }
    public void checkNavBarHeight() {
        // Navbar could be left, bottom, right if screen is small and rotated)
        // Measure each position (should only be 1) and use the biggest
        navHeight = Math.max(navBars.bottom,Math.max(navBars.left,navBars.right));
    }

    // Set and hold a reference to the soft keyboard height
    // This is set from the mainActivity and is used to move views up
    public void setSoftKeyboardHeight(int softKeyboardHeight) {
        this.softKeyboardHeight = softKeyboardHeight;
    }

    public int getSoftKeyboardHeight() {
        return softKeyboardHeight;
    }

    public void hideKeyboard() {
        // Delay a few millisecs and then hide
        new Handler().postDelayed(() -> {
            windowInsetsController.hide(typeIme);
        },500);
    }

    public void adjustViewPadding(MainActivityInterface mainActivityInterface, View view) {
        if (softKeyboardHeight > 0) {
            view.setPadding(0, 0, 0, softKeyboardHeight);
        } else {
            view.setPadding(0, 0, 0, mainActivityInterface.getDisplayMetrics()[1] / 2);
        }
    }


    // Decide on what should be hidden from the statusBar and the navBar
    // This is called on first run or on an orientation change
    public void hideOrShowSystemBars() {
        // The navBar is always visible if not in immersive mode, or if navBarKeepSpace
        showNav = !immersiveMode || navBarKeepSpace;

        // The status bars are always visible when not in immersive mode,
        // Or if the current top bar has a cutout and we aren't ignoring cutouts
        showStatus = !immersiveMode;
        showStatusInCutout = currentTopHasCutout && !ignoreCutouts;

        if (showStatus || showStatusInCutout) {
            windowInsetsController.show(typeStatusBars);
        } else {
            windowInsetsController.hide(typeStatusBars);
        }

        if (showNav) {
            windowInsetsController.show(typeNavBars);
        } else {
            windowInsetsController.hide(typeNavBars);
        }
    }

    // Display cutouts
    // Get the device cutout positions (based on portrait orientation 0)
    private void setDeviceCutouts() {
        if (insetsCompat != null && displayCutoutCompat == null) {
            displayCutoutCompat = insetsCompat.getDisplayCutout();
        }
        if (displayCutoutCompat != null) {
            switch (firstBootRotation) {
                case 0:
                default:
                    cutoutTop = displayCutoutCompat.getSafeInsetTop();
                    cutoutLeft = displayCutoutCompat.getSafeInsetLeft();
                    cutoutRight = displayCutoutCompat.getSafeInsetRight();
                    cutoutBottom = displayCutoutCompat.getSafeInsetBottom();
                    break;

                case 1:
                    cutoutTop = displayCutoutCompat.getSafeInsetLeft();
                    cutoutLeft = displayCutoutCompat.getSafeInsetBottom();
                    cutoutRight = displayCutoutCompat.getSafeInsetTop();
                    cutoutBottom = displayCutoutCompat.getSafeInsetRight();
                    break;

                case 2:
                    cutoutTop = displayCutoutCompat.getSafeInsetBottom();
                    cutoutLeft = displayCutoutCompat.getSafeInsetRight();
                    cutoutRight = displayCutoutCompat.getSafeInsetLeft();
                    cutoutBottom = displayCutoutCompat.getSafeInsetTop();
                    break;

                case 3:
                    cutoutTop = displayCutoutCompat.getSafeInsetRight();
                    cutoutLeft = displayCutoutCompat.getSafeInsetTop();
                    cutoutRight = displayCutoutCompat.getSafeInsetBottom();
                    cutoutBottom = displayCutoutCompat.getSafeInsetLeft();
                    break;
            }

            if (statusHeight == cutoutTop) {
                // Likely the statusBar has been stretched to match the cutoutTop height
                statusHeight = (int) (24f * density);
            }
            if (navHeight > cutoutTop) {
                // If the cutout is already at the bottom and the nav height has added this too;
                navHeight -= cutoutTop;
            }
        }
    }

    private void setRoundedCorners() {
        // Rounded corners only supported in Android S and can't access via CompatInsets
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            WindowInsets insets = w.getDecorView().getRootWindowInsets();
            RoundedCorner topLeft = insets.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT);
            RoundedCorner topRight = insets.getRoundedCorner(RoundedCorner.POSITION_TOP_RIGHT);
            RoundedCorner bottomLeft = insets.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_LEFT);
            RoundedCorner bottomRight = insets.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_RIGHT);

            if (topLeft != null) {
                roundedLeft = topLeft.getRadius();
                roundedTop = topLeft.getRadius();
            }

            if (topRight != null) {
                roundedRight = topRight.getRadius();
                roundedTop = Math.max(roundedTop, topRight.getRadius());
            }

            if (bottomLeft != null) {
                roundedLeft = Math.max(roundedLeft, bottomLeft.getRadius());
                roundedBottom = bottomLeft.getRadius();
            }

            if (bottomRight != null) {
                roundedRight = Math.max(roundedRight, bottomRight.getRadius());
                roundedBottom = Math.max(roundedBottom, bottomRight.getRadius());
            }
        }
    }

    private void setCurrentTopHasCutout() {
        // We have already decided where the cutouts are (actual T, B, L, R)
        // These were calculated from the firstBootRotation when insets were calculated
        // Because the screen can be rotated, we need to see it the cutout is now at the top of the view
        switch (currentRotation) {
            case 0:
            default:
                currentTopHasCutout = cutoutTop > 0;
                currentTopCutoutHeight = cutoutTop;
                break;

            case 1:
                currentTopHasCutout = cutoutLeft > 0;
                currentTopCutoutHeight = cutoutLeft;
                break;

            case 2:
                currentTopHasCutout = cutoutBottom > 0;
                currentTopCutoutHeight = cutoutBottom;
                break;

            case 3:
                currentTopHasCutout = cutoutRight > 0;
                currentTopCutoutHeight = cutoutRight;
                break;
        }
    }

    public boolean getHasCutouts() {
        return cutoutLeft > 0 || cutoutTop > 0 || cutoutRight > 0 || cutoutBottom > 0;
    }

    public int getCurrentTopCutoutHeight() {
        return currentTopCutoutHeight;
    }


    // Status bar stuff
    public boolean getShowStatus() {
        return showStatus;
    }

    public boolean getShowStatusInCutout() {
        return showStatusInCutout;
    }

    public int getStatusHeight() {
        return statusHeight;
    }

    // Device rotation changes
    public void setCurrentRotation(int currentRotation) {
        this.currentRotation = currentRotation;
        if (smallestScreenWidthDp >= 600 || currentRotation == Surface.ROTATION_0 ||
                currentRotation == Surface.ROTATION_180) {
            // Nav bar will always be at the bottom
            navBarPosition = "b";
        } else if (currentRotation == Surface.ROTATION_90) {
            navBarPosition = "r";
        } else if (currentRotation == Surface.ROTATION_270) {
            navBarPosition = "l";
        }

        // Decide if we have the cutout at the top
        setCurrentTopHasCutout();

        // Because some devices move the nav bar, check the option based on the navBarPosition;
        checkNavBarHeight();

        // Decide if we should show or hide the system bars
        hideOrShowSystemBars();

        // Update the margins
        setMargins();
    }


    // Immersive mode changes
    public boolean getImmersiveMode() {
        return immersiveMode;
    }

    public void setImmersiveMode(boolean immersiveMode) {
        this.immersiveMode = immersiveMode;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("immersiveMode", immersiveMode);
    }


    // Ignore cutout changes
    public boolean getIgnoreCutouts() {
        return ignoreCutouts;
    }

    public void setIgnoreCutouts(boolean ignoreCutouts) {
        this.ignoreCutouts = ignoreCutouts;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("ignoreCutouts", ignoreCutouts);
    }


    // Keep nav bar changes
    public boolean getNavBarKeepSpace() {
        return navBarKeepSpace;
    }

    public void setNavBarKeepSpace(boolean navBarKeepSpace) {
        this.navBarKeepSpace = navBarKeepSpace;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("navBarKeepSpace", navBarKeepSpace);
    }


    // Custom margin changes
    public int getCustomMarginTop() {
        return customMarginTop;
    }

    public void setCustomMarginTop(int customMarginTop, boolean doSave) {
        this.customMarginTop = customMarginTop;
        if (doSave) {
            mainActivityInterface.getPreferences().setMyPreferenceInt("marginTop", customMarginTop);
        }
    }

    public int getCustomMarginLeft() {
        return customMarginLeft;
    }

    public void setCustomMarginLeft(int customMarginLeft, boolean doSave) {
        this.customMarginLeft = customMarginLeft;
        if (doSave) {
            mainActivityInterface.getPreferences().setMyPreferenceInt("marginLeft", customMarginLeft);
        }
    }

    public int getCustomMarginRight() {
        return customMarginRight;
    }

    public void setCustomMarginRight(int customMarginRight, boolean doSave) {
        this.customMarginRight = customMarginRight;
        if (doSave) {
            mainActivityInterface.getPreferences().setMyPreferenceInt("marginRight", customMarginRight);
        }
    }

    public int getCustomMarginBottom() {
        return customMarginBottom;
    }

    public void setCustomMarginBottom(int customMarginBottom, boolean doSave) {
        this.customMarginBottom = customMarginBottom;
        if (doSave) {
            mainActivityInterface.getPreferences().setMyPreferenceInt("marginBottom", customMarginBottom);
        }
    }


    // Get the margins to adjust the drawerlayout
    // The margins are based on the current rotation which might be non-default!
    // They are only measured on first run and if there is a change
    public void setMargins() {
        int marginL, marginR, marginT, marginB;
        int cutoutL, cutoutR, cutoutT, cutoutB;

        switch (currentRotation) {
            case 0: // 0 degrees.
            default:
                marginL = customMarginLeft + roundedLeft;
                cutoutL = cutoutLeft;
                marginR = customMarginRight + roundedRight;
                cutoutR = cutoutRight;
                marginT = customMarginTop + roundedTop;
                cutoutT = cutoutTop;
                marginB = customMarginBottom + roundedBottom; // + nav;
                cutoutB = cutoutBottom;
                break;
            case 1: // 270 degrees (one step anticlockwise)
                marginL = customMarginTop + roundedTop;
                cutoutL = cutoutTop;
                marginR = customMarginBottom + roundedBottom; // + nav;
                cutoutR = cutoutBottom;
                marginT = customMarginRight + roundedRight;
                cutoutT = cutoutRight;
                marginB = customMarginLeft + roundedLeft;
                cutoutB = cutoutLeft;
                break;
            case 2: // 180 degrees (two steps anticlockwise/clockwise)
                marginL = customMarginRight + roundedRight;
                cutoutL = cutoutRight;
                marginR = customMarginLeft + roundedLeft;
                cutoutR = cutoutLeft;
                marginT = customMarginBottom + roundedBottom;
                cutoutT = cutoutBottom;
                marginB = customMarginTop + roundedTop; // + nav;
                cutoutB = cutoutTop;
                break;
            case 3: // 90 degrees (three steps anticlockwise or 1 step clockwise)
                marginL = customMarginBottom + roundedBottom; // + nav;
                cutoutL = cutoutBottom;
                marginR = customMarginTop + roundedTop;
                cutoutR = cutoutTop;
                marginT = customMarginLeft + roundedLeft;
                cutoutT = cutoutLeft;
                marginB = customMarginRight + roundedRight;
                cutoutB = cutoutRight;
                break;
        }

        // Add the nav height to the correct margin
        int nav = 0;
        if (showNav) {
            nav = navHeight;
        }
        switch (navBarPosition) {
            case "b":
                marginB += nav;
                break;
            case "l":
                marginL += nav;
                break;
            case "r":
                marginR += nav;
                break;
        }

        currentTopCutoutHeight = cutoutT;

        if (ignoreCutouts) {
            totalMargins = new int[]{marginL, marginT, marginR, marginB};
        } else {
            if (currentTopHasCutout && showStatusInCutout) {
                cutoutT = 0;
            }
            totalMargins = new int[]{marginL + cutoutL, marginT + cutoutT, marginR + cutoutR, marginB + cutoutB};
        }
    }

    public int[] getMargins() {
        return totalMargins;
    }

}