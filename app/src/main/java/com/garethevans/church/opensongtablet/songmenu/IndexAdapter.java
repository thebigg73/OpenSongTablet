package com.garethevans.church.opensongtablet.songmenu;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class IndexAdapter extends RecyclerView.Adapter<IndexViewHolder> {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "IndexAdapter";
    private final List<String> displayLetters = new ArrayList<>();
    private LinkedHashMap<String, Integer> fullAlphaIndex; // The map from SongListAdapter
    private String currentPrefix = ""; // Track if we are in "B" or ""
    private OnIndexClickListener listener;
    private float indexTextSize = 14f;
    private boolean isLevel2Enabled = false;

    public interface OnIndexClickListener {
        void onIndexClick(String clickedText, int songPosition, boolean isBackAction);
    }

    public void setData(LinkedHashMap<String, Integer> map, OnIndexClickListener listener, boolean isLevel2Enabled) {
        this.fullAlphaIndex = map;
        this.listener = listener;
        this.isLevel2Enabled = isLevel2Enabled;

        // If we just disabled Level 2, we must clear any active expansion
        if (!isLevel2Enabled) {
            currentPrefix = "";
        }

        refreshDisplayList();
    }

    public void setIndexTextSize(float indexTextSize) {
        this.indexTextSize = indexTextSize;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshDisplayList() {
        displayLetters.clear();
        if (fullAlphaIndex == null) return;

        // Using a LinkedHashSet ensures we don't get duplicates (like 'A' and 'A')
        // while keeping the alphabetical order.
        Set<String> listBuilder = new LinkedHashSet<>();

        for (String key : fullAlphaIndex.keySet()) {
            if (key == null || key.isEmpty()) continue;

            String firstChar = key.substring(0, 1).toUpperCase();

            // ALWAYS add the Level 1 character
            listBuilder.add(firstChar);

            // ONLY add Level 2 characters if enabled AND they match the clicked prefix
            if (isLevel2Enabled && !currentPrefix.isEmpty() && firstChar.equals(currentPrefix)) {
                if (key.length() == 2) {
                    listBuilder.add(key);
                }
            }
        }

        displayLetters.addAll(listBuilder);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IndexViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_alphabetical_list, parent, false);
        return new IndexViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull IndexViewHolder holder, int position) {
        String text = displayLetters.get(position);
        holder.textView.setText(text);
        holder.textView.setTextSize(indexTextSize);
        holder.textView.setMinimumWidth((int)(5*indexTextSize));
        if (text.length() > 1) {
            holder.textView.setPadding(40, 10, 10, 10); // Extra left padding for sub-items
            holder.textView.setAlpha(0.8f); // Slightly faded/subtle
        } else {
            holder.textView.setPadding(15, 10, 10, 10);
            holder.textView.setAlpha(1.0f);
        }

        // --- STANDARD CLICK (Scroll + Expand) ---
        holder.itemView.setOnClickListener(v -> handleSelection(text));

        // --- LONG CLICK (Collapse All) ---
        holder.itemView.setOnLongClickListener(v -> {
            if (isLevel2Enabled && !currentPrefix.isEmpty()) {
                currentPrefix = ""; // Collapse everything
                refreshDisplayList();

                // Scroll the clicked letter to top in its new collapsed position
                int newPos = displayLetters.indexOf(text);
                if (newPos != -1) {
                    listener.onIndexClick(text, newPos, true);
                }
                return true; // Return true to consume the click
            }
            return false;
        });
    }

    private void handleSelection(String text) {
        // 1. Toggle expansion for Level 1
        if (isLevel2Enabled && text.length() == 1) {
            currentPrefix = currentPrefix.equals(text) ? "" : text;
            refreshDisplayList();
        }

        // 2. Find position in the CURRENT (potentially updated) list
        int newIndexPos = displayLetters.indexOf(text);
        if (newIndexPos != -1) {
            listener.onIndexClick(text, newIndexPos, true); // Snap index to top
        }

        // 3. Scroll main song list
        Integer songPos = fullAlphaIndex.get(text);
        if (songPos != null && songPos != -1) {
            listener.onIndexClick(text, songPos, false);
        }
    }

    @Override
    public int getItemCount() {
        return displayLetters.size();
    }

    public void collapseAll() {
        this.currentPrefix = "";
        refreshDisplayList();
    }
}
