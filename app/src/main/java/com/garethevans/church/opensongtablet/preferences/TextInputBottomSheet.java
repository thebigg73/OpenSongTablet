package com.garethevans.church.opensongtablet.preferences;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.BottomSheetTextInputBinding;
import com.garethevans.church.opensongtablet.interfaces.DialogReturnInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class TextInputBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "TextInputBottomSheet";
    private final Fragment fragment;
    private final String fragname;
    private final String title;
    private final String hint;
    private final String prefName;
    private String prefVal;
    private ArrayList<String> prefChoices;
    private final boolean simpleEditText, singleLine;

    public DialogReturnInterface dialogReturnInterface;
    private BottomSheetTextInputBinding myView;
    private MainActivityInterface mainActivityInterface;

    public TextInputBottomSheet(Fragment fragment, String fragname, String title, String hint,
                                String prefName, String prefVal, boolean singleLine) {
        this.fragment = fragment;
        this.fragname = fragname;
        this.title = title;
        this.hint = hint;
        this.prefName = prefName;
        this.prefVal = prefVal;
        this.singleLine = singleLine;
        simpleEditText = true;
    }

    public TextInputBottomSheet(Fragment fragment, String fragname,
                                String title, String hint, String prefName, String prefVal,
                                ArrayList<String> prefChoices) {
        this.fragment = fragment;
        this.fragname = fragname;
        this.title = title;
        this.hint = hint;
        this.prefName = prefName;
        this.prefVal = prefVal;
        this.prefChoices = prefChoices;
        simpleEditText = false;
        singleLine = false;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            FrameLayout bottomSheet = ((BottomSheetDialog) dialog1).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        dialogReturnInterface = (DialogReturnInterface) context;
     }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetTextInputBinding.inflate(inflater,container,false);

        myView.dialogHeading.setText(title);
        myView.dialogHeading.closeAction(this);

        // Set the views
        setViews();

        // Set the listeners
        setListeners();

        return myView.getRoot();
    }

    private void setViews() {

        if (simpleEditText) {
            // Hide the unwanted views
            myView.textValues.setVisibility(View.GONE);

            // Set the current values
            myView.prefEditText.setText(prefVal);
            if (singleLine) {
                myView.prefEditText.setLines(1);
                myView.prefEditText.setMaxLines(1);
                myView.prefEditText.setOnEditorActionListener((v, actionId, event) -> {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                            actionId == EditorInfo.IME_ACTION_DONE) {
                        myView.okButton.performClick();
                    }
                    return false;
                });
            }
            ((TextInputLayout)myView.prefEditText.findViewById(R.id.holderLayout)).setHint(hint);

        } else {
            ExposedDropDownArrayAdapter arrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),R.layout.view_exposed_dropdown_item,prefChoices);
            myView.textValues.setAdapter(arrayAdapter);
            myView.textValues.setText(prefVal);
            myView.textValues.setHint(hint);
        }
    }

    private void setListeners() {
        myView.okButton.setOnClickListener(v -> {
            // Grab the new value
            if (simpleEditText) {
                prefVal = myView.prefEditText.getText().toString();
            } else {
                prefVal = myView.textValues.getText().toString();
            }

            // Save the preference if we didn't send null as the prefName
            if (prefName!=null) {
                mainActivityInterface.getPreferences().setMyPreferenceString(getContext(), prefName, prefVal);
            }

            // Update the calling fragment
            Log.d(TAG, "fragname: "+fragname);
            dialogReturnInterface.updateValue(fragment,fragname,prefName,prefVal);
            dismiss();
        });
        myView.prefEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // The user has clicked Enter/Done, so the keyboard has closed
                // Click on the ok button to save it
                myView.okButton.performClick();
            }
            return false;
        });
    }
}