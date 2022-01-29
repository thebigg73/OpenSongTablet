package com.garethevans.church.opensongtablet.midi;

import android.content.res.ColorStateList;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MidiItemTouchInterface;

public class MidiItemTouchHelper extends ItemTouchHelper.Callback {

    private final MidiItemTouchInterface midiItemTouchInterface;

    private final String TAG = "SetListItemTouchHelper";

    public MidiItemTouchHelper(MidiMessagesAdapter midiMessagesAdapter) {
        this.midiItemTouchInterface = midiMessagesAdapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && viewHolder!=null) {
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
        midiItemTouchInterface.onItemMoved(fromPosition,toPosition);
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        //Remove item
        int fromPosition = viewHolder.getAbsoluteAdapterPosition();
        Log.d(TAG,"fromPosition: "+fromPosition);
        midiItemTouchInterface.onItemSwiped(fromPosition);
    }

    public void startDrag(RecyclerView.ViewHolder v) {
        Log.d(TAG,"Drag v="+v);
    }

}