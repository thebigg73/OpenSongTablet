package com.garethevans.church.opensongtablet;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

class OpenFragment {

    public static DialogFragment openFragment(Context c) {

        DialogFragment newFragment = null;
        String message;

        switch (FullscreenActivity.whattodo) {
            case "loadset":
            case "saveset":
            case "deleteset":
            case "exportset":
            case "managesets":
                newFragment = PopUpListSetsFragment.newInstance();
                break;

            case "clearset":
                message = c.getResources().getString(R.string.options_clearthisset);
                newFragment = PopUpAreYouSureFragment.newInstance(message);
                break;

            case "deletesong":
                message = c.getResources().getString(R.string.options_song_delete) +
                        " \"" + FullscreenActivity.songfilename + "\"?";
                newFragment = PopUpAreYouSureFragment.newInstance(message);
                break;

            case "customcreate":
                newFragment = PopUpCustomSlideFragment.newInstance();
                break;

            case "customreusable_scripture":
                newFragment = PopUpCustomSlideFragment.newInstance();
                break;

            case "customreusable_slide":
                newFragment = PopUpCustomSlideFragment.newInstance();
                break;

            case "songdetails":
                newFragment = PopUpSongDetailsFragment.newInstance();
                break;

            case "alert":
                newFragment = PopUpAlertFragment.newInstance();
                break;

            case "presenter_background":
                newFragment = PopUpBackgroundsFragment.newInstance();
                break;

            case "presenter_layout":
                newFragment = PopUpLayoutFragment.newInstance();
                break;

            case "presenter_audio":
                newFragment = PopUpMediaStoreFragment.newInstance();
                break;

            case "presenter_db":
                newFragment = PopUpSoundLevelMeterFragment.newInstance();
                break;

            case "editset":
            case "setitemvariation":
                newFragment = PopUpSetViewNew.newInstance();
                break;

            case "editsong":
                if (justSong(c)) {
                    newFragment = PopUpEditSongFragment.newInstance();
                }
                break;

            case "editnotes":
                if (justSong(c)) {
                    newFragment = PopUpEditStickyFragment.newInstance();
                }
                break;

            case "renamesong":
                newFragment = PopUpSongRenameFragment.newInstance();
                break;

            case "createsong":
                newFragment = PopUpSongCreateFragment.newInstance();
                break;

            case "transpose":
                if (justSong(c)) {
                    newFragment = PopUpTransposeFragment.newInstance();
                }
                break;

            case "chordformat":
                newFragment = PopUpChordFormatFragment.newInstance();
                break;

            case "changetheme":
                newFragment = PopUpThemeChooserFragment.newInstance();
                break;

            case "autoscale":
                newFragment = PopUpScalingFragment.newInstance();
                break;

            case "changefonts":
                newFragment = PopUpFontsFragment.newInstance();
                break;

            case "connecteddisplay":
                newFragment = PopUpLayoutFragment.newInstance();
                break;

            case "pagebuttons":
                newFragment = PopUpPageButtonsFragment.newInstance();
                break;

            case "groupedpagebuttons":
                newFragment = PopUpGroupedPageButtonsFragment.newInstance();
                break;

            case "popupsettings":
                newFragment = PopUpDefaultsFragment.newInstance();
                break;

            case "extra":
                newFragment = PopUpExtraInfoFragment.newInstance();
                break;

            case "profiles":
                newFragment = PopUpProfileFragment.newInstance();
                break;

            case "footpedal":
                newFragment = PopUpPedalsFragment.newInstance();
                break;

            case "quicklaunch":
                newFragment = PopUpQuickLaunchSetup.newInstance();
                break;

            case "gestures":
                newFragment = PopUpGesturesFragment.newInstance();
                break;

            case "menuoptions":
                newFragment = PopUpMenuSettingsFragment.newInstance();
                break;

            case "newfolder":
                message = "create";
                newFragment = PopUpSongFolderRenameFragment.newInstance(message);
                break;

            case "editfoldername":
                message = "rename";
                newFragment = PopUpSongFolderRenameFragment.newInstance(message);
                break;

            case "managestorage":
                newFragment = PopUpStorageFragment.newInstance();
                break;

            case "exportsonglist":
                newFragment = PopUpExportSongListFragment.newInstance();
                break;

            case "customstoragefind":
                newFragment = PopUpDirectoryChooserFragment.newInstance();
                Bundle args = new Bundle();
                args.putString("type", "folder");
                newFragment.setArguments(args);
                break;

            case "filechooser":
                newFragment = PopUpDirectoryChooserFragment.newInstance();
                Bundle args2 = new Bundle();
                args2.putString("type", "file");
                newFragment.setArguments(args2);
                break;

            case "wipeallsongs":
                newFragment = PopUpAreYouSureFragment.newInstance(c.getResources().getString(R.string.wipesongs));
                break;

            case "errorlog":
                newFragment = PopUpWebViewFragment.newInstance();
                break;

            case "crossfade":
                newFragment = PopUpCrossFadeFragment.newInstance();
                break;

            case "autoscrolldefaults":
                newFragment = PopUpAutoScrollDefaultsFragment.newInstance();
                break;

            case "language":
                newFragment = PopUpLanguageFragment.newInstance();
                break;

            case "fullsearch":
                newFragment = PopUpFullSearchFragment.newInstance();
                break;

            case "choosefolder":
                newFragment = PopUpChooseFolderFragment.newInstance();
                break;

            case "choosechordformat":
                newFragment = PopUpChordFormatFragment.newInstance();
                break;

            // OLD
            case "importosb":
                newFragment = PopUpImportExternalFile.newInstance();
                break;

            case "findosbfiles":
                newFragment = PopUpDirectoryChooserFragment.newInstance();
                Bundle argsosb = new Bundle();
                argsosb.putString("type", "findosbfiles");
                newFragment.setArguments(argsosb);
                break;

            case "processimportosb":
                newFragment = PopUpImportExportOSBFragment.newInstance();
                break;

            case "exportosb":
                newFragment = PopUpImportExportOSBFragment.newInstance();
                break;

            case "importos":
                newFragment = PopUpImportExternalFile.newInstance();
                break;

            case "page_pad":
                newFragment = PopUpPadFragment.newInstance();
                break;

            case "page_autoscroll":
                newFragment = PopUpAutoscrollFragment.newInstance();
                break;

            case "page_metronome":
                if (justSong(c)) {
                    newFragment = PopUpMetronomeFragment.newInstance();
                }
                break;

            case "page_chords":
                if (justSong(c)) {
                    newFragment = PopUpChordsFragment.newInstance();
                }
                break;

            case "customchords":
                if (justSong(c)) {
                    newFragment = PopUpCustomChordsFragment.newInstance();
                }
                break;

            case "page_links":
                if (justSong(c)) {
                    newFragment = PopUpLinks.newInstance();
                }
                break;

            case "page_sticky":
                if (justSong(c)) {
                    newFragment = PopUpStickyFragment.newInstance();
                }
                break;

            case "page_pageselect":
                newFragment = PopUpPagesFragment.newInstance();
                break;

            case "songlongpress":
                newFragment = PopUpLongSongPressFragment.newInstance();
                break;

            case "chordie":
            case "ultimate-guitar":
            case "worshipready":
                newFragment = PopUpFindNewSongsFragment.newInstance();
                break;

            case "savecameraimage":
                newFragment = PopUpSongCreateFragment.newInstance();
                break;

        }

        return newFragment;
    }

    static String getMessage(Context c) {
        String message = "dialog";

        switch (FullscreenActivity.whattodo) {
            case "clearset":
                message = c.getResources().getString(R.string.options_clearthisset);
                break;

            case "deletesong":
                message = c.getResources().getString(R.string.options_song_delete) +
                        " \"" + FullscreenActivity.songfilename + "\"?";
                break;

            case "newfolder":
                message = "create";
                break;

            case "editfoldername":
                message = "rename";
                break;

        }
        return message;
    }

    private static boolean justSong(Context c) {
        boolean isallowed = true;
        if (FullscreenActivity.isImage || FullscreenActivity.isPDF || !FullscreenActivity.isSong) {
            FullscreenActivity.myToastMessage = c.getResources().getString(R.string.not_allowed);
            ShowToast.showToast(c);
            isallowed = false;
        }
        return isallowed;
    }

}