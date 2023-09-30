package com.garethevans.church.opensongtablet.setmenu;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.MenuSetsBinding;
import com.garethevans.church.opensongtablet.filemanagement.AreYouSureBottomSheet;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SetMenuFragment extends Fragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "SetMenuFragment";
    private MenuSetsBinding myView;
    private LinearLayoutManager llm;
    private String deeplink_sets_manage_string="", save_changes_string="", overwrite_string="",
            set_manage_click_string="", set_help_string="", set_manage_swipe_string="";
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
            myView.setTitle.setImageView(VectorDrawableCompat.create(getResources(), R.drawable.asterisk, getContext().getTheme()), Color.WHITE);
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
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        runSetShowcase();
    }

    private void setupAdapter() {
        if (getContext()!=null) {
            mainActivityInterface.newSetListAdapter();
            ItemTouchHelper.Callback callback = new SetListItemTouchHelper(getContext());
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
            mainActivityInterface.getSetListAdapter().setTouchHelper(itemTouchHelper);
            llm = new LinearLayoutManager(getContext());
            llm.setOrientation(RecyclerView.VERTICAL);
            myView.myRecyclerView.post(() -> {
                myView.myRecyclerView.setLayoutManager(llm);
                myView.myRecyclerView.setAdapter(mainActivityInterface.getSetListAdapter());
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
            myView.myRecyclerView.post(() -> {
                if (llm!=null) {
                    llm.scrollToPositionWithOffset(mainActivityInterface.getCurrentSet().getIndexSongInSet(), 0);
                }
            });
        }
    }

    public void updateSet() {
        prepareCurrentSet();
    }

    public void updateKeys() {
        // If the key has changed on some items, update them
        if (mainActivityInterface.getSetActions().getMissingKeyPositions()!=null &&
                mainActivityInterface.getSetListAdapter()!=null &&
                mainActivityInterface.getSetListAdapter().getSetList()!=null &&
                mainActivityInterface.getSetListAdapter().getSetList().size()>0) {
            mainActivityInterface.getSetListAdapter().updateKeys();
        }
    }

    public void prepareCurrentSet() {
        // We have received a call to redraw the set list either on first load or after song indexing
        myView.myRecyclerView.post(() -> {
            // Clear the original setlist by passing in a new blank arraylist
            // This also deals with notifying changes
            mainActivityInterface.getSetListAdapter().buildSetList();
            mainActivityInterface.getCurrentSet().updateSetTitleView();
        });
    }

    private void buildList() {
        Log.d(TAG,"buildList() called");
        try {
            if (mainActivityInterface.getSetListAdapter()!=null) {
                mainActivityInterface.getSetListAdapter().buildSetList();
                myView.myRecyclerView.post(() -> {
                    try {
                        myView.myRecyclerView.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

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
        Log.d(TAG,"updateItem called: "+position);
        if (mainActivityInterface.getSetListAdapter()!=null) {
            mainActivityInterface.getSetListAdapter().updateItem(position);
        }
    }

    public void clearOldHighlight(int position) {
        if (mainActivityInterface.getSetListAdapter()!=null) {
            mainActivityInterface.getSetListAdapter().clearOldHighlight(position);
        }
    }

    public void addSetItem(int currentSetPosition) {
        if (mainActivityInterface.getSetListAdapter()!=null) {
            mainActivityInterface.getSetListAdapter().addNewItem(currentSetPosition);
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
        if (mainActivityInterface.getSetListAdapter()!=null) {
            mainActivityInterface.getSetListAdapter().itemRemoved(currentSetPosition);
        }
        mainActivityInterface.getCurrentSet().updateSetTitleView();
    }

    public void scrollMenu(int height) {
        try {
            myView.myRecyclerView.smoothScrollBy(0, height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //getFocus();
    }

    public void initialiseSetItem(int setPosition) {
        // Only do this if we actually needed to highlight an item
        if (mainActivityInterface.getSetListAdapter()!=null && mainActivityInterface.getSetListAdapter().initialiseSetItem(setPosition)) {
            myView.myRecyclerView.post(() -> llm.scrollToPositionWithOffset(mainActivityInterface.getCurrentSet().getIndexSongInSet() , 0));
        }
    }

}
