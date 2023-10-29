package com.garethevans.church.opensongtablet.setmenu;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.SetItemTouchInterface;

public class SetListItemCallback extends ItemTouchHelper.Callback {

    private final Paint mClearPaint;
    private final ColorDrawable mBackground;
    private int backgroundColor;
    private final Drawable deleteDrawable;
    private final int intrinsicWidth;
    private final int intrinsicHeight;
    private boolean dragging;

    private final SetItemTouchInterface setItemTouchInterface;
    private final MainActivityInterface mainActivityInterface;
    private final String TAG = "SetListItemCallback";

    SetListItemCallback(Context c, SetAdapter setAdapter) {
        mainActivityInterface = (MainActivityInterface) c;
        setItemTouchInterface = setAdapter;
        mBackground = new ColorDrawable();
        try {
            backgroundColor = ContextCompat.getColor(c, R.color.vdarkred);
        } catch (Exception e) {
            if (c!=null) {
                backgroundColor = c.getResources().getColor(R.color.vdarkred);
            } else {
                backgroundColor = Color.parseColor("#660000");
            }
        }
        mClearPaint = new Paint();
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        deleteDrawable = ContextCompat.getDrawable(c, R.drawable.delete);
        if (deleteDrawable!=null) {
            intrinsicWidth = deleteDrawable.getIntrinsicWidth();
            intrinsicHeight = deleteDrawable.getIntrinsicHeight();
        } else {
            intrinsicWidth = 0;
            intrinsicHeight = 0;
        }
    }

    @Override
    public boolean isLongPressDragEnabled() {
        // return true here to enable long press on the RecyclerView rows for drag and drop
        // Dragging will be handled manually in the SetListItemViewHolder, so disable here
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        // This is used to enable or disable swipes
        return true;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        // Here we pass the flags for the directions of drag and swipe
        final int dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        final int swipeFlags = ItemTouchHelper.END;
        return makeMovementFlags(dragFlag,swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        // Here we set the code for the drag and drop
        Log.d(TAG,"onMove()");
        setItemTouchInterface.onItemMoved(viewHolder.getAbsoluteAdapterPosition(), target.getAbsoluteAdapterPosition());
        return true;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            dragging = true;
        } else if (!dragging) {
            Log.d(TAG,"drawing delete");
            View itemView = viewHolder.itemView;
            int itemHeight = itemView.getHeight();

            boolean isCancelled = dX == 0 && !isCurrentlyActive;

            if (isCancelled) {
                clearCanvas(c, itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, false);
                return;
            }

            mBackground.setColor(backgroundColor);
            mBackground.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getRight() + (int) dX, itemView.getBottom());
            mBackground.draw(c);

            int deleteIconMargin = 16;

            int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
            int deleteIconLeft = itemView.getLeft() + deleteIconMargin;
            int deleteIconRight = deleteIconLeft + intrinsicWidth;
            int deleteIconBottom = deleteIconTop + intrinsicHeight;


            deleteDrawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
            deleteDrawable.draw(c);

        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

    }

    private void clearCanvas(Canvas c, Float left, Float top, Float right, Float bottom) {
        c.drawRect(left, top, right, bottom, mClearPaint);
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.70f;
    }


    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}


    @Override
    public void onSelectedChanged(@Nullable @org.jetbrains.annotations.Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        // Based on the current state of the RecyclerView and whether it’s pressed or swiped, this method gets triggered.
        // Here we can customize the RecyclerView row. For example, changing the background color.
        Log.d(TAG,"onSelectedChanged()");
        Log.d(TAG,"actionState:"+actionState);
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            if (viewHolder instanceof SetListItemViewHolder) {
                SetListItemViewHolder myViewHolder =
                        (SetListItemViewHolder) viewHolder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ((SetListItemViewHolder) viewHolder).cardView.post(() -> ((SetListItemViewHolder) viewHolder).cardView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.colorSecondary))));
                }
                setItemTouchInterface.onRowSelected(myViewHolder);
            }
        }
        super.onSelectedChanged(viewHolder, actionState);
    }


    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        // This method gets triggered when the user interaction stops with the RecyclerView row
        Log.d(TAG,"clearView called");
        dragging = false;
        // Called when dragged item is released
        super.clearView(recyclerView, viewHolder);
        // Change the color back to normal if lollipop+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            viewHolder.itemView.post(() -> viewHolder.itemView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.colorAltPrimary))));
        }
        // Check we rehighlight the set item
        if (recyclerView.getAdapter()!=null) {
            SetAdapter setAdapter = (SetAdapter) recyclerView.getAdapter();
            int currentPosition = mainActivityInterface.getCurrentSet().getIndexSongInSet();
            if (currentPosition>=0 && currentPosition<setAdapter.getItemCount()) {
                Log.d(TAG,"trying to highlight position");
                viewHolder.itemView.postDelayed(setAdapter::recoverCurrentSetPosition,500);
                viewHolder.itemView.postDelayed(setAdapter::recoverCurrentSetPosition,800);
            }
        }
    }

}
