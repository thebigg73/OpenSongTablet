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
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;
import java.util.List;

public class SetMenuFragment extends Fragment {

    private final String TAG = "SetMenuFragment";

    private MenuSetsBinding myView;
    private LinearLayoutManager llm;
    private SetListAdapter setListAdapter;
    List<SetItemInfo> setItemInfos;

    private MainActivityInterface mainActivityInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        mainActivityInterface = (MainActivityInterface) context;
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = MenuSetsBinding.inflate(inflater, container, false);

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
                if (mainActivityInterface.getCurrentSet().getIndexSongInSet()>-1 &&
                        mainActivityInterface.getCurrentSet().getIndexSongInSet() < mainActivityInterface.getCurrentSet().getSetItems().size()) {
                    llm.scrollToPositionWithOffset(mainActivityInterface.getCurrentSet().getIndexSongInSet() , 0);
                }
                myView.myRecyclerView.setVisibility(View.VISIBLE);
                myView.progressBar.setVisibility(View.GONE);

                setListeners();
            });
        }).start();


        return myView.getRoot();
    }


    void setUpViews() {
        String titletext = requireActivity().getResources().getString(R.string.set) + ": " + mainActivityInterface.getSetActions().currentSetNameForMenu(getContext(),mainActivityInterface);
        myView.setTitle.setText(titletext);
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(RecyclerView.VERTICAL);
        myView.myRecyclerView.setLayoutManager(llm);
    }

    private void setListeners() {
        myView.setMasterFAB.setOnClickListener(v -> {
            SetMenuBottomSheet setMenuBottomSheet = new SetMenuBottomSheet();
            setMenuBottomSheet.show(requireActivity().getSupportFragmentManager(), "setMenuActions");
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
    }

    public void updateSet() {
        mainActivityInterface.getSetActions().shuffleSet(getContext(),mainActivityInterface);
        prepareCurrentSet();
    }

    private void prepareSetListViews() {
        mainActivityInterface.getSetActions().preferenceStringToArrays(getContext(),mainActivityInterface);
        buildList();

    }

    public void updateKeys() {
        // If the key has changed on some items, update them
        if (mainActivityInterface.getSetActions().getMissingKeyPositions()!=null &&
                setListAdapter!=null) {
            for (int position:mainActivityInterface.getSetActions().getMissingKeyPositions()) {
                try {
                    setItemInfos.get(position).songkey = mainActivityInterface.getCurrentSet().getKey(position);
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
        mainActivityInterface.getSetActions().buildSetArraysFromItems(requireContext(),mainActivityInterface);
        myView.myRecyclerView.removeAllViews();
        myView.myRecyclerView.setOnClickListener(null);
        myView.myRecyclerView.invalidate();
        buildList();
    }

    private void buildList() {
        setListAdapter = new SetListAdapter(mainActivityInterface, createList());
        ItemTouchHelper.Callback callback = new SetListItemTouchHelper(mainActivityInterface,setListAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        setListAdapter.setTouchHelper(itemTouchHelper);
        itemTouchHelper.attachToRecyclerView(myView.myRecyclerView);
        myView.myRecyclerView.setAdapter(setListAdapter);
    }

    // Get the set list item objects for the recyclerview
    private List<SetItemInfo> createList() {
        setItemInfos = new ArrayList<>();

        mainActivityInterface.getSetActions().buildSetArraysFromItems(requireContext(), mainActivityInterface);
        for (int i = 0; i<mainActivityInterface.getCurrentSet().getSetItems().size(); i++) {
            SetItemInfo si = new SetItemInfo();
            si.songitem = (i+1) + ".";
            si.songfolder = mainActivityInterface.getCurrentSet().getSetFolders().get(i);
            si.songtitle = mainActivityInterface.getCurrentSet().getSetFilenames().get(i);
            si.songkey = mainActivityInterface.getCurrentSet().getSetKeys().get(i);

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
            setItemInfos.add(si);
        }
        return setItemInfos;
    }

    public void updateItem(int position) {
        String folder = mainActivityInterface.getCurrentSet().getFolder(position);
        String filename = mainActivityInterface.getCurrentSet().getFilename(position);
        setItemInfos.get(position).songfolder = folder;
        // Check for icon
        setItemInfos.get(position).songicon = mainActivityInterface.getSetActions().
                getIconIdentifier(requireContext(), mainActivityInterface,folder,filename);
        setListAdapter.notifyItemChanged(position);
    }

}
