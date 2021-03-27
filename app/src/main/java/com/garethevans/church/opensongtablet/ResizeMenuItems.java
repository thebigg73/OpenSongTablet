/*
package com.garethevans.church.opensongtablet;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

class ResizeMenuItems {


    */
/*
    In the menu java files, add this function:
    "" = normal text size, L=larger text (for popup titles), S=smaller text (for info text)
    the boolean is if the view is to be capitalised

    private void updateFontSizes() {
        float menuFontSize = preferences.getMyPreferenceFloat(getActivity(),"songMenuAlphaIndexSize",14.0f);
        ResizeMenuItems resizeMenuItems = new ResizeMenuItems();
        resizeMenuItems.updateTextViewSize(VIEWID,menuFontSize,"",false);
        resizeMenuItems.updateButtonTextSize(VIEWID,menuFontSize,"",false);
        resizeMenuItems.updateSwitchTextSize(VIEWID,menuFontSize,"",false);
        resizeMenuItems.updateEditTextSize(VIEWID,menuFontSize,"",false);
     }

     After the intialiseViews();  call this functions:

     // Update the text size to user preference
     updateFontSizes();

    *//*


    void updateTextViewSize(TextView tv, float defaultsize, String type, boolean allCaps) {
        try {
            float size = defaultsize;
            switch (type) {
                case "T":
                case "t":
                    size = defaultsize - 4.0f;
                    break;
                case "S":
                case "s":
                    size = defaultsize - 2.0f;
                    break;
                case "L":
                case "l":
                    size = defaultsize + 2.0f;
                    break;
            }
            tv.setTextSize(size);
            tv.setAllCaps(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void updatEditTextSize(EditText tv, float defaultsize, String type, boolean allCaps) {
        try {
            float size = defaultsize;
            switch (type) {
                case "S":
                case "s":
                    size = defaultsize - 2.0f;
                    break;
                case "L":
                case "l":
                    size = defaultsize + 2.0f;
                    break;
            }
            tv.setTextSize(size);
            tv.setAllCaps(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void updateButtonTextSize(Button bv, float defaultsize, String type, boolean allCaps) {
        try {
            float size = defaultsize;
            switch (type) {
                case "T":
                case "t":
                    size = defaultsize - 4.0f;
                    break;
                case "S":
                case "s":
                    size = defaultsize - 2.0f;
                    break;
                case "L":
                case "l":
                    size = defaultsize + 2.0f;
                    break;
            }
            bv.setTextSize(size);
            bv.setAllCaps(allCaps);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void updateSwitchTextSize(SwitchCompat sw, float defaultsize, String type, boolean allCaps) {
        try {
            float size = defaultsize;
            switch (type) {
                case "T":
                case "t":
                    size = defaultsize - 4.0f;
                    break;
                case "S":
                case "s":
                    size = defaultsize - 2.0f;
                    break;
                case "L":
                case "l":
                    size = defaultsize + 2.0f;
                    break;
            }
            sw.setTextSize(size);
            sw.setAllCaps(allCaps);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}*/
