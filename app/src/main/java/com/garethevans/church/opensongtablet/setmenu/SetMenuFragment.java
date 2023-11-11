package com.garethevans.church.opensongtablet.setmenu;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.MenuSetsBinding;
import com.garethevans.church.opensongtablet.filemanagement.AreYouSureBottomSheet;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            try {
                myView.setTitle.setImageView(VectorDrawableCompat.create(getResources(), R.drawable.asterisk, getContext().getTheme()), Color.WHITE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            myView.setTitle.getImageView().setVisibility(View.GONE);
        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            mainActivityInterface.getSetActions().buildSetArraysFromItems();
            handler.post(() -> {
                setupAdapter();
                prepareCurrentSet();
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
            set_item_removed_string = getString(R.string.set_item_removed);
            undo_string = getString(R.string.undo);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        runSetShowcase();
    }

    private void setupAdapter() {
        if (getContext()!=null) {
            setAdapter = new SetAdapter(getContext(), myView.myRecyclerView);
            myView.myRecyclerView.post(() -> {
                llm = new LinearLayoutManager(getContext());
                llm.setOrientation(RecyclerView.VERTICAL);
                myView.myRecyclerView.setLayoutManager(llm);
                enableSwipeToDeleteAndUndo();
                myView.myRecyclerView.setAdapter(setAdapter);
                myView.myRecyclerView.setItemAnimator(null);
            });
        }
    }


    private void enableSwipeToDeleteAndUndo() {
        SetListItemCallback setListItemCallback = new SetListItemCallback(getContext(),setAdapter) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                final int position = viewHolder.getAbsoluteAdapterPosition();
                final SetItemInfo item = setAdapter.setList.get(position);

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

    public void scrollToItem() {
        if (mainActivityInterface.getCurrentSet().getIndexSongInSet()>-1 &&
                mainActivityInterface.getCurrentSet().getIndexSongInSet() < mainActivityInterface.getCurrentSet().getSetItems().size()) {
            myView.myRecyclerView.post(() -> {
                if (llm!=null) {
                    llm.scrollToPositionWithOffset(mainActivityInterface.getCurrentSet().getIndexSongInSet(), 0);
                }
            });
        }
    }

    // Called after rebuilding the set list
    public void updateSet() {
        prepareCurrentSet();
    }

    public void updateKeys() {
        // If the key has changed on some items, update them
        if (mainActivityInterface.getSetActions().getMissingKeyPositions()!=null &&
                setAdapter!=null &&
                setAdapter.getSetList()!=null &&
                setAdapter.getSetList().size()>0) {
            setAdapter.updateKeys();
        }
    }

    public void prepareCurrentSet() {
        // We have received a call to redraw the set list either on first load or after song indexing
        myView.myRecyclerView.post(() -> myView.myRecyclerView.setVisibility(View.INVISIBLE));
        myView.progressBar.post(() -> myView.progressBar.setVisibility(View.VISIBLE));

        if (setAdapter!=null) {
            setAdapter.buildSetList();
            // Notify the adapter (this is run on UI thread)
            setAdapter.notifyAllChanged();
        }
        mainActivityInterface.getCurrentSet().updateSetTitleView();

        myView.myRecyclerView.post(() -> myView.myRecyclerView.setVisibility(View.VISIBLE));
        myView.progressBar.post(() -> myView.progressBar.setVisibility(View.INVISIBLE));

    }

    public void updateItem(int position) {
        if (setAdapter!=null) {
            setAdapter.updateItem(position);
        }
    }

    public void clearOldHighlight(int position) {
        if (setAdapter!=null) {
            setAdapter.clearOldHighlight(position);
        }
    }

    public void addSetItem(int currentSetPosition) {
        if (setAdapter!=null) {
            setAdapter.addNewItem(currentSetPosition);
        }
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
        if (setAdapter!=null) {
            setAdapter.removeItem(currentSetPosition);
        }
    }

    public void scrollMenu(int height) {
        try {
            myView.myRecyclerView.smoothScrollBy(0, height);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initialiseSetItem(int setPosition) {
        // Only do this if we actually needed to highlight an item
        if (setAdapter!=null && setAdapter.initialiseSetItem(setPosition)) {
            myView.myRecyclerView.post(() -> llm.scrollToPositionWithOffset(mainActivityInterface.getCurrentSet().getIndexSongInSet() , 0));
        }
    }

}
