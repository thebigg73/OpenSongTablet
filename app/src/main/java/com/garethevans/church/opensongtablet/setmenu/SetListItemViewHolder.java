package com.garethevans.church.opensongtablet.setmenu;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.SetItemTouchInterface;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

public class SetListItemViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener,
        GestureDetector.OnGestureListener{
    // Identify the views
    final CardView cardView;
    final MaterialTextView cardItem, cardTitle, cardFilename, cardFolder;
    final RelativeLayout cardLayout;
    final FloatingActionButton cardEdit;
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
        this.itemTouchHelper = itemTouchHelper;
        this.setItemTouchInterface = setItemTouchInterface;
        this.mainActivityInterface = mainActivityInterface;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        gestureDetector.onTouchEvent(motionEvent);
        view.performClick();
        return true;
    }

    @Override
    public boolean onDown(@NonNull MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(@NonNull MotionEvent motionEvent) {}

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent motionEvent) {
        setItemTouchInterface.onItemClicked(mainActivityInterface,getAbsoluteAdapterPosition());
        return false;
    }

    @Override
    public boolean onScroll(@Nullable MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent motionEvent) {
        itemTouchHelper.startDrag(this);
    }

    @Override
    public boolean onFling(@Nullable MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float v, float v1) {
        return false;
    }
}
