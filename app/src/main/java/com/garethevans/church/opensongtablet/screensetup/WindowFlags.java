package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
    private final WindowInsetsControllerCompat windowInsetsControllerCompat;
    private WindowInsetsCompat insetsCompat;
    private DisplayCutoutCompat displayCutoutCompat;
    private Insets systemGestures, navBars, statusBars, systemBars, mandatoryInset;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String TAG = "WindowFlags";
    private final float density;
    private int customMarginLeft, customMarginRight, customMarginBottom, customMarginTop,
            statusHeight = 0, navHeight = 0,
            roundedLeft = 0, roundedRight = 0, roundedBottom = 0, roundedTop = 0,
            marginToolbarLeft, marginToolbarRight,
            navLeft, navRight,
            currentRotation, firstBootRotation, cutoutTop, cutoutBottom, currentRoundedTop = 0,
            cutoutLeft, cutoutRight, softKeyboardHeight = 0, currentTopCutoutHeight = 0;
    private String navBarPosition = "b";
    private int[] totalMargins = new int[4];
    private boolean immersiveMode, ignoreCutouts, navBarKeepSpace,
            showStatus, showStatusInCutout, showNav, currentTopHasCutout, ignoreRoundedCorners,
            isNavAtBottom, gestureNavigation;
    private final int typeStatusBars = WindowInsetsCompat.Type.statusBars(),
            typeNavBars = WindowInsetsCompat.Type.navigationBars(),
            typeIme = WindowInsetsCompat.Type.ime(),
            typeGestures = WindowInsetsCompat.Type.systemGestures(),
            typeSystemBars = WindowInsetsCompat.Type.systemBars(),
            smallestScreenWidthDp;


    // This is called once when the class is instantiated.
    public WindowFlags(Context c, Window w) {
        this.w = w;
        mainActivityInterface = (MainActivityInterface) c;
        windowInsetsControllerCompat = WindowCompat.getInsetsController(w, w.getDecorView());

        // Make edge to edge
        edgeToEdge();

        // Get the display density, smallest width and current orientation
        density = c.getResources().getDisplayMetrics().density;
        smallestScreenWidthDp = c.getResources().getConfiguration().smallestScreenWidthDp;

        // Get the preferences
        /*boolean defaultKeepNavSpace = false;
        try {
            @SuppressLint("DiscouragedApi") int resourceId = c.getResources().getIdentifier("config_navBarInteractionMode", "integer", "android");
            if (resourceId > 0) {
                // Tries to detect gesture navigation, but doesn't always work
                if (c.getResources().getInteger(resourceId) == 2) {
                    gestureNavigation = false;
                    defaultKeepNavSpace = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        navBarKeepSpace = mainActivityInterface.getPreferences().getMyPreferenceBoolean("navBarKeepSpace", false);
        customMarginLeft = mainActivityInterface.getPreferences().getMyPreferenceInt("marginLeft", 0);
        customMarginRight = mainActivityInterface.getPreferences().getMyPreferenceInt("marginRight", 0);
        customMarginTop = mainActivityInterface.getPreferences().getMyPreferenceInt("marginTop", 0);
        customMarginBottom = mainActivityInterface.getPreferences().getMyPreferenceInt("marginBottom", 0);
        immersiveMode = mainActivityInterface.getPreferences().getMyPreferenceBoolean("immersiveMode", true);
        ignoreCutouts = mainActivityInterface.getPreferences().getMyPreferenceBoolean("ignoreCutouts", false);
        ignoreRoundedCorners = mainActivityInterface.getPreferences().getMyPreferenceBoolean("ignoreRoundedCorners",true);
        marginToolbarLeft = mainActivityInterface.getPreferences().getMyPreferenceInt("marginToolbarLeft",0);
        marginToolbarRight = mainActivityInterface.getPreferences().getMyPreferenceInt("marginToolbarRight",0);
        gestureNavigation = mainActivityInterface.getPreferences().getMyPreferenceBoolean("gestureNavigation",false);
    }

    // Initialise the WindowInsetsCompat from MainActivity (once it is ready)
    public void setInsetsCompat(WindowInsetsCompat insetsCompat) {
        // This is sent from the main activity when the window is created and accessible
        this.insetsCompat = insetsCompat;
        firstBootRotation = w.getDecorView().getDisplay().getRotation();
        displayCutoutCompat = insetsCompat.getDisplayCutout();
        systemGestures = insetsCompat.getInsetsIgnoringVisibility(typeGestures);
        mandatoryInset = insetsCompat.getInsets(WindowInsetsCompat.Type.mandatorySystemGestures());
        navBars = insetsCompat.getInsetsIgnoringVisibility(typeNavBars);
        statusBars = insetsCompat.getInsetsIgnoringVisibility(typeStatusBars);
        systemBars = insetsCompat.getInsetsIgnoringVisibility(typeSystemBars);

        // Set the defaults that don't change on rotation/actions
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

    public void edgeToEdge() {
        // This sets the app as edge to edge (better than fullscreen)
        //WindowCompat.setDecorFitsSystemWindows(w, false);
        /*w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            w.setStatusBarColor(Color.TRANSPARENT);
            w.setNavigationBarColor(Color.TRANSPARENT);
        } else {
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
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
        if (gestureNavigation) {
            // Set this to 0.  Users change the bottom padding if required
            navHeight = 0;
        } else {
            navHeight = Math.max(navBars.bottom, Math.max(navBars.left, navBars.right));
        }
    }

    public void setGestureNavigation(boolean gestureNavigation) {
        this.gestureNavigation = gestureNavigation;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("gestureNavigation",gestureNavigation);
    }
    public boolean getGestureNavigation() {
        return gestureNavigation;
    }

    // Set and hold a reference to the soft keyboard height
    // This is set from the mainActivity and is used to move views up
    public void setSoftKeyboardHeight(int softKeyboardHeight) {
        this.softKeyboardHeight = softKeyboardHeight;
        Log.d(TAG,"keyboardHeight:"+softKeyboardHeight);
    }

    public int getSoftKeyboardHeight() {
        return softKeyboardHeight;
    }

    public void hideKeyboard() {
        // Delay a few millisecs and then hide
        if (insetsCompat!=null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> windowInsetsControllerCompat.hide(typeIme), 500);
        }
    }

    public void showKeyboard() {
        // Show after a few millisecs
        if (!insetsCompat.isVisible(WindowInsetsCompat.Type.ime())) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> windowInsetsControllerCompat.show(typeIme), 1000);
        }
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
            windowInsetsControllerCompat.show(typeStatusBars);
        } else {
            windowInsetsControllerCompat.hide(typeStatusBars);
            windowInsetsControllerCompat.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }

        if (showNav) {
            windowInsetsControllerCompat.show(typeNavBars);
        } else {
            windowInsetsControllerCompat.hide(typeNavBars);
            windowInsetsControllerCompat.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }

        if (!showNav && !showStatus && !showStatusInCutout) {
            windowInsetsControllerCompat.hide(typeSystemBars);
            windowInsetsControllerCompat.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
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

        roundedLeft = (int)(roundedLeft - (roundedLeft * Math.sin(Math.toRadians(45))));
        roundedRight = (int)(roundedRight - (roundedRight*Math.sin(Math.toRadians(45))));
        roundedTop = (int)(roundedTop - (roundedTop*Math.sin(Math.toRadians(45))));
        roundedBottom = (int)(roundedBottom - (roundedBottom*Math.sin(Math.toRadians(45))));

    }

    public int getCurrentRoundedTop() {
        return currentRoundedTop;
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
                currentRoundedTop = roundedTop;
                break;

            case 1:
                currentTopHasCutout = cutoutLeft > 0;
                currentTopCutoutHeight = cutoutLeft;
                currentRoundedTop = roundedLeft;
                break;

            case 2:
                currentTopHasCutout = cutoutBottom > 0;
                currentTopCutoutHeight = cutoutBottom;
                currentRoundedTop = roundedBottom;
                break;

            case 3:
                currentTopHasCutout = cutoutRight > 0;
                currentTopCutoutHeight = cutoutRight;
                currentRoundedTop = roundedRight;
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

    public int getMarginToolbarLeft() {
        return marginToolbarLeft;
    }
    public void setMarginToolbarLeft(int marginToolbarLeft, boolean doSave) {
        this.marginToolbarLeft = marginToolbarLeft;
        if (doSave) {
            mainActivityInterface.getPreferences().setMyPreferenceInt("marginToolbarLeft", marginToolbarLeft);
        }
    }
    public int getMarginToolbarRight() {
        return marginToolbarRight;
    }
    public void setMarginToolbarRight(int marginToolbarRight, boolean doSave) {
        this.marginToolbarRight = marginToolbarRight;
        if (doSave) {
            mainActivityInterface.getPreferences().setMyPreferenceInt("marginToolbarRight", marginToolbarRight);
        }
    }

    // Get the margins to adjust the drawerlayout
    // The margins are based on the current rotation which might be non-default!
    // They are only measured on first run and if there is a change
    public void setMargins() {
        int marginL, marginR, marginT, marginB;
        int cutoutL, cutoutR, cutoutT, cutoutB;
        int roundL, roundR, roundT, roundB;

        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("ignoreRoundedCorners",true)) {
            roundL = 0;
            roundR = 0;
            roundB = 0;
            roundT = 0;
        } else {
            roundL = roundedLeft;
            roundR = roundedRight;
            roundB = roundedBottom;
            roundT = roundedTop;
        }

        if (getHasCutouts() && !ignoreCutouts) {
            // The status bar deals with the top rounded corner
            //roundedTop = 0;
            roundT = 0;
        }

        switch (currentRotation) {
            case 0: // 0 degrees.
            default:
                marginL = customMarginLeft + roundL;
                cutoutL = cutoutLeft;
                marginR = customMarginRight + roundR;
                cutoutR = cutoutRight;
                marginT = customMarginTop + roundT;
                cutoutT = cutoutTop;
                marginB = customMarginBottom + roundB; // + nav;
                cutoutB = cutoutBottom;
                break;
            case 1: // 270 degrees (one step anticlockwise)
                marginL = customMarginTop + roundT;
                cutoutL = cutoutTop;
                marginR = customMarginBottom + roundB; // + nav;
                cutoutR = cutoutBottom;
                marginT = customMarginRight + roundR;
                cutoutT = cutoutRight;
                marginB = customMarginLeft + roundL;
                cutoutB = cutoutLeft;
                break;
            case 2: // 180 degrees (two steps anticlockwise/clockwise)
                marginL = customMarginRight + roundR;
                cutoutL = cutoutRight;
                marginR = customMarginLeft + roundL;
                cutoutR = cutoutLeft;
                marginT = customMarginBottom + roundB;
                cutoutT = cutoutBottom;
                marginB = customMarginTop + roundT; // + nav;
                cutoutB = cutoutTop;
                break;
            case 3: // 90 degrees (three steps anticlockwise or 1 step clockwise)
                marginL = customMarginBottom + roundB; // + nav;
                cutoutL = cutoutBottom;
                marginR = customMarginTop + roundT;
                cutoutR = cutoutTop;
                marginT = customMarginLeft + roundL;
                cutoutT = cutoutLeft;
                marginB = customMarginRight + roundR;
                cutoutB = cutoutRight;
                break;
        }

        // Add the nav height to the correct margin
        int nav = 0;
        if (showNav) {
            if (gestureNavigation) {
                // Set this to 0.  Users change the bottom padding if required
                navHeight = 0;
            }
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

    public boolean getHasRoundedCorners() {
        return roundedLeft>0 || roundedRight>0 || roundedBottom>0 || roundedTop>0;
    }

    public boolean getIgnoreRoundedCorners() {
        return ignoreRoundedCorners;
    }

    public void setIgnoreRoundedCorners(boolean ignoreRoundedCorners) {
        this.ignoreRoundedCorners = ignoreRoundedCorners;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("ignoreRoundedCorners",ignoreRoundedCorners);
    }

    public boolean isKeyboardShowing() {
        return insetsCompat.isVisible(WindowInsetsCompat.Type.ime());
    }
}