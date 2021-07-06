package com.garethevans.church.opensongtablet.songsandsetsmenu;

import android.content.res.ColorStateList;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.SetItemTouchInterface;

class SetListItemTouchHelper extends ItemTouchHelper.Callback {

    private final SetListAdapter setListAdapter;
    private final MainActivityInterface mainActivityInterface;
    private final SetItemTouchInterface setItemTouchInterface;
    private final String TAG = "SetListItemTouchHelper";

    public SetListItemTouchHelper(MainActivityInterface mainActivityInterface, SetListAdapter setListAdapter) {
        this.mainActivityInterface = mainActivityInterface;
        this.setItemTouchInterface = setListAdapter;
        this.setListAdapter = setListAdapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        // Dragging will be handled manually, so disable here
        return false;
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        // Called when dragged item is released
        super.clearView(recyclerView, viewHolder);
        // Change the color back to normal if lollipop+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            viewHolder.itemView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.colorAltPrimary)));
        }
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        // Called when an item is in a state of change (i.e. moving)
        super.onSelectedChanged(viewHolder, actionState);
        // If lollipop+, change the tint of the cardview item
        if (actionState==ItemTouchHelper.ACTION_STATE_DRAG) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                viewHolder.itemView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.colorSecondary)));
            }
        }
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        // To allow swiping to delete
        return true;
    }



    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags,swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAbsoluteAdapterPosition();
        int toPosition = target.getAbsoluteAdapterPosition();
        Log.d("SetListItem","fromPosition="+fromPosition+"  toPosition="+toPosition);
        setItemTouchInterface.onItemMoved(fromPosition,toPosition);
        //mAdapter.swap(mainActivityInterface.getCurrentSet(),oldPosition, newPosition);
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        //Remove item
        int fromPosition = viewHolder.getAbsoluteAdapterPosition();
        setItemTouchInterface.onItemSwiped(fromPosition);
        //mAdapter.remove(mainActivityInterface.getCurrentSet(),position);
    }

    public void startDrag(@NonNull RecyclerView.ViewHolder viewHolder) {
        Log.d(TAG, "viewHolder="+viewHolder);
    }

}