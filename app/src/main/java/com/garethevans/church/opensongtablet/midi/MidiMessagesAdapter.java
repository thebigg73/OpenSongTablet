package com.garethevans.church.opensongtablet.midi;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.MidiItemTouchInterface;

import java.util.ArrayList;

public class MidiMessagesAdapter extends RecyclerView.Adapter<MidiAdapterViewHolder> implements MidiItemTouchInterface {

    private final MainActivityInterface mainActivityInterface;
    private ItemTouchHelper itemTouchHelper;
    private ArrayList<MidiInfo> midiInfos;
    private final String TAG = "MidiMessagesAdapter";
    private boolean fromSongMessages = true;

    // Provide a suitable constructor (depends on the kind of dataset)
    public MidiMessagesAdapter(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
        midiInfos = new ArrayList<>();
    }

    public void updateMidiInfos(ArrayList<MidiInfo> newItems) {
        if (midiInfos!=null) {
            int size = getItemCount();
            midiInfos.clear();
            notifyItemRangeRemoved(0,size);
        } else {
            midiInfos = new ArrayList<>();
        }

        if (newItems!=null) {
            for (int x = 0; x < newItems.size(); x++) {
                midiInfos.add(x, newItems.get(x));
                notifyItemInserted(x);
            }
        }
        Log.d(TAG,"midiInfos.size(): "+midiInfos.size());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return midiInfos.size();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull MidiAdapterViewHolder holder, int position) {
        MidiInfo mi = midiInfos.get(position);
        String readableCommand = mi.readableCommand;
        String midiCommand = mi.midiCommand;

        if (readableCommand==null || readableCommand.trim().isEmpty()) {
            holder.vMidiReadable.setVisibility(View.GONE);
        } else if (!readableCommand.trim().isEmpty()) {
            holder.vMidiReadable.setVisibility(View.VISIBLE);
        }
        holder.vMidiReadable.setText(readableCommand);
        holder.vMidiCommand.setText(midiCommand);
    }

    @NonNull
    @Override
    public MidiAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.view_set_item, parent, false);
        return new MidiAdapterViewHolder(itemView,itemTouchHelper,this);
    }

    public void setTouchHelper(ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
    }

    public ArrayList<MidiInfo> getMidiInfos() {
        return midiInfos;
    }

    public int addToEnd(MidiInfo midiInfo) {
        midiInfos.add(midiInfo);
        notifyItemInserted(midiInfos.size()-1);
        return midiInfos.size()-1;
    }
    public void removeItem(int position) {
        midiInfos.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {
        Log.d(TAG,"onItemMoved() from:"+fromPosition+"  to:"+toPosition);

        String thisCommand = midiInfos.get(fromPosition).midiCommand;

        if (fromSongMessages) {
            // Remove this item
            mainActivityInterface.getMidi().removeFromSongMessages(fromPosition);

            // Add to the new position
            mainActivityInterface.getMidi().addToSongMessages(toPosition, thisCommand);

            // Update the song midi messages and save to the song
            mainActivityInterface.getMidi().updateSongMessages();

        }

        // Notify the changes to this adapter
        MidiInfo midiInfo = midiInfos.get(fromPosition);
        midiInfos.remove(fromPosition);
        midiInfos.add(toPosition,midiInfo);
        notifyItemMoved(fromPosition,toPosition);
    }

    public void setFromSongMessages(boolean fromSongMessages) {
        this.fromSongMessages = fromSongMessages;
    }

    @Override
    public void onItemSwiped(int fromPosition) {
        Log.d(TAG,"onItemSwiped() from:"+fromPosition);
        if (fromSongMessages) {
            // Remove from the song messages
            mainActivityInterface.getMidi().removeFromSongMessages(fromPosition);
            // Update and ave the song messages
            mainActivityInterface.getMidi().updateSongMessages();
        }

        // Remover from the adapter
        midiInfos.remove(fromPosition);
        notifyItemRemoved(fromPosition);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onItemClicked(int position) {
        Log.d(TAG,"onItemClicked() pos:"+position);
        // Send the midi message
        if (fromSongMessages) {
            mainActivityInterface.getMidi().sendMidi(position);
        }
    }

    @Override
    public void onContentChanged(int position) {
        Log.d(TAG,"onContentChaneges() pos:"+position);
    }

}