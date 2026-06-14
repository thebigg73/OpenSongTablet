package com.garethevans.church.opensongtablet.analytics;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.BottomSheetCommon;
import com.garethevans.church.opensongtablet.databinding.BottomSheetAlertInfoBinding;
import com.garethevans.church.opensongtablet.databinding.BottomSheetAnalyticsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.screensetup.Palette;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.sqlite.SQLite;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsBottomSheet extends BottomSheetCommon {

    private MainActivityInterface mainActivityInterface;
    private BottomSheetAnalyticsBinding myView;
    private final String TAG = "AnalyticsBottomSheet";
    private String popularity_string="", last_viewed_string="", last_cast_string="",
            last_added_to_set_string="", count_string="", count_set_string="";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetAnalyticsBinding.inflate(inflater,container,false);

        myView.dialogHeading.setClose(this);

        prepareStrings();

        setupViews();

        setupListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        last_cast_string = getString(R.string.analytics_last_cast);
        last_viewed_string = getString(R.string.analytics_last_viewed);
        last_added_to_set_string = getString(R.string.analytics_last_added_to_set);
        count_set_string = getString(R.string.analytics_count);
        popularity_string = getString(R.string.analytics_popularity);
    }

    private void setupViews() {
        myView.analyticsRecyclerView.removeAllViews();

        switch (SortMode.lastSortMethod) {
            case SortMode.LAST_CAST:
                myView.dialogHeading.setText(last_cast_string);
                break;

            case SortMode.LAST_SET:
                myView.dialogHeading.setText(last_added_to_set_string);
                break;

            case SortMode.LAST_VIEWED:
                myView.dialogHeading.setText(last_viewed_string);
                break;

            case SortMode.COUNT_SET:
                myView.dialogHeading.setText(count_set_string);
                break;

            case SortMode.POPULARITY:
            case "default":
                myView.dialogHeading.setText(popularity_string);
                break;
        }

        myView.analyticsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Fetch data
        List<AnalyticsItem> data = fetchAnalyticsData();
        myView.analyticsRecyclerView.setAdapter(new AnalyticsAdapter(getContext(), data, song -> {
            mainActivityInterface.doSongLoad(song.folder, song.filename,true);
            dismiss();
        }));
    }

    private void setupListeners() {
        // The song items listeners are dealt with in the adapter
        myView.sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"clicked");
                if (getContext()!=null) {
                    Dialog dialog = new Dialog(getContext());
                    dialog.setContentView(R.layout.view_sort_analytics_options);

                    // Set background to transparent so your custom XML background shows
                    if (dialog.getWindow() != null) {
                        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.popup_bg);
                        if (drawable!=null) {
                            DrawableCompat.setTint(drawable, mainActivityInterface.getPalette().secondary);
                            dialog.getWindow().setBackgroundDrawable(drawable);
                        }
                    }

                    // Programmatically find and configure views
                    // Set listeners for each text view to perform the sort
                    dialog.findViewById(R.id.sort_item_popularity).setOnClickListener(v -> {
                        SortMode.lastSortMethod = SortMode.POPULARITY;
                        setupViews();
                        dialog.dismiss();
                    });

                    dialog.findViewById(R.id.sort_item_last_viewed).setOnClickListener(v -> {
                        SortMode.lastSortMethod = SortMode.LAST_VIEWED;
                        setupViews();
                        dialog.dismiss();
                    });

                    dialog.findViewById(R.id.sort_item_last_added).setOnClickListener(v -> {
                        SortMode.lastSortMethod = SortMode.LAST_SET;
                        setupViews();
                        dialog.dismiss();
                    });

                    dialog.findViewById(R.id.sort_item_added_to_set).setOnClickListener(v -> {
                        SortMode.lastSortMethod = SortMode.COUNT_SET;
                        setupViews();
                        dialog.dismiss();
                    });

                    dialog.findViewById(R.id.sort_item_last_cast).setOnClickListener(v -> {
                        SortMode.lastSortMethod = SortMode.LAST_CAST;
                        setupViews();
                        dialog.dismiss();
                    });

                    dialog.findViewById(R.id.reset_analytics).setOnClickListener(v -> {
                        mainActivityInterface.getAnalyticsHelper().resetAnalytics();
                        setupViews();
                        dialog.dismiss();
                    });

                    // Show the dialog
                    dialog.show();
                }
            }
        });
    }
    private List<AnalyticsItem> fetchAnalyticsData() {
        // 1. Get analytics into a Map for fast lookup
        ArrayList<AnalyticsItem> resultList = new ArrayList<>();
        try (SQLiteDatabase analytics = mainActivityInterface.getAnalyticsHelper().getReadableDatabase()) {
            Map<String, AnalyticsItem> analyticsMap = new HashMap<>();
            Cursor cursor = analytics.query("song_analytics", null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                AnalyticsItem item = new AnalyticsItem();
                item.uuid = cursor.getString(cursor.getColumnIndexOrThrow(SQLite.COLUMN_SONG_UUID));
                item.viewCount = cursor.getInt(cursor.getColumnIndexOrThrow(SQLite.COLUMN_VIEW_COUNT));
                item.lastAddToSet = cursor.getLong(cursor.getColumnIndexOrThrow(SQLite.COLUMN_LAST_SET_DATE));
                item.lastCast = cursor.getLong(cursor.getColumnIndexOrThrow(SQLite.COLUMN_LAST_CAST_DATE));
                item.setCount = cursor.getInt(cursor.getColumnIndexOrThrow(SQLite.COLUMN_SET_COUNT));
                item.lastViewed = cursor.getLong(cursor.getColumnIndexOrThrow(SQLite.COLUMN_LAST_VIEWED));
                analyticsMap.put(item.uuid, item);
            }
            cursor.close();

            // 2. Build the result list by iterating your already-filtered Song list
            for (Song song : mainActivityInterface.getSongMenuFragment().getSongsFound()) {
                if (song != null && song.getUuid() != null && !song.getUuid().isEmpty()) {
                    AnalyticsItem stats = analyticsMap.get(song.getUuid());
                    if (stats == null) {
                        // Create an empty "0-stat" item if no match found
                        stats = new AnalyticsItem();
                        stats.uuid = song.getUuid();
                    }
                    stats.title = song.getTitle();
                    stats.filename = song.getFilename();
                    stats.folder = song.getFolder();
                    resultList.add(stats);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Sort the list based on the user's preference
        Collections.sort(resultList, (a, b) -> {
            switch (SortMode.lastSortMethod) {
                case SortMode.LAST_VIEWED:
                    return Long.compare(b.lastViewed, a.lastViewed);
                case SortMode.COUNT_SET:
                    return Integer.compare(b.setCount, a.setCount);
                case SortMode.LAST_CAST:
                    return Long.compare(b.lastCast, a.lastCast);
                case SortMode.LAST_SET:
                    return Long.compare(b.lastAddToSet, a.lastAddToSet);
                case SortMode.POPULARITY:
                default:
                    return Integer.compare(b.viewCount, a.viewCount); // Descending
            }
        });
        return resultList;
    }

}