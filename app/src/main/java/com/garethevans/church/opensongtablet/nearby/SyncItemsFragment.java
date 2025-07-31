package com.garethevans.church.opensongtablet.nearby;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.SettingsSyncItemsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;
import java.util.Collections;

public class SyncItemsFragment extends Fragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "SyncItemsFragment";
    private SyncNearbyFragment syncNearbyFragment;
    private MainActivityInterface mainActivityInterface;
    private SettingsSyncItemsBinding myView;
    private NearbySyncAdapter nearbySyncAdapter;
    private String nearby_get_songs_string="";
    private String nearby_get_sets_string="";
    private String nearby_get_profiles_string="";
    private final String what;

    public SyncItemsFragment(String what) {
        this.what = what;
    }

    public void setMainFragment(SyncNearbyFragment syncNearbyFragment) {
        this.syncNearbyFragment = syncNearbyFragment;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Nullable
    @Override
    public @org.jetbrains.annotations.Nullable View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsSyncItemsBinding.inflate(inflater, container, false);

        mainActivityInterface = (MainActivityInterface) getContext();
        prepareStrings();
        myView.itemsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        myView.folderFilter.setVisibility(what.equals("songs") ? View.VISIBLE:View.GONE);
        prepareFolderDropDown();

        myView.showNewUpdate.setVisibility(what.equals("songs") ? View.VISIBLE:View.GONE);
        myView.showNewUpdate.setChecked(what.equals("songs"));

        myView.downloadItems.setVisibility(View.GONE);

        setListeners();

        return myView.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (myView!=null) {
            myView.itemsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        }
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            nearby_get_songs_string = getString(R.string.nearby_get_songs);
            nearby_get_sets_string = getString(R.string.nearby_get_setlist);
            nearby_get_profiles_string = getString(R.string.nearby_get_profiles);
        }
    }

    private void prepareFolderDropDown() {
        if (what.equals("songs") && getContext()!=null) {
            ArrayList<String> foldersAvailable = mainActivityInterface.getSQLiteHelper().getFolders();
            Collections.sort(foldersAvailable);
            // Add a blank option at the top
            foldersAvailable.add(0,"");
            ExposedDropDownArrayAdapter adapter = new ExposedDropDownArrayAdapter(getContext(),myView.folderFilter,R.layout.view_exposed_dropdown_item,foldersAvailable);
            myView.folderFilter.setAdapter(adapter);
        }
    }

    public void prepareRecycler(Context c) {
        if (c!=null) {
            mainActivityInterface = (MainActivityInterface) c;
            syncNearbyFragment.announceNotPrepared(what);
            Log.d(TAG,"announceNotPrepared("+what+")");
            mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                nearbySyncAdapter = new NearbySyncAdapter(c, syncNearbyFragment, this, what);
                mainActivityInterface.getMainHandler().post(() -> {
                    if (myView != null) {
                        if (myView.folderFilter.getText() != null) {
                            nearbySyncAdapter.chooseFolder(myView.folderFilter.getText().toString());
                        }
                        myView.itemsRecyclerView.setAdapter(nearbySyncAdapter);
                        nearbySyncAdapter.prepareItems();
                    }
                });
            });
        }
    }

    public void setListeners() {
        myView.selectAllItems.setOnCheckedChangeListener((compoundButton, b) -> {
            if (nearbySyncAdapter!=null) {
                nearbySyncAdapter.selectAll(b);
            }
        });
        myView.showNewUpdate.setOnCheckedChangeListener((compoundButton, b) -> prepareRecycler(getContext()));
        myView.downloadItems.setOnClickListener(view -> requestTheseItems());
        myView.folderFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                // Send the folderChosen to the arrayAdapter
                prepareRecycler(getContext());
                //syncNearbyFragment.announceNotPrepared(what);
                //nearbySyncAdapter.chooseFolder(myView.folderFilter.getText().toString());
            }
        });
    }

    public void setItemsSelected(int count, int totalItems) {
        // Depending on how many songs have been selected, change the 'Get songs' button
        mainActivityInterface.getMainHandler().post(() -> {
            if (myView!=null) {
                myView.downloadItems.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
                String string = " (" + count + ")";
                switch (what) {
                    case "sets":
                        string = nearby_get_sets_string + string;
                        break;
                    case "profiles":
                        string = nearby_get_profiles_string + string;
                        break;
                    case "songs":
                    default:
                        string = nearby_get_songs_string + string;
                        break;
                }
                myView.downloadItems.setText(string);
                myView.selectAllItems.setVisibility(totalItems>0 ? View.VISIBLE : View.GONE);
            }
        });
    }

    public String getWhat() {
        return what;
    }
    public boolean getShowNewUpdate() {
        // Show
        if (myView!=null) {
            return myView.showNewUpdate.getChecked();
        } else {
            // If we are dealing with songs, default to new/updated
            return what.equals("songs");
        }
    }

    public void requestTheseItems() {
        if (syncNearbyFragment!=null && syncNearbyFragment.getNearbyJson()!=null) {
            syncNearbyFragment.announceNotPrepared(what);
            String filename;
            switch (what) {
                case "sets":
                    filename = mainActivityInterface.getNearbyActions().requestSetsFile;
                    break;
                case "profiles":
                    filename = mainActivityInterface.getNearbyActions().requestProfilesFile;
                    break;
                case "songs":
                default:
                    filename = mainActivityInterface.getNearbyActions().requestSongsFile;
                    break;
            }
            mainActivityInterface.getNearbyActions().getNearbySendPayloads().sendSyncContentRequest(
                    syncNearbyFragment.getNearbyJson().getDeviceSending(), filename,
                    nearbySyncAdapter.getRequestedItems(filename));
        }
    }
}
