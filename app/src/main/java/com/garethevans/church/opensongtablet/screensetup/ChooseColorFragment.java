package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.databinding.ThemeColorChooserBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;

import java.util.Map;

public class ChooseColorFragment extends Fragment {

    // viewTarget = myView.target
    // viewSatVal = myView.colorChooser
    // viewContainer = myView.viewContainer
    // viewNewColor = myView.newColor

    MainActivityInterface mainActivityInterface;
    ThemeColorChooserBinding myView;
    Preferences preferences;
    ThemeColors themeColors;
    private boolean supportsAlpha = false;
    private int oldcolor, color, alpha;
    private String hexColor, themePrefix;
    private final float[] currentColorHsv = new float[3], newColorHsv = new float[3];
    private Map<String,Integer> colorMap;

    /*private ChooseColorFragment(int color, boolean supportsAlpha) {
        this.supportsAlpha = supportsAlpha;
        this.color = color;
        if (!supportsAlpha) { // remove alpha if not supported
            color = color | 0xff000000;
        }


    }*/

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = ThemeColorChooserBinding.inflate(inflater,container,false);

        // Set up the helper classes
        setUpHelpers();

        // Set up the listeners
        setListeners();

        // Set up colour
        setupOriginalColor();

        return myView.getRoot();
    }

    private void setUpHelpers() {
        preferences = new Preferences();
        themeColors = new ThemeColors();
    }

    private void setListeners() {
        myView.colorChooser.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_MOVE
                    || event.getAction() == MotionEvent.ACTION_DOWN) {

                float x = event.getX(); // touch event are in dp units.
                float y = event.getY();

                if (x < 0.f) {
                    x = 0.f;
                }
                if (x > myView.colorChooser.getMeasuredWidth()) {
                    x = myView.colorChooser.getMeasuredWidth();
                }
                if (y < 0.f) {
                    y = 0.f;
                }
                if (y > myView.colorChooser.getMeasuredHeight()) {
                    y = myView.colorChooser.getMeasuredHeight();
                }

                setSaturation(1.f / myView.colorChooser.getMeasuredWidth() * x);
                setValue(1.f - (1.f / myView.colorChooser.getMeasuredHeight() * y));

                // update view
                moveTarget();
                myView.newColor.setBackgroundColor(getColor());


                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();
            }
            return false;
        });
    }

    private void setupOriginalColor() {
        themePrefix = preferences.getMyPreferenceString(getContext(),"appTheme","dark");
        // Load the chosen colours up
        colorMap = themeColors.getDefaultColors(getContext(),preferences);

        oldcolor = colorMap.get(StaticVariables.whattodo);
        Color.colorToHSV(oldcolor, currentColorHsv);
        alpha = Color.alpha(oldcolor);
        hexColor = String.format("%06X",(0xFFFFFF & oldcolor));
        myView.hexColor.setText(hexColor);
        myView.oldColor.setBackgroundColor(oldcolor);
        myView.colorChooser.setHue(getHue());
    }

    private void moveTarget() {
        float x = getSaturation() * myView.colorChooser.getMeasuredWidth();
        float y = (1.f - getValue()) * myView.colorChooser.getMeasuredHeight();
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) myView.target.getLayoutParams();
        layoutParams.leftMargin = (int) (myView.colorChooser.getLeft() + x - Math.floor(myView.target.getMeasuredWidth() / 2.0f) - myView.colorContainer.getPaddingLeft());
        layoutParams.topMargin = (int) (myView.colorChooser.getTop() + y - Math.floor(myView.target.getMeasuredHeight() / 2.0f) - myView.colorContainer.getPaddingTop());
        myView.target.setLayoutParams(layoutParams);
    }

    private void setSaturation(float sat) {
        currentColorHsv[1] = sat;
    }
    private float getSaturation() {
        return currentColorHsv[1];
    }

    private int getColor() {
        final int argb = Color.HSVToColor(currentColorHsv);
        hexColor = String.format("%06X", (0xFFFFFF & argb));
        myView.hexColor.setText(hexColor);
        return alpha << 24 | (argb & 0x00ffffff);
    }

    private void setValue(float val) {
        currentColorHsv[2] = val;
    }
    private float getValue() {
        return currentColorHsv[2];
    }

    private float getHue() {
        return currentColorHsv[0];
    }

    private String colorToHSV() {
        Color.colorToHSV(color, currentColorHsv);
        alpha = Color.alpha(color);
        hexColor = String.format("%06X",(0xFFFFFF & color));
        return hexColor;
    }

}
