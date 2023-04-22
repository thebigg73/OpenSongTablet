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
    private String website_pad_string="", pad_auto_string="", link_audio_string="", off_string="",
            link_error_string="", stop_string="", start_string="", deeplink_pads_string="",
            panic_stop_string="";
    private String[] key_choice_string = {};

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

        prepareStrings();

        myView.dialogHeading.setClose(this);
        myView.dialogHeading.setWebHelp(mainActivityInterface,website_pad_string);

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

    private void prepareStrings() {
        if (getContext()!=null) {
            website_pad_string = getString(R.string.website_pad);
            key_choice_string = getResources().getStringArray(R.array.key_choice);
            pad_auto_string = getString(R.string.pad_auto);
            link_audio_string = getString(R.string.link_audio);
            off_string = getString(R.string.off);
            link_error_string = getString(R.string.link_error);
            stop_string = getString(R.string.stop);
            start_string = getString(R.string.start);
            deeplink_pads_string = getString(R.string.deeplink_pads);
            panic_stop_string = getString(R.string.panic_stop);
        }
    }
    private void setupValues() {
        // The key
        if (getContext()!=null) {
            ExposedDropDownArrayAdapter keyArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                    myView.padKey, R.layout.view_exposed_dropdown_item, key_choice_string);
            myView.padKey.setAdapter(keyArrayAdapter);
        }
        myView.padKey.setText(mainActivityInterface.getSong().getKey());

        // The pad file
        ArrayList<String> padfiles = new ArrayList<>();
        padfiles.add(pad_auto_string);
        padfiles.add(link_audio_string);
        padfiles.add(off_string);
        if (getContext()!=null) {
            ExposedDropDownArrayAdapter padArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                    myView.padType, R.layout.view_exposed_dropdown_item, padfiles);
            myView.padType.setAdapter(padArrayAdapter);
        }
        if (mainActivityInterface.getSong().getPadfile() == null ||
                mainActivityInterface.getSong().getPadfile().isEmpty() ||
        mainActivityInterface.getSong().getPadfile().equals(pad_auto_string)) {
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
                        if (!localisedUri.contains("../OpenSong/") && getActivity()!=null) {
                            ContentResolver resolver = getActivity().getContentResolver();
                            resolver.takePersistableUriPermission(contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                        myView.padLinkAudio.setText(mainActivityInterface.getStorageAccess().fixUriToLocal(contentUri));
                    }
                } catch (Exception e) {
                    // Link threw an error (likely invalid)
                    mainActivityInterface.getShowToast().doItBottomSheet(link_error_string,myView.getRoot());
                    myView.padLinkAudio.requestFocus();
                    e.printStackTrace();
                }
            }
        });
    }
    private void updateStartStopButton() {
        try {
            // Rather than query the pads each time, use the local boolean padPlaying (set above)
            // This is because there is often a delay starting the pad and it being registered
            if (padPlaying) {
                // The action is to stop
                Log.d(TAG, "Set stop icon");
                if (getContext()!=null) {
                    myView.startStopPad.setIcon(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.stop, getContext().getTheme()));
                }
                myView.startStopPad.setText(stop_string);
                myView.startStopPad.setOnClickListener(v -> {
                    padPlaying = false;
                    mainActivityInterface.getPad().stopPad();
                    // Rerun this script to get the new icon and listener
                    updateStartStopButton();
                    // Check again in 2 seconds just in case the pad had an error
                    myView.startStopPad.postDelayed(() -> {
                        padPlaying = mainActivityInterface.getPad().isPadPlaying();
                        updateStartStopButton();
                    }, 2000);
                });
            } else {
                // The action is to play
                Log.d(TAG, "Set start icon");
                if (getContext()!=null) {
                    myView.startStopPad.setIcon(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.play, getContext().getTheme()));
                }
                myView.startStopPad.setText(start_string);
                myView.startStopPad.setOnClickListener(v -> {
                    padPlaying = true;
                    mainActivityInterface.getPad().startPad();
                    // Rerun this script
                    updateStartStopButton();
                    // Check again in 2 seconds just in case the pad had an error
                    myView.startStopPad.postDelayed(() -> {
                        padPlaying = mainActivityInterface.getPad().isPadPlaying();
                        updateStartStopButton();
                    }, 2000);
                });
            }
        } catch (Exception e) {
            // Catches update made if app has already closed!
        }
    }

    private void setupListeners() {
        myView.padKey.addTextChangedListener(new MyTextWatcher("padKey"));
        myView.padType.addTextChangedListener(new MyTextWatcher("padType"));
        myView.padLinkAudio.addTextChangedListener(new MyTextWatcher("padLink"));
        myView.padLoop.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getSong().setPadloop(""+b);
            mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),false);
        });
        myView.padSettings.setOnClickListener(view -> {
            mainActivityInterface.navigateToFragment(deeplink_pads_string,0);
            dismiss();
        });
        myView.padPanic.setOnClickListener(v -> {
            padPlaying = false;
            mainActivityInterface.getPad().panicStop();
            mainActivityInterface.getShowToast().doItBottomSheet(panic_stop_string,myView.getRoot());
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
        if (myView.padType.getText()!=null && myView.padType.getText().toString().equals(link_audio_string)) {
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
            mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),false);
            Log.d(TAG,"Song saved");
        }
    }

    private String niceTextFromPref(String padfile) {
        switch (padfile) {
            case "auto":
            default:
                return pad_auto_string;
            case "link":
                return link_audio_string;
            case "off":
                return off_string;
        }
    }

    private String prefFromNiceText(String text) {
        if (text.equals(link_audio_string)) {
            return "link";
        } else if (text.equals(off_string)) {
            return "off";
        } else {
            return "auto";
        }
    }
}

