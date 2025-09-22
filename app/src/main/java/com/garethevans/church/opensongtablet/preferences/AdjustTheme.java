package com.garethevans.church.opensongtablet.preferences;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyMaterialEditText;
import com.garethevans.church.opensongtablet.customviews.MyMaterialTextView;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

public class AdjustTheme {

    // This adjusts the colours used in the views
    private final String TAG = "AdjustTheme";
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private int textColor;
    private int hintColor;
    private int primaryColor;
    private int primaryAltColor;
    private int secondaryColor;
    private int secondaryAltColor;

    public AdjustTheme(Context c) {
        this.c = c;
        this.mainActivityInterface = (MainActivityInterface) c;
        setThemeColors();
    }

    public void setThemeColors() {
        String themePref = mainActivityInterface.getPreferences().getMyPreferenceString("appTheme","dark");
        Log.d(TAG,"themePref:"+themePref);
        if (themePref!=null) {
            switch (themePref) {
                case "light":
                case "custom2":
                    textColor = ContextCompat.getColor(c, R.color.black);
                    hintColor = ContextCompat.getColor(c, R.color.grey);
                    primaryColor = ContextCompat.getColor(c, R.color.light_primary);
                    primaryAltColor = ContextCompat.getColor(c, R.color.light_primary);
                    secondaryColor = ContextCompat.getColor(c, R.color.light_secondary);
                    secondaryAltColor = ContextCompat.getColor(c, R.color.light_secondary_variant);
                    break;

                case "dark":
                case "custom1":
                default:
                    textColor = ContextCompat.getColor(c, R.color.white);
                    hintColor = ContextCompat.getColor(c, R.color.vlightgrey);
                    primaryColor = ContextCompat.getColor(c, R.color.dark_primary);
                    primaryAltColor = ContextCompat.getColor(c, R.color.dark_primary);
                    secondaryColor = ContextCompat.getColor(c, R.color.dark_secondary);
                    secondaryAltColor = ContextCompat.getColor(c, R.color.dark_secondary_variant);
                    break;
            }
        }
    }

    public void applyColorsToViews(View root) {
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            group.setBackgroundColor(primaryColor);
            for (int i = 0; i < group.getChildCount(); i++) {
                applyColorsToViews(group.getChildAt(i));
            }
        }

        boolean dealtWith = false;

        if (root!=null) {
            if (root instanceof AppCompatImageView) {
                ((AppCompatImageView) root).setColorFilter(textColor, PorterDuff.Mode.SRC_IN);
                dealtWith = true;
            }

            if (root instanceof MaterialCheckBox) {
                ((MaterialCheckBox) root).setTextColor(textColor);
                ((MaterialCheckBox) root).setButtonIconTintList(ColorStateList.valueOf(textColor));
                dealtWith = true;
            }

            if (root instanceof RecyclerView) {
                root.setBackgroundColor(primaryColor);
                dealtWith = true;
            }

            if (root instanceof TextInputEditText) {
                ((TextInputEditText) root).setTextColor(textColor);
                dealtWith = true;
            }

            if (root instanceof AppCompatTextView) {
                ((AppCompatTextView) root).setTextColor(textColor);
                dealtWith = true;
            }

            if (root instanceof MaterialAutoCompleteTextView) {
                ((MaterialAutoCompleteTextView) root).setTextColor(textColor);
                dealtWith = true;
            }

            if (root instanceof MyMaterialTextView) {
                //((MyMaterialTextView) root).setTextColor(textColor);
                //((MyMaterialTextView) root).setHintColor(hintColor);
                dealtWith = true;
            }

            if (root instanceof MaterialTextView) {
                ((MaterialTextView) root).setTextColor(textColor);
                dealtWith = true;

            }

            if (root instanceof MyMaterialEditText) {
                ((MyMaterialEditText) root).setTextColor(textColor);
                ((MyMaterialEditText) root).setHintColor(hintColor);
                dealtWith = true;
            }

            if (root instanceof FloatingActionButton) {
                ((FloatingActionButton) root).setColorFilter(secondaryColor, PorterDuff.Mode.SRC_IN);
                dealtWith = true;
            }

            if (root instanceof MaterialButton) {
                ((MaterialButton) root).setTextColor(textColor);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    root.setBackgroundTintList(ColorStateList.valueOf(secondaryColor));
                } else {
                    root.setBackgroundColor(secondaryColor);
                }
                dealtWith = true;
            }

            if (root instanceof LinearLayout || root instanceof FrameLayout || root instanceof RelativeLayout) {
                root.setBackgroundColor(primaryColor);
                dealtWith = true;
            }

        }

        if (!dealtWith) {
            Log.d(TAG,"still to deal with root:"+root);
        }
        // You can add more special cases here (EditText, CheckBox, etc.)
    }

}
