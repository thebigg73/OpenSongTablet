package com.garethevans.church.opensongtablet.songprocessing;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class EditSongViewPagerAdapter extends FragmentStateAdapter {

    public final Fragment[] menuFragments = {new EditSongFragmentLyrics(), new EditSongFragmentMain(), new EditSongFragmentFeatures(), new EditSongFragmentTags()};
    public EditSongViewPagerAdapter(@NonNull FragmentManager fragmentManager,Lifecycle lifecycle) {
        super(fragmentManager,lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return menuFragments[position];
    }

    @Override
    public int getItemCount() {
        return menuFragments.length;
    }

}
