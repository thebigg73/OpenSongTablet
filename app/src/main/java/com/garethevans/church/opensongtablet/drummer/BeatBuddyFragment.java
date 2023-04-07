package com.garethevans.church.opensongtablet.drummer;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MaterialSlider;
import com.garethevans.church.opensongtablet.databinding.SettingsBeatbuddyBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;

public class BeatBuddyFragment extends Fragment {


    private MainActivityInterface mainActivityInterface;
    private SettingsBeatbuddyBinding myView;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "BeatBuddyFragment";
    private String not_set_string="", bpm_string="", folder_string="", song_string="";
    private ArrayList<String> songMessages, messageDescriptions, messageBeatBuddy;
    private SparseArray<String> beatBuddyFolder, beatBuddySong, beatBuddyTempo, beatBuddy;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsBeatbuddyBinding.inflate(inflater,container,false);

        if (getContext()!=null) {
            mainActivityInterface.updateToolbar(getString(R.string.beat_buddy));
            setupStrings();
        }
        checkExistingMessages();
        setupViews();
        return myView.getRoot();
    }

    private void setupStrings() {
            not_set_string = getString(R.string.is_not_set);
            bpm_string = getString(R.string.bpm);
            folder_string = getString(R.string.folder);
            song_string = getString(R.string.song);
    }

    private void checkExistingMessages() {
        // If we have song messages, look for ones that could belong to BeatBuddy
        songMessages = mainActivityInterface.getMidi().getSongMidiMessages();
        messageDescriptions = new ArrayList<>();
        messageBeatBuddy = new ArrayList<>();
        String folderMSB = "", folderLSB = "", song = "", drumkit = "", tempoMSB="", tempoLSB="";
        for (String item:songMessages) {
            Log.d(TAG,"item:"+item);
            String description = mainActivityInterface.getMidi().getReadableStringFromHex(item);
            String[] messageParts = mainActivityInterface.getMidi().getMessageParts();
            if (messageParts[1]!=null && messageParts[2]!=null && messageParts[3]!=null) {
                if (messageParts[2].equals("MSB")) {
                    folderMSB = messageParts[3];
                } else if (messageParts[2].equals("LSB")) {
                    folderLSB = messageParts[3];
                } else if (messageParts[1].equals("C")) {
                    song = messageParts[2];
                } else if (messageParts[1].equals("B") && messageParts[2].equals("106")) {
                    tempoMSB = messageParts[3];
                } else if (messageParts[1].equals("B") && messageParts[2].equals("107")) {
                    tempoLSB = messageParts[3];
                } else if (messageParts[1].equals("B") && messageParts[2].equals("116")) {
                    drumkit = messageParts[3];
                }
            }



        }
        if (!tempoMSB.isEmpty() && !tempoLSB.isEmpty()) {
            int bpm = (Integer.parseInt(tempoMSB)*128) + Integer.parseInt(tempoLSB);
            Log.d(TAG, "Tempo:" + bpm);
            myView.songTempo.setValue(bpm);
        }
        if (!folderMSB.isEmpty() && !folderLSB.isEmpty()) {
            Log.d(TAG, "Folder:" + ((Integer.parseInt(folderMSB)*128) + Integer.parseInt(folderLSB) + 1));
        }
        if (!song.isEmpty()) {
            Log.d(TAG, "Song:" + (Integer.parseInt(song)+1));
        }
        if (!drumkit.isEmpty()) {
            Log.d(TAG, "Drum kit:" + (Integer.parseInt(drumkit)+1));
        }
    }
    private void setupViews() {
        myView.includeSong.setChecked(mainActivityInterface.getBeatBuddy().getBeatBuddyIncludeSong());
        myView.includeSongLayout.setVisibility(
                mainActivityInterface.getBeatBuddy().getBeatBuddyIncludeSong() ? View.VISIBLE:View.GONE);
        myView.includeSong.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mainActivityInterface.getBeatBuddy().setBeatBuddyIncludeSong(isChecked);
                myView.includeSongLayout.setVisibility(isChecked ? View.VISIBLE:View.GONE);
            }
        });


        initialiseSlider(myView.beatBuddyChannel,"beatBuddyChannel",
                mainActivityInterface.getBeatBuddy().getBeatBuddyChannel(),"");
        initialiseSlider(myView.beatBuddyHeadphones, "beatBuddyVolume",
                mainActivityInterface.getBeatBuddy().getBeatBuddyHeadphones(),"%");
        initialiseSlider(myView.beatBuddyVolume, "beatBuddyHeadphones",
                mainActivityInterface.getBeatBuddy().getBeatBuddyVolume(),"%");
        initialiseSlider(myView.songTempo, "songTempo",
                getSongTempoForBeatBuddy(),bpm_string);
        initialiseSlider(myView.midiDelay, "midiDelay",
                mainActivityInterface.getMidi().getMidiDelay(),"ms");


        myView.beatBuddyChannel.setAdjustableButtons(true);
        myView.beatBuddyVolume.setAdjustableButtons(true);
        myView.beatBuddyHeadphones.setAdjustableButtons(true);
        myView.songTempo.setAdjustableButtons(true);
        myView.midiDelay.setAdjustableButtons(true);
    }

    private int getSongTempoForBeatBuddy() {
        String tempo = mainActivityInterface.getSong().getTempo();
        if (tempo==null) {
            tempo = "";
        }
        tempo = tempo.replaceAll("\\D","");
        if (tempo.isEmpty()) {
            return 39;
        } else {
            int t = Integer.parseInt(tempo);
            if (t<40 || t>300) {
                return 39;
            } else {
                return t;
            }
        }
    }

    private void initialiseSlider(MaterialSlider slider, String prefName, int value, String labelEnd) {
        if (prefName.equals("songTempo") && (value<40 || value>300)) {
            slider.setValue(39); // The off position
            slider.setHint(not_set_string);
            mainActivityInterface.getSong().setTempo("");
        } else {
            slider.setHint(value + labelEnd);
            slider.setValue(value);
        }
        slider.setLabelFormatter(value1 -> {
            if (prefName.equals("songTempo") && (value1<40||value1>300)) {
                return not_set_string;
            } else {
                return (int) value1 + labelEnd;
            }
        });
        slider.addOnSliderTouchListener(new MyOnSliderTouchListener(prefName));
        slider.addOnChangeListener(new MyOnChangeListener(slider, prefName, labelEnd));
    }

    private class MyOnSliderTouchListener implements Slider.OnSliderTouchListener {

        String prefName;
        MyOnSliderTouchListener(String prefName) {
            this.prefName = prefName;
        }

        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {}

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            int value = (int)slider.getValue();
            switch (prefName) {
                case "beatBuddyChannel":
                    mainActivityInterface.getBeatBuddy().setBeatBuddyChannel(value);
                    break;

                case "beatBuddyVolume":
                    mainActivityInterface.getBeatBuddy().setBeatBuddyVolume(value);
                    break;

                case "beatBuddyHeadphones":
                    mainActivityInterface.getBeatBuddy().setBeatBuddyHeadphones(value);
                    break;

                case "songTempo":
                    if (value==39) {
                        mainActivityInterface.getSong().setTempo("");
                    } else {
                        mainActivityInterface.getSong().setTempo(""+value);
                    }
                    mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),false);
                    mainActivityInterface.getBeatBuddy().setBeatBuddyTempo(value);
                    break;

                case "midiDelay":
                    mainActivityInterface.getMidi().setMidiDelay(value);
                    break;
            }
        }
    }

    private class MyOnChangeListener implements Slider.OnChangeListener {
        MaterialSlider materialSlider;
        String prefName;
        String labelEnd;
        MyOnChangeListener(MaterialSlider materialSlider, String prefName, String labelEnd) {
            this.materialSlider = materialSlider;
            this.prefName = prefName;
            this.labelEnd = labelEnd;
        }

        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            if (prefName.equals("songTempo") && (value<40 || value>300)) {
                materialSlider.setHint(not_set_string);
                if (!fromUser) {
                    mainActivityInterface.getSong().setTempo("");
                    mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),false);
                }
            } else if (prefName.equals("songTempo") && !fromUser) {
                materialSlider.setHint(((int) value) + labelEnd);
                mainActivityInterface.getSong().setTempo(value+"");
                mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),false);
            } else {
                // Just set the hint
                materialSlider.setHint(((int) value) + labelEnd);
            }
        }
    }
}
