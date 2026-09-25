package com.garethevans.church.opensongtablet.utilities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.File;
import java.io.IOException;

public class ScreenRecorderService extends Service {
    private static final String TAG = "ScreenRecorderService";
    private static final String CHANNEL_ID = "MediaProjectionChannel";
    private static final int NOTIFICATION_ID = 1337;

    public static final String EXTRA_MODE = "extra_mode";
    public static final String MODE_AUDIO_CAPTURE = "mode_audio_capture";
    public static final String MODE_SCREEN_RECORD = "mode_screen_record";

    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private MediaRecorder mediaRecorder;
    private VirtualDisplay virtualDisplay;
    private boolean isRecording = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private static MainActivityInterface mainActivityInterface;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.screen_recorder_started))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34+
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        }
    }

    public static void setContext(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() intent:" + intent + " flags:" + flags + " startId:" + startId);

        if (intent != null) {
            String mode = intent.getStringExtra(EXTRA_MODE);

            if (MODE_AUDIO_CAPTURE.equals(mode)) {
                Log.d(TAG, "ScreenRecorderService running in Audio Capture foreground mode.");
                return START_NOT_STICKY;
            }

            if (MODE_SCREEN_RECORD.equals(mode) && !isRecording) {
                int resultCode = intent.getIntExtra("code", 0);
                Intent data = intent.getParcelableExtra("data");

                Log.d(TAG, "resultCode:" + resultCode + " data:" + data + " projectionManager:" + projectionManager);

                // Ensure projectionManager is initialized (fall back to getting it via system service if needed)
                if (projectionManager == null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                    }
                }

                if (resultCode != 0 && data != null && projectionManager != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mediaProjection = projectionManager.getMediaProjection(resultCode, data);
                        Log.d(TAG, "mediaProjection:" + mediaProjection);

                        if (mediaProjection != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                mediaProjection.registerCallback(new MediaProjection.Callback() {
                                    @Override
                                    public void onStop() {
                                        super.onStop();
                                        stopSelf();
                                    }
                                }, null);
                            }
                            startRecording(); // 🔑 Now safely triggers recording without needing mainActivityInterface!
                        }
                    }
                }
            }
        }
        return START_NOT_STICKY;
    }

    private void startRecording() {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            try {
                initMediaRecorder();

                WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                DisplayMetrics metrics = new DisplayMetrics();
                windowManager.getDefaultDisplay().getMetrics(metrics);

                int width = metrics.widthPixels & ~1;
                int height = metrics.heightPixels & ~1;
                int density = metrics.densityDpi;

                // If 'Single App' was chosen and the app went to the background,
                // createVirtualDisplay may throw an exception or return null.
                virtualDisplay = mediaProjection.createVirtualDisplay(
                        "ScreenRecorder",
                        width,
                        height,
                        density,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                        mediaRecorder.getSurface(),
                        null,
                        null
                );

                if (virtualDisplay == null) {
                    throw new IllegalStateException("VirtualDisplay creation failed. Did you select 'Single App'?");
                }

                mediaRecorder.start();
                isRecording = true;
                Log.d(TAG, "Screen recording successfully started.");
            } catch (Exception e) {
                Log.e(TAG, "FATAL: Failed to start screen recording.", e);

                if (mainActivityInterface != null) {
                    handler.post(() -> mainActivityInterface.stopScreenRecorder());
                }

                stopSelf();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initMediaRecorder() throws IOException {
        mediaRecorder = new MediaRecorder();

        // Save directly to the app's internal files directory so it's guaranteed to persist
        //File outputFile = new File(getFilesDir(), "OpenSongApp_debug_" + System.currentTimeMillis() + ".mp4");

        File outputFile = new File(getFilesDir(), "OpenSongApp_debug_" + System.currentTimeMillis() + ".mp4");
        Log.d(TAG, "Writing recording to absolute path: " + outputFile.getAbsolutePath());

        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(outputFile.getAbsolutePath());

        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);

        int width = metrics.widthPixels & ~1;
        int height = metrics.heightPixels & ~1;

        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setVideoEncodingBitRate(512 * 1000 * 4); // ~2 Mbps
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoSize(width, height);

        mediaRecorder.prepare();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Media Projection Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    public static void clearContext() {
        mainActivityInterface = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isRecording) {
            try {
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping MediaRecorder", e);
            }
            isRecording = false;
        }

        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }

        if (mediaProjection != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mediaProjection.stop();
            }
            mediaProjection = null;
        }

        mainActivityInterface = null;

        Log.d(TAG, "ScreenRecorderService destroyed. Recording finalized.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        // Stops the service and triggers onDestroy() cleanup (releasing MediaRecorder/MediaProjection)
        stopSelf();
    }
}