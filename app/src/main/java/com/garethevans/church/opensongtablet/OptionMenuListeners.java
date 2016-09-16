package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class OptionMenuListeners extends Activity {

    public static int mychild;
    public static int myparent;

    public interface MyInterface {
        void optionLongClick(int myparent, int mychild);
        void openFragment();
        void closeMyDrawers(String which);
        void refreshActionBar();
        void loadSong();
        void shareSong();
        void showActionBar();
        void hideActionBar();
        void onSongImport();
        void prepareSongMenu();
        void rebuildSearchIndex();
        void callIntent(String what, Intent intent);
        void prepareOptionMenu();
        void findSongInFolders();
    }

    static DialogFragment newFragment;
    public static MyInterface mListener;

    public static void optionListeners(View v, Context c) {
        mListener = (MyInterface) c;

        // Identify the buttons
        Button menuSetButton = (Button) v.findViewById(R.id.menuSetButton);
        Button menuSongButton = (Button) v.findViewById(R.id.menuSongButton);
        Button menuChordsButton = (Button) v.findViewById(R.id.menuChordsButton);
        Button menuDisplayButton = (Button) v.findViewById(R.id.menuDisplayButton);
        Button menuGesturesButton = (Button) v.findViewById(R.id.menuGesturesButton);
        Button menuStorageButton = (Button) v.findViewById(R.id.menuStorageButton);
        Button menuPadButton = (Button) v.findViewById(R.id.menuPadButton);
        Button menuAutoScrollButton = (Button) v.findViewById(R.id.menuAutoScrollButton);
        Button menuOtherButton = (Button) v.findViewById(R.id.menuOtherButton);

        // Capitalise all the text by locale
        menuSetButton.setText(c.getString(R.string.options_set).toUpperCase(FullscreenActivity.locale));
        menuSongButton.setText(c.getString(R.string.options_song).toUpperCase(FullscreenActivity.locale));
        menuChordsButton.setText(c.getString(R.string.chords).toUpperCase(FullscreenActivity.locale));
        menuDisplayButton.setText(c.getString(R.string.options_display).toUpperCase(FullscreenActivity.locale));
        menuGesturesButton.setText(c.getString(R.string.options_gesturesandmenus).toUpperCase(FullscreenActivity.locale));
        menuStorageButton.setText(c.getString(R.string.options_storage).toUpperCase(FullscreenActivity.locale));
        menuPadButton.setText(c.getString(R.string.pad).toUpperCase(FullscreenActivity.locale));
        menuAutoScrollButton.setText(c.getString(R.string.autoscroll).toUpperCase(FullscreenActivity.locale));
        menuOtherButton.setText(c.getString(R.string.options_other).toUpperCase(FullscreenActivity.locale));

        // Set the listeners
        menuSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "setmenu";
                mListener.openFragment();
                mListener.closeMyDrawers("option");
            }
        });
        menuSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "songmenu";
                mListener.openFragment();
                mListener.closeMyDrawers("option");
            }
        });

    }

    public static void createFullMenu(Context c) {
        mListener = (MyInterface) c;

        // preparing list data
        FullscreenActivity.listDataHeaderOption = new ArrayList<>();
        FullscreenActivity.listDataChildOption = new HashMap<>();

        // Adding headers for option menu data
        FullscreenActivity.listDataHeaderOption.add(c.getResources().getString(R.string.options_set).toUpperCase(FullscreenActivity.locale));
        FullscreenActivity.listDataHeaderOption.add(c.getResources().getString(R.string.options_song).toUpperCase(FullscreenActivity.locale));
        FullscreenActivity.listDataHeaderOption.add(c.getResources().getString(R.string.chords).toUpperCase(FullscreenActivity.locale));
        FullscreenActivity.listDataHeaderOption.add(c.getResources().getString(R.string.options_display).toUpperCase(FullscreenActivity.locale));
        FullscreenActivity.listDataHeaderOption.add(c.getResources().getString(R.string.options_gesturesandmenus).toUpperCase(FullscreenActivity.locale));
        FullscreenActivity.listDataHeaderOption.add(c.getResources().getString(R.string.options_storage).toUpperCase(FullscreenActivity.locale));
        FullscreenActivity.listDataHeaderOption.add(c.getResources().getString(R.string.pad).toUpperCase(FullscreenActivity.locale));
        FullscreenActivity.listDataHeaderOption.add(c.getResources().getString(R.string.autoscroll).toUpperCase(FullscreenActivity.locale));
        FullscreenActivity.listDataHeaderOption.add(c.getResources().getString(R.string.options_other).toUpperCase(FullscreenActivity.locale));

        // Adding child data
        List<String> options_set = new ArrayList<>();
        options_set.add(c.getResources().getString(R.string.options_set_load));
        options_set.add(c.getResources().getString(R.string.options_set_save));
        options_set.add(c.getResources().getString(R.string.options_set_clear));
        options_set.add(c.getResources().getString(R.string.options_set_delete));
        options_set.add(c.getResources().getString(R.string.options_set_export));
        options_set.add(c.getResources().getString(R.string.add_custom_slide));
        options_set.add(c.getResources().getString(R.string.options_set_edit));
        options_set.add(c.getResources().getString(R.string.customise_set_item));

        options_set.add("");

        // Parse the saved set
        FullscreenActivity.mySet = FullscreenActivity.mySet.replace("_**$$**_", "_**$%%%$**_");
        // Break the saved set up into a new String[]
        FullscreenActivity.mSetList = FullscreenActivity.mySet.split("%%%");
        // Restore the set back to what it was
        FullscreenActivity.mySet = FullscreenActivity.mySet.replace("_**$%%%$**_", "_**$$**_");
        FullscreenActivity.setSize = FullscreenActivity.mSetList.length;

        for (int r = 0; r < FullscreenActivity.mSetList.length; r++) {
            FullscreenActivity.mSetList[r] = FullscreenActivity.mSetList[r].replace("$**_", "");
            FullscreenActivity.mSetList[r] = FullscreenActivity.mSetList[r].replace("_**$", "");
            if (!FullscreenActivity.mSetList[r].isEmpty()) {
                options_set.add(FullscreenActivity.mSetList[r]);
            }
        }

        List<String> options_song = new ArrayList<>();
        options_song.add(c.getResources().getString(R.string.options_song_edit));
        options_song.add(c.getResources().getString(R.string.options_song_stickynotes));
        options_song.add(c.getResources().getString(R.string.options_song_rename));
        options_song.add(c.getResources().getString(R.string.options_song_delete));
        options_song.add(c.getResources().getString(R.string.options_song_new));
        options_song.add(c.getResources().getString(R.string.options_song_export));
        options_song.add(c.getResources().getString(R.string.edit_song_presentation));

        List<String> options_chords = new ArrayList<>();
        options_chords.add(c.getResources().getString(R.string.options_song_transpose));
        options_chords.add(c.getResources().getString(R.string.capo_toggle));
        options_chords.add(c.getResources().getString(R.string.options_song_sharp));
        options_chords.add(c.getResources().getString(R.string.options_song_flat));
        options_chords.add(c.getResources().getString(R.string.options_options_chordformat));
        options_chords.add(c.getResources().getString(R.string.options_song_convert));
        options_chords.add(c.getResources().getString(R.string.options_options_showchords));

        List<String> options_display = new ArrayList<>();
        options_display.add(c.getResources().getString(R.string.options_options_theme));
        options_display.add(c.getResources().getString(R.string.options_options_scale));
        options_display.add(c.getResources().getString(R.string.options_options_fonts));
        options_display.add(c.getResources().getString(R.string.pagebuttons));
        options_display.add(c.getResources().getString(R.string.extra));
        options_display.add(c.getResources().getString(R.string.options_options_hidebar));
        options_display.add(c.getResources().getString(R.string.profile));

        List<String> options_gesturesandmenus = new ArrayList<>();
        options_gesturesandmenus.add(c.getResources().getString(R.string.options_options_pedal));
        options_gesturesandmenus.add(c.getResources().getString(R.string.options_options_gestures));
        options_gesturesandmenus.add(c.getResources().getString(R.string.options_options_menuswipe));
        options_gesturesandmenus.add(c.getResources().getString(R.string.options_options_songswipe));

        List<String> options_storage = new ArrayList<>();
        options_storage.add(c.getResources().getString(R.string.options_song_newfolder));
        options_storage.add(c.getResources().getString(R.string.options_song_editfolder));
        options_storage.add(c.getResources().getString(R.string.storage_choose));
        options_storage.add(c.getResources().getString(R.string.import_onsong_choose));
        options_storage.add(c.getResources().getString(R.string.refreshsongs));
        options_storage.add(c.getResources().getString(R.string.search_rebuild));
        options_storage.add(c.getResources().getString(R.string.search_log));

        List<String> options_pad = new ArrayList<>();
        options_pad.add(c.getResources().getString(R.string.crossfade_time));

        List<String> options_autoscroll = new ArrayList<>();
        options_autoscroll.add(c.getResources().getString(R.string.default_autoscroll));
        options_autoscroll.add(c.getResources().getString(R.string.options_options_autostartscroll));

        List<String> options_other = new ArrayList<>();
        options_other.add(c.getResources().getString(R.string.options_options_help));
        options_other.add("@OpenSongApp");
        options_other.add(c.getResources().getString(R.string.language));
        options_other.add(c.getResources().getString(R.string.options_options_start));
        options_other.add(c.getResources().getString(R.string.rate));

        FullscreenActivity.listDataChildOption.put(FullscreenActivity.listDataHeaderOption.get(0), options_set); // Header, Child data
        FullscreenActivity.listDataChildOption.put(FullscreenActivity.listDataHeaderOption.get(1), options_song);
        FullscreenActivity.listDataChildOption.put(FullscreenActivity.listDataHeaderOption.get(2), options_chords);
        FullscreenActivity.listDataChildOption.put(FullscreenActivity.listDataHeaderOption.get(3), options_display);
        FullscreenActivity.listDataChildOption.put(FullscreenActivity.listDataHeaderOption.get(4), options_gesturesandmenus);
        FullscreenActivity.listDataChildOption.put(FullscreenActivity.listDataHeaderOption.get(5), options_storage);
        FullscreenActivity.listDataChildOption.put(FullscreenActivity.listDataHeaderOption.get(6), options_pad);
        FullscreenActivity.listDataChildOption.put(FullscreenActivity.listDataHeaderOption.get(7), options_autoscroll);
        FullscreenActivity.listDataChildOption.put(FullscreenActivity.listDataHeaderOption.get(8), options_other);

        mListener.refreshActionBar();
    }

    public static ExpandableListView.OnChildClickListener myShortClickListener(final Context c) {

        return new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                mListener = (MyInterface) c;

                // Close the drawers
                mListener.closeMyDrawers("both");

                if (!FullscreenActivity.removingfromset) {
                    String chosenMenu = FullscreenActivity.listDataHeaderOption.get(groupPosition);

                    if (chosenMenu.equals(c.getResources().getString(R.string.options_set).toUpperCase(FullscreenActivity.locale))) {

                        // Now check for set options clicks

                        // 0 = Load a set
                        // 1 = Save current set
                        // 2 = Clear current set
                        // 3 = Delete saved set
                        // 4 = Export saved set
                        // 5 = Add a custom slide
                        // 6 = Edit current set
                        // 7 = Make a variation of an item in the set
                        // 8 =
                        // 9+  Set items (songs,etc.)

                        // Load up a list of saved sets as it will likely be needed
                        SetActions.updateOptionListSets();
                        Arrays.sort(FullscreenActivity.mySetsFiles);
                        Arrays.sort(FullscreenActivity.mySetsDirectories);
                        Arrays.sort(FullscreenActivity.mySetsFileNames);
                        Arrays.sort(FullscreenActivity.mySetsFolderNames);

                        switch (childPosition) {
                            case 0:
                                // Load a set
                                FullscreenActivity.whattodo = "loadset";
                                mListener.openFragment();
                                mListener.prepareOptionMenu();
                                break;

                            case 1:
                                // Save current set
                                FullscreenActivity.whattodo = "saveset";
                                mListener.openFragment();
                                break;

                            case 2:
                                // Clear current set
                                FullscreenActivity.whattodo = "clearset";
                                mListener.openFragment();
                                break;

                            case 3:
                                // Delete saved set
                                FullscreenActivity.whattodo = "deleteset";
                                mListener.openFragment();
                                break;

                            case 4:
                                // Export current set
                                FullscreenActivity.whattodo = "exportset";
                                mListener.openFragment();
                                break;

                            case 5:
                                // Add a custom slide
                                FullscreenActivity.whattodo = "customcreate";
                                mListener.openFragment();
                                break;

                            case 6:
                                // Edit current set
                                FullscreenActivity.whattodo = "editset";
                                mListener.openFragment();
                                break;

                            case 7:
                                // Make a variation of an item in the set
                                FullscreenActivity.whattodo = "setitemvariation";
                                mListener.openFragment();
                                break;
                        }

                        if (childPosition > 8) {
                            // Load song in set
                            FullscreenActivity.setView = "Y";
                            FullscreenActivity.pdfPageCurrent = 0;
                            // Set item is 9 less than childPosition
                            FullscreenActivity.indexSongInSet = childPosition - 9;
                            FullscreenActivity.linkclicked = FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet];
                            if (FullscreenActivity.linkclicked == null) {
                                FullscreenActivity.linkclicked = "";
                            }
                            if (FullscreenActivity.indexSongInSet == 0) {
                                // Already first item
                                FullscreenActivity.previousSongInSet = "";
                            } else {
                                FullscreenActivity.previousSongInSet = FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet - 1];
                            }

                            if (FullscreenActivity.indexSongInSet == (FullscreenActivity.setSize - 1)) {
                                // Last item
                                FullscreenActivity.nextSongInSet = "";
                            } else {
                                FullscreenActivity.nextSongInSet = FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet + 1];
                            }
                            FullscreenActivity.whichDirection = "R2L";

                            if (!FullscreenActivity.linkclicked.contains("/")) {
                                // Right it doesn't, so add the /
                                FullscreenActivity.linkclicked = "/" + FullscreenActivity.linkclicked;
                            }

                            // Now split the FullscreenActivity.linkclicked into two song parts 0=folder 1=file
                            FullscreenActivity.songpart = FullscreenActivity.linkclicked.split("/");

                            if (FullscreenActivity.songpart.length < 2) {
                                FullscreenActivity.songpart = new String[2];
                                FullscreenActivity.songpart[0] = "";
                                FullscreenActivity.songpart[1] = "";
                            }

                            // If the folder length isn't 0, it is a folder
                            if (FullscreenActivity.songpart[0].length() > 0 && !FullscreenActivity.songpart[0].contains(FullscreenActivity.text_scripture) && !FullscreenActivity.songpart[0].contains(FullscreenActivity.image) && !FullscreenActivity.songpart[0].contains(FullscreenActivity.text_slide) && !FullscreenActivity.songpart[0].contains(FullscreenActivity.text_note)) {
                                FullscreenActivity.whichSongFolder = FullscreenActivity.songpart[0];

                            } else if (FullscreenActivity.songpart[0].length() > 0 && FullscreenActivity.songpart[0].contains(FullscreenActivity.text_scripture)) {
                                FullscreenActivity.whichSongFolder = "../Scripture/_cache";
                                FullscreenActivity.songpart[0] = "../Scripture/_cache";

                            } else if (FullscreenActivity.songpart[0].length() > 0 && FullscreenActivity.songpart[0].contains(FullscreenActivity.text_slide)) {
                                FullscreenActivity.whichSongFolder = "../Slides/_cache";
                                FullscreenActivity.songpart[0] = "../Slides/_cache";

                            } else if (FullscreenActivity.songpart[0].length() > 0 && FullscreenActivity.songpart[0].contains(FullscreenActivity.text_note)) {
                                FullscreenActivity.whichSongFolder = "../Notes/_cache";
                                FullscreenActivity.songpart[0] = "../Notes/_cache";

                            } else if (FullscreenActivity.songpart[0].length() > 0 && FullscreenActivity.songpart[0].contains(FullscreenActivity.image)) {
                                FullscreenActivity.whichSongFolder = "../Images/_cache";
                                FullscreenActivity.songpart[0] = "../Images/_cache";

                            } else {
                                FullscreenActivity.whichSongFolder = FullscreenActivity.mainfoldername;
                            }

                            FullscreenActivity.songfilename = null;
                            FullscreenActivity.songfilename = "";
                            FullscreenActivity.songfilename = FullscreenActivity.songpart[1];

                            // Save the preferences and load the song
                            Preferences.savePreferences();
                            mListener.refreshActionBar();
                            mListener.loadSong();
                        }

                    } else if (chosenMenu.equals(c.getResources().getString(R.string.options_song).toUpperCase(FullscreenActivity.locale))) {
                        FullscreenActivity.linkclicked = FullscreenActivity.listDataChildOption.get(FullscreenActivity.listDataHeaderOption.get(groupPosition)).get(childPosition);

                        // Now check for song options clicks

                        // 0 = Edit song
                        // 1 = Edit sticky notes
                        // 2 = Rename song
                        // 3 = Delete song
                        // 4 = New song
                        // 5 = Export song
                        // 6 = Presentation order

                        switch (childPosition) {
                            case 0:
                                // Edit song
                                if (FullscreenActivity.isPDF) {// Can't do this action on a pdf!
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.pdf_functionnotavailable);
                                    ShowToast.showToast(c);
                                } else if (!FullscreenActivity.isSong) {// Editing a slide / note / scripture / image
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.not_allowed);
                                    ShowToast.showToast(c);
                                } else {
                                    FullscreenActivity.whattodo = "editsong";
                                    mListener.openFragment();
                                }
                                break;

                            case 1:
                                // Edit sticky notes
                                if (FullscreenActivity.isPDF) {
                                    // Can't do this action on a pdf!
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.pdf_functionnotavailable);
                                    ShowToast.showToast(c);
                                } else if (!FullscreenActivity.isSong) {
                                    // Editing a slide / note / scripture / image
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.not_allowed);
                                    ShowToast.showToast(c);
                                } else {
                                    FullscreenActivity.whattodo = "editnotes";
                                    mListener.openFragment();
                                }
                                break;

                            case 2:
                                // Rename song
                                if (!FullscreenActivity.isPDF && !FullscreenActivity.isSong) {
                                    // Editing a slide / note / scripture / image
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.not_allowed);
                                    ShowToast.showToast(c);
                                } else {
                                    FullscreenActivity.whattodo = "renamesong";
                                    mListener.openFragment();
                                }
                                break;

                            case 3:
                                // Delete song
                                if (!FullscreenActivity.isPDF && !FullscreenActivity.isSong) {
                                    // Editing a slide / note / scripture / image
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.not_allowed);
                                    ShowToast.showToast(c);
                                } else {
                                    // Delete
                                    // Give the user an are you sure prompt!
                                    FullscreenActivity.whattodo = "deletesong";
                                    mListener.openFragment();
                                }
                                break;

                            case 4:
                                // New song
                                FullscreenActivity.whattodo = "createsong";
                                mListener.openFragment();
                                break;

                            case 5:
                                // Export song
                                mListener.shareSong();
                                break;

                            case 6:
                                // Presentation order
                                if (FullscreenActivity.usePresentationOrder) {
                                    FullscreenActivity.usePresentationOrder = false;
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.edit_song_presentation) + " - "
                                            + c.getResources().getString(R.string.off);
                                } else {
                                    FullscreenActivity.usePresentationOrder = true;
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.edit_song_presentation) + " - "
                                            + c.getResources().getString(R.string.on);
                                }
                                ShowToast.showToast(c);
                                Preferences.savePreferences();
                                mListener.loadSong();
                                break;
                        }


                    } else if (chosenMenu.equals(c.getResources().getString(R.string.chords).toUpperCase(FullscreenActivity.locale))) {
                        FullscreenActivity.linkclicked = FullscreenActivity.listDataChildOption.get(FullscreenActivity.listDataHeaderOption.get(groupPosition)).get(childPosition);

                        // Now check for chord options clicks

                        // 0 = Transpose
                        // 1 = Capo toggle
                        // 2 = Use # chords
                        // 3 = Use b chords
                        // 4 = Choose chord format
                        // 5 = Convert to preferred chord format
                        // 6 = Show/hide chords

                        switch (childPosition) {
                            case 0:
                                // Transpose

                                if (FullscreenActivity.isPDF) {
                                    // Can't do this action on a pdf!
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.pdf_functionnotavailable);
                                    ShowToast.showToast(c);
                                } else if (!FullscreenActivity.isSong) {
                                    // Editing a slide / note / scripture / image
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.not_allowed);
                                    ShowToast.showToast(c);
                                } else {
                                    FullscreenActivity.transposeDirection = "+1";
                                    FullscreenActivity.transposeTimes = 1;
                                    Transpose.checkChordFormat();
                                    FullscreenActivity.whattodo = "transpose";
                                    mListener.openFragment();
                                }
                                break;

                            case 1:
                                // Capo toggle
                                switch (FullscreenActivity.capoDisplay) {
                                    case "both":
                                        FullscreenActivity.capoDisplay = "capoonly";
                                        FullscreenActivity.myToastMessage = c.getResources().getString(R.string.capo_toggle_onlycapo);
                                        break;
                                    case "capoonly":
                                        FullscreenActivity.capoDisplay = "native";
                                        FullscreenActivity.myToastMessage = c.getResources().getString(R.string.capo_toggle_native);
                                        break;
                                    default:
                                        FullscreenActivity.capoDisplay = "both";
                                        FullscreenActivity.myToastMessage = c.getResources().getString(R.string.capo_toggle_bothcapo);
                                        break;
                                }
                                ShowToast.showToast(c);
                                Preferences.savePreferences();
                                mListener.loadSong();
                                break;

                            case 2:
                                // Use # chords
                                if (FullscreenActivity.isPDF) {
                                    // Can't do this action on a pdf!
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.pdf_functionnotavailable);
                                    ShowToast.showToast(c);
                                } else if (!FullscreenActivity.isSong) {
                                    // Editing a slide / note / scripture / image
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.not_allowed);
                                    ShowToast.showToast(c);
                                } else {
                                    FullscreenActivity.transposeStyle = "sharps";
                                    FullscreenActivity.transposeDirection = "0";
                                    FullscreenActivity.switchsharpsflats = true;
                                    Transpose.checkChordFormat();
                                    try {
                                        Transpose.doTranspose();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    FullscreenActivity.switchsharpsflats = false;
                                    mListener.loadSong();
                                }
                                break;

                            case 3:
                                // Use b chords
                                if (FullscreenActivity.isPDF) {
                                    // Can't do this action on a pdf!
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.pdf_functionnotavailable);
                                    ShowToast.showToast(c);
                                } else if (!FullscreenActivity.isSong) {
                                    // Editing a slide / note / scripture / image
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.not_allowed);
                                    ShowToast.showToast(c);
                                } else {
                                    FullscreenActivity.transposeStyle = "flats";
                                    FullscreenActivity.transposeDirection = "0";
                                    FullscreenActivity.switchsharpsflats = true;
                                    Transpose.checkChordFormat();
                                    try {
                                        Transpose.doTranspose();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    FullscreenActivity.switchsharpsflats = false;
                                    mListener.loadSong();
                                }
                                break;

                            case 4:
                                // Choose chord format
                                FullscreenActivity.whattodo = "chordformat";
                                mListener.openFragment();
                                break;

                            case 5:
                                // Convert to preferred chord format
                                if (FullscreenActivity.isPDF) {
                                    // Can't do this action on a pdf!
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.pdf_functionnotavailable);
                                    ShowToast.showToast(c);
                                } else if (!FullscreenActivity.isSong) {
                                    // Editing a slide / note / scripture / image
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.not_allowed);
                                    ShowToast.showToast(c);
                                } else {
                                    FullscreenActivity.transposeDirection = "0";
                                    //chord_converting = "Y";
                                    Transpose.checkChordFormat();
                                    try {
                                        Transpose.doTranspose();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    mListener.loadSong();
                                }
                                break;

                            case 6:
                                // Show/hide chords
                                if (FullscreenActivity.isPDF) {
                                    // Can't do this action on a pdf!
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.pdf_functionnotavailable);
                                    ShowToast.showToast(c);
                                } else {
                                    if (FullscreenActivity.showChords.equals("Y")) {
                                        FullscreenActivity.showChords = "N";
                                    } else {
                                        FullscreenActivity.showChords = "Y";
                                    }
                                    // Save the preferences
                                    Preferences.savePreferences();
                                    mListener.loadSong();
                                }
                                break;
                        }

                    } else if (chosenMenu.equals(c.getResources().getString(R.string.options_display).toUpperCase(FullscreenActivity.locale))) {
                        FullscreenActivity.linkclicked = FullscreenActivity.listDataChildOption.get(FullscreenActivity.listDataHeaderOption.get(groupPosition)).get(childPosition);

                        // Now check for display options clicks

                        // 0 = Change theme
                        // 1 = Toggle autoscale
                        // 2 = Fonts
                        // 3 = Page buttons
                        // 4 = Extra information
                        // 5 = Show/hide menu bar
                        // 6 = Profiles

                        switch (childPosition) {
                            case 0:
                                // Change theme
                                if (FullscreenActivity.isPDF) {
                                    // Can't do this action on a pdf!
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.pdf_functionnotavailable);
                                    ShowToast.showToast(c);
                                } else {
                                    FullscreenActivity.whattodo = "changetheme";
                                    mListener.openFragment();
                                }
                                break;

                            case 1:
                                // Toggle autoscale
                                FullscreenActivity.whattodo = "autoscale";
                                mListener.openFragment();
                                break;

                            case 2:
                                // Fonts
                                FullscreenActivity.whattodo = "changefonts";
                                mListener.openFragment();
                                break;

                            case 3:
                                // Page buttons
                                FullscreenActivity.whattodo = "pagebuttons";
                                mListener.openFragment();
                                break;

                            case 4:
                                // Extra information
                                FullscreenActivity.whattodo = "extra";
                                mListener.openFragment();
                                break;

                            case 5:
                                // Show/hide menu bar
                                if (FullscreenActivity.hideactionbaronoff.equals("Y")) {
                                    FullscreenActivity.hideactionbaronoff = "N";
                                    FullscreenActivity.myToastMessage = c.getResources()
                                            .getString(R.string.options_options_hidebar)
                                            + " - "
                                            + c.getResources().getString(R.string.off);
                                    ShowToast.showToast(c);
                                    mListener.showActionBar();


                                } else {
                                    FullscreenActivity.hideactionbaronoff = "Y";
                                    FullscreenActivity.myToastMessage = c.getResources()
                                            .getString(R.string.options_options_hidebar)
                                            + " - "
                                            + c.getResources().getString(R.string.on);
                                    ShowToast.showToast(c);
                                    mListener.hideActionBar();
                                }
                                break;

                            case 6:
                                // Profiles
                                FullscreenActivity.whattodo = "profiles";
                                mListener.openFragment();
                                break;
                        }


                    } else if (chosenMenu.equals(c.getResources().getString(R.string.options_gesturesandmenus).toUpperCase(FullscreenActivity.locale))) {
                        FullscreenActivity.linkclicked = FullscreenActivity.listDataChildOption.get(FullscreenActivity.listDataHeaderOption.get(groupPosition)).get(childPosition);

                        // Now check for gestures and menus options clicks

                        // 0 = Assign pedals
                        // 1 = Custom gestures
                        // 2 = Toggle menu swipe on/off
                        // 3 = Toggle song swipe on/off

                        switch (childPosition) {
                            case 0:
                                //Assign pedals
                                FullscreenActivity.whattodo = "footpedal";
                                mListener.openFragment();
                                break;

                            case 1:
                                // Custom gestures
                                FullscreenActivity.whattodo = "gestures";
                                mListener.openFragment();
                                break;

                            case 2:
                                // Toggle menu swipe on/off
                                if (FullscreenActivity.swipeDrawer.equals("Y")) {
                                    FullscreenActivity.swipeDrawer = "N";
                                    mListener.closeMyDrawers("locked");
                                    FullscreenActivity.myToastMessage = c.getResources().getString(
                                            R.string.drawerswipe)
                                            + " " + c.getResources().getString(R.string.off);
                                    ShowToast.showToast(c);
                                } else {
                                    FullscreenActivity.swipeDrawer = "Y";
                                    mListener.closeMyDrawers("unlocked");
                                    FullscreenActivity.myToastMessage = c.getResources().getString(
                                            R.string.drawerswipe)
                                            + " " + c.getResources().getString(R.string.on);
                                    ShowToast.showToast(c);
                                }
                                Preferences.savePreferences();
                                break;

                            case 3:
                                // Toggle song swipe on/off
                                switch (FullscreenActivity.swipeSet) {
                                    case "Y":
                                        FullscreenActivity.swipeSet = "S";
                                        Preferences.savePreferences();
                                        FullscreenActivity.myToastMessage = c.getResources()
                                                .getString(R.string.swipeSet)
                                                + " "
                                                + c.getResources().getString(R.string.on_set);
                                        ShowToast.showToast(c);
                                        break;
                                    case "S":
                                        FullscreenActivity.swipeSet = "N";
                                        Preferences.savePreferences();
                                        FullscreenActivity.myToastMessage = c.getResources()
                                                .getString(R.string.swipeSet)
                                                + " "
                                                + c.getResources().getString(R.string.off);
                                        ShowToast.showToast(c);
                                        break;
                                    default:
                                        FullscreenActivity.swipeSet = "Y";
                                        Preferences.savePreferences();
                                        FullscreenActivity.myToastMessage = c.getResources()
                                                .getString(R.string.swipeSet)
                                                + " "
                                                + c.getResources().getString(R.string.on);
                                        ShowToast.showToast(c);
                                        break;
                                }
                                break;
                        }


                    } else if (chosenMenu.equals(c.getResources().getString(R.string.options_storage).toUpperCase(FullscreenActivity.locale))) {
                        FullscreenActivity.linkclicked = FullscreenActivity.listDataChildOption.get(FullscreenActivity.listDataHeaderOption.get(groupPosition)).get(childPosition);

                        // Now check for storage options clicks

                        // 0 = Create new song folder
                        // 1 = Edit song folder name
                        // 2 = Manage storage
                        // 3 = Import OnSong
                        // 4 = Refresh songs menu
                        // 5 = Rebuild search index
                        // 6 = View search error log

                        switch (childPosition) {
                            case 0:
                                FullscreenActivity.whattodo = "newfolder";
                                mListener.openFragment();
                                break;

                            case 1:
                                FullscreenActivity.whattodo = "editfoldername";
                                mListener.openFragment();
                                break;

                            case 2:
                                // Manage storage
                                FullscreenActivity.whattodo = "managestorage";
                                mListener.openFragment();
                                break;

                            case 3:
                                // Import OnSong
                                mListener.onSongImport();
                                break;

                            case 4:
                                // Refresh songs menu
                                mListener.prepareSongMenu();
                                break;

                            case 5:
                                // Rebuild song index
                                FullscreenActivity.safetosearch = false;
                                mListener.rebuildSearchIndex();
                                break;

                            case 6:
                                // View search error log
                                FullscreenActivity.whattodo = "errorlog";
                                mListener.openFragment();
                                break;
                        }


                    } else if (chosenMenu.equals(c.getResources().getString(R.string.pad).toUpperCase(FullscreenActivity.locale))) {
                        FullscreenActivity.linkclicked = FullscreenActivity.listDataChildOption.get(FullscreenActivity.listDataHeaderOption.get(groupPosition)).get(childPosition);

                        // Now check for pad options clicks

                        // 0 = Cross fade time

                        switch (childPosition) {
                            case 0:
                                FullscreenActivity.whattodo = "crossfade";
                                mListener.openFragment();
                                break;
                        }


                    } else if (chosenMenu.equals(c.getResources().getString(R.string.autoscroll).toUpperCase(FullscreenActivity.locale))) {
                        FullscreenActivity.linkclicked = FullscreenActivity.listDataChildOption.get(FullscreenActivity.listDataHeaderOption.get(groupPosition)).get(childPosition);

                        // Now check for autoscroll options clicks

                        // 0 = Autoscroll defaults
                        // 1 = Autostart autoscroll

                        switch (childPosition) {
                            case 0:
                                // Autoscroll delay time
                                FullscreenActivity.whattodo = "autoscrolldefaults";
                                mListener.openFragment();
                                break;

                            case 1:
                                // Autostart autoscroll
                                if (FullscreenActivity.autostartautoscroll) {
                                    FullscreenActivity.autostartautoscroll = false;
                                    Preferences.savePreferences();
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.options_options_autostartscroll) + " - " + c.getResources().getString(R.string.off);
                                    ShowToast.showToast(c);
                                } else {
                                    FullscreenActivity.autostartautoscroll = true;
                                    Preferences.savePreferences();
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.options_options_autostartscroll) + " - " + c.getResources().getString(R.string.on);
                                    ShowToast.showToast(c);
                                }
                                mListener.loadSong();
                        }


                    } else if (chosenMenu.equals(c.getResources().getString(R.string.options_other).toUpperCase(FullscreenActivity.locale))) {
                        FullscreenActivity.linkclicked = FullscreenActivity.listDataChildOption.get(FullscreenActivity.listDataHeaderOption.get(groupPosition)).get(childPosition);

                        // Now check for other options clicks

                        // 0 = Help
                        // 1 = Twitter
                        // 2 = Language
                        // 3 = Start/splash screen
                        // 4 = Rate this app
                        Intent i;
                        switch (childPosition) {
                            case 0:
                                // Help
                                String url = "https://www.opensongapp.com";
                                i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(url));
                                mListener.callIntent("web", i);
                                break;

                            case 1:
                                // Twitter
                                mListener.callIntent("twitter", null);
                                break;

                            case 2:
                                // Choose Language
                                FullscreenActivity.whattodo = "language";
                                mListener.openFragment();
                                break;

                            case 3:
                                // Start/splash screen
                                // First though, set the preference to show the current version
                                // Otherwise it won't show the splash screen
                                SharedPreferences settings = c.getSharedPreferences("mysettings",
                                        Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putInt("showSplashVersion", 0);
                                editor.apply();
                                i = new Intent();
                                i.setClass(c, SettingsActivity.class);
                                mListener.callIntent("activity", i);
                                break;

                            case 4:
                                // Rate this app
                                String appPackage = c.getPackageName();
                                i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackage));
                                mListener.callIntent("web", i);
                                break;
                        }
                    }
                }
                return false;
            }
        };
    }

    public static ExpandableListView.OnItemLongClickListener myLongClickListener(final Context c) {

        return new ExpandableListView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mListener = (MyInterface) c;
                FullscreenActivity.removingfromset = true;
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    int childPosition = ExpandableListView.getPackedPositionChild(id);
                    FullscreenActivity.myOptionListClickedItem = position;

                    if (FullscreenActivity.myOptionListClickedItem >8 && groupPosition == 0) {
                        // Long clicking on the 9th or later options will remove the
                        // song from the set (all occurrences)
                        // Remove this song from the set. Remember it has tags at the start and end
                        // Vibrate to indicate something has happened
                        Vibrator vb = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
                        vb.vibrate(50);

                        // Take away the menu items (9)
                        String tempSong = FullscreenActivity.mSetList[childPosition - 9];
                        FullscreenActivity.mSetList[childPosition - 9] = "";

                        if (FullscreenActivity.indexSongInSet == (childPosition - 9)) {
                            FullscreenActivity.setView = "N";
                        }

                        FullscreenActivity.mySet = "";
                        for (String aMSetList : FullscreenActivity.mSetList) {
                            if (!aMSetList.isEmpty()) {
                                FullscreenActivity.mySet = FullscreenActivity.mySet + "$**_" + aMSetList + "_**$";
                            }
                        }

                        // Tell the user that the song has been removed.
                        FullscreenActivity.myToastMessage = "\"" + tempSong + "\" "
                                + c.getResources().getString(R.string.removedfromset);
                        ShowToast.showToast(c);

                        // Save set
                        Preferences.savePreferences();

                        mListener.optionLongClick(myparent,mychild);
                    }
                }
                FullscreenActivity.removingfromset = false;
                return false;
            }
        };
    }
}