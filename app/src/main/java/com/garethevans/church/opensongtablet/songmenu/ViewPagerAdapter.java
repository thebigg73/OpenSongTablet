package com.garethevans.church.opensongtablet.songmenu;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.garethevans.church.opensongtablet.setmenu.SetMenuFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public final Fragment[] menuFragments = {new SongMenuFragment(), new SetMenuFragment()};
    private int openMenu = 1;

    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager,Lifecycle lifecycle) {
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
