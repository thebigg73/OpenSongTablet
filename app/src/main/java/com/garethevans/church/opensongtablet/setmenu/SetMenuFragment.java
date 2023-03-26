package com.garethevans.church.opensongtablet.setmenu;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.MenuSetsBinding;
import com.garethevans.church.opensongtablet.filemanagement.AreYouSureBottomSheet;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SetMenuFragment extends Fragment {

    private MenuSetsBinding myView;
    private LinearLayoutManager llm;
    private SetListAdapter setListAdapter;
    private ArrayList<SetItemInfo> setItemInfos;
    private String deeplink_sets_manage_string="", save_changes_string="", overwrite_string="",
            set_manage_click_string="", set_help_string="", set_manage_swipe_string="",
            slide_string="", note_string="", scripture_string="", image_string="",
            variation_string="", pdf_string="";
    private MainActivityInterface mainActivityInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        mainActivityInterface = (MainActivityInterface) context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = MenuSetsBinding.inflate(inflater, container, false);

        prepareStrings();

        mainActivityInterface.registerFragment(this,"SetMenuFragment");

        // Load up current set details by initialising them in the CurrentSet class
        // They can be called up or saved in that class
        mainActivityInterface.getCurrentSet().loadCurrentSet();
        mainActivityInterface.getCurrentSet().loadSetCurrentBeforeEdits();
        mainActivityInterface.getCurrentSet().loadSetCurrentBeforeEdits();

        // Initialise the set title by passing reference to the views
        mainActivityInterface.getCurrentSet().initialiseSetTitleViews(myView.setTitle.getImageView(),myView.setTitle, myView.saveSetButton);

        myView.myRecyclerView.setVisibility(View.GONE);
        myView.progressBar.setVisibility(View.VISIBLE);
        if (getContext()!=null) {
            myView.setTitle.setImageView(ContextCompat.getDrawable(getContext(), R.drawable.asterisk), Color.WHITE);
            myView.setTitle.getImageView().setVisibility(View.GONE);
        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            mainActivityInterface.getSetActions().buildSetArraysFromItems();
            handler.post(() -> {
                setupAdapter();
                buildList();
                setListeners();
                mainActivityInterface.getCurrentSet().updateSetTitleView();
                scrollToItem();
            });
        });

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            deeplink_sets_manage_string = getString(R.string.deeplink_sets_manage);
            save_changes_string = getString(R.string.save_changes);
            overwrite_string = getString(R.string.overwrite);
            set_manage_click_string = getString(R.string.set_manage_click);
            set_help_string = getString(R.string.set_help);
            set_manage_swipe_string = getString(R.string.set_manage_swipe);
            slide_string = getString(R.string.slide);
            note_string = getString(R.string.note);
            scripture_string = getString(R.string.scripture);
            image_string = getString(R.string.image);
            variation_string = getString(R.string.variation);
            pdf_string = getString(R.string.pdf);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        runSetShowcase();
    }

    private void setupAdapter() {
        if (getContext()!=null) {
            setListAdapter = new SetListAdapter(getContext());
            ItemTouchHelper.Callback callback = new SetListItemTouchHelper(setListAdapter);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
            setListAdapter.setTouchHelper(itemTouchHelper);
            llm = new LinearLayoutManager(getContext());
            llm.setOrientation(RecyclerView.VERTICAL);
            myView.myRecyclerView.post(() -> {
                myView.myRecyclerView.setLayoutManager(llm);
                myView.myRecyclerView.setAdapter(setListAdapter);
                itemTouchHelper.attachToRecyclerView(myView.myRecyclerView);
            });
        }
    }

    private void setListeners() {
        myView.saveSetButton.post(() -> myView.saveSetButton.setOnClickListener(v -> {
            String currentSetName = mainActivityInterface.getCurrentSet().getSetCurrentLastName();
            if (currentSetName==null || currentSetName.isEmpty()) {
                // We need the user to give the set a name
                mainActivityInterface.setWhattodo("saveset");
                mainActivityInterface.navigateToFragment(deeplink_sets_manage_string,0);
            } else {
                // Prompt the user to confirm overwriting the original
                String message = save_changes_string + ": " + currentSetName + "\n\n" + overwrite_string;
                AreYouSureBottomSheet areYouSureBottomSheet = new AreYouSureBottomSheet("saveset",message,null,"SetMenuFragment",SetMenuFragment.this,null);
                areYouSureBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"areYouSure");
            }
        }));
        myView.setMasterFAB.post(() -> myView.setMasterFAB.setOnClickListener(v -> {
            if (getActivity()!=null) {
                SetMenuBottomSheet setMenuBottomSheet = new SetMenuBottomSheet();
                setMenuBottomSheet.show(getActivity().getSupportFragmentManager(), "setMenuBottomSheet");
            }
        }));
        myView.myRecyclerView.post(() -> myView.myRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    myView.setMasterFAB.show();
                } else {
                    myView.setMasterFAB.hide();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        }));

    }

    public void scrollToItem() {
        if (mainActivityInterface.getCurrentSet().getIndexSongInSet()>-1 &&
                mainActivityInterface.getCurrentSet().getIndexSongInSet() < mainActivityInterface.getCurrentSet().getSetItems().size()) {
            myView.myRecyclerView.post(() -> llm.scrollToPositionWithOffset(mainActivityInterface.getCurrentSet().getIndexSongInSet() , 0));
        }
    }

    public void updateSet() {
        prepareCurrentSet();
    }

    public void updateKeys() {
        // If the key has changed on some items, update them
        if (mainActivityInterface.getSetActions().getMissingKeyPositions()!=null &&
                setListAdapter!=null && setListAdapter.getSetList()!=null) {
            for (int position:mainActivityInterface.getSetActions().getMissingKeyPositions()) {
                try {
                    setListAdapter.getSetList().get(position).songkey = mainActivityInterface.getCurrentSet().getKey(position);
                    setListAdapter.notifyItemChanged(position);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mainActivityInterface.getSetActions().nullMissingKeyPositions();
        }
    }

    public void prepareCurrentSet() {
        // We have received a call to redraw the set list either on first load or after song indexing
        myView.myRecyclerView.post(() -> {
            // Clear the original setlist by passing in a new blank arraylist
            // This also deals with notifying changes
            setListAdapter.updateSetList(new ArrayList<>());
            buildList();
            mainActivityInterface.getCurrentSet().updateSetTitleView();
        });
    }

    private void buildList() {
        try {
            setItemInfos = new ArrayList<>();
            for (int i = 0; i < mainActivityInterface.getCurrentSet().getSetItems().size(); i++) {
                setItemInfos.add(makeSetItem(i));
            }
            myView.myRecyclerView.post(() -> {
                try {
                    if (setListAdapter!=null) {
                        setListAdapter.updateSetList(setItemInfos);
                        myView.myRecyclerView.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            myView.progressBar.post(() -> {
                try {
                    myView.progressBar.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateItem(int position) {
        if (position>=0) {
            try {
                String folder = mainActivityInterface.getCurrentSet().getFolder(position);
                String filename = mainActivityInterface.getCurrentSet().getFilename(position);
                String key = mainActivityInterface.getCurrentSet().getKey(position);
                setListAdapter.getSetList().get(position).songfolder = folder.replace("**", "../");
                setListAdapter.getSetList().get(position).songfoldernice = folder;
                setListAdapter.getSetList().get(position).songfilename = filename;
                setListAdapter.getSetList().get(position).songtitle = filename;
                setListAdapter.getSetList().get(position).songkey = key;

                // Check for icon
                setListAdapter.getSetList().get(position).songicon = mainActivityInterface.getSetActions().
                        getIconIdentifier(folder, filename);

                mainActivityInterface.getCurrentSet().updateSetTitleView();
                setListAdapter.updateHighlightedItem(position);
                setListAdapter.notifyItemChanged(position);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addSetItem(int currentSetPosition) {
        setListAdapter.itemAdded(makeSetItem(currentSetPosition));
        mainActivityInterface.getCurrentSet().updateSetTitleView();
    }

    public void runSetShowcase() {
        if (getActivity()!=null) {
            try {
                String info = set_manage_click_string + "\n" + set_help_string +
                        "\n" + set_manage_swipe_string;
                myView.myRecyclerView.post(() -> mainActivityInterface.getShowCase().singleShowCase(getActivity(),
                        myView.setTitle, null, info, true, "setFragment"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Called from clicking on checkboxes in song menu (via MainActivity)
    public void removeSetItem(int currentSetPosition) {
        setListAdapter.itemRemoved(currentSetPosition);
        mainActivityInterface.getCurrentSet().updateSetTitleView();
    }
    private SetItemInfo makeSetItem(int i) {
        SetItemInfo si = new SetItemInfo();
        si.songitem = (i+1) + ".";
        if (i<mainActivityInterface.getCurrentSet().getSetFolders().size()) {
            si.songfolder = mainActivityInterface.getCurrentSet().getSetFolders().get(i);
            si.songfoldernice = mainActivityInterface.getCurrentSet().getSetFolders().get(i);
        }
        if (i<mainActivityInterface.getCurrentSet().getSetFilenames().size()) {
            si.songtitle = Uri.decode(mainActivityInterface.getCurrentSet().getSetFilenames().get(i));
            si.songfilename = mainActivityInterface.getCurrentSet().getSetFilenames().get(i);
        }
        if (i<mainActivityInterface.getCurrentSet().getSetKeys().size()) {
            si.songkey = mainActivityInterface.getCurrentSet().getSetKeys().get(i);
        }

        // Decide on the icon to use for the set item
        if (si.songfolder!=null) {
            if (si.songfolder.equals("**Slides")) {
                si.songicon = "Slides";
                si.songfoldernice = slide_string;
            } else if (si.songfolder.equals("**Notes")) {
                si.songicon = "Notes";
                si.songfoldernice = note_string;
            } else if (si.songfolder.equals("**Scripture")) {
                si.songicon = "Scripture";
                si.songfoldernice = scripture_string;
            } else if (si.songfolder.equals("**Images")) {
                si.songicon = "Images";
                si.songfoldernice = image_string;
            } else if (si.songfolder.equals("**Variations")) {
                si.songicon = "Variations";
                si.songfoldernice = variation_string;
            } else if (si.songtitle.toLowerCase(Locale.ROOT).contains(".pdf")) {
                si.songicon = ".pdf";
                si.songfoldernice = pdf_string;
            } else {
                si.songicon = "Songs";
            }
        } else {
            si.songicon = "Songs";
        }
        return si;
    }

    public void scrollMenu(int height) {
        try {
            myView.myRecyclerView.smoothScrollBy(0, height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        getFocus();
    }
    public void getFocus() {
        try {
            myView.myRecyclerView.requestFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initialiseSetItem(int setPosition) {
        // Only do this if we actually needed to highlight an item
        if (setListAdapter!=null && setListAdapter.initialiseSetItem(setPosition)) {
            myView.myRecyclerView.post(() -> llm.scrollToPositionWithOffset(mainActivityInterface.getCurrentSet().getIndexSongInSet() , 0));
        }
    }

}
