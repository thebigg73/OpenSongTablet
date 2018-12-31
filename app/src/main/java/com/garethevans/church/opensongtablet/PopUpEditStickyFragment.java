package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

public class PopUpEditStickyFragment extends DialogFragment {

    static PopUpEditStickyFragment newInstance() {
        PopUpEditStickyFragment frag;
        frag = new PopUpEditStickyFragment();
        return frag;
    }

    public interface MyInterface {
        void loadSong();
        void pageButtonAlpha(String s);
    }

    private MyInterface mListener;

    SeekBar stickyNotesWidth_SeekBar, stickyNotesOpacity_SeekBar;
    TextView stickyNotesWidth_TextView, stickyNotesOpacity_TextView;
    SwitchCompat stickyTextSize;
    EditText editStickyText;
    Preferences preferences;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_editsticky, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.options_song_stickynotes));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(saveMe,getActivity());
                saveMe.setEnabled(false);
                doSave();
            }
        });

        preferences = new Preferences();

        // Initialise the views
        editStickyText = V.findViewById(R.id.editStickyText);
        stickyNotesWidth_SeekBar = V.findViewById(R.id.stickyNotesWidth_SeekBar);
        stickyNotesWidth_TextView = V.findViewById(R.id.stickyNotesWidth_TextView);
        stickyNotesOpacity_SeekBar = V.findViewById(R.id.stickyNotesOpacity_SeekBar);
        stickyNotesOpacity_TextView = V.findViewById(R.id.stickyNotesOpacity_TextView);
        stickyTextSize = V.findViewById(R.id.stickyTextSize);

        // Set the text if it exists
        editStickyText.setText(FullscreenActivity.mNotes);
        String s = ""+FullscreenActivity.stickyWidth;
        stickyNotesWidth_TextView.setText(s);
        stickyNotesWidth_SeekBar.setProgress(FullscreenActivity.stickyWidth-200);
        int val = (int) (FullscreenActivity.stickyOpacity * 10) - 2;
        stickyNotesOpacity_SeekBar.setProgress(val);
        s =  ((int) (FullscreenActivity.stickyOpacity*100)) + "%";
        stickyNotesOpacity_TextView.setText(s);
        // Set the switch up based on preferences
        if (FullscreenActivity.stickyTextSize==18.0f) {
            stickyTextSize.setChecked(true);
        } else {
            stickyTextSize.setChecked(false);
        }

        stickyTextSize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    FullscreenActivity.stickyTextSize = 18.0f;
                } else {
                    FullscreenActivity.stickyTextSize = 14.0f;
                }
                Preferences.savePreferences();
            }
        });

        stickyNotesWidth_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                FullscreenActivity.stickyWidth = i + 200;
                String s = ""+FullscreenActivity.stickyWidth;
                stickyNotesWidth_TextView.setText(s);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Preferences.savePreferences();
            }
        });
        stickyNotesOpacity_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                FullscreenActivity.stickyOpacity = ((float) (i+2))/10.0f;
                String s =  ((int) (FullscreenActivity.stickyOpacity*100)) + "%";
                stickyNotesOpacity_TextView.setText(s);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Preferences.savePreferences();
            }
        });
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());

        return V;
    }

    public void doSave() {
        FullscreenActivity.mNotes = editStickyText.getText().toString();
        // Save the file
        PopUpEditSongFragment.prepareSongXML();
        PopUpEditSongFragment.justSaveSongXML(getActivity(), preferences);
        if (mListener!=null) {
            mListener.loadSong();
        }
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        if (mListener!=null) {
            mListener.pageButtonAlpha("");
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}