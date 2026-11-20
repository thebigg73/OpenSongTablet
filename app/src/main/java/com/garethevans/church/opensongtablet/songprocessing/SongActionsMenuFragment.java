package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyMaterialSimpleTextView;
import com.garethevans.church.opensongtablet.databinding.SettingsSongactionsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.links.LinksViewBottomSheet;
import com.garethevans.church.opensongtablet.midi.MidiSongBottomSheet;
import com.garethevans.church.opensongtablet.preferences.TextInputBottomSheet;

import java.io.InputStream;
import java.io.OutputStream;

public class SongActionsMenuFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsSongactionsBinding myView;
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "SongActionsMenuFrag";
    private String song_actions_string="", search_index_wait_string="", not_allowed_string="",
        edit_song_string="", delete_string="", export_song_string="", search_string="",
    youtube_string="", music_string="", duplicate_string="", filename_string="",
    song_new_name_string="", deeplink_backup_string="", delete_song_warning_string="",
    deeplink_tags_string="", success_string="", song_name_already_taken_string="",
    error_song_not_saved_string="";

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

        // Set views
        setupViews();

        // Set Listeners
        setupListeners();

        // If we need to perform one of these actions, do it
        if (mainActivityInterface.getWhattodo().equals("duplicateSong")) {
            mainActivityInterface.setWhattodo("");
            myView.duplicate.performClick();
        } else if (mainActivityInterface.getWhattodo().equals("deleteSong")) {
            mainActivityInterface.setWhattodo("");
            myView.delete.performClick();
        } else if (mainActivityInterface.getWhattodo().equals("youTube")) {
            mainActivityInterface.setWhattodo("");
            myView.youTube.performClick();
        } else if (mainActivityInterface.getWhattodo().equals("youTubeMusic")) {
            mainActivityInterface.setWhattodo("");
            myView.youTubeMusic.performClick();
        } else if (mainActivityInterface.getWhattodo().equals("spotify")) {
            mainActivityInterface.setWhattodo("");
            myView.spotify.performClick();
        }
        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            song_actions_string = getString(R.string.song_actions);
            search_index_wait_string = getString(R.string.index_songs_wait);
            not_allowed_string = getString(R.string.not_allowed);
            edit_song_string = getString(R.string.edit_song);
            export_song_string = getString(R.string.export_current_song);
            search_string = getString(R.string.search);
            youtube_string = getString(R.string.youtube);
            music_string = getString(R.string.music);
            duplicate_string = getString(R.string.duplicate);
            filename_string = getString(R.string.filename);
            song_new_name_string = getString(R.string.song_new_name);
            deeplink_backup_string = getString(R.string.deeplink_backup);
            delete_song_warning_string = getString(R.string.delete_song_warning);
            deeplink_tags_string = getString(R.string.deeplink_tags);
            success_string = getString(R.string.success);
            song_name_already_taken_string = getString(R.string.song_name_already_taken);
            error_song_not_saved_string = getString(R.string.error_song_not_saved);
            delete_string = getString(R.string.delete);
        }
    }

    private void setupViews() {
        // Hide the multitrack player if not running Lollipop or later
        myView.mutitrackPlayer.setVisibility(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? View.VISIBLE:View.GONE);
    }

    private void addCurrentSong() {
        String currentSong = mainActivityInterface.getSong().getTitle();
        if (currentSong!=null && !currentSong.isEmpty()) {
            currentSong = " (" + currentSong + ")";
            String newText = edit_song_string + currentSong;
            myView.edit.setHint(newText);
            newText = delete_string + currentSong;
            myView.delete.setHint(newText);
            newText = export_song_string + currentSong;
            myView.share.setHint(newText);
            newText = search_string + currentSong;
            myView.youTube.setHint(newText);
            myView.youTubeMusic.setHint(newText);
            myView.spotify.setHint(newText);
            newText = youtube_string + " " + music_string;
            myView.youTubeMusic.setText(newText);
        }
        // GE - hidden this option, but reserving the right to reinstate even just for me
        // myView.graceTime.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("graceTime",true));
    }

    private void setupListeners() {
        myView.backupButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(deeplink_backup_string,0));
        myView.importButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.import_graph));
        myView.edit.setOnClickListener(v -> actionAllowed(R.id.editsong_graph));
        myView.duplicate.setOnClickListener(v -> {
            if (mainActivityInterface.getProcessSong().isValidSong(mainActivityInterface.getSong())) {
                TextInputBottomSheet textInputBottomSheet = new TextInputBottomSheet(this,"songActionsMenuFragment",
                        duplicate_string,filename_string,
                        duplicate_string + ": " +
                                mainActivityInterface.getSong().getFilename() + "\n" + song_new_name_string,
                        null,null,true);
                textInputBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "textInputBottomSheet");
            } else {
                mainActivityInterface.getShowToast().doIt(not_allowed_string);
            }
        });
        myView.delete.setOnClickListener(v -> {
            if (mainActivityInterface.getProcessSong().isValidSong(mainActivityInterface.getSong())) {
                mainActivityInterface.displayAreYouSure("deleteSong",
                        delete_song_warning_string, null,
                        "SongActionsMenuFragment", this,
                        mainActivityInterface.getSong());
            } else {
                mainActivityInterface.getShowToast().doIt(not_allowed_string);
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
        myView.drummer.setOnClickListener(v -> {
            mainActivityInterface.navHome();
            mainActivityInterface.displayDrummerPopup();
        });
        myView.stickyNotes.setOnClickListener(v -> actionAllowed(R.id.stickyNotesFragment));
        myView.highlighter.setOnClickListener(v -> actionAllowed(R.id.highlighterFragment));
        myView.linksView.setOnClickListener(v -> {
            if (mainActivityInterface.getProcessSong().isValidSong(mainActivityInterface.getSong())) {
                LinksViewBottomSheet linksViewBottomSheet = new LinksViewBottomSheet();
                linksViewBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "LinksViewBottomSheet");
                mainActivityInterface.navHome();
            } else {
                mainActivityInterface.getShowToast().doIt(not_allowed_string);
            }
        });
        myView.links.setOnClickListener(v -> actionAllowed(R.id.linksFragment));
        myView.chords.setOnClickListener(v -> actionAllowed(R.id.chords_graph));
        myView.notation.setOnClickListener(v -> actionAllowed(R.id.musicScoreFragment));
        myView.tags.setOnClickListener(v -> {
            if (mainActivityInterface.getSongListBuildIndex().getCurrentlyIndexing()) {
                String progressText = "";
                if (mainActivityInterface.getSongMenuFragment()!=null) {
                    MyMaterialSimpleTextView progressView = mainActivityInterface.getSongMenuFragment().getProgressText();
                    if (progressView!=null && progressView.getText()!=null) {
                        progressText = " " + progressView.getText().toString();
                    }
                }
                mainActivityInterface.getShowToast().doIt(search_index_wait_string+progressText);
            } else {
                mainActivityInterface.navigateToFragment(deeplink_tags_string, 0);
            }
        });
        myView.midi.setOnClickListener(v -> {
            if (mainActivityInterface.getProcessSong().isValidSong(mainActivityInterface.getSong())) {
                mainActivityInterface.navHome();
                MidiSongBottomSheet midiSongBottomSheet = new MidiSongBottomSheet();
                midiSongBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "midiSongBottomSheet");
            } else {
                mainActivityInterface.getShowToast().doIt(not_allowed_string);
            }
        });

        myView.youTube.setOnClickListener(v -> searchSong("YouTube"));
        myView.youTubeMusic.setOnClickListener(v -> searchSong("YouTubeMusic"));
        myView.spotify.setOnClickListener(v -> searchSong("Spotify"));
        myView.mutitrackPlayer.setOnClickListener(v -> mainActivityInterface.displayMultiTrack());
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
            mainActivityInterface.getShowToast().doIt(not_allowed_string);
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
            mainActivityInterface.getShowToast().doIt(song_name_already_taken_string);
        } else {
            // Because we want to create a new copy, but change the title as well, we create the XML
            mainActivityInterface.getSong().setTitle(newName);
            mainActivityInterface.getSong().setFilename(newName);
            String content = mainActivityInterface.getProcessSong().getXML(mainActivityInterface.getSong());

            // Now write the file
            if (mainActivityInterface.getSong().getFiletype()==null) {
                mainActivityInterface.getSong().setFiletype(mainActivityInterface.getStorageAccess().tryToFixFileTypeFromNull(mainActivityInterface.getSong().getFilename()));
            }

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
                    mainActivityInterface.getShowToast().doIt(success_string);
                    mainActivityInterface.getNonOpenSongSQLiteHelper().createSong(folder,newName);
                    mainActivityInterface.getSong().setFilename(newName);
                    mainActivityInterface.getSong().setTitle(newName);
                    mainActivityInterface.getNonOpenSongSQLiteHelper().updateSong(mainActivityInterface.getSong());
                    // Set the new filename to load and navHome (also trigger the menu rebuild)
                    loadNewSong();
                } else {
                    mainActivityInterface.getShowToast().doIt(error_song_not_saved_string);
                    mainActivityInterface.getSong().setTitle(oldTitle);
                    mainActivityInterface.getSong().setFilename(oldName);
                }

            } else {
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doDuplicate doStringWriteToFile Songs/"+mainActivityInterface.getSong().getFolder()+"/"+newName+" with: "+content);
                if (mainActivityInterface.getStorageAccess().writeFileFromString(
                        "Songs", mainActivityInterface.getSong().getFolder(),
                        newName, content, false)) {
                    mainActivityInterface.getShowToast().doIt(success_string);
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
                    mainActivityInterface.getShowToast().doIt(error_song_not_saved_string);
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
