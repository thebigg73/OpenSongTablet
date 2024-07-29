package com.garethevans.church.opensongtablet.preferences;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.BottomSheetTextInputBinding;
import com.garethevans.church.opensongtablet.interfaces.DialogReturnInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class TextInputBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "TextInputBottomSheet";
    private final Fragment fragment;
    private final String fragname;
    private final String title;
    private final String hint;
    private final String prefName;
    private String prefVal;
    private final String extra;
    private ArrayList<String> prefChoices;
    private final boolean simpleEditText, singleLine;

    public DialogReturnInterface dialogReturnInterface;
    private BottomSheetTextInputBinding myView;
    private MainActivityInterface mainActivityInterface;

    public TextInputBottomSheet() {
        // Default constructor required to avoid re-instantiation failures
        // Just close the bottom sheet
        fragment = null;
        fragname = "";
        title = "";
        hint = "";
        extra = "";
        prefName = "";
        prefVal = "";
        singleLine = true;
        simpleEditText = true;
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TextInputBottomSheet(Fragment fragment, String fragname, String title, String hint,
                                String extra, String prefName, String prefVal, boolean singleLine) {
        this.fragment = fragment;
        this.fragname = fragname;
        this.title = title;
        this.hint = hint;
        this.prefName = prefName;
        this.prefVal = prefVal;
        this.singleLine = singleLine;
        this.extra = extra;
        simpleEditText = true;
    }

    public TextInputBottomSheet(Fragment fragment, String fragname, String title, String hint,
                                String extra, String prefName, String prefVal, ArrayList<String> prefChoices) {
        this.fragment = fragment;
        this.fragname = fragname;
        this.title = title;
        this.hint = hint;
        this.prefName = prefName;
        this.prefVal = prefVal;
        this.prefChoices = prefChoices;
        this.extra = extra;
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
                BottomSheetBehavior.from(bottomSheet).setDraggable(false);
            }
            setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenBottomSheet);
            mainActivityInterface.getMainHandler().postDelayed(() -> dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE),2000);
        });
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        dialogReturnInterface = (DialogReturnInterface) context;
     }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = null;
        if (getActivity()!=null) {
            w = getActivity().getWindow();
        }
        if (w!=null) {
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetTextInputBinding.inflate(inflater,container,false);

        myView.dialogHeading.setText(title);

        // Initialise the 'close' floatingactionbutton
        myView.dialogHeading.setClose(this);

        // Set the views
        setViews();

        // Set the listeners
        setListeners();

        return myView.getRoot();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        mainActivityInterface.getWindowFlags().hideKeyboard();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mainActivityInterface.getWindowFlags().hideKeyboard();
    }

    private void setViews() {

        if (extra!=null && !extra.isEmpty()) {
            myView.infoText.setText(extra);
            myView.infoText.setVisibility(View.VISIBLE);
        }

        // For some views, we want monospace text
        if (fragname.equals("SongSectionsFragment")) {
            myView.prefEditText.setTypeface(mainActivityInterface.getMyFonts().getMonoFont());
        }

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
            } else {
                mainActivityInterface.getProcessSong().editBoxToMultiline(myView.prefEditText);
                mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.prefEditText,6);
            }

            myView.prefEditText.setHint(hint);
            myView.prefEditText.requestFocus();

            myView.prefEditText.setGravity(Gravity.START);

        } else {
            // Hide the unwanted views
            myView.prefEditText.setVisibility(View.GONE);
            if (getContext()!=null) {
                ExposedDropDownArrayAdapter arrayAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.textValues, R.layout.view_exposed_dropdown_item, prefChoices);
                myView.textValues.setAdapter(arrayAdapter);
            }
            if (prefVal==null) {
                prefVal = "";
            }
            myView.textValues.setText(prefVal);
            myView.textValues.setHint(hint);
            Log.d(TAG,"hint:"+hint);
            Log.d(TAG,"text:"+prefVal);
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
                mainActivityInterface.getPreferences().setMyPreferenceString(prefName, prefVal);
            }

            if (fragment!=null) {
                // Update the calling fragment
                Log.d(TAG, "fragname: " + fragname);
                dialogReturnInterface.updateValue(fragment, fragname, prefName, prefVal);
            }
            dismiss();
        });

        myView.prefEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // The user has clicked Enter/Done, so the keyboard has closed
                // Click on the okay button to save it
                myView.okButton.performClick();
            }
            return false;
        });
    }
}