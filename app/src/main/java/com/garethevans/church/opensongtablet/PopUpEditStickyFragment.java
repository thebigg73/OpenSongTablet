package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Objects;

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

    private TextView stickyNotesWidth_TextView, stickyNotesOpacity_TextView;
    private EditText editStickyText;
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
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_editsticky, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.options_song_stickynotes));
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
        SeekBar stickyNotesWidth_SeekBar = V.findViewById(R.id.stickyNotesWidth_SeekBar);
        stickyNotesWidth_TextView = V.findViewById(R.id.stickyNotesWidth_TextView);
        SeekBar stickyNotesOpacity_SeekBar = V.findViewById(R.id.stickyNotesOpacity_SeekBar);
        stickyNotesOpacity_TextView = V.findViewById(R.id.stickyNotesOpacity_TextView);
        SwitchCompat stickyTextSize = V.findViewById(R.id.stickyTextSize);

        // Set the text if it exists
        editStickyText.setText(StaticVariables.mNotes);
        int sw = preferences.getMyPreferenceInt(getActivity(),"stickyWidth",400);
        String s = ""+sw;
        stickyNotesWidth_TextView.setText(s);
        stickyNotesWidth_SeekBar.setProgress(sw-200);
        int val = (int) (preferences.getMyPreferenceFloat(getActivity(),"stickyOpacity",0.8f) * 10) - 2;
        stickyNotesOpacity_SeekBar.setProgress(val);
        s =  ((int) (preferences.getMyPreferenceFloat(getActivity(),"stickyOpacity",0.8f)*100)) + "%";
        stickyNotesOpacity_TextView.setText(s);
        // Set the switch up based on preferences
        stickyTextSize.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"stickyLargeFont",true));

        stickyTextSize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferences.setMyPreferenceBoolean(getActivity(),"stickyLargeFont",b);
            }
        });

        stickyNotesWidth_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                String s = ""+(i+200);
                stickyNotesWidth_TextView.setText(s);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int i = seekBar.getProgress() + 200;
                preferences.setMyPreferenceInt(getActivity(),"stickyWidth",i);
            }
        });
        stickyNotesOpacity_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                i = (int)((i+2.0f)/10.0f);
                String s =  (i*100) + "%";
                stickyNotesOpacity_TextView.setText(s);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float i = (int)((seekBar.getProgress()+2.0f)/10.0f);
                preferences.setMyPreferenceFloat(getActivity(),"stickyOpacity",i);
            }
        });
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    public void doSave() {
        StaticVariables.mNotes = editStickyText.getText().toString();
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