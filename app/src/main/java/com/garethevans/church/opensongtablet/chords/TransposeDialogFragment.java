package com.garethevans.church.opensongtablet.chords;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.TransposeDialogBinding;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.sqlite.CommonSQL;
import com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLiteHelper;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;
import com.google.android.material.button.MaterialButton;

import java.io.OutputStream;

public class TransposeDialogFragment extends DialogFragment {

    private boolean editSong = false;  // This is set to true when coming here from EditSongFragment
    private Song song;

    private TransposeDialogBinding myView;
    private SeekBar transposeSeekBar;
    private TextView transposeValTextView,keyChange_TextView;
    private RadioButton chordFormat1Radio, chordFormat2Radio, chordFormat3Radio, chordFormat4Radio,
            chordFormat5Radio, chordFormat6Radio;
    private SwitchCompat assumePreferred_SwitchCompat;
    private LinearLayout chooseFormat_LinearLayout;
    private MaterialButton doTransposeButton;
    private Preferences preferences;
    private StorageAccess storageAccess;
    private Transpose transpose;
    private ProcessSong processSong;
    private SQLiteHelper sqLiteHelper;
    private NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper;
    private CommonSQL commonSQL;

    private MainActivityInterface mainActivityInterface;

    private String transposeDirection;
    private int transposeTimes;

    public TransposeDialogFragment(boolean editSong, Song song) {
        // This is called from the EditSongFragment.  Receive temp lyrics and key
        this.editSong = editSong;
        this.song = song;
    }

