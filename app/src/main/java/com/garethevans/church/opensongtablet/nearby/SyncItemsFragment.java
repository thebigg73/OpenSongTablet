package com.garethevans.church.opensongtablet.nearby;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsSyncItemsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class SyncItemsFragment extends Fragment {

    private SyncNearbyFragment syncNearbyFragment;
    private MainActivityInterface mainActivityInterface;
    private final String TAG = "SyncSongFragment";
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

        myView.showNewUpdate.setVisibility(what.equals("songs") ? View.VISIBLE:View.GONE);
        myView.showNewUpdate.setChecked(what.equals("songs"));

        myView.downloadItems.setVisibility(View.GONE);

        setListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            nearby_get_songs_string = getString(R.string.nearby_get_songs);
            nearby_get_sets_string = getString(R.string.nearby_get_setlist);
            nearby_get_profiles_string = getString(R.string.nearby_get_profiles);
        }
    }

    public void prepareRecycler() {
        if (getContext()!=null) {
            syncNearbyFragment.announceNotPrepared(what);
            mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                nearbySyncAdapter = new NearbySyncAdapter(getContext(), syncNearbyFragment, this);
                mainActivityInterface.getMainHandler().post(() -> {
                    if (myView != null) {
                        myView.itemsRecyclerView.setAdapter(nearbySyncAdapter);
                    }
                    syncNearbyFragment.announcePrepared(what);
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
        myView.showNewUpdate.setOnCheckedChangeListener((compoundButton, b) -> {
            prepareRecycler();
        });
        myView.downloadItems.setOnClickListener(view -> requestTheseItems());
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
        return myView.showNewUpdate.getChecked();
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
