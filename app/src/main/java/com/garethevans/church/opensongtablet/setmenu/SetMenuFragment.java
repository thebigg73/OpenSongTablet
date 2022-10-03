package com.garethevans.church.opensongtablet.setmenu;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.FastScroller;
import com.garethevans.church.opensongtablet.databinding.MenuSetsBinding;
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

        myView.myRecyclerView.setVisibility(View.GONE);
        myView.progressBar.setVisibility(View.VISIBLE);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            mainActivityInterface.getSetActions().buildSetArraysFromItems();
            handler.post(() -> {
                setupAdapter();
                buildList();
                setListeners();
                updateSetTitle();
                scrollToItem();
            });
        });

        return myView.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        runSetShowcase();
    }

    private void setupAdapter() {
        setListAdapter = new SetListAdapter(requireContext());
        ItemTouchHelper.Callback callback = new SetListItemTouchHelper(setListAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        setListAdapter.setTouchHelper(itemTouchHelper);
        llm = new LinearLayoutManager(requireContext());
        llm.setOrientation(RecyclerView.VERTICAL);
        myView.myRecyclerView.post(() -> {
            myView.myRecyclerView.setLayoutManager(llm);
            myView.myRecyclerView.setAdapter(setListAdapter);
            itemTouchHelper.attachToRecyclerView(myView.myRecyclerView);
        });
    }

    private void setListeners() {
        myView.setMasterFAB.post(() -> myView.setMasterFAB.setOnClickListener(v -> {
            SetMenuBottomSheet setMenuBottomSheet = new SetMenuBottomSheet();
            setMenuBottomSheet.show(requireActivity().getSupportFragmentManager(), "setMenuBottomSheet");
        }));
        myView.myRecyclerView.post(() -> {
            myView.myRecyclerView.setFastScrollListener(new FastScroller.FastScrollListener() {
                @Override
                public void onFastScrollStart(@NonNull FastScroller fastScroller) {
                    myView.setMasterFAB.hide();
                }

                @Override
                public void onFastScrollStop(@NonNull FastScroller fastScroller) {
                    myView.setMasterFAB.show();
                }
            });
            myView.myRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        myView.setMasterFAB.show();
                    } else {
                        myView.setMasterFAB.hide();
                    }
                    super.onScrollStateChanged(recyclerView, newState);
                }
            });
        });

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
                setListAdapter!=null) {
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
            updateSetTitle();
        });
    }

    private void buildList() {
        try {
            setItemInfos = new ArrayList<>();
            for (int i = 0; i < mainActivityInterface.getCurrentSet().getSetItems().size(); i++) {
                setItemInfos.add(makeSetItem(i));
            }
            myView.myRecyclerView.post(() -> {
                setListAdapter.updateSetList(setItemInfos);
                myView.myRecyclerView.setVisibility(View.VISIBLE);
            });
            myView.progressBar.post(() -> myView.progressBar.setVisibility(View.GONE));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateItem(int position) {
        String folder = mainActivityInterface.getCurrentSet().getFolder(position);
        String filename = mainActivityInterface.getCurrentSet().getFilename(position);
        String key = mainActivityInterface.getCurrentSet().getKey(position);
        setListAdapter.getSetList().get(position).songfolder = folder.replace("**","../");
        setListAdapter.getSetList().get(position).songfoldernice = folder;
        setListAdapter.getSetList().get(position).songfilename = filename;
        setListAdapter.getSetList().get(position).songtitle = filename;
        setListAdapter.getSetList().get(position).songkey = key;

        // Check for icon
        setListAdapter.getSetList().get(position).songicon = mainActivityInterface.getSetActions().
                getIconIdentifier(folder,filename);

        updateSetTitle();
        setListAdapter.updateHighlightedItem(position);
        setListAdapter.notifyItemChanged(position);
    }

    public void updateSetTitle() {
        // Save the changes
        String titletext = requireContext().getResources().getString(R.string.set) + ": " + mainActivityInterface.getSetActions().currentSetNameForMenu();
        myView.setTitle.post(() -> myView.setTitle.setText(titletext));
    }

    public void addSetItem(int currentSetPosition) {
        setListAdapter.getSetList().add(makeSetItem(currentSetPosition));
        setListAdapter.notifyItemInserted(currentSetPosition);
        updateSetTitle();
    }

    public void runSetShowcase() {
        try {
            String info = getString(R.string.set_manage_click) + "\n" + getString(R.string.set_help) +
                    "\n" + getString(R.string.set_manage_swipe);
            myView.myRecyclerView.post(() -> mainActivityInterface.getShowCase().singleShowCase(requireActivity(),
                    myView.setTitle, null, info, true, "setFragment"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Called from clicking on checkboxes in song menu (via MainActivity)
    public void removeSetItem(int currentSetPosition) {
        setListAdapter.getSetList().remove(currentSetPosition);
        setListAdapter.notifyItemRemoved(currentSetPosition);
        updateSetTitle();
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
                si.songfoldernice = getString(R.string.slide);
            } else if (si.songfolder.equals("**Notes")) {
                si.songicon = "Notes";
                si.songfoldernice = getString(R.string.note);
            } else if (si.songfolder.equals("**Scripture")) {
                si.songicon = "Scripture";
                si.songfoldernice = getString(R.string.scripture);
            } else if (si.songfolder.equals("**Images")) {
                si.songicon = "Images";
                si.songfoldernice = getString(R.string.image);
            } else if (si.songfolder.equals("**Variations")) {
                si.songicon = "Variations";
                si.songfoldernice = getString(R.string.variation);
            } else if (si.songtitle.toLowerCase(Locale.ROOT).contains(".pdf")) {
                si.songicon = ".pdf";
                si.songfoldernice = getString(R.string.pdf);
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
