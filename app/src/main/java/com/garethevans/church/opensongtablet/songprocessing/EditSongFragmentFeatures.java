package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    private String whichLink = "audio";
    private String pad_auto_string="";
    private String link_audio_string="";
    private String off_string="";
    private String tempo_string="";
    private String bpm_string="";
    private String link_youtube_string="";
    private String link_web_string="";
    private String link_file_string="";
    private String custom_string="";
    private String link_string="";
    private String online_search_string="";
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "EditSongFeatures";
    private String[] key_choice_string={};

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        editSongFragmentInterface = (EditSongFragmentInterface) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            prepareStrings();

            // Set up the values
            setupValues();

            // Set up the listeners
            setupListeners();

        });
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = EditSongFeaturesBinding.inflate(inflater, container, false);

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
            String search_string = getString(R.string.search);
            String online_string = getString(R.string.online);
            online_search_string = search_string +" ("+ online_string +")";
        }
    }
    private void setupValues() {
        // The key
        mainActivityInterface.getTranspose().checkOriginalKeySet(mainActivityInterface.getTempSong());
        if (getContext()!=null) {
            myView.key.post(() -> {
                ExposedDropDownArrayAdapter keyArrayAdapter1 = new ExposedDropDownArrayAdapter(getContext(),
                        myView.key, R.layout.view_exposed_dropdown_item, key_choice_string);
                myView.key.setAdapter(keyArrayAdapter1);
                myView.key.setText(mainActivityInterface.getTempSong().getKey());
            });
            myView.originalkey.post(() -> {
                ExposedDropDownArrayAdapter keyArrayAdapter2 = new ExposedDropDownArrayAdapter(getContext(),
                        myView.originalkey, R.layout.view_exposed_dropdown_item, key_choice_string);
                myView.originalkey.setAdapter(keyArrayAdapter2);
                myView.originalkey.setText(mainActivityInterface.getTempSong().getKeyOriginal());
            });

        }
        myView.searchOnline.post(() -> myView.searchOnline.setText(online_search_string));

        // The capo
        setupCapo();

        // The pad file
        ArrayList<String> padfiles = new ArrayList<>();
        padfiles.add(pad_auto_string);
        padfiles.add(link_audio_string);
        padfiles.add(off_string);
        if (getContext()!=null) {
            myView.pad.post(() -> {
                ExposedDropDownArrayAdapter padArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                        myView.pad, R.layout.view_exposed_dropdown_item, padfiles);
                myView.pad.setAdapter(padArrayAdapter);
                myView.pad.setText(niceTextFromPref(mainActivityInterface.getTempSong().getPadfile()));
            });
        }
        if (mainActivityInterface.getTempSong().getPadfile() == null ||
                mainActivityInterface.getTempSong().getPadfile().isEmpty()) {
            mainActivityInterface.getTempSong().setPadfile("auto");
        }

        // The loop
        if (mainActivityInterface.getTempSong().getPadloop()!=null) {
            myView.loop.post(() -> myView.loop.setChecked(mainActivityInterface.getTempSong().getPadloop().equals("true")));
        } else {
            myView.loop.post(() -> myView.loop.setChecked(false));
        }

        // The tempo
        ArrayList<String> tempos = new ArrayList<>();
        tempos.add("");
        for (int x = 40; x < 300; x++) {
            tempos.add(String.valueOf(x));
        }
        if (getContext()!=null) {
            myView.tempo.post(() -> {
                ExposedDropDownArrayAdapter tempoArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                        myView.tempo, R.layout.view_exposed_dropdown_item, tempos);
                myView.tempo.setAdapter(tempoArrayAdapter);
                myView.tempo.setHint(tempo_string + " ("+bpm_string+")");
                myView.tempo.setText(mainActivityInterface.getTempSong().getTempo());
                mainActivityInterface.getMetronome().initialiseTapTempo(myView.tapTempo,myView.timesig,null,null,myView.tempo);
            });
        }

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
            myView.timesig.post(() -> {
                ExposedDropDownArrayAdapter timesigArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                        myView.timesig, R.layout.view_exposed_dropdown_item, timesigs);
                myView.timesig.setAdapter(timesigArrayAdapter);
                myView.timesig.setText(mainActivityInterface.getTempSong().getTimesig());
            });
        }

        // Duration and delay
        if (mainActivityInterface.getTempSong().getAutoscrolllength()==null ||
                mainActivityInterface.getTempSong().getAutoscrolllength().isEmpty()) {
            mainActivityInterface.getTempSong().setAutoscrolllength("0");
        }
        int[] timeVals = mainActivityInterface.getTimeTools().getMinsSecsFromSecs(Integer.parseInt(mainActivityInterface.getTempSong().getAutoscrolllength()));

        myView.durationMins.post(() -> {
            myView.durationMins.setInputType(InputType.TYPE_CLASS_NUMBER);
            myView.durationMins.setDigits("0123456789");
            myView.durationMins.setText(String.valueOf(timeVals[0]));
        });
        myView.durationSecs.post(() -> {
            myView.durationSecs.setInputType(InputType.TYPE_CLASS_NUMBER);
            myView.durationSecs.setDigits("0123456789");
            myView.durationSecs.setText(String.valueOf(timeVals[1]));
        });
        myView.delay.post(() -> {
            myView.delay.setInputType(InputType.TYPE_CLASS_NUMBER);
            myView.delay.setDigits("0123456789");
            myView.delay.setText(mainActivityInterface.getTempSong().getAutoscrolldelay());
        });

        // The midi, abc and customchords
        myView.midi.post(() -> {
            myView.midi.setText(mainActivityInterface.getTempSong().getMidi());
            mainActivityInterface.getProcessSong().editBoxToMultiline(myView.midi);
        });
        myView.abc.post(() -> {
            myView.abc.setText(mainActivityInterface.getTempSong().getAbc());
            mainActivityInterface.getProcessSong().editBoxToMultiline(myView.abc);
        });
        myView.customChords.post(() -> {
            myView.customChords.setText(mainActivityInterface.getTempSong().getCustomchords());
            mainActivityInterface.getProcessSong().editBoxToMultiline(myView.customChords);
        });
        checkLines();

        // The links
        ArrayList<String> linkOptions = new ArrayList<>();
        linkOptions.add(link_audio_string);
        linkOptions.add(link_youtube_string);
        linkOptions.add(link_web_string);
        linkOptions.add(link_file_string);
        if (getContext()!=null) {
            myView.linkType.post(() -> {
                ExposedDropDownArrayAdapter linkArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                        myView.linkType, R.layout.view_exposed_dropdown_item, linkOptions);
                myView.linkType.setAdapter(linkArrayAdapter);
                myView.linkType.setText(link_audio_string);
            });
        }
        setLink();

        // Resize the bottom padding to the soft keyboard height or half the screen height for the soft keyboard (workaround)
        myView.resizeForKeyboardLayout.post(() -> mainActivityInterface.getWindowFlags().adjustViewPadding(mainActivityInterface,myView.resizeForKeyboardLayout));
    }

    private void setupCapo() {
        ArrayList<String> capos = new ArrayList<>();
        capos.add("");
        String songkey = mainActivityInterface.getTempSong().getKey();
        String origkey = mainActivityInterface.getTempSong().getKeyOriginal();
        if ((songkey==null || songkey.isEmpty()) && origkey!=null && !origkey.isEmpty()) {
            songkey = origkey;
            mainActivityInterface.getTempSong().setKey(origkey);
            String finalOrigkey = origkey;
            myView.key.post(() -> myView.key.setText(finalOrigkey));

        }
        if ((origkey==null || origkey.isEmpty()) && songkey!=null && !songkey.isEmpty()) {
            origkey = songkey;
            mainActivityInterface.getTempSong().setKeyOriginal(songkey);
            String finalSongkey = songkey;
            myView.originalkey.post(() -> myView.originalkey.setText(finalSongkey));
        }

        for (int x = 1; x < 12; x++) {
            // If we have a ket set, work out the capo key
            String capokey = "";
            if (songkey!=null && !songkey.isEmpty()) {
                capokey = " (" + mainActivityInterface.getTranspose().transposeChordForCapo(x,"."+songkey) + ")";
            }
            capos.add(x + capokey.replace(".",""));
        }

        if (getContext()!=null) {
            myView.capo.post(() -> {
                ExposedDropDownArrayAdapter capoArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                        myView.capo, R.layout.view_exposed_dropdown_item, capos);
                myView.capo.setAdapter(capoArrayAdapter);
            });
        }

        String songcapo = mainActivityInterface.getTempSong().getCapo();
        if (songcapo==null) {
            songcapo = "";
        }

        Log.d(TAG,"songcapo:"+songcapo+"  songkey:"+songkey);
        if (songkey!=null && !songkey.isEmpty() && !songcapo.isEmpty()) {
            songcapo = songcapo.replaceAll("\\D","").trim();
            if (!songcapo.isEmpty()) {
                songcapo += " (" + mainActivityInterface.getTranspose().transposeChordForCapo(Integer.parseInt(songcapo),"."+songkey) + ")";
            }
        }

        String finalSongcapo = songcapo;
        myView.capo.post(() -> myView.capo.setText(finalSongcapo.replace(".","")));
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
        myView.linkValue.post(() -> myView.linkValue.setText(linkvalue));
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
        myView.midi.post(() -> mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.midi, 2));
        myView.abc.post(() -> mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.abc, 2));
    }
    private void setupListeners() {
        new Handler(Looper.getMainLooper()).post(() -> {
            myView.searchOnline.setOnClickListener(view -> {
                GetBPMBottomSheet getBPMBottomSheet = new GetBPMBottomSheet(EditSongFragmentFeatures.this);
                getBPMBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "GetBPMBottomSheet");
            });
            // The simple text only fields
            myView.key.addTextChangedListener(new MyTextWatcher("key"));
            myView.originalkey.addTextChangedListener(new MyTextWatcher("originalkey"));
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
            myView.featuresScrollView.setExtendedFabToAnimate(editSongFragmentInterface.getSaveButton());
        });
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
                    setupCapo();
                    break;
                case "originalkey":
                    mainActivityInterface.getTempSong().setKeyOriginal(editable.toString());
                    break;
                case "capo":
                    // Get rid of any new key text (e.g. convert '1 (D)' to '1')
                    String fixedcapo = editable.toString().replaceAll("\\D","").trim();
                    mainActivityInterface.getTempSong().setCapo(fixedcapo);
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
        mainActivityInterface.getTempSong().setAutoscrolllength(String.valueOf(total));
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


    // From the online search bottom sheet
    public void updateKey(String foundKey) {
        myView.key.setText(foundKey);
    }
    public void updateTempo(int tempo) {
        myView.tempo.setText(String.valueOf(tempo));
    }

    public void updateDuration(int mins, int secs) {
        myView.durationMins.setText(String.valueOf(mins));
        myView.durationSecs.setText(String.valueOf(secs));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}
