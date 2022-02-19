package com.garethevans.church.opensongtablet.presenter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PageAdapter extends FragmentStateAdapter {

    public final Fragment[] menuFragments = {new SongSectionsFragment(),
            new AdvancedFragment()};
    //new MediaFragment(),
    //new AlertFragment(),
    //new SettingsFragment()

    private int openMenu = 1;

    public PageAdapter(@NonNull FragmentManager fragmentManager, Lifecycle lifecycle) {
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
        return 2;
    }

    public boolean isSongMenu() {
        return openMenu==1;
    }
}