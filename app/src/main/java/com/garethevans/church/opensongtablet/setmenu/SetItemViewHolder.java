package com.garethevans.church.opensongtablet.setmenu;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.SetItemTouchInterface;
import com.google.android.material.textview.MaterialTextView;

public class SetItemViewHolder extends RecyclerView.ViewHolder implements
        View.OnTouchListener, GestureDetector.OnGestureListener {

    final MaterialTextView vItem;
    final MaterialTextView vSongTitle;
    final MaterialTextView vSongFolder;
    final RelativeLayout vCard;
    final CardView cardView;
    private final ItemTouchHelper itemTouchHelper;
    private final SetItemTouchInterface setItemTouchInterface;
    private final GestureDetector gestureDetector;
    private final MainActivityInterface mainActivityInterface;

    public SetItemViewHolder(MainActivityInterface mainActivityInterface, View v, ItemTouchHelper itemTouchHelper, SetItemTouchInterface setItemTouchInterface) {
        super(v);
        cardView = v.findViewById(R.id.card_view);
        vCard = v.findViewById(R.id.cardview_layout);
        vItem = v.findViewById(R.id.cardview_item);
        vSongTitle = v.findViewById(R.id.cardview_songtitle);
        vSongFolder = v.findViewById(R.id.cardview_folder);
        v.setOnTouchListener(this);
        gestureDetector = new GestureDetector(v.getContext(),this);
        this.itemTouchHelper = itemTouchHelper;
        this.setItemTouchInterface = setItemTouchInterface;
        this.mainActivityInterface = mainActivityInterface;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {}

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        setItemTouchInterface.onItemClicked(mainActivityInterface,getAbsoluteAdapterPosition());
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        try {
            itemTouchHelper.startDrag(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        v.performClick();
        return true;
    }

}