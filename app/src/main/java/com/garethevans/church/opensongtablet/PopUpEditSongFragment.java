package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PopUpEditSongFragment extends DialogFragment implements PopUpPresentationOrderFragment.MyInterface {

    static PopUpEditSongFragment newInstance() {
        PopUpEditSongFragment frag;
        frag = new PopUpEditSongFragment();
        return frag;
    }

    // The General views available
    private EditText edit_song_title, edit_song_author, edit_song_copyright, edit_song_duration,
            edit_song_presentation, edit_song_notes, edit_song_lyrics;
    private Spinner edit_song_key, edit_song_timesig, edit_song_capo, edit_song_capo_print;
    private SeekBar predelay_SeekBar, edit_song_tempo;
    private TextView predelay_TextView;
    private TextView tempo_text;
    private TextView availabletags;
    private ActionMode mActionMode = null;

    // Advanced
    private EditText edit_song_CCLI, edit_song_aka, edit_song_key_line, edit_song_hymn, edit_song_user1,
            edit_song_user2, edit_song_user3, edit_song_midi, edit_song_midi_index,
            edit_song_restrictions, edit_song_books, edit_song_pitch, customTheme;
    private Spinner edit_song_pad_file;

    private LinearLayout generalSettings, advancedSettings;

    private Button fix_lyrics;
    private FloatingActionButton addBrackets;
    private RelativeLayout transposeDown_RelativeLayout, transposeUp_RelativeLayout;

    private static int temposlider;
    private View V;

    private TextSongConvert textSongConvert;
    private ChordProConvert chordProConvert;
    private StorageAccess storageAccess;
    private Preferences preferences;
    private ProcessSong processSong;
    private SQLite sqLite;
    private SQLiteHelper sqLiteHelper;
    private NonOpenSongSQLite nonOpenSongSQLite;
    private NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper;
    private Transpose transpose;

    //private boolean keyboardopen = false;

    @Override
    public void updatePresentationOrder() {
        edit_song_presentation.setText(StaticVariables.mPresentation);
    }

    private void saveEdit(boolean ended) {

        // If we are editing as chordpro, convert to OpenSong
        if (preferences.getMyPreferenceBoolean(getContext(), "editAsChordPro", false)) {
            String textLyrics = edit_song_lyrics.getText().toString();
            edit_song_lyrics.setText(chordProConvert.fromChordProToOpenSong(textLyrics));
        }
        // Go through the fields and save them
        // Get the variables
        // Set the newtext to the FullscreenActivity variables
        StaticVariables.mTitle = edit_song_title.getText().toString();
        StaticVariables.mAuthor = edit_song_author.getText().toString();
        StaticVariables.mCopyright = edit_song_copyright.getText().toString();
        StaticVariables.mLyrics = processSong.fixStartOfLines(edit_song_lyrics.getText().toString());
        StaticVariables.mPresentation = edit_song_presentation.getText().toString();
        StaticVariables.mHymnNumber = edit_song_hymn.getText().toString();
        StaticVariables.mCCLI = edit_song_CCLI.getText().toString();
        StaticVariables.mUser1 = edit_song_user1.getText().toString();
        StaticVariables.mUser2 = edit_song_user2.getText().toString();
        StaticVariables.mUser3 = edit_song_user3.getText().toString();
        StaticVariables.mAka = edit_song_aka.getText().toString();
        StaticVariables.mKeyLine = edit_song_key_line.getText().toString();
        StaticVariables.mKey = edit_song_key.getItemAtPosition(edit_song_key.getSelectedItemPosition()).toString();
        StaticVariables.mDuration = edit_song_duration.getText().toString();
        int predelayval = predelay_SeekBar.getProgress();
        if (predelayval == 0) {
            StaticVariables.mPreDelay = "";
        } else {
            StaticVariables.mPreDelay = "" + (predelayval - 1);
        }
        StaticVariables.mBooks = edit_song_books.getText().toString();
        StaticVariables.mMidi = edit_song_midi.getText().toString();
        StaticVariables.mMidiIndex = edit_song_midi_index.getText().toString();
        StaticVariables.mPitch = edit_song_pitch.getText().toString();
        StaticVariables.mRestrictions = edit_song_restrictions.getText().toString();
        StaticVariables.mNotes = edit_song_notes.getText().toString();
        StaticVariables.mPadFile = edit_song_pad_file.getItemAtPosition(edit_song_pad_file.getSelectedItemPosition()).toString();

        // Get the position of the capo fret
        StaticVariables.mCapo = edit_song_capo.getItemAtPosition(edit_song_capo.getSelectedItemPosition()).toString();
        // If this contains a space (e.g. 2 (A)), then take the substring at the start as the actual fret
        // This is because we may have a quick capo key reckoner next to the fret number
        if (StaticVariables.mCapo.contains(" ")) {
            int pos = StaticVariables.mCapo.indexOf(" ");
            if (pos > 0) {
                StaticVariables.mCapo = StaticVariables.mCapo.substring(0, pos).trim();
            }
        }

        int tempmCapoPrint = edit_song_capo_print.getSelectedItemPosition();
        if (tempmCapoPrint == 1) {
            StaticVariables.mCapoPrint = "true";
        } else if (tempmCapoPrint == 2) {
            StaticVariables.mCapoPrint = "false";
        } else {
            StaticVariables.mCapoPrint = "";
        }
        int valoftempobar = edit_song_tempo.getProgress() + 39;
        if (valoftempobar > 39) {
            StaticVariables.mTempo = "" + valoftempobar;
        } else {
            StaticVariables.mTempo = "";
        }
        StaticVariables.mTimeSig = edit_song_timesig.getItemAtPosition(edit_song_timesig.getSelectedItemPosition()).toString();

        StaticVariables.mTheme = customTheme.getText().toString();
        if (!StaticVariables.mTheme.endsWith(";")) {
            StaticVariables.mTheme += ";";
        }

        // Set the AltTheme to the same as the Theme?
        StaticVariables.mAltTheme = StaticVariables.mTheme;

        // Prepare the new XML file
        prepareSongXML();

        // Makes sure all & are replaced with &amp;
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("&amp;", "&");
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("&", "&amp;");

        // Now write the modified song
        String where = getLocation();
        String folder = getFolder();

        if (!FullscreenActivity.isPDF && !FullscreenActivity.isImage) {
            Uri uri = storageAccess.getUriForItem(getContext(), preferences, where, folder, StaticVariables.songfilename);
            Log.d("PopUpEditSong", "where=" + where + "\nfolder=" + folder + "\nsongfilename=" + StaticVariables.songfilename);
            // Check the uri exists for the outputstream to be valid
            storageAccess.lollipopCreateFileForOutputStream(getContext(), preferences, uri, null,
                    where, folder, StaticVariables.songfilename);

            OutputStream outputStream = storageAccess.getOutputStream(getContext(), uri);
            storageAccess.writeFileFromString(FullscreenActivity.mynewXML, outputStream);

        }

        // If this isn't a song (a set item, variation, etc, it won't be in the database, so the following will fail
        // That's ok though!!  We don't want to update search indexes or song menus
        try {
            if ((FullscreenActivity.isPDF || FullscreenActivity.isImage) && nonOpenSongSQLite == null) {
                nonOpenSongSQLiteHelper.createBasicSong(getContext(),storageAccess,preferences,
                        StaticVariables.whichSongFolder,StaticVariables.songfilename);
                nonOpenSongSQLite = nonOpenSongSQLiteHelper.getSong(getContext(),storageAccess,preferences,
                        StaticVariables.whichSongFolder+"/"+StaticVariables.songfilename);
            }

            if ((FullscreenActivity.isPDF || FullscreenActivity.isImage) && nonOpenSongSQLite!=null && nonOpenSongSQLite.getId()>-1) {
                Log.d("d", "id=" + nonOpenSongSQLite.getId());
                nonOpenSongSQLite.setId(nonOpenSongSQLite.getId());
                nonOpenSongSQLite.setFolder(StaticVariables.whichSongFolder);
                nonOpenSongSQLite.setFilename(StaticVariables.songfilename);
                nonOpenSongSQLite.setSongid(StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename);
                nonOpenSongSQLiteHelper.updateSong(getContext(), storageAccess, preferences, nonOpenSongSQLite);

            } else if (sqLite!=null && sqLite.getId()>-1) {
                Log.d("d", "id=" + sqLite.getId());
                sqLite.setId(sqLite.getId());
                sqLite.setFolder(StaticVariables.whichSongFolder);
                sqLite.setFilename(StaticVariables.songfilename);
                sqLite.setSongid(StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename);
                sqLite.setTitle(StaticVariables.mTitle);
                sqLite.setAuthor(StaticVariables.mAuthor);
                sqLite.setCopyright(StaticVariables.mCopyright);
                sqLite.setLyrics(StaticVariables.mLyrics);
                sqLite.setHymn_num(StaticVariables.mHymnNumber);
                sqLite.setCcli(StaticVariables.mCCLI);
                sqLite.setTheme(StaticVariables.mTheme);
                sqLite.setAlttheme(StaticVariables.mAltTheme);
                sqLite.setUser1(StaticVariables.mUser1);
                sqLite.setUser2(StaticVariables.mUser2);
                sqLite.setUser3(StaticVariables.mUser3);
                sqLite.setKey(StaticVariables.mKey);
                sqLite.setTimesig(StaticVariables.mTimeSig);
                sqLite.setAka(StaticVariables.mAka);
                sqLiteHelper.updateSong(getContext(), sqLite);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // If we are autologging CCLI information
        if (preferences.getMyPreferenceBoolean(getContext(),"ccliAutomaticLogging",false)) {
            PopUpCCLIFragment.addUsageEntryToLog(getContext(), preferences, StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename,
                    StaticVariables.mTitle, StaticVariables.mAuthor,
                    StaticVariables.mCopyright, StaticVariables.mCCLI, "3"); // Edited
        }

        FullscreenActivity.mynewXML = "";

        if (ended) {
            // If we were sent here because we needed to edit the song, we can now reset that
            FullscreenActivity.needtoeditsong = false;

            // Prepare the message
            StaticVariables.myToastMessage = getResources().getString(R.string.edit_save) + " - " +
                    getResources().getString(R.string.ok);

            // Now tell the main page to refresh itself with this new song
            // Don't need to reload the XML as we already have all its values
            mListener.loadSong();
            mListener.prepareSongMenu();

            // Now dismiss this popup
            forceHideKeyboard();
            dismiss();
        }
    }

    private MyInterface mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (MyInterface) context;
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            forceHideKeyboard();
            this.dismiss();
        }
    }

    public static void justSaveSongXML(Context c, Preferences preferences) {
        // Only do this if the title or song file doesn't identify it as the 'welcome to opensongapp' file
        if (!StaticVariables.mTitle.equals("Welcome to OpenSongApp") &&
                !FullscreenActivity.mynewXML.contains("Welcome to OpenSongApp") &&
                !FullscreenActivity.isImage && !FullscreenActivity.isPDF) {
            // Now write the modified song
            StorageAccess storageAccess = new StorageAccess();

            String where = getLocation();
            String folder = getFolder();

            Uri uri = storageAccess.getUriForItem(c, preferences, where, folder, StaticVariables.songfilename);

            Log.d("PopUpEditSong","uri="+uri);

            // Check the uri exists for the outputstream to be valid
            storageAccess.lollipopCreateFileForOutputStream(c, preferences, uri, null,
                    where, folder, StaticVariables.songfilename);

            OutputStream outputStream = storageAccess.getOutputStream(c, uri);
            storageAccess.writeFileFromString(FullscreenActivity.mynewXML, outputStream);
        }
    }

    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            transposeDown_RelativeLayout.setVisibility(View.VISIBLE);
            transposeUp_RelativeLayout.setVisibility(View.VISIBLE);
            return false;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Log.d("d", "clicked");
            return false;
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            Log.d("d", "destroy");
            transposeDown_RelativeLayout.setVisibility(View.GONE);
            transposeUp_RelativeLayout.setVisibility(View.GONE);
            mActionMode = null;
        }
    };

    private void cancelEdit() {
        // Load the song back up with the default values
        try {
            LoadXML.loadXML(getContext(), preferences, storageAccess, processSong);
        } catch (Exception e) {
            e.printStackTrace();
        }
        forceHideKeyboard();
        dismiss();
    }

    private class seekBarListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            String newtext = getResources().getString(R.string.notset);
            temposlider = edit_song_tempo.getProgress() + 39;
            if (temposlider > 39) {
                newtext = temposlider + " " + getResources().getString(R.string.bpm);
            }
            tempo_text.setText(newtext);
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    public static void prepareSongXML() {
        // Prepare the new XML file

        if (StaticVariables.mEncoding == null || StaticVariables.mEncoding.equals("")) {
            StaticVariables.mEncoding = "UTF-8";
        }
        String myNEWXML = "<?xml version=\"1.0\" encoding=\"" + StaticVariables.mEncoding + "\"?>\n";
        myNEWXML += "<song>\n";
        myNEWXML += "  <title>" + parseToHTMLEntities(StaticVariables.mTitle) + "</title>\n";
        myNEWXML += "  <author>" + parseToHTMLEntities(StaticVariables.mAuthor) + "</author>\n";
        myNEWXML += "  <copyright>" + parseToHTMLEntities(StaticVariables.mCopyright) + "</copyright>\n";
        myNEWXML += "  <presentation>" + parseToHTMLEntities(StaticVariables.mPresentation) + "</presentation>\n";
        myNEWXML += "  <hymn_number>" + parseToHTMLEntities(StaticVariables.mHymnNumber) + "</hymn_number>\n";
        myNEWXML += "  <capo print=\"" + parseToHTMLEntities(StaticVariables.mCapoPrint) + "\">" + parseToHTMLEntities(StaticVariables.mCapo) + "</capo>\n";
        myNEWXML += "  <tempo>" + parseToHTMLEntities(StaticVariables.mTempo) + "</tempo>\n";
        myNEWXML += "  <time_sig>" + parseToHTMLEntities(StaticVariables.mTimeSig) + "</time_sig>\n";
        myNEWXML += "  <duration>" + parseToHTMLEntities(StaticVariables.mDuration) + "</duration>\n";
        myNEWXML += "  <predelay>" + parseToHTMLEntities(StaticVariables.mPreDelay) + "</predelay>\n";
        myNEWXML += "  <ccli>" + parseToHTMLEntities(StaticVariables.mCCLI) + "</ccli>\n";
        myNEWXML += "  <theme>" + parseToHTMLEntities(StaticVariables.mTheme) + "</theme>\n";
        myNEWXML += "  <alttheme>" + parseToHTMLEntities(StaticVariables.mAltTheme) + "</alttheme>\n";
        myNEWXML += "  <user1>" + parseToHTMLEntities(StaticVariables.mUser1) + "</user1>\n";
        myNEWXML += "  <user2>" + parseToHTMLEntities(StaticVariables.mUser2) + "</user2>\n";
        myNEWXML += "  <user3>" + parseToHTMLEntities(StaticVariables.mUser3) + "</user3>\n";
        myNEWXML += "  <key>" + parseToHTMLEntities(StaticVariables.mKey) + "</key>\n";
        myNEWXML += "  <aka>" + parseToHTMLEntities(StaticVariables.mAka) + "</aka>\n";
        myNEWXML += "  <key_line>" + parseToHTMLEntities(StaticVariables.mKeyLine) + "</key_line>\n";
        myNEWXML += "  <books>" + parseToHTMLEntities(StaticVariables.mBooks) + "</books>\n";
        myNEWXML += "  <midi>" + parseToHTMLEntities(StaticVariables.mMidi) + "</midi>\n";
        myNEWXML += "  <midi_index>" + parseToHTMLEntities(StaticVariables.mMidiIndex) + "</midi_index>\n";
        myNEWXML += "  <pitch>" + parseToHTMLEntities(StaticVariables.mPitch) + "</pitch>\n";
        myNEWXML += "  <restrictions>" + parseToHTMLEntities(StaticVariables.mRestrictions) + "</restrictions>\n";
        myNEWXML += "  <notes>" + parseToHTMLEntities(StaticVariables.mNotes) + "</notes>\n";
        myNEWXML += "  <lyrics>" + parseToHTMLEntities(StaticVariables.mLyrics) + "</lyrics>\n";
        myNEWXML += "  <linked_songs>" + parseToHTMLEntities(StaticVariables.mLinkedSongs) + "</linked_songs>\n";
        myNEWXML += "  <pad_file>" + parseToHTMLEntities(StaticVariables.mPadFile) + "</pad_file>\n";
        myNEWXML += "  <custom_chords>" + parseToHTMLEntities(StaticVariables.mCustomChords) + "</custom_chords>\n";
        myNEWXML += "  <link_youtube>" + parseToHTMLEntities(StaticVariables.mLinkYouTube) + "</link_youtube>\n";
        myNEWXML += "  <link_web>" + parseToHTMLEntities(StaticVariables.mLinkWeb) + "</link_web>\n";
        myNEWXML += "  <link_audio>" + parseToHTMLEntities(StaticVariables.mLinkAudio) + "</link_audio>\n";
        myNEWXML += "  <loop_audio>" + parseToHTMLEntities(StaticVariables.mLoopAudio) + "</loop_audio>\n";
        myNEWXML += "  <link_other>" + parseToHTMLEntities(StaticVariables.mLinkOther) + "</link_other>\n";
        myNEWXML += "  <abcnotation>" + parseToHTMLEntities(StaticVariables.mNotation) + "</abcnotation>\n";

        if (!StaticVariables.mExtraStuff1.isEmpty()) {
            myNEWXML += "  " + StaticVariables.mExtraStuff1 + "\n";
        }
        if (!StaticVariables.mExtraStuff2.isEmpty()) {
            myNEWXML += "  " + StaticVariables.mExtraStuff2 + "\n";
        }
        myNEWXML += "</song>";

        FullscreenActivity.mynewXML = myNEWXML;
    }

    public interface MyInterface {
        void prepareSongMenu();
        void loadSong();
    }

    static String parseToHTMLEntities(String val) {
        if (val == null) {
            val = "";
        }
        // Make sure all vals are unencoded to start with
        // Now HTML encode everything that needs encoded
        // Protected are < > &
        // Change < to __lt;  We'll later replace the __ with &.  Do this to deal with &amp; separately
        val = val.replace("<", "__lt;");
        val = val.replace("&lt;", "__lt;");

        // Change > to __gt;  We'll later replace the __ with &.  Do this to deal with &amp; separately
        val = val.replace(">", "__gt;");
        val = val.replace("&gt;", "__gt;");

        // Change &apos; to ' as they don't need encoding in this format - also makes it compatible with desktop
        val = val.replace("&apos;", "'");
        //val = val.replace("\'", "'");

        // Change " to __quot;  We'll later replace the __ with &.  Do this to deal with &amp; separately
        val = val.replace("\"", "__quot;");
        val = val.replace("&quot;", "__quot;");

        // Now deal with the remaining ampersands
        val = val.replace("&amp;", "&");  // Reset any that already encoded - all need encoded now
        val = val.replace("&&", "&");     // Just in case we have wrongly encoded old ones e.g. &amp;&quot;
        val = val.replace("&", "&amp;");  // Reencode all remaining ampersands

        // Now replace the other protected encoded entities back with their leading ampersands
        val = val.replace("__lt;", "&lt;");
        val = val.replace("__gt;", "&gt;");
        val = val.replace("__quot;", "&quot;");

        return val;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        try {
            forceHideKeyboard();
            this.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCapoSpinner(String mKey) {
        ArrayList<String> capooptions = transpose.quickCapoKey(getContext(), preferences, mKey);
        ArrayAdapter<String> aa = new ArrayAdapter<>(requireContext(), R.layout.my_spinner, capooptions);
        aa.notifyDataSetChanged();
        edit_song_capo.setAdapter(aa);
        // Where is the key in the available array
        int index = -1;
        List<String> capo_choice = Arrays.asList(getResources().getStringArray(R.array.capo));
        for (int w = 0; w < capo_choice.size(); w++) {
            if (StaticVariables.mCapo.equals(capo_choice.get(w))) {
                index = w;
            }
        }
        edit_song_capo.setSelection(index);
    }

    private void changeFormat(boolean chordpro) {
        String textLyrics = edit_song_lyrics.getText().toString();
        Log.d("d", textLyrics);
        if (chordpro) {
            edit_song_lyrics.setText(chordProConvert.fromOpenSongToChordPro(textLyrics, getContext(), processSong));
            availabletags.setVisibility(View.GONE);
            fix_lyrics.setVisibility(View.GONE);
            edit_song_lyrics.requestFocus();
        } else {
            edit_song_lyrics.setText(chordProConvert.fromChordProToOpenSong(textLyrics));
            edit_song_lyrics.requestFocus();
            availabletags.setVisibility(View.VISIBLE);
            fix_lyrics.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
            if (getDialog().getWindow()!=null) {
                getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }
        }
        V = inflater.inflate(R.layout.popup_editsong, container, false);
        storageAccess = new StorageAccess();
        textSongConvert = new TextSongConvert();
        chordProConvert = new ChordProConvert();
        preferences = new Preferences();
        processSong = new ProcessSong();
        transpose = new Transpose();

        String songid = StaticVariables.whichSongFolder.replace("'","''") + "/" + StaticVariables.songfilename.replace("'","''");
        if (FullscreenActivity.isImage || FullscreenActivity.isPDF) {
            nonOpenSongSQLiteHelper = new NonOpenSongSQLiteHelper(getContext());
            nonOpenSongSQLite = nonOpenSongSQLiteHelper.getSong(getContext(),storageAccess,preferences,songid);
        } else {
            sqLiteHelper = new SQLiteHelper(getContext());
            sqLite = sqLiteHelper.getSong(getContext(), songid);
        }

        Log.d("PopUpEditSong","whichSongFolder="+StaticVariables.whichSongFolder+"\nsongfilename="+StaticVariables.songfilename);
        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getResources().getString(R.string.edit));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getContext());
            closeMe.setEnabled(false);
            if (FullscreenActivity.needtoeditsong) {
                saveEdit(true);
            } else {
                cancelEdit();
            }
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(saveMe,getContext());
            saveMe.setEnabled(false);
            saveEdit(true);
        });

        // Initialise the basic views
        availabletags = V.findViewById(R.id.availabletags);
        hideIfPDF(availabletags);
        edit_song_title = V.findViewById(R.id.edit_song_title);
        edit_song_author = V.findViewById(R.id.edit_song_author);
        edit_song_copyright = V.findViewById(R.id.edit_song_copyright);
        edit_song_key = V.findViewById(R.id.edit_song_key);
        edit_song_duration = V.findViewById(R.id.edit_song_duration);
        predelay_SeekBar = V.findViewById(R.id.predelay_SeekBar);
        predelay_TextView = V.findViewById(R.id.predelay_TextView);
        edit_song_tempo = V.findViewById(R.id.edit_song_tempo);
        tempo_text = V.findViewById(R.id.tempo_text);
        edit_song_timesig = V.findViewById(R.id.edit_song_timesig);
        edit_song_capo = V.findViewById(R.id.edit_song_capo);
        edit_song_capo_print = V.findViewById(R.id.edit_song_capo_print);
        hideIfPDF(edit_song_capo_print);
        edit_song_presentation = V.findViewById(R.id.edit_song_presentation);
        edit_song_presentation.setFocusable(false);
        edit_song_presentation.setOnClickListener(v -> {
            // Save the song first - false to stop everything reloading
            saveEdit(false);

            DialogFragment newFragment = PopUpPresentationOrderFragment.newInstance();
            newFragment.show(requireActivity().getSupportFragmentManager(), "dialog");
            forceHideKeyboard();
            dismiss();
        });
        hideIfPDF(V.findViewById(R.id.myPresentation));
        hideIfPDF(edit_song_presentation);
        edit_song_notes = V.findViewById(R.id.edit_song_notes);
        addBrackets = V.findViewById(R.id.addBrackets);
        addBrackets.hide();
        addBrackets.setOnClickListener(v -> {
            int start = Math.max(edit_song_lyrics.getSelectionStart(), 0);
            int end = Math.max(edit_song_lyrics.getSelectionEnd(), 0);
            edit_song_lyrics.getText().replace(Math.min(start, end), Math.max(start, end),
                    "[]", 0, 2);
            edit_song_lyrics.setSelection(start+1);
        });
        transposeDown_RelativeLayout = V.findViewById(R.id.transposeDown_RelativeLayout);
        transposeUp_RelativeLayout = V.findViewById(R.id.transposeUp_RelativeLayout);
        transposeDown_RelativeLayout.setVisibility(View.GONE);
        transposeUp_RelativeLayout.setVisibility(View.GONE);
        FloatingActionButton transposeUpFAB = V.findViewById(R.id.transposeUpFAB);
        transposeUpFAB.setOnClickListener(v -> {
            int startSelection = edit_song_lyrics.getSelectionStart();
            int endSelection = edit_song_lyrics.getSelectionEnd();

            if (startSelection > 0 && endSelection > startSelection) {
                String selectedText = edit_song_lyrics.getText().toString().substring(startSelection, endSelection);
                // Transpose it
                selectedText = transpose.transposeThisString(getContext(),preferences,"+1",  selectedText);

                // Replace the old text
                String lyricsinfront = edit_song_lyrics.getText().toString().substring(0, startSelection);
                String lyricsafter = edit_song_lyrics.getText().toString().substring(endSelection);
                String newtext = lyricsinfront + selectedText + lyricsafter;
                edit_song_lyrics.setText(newtext);
                edit_song_lyrics.setSelection(startSelection);

            }
        });
        FloatingActionButton transposeDownFAB = V.findViewById(R.id.transposeDownFAB);
        transposeDownFAB.setOnClickListener(v -> {
            int startSelection = edit_song_lyrics.getSelectionStart();
            int endSelection = edit_song_lyrics.getSelectionEnd();

            if (startSelection > 0 && endSelection > startSelection) {
                String selectedText = edit_song_lyrics.getText().toString().substring(startSelection, endSelection);
                // Transpose it
                selectedText = transpose.transposeThisString(getContext(),preferences,"-1",  selectedText);

                // Replace the old text
                String lyricsinfront = edit_song_lyrics.getText().toString().substring(0, startSelection);
                String lyricsafter = edit_song_lyrics.getText().toString().substring(endSelection);
                String newtext = lyricsinfront + selectedText + lyricsafter;
                edit_song_lyrics.setText(newtext);
                edit_song_lyrics.setSelection(startSelection);

            }
        });
        SwitchCompat editAsChordPro = V.findViewById(R.id.editAsChordPro);
        editAsChordPro.setChecked(preferences.getMyPreferenceBoolean(getContext(),"editAsChordPro",false));
        editAsChordPro.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setMyPreferenceBoolean(getContext(),"editAsChordPro",isChecked);
            changeFormat(isChecked);
        });
        edit_song_lyrics = V.findViewById(R.id.edit_song_lyrics);
        edit_song_lyrics.setHorizontallyScrolling(true);
        edit_song_lyrics.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                addBrackets.show();
                //keyboardopen = false;
                forceShowKeyboard();
            } else {
                addBrackets.hide();
                transposeDown_RelativeLayout.setVisibility(View.GONE);
                transposeUp_RelativeLayout.setVisibility(View.GONE);
                forceHideKeyboard();
            }
        });
        edit_song_lyrics.setOnClickListener(v -> {
            // Show the keyboard
            //keyboardopen = false;
            forceShowKeyboard();
            transposeDown_RelativeLayout.setVisibility(View.GONE);
            transposeUp_RelativeLayout.setVisibility(View.GONE);
        });
        edit_song_lyrics.setOnLongClickListener(v -> {
            if (mActionMode != null) {
                return false;
            }
            Log.d("d", "onlongclick");
            // Start the CAB using the ActionMode.Callback defined above
            mActionMode = requireActivity().startActionMode(mActionModeCallback);
            edit_song_lyrics.setSelected(true);
            return false;
        });
        // Buttons
        Button toggleGeneralAdvanced = V.findViewById(R.id.show_general_advanced);
        generalSettings = V.findViewById(R.id.general_settings);
        TextView abcnotation = V.findViewById(R.id.abcnotation);
        abcnotation.setOnClickListener(view -> {
            // Save the song first - false to stop everything reloading
            saveEdit(false);

            FullscreenActivity.whattodo = "abcnotation_edit";
            DialogFragment newFragment = PopUpABCNotationFragment.newInstance();
            newFragment.show(requireActivity().getSupportFragmentManager(), "dialog");
            forceHideKeyboard();
            dismiss();
        });
        fix_lyrics = V.findViewById(R.id.fix_lyrics);
        fix_lyrics.setOnClickListener(view -> {
            String text = edit_song_lyrics.getText().toString();
            if (preferences.getMyPreferenceBoolean(getContext(),"editAsChordPro",false)) {
                // If we are editing as ChordPro, convert it back to OpenSong first
                text = chordProConvert.fromChordProToOpenSong(text);
                // Now fix it
                text = textSongConvert.convertText(getContext(),text);
                // Now set it back to the ChordPro format
                text = chordProConvert.fromOpenSongToChordPro(text,getContext(),processSong);
            } else {
                // Using OpenSong format, so simply fix it
                text = textSongConvert.convertText(getContext(),text);
            }
            edit_song_lyrics.setText(text);
        });

        // Initialise the advanced views
        edit_song_CCLI = V.findViewById(R.id.edit_song_ccli);
        edit_song_aka = V.findViewById(R.id.edit_song_aka);
        edit_song_key_line = V.findViewById(R.id.edit_song_keyline);
        hideIfPDF(V.findViewById(R.id.myKeyLine));
        hideIfPDF(edit_song_key_line);
        edit_song_hymn = V.findViewById(R.id.edit_song_hymn);
        edit_song_user1 = V.findViewById(R.id.edit_song_user1);
        edit_song_user2 = V.findViewById(R.id.edit_song_user2);
        edit_song_user3 = V.findViewById(R.id.edit_song_user3);
        edit_song_pad_file = V.findViewById(R.id.edit_pad_file);
        edit_song_midi = V.findViewById(R.id.edit_song_midi);
        edit_song_midi_index = V.findViewById(R.id.edit_song_midi_index);
        hideIfPDF(V.findViewById(R.id.myMidiIndex)); // Not using this
        hideIfPDF(edit_song_midi_index);
        edit_song_restrictions = V.findViewById(R.id.edit_song_restrictions);
        hideIfPDF(V.findViewById(R.id.myRestrictions));
        hideIfPDF(edit_song_restrictions);
        edit_song_books = V.findViewById(R.id.edit_song_books);
        hideIfPDF(V.findViewById(R.id.myBooks));
        hideIfPDF(edit_song_books);
        edit_song_pitch = V.findViewById(R.id.edit_song_pitch);
        hideIfPDF(V.findViewById(R.id.myPitch));
        hideIfPDF(edit_song_pitch);
        customTheme = V.findViewById(R.id.customTheme);
        advancedSettings = V.findViewById(R.id.advanced_settings);

        // Listeners for the buttons
        toggleGeneralAdvanced.setOnClickListener(v -> {

             if (generalSettings.getVisibility() == View.VISIBLE) {
                // Slide the general settings left
                generalSettings.startAnimation(AnimationUtils.loadAnimation(
                        V.getContext(), R.anim.slide_out_left));
                // Wait 300ms before hiding the general settings and unhiding the advanced settings
                Handler delayfadeinredraw = new Handler();
                delayfadeinredraw.postDelayed(() -> generalSettings.setVisibility(View.GONE), 300); // 300ms

                // Wait 300ms before sliding in the advanced settings
                Handler delayfadeinredraw2 = new Handler();
                delayfadeinredraw2.postDelayed(() -> {
                    advancedSettings.startAnimation(AnimationUtils
                            .loadAnimation(V.getContext(),
                                    R.anim.slide_in_right));
                    advancedSettings.setVisibility(View.VISIBLE);
                }, 600); // 300ms

            } else {
                // Slide the advanced settings right
                advancedSettings.startAnimation(AnimationUtils.loadAnimation(
                        V.getContext(), R.anim.slide_out_right));
                // Wait 300ms before hiding the advanced settings and unhiding the general settings
                Handler delayfadeinredraw = new Handler();
                delayfadeinredraw.postDelayed(() -> advancedSettings.setVisibility(View.GONE), 300); // 300ms

                // Wait 300ms before sliding in the general settings
                Handler delayfadeinredraw2 = new Handler();
                delayfadeinredraw2.postDelayed(() -> {
                    generalSettings.startAnimation(AnimationUtils
                            .loadAnimation(V.getContext(),
                                    R.anim.slide_in_left));
                    generalSettings.setVisibility(View.VISIBLE);
                }, 600); // 300ms
            }
        });

        // Fill in the current values
        // Start with the simple EditTexts
        edit_song_title.setText(StaticVariables.mTitle);
        edit_song_author.setText(StaticVariables.mAuthor);
        edit_song_copyright.setText(StaticVariables.mCopyright);
        edit_song_presentation.setText(StaticVariables.mPresentation);
        edit_song_duration.setText(StaticVariables.mDuration);
        if (StaticVariables.mPreDelay.isEmpty()) {
            predelay_SeekBar.setProgress(0);
            predelay_TextView.setText("");
        } else {
            int val=Integer.parseInt(StaticVariables.mPreDelay.replaceAll("[\\D]",""));
            if (val<0) {
                val=0;
            }
            String text = val + " s";
            predelay_SeekBar.setProgress(val+1);
            predelay_TextView.setText(text);
        }
        edit_song_notes.setText(StaticVariables.mNotes);
        edit_song_duration.setText(StaticVariables.mDuration);
        edit_song_lyrics.setTypeface(Typeface.MONOSPACE);
        // Get the lyrics into a temp string (so we can get rid of rubbish tabs, etc)
        String editBoxLyrics = StaticVariables.mLyrics;
        editBoxLyrics = editBoxLyrics.replaceAll("\r\n", "\n");
        editBoxLyrics = editBoxLyrics.replaceAll("\n\r", "\n");
        editBoxLyrics = editBoxLyrics.replaceAll("\t", "    ");
        editBoxLyrics = editBoxLyrics.replace("\b", "    ");
        editBoxLyrics = editBoxLyrics.replaceAll("\f", "    ");
        edit_song_lyrics.setText(editBoxLyrics);
        // If the user wants to use ChordPro format, change it
        if (preferences.getMyPreferenceBoolean(getContext(),"editAsChordPro",false)) {
            changeFormat(true);
        }
        edit_song_CCLI.setText(StaticVariables.mCCLI);
        edit_song_aka.setText(StaticVariables.mAka);
        edit_song_key_line.setText(StaticVariables.mKeyLine);
        edit_song_hymn.setText(StaticVariables.mHymnNumber);
        edit_song_user1.setText(StaticVariables.mUser1);
        edit_song_user2.setText(StaticVariables.mUser2);
        edit_song_user3.setText(StaticVariables.mUser3);
        edit_song_midi.setText(StaticVariables.mMidi);
        edit_song_midi_index.setText(StaticVariables.mMidiIndex);
        edit_song_restrictions.setText(StaticVariables.mRestrictions);
        edit_song_books.setText(StaticVariables.mBooks);
        edit_song_pitch.setText(StaticVariables.mPitch);
        abcnotation.setText(StaticVariables.mNotation);

        customTheme.setText(StaticVariables.mTheme);

        // Now the Spinners
        // The Key
        ArrayAdapter<CharSequence> song_key = ArrayAdapter.createFromResource(requireContext(),
                R.array.key_choice,
                R.layout.my_spinner);
        song_key.setDropDownViewResource(R.layout.my_spinner);
        edit_song_key.setAdapter(song_key);
        // Where is the key in the available array
        int index = -1;
        List<String> key_choice = Arrays.asList(getResources().getStringArray(R.array.key_choice));
        for (int w=0;w<key_choice.size();w++) {
            if (StaticVariables.mKey.equals(key_choice.get(w))) {
                index = w;
            }
        }
        edit_song_key.setSelection(index);

        // The time sig
        ArrayAdapter<CharSequence> time_sigs = ArrayAdapter.createFromResource(requireContext(),
                R.array.timesig,
                R.layout.my_spinner);
        time_sigs.setDropDownViewResource(R.layout.my_spinner);
        edit_song_timesig.setAdapter(time_sigs);
        // Where is the key in the available array
        index = -1;
        List<String> timesig = Arrays.asList(getResources().getStringArray(R.array.timesig));
        for (int w=0;w<timesig.size();w++) {
            if (StaticVariables.mTimeSig.equals(timesig.get(w))) {
                index = w;
            }
        }
        edit_song_timesig.setSelection(index);

        // The capo fret
        setCapoSpinner(StaticVariables.mKey);

        // If the key changes, we need to update the spinner for the capo fret
        edit_song_key.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setCapoSpinner(edit_song_key.getItemAtPosition(i).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                setCapoSpinner("");
            }
        });

        // The capo print
        ArrayAdapter<CharSequence> capo_print = ArrayAdapter.createFromResource(requireContext(),
                R.array.capoprint,
                R.layout.my_spinner);
        capo_print.setDropDownViewResource(R.layout.my_spinner);
        edit_song_capo_print.setAdapter(capo_print);
        // Where is the key in the available array
        switch (StaticVariables.mCapoPrint) {
            case "true":
                edit_song_capo_print.setSelection(1);
                break;
            case "false":
                edit_song_capo_print.setSelection(2);
                break;
            default:
                edit_song_capo_print.setSelection(0);
                break;
        }

        // The pad file
        // Currently only auto or off
        ArrayList<String> pad_option = new ArrayList<>();
        String auto = getResources().getString(R.string.pad_auto);
        String link = getResources().getString(R.string.link_audio);
        String off = getResources().getString(R.string.off);
        pad_option.add(auto);
        pad_option.add(link);
        pad_option.add(off);
        ArrayAdapter<String> pad_file;
        pad_file = new ArrayAdapter<>(requireContext(), R.layout.my_spinner, pad_option);
        pad_file.setDropDownViewResource(R.layout.my_spinner);
        edit_song_pad_file.setAdapter(pad_file);
        // Only allow auto for now (first index)
        if (StaticVariables.mPadFile.equals(off)) {
            edit_song_pad_file.setSelection(2);
        } else if (StaticVariables.mPadFile.equals(link)) {
            edit_song_pad_file.setSelection(1);
        } else {
            edit_song_pad_file.setSelection(0);
        }

        // Now the seekbars
        String temp_tempo = StaticVariables.mTempo;
        temp_tempo = temp_tempo.replace("Very Fast", "140");
        temp_tempo = temp_tempo.replace("Fast", "120");
        temp_tempo = temp_tempo.replace("Moderate", "100");
        temp_tempo = temp_tempo.replace("Slow", "80");
        temp_tempo = temp_tempo.replace("Very Slow", "60");
        temp_tempo = temp_tempo.replaceAll("[\\D]", "");
        // Get numerical value for slider
        try {
            temposlider = Integer.parseInt(temp_tempo);
        } catch(NumberFormatException nfe) {
            temposlider = 39;
        }
        temposlider = temposlider - 39;

        String newtext = getResources().getString(R.string.notset);
        if (temposlider<1) {
            temposlider=0;
        } else {
            newtext = temp_tempo + " " + getResources().getString(R.string.bpm);
        }
        tempo_text.setText(newtext);
        edit_song_tempo.setProgress(temposlider);
        edit_song_tempo.setOnSeekBarChangeListener(new seekBarListener());

        predelay_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress==0) {
                    predelay_TextView.setText("");
                } else {
                    String text = (progress-1)+"s";
                    predelay_TextView.setText(text);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void hideIfPDF(View v) {
        if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
            v.setVisibility(View.GONE);
        }
    }
    private void forceShowKeyboard() {
        /*if (!keyboardopen && edit_song_lyrics.hasFocus()) {
            try {
                keyboardopen=true;
                Log.d("d", "Force show keyboard");
                edit_song_lyrics.requestFocus();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (getContext()!=null) {
                            getContext().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (getContext() != null) {
                                        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                        if (imm != null) {
                                            imm.showSoftInput(edit_song_lyrics, 0);
                                            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                                            //imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                        }
                                    }
                                }
                            });
                        }
                    }
                }, 200);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
    }

    private void forceHideKeyboard() {
            /*try {
                if (v==null && getContext()!=null) {
                    v = new TextView(getContext());
                } else if (v==null) {
                    v = new TextView(getContext());
                }
                Log.d("d", "Force hide keyboard");
                InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getContext()).getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm!=null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                keyboardopen = false;
            } catch (Exception e) {
                e.printStackTrace();
            }*/
    }

    private static String getLocation() {
        if (StaticVariables.whichSongFolder.startsWith("../")) {
            return StaticVariables.whichSongFolder.replace("../","");
        } else {
            return "Songs";
        }
    }

    private static String getFolder() {
        if (StaticVariables.whichSongFolder.startsWith("../")) {
            return "";
        } else {
            return StaticVariables.whichSongFolder;
        }
    }

    @Override
    public void onDismiss(@NonNull final DialogInterface dialog) {
        forceHideKeyboard();
    }
}