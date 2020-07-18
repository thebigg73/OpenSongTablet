package com.garethevans.church.opensongtablet.chords;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.SongXML;
import com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLite;
import com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLiteHelper;
import com.garethevans.church.opensongtablet.sqlite.SQLite;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;
import com.google.android.material.button.MaterialButton;

import java.io.OutputStream;
import java.util.ArrayList;

public class TransposeDialogFragment extends DialogFragment {

    boolean editSong = false;  // This is set to true when coming here from EditSongFragment
    String lyrics = StaticVariables.mLyrics;  //  These get updated from the caller
    String key = StaticVariables.mKey;

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
    private SongXML songXML;
    private ProcessSong processSong;

    private MainActivityInterface mainActivityInterface;

    public TransposeDialogFragment(boolean editSong, String key, String lyrics) {
        // This is called from the EditSongFragment.  Receive temp lyrics and key
        Log.d("TransposeFrag","Caller: key="+key);

        this.editSong = editSong;
        this.lyrics = lyrics;
        this.key = key;
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
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Initialise helpers
        initialiseHelpers();

        // Initialise views
        initialiseViews();

        // Set up views to match preferences
        setButtons();

        // Set the listeners
        setListeners();

        // Initialise the transpose values
        StaticVariables.transposeDirection = "";
        StaticVariables.transposeTimes = Math.abs(0);

        // Decide if we are using preferred chord format
        usePreferredChordFormat(preferences.getMyPreferenceBoolean(getActivity(),"chordFormatUsePreferred",false));

        Log.d("TransposeFrag","End of initiliasation: key="+key);

        return myView.getRoot();
    }

    // Initialise the helpers
    private void initialiseHelpers() {
        preferences = new Preferences();
        storageAccess = new StorageAccess();
        transpose = new Transpose();
        songXML = new SongXML();
        processSong = new ProcessSong();
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
        if (key!=null && !key.equals("")) {
            String keychange = getString(R.string.edit_song_key) + ": " + key + "\n" +
                    getString(R.string.transpose) + ": " + key;
            keyChange_TextView.setText(keychange);
        } else {
            keyChange_TextView.setText("");
            keyChange_TextView.setVisibility(View.GONE);
        }

        // Set the detected chordformat
        switch (StaticVariables.detectedChordFormat) {
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
                    StaticVariables.transposeDirection = "-1";
                    StaticVariables.transposeTimes = Math.abs(val);
                    String text = "-"+Math.abs(val);
                    transposeValTextView.setText(text);

                } else if (val>0) {
                    StaticVariables.transposeDirection = "+1";
                    StaticVariables.transposeTimes = Math.abs(val);
                    String text = "+"+Math.abs(val);
                    transposeValTextView.setText(text);
                } else {
                    StaticVariables.transposeDirection = "";
                    StaticVariables.transposeTimes = Math.abs(0);
                    transposeValTextView.setText("0");
                }

                // If the song has a key specified, we will add in the text for current and new key
                if (key!=null && !key.equals("")) {
                    // Get the new key value
                    String keynum = transpose.keyToNumber(key);
                    String transpkeynum = transpose.transposeKey(keynum, StaticVariables.transposeDirection, StaticVariables.transposeTimes);
                    String newkey = transpose.numberToKey(getActivity(), preferences, transpkeynum);

                    String keychange = getString(R.string.edit_song_key) + ": " + key + "\n" +
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
            formattouse = StaticVariables.detectedChordFormat;
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
            StaticVariables.detectedChordFormat = 1;
        }
        if (chordFormat2Radio.isChecked()) {
            StaticVariables.detectedChordFormat = 2;
        }
        if (chordFormat3Radio.isChecked()) {
            StaticVariables.detectedChordFormat = 3;
        }
        if (chordFormat4Radio.isChecked()) {
            StaticVariables.detectedChordFormat = 4;
        }
        if (chordFormat5Radio.isChecked()) {
            StaticVariables.detectedChordFormat = 5;
        }
        if (chordFormat6Radio.isChecked()) {
            StaticVariables.detectedChordFormat = 6;
        }
    }

    private void doTranspose() {
        new Thread(() -> {
            requireActivity().runOnUiThread(this::getValues);

            // Do the transpose
            // If we are editing the song, don't write this, just get the returned values as an array
            try {
                ArrayList<String> newValues = transpose.doTranspose(getActivity(), key, lyrics, preferences,
                        false, false);
                key = newValues.get(0);
                lyrics = newValues.get(1);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // If we are just editing, update the edit fragment and dismiss, otherwise save the new values
            if (editSong) {
                requireActivity().runOnUiThread(() -> mainActivityInterface.updateKeyAndLyrics(key, lyrics));

            } else {
                // Write the new improved XML file
                StaticVariables.mLyrics = lyrics;
                StaticVariables.mKey = key;

                String newXML = songXML.getXML(processSong);

                if (StaticVariables.fileType.equals("PDF")||StaticVariables.fileType.equals("XML")) {
                    NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper = new NonOpenSongSQLiteHelper(requireContext());
                    NonOpenSongSQLite nonOpenSongSQLite = nonOpenSongSQLiteHelper.getSong(requireContext(),
                            storageAccess,preferences,nonOpenSongSQLiteHelper.getSongId());
                    nonOpenSongSQLiteHelper.updateSong(requireContext(),storageAccess,preferences,nonOpenSongSQLite);
                } else {
                    SQLiteHelper sqLiteHelper = new SQLiteHelper(requireContext());
                    SQLite sqLite = sqLiteHelper.getSong(requireContext(),sqLiteHelper.getSongId());
                    sqLiteHelper.updateSong(requireContext(),sqLite);
                    // Now write the file
                    Uri uri = storageAccess.getUriForItem(requireContext(),preferences,"Songs",
                            StaticVariables.whichSongFolder, StaticVariables.songfilename);
                    OutputStream outputStream = storageAccess.getOutputStream(requireContext(),uri);
                    storageAccess.writeFileFromString(newXML,outputStream);
                }
                requireActivity().runOnUiThread(() -> mainActivityInterface.doSongLoad());
            }
            dismiss();
        }).start();
    }
}
