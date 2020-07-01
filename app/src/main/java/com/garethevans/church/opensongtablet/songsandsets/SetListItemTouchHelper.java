package com.garethevans.church.opensongtablet.songsandsets;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

class SetListItemTouchHelper extends ItemTouchHelper.SimpleCallback {

    SetListAdapter mAdapter;

    SetListItemTouchHelper(SetListAdapter mAdapter){
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            this.mAdapter = mAdapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        mAdapter.swap(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        //Remove item
        mAdapter.remove(viewHolder.getAdapterPosition());
    }
}