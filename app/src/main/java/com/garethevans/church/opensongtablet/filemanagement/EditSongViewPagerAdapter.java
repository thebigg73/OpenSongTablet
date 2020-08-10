package com.garethevans.church.opensongtablet.filemanagement;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView.MeasurableAdapter;

public class EditSongViewPagerAdapter extends FragmentStateAdapter implements MeasurableAdapter {

    EditContent editContent;

    public final Fragment[] menuFragments = {new EditSongFragmentMain(), new EditSongFragmentFeatures(), new EditSongFragmentTags()};

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

    @Override
    public int getViewTypeHeight(RecyclerView recyclerView, @Nullable RecyclerView.ViewHolder viewHolder, int viewType) {
        return viewHolder.itemView.getMeasuredHeight();
    }
}
