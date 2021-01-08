package com.garethevans.church.opensongtablet.songsandsetsmenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

class SetListItemTouchHelper extends ItemTouchHelper.SimpleCallback {

    private final SetListAdapter mAdapter;
    private final MainActivityInterface mainActivityInterface;

    SetListItemTouchHelper(SetListAdapter mAdapter, MainActivityInterface mainActivityInterface){
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            this.mAdapter = mAdapter;
            this.mainActivityInterface = mainActivityInterface;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        mAdapter.swap(mainActivityInterface.getCurrentSet(),viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        //Remove item
        mAdapter.remove(mainActivityInterface.getCurrentSet(),viewHolder.getAdapterPosition());
    }
}