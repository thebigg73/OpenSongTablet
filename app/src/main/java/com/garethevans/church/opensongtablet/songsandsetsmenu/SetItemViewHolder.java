package com.garethevans.church.opensongtablet.songsandsetsmenu;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.SetItemTouchInterface;

public class SetItemViewHolder extends RecyclerView.ViewHolder implements
        View.OnTouchListener, GestureDetector.OnGestureListener {

    final View v;
    final TextView vItem;
    final TextView vSongTitle;
    final TextView vSongFolder;
    final RelativeLayout vCard;
    ItemTouchHelper itemTouchHelper;
    SetItemTouchInterface setItemTouchInterface;
    GestureDetector gestureDetector;
    MainActivityInterface mainActivityInterface;

    public SetItemViewHolder(MainActivityInterface mainActivityInterface, View v, ItemTouchHelper itemTouchHelper, SetItemTouchInterface setItemTouchInterface) {
        super(v);
        this.v = v;
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
    public void onShowPress(MotionEvent e) {

    }

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