package com.garethevans.church.opensongtablet.nearby;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class SyncViewPagerAdapter extends FragmentStateAdapter {

    private Context c;
    public void setContext(Context c) {
        this.c = c;
    }
    public final Fragment[] menuFragments = {new SyncItemsFragment("songs"),
            new SyncItemsFragment("sets"), new SyncItemsFragment("profiles")};

    public SyncViewPagerAdapter(@NonNull FragmentManager fragmentManager, Lifecycle lifecycle) {
        super(fragmentManager,lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return menuFragments[position];
    }

    @Override
    public int getItemCount() {
        return 3;
    }

}
