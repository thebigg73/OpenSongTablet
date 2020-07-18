package com.garethevans.church.opensongtablet.songsandsets;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetMenuFragment extends Fragment {

    MenuSetsBinding menuSetsBinding;
    StorageAccess storageAccess;
    Preferences preferences;
    SetActions setActions;
    LoadSong loadSong;
    ShowToast showToast;
    ProcessSong processSong;

    static ArrayList<String> mSongName, mFolderName;
    LinearLayoutManager llm;

    private static MainActivityInterface mainActivityInterface;

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
        menuSetsBinding = MenuSetsBinding.inflate(inflater, container, false);

        storageAccess = new StorageAccess();
        preferences = new Preferences();
        setActions = new SetActions();
        loadSong = new LoadSong();
        processSong = new ProcessSong();

        new Thread(() -> {
            requireActivity().runOnUiThread(() -> {
                menuSetsBinding.progressBar.setVisibility(View.VISIBLE);
                menuSetsBinding.myRecyclerView.setVisibility(View.GONE);
                setUpViews();
            });

            prepareCurrentSet();

            requireActivity().runOnUiThread(this::prepareSetListViews);
            // Try to move to the corresponding item in the set that we are viewing.
            setActions.indexSongInSet();

            requireActivity().runOnUiThread(() -> {
                // If the song is found (indexSongInSet>-1 and lower than the number of items shown), smooth scroll to it
                if (StaticVariables.indexSongInSet>-1 && StaticVariables.indexSongInSet< StaticVariables.mTempSetList.size()) {
                    llm.scrollToPositionWithOffset(StaticVariables.indexSongInSet , 0);
                }
                menuSetsBinding.myRecyclerView.setVisibility(View.VISIBLE);
                menuSetsBinding.progressBar.setVisibility(View.GONE);
            });
        }).start();

        return menuSetsBinding.getRoot();
    }

    void setUpViews() {
        String titletext = requireActivity().getResources().getString(R.string.set) + getSetName();
        menuSetsBinding.setTitle.setText(titletext);
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(RecyclerView.VERTICAL);
        menuSetsBinding.myRecyclerView.setLayoutManager(llm);
    }

    String getSetName() {
        // This decides on the set name to display as a title
        // If it is a new set (unsaved), it will be called 'current (unsaved)'
        // If it is a non-modified loaded set, it will be called 'set name'
        // If it is a modified, unsaved, loaded set, it will be called 'set name (unsaved)'

        String title;
        String lastSetName = preferences.getMyPreferenceString(getActivity(),"setCurrentLastName","");
        if (lastSetName==null || lastSetName.equals("")) {
            title = ": " + requireActivity().getString(R.string.currentset) +
                    " (" + getActivity().getString(R.string.notsaved) + ")";
        } else {
            String name = lastSetName.replace("__","/");
            title = ": " + name;
            if (!preferences.getMyPreferenceString(getActivity(),"setCurrent","")
                    .equals(preferences.getMyPreferenceString(getActivity(),"setCurrentBeforeEdits",""))) {
                title += " (" + requireActivity().getString(R.string.notsaved) + ")";
            }
        }
        return title;
    }

    public void prepareCurrentSet() {
        // Grab the saved set list array and put it into a list
        // This way we work with a temporary version
        if (StaticVariables.mSetList==null) {
            setActions.prepareSetList(requireActivity(),preferences);
        }

        if (StaticVariables.doneshuffle && StaticVariables.mTempSetList != null && StaticVariables.mTempSetList.size() > 0) {
            Log.d("d", "We've shuffled the set list");
        } else {
            StaticVariables.mTempSetList = new ArrayList<>();
            StaticVariables.mTempSetList.addAll(Arrays.asList(StaticVariables.mSetList));
        }

        extractSongsAndFolders();
        StaticVariables.doneshuffle = false;

    }

    private void prepareSetListViews() {
        SetListAdapter ma = new SetListAdapter(createList(StaticVariables.mTempSetList.size()),
                getActivity(), preferences, showToast);
        menuSetsBinding.myRecyclerView.setAdapter(ma);
        ItemTouchHelper.Callback callback = new SetListItemTouchHelper(ma);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(menuSetsBinding.myRecyclerView);
        /*menuSetsBinding.myRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if ((dy != 0) && menuSetsBinding.setMasterFAB.getVisibility() == View.VISIBLE) {
                    menuSetsBinding.setMasterFAB.hide();
                } else if (dy < 0 && menuSetsBinding.setMasterFAB.getVisibility() != View.VISIBLE) {
                    menuSetsBinding.setMasterFAB.show();
                }
            }
        });*/
    }

    private void extractSongsAndFolders() {
        // Populate the set list list view
        // Split the set items into song and folder
        mSongName = new ArrayList<>();
        mFolderName = new ArrayList<>();

        if (StaticVariables.mTempSetList==null) {
            Log.d("PopUpSetView","mTempSetList is null");
        }
        String tempTitle;
        if (StaticVariables.mTempSetList != null && StaticVariables.mTempSetList.size() > 0) {
            for (int i = 0; i < StaticVariables.mTempSetList.size(); i++) {
                if (!StaticVariables.mTempSetList.get(i).contains("/")) {
                    tempTitle = "/" + StaticVariables.mTempSetList.get(i);
                } else {
                    tempTitle = StaticVariables.mTempSetList.get(i);
                }
                // Replace the last instance of a / (as we may have subfolders)
                String mysongfolder = tempTitle.substring(0, tempTitle.lastIndexOf("/"));
                String mysongtitle = tempTitle.substring(tempTitle.lastIndexOf("/"));
                if (mysongtitle.startsWith("/")) {
                    mysongtitle = mysongtitle.substring(1);
                }

                if (mysongfolder.isEmpty()) {
                    mysongfolder = getResources().getString(R.string.mainfoldername);
                }

                if (mysongtitle.isEmpty() || mysongfolder.equals("")) {
                    mysongtitle = "!ERROR!";
                }
                mSongName.add(i, mysongtitle);
                mFolderName.add(i, mysongfolder);
            }
        }
    }

    private List<SetItemInfo> createList(int size) {
        List<SetItemInfo> result = new ArrayList<>();
        for (int i=1; i <= size; i++) {
            if (!mSongName.get(i - 1).equals("!ERROR!")) {
                SetItemInfo si = new SetItemInfo();
                si.songitem = i+".";
                si.songtitle = mSongName.get(i - 1);
                si.songfolder = mFolderName.get(i - 1);
                String songLocation = loadSong.getTempFileLocation(requireActivity(),mFolderName.get(i-1),mSongName.get(i-1));
                si.songkey = loadSong.grabNextSongInSetKey(getActivity(), preferences, storageAccess, processSong, songLocation);
                // Decide what image we'll need - song, image, note, slide, scripture, variation
                if (mFolderName.get(i - 1).equals("**"+ requireActivity().getResources().getString(R.string.slide))) {
                    si.songicon = getActivity().getResources().getString(R.string.slide);
                } else if (mFolderName.get(i - 1).equals("**"+getActivity().getResources().getString(R.string.note))) {
                    si.songicon = getActivity().getResources().getString(R.string.note);
                } else if (mFolderName.get(i - 1).equals("**"+getActivity().getResources().getString(R.string.scripture))) {
                    si.songicon = getActivity().getResources().getString(R.string.scripture);
                } else if (mFolderName.get(i - 1).equals("**"+getActivity().getResources().getString(R.string.image))) {
                    si.songicon = getActivity().getResources().getString(R.string.image);
                } else if (mFolderName.get(i - 1).equals("**"+getActivity().getResources().getString(R.string.variation))) {
                    si.songicon = getActivity().getResources().getString(R.string.variation);
                } else if (mSongName.get(i - 1).contains(".pdf") || mSongName.get(i - 1).contains(".PDF")) {
                    si.songicon = ".pdf";
                } else {
                    si.songicon = getActivity().getResources().getString(R.string.song);
                }
                result.add(si);
            }
        }
        return result;
    }

    public static void makeVariation(Context c, Preferences preferences, ShowToast showToast) {
        // Prepare the name of the new variation slide
        // If the file already exists, add _ to the filename
        StringBuilder newsongname = new StringBuilder(StaticVariables.songfilename);
        StorageAccess storageAccess = new StorageAccess();
        Uri uriVariation = storageAccess.getUriForItem(c, preferences, "Variations", "",
                StaticVariables.songfilename);

        // Original file
        Uri uriOriginal = storageAccess.getUriForItem(c, preferences, "Songs", StaticVariables.whichSongFolder,
                StaticVariables.songfilename);

        // Copy the file into the variations folder
        InputStream inputStream = storageAccess.getInputStream(c, uriOriginal);

        // Check the uri exists for the outputstream to be valid
        storageAccess.lollipopCreateFileForOutputStream(c, preferences, uriVariation, null,
                "Variations", "", StaticVariables.songfilename);

        OutputStream outputStream = storageAccess.getOutputStream(c, uriVariation);
        storageAccess.copyFile(inputStream, outputStream);

        // Fix the song name and folder for loading
        StaticVariables.songfilename = newsongname.toString();
        StaticVariables.whichSongFolder = "../Variations";
        StaticVariables.whatsongforsetwork = "\"$**_**" + c.getResources().getString(R.string.variation) + "/" + newsongname + "_**$";

        // Replace the set item with the variation
        StaticVariables.mSetList[StaticVariables.indexSongInSet] = "**" + c.getResources().getString(R.string.variation) + "/" + newsongname;
        StaticVariables.mTempSetList.set(StaticVariables.indexSongInSet,"**" + c.getResources().getString(R.string.variation) + "/" + newsongname);
        // Rebuild the mySet variable
        StringBuilder new_mySet = new StringBuilder();
        for (String thisitem : StaticVariables.mSetList) {
            new_mySet.append("$**_").append(thisitem).append("_**$");
        }
        preferences.setMyPreferenceString(c,"setCurrent",new_mySet.toString());

        showToast.doIt(c, c.getResources().getString(R.string.variation_edit));
        // Now load the new variation item up
        loadSong(c,preferences);

    }

    public static void loadSong(Context c, Preferences preferences) {
        StaticVariables.setView = true;
        if (StaticVariables.setchanged && mainActivityInterface != null) {
            // We've edited the set and then clicked on a song, so save the set first
            StaticVariables.whattodo = "saveset";
            StringBuilder tempmySet = new StringBuilder();
            String tempItem;
            if (StaticVariables.mTempSetList == null) {
                StaticVariables.mTempSetList = new ArrayList<>();
            }
            for (int z = 0; z < StaticVariables.mTempSetList.size(); z++) {
                tempItem = StaticVariables.mTempSetList.get(z);
                tempmySet.append("$**_").append(tempItem).append("_**$");
            }
            preferences.setMyPreferenceString(c,"setCurrent",tempmySet.toString());
        }
        if (mainActivityInterface != null) {
            mainActivityInterface.loadSongFromSet();
        }
    }

    private void doSave() {
        StringBuilder tempmySet = new StringBuilder();
        String tempItem;
        if (StaticVariables.mTempSetList == null) {
            StaticVariables.mTempSetList = new ArrayList<>();
        }
        for (int z = 0; z < StaticVariables.mTempSetList.size(); z++) {
            tempItem = StaticVariables.mTempSetList.get(z);
            tempmySet.append("$**_").append(tempItem).append("_**$");
        }
        preferences.setMyPreferenceString(getActivity(),"setCurrent",tempmySet.toString());
        StaticVariables.mTempSetList = null;
        setActions.prepareSetList(getActivity(),preferences);
        showToast.doIt(getActivity(),requireActivity().getString(R.string.currentset) + " - " + getActivity().getString(R.string.ok));
    }

    private void refresh() {
        if (mainActivityInterface != null) {
            mainActivityInterface.refreshAll();
        }
    }

}
