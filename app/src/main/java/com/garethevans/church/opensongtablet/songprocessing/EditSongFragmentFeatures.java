package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
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
    private String whichLink = "audio", pad_auto_string="", link_audio_string="", off_string="",
            tempo_string="", bpm_string="", link_youtube_string="", link_web_string="",
            link_file_string="", custom_string="", link_string="";
    @SuppressWarnings("unused")
    private final String TAG = "EditSongFeatures";
    private String[] key_choice_string={};

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        editSongFragmentInterface = (EditSongFragmentInterface) context;
        if (getActivity()!=null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = EditSongFeaturesBinding.inflate(inflater, container, false);

        prepareStrings();

        // Set up the values
        setupValues();

        // Set up the listeners
        setupListeners();

        myView.getRoot().requestFocus();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            key_choice_string = getResources().getStringArray(R.array.key_choice);
            pad_auto_string = getString(R.string.pad_auto);
            link_audio_string = getString(R.string.link_audio);
            off_string = getString(R.string.off);
            tempo_string = getString(R.string.tempo);
            bpm_string = getString(R.string.bpm);
            link_youtube_string = getString(R.string.link_youtube);
            link_web_string = getString(R.string.link_web);
            link_file_string = getString(R.string.link_file);
            custom_string = getString(R.string.custom);
            link_string = getString(R.string.link);
        }
    }
    private void setupValues() {
        // The key
        if (getContext()!=null) {
            ExposedDropDownArrayAdapter keyArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                    myView.key, R.layout.view_exposed_dropdown_item, key_choice_string);
            myView.key.setAdapter(keyArrayAdapter);
            myView.originalkey.setAdapter(keyArrayAdapter);
        }
        myView.key.setText(mainActivityInterface.getTempSong().getKey());
        mainActivityInterface.getTranspose().checkOriginalKeySet(mainActivityInterface.getTempSong());
        myView.originalkey.setText(mainActivityInterface.getTempSong().getKeyOriginal());

        // The capo
        ArrayList<String> capos = new ArrayList<>();
        capos.add("");
        for (int x = 1; x < 12; x++) {
            capos.add(x + "");
        }
        if (getContext()!=null) {
            ExposedDropDownArrayAdapter capoArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                    myView.capo, R.layout.view_exposed_dropdown_item, capos);
            myView.capo.setAdapter(capoArrayAdapter);
        }
        myView.capo.setText(mainActivityInterface.getTempSong().getCapo());

        // The pad file
        ArrayList<String> padfiles = new ArrayList<>();
        padfiles.add(pad_auto_string);
        padfiles.add(link_audio_string);
        padfiles.add(off_string);
        if (getContext()!=null) {
            ExposedDropDownArrayAdapter padArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                    myView.pad, R.layout.view_exposed_dropdown_item, padfiles);
            myView.pad.setAdapter(padArrayAdapter);
        }
        if (mainActivityInterface.getTempSong().getPadfile() == null ||
                mainActivityInterface.getTempSong().getPadfile().isEmpty()) {
            mainActivityInterface.getTempSong().setPadfile("auto");
        }
        myView.pad.setText(niceTextFromPref(mainActivityInterface.getTempSong().getPadfile()));

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
        if (getContext()!=null) {
            ExposedDropDownArrayAdapter tempoArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                    myView.tempo, R.layout.view_exposed_dropdown_item, tempos);
            myView.tempo.setAdapter(tempoArrayAdapter);
        }
        myView.tempo.setHint(tempo_string + " ("+bpm_string+")");
        myView.tempo.setText(mainActivityInterface.getTempSong().getTempo());
        mainActivityInterface.getMetronome().initialiseTapTempo(myView.tapTempo,myView.timesig,null,null,myView.tempo);

        // The timesig
        ArrayList<String> timesigs = new ArrayList<>();
        for (int divisions = 1; divisions <= 16; divisions++) {
            if (divisions == 1 || divisions == 2 || divisions == 4 || divisions == 8 || divisions == 16) {
                for (int beats = 1; beats <= 16; beats++) {
                    timesigs.add(beats + "/" + divisions);
                }
            }
        }
        if (getContext()!=null) {
            ExposedDropDownArrayAdapter timesigArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                    myView.timesig, R.layout.view_exposed_dropdown_item, timesigs);
            myView.timesig.setAdapter(timesigArrayAdapter);
        }
        myView.timesig.setText(mainActivityInterface.getTempSong().getTimesig());

        // Duration and delay
        myView.durationMins.setInputType(InputType.TYPE_CLASS_NUMBER);
        myView.durationSecs.setInputType(InputType.TYPE_CLASS_NUMBER);
        myView.delay.setInputType(InputType.TYPE_CLASS_NUMBER);
        myView.durationMins.setDigits("0123456789");
        myView.durationSecs.setDigits("0123456789");
        myView.delay.setDigits("0123456789");
        if (mainActivityInterface.getTempSong().getAutoscrolllength()==null ||
        mainActivityInterface.getTempSong().getAutoscrolllength().isEmpty()) {
            mainActivityInterface.getTempSong().setAutoscrolllength("0");
        }
        int[] timeVals = mainActivityInterface.getTimeTools().getMinsSecsFromSecs(Integer.parseInt(mainActivityInterface.getTempSong().getAutoscrolllength()));
        myView.durationMins.setText(timeVals[0]+"");
        myView.durationSecs.setText(timeVals[1]+"");
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
        linkOptions.add(link_audio_string);
        linkOptions.add(link_youtube_string);
        linkOptions.add(link_web_string);
        linkOptions.add(link_file_string);
        if (getContext()!=null) {
            ExposedDropDownArrayAdapter linkArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                    myView.linkType, R.layout.view_exposed_dropdown_item, linkOptions);
            myView.linkType.setAdapter(linkArrayAdapter);
        }
        myView.linkType.setText(link_audio_string);
        setLink();

        // Resize the bottom padding to the soft keyboard height or half the screen height for the soft keyboard (workaround)
        mainActivityInterface.getWindowFlags().adjustViewPadding(mainActivityInterface,myView.resizeForKeyboardLayout);
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
        myView.durationMins.addTextChangedListener(new MyTextWatcher("durationMins"));
        myView.durationSecs.addTextChangedListener(new MyTextWatcher("durationSecs"));
        myView.delay.addTextChangedListener(new MyTextWatcher("delay"));
        myView.midi.addTextChangedListener(new MyTextWatcher("midi"));
        myView.abc.addTextChangedListener(new MyTextWatcher("abc"));
        myView.customChords.addTextChangedListener(new MyTextWatcher("customchords"));
        myView.linkType.addTextChangedListener(new MyTextWatcher("linktype"));
        myView.linkValue.addTextChangedListener(new MyTextWatcher("linkvalue"));

        myView.tapTempo.setOnClickListener(button -> mainActivityInterface.getMetronome().tapTempo());
        // Scroll listener
        myView.nestedScrollView.setExtendedFabToAnimate(editSongFragmentInterface.getSaveButton());
    }

    private String niceTextFromPref(String prefText) {
        switch (prefText) {
            default:
                return "";
            case "auto":
                return pad_auto_string;
            case "link":
                return link_audio_string;
            case "custom":
                return custom_string;
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
                case "durationMins":
                case "durationSecs":
                    updateTime();
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
                    if (editable.toString().equals(link_audio_string)) {
                        whichLink = "audio";
                    } else if (editable.toString().equals(link_youtube_string)) {
                        whichLink = "youtube";
                    } else if (editable.toString().equals(link_web_string)) {
                        whichLink = "web";
                    } else if (editable.toString().equals(link_file_string)) {
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

    private void updateTime() {
        // Because secs could be over 60, get the total, reformat and update
        String minsText;
        String secsText;
        if (myView.durationMins.getText()==null ||
        myView.durationMins.getText().toString().isEmpty()) {
            minsText = "0";
        } else {
            minsText = myView.durationMins.getText().toString();
        }
        if (myView.durationSecs.getText()==null ||
                myView.durationSecs.getText().toString().isEmpty()) {
            secsText = "0";
        } else {
            secsText = myView.durationSecs.getText().toString();
        }
        int mins = Integer.parseInt(minsText);
        int secs = Integer.parseInt(secsText);
        int total = mainActivityInterface.getTimeTools().totalSecs(mins,secs);
        mainActivityInterface.getTempSong().setAutoscrolllength(total+"");
    }
    private String shortText(String niceText) {
        if (niceText.equals(off_string)) {
            return "off";
        } else if (niceText.equals(link_string)) {
            return "link";
        } else if (niceText.equals(pad_auto_string)) {
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
