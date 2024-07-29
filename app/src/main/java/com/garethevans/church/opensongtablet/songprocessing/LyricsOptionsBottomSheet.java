package com.garethevans.church.opensongtablet.songprocessing;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetEditSongLyricsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.midi.InlineMidiBottomSheet;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class LyricsOptionsBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetEditSongLyricsBinding myView;
    private MainActivityInterface mainActivityInterface;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "LyricsBottomSheet";
    private int colorOn, colorOff;
    private final EditSongFragmentLyrics openingFragment;

    public LyricsOptionsBottomSheet() {
        // Default constructor required to avoid re-instantiation failures
        // Just close the bottom sheet
        openingFragment = null;
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Initialise with a reference to the opening fragment
    LyricsOptionsBottomSheet(EditSongFragmentLyrics openingFragment) {
        this.openingFragment = openingFragment;
    }

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
                BottomSheetBehavior.from(bottomSheet).setDraggable(false);
            }
        });
        return dialog;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = BottomSheetEditSongLyricsBinding.inflate(inflater,container,false);
        myView.dialogHeading.setClose(this);

        // The button colors
        colorOn = getResources().getColor(R.color.colorSecondary);
        colorOff = getResources().getColor(R.color.colorAltPrimary);

        // Prepare views
        prepareViews();

        // Set listeners
        setListeners();

        return myView.getRoot();

    }

    private void prepareViews() {
        myView.insertSection.setHint("[V]="+getString(R.string.verse) +
                " , [V1]="+getString(R.string.verse)+" 1, [C]="+getString(R.string.chorus) +
                ", [B]="+getString(R.string.bridge)+", [P]="+getString(R.string.prechorus) +
                ", [...]="+getString(R.string.custom)+", [*" + getString(R.string.text) + ":" +
                getString(R.string.verse) + "]="+getString(R.string.filters));
        openSongOrChoProButtonColor();
        if (openingFragment!=null) {
            myView.textSize.setHint(String.valueOf((int) openingFragment.getEditTextSize()));
        }
        setTransposeDetectedFormat();
    }

    private void openSongOrChoProButtonColor() {
        mainActivityInterface.getTempSong().setEditingAsChoPro(mainActivityInterface.getPreferences().getMyPreferenceBoolean("editAsChordPro",false));
        if (mainActivityInterface.getTempSong().getEditingAsChoPro()) {
            myView.choPro.setBackgroundTintList(ColorStateList.valueOf(colorOn));
            myView.openSong.setBackgroundTintList(ColorStateList.valueOf(colorOff));
        } else {
            myView.choPro.setBackgroundTintList(ColorStateList.valueOf(colorOff));
            myView.openSong.setBackgroundTintList(ColorStateList.valueOf(colorOn));
        }
    }

    private void setListeners() {
        myView.textSizeDown.setOnClickListener(v -> checkTextSize(-1));
        myView.textSizeUp.setOnClickListener(v -> checkTextSize(+1));
        myView.insertSection.setOnClickListener(v -> {
            try {
                if (openingFragment != null) {
                    openingFragment.insertSection("[]", 1);
                }
                this.dismiss();
            } catch (Exception e) {
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+": \n"+e);
            }
        });
        myView.insertInlineMidi.setOnClickListener(view -> {
            if (openingFragment!=null) {
                InlineMidiBottomSheet inlineMidiBottomSheet = new InlineMidiBottomSheet(openingFragment);
                inlineMidiBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "InlineMIDIMessages");
            }
            dismiss();
        });
        myView.insertColumnBreak.setOnClickListener(v -> {
            if (openingFragment!=null) {
                openingFragment.insertSection("!--", 4);
            }
            this.dismiss();
        });
        myView.copyChordSections.setOnClickListener(v -> {
            if (openingFragment!=null) {
                openingFragment.copyChords();
            }
            this.dismiss();
        });
        myView.openSong.setOnClickListener(v -> {
            // Only do this if we aren't editing as OpenSong already
            if (mainActivityInterface.getTempSong().getEditingAsChoPro()) {
                mainActivityInterface.getTempSong().setEditingAsChoPro(false);
                mainActivityInterface.getPreferences().setMyPreferenceBoolean("editAsChordPro",false);
                openSongOrChoProButtonColor();
                if (openingFragment!=null) {
                    openingFragment.convertToOpenSong();
                }
                this.dismiss();
            }
        });
        myView.choPro.setOnClickListener(v -> {
            // Only do this if we aren't editing as ChordPro already
            if (!mainActivityInterface.getTempSong().getEditingAsChoPro()) {
                mainActivityInterface.getTempSong().setEditingAsChoPro(true);
                mainActivityInterface.getPreferences().setMyPreferenceBoolean("editAsChordPro",true);
                openSongOrChoProButtonColor();
                if (openingFragment!=null) {
                    openingFragment.convertToChoPro();
                }
                this.dismiss();
            }
        });

        myView.transposeDown.setOnClickListener(v -> {
            if (openingFragment!=null) {
                openingFragment.transpose("-1");
            }
        });
        myView.transposeUp.setOnClickListener(v -> {
            if (openingFragment!=null) {
                openingFragment.transpose("+1");
            }
        });

        myView.autoFix.setOnClickListener(v -> {
            if (openingFragment!=null) {
                openingFragment.autoFix();
            }
            this.dismiss();
        });

    }


    private void setTransposeDetectedFormat() {
        String text = getString(R.string.chordformat_detected) + ": ";
        switch (mainActivityInterface.getTempSong().getDetectedChordFormat()) {
            case 1:
                myView.transposeText.setHint(text + getString(R.string.chordformat_1));
                break;
            case 2:
                myView.transposeText.setHint(text + getString(R.string.chordformat_2));
                break;
            case 3:
                myView.transposeText.setHint(text + getString(R.string.chordformat_3));
                break;
            case 4:
                myView.transposeText.setHint(text + getString(R.string.chordformat_4));
                break;
            case 5:
                myView.transposeText.setHint(text + getString(R.string.chordformat_5));
                break;
            case 6:
                myView.transposeText.setHint(text + getString(R.string.chordformat_6));
                break;
        }
    }

    private void checkTextSize(int change) {
        // Adjust it
        if (openingFragment!=null) {
            float editTextSize = openingFragment.getEditTextSize();
            editTextSize = editTextSize + change;

            // Max is 24
            if (editTextSize >= 24) {
                editTextSize = 24;
                myView.textSizeUp.setEnabled(false);
            } else {
                myView.textSizeUp.setEnabled(true);
            }

            // Min is 8
            if (editTextSize <= 8) {
                editTextSize = 8;
                myView.textSizeDown.setEnabled(false);
            } else {
                myView.textSizeDown.setEnabled(true);
            }

            // Save this to the user preferences and update the fragment
            myView.textSize.setHint(String.valueOf((int) editTextSize));
            openingFragment.setEditTextSize(editTextSize);
            mainActivityInterface.getPreferences().setMyPreferenceFloat("editTextSize", editTextSize);
        }
    }


}
