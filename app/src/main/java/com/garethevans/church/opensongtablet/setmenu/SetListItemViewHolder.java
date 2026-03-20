package com.garethevans.church.opensongtablet.setmenu;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyFloatingActionButton;
import com.garethevans.church.opensongtablet.customviews.MyMaterialSimpleTextView;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.SetItemTouchInterface;
import com.google.android.material.card.MaterialCardView;

public class SetListItemViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener,
        GestureDetector.OnGestureListener{
    // Identify the views
    final MaterialCardView cardView;
    final MyMaterialSimpleTextView cardItem, cardTitle, cardFilename, cardFolder;
    final RelativeLayout cardLayout;
    final MyFloatingActionButton cardEdit;
    private final ItemTouchHelper itemTouchHelper;
    private final SetItemTouchInterface setItemTouchInterface;
    private final MainActivityInterface mainActivityInterface;
    private final GestureDetector gestureDetector;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "SetListItemViewHolder";
    public SetListItemViewHolder(@NonNull View itemView, MainActivityInterface mainActivityInterface,
                                 ItemTouchHelper itemTouchHelper, SetItemTouchInterface setItemTouchInterface) {
        super(itemView);
        cardView = itemView.findViewById(R.id.card_view);
        cardLayout = itemView.findViewById(R.id.cardview_layout);
        cardItem = itemView.findViewById(R.id.cardview_item);
        cardTitle = itemView.findViewById(R.id.cardview_songtitle);
        cardFilename = itemView.findViewById(R.id.cardview_songfilename);
        cardFolder = itemView.findViewById(R.id.cardview_folder);
        cardEdit = itemView.findViewById(R.id.cardview_edit);
        itemView.setOnTouchListener(this);
        gestureDetector = new GestureDetector(itemView.getContext(),this);
        gestureDetector.setIsLongpressEnabled(false);
        this.itemTouchHelper = itemTouchHelper;
        this.setItemTouchInterface = setItemTouchInterface;
        this.mainActivityInterface = mainActivityInterface;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // Pass everything to the detector
        boolean handled = gestureDetector.onTouchEvent(motionEvent);

        // If the finger is lifted or the touch is cancelled,
        // make sure we allow the RecyclerView to breathe again
        if (motionEvent.getAction() == MotionEvent.ACTION_UP ||
                motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
            view.getParent().requestDisallowInterceptTouchEvent(false);
        }

        // IMPORTANT: Return true if handled, but also return true for DOWN
        // to ensure we get the rest of the events.
        return handled || motionEvent.getAction() == MotionEvent.ACTION_DOWN;
    }

    @Override
    public boolean onDown(@NonNull MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(@NonNull MotionEvent motionEvent) {}

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent motionEvent) {
        Log.d(TAG, "onSingleTapUp()");

        // 1. Run your click logic
        setItemTouchInterface.onItemClicked(mainActivityInterface, getAbsoluteAdapterPosition());

        // 2. FORCE the view to cancel its touch state.
        // This prevents the ItemTouchHelper from staying "primed" for a drag.
        motionEvent.setAction(MotionEvent.ACTION_CANCEL);
        gestureDetector.onTouchEvent(motionEvent);

        return true; // Return true to say we fully handled this
    }

    @Override
    public boolean onScroll(@Nullable MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent motionEvent) {
        // Tell the parent layout NOT to steal this touch event
        //if (itemView.getParent()!=null) {
        //    itemView.getParent().requestDisallowInterceptTouchEvent(true);
        //}
        //itemTouchHelper.startDrag(this);
        Log.d(TAG, "LongPress detected - letting ItemTouchHelper handle it");
    }

    @Override
    public boolean onFling(@Nullable MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float v, float v1) {
        return false;
    }
}
