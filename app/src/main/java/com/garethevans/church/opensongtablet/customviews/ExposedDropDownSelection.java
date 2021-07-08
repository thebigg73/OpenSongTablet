package com.garethevans.church.opensongtablet.customviews;

import android.view.View;

import com.garethevans.church.opensongtablet.R;

import java.util.ArrayList;
import java.util.Collections;

public class ExposedDropDownSelection {

    /*
    This class is used to scroll to the currently selected position in an
    exposeddropdownlist - actually an autocomplete textview.
    We need to receive the selected text, a reference to the view and
    the array that the dropdown list is constructed from.
    We then set this selection.  It is done via an onclick listener
    The text selected is dealt with in the appropriate fragment via a textwatcher
     */

    public void keepSelectionPosition(ExposedDropDown exposedDropDown, ArrayList<String> arrayList) {
        // Deal with the arrow
        exposedDropDown.getTextInputLayout().setEndIconOnClickListener(new View.OnClickListener() {
            boolean isShowing = false;

            @Override
            public void onClick(View v) {
                isShowing = listenerAction(exposedDropDown, arrayList, isShowing);
            }
        });
        // Deal with the rest of the dropdown clickable area by making it click the end icon
        exposedDropDown.getAutoCompleteTextView().setOnClickListener(v -> exposedDropDown.getTextInputLayout().findViewById(R.id.text_input_end_icon).performClick());
    }

    public void keepSelectionPosition(ExposedDropDown exposedDropDown, String[] stringArray) {
        // Deal with the arrow
        exposedDropDown.getTextInputLayout().setEndIconOnClickListener(new View.OnClickListener() {
            boolean isShowing = false;

            @Override
            public void onClick(View v) {
                isShowing = listenerAction(exposedDropDown, stringArray, isShowing);
            }
        });
        // Deal with the rest of the dropdown clickable area by making it click the end icon
        exposedDropDown.getAutoCompleteTextView().setOnClickListener(v -> exposedDropDown.getTextInputLayout().findViewById(R.id.text_input_end_icon).performClick());
    }

    private boolean listenerAction(ExposedDropDown exposedDropDown, ArrayList<String> arrayList, boolean isShowing) {
        // isShowing seems to be the opposite for the dropdown arrow and the clickable area
        // This is due to what is focused I think
        if (isShowing) {
            exposedDropDown.getAutoCompleteTextView().dismissDropDown();
        } else {
            exposedDropDown.getAutoCompleteTextView().showDropDown();
            exposedDropDown.getAutoCompleteTextView().setListSelection(getPositionInArray(getSelectedText(exposedDropDown), arrayList));
        }
        return !isShowing;
    }
    private boolean listenerAction(ExposedDropDown exposedDropDown, String[] stringArray, boolean isShowing) {
        // isShowing seems to be the opposite for the dropdown arrow and the clickable area
        // This is due to what is focused I think
        if (isShowing) {
            exposedDropDown.getAutoCompleteTextView().dismissDropDown();
        } else {
            exposedDropDown.getAutoCompleteTextView().showDropDown();
            exposedDropDown.getAutoCompleteTextView().setListSelection(getPositionInArray(getSelectedText(exposedDropDown), stringArray));
        }
        return !isShowing;
    }

    private String getSelectedText(ExposedDropDown exposedDropDown) {
        if (exposedDropDown.getAutoCompleteTextView()!=null && exposedDropDown.getAutoCompleteTextView().getText()!=null) {
            return exposedDropDown.getAutoCompleteTextView().getText().toString();
        } else {
            return "";
        }
    }
    private int getPositionInArray(String string, ArrayList<String> arrayList) {
        return arrayList.indexOf(string);
    }
    private int getPositionInArray(String string, String[] stringArray) {
        ArrayList<String> arrayList = new ArrayList<>();
        Collections.addAll(arrayList, stringArray);
        return arrayList.indexOf(string);
    }
}
