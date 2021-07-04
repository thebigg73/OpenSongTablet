package com.garethevans.church.opensongtablet.preferences;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.BottomSheetTextInputBinding;
import com.garethevans.church.opensongtablet.interfaces.DialogReturnInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class TextInputBottomSheet extends BottomSheetDialogFragment {

    private final Fragment fragment;
    private final String fragname;
    private final String title;
    private final String hint;
    private final String prefName;
    private String prefVal;
    private ArrayList<String> prefChoices;
    private final boolean simpleEditText, singleLine;

    private BottomSheetTextInputBinding myView;
    private DialogReturnInterface dialogReturnInterface;
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        dialogReturnInterface = (DialogReturnInterface) context;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetTextInputBinding.inflate(inflater,container,false);
        if (getDialog()!=null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.scrim)));
            getDialog().setCanceledOnTouchOutside(true);
        }

        // Set the views
        setViews();

        // Set the listeners
        setListeners();

        return myView.getRoot();
    }

    private void setViews() {
        ((TextView)myView.dialogHeading.findViewById(R.id.title)).setText(title);

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
            ExposedDropDownArrayAdapter arrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),R.layout.exposed_dropdown,prefChoices);
            myView.textValues.setAdapter(arrayAdapter);
            myView.textValues.setText(prefVal);
            ((TextInputLayout)myView.textValues.findViewById(R.id.textLayout)).setHint(hint);
        }
    }

    private void setListeners() {
        myView.dialogHeading.findViewById(R.id.close).setOnClickListener(v -> dismiss());
        myView.okButton.setOnClickListener(v -> {
            // Grab the new value
            if (simpleEditText) {
                if (myView.prefEditText.getEditText()!=null && myView.prefEditText.getEditText().getText()!=null) {
                    prefVal = myView.prefEditText.getEditText().getText().toString();
                } else {
                    prefVal = "";
                }
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