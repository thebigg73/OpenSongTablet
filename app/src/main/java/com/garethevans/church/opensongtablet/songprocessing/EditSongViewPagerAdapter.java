package com.garethevans.church.opensongtablet.songprocessing;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class EditSongViewPagerAdapter extends FragmentStateAdapter {

    public EditSongViewPagerAdapter(@NonNull FragmentManager fragmentManager,Lifecycle lifecycle) {
        super(fragmentManager,lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1: return new EditSongFragmentMain();
            case 2: return new EditSongFragmentFeatures();
            case 3: return new EditSongFragmentTags();
            case 0:
            default: return new EditSongFragmentLyrics();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }

}
