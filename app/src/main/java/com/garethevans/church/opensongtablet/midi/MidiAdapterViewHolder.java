package com.garethevans.church.opensongtablet.midi;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MidiItemTouchInterface;

public class MidiAdapterViewHolder  extends RecyclerView.ViewHolder  implements
        View.OnTouchListener, GestureDetector.OnGestureListener{

    final TextView vItem;
    final TextView vMidiReadable;
    final TextView vMidiCommand;
    final RelativeLayout vCard;
    private final ItemTouchHelper itemTouchHelper;
    private final MidiItemTouchInterface midiItemTouchInterface;
    private final GestureDetector gestureDetector;
    private final String TAG = "MidiAdapterViewHolder";

    public MidiAdapterViewHolder(View v, ItemTouchHelper itemTouchHelper, MidiItemTouchInterface midiItemTouchInterface) {
        super(v);
        vCard = itemView.findViewById(R.id.cardview_layout);
        vItem = itemView.findViewById(R.id.cardview_item);
        vItem.setVisibility(View.GONE);
        vMidiReadable = itemView.findViewById(R.id.cardview_songtitle);
        vMidiCommand = itemView.findViewById(R.id.cardview_folder);
        v.setOnTouchListener(this);
        gestureDetector = new GestureDetector(v.getContext(),this);
        this.itemTouchHelper = itemTouchHelper;
        this.midiItemTouchInterface = midiItemTouchInterface;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) { }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        Log.d(TAG,"onSingleTapUp()");
        midiItemTouchInterface.onItemClicked(getAbsoluteAdapterPosition());
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
        Log.d(TAG,"event="+motionEvent);
        try {
            itemTouchHelper.startDrag(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        gestureDetector.onTouchEvent(motionEvent);
        view.performClick();
        return true;
    }


}
