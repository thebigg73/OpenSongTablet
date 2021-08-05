package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownSelection;
import com.garethevans.church.opensongtablet.databinding.EditSongFeaturesBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class EditSongFragmentFeatures extends Fragment {

    EditSongFeaturesBinding myView;
    MainActivityInterface mainActivityInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = requireActivity().getWindow();
        if (w!=null) {
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = EditSongFeaturesBinding.inflate(inflater, container, false);

        // Set up the values
        setupValues();

        // Set up the listeners
        setupListeners();

        return myView.getRoot();
    }

    private void setupValues() {
        // The key
        ExposedDropDownArrayAdapter keyArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),
                R.layout.view_exposed_dropdown_item, getResources().getStringArray(R.array.key_choice));
        myView.key.setAdapter(keyArrayAdapter);
        ExposedDropDownSelection exposedDropDownSelection = new ExposedDropDownSelection();
        exposedDropDownSelection.keepSelectionPosition(myView.key, getResources().getStringArray(R.array.key_choice));
        myView.key.setText(mainActivityInterface.getTempSong().getKey());

        // The capo
        ArrayList<String> capos = new ArrayList<>();
        capos.add("");
        for (int x = 1; x < 12; x++) {
            capos.add(x + "");
        }
        ExposedDropDownArrayAdapter capoArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),
                R.layout.view_exposed_dropdown_item, capos);
        exposedDropDownSelection.keepSelectionPosition(myView.capo, capos);
        myView.capo.setAdapter(capoArrayAdapter);
        myView.capo.setText(mainActivityInterface.getTempSong().getCapo());

        // The pad file
        ArrayList<String> padfiles = new ArrayList<>();
        padfiles.add(getString(R.string.pad_auto));
        padfiles.add(getString(R.string.custom));
        padfiles.add(getString(R.string.link_audio));
        ExposedDropDownArrayAdapter padArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),
                R.layout.view_exposed_dropdown_item, padfiles);
        exposedDropDownSelection.keepSelectionPosition(myView.pad, padfiles);
        myView.pad.setAdapter(padArrayAdapter);
        if (mainActivityInterface.getTempSong().getPadfile().isEmpty()) {
            mainActivityInterface.getTempSong().setPadfile("auto");
        }
        myView.pad.setText(niceTextFromPref(mainActivityInterface.getTempSong().getPadfile()));

        // The loop
        myView.loop.setChecked(mainActivityInterface.getTempSong().getPadloop().equals("true"));

        // The tempo
        ArrayList<String> tempos = new ArrayList<>();
        tempos.add("");
        for (int x = 40; x < 300; x++) {
            tempos.add(x + "");
        }
        ExposedDropDownArrayAdapter tempoArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),
                R.layout.view_exposed_dropdown_item, tempos);
        exposedDropDownSelection.keepSelectionPosition(myView.tempo, tempos);
        myView.tempo.setAdapter(tempoArrayAdapter);
        myView.tempo.setHint(getString(R.string.tempo) + " ("+getString(R.string.bpm)+")");
        myView.tempo.setText(mainActivityInterface.getTempSong().getTempo());

        // The timesig
        ArrayList<String> timesigs = new ArrayList<>();
        for (int divisions = 1; divisions <= 16; divisions++) {
            if (divisions == 1 || divisions == 2 || divisions == 4 || divisions == 8 || divisions == 16) {
                for (int beats = 1; beats <= 16; beats++) {
                    timesigs.add(beats + "/" + divisions);
                }
            }
        }
        ExposedDropDownArrayAdapter timesigArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),
                R.layout.view_exposed_dropdown_item, timesigs);
        exposedDropDownSelection.keepSelectionPosition(myView.timesig, timesigs);
        myView.timesig.setAdapter(timesigArrayAdapter);
        myView.timesig.setText(mainActivityInterface.getTempSong().getTimesig());

        // Duration and delay
        myView.duration.setInputType(InputType.TYPE_CLASS_NUMBER);
        myView.delay.setInputType(InputType.TYPE_CLASS_NUMBER);
        myView.duration.setDigits("0123456789");
        myView.delay.setDigits("0123456789");
        myView.duration.setText(mainActivityInterface.getTempSong().getAutoscrolllength());
        myView.delay.setText(mainActivityInterface.getTempSong().getAutoscrolldelay());

        // The midi, abc and customchords
        myView.midi.setText(mainActivityInterface.getTempSong().getMidi());
        myView.abc.setText(mainActivityInterface.getTempSong().getAbc());
        myView.customChords.setText(mainActivityInterface.getTempSong().getCustomchords());
        mainActivityInterface.getProcessSong().editBoxToMultiline(myView.midi);
        mainActivityInterface.getProcessSong().editBoxToMultiline(myView.abc);
        mainActivityInterface.getProcessSong().editBoxToMultiline(myView.customChords);
        checkLines();
    }

    private void checkLines() {
        mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.midi, 2);
        mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.abc, 2);
        mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.midi, 2);
    }
    private void setupListeners() {
        // The simple text only fields
        myView.key.addTextChangedListener(new MyTextWatcher("key"));
        myView.capo.addTextChangedListener(new MyTextWatcher("capo"));
        myView.pad.addTextChangedListener(new MyTextWatcher("pad"));
        myView.loop.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                mainActivityInterface.getTempSong().setPadloop("true");
            } else {
                mainActivityInterface.getTempSong().setPadloop("false");
            }
        });
        myView.tempo.addTextChangedListener(new MyTextWatcher("tempo"));
        myView.timesig.addTextChangedListener(new MyTextWatcher("timesig"));
        myView.duration.addTextChangedListener(new MyTextWatcher("duration"));
        myView.delay.addTextChangedListener(new MyTextWatcher("delay"));
        myView.midi.addTextChangedListener(new MyTextWatcher("midi"));
        myView.abc.addTextChangedListener(new MyTextWatcher("abc"));
        myView.customChords.addTextChangedListener(new MyTextWatcher("customchords"));
    }

    private String niceTextFromPref(String prefText) {
        switch (prefText) {
            default:
                return "";
            case "auto":
                return getString(R.string.pad_auto);
            case "link":
                return getString(R.string.link_audio);
            case "custom":
                return getString(R.string.custom);
        }
    }

    private class MyTextWatcher implements TextWatcher {

        String what;

        MyTextWatcher(String what) {
            this.what = what;
        }
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void afterTextChanged(Editable editable) {
            switch (what) {
                case "key":
                    mainActivityInterface.getTempSong().setKey(editable.toString());
                    break;
                case "capo":
                    mainActivityInterface.getTempSong().setCapo(editable.toString());
                    break;
                case "pad":
                    // We need to save the English short text in the preferences
                    mainActivityInterface.getTempSong().setPadfile(shortText(editable.toString()));
                    break;
                case "tempo":
                    mainActivityInterface.getTempSong().setTempo(editable.toString());
                    break;
                case "timesig":
                    mainActivityInterface.getTempSong().setTimesig(editable.toString());
                    break;
                case "duration":
                    mainActivityInterface.getTempSong().setAutoscrolllength(editable.toString());
                    break;
                case "delay":
                    mainActivityInterface.getTempSong().setAutoscrolldelay(editable.toString());
                    break;
                case "midi":
                    mainActivityInterface.getTempSong().setMidi(editable.toString());
                    break;
                case "abc":
                    mainActivityInterface.getTempSong().setAbc(editable.toString());
                    break;
                case "customchords":
                    mainActivityInterface.getTempSong().setCustomChords(editable.toString());
                    break;
            }
        }
    }

    private String shortText(String niceText) {
        if (niceText.equals(getString(R.string.custom))) {
            return "custom";
        } else if (niceText.equals(getString(R.string.link))) {
            return "link";
        } else if (niceText.equals(getString(R.string.pad_auto))) {
            return "auto";
        } else {
            return "";
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}
