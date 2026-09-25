package com.garethevans.church.opensongtablet.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.screensetup.Palette;
import com.garethevans.church.opensongtablet.utilities.ScreenRecorderService;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class StopRecordingPopUp {
    private final Context context;
    private PopupWindow popupWindow;
    private MyExtendedFloatingActionButton btnStop;
    private final Palette palette;
    private final Handler timerHandler;
    private Runnable timerRunnable;
    private long startTime = 0L;

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "StopRecordingPopUp";
    // Variables for drag tracking
    private int originalX = 0;
    private int originalY = 0;
    private float offsetX = 0f;
    private float offsetY = 0f;

    private final MainActivityInterface mainActivityInterface;

    public StopRecordingPopUp(Context context) {
        this.context = context;
        mainActivityInterface = (MainActivityInterface) context;
        palette = new Palette(context);
        palette.secondary = palette.errorColor;
        timerHandler = new Handler(Looper.getMainLooper());
        setupViews();
    }

    @SuppressLint("ClickableViewAccessibility")
    @SuppressWarnings("InflateParams")
    private void setupViews() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rootLayout = inflater.inflate(R.layout.popup_stop_recording, null, false);

        btnStop = rootLayout.findViewById(R.id.btnStopRecording);
        btnStop.setPalette(palette);

        // 🔑 Attach the touch listener directly to btnStop so it captures drags and taps correctly
        btnStop.setOnTouchListener(new View.OnTouchListener() {
            private float startX;
            private float startY;
            private boolean isDragging = false;
            private static final int DRAG_THRESHOLD = 15; // Minimum pixels moved to count as a drag

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getRawX();
                        startY = event.getRawY();
                        offsetX = event.getRawX() - originalX;
                        offsetY = event.getRawY() - originalY;
                        isDragging = false;
                        return true; // Consume event to start tracking movement

                    case MotionEvent.ACTION_MOVE:
                        float distanceX = Math.abs(event.getRawX() - startX);
                        float distanceY = Math.abs(event.getRawY() - startY);

                        if (!isDragging && (distanceX > DRAG_THRESHOLD || distanceY > DRAG_THRESHOLD)) {
                            isDragging = true;
                        }

                        if (isDragging) {
                            originalX = (int) (event.getRawX() - offsetX);
                            originalY = (int) (event.getRawY() - offsetY);

                            if (popupWindow != null && popupWindow.isShowing()) {
                                popupWindow.update(originalX, originalY, -1, -1);
                            }
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!isDragging) {
                            // If the user didn't drag it, trigger the click action below
                            v.performClick();
                        }
                        return true;
                }
                return false;
            }
        });

        btnStop.setOnClickListener(v -> {
            Log.d(TAG, "Stop button clicked. Beginning teardown flow...");

            //File filesDir = context.getFilesDir();
            //File[] existingFiles = filesDir.listFiles((dir, name) -> name.startsWith("OpenSongApp_debug") && name.endsWith(".mp4"));

            File filesDir = context.getFilesDir();
            File[] existingFiles = filesDir.listFiles((dir, name) ->
                    name.startsWith("OpenSongApp_debug") && name.endsWith(".mp4")
            );

            Log.d(TAG, "Listing ALL files in filesDir (Count: " + (existingFiles != null ? existingFiles.length : 0) + ")");
            if (existingFiles != null) {
                for (File f : existingFiles) {
                    Log.d(TAG, "Found file: " + f.getName() + " | Size: " + f.length() + " bytes");
                }
            }

            if (existingFiles != null) {
                Log.d(TAG, "Found " + existingFiles.length + " matching recording files before sorting.");
            } else {
                Log.d(TAG, "No existing recording files found in directory!");
            }

            File latestFile = null;
            if (existingFiles != null && existingFiles.length > 0) {
                java.util.Arrays.sort(existingFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                latestFile = existingFiles[0];
                Log.d(TAG, "Latest file identified: " + latestFile.getAbsolutePath() + " | Size: " + latestFile.length() + " bytes");
            }

            // Stop the recording service
            Intent stopIntent = new Intent(context, ScreenRecorderService.class);
            context.stopService(stopIntent);
            mainActivityInterface.stopScreenRecorder();
            dismiss();

            // 🔑 Add a tiny delay or check file readiness to allow the MediaRecorder buffer to flush fully
            if (latestFile != null && latestFile.exists()) {
                Log.d(TAG, "Preparing share intent for: " + latestFile.getAbsolutePath());
                try {
                    Uri videoUri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            context.getPackageName() + ".fileprovider",
                            latestFile
                    );

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("video/mp4");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    shareIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    Intent chooserIntent = Intent.createChooser(shareIntent, context.getString(R.string.share));
                    chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    // 🔑 Explicitly grant permission to all apps that can handle this intent
                    List<ResolveInfo> resolveInfoList = context.getPackageManager()
                            .queryIntentActivities(chooserIntent, PackageManager.MATCH_DEFAULT_ONLY);

                    for (ResolveInfo resolveInfo : resolveInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        context.grantUriPermission(
                                packageName,
                                videoUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        );
                    }

                    context.startActivity(chooserIntent);
                    Log.d(TAG, "Share intent successfully dispatched with explicit grants.");
                } catch (Exception e) {
                    Log.e(TAG, "FATAL: Failed to share video intent: ", e);
                }
            }
        });

        popupWindow = new PopupWindow(rootLayout,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                false);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(false);
    }

    public void show(View anchorView) {
        if (popupWindow != null && !popupWindow.isShowing() && anchorView != null) {
            // Default initial placement: Top center
            originalX = 0;
            originalY = 100;
            popupWindow.showAtLocation(anchorView, Gravity.TOP | Gravity.CENTER_HORIZONTAL, originalX, originalY);

            startTime = System.currentTimeMillis();
            startTimer();
        }
    }

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millis = System.currentTimeMillis() - startTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

                if (btnStop != null) {
                    btnStop.setText(timeFormatted);
                }

                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    public void dismiss() {
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }

        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    public boolean isShowing() {
        return popupWindow != null && popupWindow.isShowing();
    }
}