package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsSongactionsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.midi.MidiSongBottomSheet;
import com.garethevans.church.opensongtablet.preferences.TextInputBottomSheet;
import com.google.android.material.textview.MaterialTextView;

import java.io.InputStream;
import java.io.OutputStream;

public class SongActionsMenuFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsSongactionsBinding myView;
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "SongActionsMenuFrag";
    private String song_actions_string="", search_index_wait_string="";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(song_actions_string);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsSongactionsBinding.inflate(inflater,container,false);

        prepareStrings();

        // Add the current song title to the menu
        addCurrentSong();

        // Set Listeners
        setListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            song_actions_string = getString(R.string.song_actions);
            search_index_wait_string = getString(R.string.index_songs_wait);
        }
    }
    private void addCurrentSong() {
        String currentSong = mainActivityInterface.getSong().getTitle();
        if (currentSong!=null && !currentSong.isEmpty()) {
            currentSong = " (" + currentSong + ")";
            String newText = getString(R.string.edit_song) + currentSong;
            myView.edit.setHint(newText);
            newText = getString(R.string.delete) + currentSong;
            myView.delete.setHint(newText);
            newText = getString(R.string.export_current_song) + currentSong;
            myView.share.setHint(newText);
            newText = getString(R.string.search) + currentSong;
            myView.youTube.setHint(newText);
            myView.youTubeMusic.setHint(newText);
            myView.spotify.setHint(newText);
            newText = getString(R.string.youtube) + " " + getString(R.string.music);
            myView.youTubeMusic.setText(newText);
        }
        // GE - hidden this option, but reserving the right to reinstate even just for me
        // myView.graceTime.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("graceTime",true));
    }

    private void setListeners() {
        myView.backupButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(getString(R.string.deeplink_backup),0));
        myView.importButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.import_graph));
        myView.edit.setOnClickListener(v -> actionAllowed(R.id.editsong_graph));
        myView.duplicate.setOnClickListener(v -> {
            if (mainActivityInterface.getProcessSong().isValidSong(mainActivityInterface.getSong())) {
                TextInputBottomSheet textInputBottomSheet = new TextInputBottomSheet(this,"songActionsMenuFragment",
                        getString(R.string.duplicate),getString(R.string.filename),
                        getString(R.string.duplicate) + ": " +
                                mainActivityInterface.getSong().getFilename() + "\n" + getString(R.string.song_new_name),
                        null,null,true);
                textInputBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "textInputBottomSheet");
            } else {
                mainActivityInterface.getShowToast().doIt(getString(R.string.not_allowed));
            }
        });
        myView.delete.setOnClickListener(v -> {
            if (mainActivityInterface.getProcessSong().isValidSong(mainActivityInterface.getSong())) {
                mainActivityInterface.displayAreYouSure("deleteSong",
                        getString(R.string.delete_song_warning), null,
                        "SongActionsMenuFragment", this,
                        mainActivityInterface.getSong());
            } else {
                mainActivityInterface.getShowToast().doIt(getString(R.string.not_allowed));
            }
        });
        myView.share.setOnClickListener(v -> {
            // Set the 'whatToDo' variable as empty so it isn't treated as a set
            mainActivityInterface.setWhattodo("");
            actionAllowed(R.id.exportFragment);
        });

        myView.pad.setOnClickListener(v -> actionAllowed(R.id.pads_graph));
        myView.autoscroll.setOnClickListener(v -> actionAllowed(R.id.autoscrollSettingsFragment));
        myView.metronome.setOnClickListener(v -> actionAllowed(R.id.metronomeFragment));
        myView.stickyNotes.setOnClickListener(v -> actionAllowed(R.id.stickyNotesFragment));
        myView.highlighter.setOnClickListener(v -> actionAllowed(R.id.highlighterFragment));
        myView.links.setOnClickListener(v -> actionAllowed(R.id.linksFragment));
        myView.chords.setOnClickListener(v -> actionAllowed(R.id.chords_graph));
        myView.notation.setOnClickListener(v -> actionAllowed(R.id.musicScoreFragment));
        myView.tags.setOnClickListener(v -> {
            if (mainActivityInterface.getSongListBuildIndex().getCurrentlyIndexing()) {
                String progressText = "";
                if (mainActivityInterface.getSongMenuFragment()!=null) {
                    MaterialTextView progressView = mainActivityInterface.getSongMenuFragment().getProgressText();
                    if (progressView!=null && progressView.getText()!=null) {
                        progressText = " " + progressView.getText().toString();
                    }
                }
                mainActivityInterface.getShowToast().doIt(search_index_wait_string+progressText);
            } else {
                mainActivityInterface.navigateToFragment(getString(R.string.deeplink_tags), 0);
            }
        });
        myView.midi.setOnClickListener(v -> {
            if (mainActivityInterface.getProcessSong().isValidSong(mainActivityInterface.getSong())) {
                mainActivityInterface.navHome();
                MidiSongBottomSheet midiSongBottomSheet = new MidiSongBottomSheet();
                midiSongBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "midiSongBottomSheet");
            } else {
                mainActivityInterface.getShowToast().doIt(getString(R.string.not_allowed));
            }
        });

        myView.youTube.setOnClickListener(v -> searchSong("YouTube"));
        myView.youTubeMusic.setOnClickListener(v -> searchSong("YouTubeMusic"));
        myView.spotify.setOnClickListener(v -> searchSong("Spotify"));
        // GE - hidden this option, but reserving the right to reinstate even just for me
        // myView.graceTime.setOnCheckedChangeListener((compoundButton, b) -> mainActivityInterface.getPreferences().setMyPreferenceBoolean("graceTime",b));
    }

    private void searchSong(String where) {
        switch (where) {
            case "YouTube":
                mainActivityInterface.openDocument("https://www.youtube.com/search?q=" +
                    mainActivityInterface.getSong().getTitle());
            break;

            case "YouTubeMusic":
                mainActivityInterface.openDocument("https://music.youtube.com/search?q=" +
                    mainActivityInterface.getSong().getTitle());
            break;

            case "Spotify":
                mainActivityInterface.openDocument("https://open.spotify.com/search/results/" +
                        mainActivityInterface.getSong().getTitle());
                break;

        }
    }

    private void actionAllowed(int id) {
        if (mainActivityInterface.getProcessSong().isValidSong(mainActivityInterface.getSong())) {
            mainActivityInterface.navigateToFragment(null, id);
        } else {
            mainActivityInterface.getShowToast().doIt(getString(R.string.not_allowed));
        }
    }

    // Receieved from textInputBottomSheet via the MainActivity
    public void doDuplicate(String newName) {
        String oldName = mainActivityInterface.getSong().getFilename();
        String oldTitle = mainActivityInterface.getSong().getTitle();
        String folder = mainActivityInterface.getSong().getFolder();
        Uri duplicateSongUri = mainActivityInterface.getStorageAccess().getUriForItem("Songs",folder, newName);

        // Only proceed if the song doesn't exist already
        if (mainActivityInterface.getStorageAccess().uriExists(duplicateSongUri)) {
            // Warn the user and stop
            mainActivityInterface.getShowToast().doIt(getString(R.string.song_name_already_taken));
        } else {
            // Because we want to create a new copy, but change the title as well, we create the XML
            mainActivityInterface.getSong().setTitle(newName);
            mainActivityInterface.getSong().setFilename(newName);
            String content = mainActivityInterface.getProcessSong().getXML(mainActivityInterface.getSong());

            // Now write the file
            if (mainActivityInterface.getSong().getFiletype().equals("PDF") ||
                mainActivityInterface.getSong().getFiletype().equals("IMG")) {
                // Copy the actual file
                Uri originalUri = mainActivityInterface.getStorageAccess().getUriForItem("Songs",folder,oldName);
                InputStream inputStream = mainActivityInterface.getStorageAccess().
                        getInputStream(originalUri);
                OutputStream outputStream = mainActivityInterface.getStorageAccess().
                        getOutputStream(duplicateSongUri);
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" copyPDF copyFile from Songs/"+folder+"/"+oldName+" to Songs/"+folder+"/"+newName);
                if (mainActivityInterface.getStorageAccess().copyFile(inputStream,outputStream)) {
                    // Success.  Add to the non-opensong database
                    mainActivityInterface.getShowToast().doIt(getString(R.string.success));
                    mainActivityInterface.getNonOpenSongSQLiteHelper().createSong(folder,newName);
                    mainActivityInterface.getSong().setFilename(newName);
                    mainActivityInterface.getSong().setTitle(newName);
                    mainActivityInterface.getNonOpenSongSQLiteHelper().updateSong(mainActivityInterface.getSong());
                    // Set the new filename to load and navHome (also trigger the menu rebuild)
                    loadNewSong();
                } else {
                    mainActivityInterface.getShowToast().doIt(getString(R.string.error_song_not_saved));
                    mainActivityInterface.getSong().setTitle(oldTitle);
                    mainActivityInterface.getSong().setFilename(oldName);
                }

            } else {
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doDuplicate doStringWriteToFile Songs/"+mainActivityInterface.getSong().getFolder()+"/"+newName+" with: "+content);
                if (mainActivityInterface.getStorageAccess().doStringWriteToFile(
                        "Songs", mainActivityInterface.getSong().getFolder(),
                        newName, content)) {
                    mainActivityInterface.getShowToast().doIt(getString(R.string.success));
                    mainActivityInterface.getSong().setFilename(newName);

                    // Add the new song to the database too
                    mainActivityInterface.getSQLiteHelper().createSong(
                            mainActivityInterface.getSong().getFolder(), newName);
                    mainActivityInterface.getSQLiteHelper().updateSong(mainActivityInterface.getSong());

                    // Set the new filename to load and navHome (also trigger the menu rebuild)
                    loadNewSong();
                } else {
                    mainActivityInterface.getSong().setTitle(oldTitle);
                    mainActivityInterface.getSong().setFilename(oldName);
                    mainActivityInterface.getShowToast().doIt(getString(R.string.error_song_not_saved));
                }
            }
        }
    }

    private void loadNewSong() {
        mainActivityInterface.getPreferences().setMyPreferenceString(
                "songFilename",mainActivityInterface.getSong().getFilename());
        mainActivityInterface.updateSongMenu(mainActivityInterface.getSong());
        mainActivityInterface.navHome();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}
