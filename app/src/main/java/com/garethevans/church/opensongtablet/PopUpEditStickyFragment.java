package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    private Preferences preferences;
    private StorageAccess storageAccess;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (MyInterface) context;
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
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }
        View V = inflater.inflate(R.layout.popup_editsticky, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.stickynotes_edit));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getContext());
            closeMe.setEnabled(false);
            dismiss();
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(saveMe,getContext());
            saveMe.setEnabled(false);
            doSave();
        });

        preferences = new Preferences();
        storageAccess = new StorageAccess();

        // Initialise the views
        editStickyText = V.findViewById(R.id.editStickyText);
        SeekBar stickyNotesWidth_SeekBar = V.findViewById(R.id.stickyNotesWidth_SeekBar);
        stickyNotesWidth_TextView = V.findViewById(R.id.stickyNotesWidth_TextView);
        SeekBar stickyNotesOpacity_SeekBar = V.findViewById(R.id.stickyNotesOpacity_SeekBar);
        stickyNotesOpacity_TextView = V.findViewById(R.id.stickyNotesOpacity_TextView);
        SwitchCompat stickyTextSize = V.findViewById(R.id.stickyTextSize);

        // Set the text if it exists
        editStickyText.setText(StaticVariables.mNotes);
        int sw = preferences.getMyPreferenceInt(getContext(),"stickyWidth",400);
        String s = ""+sw;
        stickyNotesWidth_TextView.setText(s);
        stickyNotesWidth_SeekBar.setProgress(sw-200);
        int val = (int) (preferences.getMyPreferenceFloat(getContext(),"stickyOpacity",0.8f) * 10) - 2;
        stickyNotesOpacity_SeekBar.setProgress(val);
        s =  ((int) (preferences.getMyPreferenceFloat(getContext(),"stickyOpacity",0.8f)*100)) + "%";
        stickyNotesOpacity_TextView.setText(s);
        // Set the switch up based on preferences
        stickyTextSize.setChecked(preferences.getMyPreferenceBoolean(getContext(),"stickyLargeFont",true));

        stickyTextSize.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(getContext(),"stickyLargeFont",b));

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
                preferences.setMyPreferenceInt(getContext(),"stickyWidth",i);
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
                preferences.setMyPreferenceFloat(getContext(),"stickyOpacity",i);
            }
        });
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void doSave() {
        StaticVariables.mNotes = editStickyText.getText().toString();
        // Save the file
        PopUpEditSongFragment.prepareSongXML();

        if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
            NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper = new NonOpenSongSQLiteHelper(getContext());
            NonOpenSongSQLite nonOpenSongSQLite = nonOpenSongSQLiteHelper.getSong(getContext(),storageAccess,preferences,nonOpenSongSQLiteHelper.getSongId());
            nonOpenSongSQLiteHelper.updateSong(getContext(),storageAccess,preferences,nonOpenSongSQLite);
        } else {
            PopUpEditSongFragment.justSaveSongXML(getContext(), preferences);
        }

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
    public void onDismiss(@NonNull final DialogInterface dialog) {
        if (mListener!=null) {
            mListener.pageButtonAlpha("");
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

}