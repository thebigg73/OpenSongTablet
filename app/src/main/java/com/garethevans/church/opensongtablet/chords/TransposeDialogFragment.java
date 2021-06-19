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
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.button.MaterialButton;

import java.io.OutputStream;

public class TransposeDialogFragment extends DialogFragment {

    private boolean editSong = false;  // This is set to true when coming here from EditSongFragment

    private TransposeDialogBinding myView;
    private SeekBar transposeSeekBar;
    private TextView transposeValTextView,keyChange_TextView;
    private RadioButton chordFormat1Radio, chordFormat2Radio, chordFormat3Radio, chordFormat4Radio,
            chordFormat5Radio, chordFormat6Radio;
    private SwitchCompat assumePreferred_SwitchCompat;
    private LinearLayout chooseFormat_LinearLayout;
    private MaterialButton doTransposeButton;
    private MainActivityInterface mainActivityInterface;

    private String transposeDirection;
    private int transposeTimes;

    public TransposeDialogFragment(boolean editSong) {
        // This is called from the EditSongFragment.  Receive temp lyrics and key
        this.editSong = editSong;
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
        usePreferredChordFormat(mainActivityInterface.getPreferences().getMyPreferenceBoolean(getActivity(),"chordFormatUsePreferred",false));

        return myView.getRoot();
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
        if (mainActivityInterface.getSong().getKey()!=null && !mainActivityInterface.getSong().getKey().equals("")) {
            String keychange = getString(R.string.key) + ": " + mainActivityInterface.getSong().getKey() + "\n" +
                    getString(R.string.transpose) + ": " + mainActivityInterface.getSong().getKey();
            keyChange_TextView.setText(keychange);
        } else {
            keyChange_TextView.setText("");
            keyChange_TextView.setVisibility(View.GONE);
        }

        // Set the detected chordformat
        switch (mainActivityInterface.getSong().getDetectedChordFormat()) {
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
                if (mainActivityInterface.getSong().getKey()!=null && !mainActivityInterface.getSong().getKey().equals("")) {
                    // Get the new key value
                    String keynum = mainActivityInterface.getTranspose().keyToNumber(mainActivityInterface.getSong().getKey());
                    String transpkeynum = mainActivityInterface.getTranspose().transposeKey(keynum, transposeDirection, transposeTimes);
                    String newkey = mainActivityInterface.getTranspose().numberToKey(getActivity(), mainActivityInterface, transpkeynum);

                    String keychange = getString(R.string.key) + ": " + mainActivityInterface.getSong().getKey() + "\n" +
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
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(getActivity(),"chordFormatUsePreferred",isChecked);
            usePreferredChordFormat(isChecked);
        });
    }

    private void usePreferredChordFormat(boolean trueorfalse) {
        // Use preferred chord format or detected one
        int formattouse;

        if (trueorfalse) {
            formattouse = mainActivityInterface.getPreferences().getMyPreferenceInt(getActivity(),"chordFormat",1);
        } else {
            formattouse = mainActivityInterface.getSong().getDetectedChordFormat();
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

        boolean usePreferred = mainActivityInterface.getPreferences().getMyPreferenceBoolean(getActivity(),"chordFormatUsePreferred",true);
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
            mainActivityInterface.getSong().setDetectedChordFormat(1);
        }
        if (chordFormat2Radio.isChecked()) {
            mainActivityInterface.getSong().setDetectedChordFormat(2);
        }
        if (chordFormat3Radio.isChecked()) {
            mainActivityInterface.getSong().setDetectedChordFormat(3);
        }
        if (chordFormat4Radio.isChecked()) {
            mainActivityInterface.getSong().setDetectedChordFormat(4);
        }
        if (chordFormat5Radio.isChecked()) {
            mainActivityInterface.getSong().setDetectedChordFormat(5);
        }
        if (chordFormat6Radio.isChecked()) {
            mainActivityInterface.getSong().setDetectedChordFormat(6);
        }
    }

    private void doTranspose() {
        new Thread(() -> {
            requireActivity().runOnUiThread(this::getValues);

            // Do the transpose
            // If we are editing the song, don't write this, just get the returned values as an array
            try {
                //TODO
               /* song = mainActivityInterface.getTranspose().doTranspose(getActivity(), mainActivityInterface, transposeDirection,
                        transposeTimes,false, false);
*/
            } catch (Exception e) {
                e.printStackTrace();
            }

            // If we are just editing, update the edit fragment and dismiss, otherwise save the new values
            if (editSong) {
                requireActivity().runOnUiThread(() -> mainActivityInterface.updateKeyAndLyrics(mainActivityInterface.getSong()));

            } else {
                // Write the new improved XML file
                String newXML = mainActivityInterface.getProcessSong().getXML(requireContext(),mainActivityInterface,mainActivityInterface.getSong());

                if (mainActivityInterface.getSong().getFiletype().equals("PDF")||mainActivityInterface.getSong().getFiletype().equals("XML")) {
                    mainActivityInterface.getNonOpenSongSQLiteHelper().updateSong(requireContext(),mainActivityInterface, mainActivityInterface.getSong());
                    mainActivityInterface.getSQLiteHelper().updateSong(requireContext(),mainActivityInterface,mainActivityInterface.getSong());

                } else {
                    mainActivityInterface.getSQLiteHelper().updateSong(requireContext(),mainActivityInterface,mainActivityInterface.getSong());
                    // Now write the file
                    Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(requireContext(),mainActivityInterface,"Songs",
                            mainActivityInterface.getSong().getFolder(), mainActivityInterface.getSong().getFilename());
                    OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(requireContext(),uri);
                    mainActivityInterface.getStorageAccess().writeFileFromString(newXML,outputStream);
                }
                requireActivity().runOnUiThread(() -> mainActivityInterface.doSongLoad(mainActivityInterface.getSong().getFolder(),mainActivityInterface.getSong().getFilename()));
            }
            dismiss();
        }).start();
    }
}
