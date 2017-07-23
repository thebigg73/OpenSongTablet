package com.garethevans.church.opensongtablet;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

class SetListItemTouchHelper extends ItemTouchHelper.SimpleCallback {
    private MyAdapter mAdapter;

    SetListItemTouchHelper(MyAdapter mAdapter){
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            this.mAdapter = mAdapter;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        mAdapter.swap(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        //Remove item
        mAdapter.remove(viewHolder.getAdapterPosition());
    }
}