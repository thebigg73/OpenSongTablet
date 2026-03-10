package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class PresoOrderItemTouchHelper extends ItemTouchHelper.Callback {

    private final PresentationOrderAdapter presentationOrderAdapter;
    private ColorStateList prevColor;
    private final MainActivityInterface mainActivityInterface;

    PresoOrderItemTouchHelper(Context c, PresentationOrderAdapter presentationOrderAdapter) {
        this.presentationOrderAdapter = presentationOrderAdapter;
        mainActivityInterface = (MainActivityInterface) c;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAbsoluteAdapterPosition();
        int toPosition = target.getAbsoluteAdapterPosition();
        presentationOrderAdapter.onItemMoved(fromPosition,toPosition);
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int fromPosition = viewHolder.getAbsoluteAdapterPosition();
        presentationOrderAdapter.onItemDismissed(fromPosition);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        // Same as default, but keep it
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        // Same as default, but keep it
        return true;
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        // Called when dragged item is released
        super.clearView(recyclerView, viewHolder);
        // Change the color back to normal if lollipop+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (prevColor==null) {
                viewHolder.itemView.setBackgroundTintList(ColorStateList.valueOf(mainActivityInterface.getPalette().secondaryVariant));
                prevColor = ColorStateList.valueOf(mainActivityInterface.getPalette().primaryVariant);
            } else {
                viewHolder.itemView.setBackgroundTintList(prevColor);
            }
        }
        // TRIGGER THE SYNC HERE
        presentationOrderAdapter.finalSync();
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        // Called when an item is in a state of change (i.e. moving)
        super.onSelectedChanged(viewHolder, actionState);
        // If lollipop+, change the tint of the cardview item
        if (actionState==ItemTouchHelper.ACTION_STATE_DRAG) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && viewHolder!=null) {
                prevColor = viewHolder.itemView.getBackgroundTintList();
                viewHolder.itemView.setBackgroundTintList(ColorStateList.valueOf(mainActivityInterface.getPalette().secondary));
            }
        }
    }

}