package com.garethevans.church.opensongtablet.interfaces;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public interface MainActivityInterface {
    void hideActionButton(boolean hide);
    void hideActionBar(boolean hide);
    void updateToolbar(String what);
    void showTutorial(String what);
    void indexSongs();
    void initialiseActivity();
    void moveToSongInSongMenu();
    void hideKeyboard();
    void navigateToFragment(int id);
    void returnToHome(Fragment fragment, Bundle bundle);
    void songMenuActionButtonShow(boolean show);
    void lockDrawer(boolean lock);
    void doSongLoad();
    void loadSongFromSet();
    void updateKeyAndLyrics(String key, String lyrics);
    void editSongSaveButtonAnimation(boolean pulse);
    void registerFragment(Fragment frag, String what);
    void displayAreYouSure(String what, String action, ArrayList<String> arguments);
    void confirmedAction(boolean agree, String what, ArrayList<String> arguments);
    void refreshAll();
    void doExport(String what);
    void refreshSetList();
    void openDialog(DialogFragment frag, String tag);
    void updateFragment(String what, Fragment callingFragment);
}