    public TransposeDialogFragment() {
        // Null initialised for when we come here from performance/presentation/stage mode
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = TransposeDialogBinding.inflate(inflater,container,false);
        Window w = requireDialog().getWindow();
        if (w!=null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Initialise helpers
        initialiseHelpers();

        // Initialise views
        initialiseViews();

        // Set up views to match preferences
        setButtons();

        // Set the listeners
        setListeners();

        // Initialise the transpose values
        transposeDirection = "";
        transposeTimes = Math.abs(0);

        // Decide if we are using preferred chord format
        usePreferredChordFormat(preferences.getMyPreferenceBoolean(getActivity(),"chordFormatUsePreferred",false));

        return myView.getRoot();
    }

    // Initialise the helpers
    private void initialiseHelpers() {
        preferences = mainActivityInterface.getPreferences();
        storageAccess = mainActivityInterface.getStorageAccess();
        transpose = mainActivityInterface.getTranspose();
        processSong = mainActivityInterface.getProcessSong();
        sqLiteHelper = mainActivityInterface.getSQLiteHelper();
        nonOpenSongSQLiteHelper = mainActivityInterface.getNonOpenSongSQLiteHelper();
        commonSQL = mainActivityInterface.getCommonSQL();
    }

    // Initialise the views
    private void initialiseViews() {
        transposeSeekBar = myView.transposeSeekBar;
        keyChange_TextView = myView.keyChangeTextView;
        transposeValTextView = myView.transposeValTextView;
        chordFormat1Radio = myView.chordFormat1Radio;
        chordFormat2Radio = myView.chordFormat2Radio;
        chordFormat3Radio = myView.chordFormat3Radio;
        chordFormat4Radio = myView.chordFormat4Radio;
        chordFormat5Radio = myView.chordFormat5Radio;
        chordFormat6Radio = myView.chordFormat6Radio;
        assumePreferred_SwitchCompat = myView.assumePreferredSwitchCompat;
        chooseFormat_LinearLayout = myView.chooseFormatLinearLayout;
        doTransposeButton = myView.doTransposeButton;
    }

    private void setButtons() {
        transposeSeekBar.setMax(12);
        transposeSeekBar.setProgress(6);
        transposeValTextView.setText("0");

        // If the song has a key specified, we will add in the text for current and new key
        if (song.getKey()!=null && !song.getKey().equals("")) {
            String keychange = getString(R.string.key) + ": " + song.getKey() + "\n" +
                    getString(R.string.transpose) + ": " + song.getKey();
            keyChange_TextView.setText(keychange);
        } else {
            keyChange_TextView.setText("");
            keyChange_TextView.setVisibility(View.GONE);
        }

        // Set the detected chordformat
        switch (song.getDetectedChordFormat()) {
            case 1:
                chordFormat1Radio.setChecked(true);
                break;
            case 2:
                chordFormat2Radio.setChecked(true);
                break;
            case 3:
                chordFormat3Radio.setChecked(true);
                break;
            case 4:
                chordFormat4Radio.setChecked(true);
                break;
            case 5:
                chordFormat5Radio.setChecked(true);
                break;
            case 6:
                chordFormat6Radio.setChecked(true);
                break;
        }
    }

    private void setListeners() {
        //0=-6, 1=-5, 2=-4, 3=-3, 4=-2, 5=-1, 6=0, 7=1, 8=2, 9=3, 10=4, 11=5, 12=6
        transposeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int val = progress-6;
                if (val<0) {
                    transposeDirection = "-1";
                    transposeTimes = Math.abs(val);
                    String text = "-"+Math.abs(val);
                    transposeValTextView.setText(text);

                } else if (val>0) {
                    transposeDirection = "+1";
                    transposeTimes = Math.abs(val);
                    String text = "+"+Math.abs(val);
                    transposeValTextView.setText(text);
                } else {
                    transposeDirection = "";
                    transposeTimes = Math.abs(0);
                    transposeValTextView.setText("0");
                }

                // If the song has a key specified, we will add in the text for current and new key
                if (song.getKey()!=null && !song.getKey().equals("")) {
                    // Get the new key value
                    String keynum = transpose.keyToNumber(song.getKey());
                    String transpkeynum = transpose.transposeKey(keynum, transposeDirection, transposeTimes);
                    String newkey = transpose.numberToKey(getActivity(), preferences, transpkeynum);

                    String keychange = getString(R.string.key) + ": " + song.getKey() + "\n" +
                            getString(R.string.transpose) + ": " + newkey;
                    keyChange_TextView.setText(keychange);
                } else {
                    keyChange_TextView.setText("");
                    keyChange_TextView.setVisibility(View.GONE);
                }
                doTransposeButton.setOnClickListener(v -> doTranspose());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        assumePreferred_SwitchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setMyPreferenceBoolean(getActivity(),"chordFormatUsePreferred",isChecked);
            usePreferredChordFormat(isChecked);
        });
    }

    private void usePreferredChordFormat(boolean trueorfalse) {
        // Use preferred chord format or detected one
        int formattouse;

        if (trueorfalse) {
            formattouse = preferences.getMyPreferenceInt(getActivity(),"chordFormat",1);
        } else {
            formattouse = song.getDetectedChordFormat();
        }

        switch (formattouse) {
            case 1:
                chordFormat1Radio.setChecked(true);
                break;
            case 2:
                chordFormat2Radio.setChecked(true);
                break;
            case 3:
                chordFormat3Radio.setChecked(true);
                break;
            case 4:
                chordFormat4Radio.setChecked(true);
                break;
            case 5:
                chordFormat5Radio.setChecked(true);
                break;
        }

        boolean usePreferred = preferences.getMyPreferenceBoolean(getActivity(),"chordFormatUsePreferred",true);
        assumePreferred_SwitchCompat.setChecked(usePreferred);

        if (usePreferred) {
            chooseFormat_LinearLayout.setVisibility(View.GONE);
        } else {
            chooseFormat_LinearLayout.setVisibility(View.VISIBLE);
        }
    }

    private void getValues() {
        // Extract the transpose value and the chord format
        if (chordFormat1Radio.isChecked()) {
            song.setDetectedChordFormat(1);
        }
        if (chordFormat2Radio.isChecked()) {
            song.setDetectedChordFormat(2);
        }
        if (chordFormat3Radio.isChecked()) {
            song.setDetectedChordFormat(3);
        }
        if (chordFormat4Radio.isChecked()) {
            song.setDetectedChordFormat(4);
        }
        if (chordFormat5Radio.isChecked()) {
            song.setDetectedChordFormat(5);
        }
        if (chordFormat6Radio.isChecked()) {
            song.setDetectedChordFormat(6);
        }
    }

    private void doTranspose() {
        new Thread(() -> {
            requireActivity().runOnUiThread(this::getValues);

            // Do the transpose
            // If we are editing the song, don't write this, just get the returned values as an array
            try {
                song = transpose.doTranspose(getActivity(), song, preferences,transposeDirection,
                        transposeTimes,false, false);

            } catch (Exception e) {
                e.printStackTrace();
            }

            // If we are just editing, update the edit fragment and dismiss, otherwise save the new values
            if (editSong) {
                requireActivity().runOnUiThread(() -> mainActivityInterface.updateKeyAndLyrics(song));

            } else {
                // Write the new improved XML file
                String newXML = processSong.getXML(song);

                if (song.getFiletype().equals("PDF")||song.getFiletype().equals("XML")) {
                    nonOpenSongSQLiteHelper.updateSong(requireContext(),commonSQL,storageAccess,preferences,song);
                    sqLiteHelper.updateSong(requireContext(),commonSQL,song);

                } else {
                    sqLiteHelper.updateSong(requireContext(),commonSQL,song);
                    // Now write the file
                    Uri uri = storageAccess.getUriForItem(requireContext(),preferences,"Songs",
                            song.getFolder(), song.getFilename());
                    OutputStream outputStream = storageAccess.getOutputStream(requireContext(),uri);
                    storageAccess.writeFileFromString(newXML,outputStream);
                }
                requireActivity().runOnUiThread(() -> mainActivityInterface.doSongLoad(song.getFolder(),song.getFilename()));
            }
            dismiss();
        }).start();
    }
}
