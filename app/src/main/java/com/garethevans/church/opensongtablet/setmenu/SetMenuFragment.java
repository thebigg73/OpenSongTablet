package com.garethevans.church.opensongtablet.setmenu;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
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
import com.google.android.material.snackbar.Snackbar;

public class SetMenuFragment extends Fragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "SetMenuFragment";
    private MenuSetsBinding myView;
    private LinearLayoutManager llm;
    private String deeplink_sets_manage_string="", save_changes_string="", overwrite_string="",
            set_manage_click_string="", set_help_string="", set_manage_swipe_string="",
            set_item_removed_string="", undo_string="";
    private MainActivityInterface mainActivityInterface;
    private SetAdapter setAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        mainActivityInterface = (MainActivityInterface) context;
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareStrings();
        setListeners();
        runSetShowcase();
    }


    // Don't process the set yet, just get the fragment ready
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = MenuSetsBinding.inflate(inflater, container, false);

        if (getContext()!=null) {
            mainActivityInterface = (MainActivityInterface) getContext();
            mainActivityInterface.registerFragment(this,"SetMenuFragment");
        }

        // Prepare the strings
        prepareStrings();

        // Set up the adapter
        setupAdapter();

        // Set up listeners
        setListeners();

        // Initialise the set title by passing reference to the views
        mainActivityInterface.getCurrentSet().initialiseSetTitleViews(myView.setTitle.getImageView(),myView.setTitle, myView.saveSetButton);

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
            set_item_removed_string = getString(R.string.set_item_removed);
            undo_string = getString(R.string.undo);
        }
    }

    private void setupAdapter() {
        if (getContext()!=null && myView!=null) {
            setAdapter = new SetAdapter(getContext(), myView.myRecyclerView);
            myView.myRecyclerView.post(() -> {
                llm = new LinearLayoutManager(getContext());
                llm.setOrientation(RecyclerView.VERTICAL);
                myView.myRecyclerView.setLayoutManager(llm);
                enableSwipeToDeleteAndUndo();
                myView.myRecyclerView.setAdapter(setAdapter);
                myView.myRecyclerView.setItemAnimator(null);
            });
            mainActivityInterface.getCurrentSet().updateSetTitleView();
        }
    }

    private void enableSwipeToDeleteAndUndo() {
        SetListItemCallback setListItemCallback = new SetListItemCallback(getContext(),setAdapter) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                final int position = viewHolder.getAbsoluteAdapterPosition();
                final SetItemInfo item = mainActivityInterface.getCurrentSet().getSetItemInfo(position);

                setAdapter.removeItem(position, true);

                Snackbar snackbar = Snackbar.make(myView.coordinatorLayout, set_item_removed_string, Snackbar.LENGTH_LONG);
                snackbar.setAction(undo_string, view -> {
                    setAdapter.restoreItem(item, position);
                    myView.myRecyclerView.scrollToPosition(position);
                });

                if (getContext()!=null) {
                    try {
                        snackbar.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorSecondary)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    snackbar.setActionTextColor(Color.WHITE);
                    snackbar.setTextColor(Color.WHITE);
                }
                snackbar.show();
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(setListItemCallback);
        setAdapter.setTouchHelper(itemTouchhelper);
        itemTouchhelper.attachToRecyclerView(myView.myRecyclerView);
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
                AreYouSureBottomSheet areYouSureBottomSheet = new AreYouSureBottomSheet("saveset",
                        message,null,"SetMenuFragment",
                        SetMenuFragment.this,null);
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

    // Show the set showcase
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


    public void changeVisibility(boolean visible) {
        if (myView!=null) {
            myView.myRecyclerView.post(() -> myView.myRecyclerView.setVisibility(visible ? View.VISIBLE:View.INVISIBLE));
            myView.progressBar.post(() -> myView.progressBar.setVisibility(visible ? View.GONE:View.VISIBLE));
        }
    }

    // Scroll to the item in the set list menu
    public void scrollToItem() {
        if (mainActivityInterface!=null && mainActivityInterface.getCurrentSet().getIndexSongInSet()>-1 &&
                mainActivityInterface.getCurrentSet().getIndexSongInSet() < mainActivityInterface.getCurrentSet().getCurrentSetSize()) {
            myView.myRecyclerView.postDelayed(() -> {
                int position = mainActivityInterface.getCurrentSet().getIndexSongInSet();
                if (position<0 && mainActivityInterface.getCurrentSet().getCurrentSetSize()>0) {
                    position = 0;
                }
                if (llm!=null && position>=0) {
                    try {
                        llm.scrollToPositionWithOffset(position, 0);
                        mainActivityInterface.notifyInlineSetScrollToItem();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            },100);
        }
    }

    // Scroll the menu using a pedal
    public void scrollMenu(int height) {
        try {
            myView.myRecyclerView.smoothScrollBy(0, height);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Called after songs have been indexed for items that have key set
    // We need to check the original key for the song
    public void updateKeys() {
        // If the key has changed on some items, update them
        if (mainActivityInterface.getSetActions().getMissingKeyPositions()!=null &&
                setAdapter!=null &&
                mainActivityInterface.getCurrentSet().getSetItemInfos()!=null &&
                mainActivityInterface.getCurrentSet().getCurrentSetSize()>0) {
            setAdapter.updateKeys();
        }
    }

    // Called when clicking on clear set/create new set or sorting/shuffling
    public void notifyItemRangeRemoved(int from, int count) {
        if (setAdapter!=null && mainActivityInterface!=null) {
            mainActivityInterface.getMainHandler().post(() -> setAdapter.notifyItemRangeRemoved(from, count));
        }
    }

    // Called when sorting or shuffling the set
    public void notifyItemRangeChanged(int from, int count) {
        if (setAdapter!=null && mainActivityInterface!=null) {
            mainActivityInterface.getMainHandler().post(() -> setAdapter.notifyItemRangeChanged(from, count));
        }
    }
    // Called when editing a set item
    public void notifyItemChanged(int position) {
        if (setAdapter!=null && mainActivityInterface!=null) {
            mainActivityInterface.getMainHandler().post(() -> {
                setAdapter.notifyItemChanged(position);
                mainActivityInterface.notifyInlineSetChanged(position);
            });

        }
    }

    // Called when adding a song to the end of the set
    public void notifyItemInserted() {
        // The item was added to the set already by the calling method (song menu or action)
        if (setAdapter!=null && mainActivityInterface.getCurrentSet().getCurrentSetSize()>0) {
            mainActivityInterface.getMainHandler().post(() -> setAdapter.insertItem());
        }
    }
    public void notifyItemRemoved(int position) {
        if (setAdapter!=null && mainActivityInterface.getCurrentSet().getCurrentSetSize()>position) {
            mainActivityInterface.getMainHandler().post(() -> setAdapter.removeItem(position,false));
        }
    }

    // Called when we edit a set item from the bottom sheet
    public void updateItem(int position) {
        if (setAdapter!=null) {
            setAdapter.updateItem(position);
            // Save the currentSet
            mainActivityInterface.getCurrentSet().setSetCurrent(mainActivityInterface.getSetActions().getSetAsPreferenceString());
        }
    }

    public void updateAdapterPrefs() {
        if (setAdapter!=null) {
            setAdapter.getUpdatedPreferences();
        }
    }

    public void updateHighlight() {
        // Highlight the current set item after checking the positions
        // Only do this if we aren't in the middle of doing it already
        if (setAdapter!=null && mainActivityInterface.getSong()!=null &&
                !setAdapter.getUpdatingHighlight() && setAdapter.getHighlightChangeAllowed()) {
            int selectedPosition = mainActivityInterface.getCurrentSet().getIndexSongInSet();
            int previousPosition = mainActivityInterface.getCurrentSet().getPrevIndexSongInSet();

            if (previousPosition>-1) {
                setAdapter.removeHighlight(previousPosition);
            }
            if (selectedPosition>-1) {
                setAdapter.updateHighlight(selectedPosition);
            }
        }
    }

    public void removeHighlight() {
        if (setAdapter!=null && mainActivityInterface.getCurrentSet().getPrevIndexSongInSet()>-1 &&
                !setAdapter.getRemovingHighlight() && setAdapter.getHighlightChangeAllowed()) {
            // Remove the highlight from the last selected item
            setAdapter.removeHighlight(mainActivityInterface.getCurrentSet().getPrevIndexSongInSet());
        }
    }

    public void setHighlightChangeAllowed(boolean highlightChangeAllowed) {
        if (setAdapter!=null) {
            setAdapter.setHighlightChangeAllowed(highlightChangeAllowed);
        }
    }
}
