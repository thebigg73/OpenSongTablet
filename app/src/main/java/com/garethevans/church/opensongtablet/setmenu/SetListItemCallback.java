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
import androidx.cardview.widget.CardView;
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
    private Drawable deleteDrawable;
    private final int intrinsicWidth;
    private final int intrinsicHeight;
    private boolean dragging;
    private int originalColorForDragging = -1;

    private final SetItemTouchInterface setItemTouchInterface;
    private final MainActivityInterface mainActivityInterface;
    private final SetAdapter setAdapter;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String TAG = "SetListItemCallback";

    SetListItemCallback(Context c, SetAdapter setAdapter) {
        mainActivityInterface = (MainActivityInterface) c;
        setItemTouchInterface = setAdapter;
        this.setAdapter = setAdapter;
        mBackground = new ColorDrawable();
        try {
            backgroundColor = ContextCompat.getColor(c, R.color.vdarkred);
        } catch (Exception e) {
            if (c != null) {
                backgroundColor = c.getResources().getColor(R.color.vdarkred);
            } else {
                backgroundColor = Color.parseColor("#660000");
            }
        }
        mClearPaint = new Paint();
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        deleteDrawable = null;
        try {
            deleteDrawable = ContextCompat.getDrawable(c, R.drawable.delete);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (deleteDrawable != null) {
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
        return true;
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
        return makeMovementFlags(dragFlag, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        // Here we set the code for the drag and drop
        setItemTouchInterface.onItemMoved(viewHolder.getAbsoluteAdapterPosition(), target.getAbsoluteAdapterPosition());
        return true;
    }


    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

        // Only draw the red background if we are currently swiping
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            View itemView = viewHolder.itemView;

            // If the swipe is finished and returned to 0, clear it
            if (dX == 0 && !isCurrentlyActive) {
                clearCanvas(c, (float) itemView.getLeft(), (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                return;
            }

            // DRAWING THE "UNDERLAY"
            // We draw the red background in the gap created by dX
            mBackground.setColor(backgroundColor);

            // Use Math.max to ensure the right bound never goes 'negative'
            // if the user tries to swipe slightly left
            int rightBound = itemView.getLeft() + (int) dX;

            mBackground.setBounds(
                    itemView.getLeft(),
                    itemView.getTop(),
                    rightBound,
                    itemView.getBottom()
            );
            mBackground.draw(c);

            // ICON LOGIC
            if (deleteDrawable != null && dX > (intrinsicWidth + 32)) {
                int itemHeight = itemView.getHeight();
                int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int deleteIconLeft = itemView.getLeft() + 32; // Fixed margin from left
                int deleteIconRight = deleteIconLeft + intrinsicWidth;
                int deleteIconBottom = deleteIconTop + intrinsicHeight;

                deleteDrawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
                deleteDrawable.draw(c);
            }
        }

        // super must be called to draw the actual song item over our red background
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private void clearCanvas(Canvas c, Float left, Float top, Float right, Float bottom) {
        c.drawRect(left, top, right, bottom, mClearPaint);
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        // return 0.70f;
        return 0.50f;
    }


    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // 1. Get the current position of the item being swiped
        final int position = viewHolder.getBindingAdapterPosition();

        // If position is -1, the item is already gone or being animated
        if (position == RecyclerView.NO_POSITION) return;

        // 2. Use a 'post' to ensure the swipe animation finishes its physical
        // travel before we modify the adapter's data.
        viewHolder.itemView.post(new Runnable() {
            @Override
            public void run() {
                // Call your existing removeItem method
                // 'true' updates the checkmarks in your song menu
                setAdapter.removeItem(position, true);
            }
        });
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        // Makes the item fly off the screen faster with a smaller 'flick'
        return defaultValue * 0.2f;
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        // 1. Handle the Drag Start (Your existing logic)
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            // Only change background if we are DEFINITELY dragging
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                // Provide a small vibration when the drag starts
                if (viewHolder!=null) {
                    viewHolder.itemView.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
                }
                dragging = true; // Make sure this boolean is set!
                if (viewHolder instanceof SetListItemViewHolder) {
                    SetListItemViewHolder myViewHolder = (SetListItemViewHolder) viewHolder;
                    originalColorForDragging = myViewHolder.cardView.getCardBackgroundColor().getDefaultColor();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        myViewHolder.cardView.setCardBackgroundColor(
                                ColorStateList.valueOf(mainActivityInterface.getMyThemeColors().getSetDraggedColor(viewHolder.itemView))
                        );
                    }
                    setItemTouchInterface.onRowSelected(myViewHolder);
                }
            }
        }


        // 2. THE FIX: Handle when the gesture ends (Idle state)
        if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            // This triggers when the user lets go.
            // If they were swiping and it didn't finish, this forces the visual reset.
            if (viewHolder != null) {
                getDefaultUIUtil().clearView(viewHolder.itemView);
            }
        }

        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {

        // 1. Reset the visual translation (fixes the "sliver" and "stuck" view)
        getDefaultUIUtil().clearView(viewHolder.itemView);
        super.clearView(recyclerView, viewHolder);

        // 2. Reset background color if it was a CardView
        if (viewHolder.itemView instanceof CardView) {
            ((CardView) viewHolder.itemView).setCardBackgroundColor(originalColorForDragging);
        }

        // 3. ONLY refresh everything if we were DRAGGING
        // If we were swiping, the removeItem method handles the update safely.
        if (dragging) {
            dragging = false;

            if (recyclerView.getAdapter() != null) {
                SetAdapter adapter = (SetAdapter) recyclerView.getAdapter();
                recyclerView.post(() -> {
                    adapter.notifyDataSetChanged();
                    mainActivityInterface.getCurrentSet().updateSetTitleView();
                    mainActivityInterface.notifyInlineSetChanged(-1);
                    mainActivityInterface.getDisplayPrevNext().setPrevNext();
                });
            }
        } else {
            // We were swiping, not dragging.
            // Just do a lightweight update for the title/buttons without
            // nuking the RecyclerView's internal state.
            mainActivityInterface.getCurrentSet().updateSetTitleView();
            mainActivityInterface.getDisplayPrevNext().setPrevNext();
        }
    }

}
