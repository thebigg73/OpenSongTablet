package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
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
import com.garethevans.church.opensongtablet.databinding.EditSongFeaturesBinding;
import com.garethevans.church.opensongtablet.interfaces.EditSongFragmentInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class EditSongFragmentFeatures extends Fragment {

    private EditSongFeaturesBinding myView;
    private MainActivityInterface mainActivityInterface;
    private EditSongFragmentInterface editSongFragmentInterface;
    private String whichLink = "audio";
    private final String TAG = "EditSongFeatures";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        editSongFragmentInterface = (EditSongFragmentInterface) context;
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
                myView.key, R.layout.view_exposed_dropdown_item, getResources().getStringArray(R.array.key_choice));
        myView.key.setAdapter(keyArrayAdapter);
        myView.key.setText(mainActivityInterface.getTempSong().getKey());

        // The capo
        ArrayList<String> capos = new ArrayList<>();
        capos.add("");
        for (int x = 1; x < 12; x++) {
            capos.add(x + "");
        }
        ExposedDropDownArrayAdapter capoArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),
                myView.capo, R.layout.view_exposed_dropdown_item, capos);
        myView.capo.setAdapter(capoArrayAdapter);
        myView.capo.setText(mainActivityInterface.getTempSong().getCapo());

        // The pad file
        ArrayList<String> padfiles = new ArrayList<>();
        padfiles.add(getString(R.string.pad_auto));
        padfiles.add(getString(R.string.link_audio));
        padfiles.add(getString(R.string.off));
        ExposedDropDownArrayAdapter padArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),
                myView.pad, R.layout.view_exposed_dropdown_item, padfiles);
        myView.pad.setAdapter(padArrayAdapter);
        Log.d(TAG,"padFile="+mainActivityInterface.getTempSong().getPadfile());
        if (mainActivityInterface.getTempSong().getPadfile() == null ||
                mainActivityInterface.getTempSong().getPadfile().isEmpty()) {
            mainActivityInterface.getTempSong().setPadfile("auto");
        }
        myView.pad.setText(niceTextFromPref(mainActivityInterface.getTempSong().getPadfile()));
        Log.d(TAG,"padFile="+mainActivityInterface.getTempSong().getPadfile());

        // The loop
        if (mainActivityInterface.getTempSong().getPadloop()!=null) {
            myView.loop.setChecked(mainActivityInterface.getTempSong().getPadloop().equals("true"));
        } else {
            myView.loop.setChecked(false);
        }

        // The tempo
        ArrayList<String> tempos = new ArrayList<>();
        tempos.add("");
        for (int x = 40; x < 300; x++) {
            tempos.add(x + "");
        }
        ExposedDropDownArrayAdapter tempoArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),
                myView.tempo, R.layout.view_exposed_dropdown_item, tempos);
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
                myView.timesig, R.layout.view_exposed_dropdown_item, timesigs);
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

        // The links
        ArrayList<String> linkOptions = new ArrayList<>();
        linkOptions.add(getString(R.string.link_audio));
        linkOptions.add(getString(R.string.link_youtube));
        linkOptions.add(getString(R.string.link_web));
        linkOptions.add(getString(R.string.link_file));
        ExposedDropDownArrayAdapter linkArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),
                myView.linkType, R.layout.view_exposed_dropdown_item, linkOptions);
        myView.linkType.setAdapter(linkArrayAdapter);
        myView.linkType.setText(getString(R.string.link_audio));
        setLink();
    }

    private void setLink() {
        String linkvalue;
        switch (whichLink) {
            case "audio":
            default:
                linkvalue = mainActivityInterface.getTempSong().getLinkaudio();
                break;
            case "youtube":
                linkvalue = mainActivityInterface.getTempSong().getLinkyoutube();
                break;
            case "web":
                linkvalue = mainActivityInterface.getTempSong().getLinkweb();
                break;
            case "other":
                linkvalue = mainActivityInterface.getTempSong().getLinkother();
                break;
        }
        myView.linkValue.setText(linkvalue);
    }

    private void editLink(String value) {
        switch (whichLink) {
            case "audio":
            default:
                mainActivityInterface.getTempSong().setLinkaudio(value);
                break;
            case "youtube":
                mainActivityInterface.getTempSong().setLinkyoutube(value);
                break;
            case "web":
                mainActivityInterface.getTempSong().setLinkweb(value);
                break;
            case "other":
                mainActivityInterface.getTempSong().setLinkother(value);
                break;
        }
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
        myView.linkType.addTextChangedListener(new MyTextWatcher("linktype"));
        myView.linkValue.addTextChangedListener(new MyTextWatcher("linkvalue"));

        // Scroll listener
        myView.nestedScrollView.setExtendedFabToAnimate(editSongFragmentInterface.getSaveButton());
    }

    private String niceTextFromPref(String prefText) {
        Log.d(TAG,"prefText: "+prefText);
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
                case "linktype":
                    if (editable.toString().equals(getString(R.string.link_audio))) {
                        whichLink = "audio";
                    } else if (editable.toString().equals(getString(R.string.link_youtube))) {
                        whichLink = "youtube";
                    } else if (editable.toString().equals(getString(R.string.link_web))) {
                        whichLink = "web";
                    } else if (editable.toString().equals(getString(R.string.link_file))) {
                        whichLink = "other";
                    }
                    setLink();
                    break;
                case "linkvalue":
                    editLink(editable.toString());
                    break;
            }
        }
    }

    private String shortText(String niceText) {
        Log.d(TAG,"niceText="+niceText);
        if (niceText.equals(getString(R.string.off))) {
            return "off";
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
