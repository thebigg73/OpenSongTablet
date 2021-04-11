package com.garethevans.church.opensongtablet.appdata;

import android.view.View;
import android.widget.AutoCompleteTextView;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class ExposedDropDownSelection {

    /*
    This class is used to scroll to the currently selected position in an
    exposeddropdownlist - actually an autocomplete textview.
    We need to receive the selected text, a reference to the view and
    the array that the dropdown list is constructed from.
    We then set this selection.  It is done via an onclick listener
    The text selected is dealt with in the appropriate fragment via a textwatcher
     */

    public void keepSelectionPosition(TextInputLayout textInputLayout, AutoCompleteTextView autoCompleteTextView, ArrayList<String> arrayList) {
        // Deal with the arrow
        textInputLayout.setEndIconOnClickListener(new View.OnClickListener() {
            boolean isShowing = false;

            @Override
            public void onClick(View v) {
                isShowing = listenerAction(autoCompleteTextView, arrayList, isShowing);
            }
        });
        // Deal with the rest of the dropdown clickable area by making it click the end icon
        autoCompleteTextView.setOnClickListener(v -> textInputLayout.findViewById(R.id.text_input_end_icon).performClick());
    }


    private boolean listenerAction(AutoCompleteTextView autoCompleteTextView, ArrayList<String> arrayList, boolean isShowing) {
        // isShowing seems to be the opposite for the dropdown arrow and the clickable area
        // This is due to what is focused I think
        if (isShowing) {
            autoCompleteTextView.dismissDropDown();
        } else {
            autoCompleteTextView.showDropDown();
            autoCompleteTextView.setListSelection(getPositionInArray(getSelectedText(autoCompleteTextView), arrayList));
        }
        return !isShowing;
    }

    private String getSelectedText(AutoCompleteTextView autoCompleteTextView) {
        if (autoCompleteTextView!=null && autoCompleteTextView.getText()!=null) {
            return autoCompleteTextView.getText().toString();
        } else {
            return "";
        }
    }
    private int getPositionInArray(String string, ArrayList<String> arrayList) {
        return arrayList.indexOf(string);
    }
}
