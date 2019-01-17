package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

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
    EditText edit_song_title, edit_song_author, edit_song_copyright, edit_song_duration,
            edit_song_presentation, edit_song_notes, edit_song_lyrics;
    Spinner edit_song_key, edit_song_timesig, edit_song_capo, edit_song_capo_print;
    SeekBar predelay_SeekBar, edit_song_tempo;
    TextView predelay_TextView, tempo_text, abcnotation, availabletags;
    SwitchCompat editAsChordPro;

    // Advanced
    EditText edit_song_CCLI, edit_song_aka, edit_song_key_line, edit_song_hymn, edit_song_user1,
            edit_song_user2, edit_song_user3, edit_song_midi, edit_song_midi_index,
            edit_song_restrictions, edit_song_books, edit_song_pitch, customTheme;
    Spinner edit_song_pad_file;

    LinearLayout generalSettings, advancedSettings;

    // Buttons
    Button toggleGeneralAdvanced, fix_lyrics;
    FloatingActionButton addBrackets;

    static int temposlider;
    View V;
    String title;

    TextSongConvert textSongConvert;
    ChordProConvert chordProConvert;
    StorageAccess storageAccess;
    ListSongFiles listSongFiles;
    Preferences preferences;

    @Override
    public void updatePresentationOrder() {
        edit_song_presentation.setText(FullscreenActivity.mPresentation);
    }

    public void saveEdit(boolean ended) {

        // If we are editing as chordpro, convert to OpenSong
        if (FullscreenActivity.editAsChordPro) {
            String textLyrics = edit_song_lyrics.getText().toString();
            edit_song_lyrics.setText(chordProConvert.fromChordProToOpenSong(textLyrics));
        }
        // Go through the fields and save them
        // Get the variables
        // Set the newtext to the FullscreenActivity variables
        FullscreenActivity.mTitle = edit_song_title.getText().toString();
        FullscreenActivity.mAuthor = edit_song_author.getText().toString();
        FullscreenActivity.mCopyright = edit_song_copyright.getText().toString();
        FullscreenActivity.mLyrics = ProcessSong.fixStartOfLines(edit_song_lyrics.getText().toString());
        FullscreenActivity.mPresentation = edit_song_presentation.getText().toString();
        FullscreenActivity.mHymnNumber = edit_song_hymn.getText().toString();
        FullscreenActivity.mCCLI = edit_song_CCLI.getText().toString();
        FullscreenActivity.mUser1 = edit_song_user1.getText().toString();
        FullscreenActivity.mUser2 = edit_song_user2.getText().toString();
        FullscreenActivity.mUser3 = edit_song_user3.getText().toString();
        FullscreenActivity.mAka = edit_song_aka.getText().toString();
        FullscreenActivity.mKeyLine = edit_song_key_line.getText().toString();
        FullscreenActivity.mKey = edit_song_key.getItemAtPosition(edit_song_key.getSelectedItemPosition()).toString();
        FullscreenActivity.mDuration = edit_song_duration.getText().toString();
        int predelayval = predelay_SeekBar.getProgress();
        if (predelayval == 0) {
            FullscreenActivity.mPreDelay = "";
        } else {
            FullscreenActivity.mPreDelay = "" + (predelayval - 1);
        }
        FullscreenActivity.mBooks = edit_song_books.getText().toString();
        FullscreenActivity.mMidi = edit_song_midi.getText().toString();
        FullscreenActivity.mMidiIndex = edit_song_midi_index.getText().toString();
        FullscreenActivity.mPitch = edit_song_pitch.getText().toString();
        FullscreenActivity.mRestrictions = edit_song_restrictions.getText().toString();
        FullscreenActivity.mNotes = edit_song_notes.getText().toString();
        FullscreenActivity.mPadFile = edit_song_pad_file.getItemAtPosition(edit_song_pad_file.getSelectedItemPosition()).toString();

        // Get the position of the capo fret
        FullscreenActivity.mCapo = edit_song_capo.getItemAtPosition(edit_song_capo.getSelectedItemPosition()).toString();
        // If this contains a space (e.g. 2 (A)), then take the substring at the start as the actual fret
        // This is because we may have a quick capo key reckoner next to the fret number
        if (FullscreenActivity.mCapo.contains(" ")) {
            int pos = FullscreenActivity.mCapo.indexOf(" ");
            if (pos > 0) {
                FullscreenActivity.mCapo = FullscreenActivity.mCapo.substring(0, pos).trim();
            }
        }

        int tempmCapoPrint = edit_song_capo_print.getSelectedItemPosition();
        if (tempmCapoPrint == 1) {
            FullscreenActivity.mCapoPrint = "true";
        } else if (tempmCapoPrint == 2) {
            FullscreenActivity.mCapoPrint = "false";
        } else {
            FullscreenActivity.mCapoPrint = "";
        }
        int valoftempobar = edit_song_tempo.getProgress() + 39;
        if (valoftempobar > 39) {
            FullscreenActivity.mTempo = "" + valoftempobar;
        } else {
            FullscreenActivity.mTempo = "";
        }
        FullscreenActivity.mTimeSig = edit_song_timesig.getItemAtPosition(edit_song_timesig.getSelectedItemPosition()).toString();

        FullscreenActivity.mTheme = customTheme.getText().toString();
        if (!FullscreenActivity.mTheme.endsWith(";")) {
            FullscreenActivity.mTheme += ";";
        }

        // Set the AltTheme to the same as the Theme?
        FullscreenActivity.mAltTheme = FullscreenActivity.mTheme;

        // Prepare the new XML file
        prepareSongXML();

        // Makes sure all & are replaced with &amp;
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("&amp;", "&");
        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("&", "&amp;");

        // Now write the modified song
        Uri uri = storageAccess.getUriForItem(getActivity(), preferences, "Songs", FullscreenActivity.whichSongFolder,
                FullscreenActivity.songfilename);

        // Check the uri exists for the outputstream to be valid
        storageAccess.lollipopCreateFileForOutputStream(getActivity(), preferences, uri, null,
                "Songs", FullscreenActivity.whichSongFolder, FullscreenActivity.songfilename);

        OutputStream outputStream = storageAccess.getOutputStream(getActivity(), uri);
        storageAccess.writeFileFromString(FullscreenActivity.mynewXML, outputStream);

        // If we are autologging CCLI information
        if (FullscreenActivity.ccli_automatic) {
            PopUpCCLIFragment.addUsageEntryToLog(getActivity(), preferences, FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename,
                    FullscreenActivity.mTitle.toString(), FullscreenActivity.mAuthor.toString(),
                    FullscreenActivity.mCopyright.toString(), FullscreenActivity.mCCLI, "3"); // Edited
        }

        FullscreenActivity.mynewXML = "";

        // Save the preferences
        Preferences.savePreferences();

        if (ended) {
            // Prepare the message
            FullscreenActivity.myToastMessage = getResources().getString(R.string.edit_save) + " - " +
                    getResources().getString(R.string.ok);

            // Now tell the main page to refresh itself with this new song
            // Don't need to reload the XML as we already have all its values
            mListener.rebuildSearchIndex();

            // Now dismiss this popup
            dismiss();
        }
    }

    private MyInterface mListener;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (MyInterface) activity;
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
            this.dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        }
    }

    public static void justSaveSongXML(Context c, Preferences preferences) {
        // Only do this if the title or song file doesn't identify it as the 'welcome to opensongapp' file
        if (!FullscreenActivity.mTitle.equals("Welcome to OpenSongApp") &&
                !FullscreenActivity.mynewXML.contains("Welcome to OpenSongApp")) {
            // Now write the modified song
            StorageAccess storageAccess = new StorageAccess();
            Uri uri = storageAccess.getUriForItem(c, preferences, "Songs", FullscreenActivity.whichSongFolder,
                    FullscreenActivity.songfilename);

            // Check the uri exists for the outputstream to be valid
            storageAccess.lollipopCreateFileForOutputStream(c, preferences, uri, null,
                    "Songs", FullscreenActivity.whichSongFolder, FullscreenActivity.songfilename);

            OutputStream outputStream = storageAccess.getOutputStream(c, uri);
            storageAccess.writeFileFromString(FullscreenActivity.mynewXML, outputStream);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        V = inflater.inflate(R.layout.popup_editsong, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.options_song_edit));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                cancelEdit();
            }
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(saveMe,getActivity());
                saveMe.setEnabled(false);
                saveEdit(true);
            }
        });

        textSongConvert = new TextSongConvert();
        chordProConvert = new ChordProConvert();
        storageAccess = new StorageAccess();
        listSongFiles = new ListSongFiles();
        preferences = new Preferences();

        // Initialise the basic views
        availabletags = V.findViewById(R.id.availabletags);
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
        edit_song_presentation = V.findViewById(R.id.edit_song_presentation);
        edit_song_presentation.setFocusable(false);
        edit_song_presentation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save the song first - false to stop everything reloading
                saveEdit(false);

                DialogFragment newFragment = PopUpPresentationOrderFragment.newInstance();
                newFragment.show(getFragmentManager(), "dialog");
                dismiss();
            }
        });
        edit_song_notes = V.findViewById(R.id.edit_song_notes);
        addBrackets = V.findViewById(R.id.addBrackets);
        addBrackets.setVisibility(View.GONE);
        addBrackets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int start = Math.max(edit_song_lyrics.getSelectionStart(), 0);
                int end = Math.max(edit_song_lyrics.getSelectionEnd(), 0);
                edit_song_lyrics.getText().replace(Math.min(start, end), Math.max(start, end),
                        "[]", 0, 2);
                edit_song_lyrics.setSelection(start+1);
            }
        });
        editAsChordPro = V.findViewById(R.id.editAsChordPro);
        editAsChordPro.setChecked(FullscreenActivity.editAsChordPro);
        editAsChordPro.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FullscreenActivity.editAsChordPro = isChecked;
                Preferences.savePreferences();
                changeFormat(isChecked);
            }
        });
        edit_song_lyrics = V.findViewById(R.id.edit_song_lyrics);
        edit_song_lyrics.setHorizontallyScrolling(true);
        edit_song_lyrics.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    addBrackets.setVisibility(View.VISIBLE);
                } else {
                    addBrackets.setVisibility(View.GONE);
                }
            }
        });
        toggleGeneralAdvanced = V.findViewById(R.id.show_general_advanced);
        generalSettings = V.findViewById(R.id.general_settings);
        abcnotation = V.findViewById(R.id.abcnotation);
        abcnotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save the song first - false to stop everything reloading
                saveEdit(false);

                FullscreenActivity.whattodo = "abcnotation_edit";
                DialogFragment newFragment = PopUpABCNotationFragment.newInstance();
                newFragment.show(getFragmentManager(), "dialog");
                dismiss();
            }
        });
        fix_lyrics = V.findViewById(R.id.fix_lyrics);
        fix_lyrics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = edit_song_lyrics.getText().toString();
                if (FullscreenActivity.editAsChordPro) {
                    // If we are editing as ChordPro, convert it back to OpenSong first
                    text = chordProConvert.fromChordProToOpenSong(text);
                    // Now fix it
                    text = textSongConvert.convertText(getActivity(),text);
                    // Now set it back to the ChordPro format
                    text = chordProConvert.fromOpenSongToChordPro(text,getActivity());
                } else {
                    // Using OpenSong format, so simply fix it
                    text = textSongConvert.convertText(getActivity(),text);
                }
                edit_song_lyrics.setText(text);
            }
        });

        // Initialise the advanced views
        edit_song_CCLI = V.findViewById(R.id.edit_song_ccli);
        edit_song_aka = V.findViewById(R.id.edit_song_aka);
        edit_song_key_line = V.findViewById(R.id.edit_song_keyline);
        edit_song_hymn = V.findViewById(R.id.edit_song_hymn);
        edit_song_user1 = V.findViewById(R.id.edit_song_user1);
        edit_song_user2 = V.findViewById(R.id.edit_song_user2);
        edit_song_user3 = V.findViewById(R.id.edit_song_user3);
        edit_song_pad_file = V.findViewById(R.id.edit_pad_file);
        edit_song_midi = V.findViewById(R.id.edit_song_midi);
        edit_song_midi_index = V.findViewById(R.id.edit_song_midi_index);
        edit_song_restrictions = V.findViewById(R.id.edit_song_restrictions);
        edit_song_books = V.findViewById(R.id.edit_song_books);
        edit_song_pitch = V.findViewById(R.id.edit_song_pitch);
        customTheme = V.findViewById(R.id.customTheme);
        advancedSettings = V.findViewById(R.id.advanced_settings);

        // Listeners for the buttons
        toggleGeneralAdvanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                 if (generalSettings.getVisibility() == View.VISIBLE) {
                    // Slide the general settings left
                    generalSettings.startAnimation(AnimationUtils.loadAnimation(
                            V.getContext(), R.anim.slide_out_left));
                    // Wait 300ms before hiding the general settings and unhiding the advanced settings
                    Handler delayfadeinredraw = new Handler();
                    delayfadeinredraw.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            generalSettings.setVisibility(View.GONE);
                        }
                    }, 300); // 300ms

                    // Wait 300ms before sliding in the advanced settings
                    Handler delayfadeinredraw2 = new Handler();
                    delayfadeinredraw2.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            advancedSettings.startAnimation(AnimationUtils
                                    .loadAnimation(V.getContext(),
                                            R.anim.slide_in_right));
                            advancedSettings.setVisibility(View.VISIBLE);
                        }
                    }, 600); // 300ms

                } else {
                    // Slide the advanced settings right
                    advancedSettings.startAnimation(AnimationUtils.loadAnimation(
                            V.getContext(), R.anim.slide_out_right));
                    // Wait 300ms before hiding the advanced settings and unhiding the general settings
                    Handler delayfadeinredraw = new Handler();
                    delayfadeinredraw.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            advancedSettings.setVisibility(View.GONE);
                        }
                    }, 300); // 300ms

                    // Wait 300ms before sliding in the general settings
                    Handler delayfadeinredraw2 = new Handler();
                    delayfadeinredraw2.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            generalSettings.startAnimation(AnimationUtils
                                    .loadAnimation(V.getContext(),
                                            R.anim.slide_in_left));
                            generalSettings.setVisibility(View.VISIBLE);
                        }
                    }, 600); // 300ms
                }
            }
        });

        // Fill in the current values
        // Start with the simple EditTexts
        edit_song_title.setText(FullscreenActivity.mTitle);
        edit_song_author.setText(FullscreenActivity.mAuthor);
        edit_song_copyright.setText(FullscreenActivity.mCopyright);
        edit_song_presentation.setText(FullscreenActivity.mPresentation);
        edit_song_duration.setText(FullscreenActivity.mDuration);
        if (FullscreenActivity.mPreDelay.isEmpty()) {
            predelay_SeekBar.setProgress(0);
            predelay_TextView.setText("");
        } else {
            int val=Integer.parseInt(FullscreenActivity.mPreDelay.replaceAll("[\\D]",""));
            if (val<0) {
                val=0;
            }
            String text = val + " s";
            predelay_SeekBar.setProgress(val+1);
            predelay_TextView.setText(text);
        }
        edit_song_notes.setText(FullscreenActivity.mNotes);
        edit_song_duration.setText(FullscreenActivity.mDuration);
        edit_song_lyrics.setTypeface(Typeface.MONOSPACE);
        // Get the lyrics into a temp string (so we can get rid of rubbish tabs, etc)
        String editBoxLyrics = FullscreenActivity.mLyrics;
        editBoxLyrics = editBoxLyrics.replaceAll("\r\n", "\n");
        editBoxLyrics = editBoxLyrics.replaceAll("\n\r", "\n");
        editBoxLyrics = editBoxLyrics.replaceAll("\t", "    ");
        editBoxLyrics = editBoxLyrics.replace("\b", "    ");
        editBoxLyrics = editBoxLyrics.replaceAll("\f", "    ");
        edit_song_lyrics.setText(editBoxLyrics);
        // If the user wants to use ChordPro format, change it
        if (FullscreenActivity.editAsChordPro) {
            changeFormat(true);
        }
        edit_song_CCLI.setText(FullscreenActivity.mCCLI);
        edit_song_aka.setText(FullscreenActivity.mAka);
        edit_song_key_line.setText(FullscreenActivity.mKeyLine);
        edit_song_hymn.setText(FullscreenActivity.mHymnNumber);
        edit_song_user1.setText(FullscreenActivity.mUser1);
        edit_song_user2.setText(FullscreenActivity.mUser2);
        edit_song_user3.setText(FullscreenActivity.mUser3);
        edit_song_midi.setText(FullscreenActivity.mMidi);
        edit_song_midi_index.setText(FullscreenActivity.mMidiIndex);
        edit_song_restrictions.setText(FullscreenActivity.mRestrictions);
        edit_song_books.setText(FullscreenActivity.mBooks);
        edit_song_pitch.setText(FullscreenActivity.mPitch);
        abcnotation.setText(FullscreenActivity.mNotation);

        customTheme.setText(FullscreenActivity.mTheme);

        // Now the Spinners
        // The Key
        ArrayAdapter<CharSequence> song_key = ArrayAdapter.createFromResource(getActivity(),
                R.array.key_choice,
                R.layout.my_spinner);
        song_key.setDropDownViewResource(R.layout.my_spinner);
        edit_song_key.setAdapter(song_key);
        // Where is the key in the available array
        int index = -1;
        List<String> key_choice = Arrays.asList(getResources().getStringArray(R.array.key_choice));
        for (int w=0;w<key_choice.size();w++) {
            if (FullscreenActivity.mKey.equals(key_choice.get(w))) {
                index = w;
            }
        }
        edit_song_key.setSelection(index);

        // The time sig
        ArrayAdapter<CharSequence> time_sigs = ArrayAdapter.createFromResource(getActivity(),
                R.array.timesig,
                R.layout.my_spinner);
        time_sigs.setDropDownViewResource(R.layout.my_spinner);
        edit_song_timesig.setAdapter(time_sigs);
        // Where is the key in the available array
        index = -1;
        List<String> timesig = Arrays.asList(getResources().getStringArray(R.array.timesig));
        for (int w=0;w<timesig.size();w++) {
            if (FullscreenActivity.mTimeSig.equals(timesig.get(w))) {
                index = w;
            }
        }
        edit_song_timesig.setSelection(index);

        // The capo fret
        setCapoSpinner(FullscreenActivity.mKey);

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
        ArrayAdapter<CharSequence> capo_print = ArrayAdapter.createFromResource(getActivity(),
                R.array.capoprint,
                R.layout.my_spinner);
        capo_print.setDropDownViewResource(R.layout.my_spinner);
        edit_song_capo_print.setAdapter(capo_print);
        // Where is the key in the available array
        switch (FullscreenActivity.mCapoPrint) {
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
        pad_file = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, pad_option);
        pad_file.setDropDownViewResource(R.layout.my_spinner);
        edit_song_pad_file.setAdapter(pad_file);
        // Only allow auto for now (first index)
        if (FullscreenActivity.mPadFile.equals(auto)) {
            edit_song_pad_file.setSelection(1);
        } else if (FullscreenActivity.mPadFile.equals(link)) {
            edit_song_pad_file.setSelection(2);
        } else {
            edit_song_pad_file.setSelection(0);
        }

        // Now the seekbars
        String temp_tempo = FullscreenActivity.mTempo;
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
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());

        return V;
    }

    public void cancelEdit() {
        // Load the song back up with the default values
        try {
            LoadXML.loadXML(getActivity(), preferences, listSongFiles, storageAccess);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dismiss();
    }

    private class seekBarListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            String newtext = getResources().getString(R.string.notset);
            temposlider = edit_song_tempo.getProgress()+39;
            if (temposlider>39) {
                newtext = temposlider+" "+getResources().getString(R.string.bpm);
            }
            tempo_text.setText(newtext);
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {}
    }

    public static void prepareBlankSongXML() {
        // Prepare the new XML file
        String myNEWXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        myNEWXML += "<song>\n";
        myNEWXML += "  <title>" + FullscreenActivity.songfilename + "</title>\n";
        myNEWXML += "  <author></author>\n";
        myNEWXML += "  <copyright></copyright>\n";
        myNEWXML += "  <presentation></presentation>\n";
        myNEWXML += "  <hymn_number></hymn_number>\n";
        myNEWXML += "  <capo print=\"\"></capo>\n";
        myNEWXML += "  <tempo></tempo>\n";
        myNEWXML += "  <time_sig></time_sig>\n";
        myNEWXML += "  <duration></duration>\n";
        myNEWXML += "  <predelay></predelay>\n";
        myNEWXML += "  <ccli></ccli>\n";
        myNEWXML += "  <theme></theme>\n";
        myNEWXML += "  <alttheme></alttheme>\n";
        myNEWXML += "  <user1></user1>\n";
        myNEWXML += "  <user2></user2>\n";
        myNEWXML += "  <user3></user3>\n";
        myNEWXML += "  <key></key>\n";
        myNEWXML += "  <aka></aka>\n";
        myNEWXML += "  <key_line></key_line>\n";
        myNEWXML += "  <books></books>\n";
        myNEWXML += "  <midi></midi>\n";
        myNEWXML += "  <midi_index></midi_index>\n";
        myNEWXML += "  <pitch></pitch>\n";
        myNEWXML += "  <restrictions></restrictions>\n";
        myNEWXML += "  <notes></notes>\n";
        myNEWXML += "  <lyrics>[V]\n</lyrics>\n";
        myNEWXML += "  <linked_songs></linked_songs>\n";
        myNEWXML += "  <pad_file></pad_file>\n";
        myNEWXML += "  <custom_chords></custom_chords>\n";
        myNEWXML += "  <link_youtube></link_youtube>\n";
        myNEWXML += "  <link_web></link_web>\n";
        myNEWXML += "  <link_audio></link_audio>\n";
        myNEWXML += "  <loop_audio>false</loop_audio>\n";
        myNEWXML += "  <link_other></link_other>\n";
        myNEWXML += "  <abcnotation></abcnotation>\n";
        myNEWXML += "</song>";
        FullscreenActivity.mynewXML = myNEWXML;
    }

    public static void prepareSongXML() {
        // Prepare the new XML file

        if (FullscreenActivity.mEncoding==null || FullscreenActivity.mEncoding.equals("")) {
            FullscreenActivity.mEncoding = "UTF-8";
        }
        String myNEWXML = "<?xml version=\"1.0\" encoding=\""+FullscreenActivity.mEncoding+"\"?>\n";
        myNEWXML += "<song>\n";
        myNEWXML += "  <title>" + parseToHTMLEntities(FullscreenActivity.mTitle.toString()) + "</title>\n";
        myNEWXML += "  <author>" + parseToHTMLEntities(FullscreenActivity.mAuthor.toString()) + "</author>\n";
        myNEWXML += "  <copyright>" + parseToHTMLEntities(FullscreenActivity.mCopyright.toString()) + "</copyright>\n";
        myNEWXML += "  <presentation>" + parseToHTMLEntities(FullscreenActivity.mPresentation) + "</presentation>\n";
        myNEWXML += "  <hymn_number>" + parseToHTMLEntities(FullscreenActivity.mHymnNumber) + "</hymn_number>\n";
        myNEWXML += "  <capo print=\"" + parseToHTMLEntities(FullscreenActivity.mCapoPrint) + "\">" + parseToHTMLEntities(FullscreenActivity.mCapo) + "</capo>\n";
        myNEWXML += "  <tempo>" + parseToHTMLEntities(FullscreenActivity.mTempo) + "</tempo>\n";
        myNEWXML += "  <time_sig>" + parseToHTMLEntities(FullscreenActivity.mTimeSig) + "</time_sig>\n";
        myNEWXML += "  <duration>" + parseToHTMLEntities(FullscreenActivity.mDuration) + "</duration>\n";
        myNEWXML += "  <predelay>" + parseToHTMLEntities(FullscreenActivity.mPreDelay) + "</predelay>\n";
        myNEWXML += "  <ccli>" + parseToHTMLEntities(FullscreenActivity.mCCLI) + "</ccli>\n";
        myNEWXML += "  <theme>" + parseToHTMLEntities(FullscreenActivity.mTheme) + "</theme>\n";
        myNEWXML += "  <alttheme>" + parseToHTMLEntities(FullscreenActivity.mAltTheme) + "</alttheme>\n";
        myNEWXML += "  <user1>" + parseToHTMLEntities(FullscreenActivity.mUser1) + "</user1>\n";
        myNEWXML += "  <user2>" + parseToHTMLEntities(FullscreenActivity.mUser2) + "</user2>\n";
        myNEWXML += "  <user3>" + parseToHTMLEntities(FullscreenActivity.mUser3) + "</user3>\n";
        myNEWXML += "  <key>" + parseToHTMLEntities(FullscreenActivity.mKey) + "</key>\n";
        myNEWXML += "  <aka>" + parseToHTMLEntities(FullscreenActivity.mAka) + "</aka>\n";
        myNEWXML += "  <key_line>" + parseToHTMLEntities(FullscreenActivity.mKeyLine) + "</key_line>\n";
        myNEWXML += "  <books>" + parseToHTMLEntities(FullscreenActivity.mBooks) + "</books>\n";
        myNEWXML += "  <midi>" + parseToHTMLEntities(FullscreenActivity.mMidi) + "</midi>\n";
        myNEWXML += "  <midi_index>" + parseToHTMLEntities(FullscreenActivity.mMidiIndex) + "</midi_index>\n";
        myNEWXML += "  <pitch>" + parseToHTMLEntities(FullscreenActivity.mPitch) + "</pitch>\n";
        myNEWXML += "  <restrictions>" + parseToHTMLEntities(FullscreenActivity.mRestrictions) + "</restrictions>\n";
        myNEWXML += "  <notes>" + parseToHTMLEntities(FullscreenActivity.mNotes) + "</notes>\n";
        myNEWXML += "  <lyrics>" + parseToHTMLEntities(FullscreenActivity.mLyrics) + "</lyrics>\n";
        myNEWXML += "  <linked_songs>" + parseToHTMLEntities(FullscreenActivity.mLinkedSongs) + "</linked_songs>\n";
        myNEWXML += "  <pad_file>" + parseToHTMLEntities(FullscreenActivity.mPadFile) + "</pad_file>\n";
        myNEWXML += "  <custom_chords>" + parseToHTMLEntities(FullscreenActivity.mCustomChords) + "</custom_chords>\n";
        myNEWXML += "  <link_youtube>" + parseToHTMLEntities(FullscreenActivity.mLinkYouTube) + "</link_youtube>\n";
        myNEWXML += "  <link_web>" + parseToHTMLEntities(FullscreenActivity.mLinkWeb) + "</link_web>\n";
        myNEWXML += "  <link_audio>" + parseToHTMLEntities(FullscreenActivity.mLinkAudio) + "</link_audio>\n";
        myNEWXML += "  <loop_audio>" + parseToHTMLEntities(FullscreenActivity.mLoopAudio) + "</loop_audio>\n";
        myNEWXML += "  <link_other>" + parseToHTMLEntities(FullscreenActivity.mLinkOther) + "</link_other>\n";
        myNEWXML += "  <abcnotation>" + parseToHTMLEntities(FullscreenActivity.mNotation) + "</abcnotation>\n";

        if (!FullscreenActivity.mExtraStuff1.isEmpty()) {
            myNEWXML += "  " + FullscreenActivity.mExtraStuff1 + "\n";
        }
        if (!FullscreenActivity.mExtraStuff2.isEmpty()) {
            myNEWXML += "  " + FullscreenActivity.mExtraStuff2 + "\n";
        }
        myNEWXML += "</song>";

        FullscreenActivity.mynewXML = myNEWXML;
    }

    public interface MyInterface {
        void rebuildSearchIndex();
    }

    public static String parseToHTMLEntities(String val) {
        if (val==null) {
            val = "";
        }
        // Make sure all vals are unencoded to start with
        // Now HTML encode everything that needs encoded
        // Protected are < > &
        // Change < to __lt;  We'll later replace the __ with &.  Do this to deal with &amp; separately
        val = val.replace("<","__lt;");
        val = val.replace("&lt;","__lt;");

        // Change > to __gt;  We'll later replace the __ with &.  Do this to deal with &amp; separately
        val = val.replace(">","__gt;");
        val = val.replace("&gt;","__gt;");

        // Change &apos; to ' as they don't need encoding in this format - also makes it compatible with desktop
        val = val.replace("&apos;","'");
        val = val.replace("\'","'");

        // Change " to __quot;  We'll later replace the __ with &.  Do this to deal with &amp; separately
        val = val.replace("\"","__quot;");
        val = val.replace("&quot;","__quot;");

        // Now deal with the remaining ampersands
        val = val.replace("&amp;","&");  // Reset any that already encoded - all need encoded now
        val = val.replace("&&","&");     // Just in case we have wrongly encoded old ones e.g. &amp;&quot;
        val = val.replace("&","&amp;");  // Reencode all remaining ampersands

        // Now replace the other protected encoded entities back with their leading ampersands
        val = val.replace("__lt;","&lt;");
        val = val.replace("__gt;","&gt;");
        val = val.replace("__quot;","&quot;");

        return val;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

    void setCapoSpinner(String mKey) {
        ArrayList<String> capooptions = Transpose.quickCapoKey(mKey);
        ArrayAdapter<String> aa = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, capooptions);
        aa.notifyDataSetChanged();
        edit_song_capo.setAdapter(aa);
        // Where is the key in the available array
        int index = -1;
        List<String> capo_choice = Arrays.asList(getResources().getStringArray(R.array.capo));
        for (int w=0;w<capo_choice.size();w++) {
            if (FullscreenActivity.mCapo.equals(capo_choice.get(w))) {
                index = w;
            }
        }
        edit_song_capo.setSelection(index);
    }

    public void changeFormat(boolean chordpro) {
        String textLyrics = edit_song_lyrics.getText().toString();
        Log.d("d",textLyrics);
        if (chordpro) {
            edit_song_lyrics.setText(chordProConvert.fromOpenSongToChordPro(textLyrics, getActivity()));
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
}