package com.garethevans.church.opensongtablet.beatbuddy;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.customviews.MaterialSlider;
import com.garethevans.church.opensongtablet.databinding.SettingsBeatbuddyCommandsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.midi.MidiInfo;
import com.garethevans.church.opensongtablet.midi.MidiItemTouchHelper;
import com.garethevans.church.opensongtablet.midi.MidiMessagesAdapter;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BBCommandsFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsBeatbuddyCommandsBinding myView;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "BeatBuddyFragment";
    private String not_set_string="", bpm_string="", folder_string="", song_string="", channel_string="",
        success_string="", tempo_string="", volume_string="", unknown_string="", drumkit_string="",
            headphone_volume_string="", web_string="", playlist_string="", beat_buddy_string="";
    private ArrayList<String> messageDescriptions;
    private ArrayList<String> messageBeatBuddy;
    private String songCommand, tempoCommand, volumeCommand, volumeHPCommand, drumKitCommand, beatBuddyCommands;
    private int fromSongMessages_channel, fromSongMessages_folderMSB, fromSongMessages_folderLSB,
            fromSongMessages_songPC, fromSongMessages_tempoMSB, fromSongMessages_tempoLSB,
            fromSongMessages_volumeCC, fromSongMessages_volumeHPCC, fromSongMessages_drumKitCC;
    private BBSQLite bbsqLite;
    private String searchAerosFolder, searchAerosSong, searchDrumKit;
    private String webAddress;

    private MidiMessagesAdapter midiMessagesAdapter = null;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(beat_buddy_string + ": "+getString(R.string.midi_commands));
        mainActivityInterface.updateToolbarHelp(webAddress);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsBeatbuddyCommandsBinding.inflate(inflater,container,false);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            if (getContext()!=null) {
                bbsqLite = new BBSQLite(getContext());
                myView.currentSongMessages.post(() -> myView.currentSongMessages.setLayoutManager(new LinearLayoutManager(getContext())));
                midiMessagesAdapter = new MidiMessagesAdapter(getContext());
            }

            prepareStrings();

            webAddress = web_string;

            checkExistingMessages();
            setupViews();
            checkBeatBuddyValues();
            checkTempoTimeSig();
        });

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            web_string = getString(R.string.website_beatbuddy_commands);
            not_set_string = getString(R.string.is_not_set);
            bpm_string = getString(R.string.bpm);
            folder_string = getString(R.string.folder);
            song_string = getString(R.string.song);
            success_string = getString(R.string.success);
            channel_string = getString(R.string.midi_channel);
            tempo_string = getString(R.string.tempo);
            volume_string = getString(R.string.volume);
            headphone_volume_string = getString(R.string.volume_headphone);
            unknown_string = getString(R.string.unknown);
            drumkit_string = getString(R.string.drum_kit);
            playlist_string = getString(R.string.playlist);
            beat_buddy_string = getString(R.string.beat_buddy);
            searchAerosSong = bbsqLite.COLUMN_FOLDER_NUM + "=? AND " + bbsqLite.COLUMN_SONG_NUM + "=?";
            searchAerosFolder = bbsqLite.COLUMN_FOLDER_NUM + "=?";
            searchDrumKit = bbsqLite.COLUMN_KIT_NUM + "=?";
        }
    }

    private void checkExistingMessages() {
        // If we have song messages, look for ones that could belong to BeatBuddy
        mainActivityInterface.getMidi().buildSongMidiMessages();
        ArrayList<String> songMessages = mainActivityInterface.getMidi().getSongMidiMessages();
        messageDescriptions = new ArrayList<>();
        messageBeatBuddy = new ArrayList<>();
        fromSongMessages_channel=-1;
        fromSongMessages_folderMSB = -1;
        fromSongMessages_folderLSB = -1;
        fromSongMessages_songPC = -1;
        fromSongMessages_tempoMSB = -1;
        fromSongMessages_tempoLSB = -1;
        fromSongMessages_volumeCC = -1;
        fromSongMessages_volumeHPCC = -1;
        fromSongMessages_drumKitCC = -1;

        for (String item: songMessages) {
            if (!item.trim().isEmpty()) {
                String channelLSB = "";
                String folderMSB = "";
                String folderLSB = "";
                String songPC = "";
                String drumkitCC = "";
                String volumeCC = "";
                String volumeHPCC = "";
                String tempoMSB = "";
                String tempoLSB = "";
                // Make message parts
                mainActivityInterface.getMidi().getReadableStringFromHex(item);
                String[] messageParts = mainActivityInterface.getMidi().getMessageParts();
                if (messageParts[1] != null && messageParts[2] != null && messageParts[3] != null) {
                    if (messageParts[2].equals("MSB")) {
                        channelLSB = messageParts[0];
                        folderMSB = messageParts[3];
                    } else if (messageParts[2].equals("LSB")) {
                        channelLSB = messageParts[0];
                        folderLSB = messageParts[3];
                    } else if (messageParts[1].equals("C")) {
                        channelLSB = messageParts[0];
                        songPC = messageParts[2];
                    } else if (messageParts[1].equals("B") && messageParts[2].equals(""+mainActivityInterface.getBeatBuddy().getCC_Tempo_MSB())) {
                        channelLSB = messageParts[0];
                        tempoMSB = messageParts[3];
                    } else if (messageParts[1].equals("B") && messageParts[2].equals(""+mainActivityInterface.getBeatBuddy().getCC_Tempo_LSB())) {
                        channelLSB = messageParts[0];
                        tempoLSB = messageParts[3];
                    } else if (messageParts[1].equals("B") && messageParts[2].equals(""+mainActivityInterface.getBeatBuddy().getCC_Drum_kit())) {
                        channelLSB = messageParts[0];
                        drumkitCC = messageParts[3];
                    } else if (messageParts[1].equals("B") && messageParts[2].equals(""+mainActivityInterface.getBeatBuddy().getCC_Mix_vol())) {
                        channelLSB = messageParts[0];
                        volumeCC = messageParts[3];
                    } else if (messageParts[1].equals("B") && messageParts[2].equals(""+mainActivityInterface.getBeatBuddy().getCC_HP_vol())) {
                        channelLSB = messageParts[0];
                        volumeHPCC = messageParts[3];
                    } else {
                        channelLSB = messageParts[0];
                    }
                }

                String channelMessage = "";
                if (!channelLSB.isEmpty()) {
                    fromSongMessages_channel = Integer.parseInt(channelLSB);
                    channelMessage = channel_string + ": " + fromSongMessages_channel + "\n";
                }

                String folderMSBMessage = "";
                if (!folderMSB.isEmpty()) {
                    fromSongMessages_folderMSB = Integer.parseInt(folderMSB);
                    folderMSBMessage = folder_string + " (MSB):" + (
                            fromSongMessages_folderMSB * 128) + " (*128)";
                }

                String folderLSBMessage = "";
                if (!folderLSB.isEmpty()) {
                    fromSongMessages_folderLSB = Integer.parseInt(folderLSB);
                    int val = fromSongMessages_folderLSB;
                    if (mainActivityInterface.getBeatBuddy().getBeatBuddyAerosMode() && val>111) {
                        folderLSBMessage = playlist_string + " (LSB):" + (fromSongMessages_folderLSB - 111 + 1);
                    } else {
                        folderLSBMessage = folder_string + " (LSB):" + (fromSongMessages_folderLSB + 1);
                    }
                }

                String songPCMessage = "";
                if (!songPC.isEmpty()) {
                    fromSongMessages_songPC = Integer.parseInt(songPC);
                    songPCMessage = song_string + " (PC):" + (fromSongMessages_songPC + 1);
                }

                String tempoMSBMessage = "";
                if (!tempoMSB.isEmpty()) {
                    fromSongMessages_tempoMSB = Integer.parseInt(tempoMSB);
                    tempoMSBMessage = tempo_string + " (MSB):" + (fromSongMessages_tempoMSB * 128 + " (*128)");
                }

                String tempoLSBMessage = "";
                if (!tempoLSB.isEmpty()) {
                    fromSongMessages_tempoLSB = Integer.parseInt(tempoLSB);
                    tempoLSBMessage = tempo_string + " (LSB):" + fromSongMessages_tempoLSB;
                }

                String volumeCCMessage = "";
                if (!volumeCC.isEmpty()) {
                    fromSongMessages_volumeCC = Integer.parseInt(volumeCC);
                    volumeCCMessage = volume_string + " (CC):" + fromSongMessages_volumeCC;
                }

                String volumeHPCCMessage = "";
                if (!volumeHPCC.isEmpty()) {
                    fromSongMessages_volumeHPCC = Integer.parseInt(volumeHPCC);
                    volumeHPCCMessage = headphone_volume_string + " (CC):" + fromSongMessages_volumeHPCC;
                }

                String drumKitCCMessage = "";
                if (!drumkitCC.isEmpty()) {
                    fromSongMessages_drumKitCC = Integer.parseInt(drumkitCC);
                    drumKitCCMessage = drumkit_string + " (CC):" + (fromSongMessages_drumKitCC + 1);
                }

                String known_message = folderMSBMessage + folderLSBMessage + songPCMessage +
                        tempoMSBMessage + tempoLSBMessage + volumeCCMessage + volumeHPCCMessage +
                        drumKitCCMessage;
                if (known_message.trim().isEmpty()) {
                    known_message = unknown_string;
                }

                known_message = channelMessage + known_message;
                messageBeatBuddy.add(known_message);
                messageDescriptions.add(item);
            }
        }
    }

    private void setupViews() {
        // All view setups are done as post (for UI)
        // Make the FABs hide/show with scroll
        updateSongCommand();

        // Auto send BB commands (dynamic)
        myView.beatBuddyAutoLookup.post(() -> {
            myView.beatBuddyAutoLookup.setChecked(mainActivityInterface.getBeatBuddy().getBeatBuddyAutoLookup());
            myView.beatBuddyAutoLookup.setOnCheckedChangeListener((compoundButton, b) -> mainActivityInterface.getBeatBuddy().setBeatBuddyAutoLookup(b));
        });

        // Auto send song MIDI messages commands (static)
        myView.autoSendMidi.post(() -> {
            myView.autoSendMidi.setChecked(mainActivityInterface.getMidi().getMidiSendAuto());
            myView.autoSendMidi.setOnCheckedChangeListener((compoundButton, b) -> mainActivityInterface.getMidi().setMidiSendAuto(b));
        });

        // Set the input method to numbers only.  Not using sliders as the folder can be huge number!
        myView.songFolder.post(() -> {
            myView.songFolder.setInputType(InputType.TYPE_CLASS_NUMBER);
            myView.songFolder.addTextChangedListener(new SongCommandChange());
        });

        myView.songNumber.post(() -> {
            myView.songNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
            myView.songNumber.addTextChangedListener(new SongCommandChange());
        });

        myView.beatBuddyUseImported.post(() -> {
                    myView.beatBuddyUseImported.setChecked(mainActivityInterface.getBeatBuddy().getBeatBuddyUseImported());
                    myView.beatBuddyUseImported.setOnCheckedChangeListener((compoundButton, b) -> {
                        mainActivityInterface.getBeatBuddy().setBeatBuddyUseImported(b);
                        setSliderHintText(myView.aerosFolder, null, searchAerosFolder, true,
                                bbsqLite.COLUMN_FOLDER_NAME, (int) myView.aerosFolder.getValue(), -1);
                        setSliderHintText(myView.aerosSong, song_string, searchAerosSong, true,
                                bbsqLite.COLUMN_SONG_NAME, (int) myView.aerosFolder.getValue(), (int) myView.aerosSong.getValue());
                        setSliderHintText(myView.drumKit, drumkit_string, searchDrumKit, false,
                                bbsqLite.COLUMN_KIT_NAME, (int) myView.drumKit.getValue(), -1);
                        checkBeatBuddyValues();
                    });
                });

        // If we are including song details
        myView.includeSong.post(() -> {
            myView.includeSong.setChecked(mainActivityInterface.getBeatBuddy().getBeatBuddyIncludeSong());
            myView.includeSong.setOnCheckedChangeListener((buttonView, isChecked) -> {
                mainActivityInterface.getBeatBuddy().setBeatBuddyIncludeSong(isChecked);
                myView.includeSongLayout.setVisibility(isChecked &&
                        !mainActivityInterface.getBeatBuddy().getBeatBuddyAerosMode()? View.VISIBLE:View.GONE);
                myView.aerosSliders.setVisibility(isChecked &&
                        mainActivityInterface.getBeatBuddy().getBeatBuddyAerosMode()? View.VISIBLE:View.GONE);
                myView.aerosMode.setVisibility(isChecked? View.VISIBLE:View.GONE);
                myView.songBrowser.setVisibility(isChecked? View.VISIBLE:View.GONE);
                updateSongCommand();
                myView.includeSongErrors.setVisibility(isChecked &&
                        !mainActivityInterface.getBeatBuddy().getBeatBuddyAerosMode() ? View.VISIBLE:View.GONE);
            });
        });

        myView.aerosMode.post(() -> {
            myView.aerosMode.setChecked(mainActivityInterface.getBeatBuddy().getBeatBuddyAerosMode());
            myView.aerosMode.setVisibility(mainActivityInterface.getBeatBuddy().getBeatBuddyIncludeSong()?View.VISIBLE:View.GONE);
            myView.aerosMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                mainActivityInterface.getBeatBuddy().setBeatBuddyAerosMode(isChecked);
                // Update any existing song messages
                checkExistingMessages();
                updateRecyclerView();
                myView.aerosSliders.setVisibility(isChecked &&
                        mainActivityInterface.getBeatBuddy().getBeatBuddyIncludeSong() ? View.VISIBLE:View.GONE);
                myView.includeSongLayout.setVisibility(!isChecked &&
                        mainActivityInterface.getBeatBuddy().getBeatBuddyIncludeSong() ? View.VISIBLE:View.GONE);
                myView.includeSongErrors.setVisibility(!isChecked &&
                        mainActivityInterface.getBeatBuddy().getBeatBuddyIncludeSong() ? View.VISIBLE:View.GONE);
            });
        });

        // When in normal mode, show the text input for Folder (1-128^2) and Song (1-128)
        myView.includeSongLayout.post(() -> myView.includeSongLayout.setVisibility(
                mainActivityInterface.getBeatBuddy().getBeatBuddyIncludeSong() &&
                        !mainActivityInterface.getBeatBuddy().getBeatBuddyAerosMode()? View.VISIBLE:View.GONE));
        // When in Aeros mode, show the sliders for Folder/Playlist and Song
        myView.aerosSliders.post(() -> myView.aerosSliders.setVisibility(
                mainActivityInterface.getBeatBuddy().getBeatBuddyIncludeSong() &&
                        mainActivityInterface.getBeatBuddy().getBeatBuddyAerosMode()? View.VISIBLE:View.GONE));
        myView.songBrowser.post(() -> {
            myView.songBrowser.setVisibility(mainActivityInterface.getBeatBuddy().getBeatBuddyIncludeSong()?View.VISIBLE:View.GONE);
            myView.songBrowser.setOnClickListener(view -> buildBBDefaultSongs());
        });

        myView.includeSongErrors.post(() -> myView.includeSongErrors.setVisibility(mainActivityInterface.getBeatBuddy().getBeatBuddyIncludeSong() &&
                !mainActivityInterface.getBeatBuddy().getBeatBuddyAerosMode() ? View.VISIBLE:View.GONE));

        // Include volume
        myView.includeVolume.post(() -> {
            myView.includeVolume.setChecked(mainActivityInterface.getBeatBuddy().getBeatBuddyIncludeVolume());
            myView.includeVolume.setOnCheckedChangeListener((buttonView, isChecked) -> {
                mainActivityInterface.getBeatBuddy().setBeatBuddyIncludeVolume(isChecked);
                myView.beatBuddyVolume.setVisibility(isChecked ? View.VISIBLE:View.GONE);
                updateVolumeCommand();
            });
        });
        myView.beatBuddyVolume.post(() -> myView.beatBuddyVolume.setVisibility(
                mainActivityInterface.getBeatBuddy().getBeatBuddyIncludeVolume() ? View.VISIBLE:View.GONE));

        myView.includeHPVolume.post(() -> {
            myView.includeHPVolume.setChecked(mainActivityInterface.getBeatBuddy().getBeatBuddyIncludeHPVolume());
            myView.includeHPVolume.setOnCheckedChangeListener((buttonView, isChecked) -> {
                mainActivityInterface.getBeatBuddy().setBeatBuddyIncludeHPVolume(isChecked);
                myView.beatBuddyHPVolume.setVisibility(isChecked ? View.VISIBLE:View.GONE);
                updateVolumeHPCommand();
            });
        });
        myView.beatBuddyHPVolume.post(() -> myView.beatBuddyHPVolume.setVisibility(
                mainActivityInterface.getBeatBuddy().getBeatBuddyIncludeHPVolume() ? View.VISIBLE:View.GONE));


        // Include tempo change
        myView.includeTempo.post(() -> {
            myView.includeTempo.setChecked(mainActivityInterface.getBeatBuddy().getBeatBuddyIncludeTempo());
            myView.includeTempo.setOnCheckedChangeListener((buttonView, isChecked) -> {
                mainActivityInterface.getBeatBuddy().setBeatBuddyIncludeTempo(isChecked);
                myView.songTempo.setVisibility(isChecked ? View.VISIBLE:View.GONE);
            });
        });
        myView.songTempo.post(() -> myView.songTempo.setVisibility(
                mainActivityInterface.getBeatBuddy().getBeatBuddyIncludeTempo() ? View.VISIBLE:View.GONE));


        // Include drum kit
        myView.includeDrumKit.post(() -> {
            myView.includeDrumKit.setChecked(mainActivityInterface.getBeatBuddy().getBeatBuddyIncludeDrumKit());
            myView.includeDrumKit.setOnCheckedChangeListener((buttonView, isChecked) -> {
                mainActivityInterface.getBeatBuddy().setBeatBuddyIncludeDrumKit(isChecked);
                myView.drumKit.setVisibility(isChecked ? View.VISIBLE:View.GONE);
            });
        });
        myView.drumKit.post(() -> myView.drumKit.setVisibility(
                mainActivityInterface.getBeatBuddy().getBeatBuddyIncludeDrumKit() ? View.VISIBLE:View.GONE));


        // Initialise the sliders, values, hints and listeners
        // Enable the + / - adjustment buttons for fine tuning
        // Update values with existing BeatBuddy MIDI commands (last identified)
        myView.beatBuddyChannel.post(() -> {
            initialiseSlider(myView.beatBuddyChannel,"beatBuddyChannel",
                    mainActivityInterface.getBeatBuddy().getBeatBuddyChannel(),"");
            myView.beatBuddyChannel.setAdjustableButtons(true);
            if (fromSongMessages_channel>-1) {
                myView.beatBuddyChannel.setValue(fromSongMessages_channel);
                mainActivityInterface.getBeatBuddy().setBeatBuddyChannel(fromSongMessages_channel);
            }
        });

        myView.beatBuddyVolume.post(() -> {
            initialiseSlider(myView.beatBuddyVolume, "beatBuddyVolume",
                    mainActivityInterface.getBeatBuddy().getBeatBuddyVolume(),"%");
            myView.beatBuddyVolume.setAdjustableButtons(true);
            if (fromSongMessages_volumeCC>-1) {
                int vol = fromSongMessages_volumeCC;
                myView.beatBuddyVolume.setValue(vol);
            }
        });

        myView.beatBuddyHPVolume.post(() -> {
            initialiseSlider(myView.beatBuddyHPVolume, "beatBuddyHPVolume",
                    mainActivityInterface.getBeatBuddy().getBeatBuddyHPVolume(),"%");
            myView.beatBuddyHPVolume.setAdjustableButtons(true);
            if (fromSongMessages_volumeHPCC>-1) {
                int hpvol = fromSongMessages_volumeHPCC;
                myView.beatBuddyHPVolume.setValue(hpvol);
            }
        });

        myView.songTempo.post(() -> {
            initialiseSlider(myView.songTempo, "songTempo",
                    getSongTempoForBeatBuddy(),bpm_string);
            myView.songTempo.setAdjustableButtons(true);
            if (fromSongMessages_tempoMSB>-1 && fromSongMessages_tempoLSB>-1) {
                int bpm = (fromSongMessages_tempoMSB*128) + fromSongMessages_tempoLSB;
                // Set this tempo
                myView.songTempo.setValue(bpm);
            }
        });

        myView.midiDelay.post(() -> {
            initialiseSlider(myView.midiDelay, "midiDelay",
                    mainActivityInterface.getMidi().getMidiDelay(),"ms");
            myView.midiDelay.setAdjustableButtons(true);
        });

        myView.drumKit.post(() -> {
            initialiseSlider(myView.drumKit, "beatBuddyDrumKit",
                    mainActivityInterface.getBeatBuddy().getBeatBuddyDrumKit(),"");
            myView.drumKit.setAdjustableButtons(true);
            if (fromSongMessages_drumKitCC>-1) {
                int kit = fromSongMessages_drumKitCC+1;
                myView.drumKit.setValue(kit);
            }
        });

        myView.aerosFolder.post(() -> {
            initialiseSlider(myView.aerosFolder, "aerosFolder",
                    1,"");
            myView.aerosFolder.setAdjustableButtons(true);
            if (!mainActivityInterface.getBeatBuddy().getBeatBuddyAerosMode() &&
                    fromSongMessages_folderMSB>-1 && fromSongMessages_folderLSB>-1) {
                int folder = (fromSongMessages_folderMSB*128) + fromSongMessages_folderLSB + 1;
                myView.songFolder.setText(""+folder);
            } else if (mainActivityInterface.getBeatBuddy().getBeatBuddyAerosMode() &&
                    fromSongMessages_folderLSB>-1) {
                int folder = fromSongMessages_folderLSB + 1;
                if (folder<=128) {
                    myView.aerosFolder.setValue(folder);
                }
            }
        });

        myView.aerosSong.post(() -> {
            initialiseSlider(myView.aerosSong, "aerosSong",
                    1,"");
            myView.aerosSong.setAdjustableButtons(true);
            if (fromSongMessages_songPC>-1) {
                int song = fromSongMessages_songPC+1;
                if (!mainActivityInterface.getBeatBuddy().getBeatBuddyAerosMode()) {
                    myView.songNumber.setText("" + song);
                } else {
                    myView.aerosSong.setValue(song);
                }
            }
        });


        myView.testSongCode.post(() -> myView.testSongCode.setOnClickListener(view -> {
            updateMidiCommands();
            if (!beatBuddyCommands.isEmpty()) {
                mainActivityInterface.getMidi().sendMidiHexSequence(beatBuddyCommands);
            }
        }));

        myView.addMidiCommands.post(() -> myView.addMidiCommands.setOnClickListener(view -> {
            updateMidiCommands();
            // Now save to the song
            saveMessagesToSong();
            updateRecyclerView();
        }));

        myView.currentSongMessages.post(() -> {
            if (midiMessagesAdapter!=null && myView!=null) {
                myView.currentSongMessages.setLayoutManager(new LinearLayoutManager(getContext()));
                ItemTouchHelper.Callback callback = new MidiItemTouchHelper(midiMessagesAdapter,true);
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
                midiMessagesAdapter.setTouchHelper(itemTouchHelper);
                myView.currentSongMessages.setAdapter(midiMessagesAdapter);
                itemTouchHelper.attachToRecyclerView(myView.currentSongMessages);
                updateRecyclerView();
            }
        });

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
        if (prefName!=null && prefName.equals("songTempo") && (value<40 || value>300)) {
            slider.setValue(39); // The off position
            slider.setHint(not_set_string);
            mainActivityInterface.getSong().setTempo("");
        } else if (prefName!=null && prefName.equals("aerosFolder")) {
            setSliderHintText(myView.aerosFolder,null,searchAerosFolder,true,
                    bbsqLite.COLUMN_FOLDER_NAME,value,-1);
        } else if (prefName!=null && prefName.equals("aerosSong")) {
            setSliderHintText(myView.aerosSong,song_string,searchAerosSong,true,
                    bbsqLite.COLUMN_SONG_NAME,(int)myView.aerosFolder.getValue(),value);
            slider.setValue(value);
        } else if (prefName!=null && prefName.equals("beatBuddyDrumKit")) {
            setSliderHintText(myView.drumKit, drumkit_string, searchDrumKit, false,
                    bbsqLite.COLUMN_KIT_NAME, value, -1);
        } else {
            slider.setHint(value + labelEnd);
            slider.setValue(value);
        }
        slider.setLabelFormatter(value1 -> {
            if (prefName!=null && prefName.equals("songTempo") && (value1<40||value1>300)) {
                return not_set_string;
            } else if (prefName!=null && prefName.equals("aerosFolder")) {
                if (value > 111) {
                    return playlist_string+"\n"+(int)(value1-111);
                } else {
                    return folder_string + "\n" + (int)value1;
                }
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
            if (prefName!=null) {
                switch (prefName) {
                    case "beatBuddyChannel":
                        mainActivityInterface.getBeatBuddy().setBeatBuddyChannel(value);
                        break;

                    case "beatBuddyVolume":
                        mainActivityInterface.getBeatBuddy().setBeatBuddyVolume(value);
                        break;

                    case "beatBuddyHPVolume":
                        mainActivityInterface.getBeatBuddy().setBeatBuddyHPVolume(value);
                        break;

                    case "songTempo":
                        if (value == 39) {
                            mainActivityInterface.getSong().setTempo("");
                        } else {
                            mainActivityInterface.getSong().setTempo("" + value);
                        }
                        mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(), false);
                        break;

                    case "midiDelay":
                        mainActivityInterface.getMidi().setMidiDelay(value);
                        break;

                    case "beatBuddyDrumKit":
                        mainActivityInterface.getBeatBuddy().setBeatBuddyDrumKit(value);
                        break;

                    case "aerosFolder":
                    case "aerosSong":
                        // Don't need to make any global changes
                        break;
                }
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
            if (prefName!=null && prefName.equals("songTempo") && (value<40 || value>300)) {
                materialSlider.setHint(not_set_string);
                if (!fromUser) {
                    mainActivityInterface.getSong().setTempo("");
                    mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),false);
                }
            } else if (prefName!=null && prefName.equals("songTempo") && !fromUser) {
                materialSlider.setHint(((int) value) + labelEnd);
                mainActivityInterface.getSong().setTempo(value+"");
                mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),false);
            } else if (prefName!=null && prefName.equals("aerosFolder")) {
                setSliderHintText(myView.aerosFolder, null, searchAerosFolder, true,
                        bbsqLite.COLUMN_FOLDER_NAME, (int) value, -1);
                setSliderHintText(myView.aerosSong,song_string,searchAerosSong,true,
                        bbsqLite.COLUMN_SONG_NAME,(int)value,(int)myView.aerosSong.getValue());
                slider.setValue(value);

            } else if (prefName!=null && prefName.equals("aerosSong")) {
                setSliderHintText(myView.aerosSong, song_string, searchAerosSong, true,
                        bbsqLite.COLUMN_SONG_NAME, (int) myView.aerosFolder.getValue(), (int) value);

            } else if (prefName!=null && prefName.equals("beatBuddyDrumKit")) {
                setSliderHintText(myView.drumKit, drumkit_string, searchDrumKit, false,
                        bbsqLite.COLUMN_KIT_NAME, (int) value, -1);
            } else {
                // Just set the hint
                materialSlider.setHint(((int) value) + labelEnd);
            }


            // Update the ones that are user prefs that aren't checked on save
            if (prefName.equals("beatBuddyChannel")) {
                // Check the channel (may have been from the +/- buttons
                mainActivityInterface.getBeatBuddy().setBeatBuddyChannel((int)value);
            } else if (prefName.equals("midiDelay")) {
                mainActivityInterface.getMidi().setMidiDelay((int)value);
            }
        }
    }
    private void setSliderHintText(MaterialSlider slider, String prefix, String querySearch ,
                                   boolean songs, String getColumn, int value1, int value2) {
        String hint;
        String table;
        if (songs) {
            table = mainActivityInterface.getBeatBuddy().getBeatBuddyUseImported() ?
                    bbsqLite.TABLE_NAME_MY_SONGS:bbsqLite.TABLE_NAME_DEFAULT_SONGS;
        } else {
            table = mainActivityInterface.getBeatBuddy().getBeatBuddyUseImported() ?
                    bbsqLite.TABLE_NAME_MY_DRUMS:bbsqLite.TABLE_NAME_DEFAULT_DRUMS;
        }
        querySearch = "SELECT DISTINCT " + getColumn + " FROM " + table +" WHERE " + querySearch;
        String[] args;
        int value = value1;
        if (value2!=-1) {
            args = new String[]{""+value1,""+value2};
            value = value2;
        } else {
            args = new String[]{""+value1};
        }

        if (slider==myView.aerosFolder) {
            if (value1 > 111) {
                hint = playlist_string + " " + (value - 111);
            } else {
                hint = folder_string + " " + value;
            }
        } else {
            hint = prefix + " " + value;
        }

        String lookedUpHint = bbsqLite.lookupValue(getColumn, querySearch, args);
        if (!lookedUpHint.isEmpty()) {
            lookedUpHint = ": "+lookedUpHint;
        }
        hint = hint.trim();
        slider.setHint(hint+lookedUpHint);
    }

    private class SongCommandChange implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            updateSongCommand();
        }
    }

    private void updateMidiCommands() {
        updateSongCommand();
        updateTempoCommand();
        updateVolumeCommand();
        updateVolumeHPCommand();
        updateDrumKitCommand();
        beatBuddyCommands = "";
        if (!songCommand.isEmpty()) {
            beatBuddyCommands += songCommand + "\n";
        }
        if (!volumeCommand.isEmpty()) {
            beatBuddyCommands += volumeCommand + "\n";
        }
        if (!volumeHPCommand.isEmpty()) {
            beatBuddyCommands += volumeHPCommand + "\n";
        }
        if (!tempoCommand.isEmpty()) {
            beatBuddyCommands += tempoCommand + "\n";
        }
        if (!drumKitCommand.isEmpty()) {
            beatBuddyCommands += drumKitCommand + "\n";
        }
        beatBuddyCommands = beatBuddyCommands.trim();
    }

    private void updateSongCommand() {
        songCommand = "";
        int songFolderNum = -1;
        int songNum = -1;
        boolean songFolderOk = false;
        boolean songNumberOk = false;

        // Get the song folder and song number
        if (myView.includeSong.getChecked()) {
            if (mainActivityInterface.getBeatBuddy().getBeatBuddyAerosMode()) {
                songFolderNum = (int)myView.aerosFolder.getValue();
                songNum = (int)myView.aerosSong.getValue();
                songFolderOk = true;
                songNumberOk = true;
            } else {
                if (myView.songFolder.getText() != null && !myView.songFolder.getText().toString().isEmpty()) {
                    String songFolder = myView.songFolder.getText().toString().replaceAll("\\D", "");
                    try {
                        songFolderNum = Integer.parseInt(songFolder);
                        if (songFolderNum >= 1 && songFolderNum <= 16384) {
                            songFolderOk = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (myView.songNumber.getText() != null && !myView.songNumber.getText().toString().isEmpty()) {
                    String songNumber = myView.songNumber.getText().toString().replaceAll("\\D", "");
                    try {
                        songNum = Integer.parseInt(songNumber);
                        if (songNum >= 1 && songNum <= 128) {
                            songNumberOk = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            myView.songFolderError.setVisibility(!songFolderOk &&
                    !mainActivityInterface.getBeatBuddy().getBeatBuddyAerosMode()? View.VISIBLE : View.GONE);
            myView.songNumberError.setVisibility(!songNumberOk &&
                    !mainActivityInterface.getBeatBuddy().getBeatBuddyAerosMode()? View.VISIBLE : View.GONE);

            if (songFolderOk && songNumberOk) {
                songCommand = mainActivityInterface.getBeatBuddy().getSongCode(songFolderNum, songNum);
            } else {
                songCommand = "";
            }
        }
    }

    private void updateTempoCommand() {
        if (myView.includeTempo.getChecked() && myView.songTempo.getValue()>=40) {
            tempoCommand = mainActivityInterface.getBeatBuddy().getTempoCode((int)myView.songTempo.getValue());
        } else {
            tempoCommand = "";
        }
    }

    private void updateVolumeCommand() {
        volumeCommand = "";
        if (myView.includeVolume.getChecked()) {
            mainActivityInterface.getBeatBuddy().setBeatBuddyVolume((int)myView.beatBuddyVolume.getValue());
            volumeCommand = mainActivityInterface.getBeatBuddy().getVolumeCode();
        }
    }
    private void updateVolumeHPCommand() {
        volumeHPCommand = "";
        if (myView.includeHPVolume.getChecked()) {
            mainActivityInterface.getBeatBuddy().setBeatBuddyHPVolume((int)myView.beatBuddyHPVolume.getValue());
            volumeHPCommand = mainActivityInterface.getBeatBuddy().getVolumeHPCode();
        }
    }

    private void updateDrumKitCommand() {
        drumKitCommand = "";
        if (myView.includeDrumKit.getChecked()) {
            mainActivityInterface.getBeatBuddy().setBeatBuddyDrumKit((int)myView.drumKit.getValue());
            drumKitCommand = mainActivityInterface.getBeatBuddy().getDrumKitCode();
        }
    }

    private void saveMessagesToSong() {
        String currentMidiSongMessages = mainActivityInterface.getSong().getMidi().trim();
        if (myView.includeSong.getChecked() && !songCommand.isEmpty()) {
            // Add the song
            currentMidiSongMessages += "\n" + songCommand;
        }
        currentMidiSongMessages = currentMidiSongMessages.trim();

        if (myView.includeTempo.getChecked() && !tempoCommand.isEmpty()) {
            // Add the tempo
            currentMidiSongMessages += "\n" + tempoCommand;
        }
        currentMidiSongMessages = currentMidiSongMessages.trim();


        if (myView.includeVolume.getChecked() && !volumeCommand.isEmpty()) {
            // Add the volume
            currentMidiSongMessages += "\n" + volumeCommand;
        }
        currentMidiSongMessages = currentMidiSongMessages.trim();

        if (myView.includeHPVolume.getChecked() && !volumeHPCommand.isEmpty()) {
            // Add the headphone volume
            currentMidiSongMessages += "\n" + volumeHPCommand;
        }
        currentMidiSongMessages = currentMidiSongMessages.trim();


        if (myView.includeDrumKit.getChecked() && !drumKitCommand.isEmpty()) {
            // Add the drumkit
            currentMidiSongMessages += "\n" + drumKitCommand;
        }
        currentMidiSongMessages = currentMidiSongMessages.trim();

        mainActivityInterface.getSong().setMidi(currentMidiSongMessages);
        mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),false);
        mainActivityInterface.getShowToast().doIt(success_string);

        // Now update the recyclerview
        checkExistingMessages();
        updateRecyclerView();
    }

    private void updateRecyclerView() {
        ArrayList<MidiInfo> midiInfos = new ArrayList<>();
        for (int x=0; x<messageDescriptions.size(); x++) {
            MidiInfo midiInfo = new MidiInfo();
            midiInfo.midiCommand = messageDescriptions.get(x);
            midiInfo.readableCommand = messageBeatBuddy.get(x);
            midiInfos.add(midiInfo);
        }
        midiMessagesAdapter.updateMidiInfos(midiInfos);
    }

    private void buildBBDefaultSongs() {
        // Built the default BB songs from the library
        // Do this on a separate thread
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            if (bbsqLite!=null) {
                BottomSheetBeatBuddySongs bottomSheetBeatBuddySongs = new BottomSheetBeatBuddySongs(
                        BBCommandsFragment.this,bbsqLite);
                bottomSheetBeatBuddySongs.show(mainActivityInterface.getMyFragmentManager(),
                        "BottomSheetBeatBuddySongs");
            }
        });
    }

    public void changeSong(int folder, int song) {
        if (mainActivityInterface.getBeatBuddy().getBeatBuddyAerosMode()) {
            if (folder<128) {
                myView.aerosFolder.setValue(folder);
            }
            if (song<128) {
                myView.aerosSong.setValue(song);
            }
        } else {
            myView.songFolder.setText(folder+"");
            myView.songNumber.setText(song+"");
        }
    }

    private void checkBeatBuddyValues() {
        // Decide which songs and kits to use
        if (getContext()!=null) {
            try (BBSQLite bbsqLite = new BBSQLite(getContext())) {
                String tableSongs = bbsqLite.TABLE_NAME_DEFAULT_SONGS;
                String tableKits = bbsqLite.TABLE_NAME_DEFAULT_DRUMS;
                if (mainActivityInterface.getBeatBuddy().getBeatBuddyUseImported()) {
                    tableSongs = bbsqLite.TABLE_NAME_MY_SONGS;
                    tableKits = bbsqLite.TABLE_NAME_MY_DRUMS;
                }
                ArrayList<String> songs = bbsqLite.getUnique(bbsqLite.COLUMN_SONG_NAME, tableSongs);
                ArrayList<String> kits = bbsqLite.getUnique(bbsqLite.COLUMN_KIT_NAME, tableKits);

                myView.beatBuddySong.post(() -> {
                    ExposedDropDownArrayAdapter songsAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.beatBuddySong, R.layout.view_exposed_dropdown_item, songs);
                    myView.beatBuddySong.setAdapter(songsAdapter);
                    // If we don't have a value, look for one and auto add it
                    if (mainActivityInterface.getSong().getFilename() != null && songs.contains(mainActivityInterface.getSong().getFilename().replace(",", ""))) {
                        mainActivityInterface.getSong().setBeatbuddysong(mainActivityInterface.getSong().getFilename().replace(",", ""));
                    } else if (mainActivityInterface.getSong().getTitle() != null && songs.contains(mainActivityInterface.getSong().getTitle().replace(",", ""))) {
                        mainActivityInterface.getSong().setBeatbuddysong(mainActivityInterface.getSong().getTitle().replace(",", ""));
                    }
                    if (mainActivityInterface.getSong().getBeatbuddysong() == null) {
                        mainActivityInterface.getSong().setBeatbuddysong("");
                    }
                    if (mainActivityInterface.getSong().getBeatbuddykit() == null) {
                        mainActivityInterface.getSong().setBeatbuddykit("");
                    }
                    myView.beatBuddySong.setText(mainActivityInterface.getSong().getBeatbuddysong());
                    myView.beatBuddySong.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            // Update the song
                            mainActivityInterface.getSong().setBeatbuddysong(myView.beatBuddySong.getText().toString());
                            mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(), false);
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                        }
                    });
                });

                myView.beatBuddyKit.post(() -> {
                    ExposedDropDownArrayAdapter kitsAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.beatBuddyKit, R.layout.view_exposed_dropdown_item, kits);
                    myView.beatBuddyKit.setAdapter(kitsAdapter);
                    myView.beatBuddyKit.setText(mainActivityInterface.getSong().getBeatbuddykit());
                    myView.beatBuddyKit.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            // Update the song
                            mainActivityInterface.getSong().setBeatbuddykit(myView.beatBuddyKit.getText().toString());
                            mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(), false);
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                        }
                    });
                });
            }
        }
    }

    private void checkTempoTimeSig() {
        // The tempo
        ArrayList<String> tempos = new ArrayList<>();
        tempos.add("");
        for (int x = 40; x < 300; x++) {
            tempos.add(x + "");
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
            myView.tempoDropDown.post(() -> {
                ExposedDropDownArrayAdapter tempoArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                        myView.tempoDropDown, R.layout.view_exposed_dropdown_item, tempos);
                myView.tempoDropDown.setAdapter(tempoArrayAdapter);
                myView.tempoDropDown.setHint(tempo_string + " ("+bpm_string+")");
                myView.tempoDropDown.setText(mainActivityInterface.getSong().getTempo());
                myView.tempoDropDown.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        mainActivityInterface.getSong().setTempo(myView.tempoDropDown.getText().toString());
                        mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),false);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {}
                });
            });

            myView.timesigDropDown.post(() -> {
                ExposedDropDownArrayAdapter timesigArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                        myView.timesigDropDown, R.layout.view_exposed_dropdown_item, timesigs);
                myView.timesigDropDown.setAdapter(timesigArrayAdapter);
                myView.timesigDropDown.setText(mainActivityInterface.getSong().getTimesig());
                myView.timesigDropDown.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        mainActivityInterface.getSong().setTimesig(myView.timesigDropDown.getText().toString());
                        mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),false);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {}
                });
            });
        }
    }
}
