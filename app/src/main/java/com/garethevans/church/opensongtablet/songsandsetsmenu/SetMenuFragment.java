package com.garethevans.church.opensongtablet.songsandsetsmenu;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.MenuSetsBinding;
import com.garethevans.church.opensongtablet.filemanagement.LoadSong;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;
import com.garethevans.church.opensongtablet.setprocessing.CurrentSet;
import com.garethevans.church.opensongtablet.setprocessing.SetActions;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.ArrayList;
import java.util.List;

public class SetMenuFragment extends Fragment {

    MenuSetsBinding myView;
    StorageAccess storageAccess;
    Preferences preferences;
    SetActions setActions;
    LoadSong loadSong;
    ProcessSong processSong;
    CurrentSet currentSet;
    ShowToast showToast;
    Song song;

    LinearLayoutManager llm;

    private MainActivityInterface mainActivityInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        mainActivityInterface = (MainActivityInterface) context;
        super.onAttach(context);
    }
    public interface MyInterface {
        void loadSongFromSet();
        //void shuffleSongsInSet();
        void confirmedAction();
        void refreshAll();
        //void closePopUps();
        //void pageButtonAlpha(String s);
        //void windowFlags();
        //void openFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = MenuSetsBinding.inflate(inflater, container, false);

        // Get the helpers
        getHelpers();

        new Thread(() -> {
            requireActivity().runOnUiThread(() -> {
                myView.progressBar.setVisibility(View.VISIBLE);
                myView.myRecyclerView.setVisibility(View.GONE);
                setUpViews();
            });

            requireActivity().runOnUiThread(this::prepareSetListViews);

            // Try to move to the corresponding item in the set that we are viewing.
            requireActivity().runOnUiThread(() -> {
                // If the song is found (indexSongInSet>-1 and lower than the number of items shown), smooth scroll to it
                if (currentSet.getIndexSongInSet()>-1 && currentSet.getIndexSongInSet() < currentSet.getCurrentSet().size()) {
                    llm.scrollToPositionWithOffset(currentSet.getIndexSongInSet() , 0);
                }
                myView.myRecyclerView.setVisibility(View.VISIBLE);
                myView.progressBar.setVisibility(View.GONE);
            });
        }).start();

        return myView.getRoot();
    }

    private void getHelpers() {
        storageAccess = mainActivityInterface.getStorageAccess();
        preferences = mainActivityInterface.getPreferences();
        setActions = mainActivityInterface.getSetActions();
        loadSong = mainActivityInterface.getLoadSong();
        processSong = mainActivityInterface.getProcessSong();
        currentSet = mainActivityInterface.getCurrentSet();
        song = mainActivityInterface.getSong();
        showToast = mainActivityInterface.getShowToast();
    }

    void setUpViews() {
        String titletext = requireActivity().getResources().getString(R.string.set) + ": " + setActions.currentSetNameForMenu(getContext(),mainActivityInterface);
        myView.setTitle.setText(titletext);
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(RecyclerView.VERTICAL);
        myView.myRecyclerView.setLayoutManager(llm);
    }

    public void shuffleSet() {
        setActions.shuffleSet(getContext(),mainActivityInterface);
    }

    private void prepareSetListViews() {
        setActions.prepareSetList(getContext(),mainActivityInterface);
        SetListAdapter ma = new SetListAdapter(mainActivityInterface,createList());
        myView.myRecyclerView.setAdapter(ma);
        ItemTouchHelper.Callback callback = new SetListItemTouchHelper(ma,mainActivityInterface);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(myView.myRecyclerView);
    }

    public void prepareCurrentSet() {}

    // Get the set list item objects for the recyclerview
    private List<SetItemInfo> createList() {
        List<SetItemInfo> result = new ArrayList<>();
        setActions.checkArraysMatch(getContext(),mainActivityInterface);

        for (int i=0; i<currentSet.getCurrentSet().size(); i++) {
            SetItemInfo si = new SetItemInfo();
            si.songitem = i + ".";
            si.songfolder = currentSet.getCurrentSet_Folder().get(i);
            si.songtitle = currentSet.getCurrentSet_Filename().get(i);
            si.songkey = currentSet.getCurrentSet_Key().get(i);

            // Decide on the icon to use for the set item
            if (si.songfolder.equals("**" + getString(R.string.slide))) {
                si.songicon = getString(R.string.slide);
            } else if (si.songfolder.equals("**" + getString(R.string.note))) {
                si.songicon = getString(R.string.note);
            } else if (si.songfolder.equals("**" + getString(R.string.scripture))) {
                si.songicon = getString(R.string.scripture);
            } else if (si.songfolder.equals("**" + getString(R.string.image))) {
                si.songicon = getString(R.string.image);
            } else if (si.songfolder.equals("**" + getString(R.string.variation))) {
                si.songicon = getString(R.string.variation);
            } else if (si.songtitle.contains(".pdf") || si.songtitle.contains(".PDF")) {
                si.songicon = ".pdf";
            } else {
                si.songicon = getString(R.string.song);
            }
            result.add(si);
        }
        return result;
    }

}
