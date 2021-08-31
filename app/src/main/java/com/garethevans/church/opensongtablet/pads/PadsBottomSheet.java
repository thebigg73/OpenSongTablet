package com.garethevans.church.opensongtablet.pads;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.BottomSheetPadsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class PadsBottomSheet extends BottomSheetDialogFragment {

    private MainActivityInterface mainActivityInterface;
    private BottomSheetPadsBinding myView;
    private final String TAG = "PadsBottomSheet";
    ActivityResultLauncher<Intent> activityResultLauncher;
    private boolean padPlaying;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
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
        myView = BottomSheetPadsBinding.inflate(inflater, container, false);

        myView.dialogHeading.setClose(this);

        // Set up values
        setupValues();

        // Initialise launcher
        initialiseLauncher();

        // The stop/play button
        updateStartStopButton();

        // Set up listeners
        setupListeners();

        return myView.getRoot();
    }

    private void setupValues() {
        // The key
        ExposedDropDownArrayAdapter keyArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),
                myView.padKey, R.layout.view_exposed_dropdown_item, getResources().getStringArray(R.array.key_choice));
        myView.padKey.setAdapter(keyArrayAdapter);
        myView.padKey.setText(mainActivityInterface.getSong().getKey());

        // The pad file
        ArrayList<String> padfiles = new ArrayList<>();
        padfiles.add(getString(R.string.pad_auto));
        padfiles.add(getString(R.string.link_audio));
        padfiles.add(getString(R.string.off));
        ExposedDropDownArrayAdapter padArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),
                myView.padType, R.layout.view_exposed_dropdown_item, padfiles);
        myView.padType.setAdapter(padArrayAdapter);
        if (mainActivityInterface.getSong().getPadfile() == null ||
                mainActivityInterface.getSong().getPadfile().isEmpty() ||
        mainActivityInterface.getSong().getPadfile().equals(getString(R.string.pad_auto))) {
            mainActivityInterface.getSong().setPadfile("auto");
        }
        myView.padType.setText(niceTextFromPref(mainActivityInterface.getSong().getPadfile()));
        myView.padLinkAudio.setFocusable(false);
        myView.padLinkAudio.setText(mainActivityInterface.getSong().getLinkaudio());
        showOrHideLink();

        // The loop
        myView.padLoop.setChecked(mainActivityInterface.getSong().getPadloop()!=null && mainActivityInterface.getSong().getPadloop().equals("true"));

        padPlaying = mainActivityInterface.getPad().isPadPlaying();
    }

    private void initialiseLauncher() {
        // Initialise the launcher
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Log.d(TAG,"resultCode()="+result.getResultCode());
            if (result.getResultCode() == Activity.RESULT_OK) {
                try {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri contentUri = data.getData();

                        // If this is a localised (i.e. inside OpenSong folder), we don't need to take the permissions
                        // There is a limit of 128-512 permissions allowed (depending on Android version).
                        String localisedUri = mainActivityInterface.getStorageAccess().fixUriToLocal(contentUri);
                        if (!localisedUri.contains("../OpenSong/")) {
                            ContentResolver resolver = requireActivity().getContentResolver();
                            resolver.takePersistableUriPermission(contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                        myView.padLinkAudio.setText(mainActivityInterface.getStorageAccess().fixUriToLocal(contentUri));
                    }
                } catch (Exception e) {
                    // Link threw an error (likely invalid)
                    mainActivityInterface.getShowToast().doIt(requireContext(),getString(R.string.link_error));
                    myView.padLinkAudio.requestFocus();
                    e.printStackTrace();
                }
            }
        });
    }
    private void updateStartStopButton() {
        // Rather than query the pads each time, use the local boolean padPlaying (set above)
        // This is because there is often a delay starting the pad and it being registered
        if (padPlaying) {
           // The action is to stop
            Log.d(TAG,"Set stop icon");
            myView.startStopPad.setIcon(ResourcesCompat.getDrawable(requireContext().getResources(),R.drawable.ic_stop_white_36dp, requireContext().getTheme()));
            myView.startStopPad.setText(getString(R.string.stop));
            myView.startStopPad.setOnClickListener(v -> {
                padPlaying = false;
                mainActivityInterface.getPad().stopPad(requireContext());
                // Rerun this script to get the new icon and listener
                updateStartStopButton();
                // Check again in 2 seconds just in case the pad had an error
                myView.startStopPad.postDelayed(() -> {
                    padPlaying = mainActivityInterface.getPad().isPadPlaying();
                    updateStartStopButton();
                },2000);
           });
        } else {
            // The action is to play
            Log.d(TAG,"Set start icon");
            myView.startStopPad.setIcon(ResourcesCompat.getDrawable(requireContext().getResources(),R.drawable.ic_play_white_36dp, requireContext().getTheme()));
            myView.startStopPad.setText(getString(R.string.start));
            myView.startStopPad.setOnClickListener(v -> {
                padPlaying = true;
                mainActivityInterface.getPad().startPad(requireContext());
                // Rerun this script
                updateStartStopButton();
                // Check again in 2 seconds just in case the pad had an error
                myView.startStopPad.postDelayed(() -> {
                    padPlaying = mainActivityInterface.getPad().isPadPlaying();
                    updateStartStopButton();
                },2000);
            });
        }
    }

    private void setupListeners() {
        myView.padKey.addTextChangedListener(new MyTextWatcher("padKey"));
        myView.padType.addTextChangedListener(new MyTextWatcher("padType"));
        myView.padLinkAudio.addTextChangedListener(new MyTextWatcher("padLink"));
        myView.padLoop.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getSong().setPadloop(""+b);
            mainActivityInterface.getSaveSong().updateSong(requireContext(),mainActivityInterface);
        });
        myView.padSettings.setOnClickListener(view -> {
            mainActivityInterface.navigateToFragment("opensongapp://settings/pads",0);
            dismiss();
        });
        myView.padPanic.setOnClickListener(v -> {
            padPlaying = false;
            mainActivityInterface.getPad().panicStop();
            mainActivityInterface.getShowToast().doIt(requireContext(),getString(R.string.panic_stop));
            updateStartStopButton();
        });
        myView.padLinkAudio.setOnClickListener(v -> {
            myView.padLinkAudio.setText(""); // Trigger a reset that is saved
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("audio/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            activityResultLauncher.launch(intent);
        });
    }

    private void showOrHideLink() {
        if (myView.padType.getText()!=null && myView.padType.getText().toString().equals(getString(R.string.link_audio))) {
            myView.padLinkAudio.setVisibility(View.VISIBLE);
        } else {
            myView.padLinkAudio.setVisibility(View.GONE);
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private final String which;

        MyTextWatcher(String which) {
            this.which = which;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void afterTextChanged(Editable editable) {
            switch (which) {
                case "padKey":
                    mainActivityInterface.getSong().setKey(editable.toString());
                    break;
                case "padType":
                    mainActivityInterface.getSong().setPadfile(prefFromNiceText(editable.toString()));
                    showOrHideLink();
                    break;
                case "padLink":
                    mainActivityInterface.getSong().setLinkaudio(editable.toString());
                    break;
            }
            mainActivityInterface.getSaveSong().updateSong(requireContext(), mainActivityInterface);
            Log.d(TAG,"Song saved");
        }
    }

    private String niceTextFromPref(String padfile) {
        switch (padfile) {
            case "auto":
            default:
                return getString(R.string.pad_auto);
            case "link":
                return getString(R.string.link_audio);
            case "off":
                return getString(R.string.off);
        }
    }

    private String prefFromNiceText(String text) {
        if (text.equals(getString(R.string.link_audio))) {
            return "link";
        } else if (text.equals(getString(R.string.off))) {
            return "off";
        } else {
            return "auto";
        }
    }
}

