package com.garethevans.church.opensongtablet.midi;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.interfaces.MidiAdapterInterface;

import java.util.ArrayList;

public class MidiMessagesAdapter extends RecyclerView.Adapter<MidiMessagesAdapter.MyViewHolder> {

    private final ArrayList<String> dataSet;
    private final Context c;
    private final MidiAdapterInterface midiAdapterInterface;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textView;
        public MyViewHolder(TextView v) {
            super(v);
            textView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MidiMessagesAdapter(Context c, MidiAdapterInterface midiAdapterInterface, ArrayList<String> dataSet) {
        this.c = c;
        this.midiAdapterInterface = midiAdapterInterface;
        this.dataSet = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public MidiMessagesAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        TextView v = new TextView(c);
        return new MyViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView.setText(dataSet.get(position));
        holder.textView.setTextColor(Color.BLACK);
        holder.textView.setBackgroundColor(Color.LTGRAY);
        LinearLayout.LayoutParams  llp= new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llp.setMargins(12,12,12,12);
        holder.textView.setLayoutParams(llp);
        holder.textView.setTextSize(18.0f);
        holder.textView.setPadding(24, 24, 24, 24);
        holder.textView.setOnClickListener(v -> midiAdapterInterface.sendMidiFromList(position));
        holder.textView.setOnLongClickListener(v -> {
            midiAdapterInterface.deleteMidiFromList(position);
            return true;
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}