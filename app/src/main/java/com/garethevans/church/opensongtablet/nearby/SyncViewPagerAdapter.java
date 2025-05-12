package com.garethevans.church.opensongtablet.nearby;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class SyncViewPagerAdapter extends FragmentStateAdapter {
    public final Fragment[] menuFragments = {new SyncSongFragment(),
            new SyncSetFragment(), new SyncProfileFragment()};
    private int openMenu = 1;

    public SyncViewPagerAdapter(@NonNull FragmentManager fragmentManager, Lifecycle lifecycle) {
        super(fragmentManager,lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        openMenu = 1;
        return menuFragments[position];
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public boolean isSongMenu() {
        return openMenu==1;
    }
    public boolean isSetMenu() {
        return openMenu==2;
    }
    public boolean isProfileMenu() {
        return openMenu==3;
    }
}
