package com.garethevans.church.opensongtablet.nearby;

import androidx.fragment.app.Fragment;

public class SyncSetFragment extends Fragment {

    private SyncNearbyFragment syncNearbyFragment;

    public void setMainFragment(SyncNearbyFragment syncNearbyFragment) {
        this.syncNearbyFragment = syncNearbyFragment;
    }
}
