package com.garethevans.church.opensongtablet.setmenu;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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


// TODO - set title not updating


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
    boolean firstLoad = true;

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

                setAdapter.removeItem(position);

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
        Log.d(TAG,"scrollToItem()");
        if (mainActivityInterface.getCurrentSet().getIndexSongInSet()>-1 &&
                mainActivityInterface.getCurrentSet().getIndexSongInSet() < mainActivityInterface.getCurrentSet().getCurrentSetSize()) {
            myView.myRecyclerView.postDelayed(() -> {
                if (llm!=null) {
                    llm.scrollToPositionWithOffset(mainActivityInterface.getCurrentSet().getIndexSongInSet(), 0);
                }
            },800);
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

    // Clear the set
    public void clearSet() {
        if (setAdapter!=null) {
            mainActivityInterface.getMainHandler().post(() -> setAdapter.notifyItemRangeRemoved(0,mainActivityInterface.getCurrentSet().getCurrentSetSize()));
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

    public void notifyToClearSet() {
        Log.d(TAG,"notifyToClearSet()");
        // Called when a set is cleared
        if (myView!=null && setAdapter!=null && mainActivityInterface.getCurrentSet().getCurrentSetSize()>0) {
            if (mainActivityInterface.getCurrentSet().getCurrentSetSize()>0) {
                // Just in case it is on the wrong thread!
                // Calling it post stops it working for some reason?
                try {
                    mainActivityInterface.getMainHandler().post(() -> setAdapter.notifyItemRangeRemoved(0, mainActivityInterface.getCurrentSet().getCurrentSetSize()));
                    //setAdapter.notifyItemRangeRemoved(0, mainActivityInterface.getCurrentSet().getCurrentSetSize());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        mainActivityInterface.notifyToClearInlineSet();
    }



    // TODO reinstate these once I've rationalised them












    // Called after rebuilding the set list
    public void updateSet() {
        Log.d(TAG,"updateSet()");
        prepareCurrentSet();
    }



    public void prepareCurrentSet() {
        // Reset the setCurrent
        Log.d(TAG,"prepareCurrentSet()");
        mainActivityInterface.getCurrentSet().updateSetTitleView();
        /*if (!mainActivityInterface.getSetActions().getProcessingSet()) {
            Log.d(TAG, "prepareCurrentSet()");
            if (myView != null && mainActivityInterface != null) {
                // We have received a call to redraw the set list either on first load or after song indexing
                // The current adapter has been cleared already via notify
                myView.myRecyclerView.post(() -> {
                    if (myView != null) {
                        myView.myRecyclerView.setVisibility(View.INVISIBLE);
                    }
                });
                myView.progressBar.post(() -> {
                    if (myView != null) {
                        myView.progressBar.setVisibility(View.VISIBLE);
                    }
                });

                //notifyToInsertAllSet();

                mainActivityInterface.getCurrentSet().updateSetTitleView();

                myView.myRecyclerView.post(() -> myView.myRecyclerView.setVisibility(View.VISIBLE));
                myView.progressBar.post(() -> myView.progressBar.setVisibility(View.INVISIBLE));
            }
        }*/
    }

    public void updateHighlight() {
        Log.d(TAG,"updateHighlight() called");
        if (setAdapter!=null) {
            setAdapter.updateHighlight();
        }
    }

    // Called when we edit a set item from the bottom sheet
    public void updateItem(int position) {
        Log.d(TAG,"updateItem("+position+")");
        if (setAdapter!=null) {
            setAdapter.updateItem(position);
        }
    }





    public void initialiseSetItem() {
        Log.d(TAG,"initialiseSetItem()");
        /*// Only do this if we actually needed to highlight an item
        if (setAdapter!=null && setAdapter.initialiseSetItem()) {
            myView.myRecyclerView.post(() -> llm.scrollToPositionWithOffset(mainActivityInterface.getCurrentSet().getIndexSongInSet() , 0));
        }*/
    }



    public void notifyToInsertAllSet() {
        Log.d(TAG,"notifyToInsertAllSet()");

        /*// Called when a set is recalculated
        if (!mainActivityInterface.getSetActions().getProcessingSet()) {
            if (myView != null && setAdapter != null && mainActivityInterface.getCurrentSet().getCurrentSetSize() > 0) {
                Log.d(TAG, "notifyToUpdateSet()");
                // Because this can be done from a different menu/view, post
                myView.myRecyclerView.post(() -> setAdapter.notifyToInsertAllSet());
            }
            mainActivityInterface.notifyToInsertAllInlineSet();
        }*/
    }

    public void refreshLayout() {
        Log.d(TAG,"refreshLayout()");

        /*// First run or we have adjusted the font sizes from MenuSettingsFragment
        if (mainActivityInterface != null) {
            mainActivityInterface.getThreadPoolExecutor.execute(() -> {
                Handler handler = new Handler(Looper.getMainLooper());
                if (setAdapter!=null && mainActivityInterface.getCurrentSet().getCurrentSetSize()>0) {
                    setAdapter.notifyItemRangeRemoved(0,mainActivityInterface.getCurrentSet().getCurrentSetSize());
                }
                mainActivityInterface.getSetActions().parseCurrentSet();
                handler.post(() -> {
                    setupAdapter();
                    Log.d(TAG,"refreshLayout()");
                    prepareCurrentSet();
                    setListeners();
                    mainActivityInterface.getCurrentSet().updateSetTitleView();
                    scrollToItem();
                });
            });
        }*/
    }

}
