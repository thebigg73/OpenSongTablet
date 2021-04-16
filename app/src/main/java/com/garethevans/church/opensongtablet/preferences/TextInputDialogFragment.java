package com.garethevans.church.opensongtablet.preferences;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.TextInputDialogBinding;
import com.garethevans.church.opensongtablet.interfaces.DialogReturnInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class TextInputDialogFragment extends DialogFragment {

    private final Fragment fragment;
    private final String fragname;
    private final String title;
    private final String hint;
    private final String prefName;
    private String prefVal;
    private ArrayList<String> prefChoices;
    private final boolean simpleEditText, singleLine;

    private TextInputDialogBinding myView;
    private DialogReturnInterface dialogReturnInterface;
    private MainActivityInterface mainActivityInterface;

    public TextInputDialogFragment(Fragment fragment, String fragname, String title, String hint,
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

    public TextInputDialogFragment(Fragment fragment, String fragname,
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        dialogReturnInterface = (DialogReturnInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = TextInputDialogBinding.inflate(inflater,container,false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Set the views
        setViews();

        // Set the listeners
        setListeners();

        return myView.getRoot();
    }

    private void setViews() {
        myView.dialogTitle.setText(title);

        if (simpleEditText) {
            // Hide the unwanted views
            myView.textLayout.setVisibility(View.GONE);
            myView.textValues.setVisibility(View.GONE);

            // Set the current values
            myView.prefEditText.getEditText().setText(prefVal);
            if (singleLine) {
                myView.prefEditText.getEditText().setLines(1);
                myView.prefEditText.getEditText().setMaxLines(1);
                myView.prefEditText.getEditText().setOnEditorActionListener((v, actionId, event) -> {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                            actionId == EditorInfo.IME_ACTION_DONE) {
                        myView.okButton.performClick();
                    }
                    return false;
                });
            }
            ((TextInputLayout)myView.prefEditText.findViewById(R.id.holderLayout)).setHint(hint);

        } else {
            ExposedDropDownArrayAdapter arrayAdapter = new ExposedDropDownArrayAdapter(getContext(),R.layout.exposed_dropdown,prefChoices);
            myView.textValues.setAdapter(arrayAdapter);
            myView.textValues.setText(prefVal);
            ((TextInputLayout)myView.textValues.findViewById(R.id.textLayout)).setHint(hint);
        }
    }

    private void setListeners() {
        myView.cancelButton.setOnClickListener(v -> dismiss());
        myView.okButton.setOnClickListener(v -> {
            // Grab the new value
            if (simpleEditText) {
                prefVal = myView.prefEditText.getEditText().getText().toString();
            } else {
                prefVal = myView.textValues.getText().toString();
            }

            // Save the preference
            mainActivityInterface.getPreferences().setMyPreferenceString(getContext(),prefName,prefVal);

            // Update the calling fragment
            dialogReturnInterface.updateValue(fragment,fragname,prefName,prefVal);
            dismiss();
        });
    }
}