package com.garethevans.church.opensongtablet.chorddetector;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.FloatWindow;
import com.garethevans.church.opensongtablet.customviews.MyFloatingActionButton;
import com.garethevans.church.opensongtablet.customviews.MyMaterialSimpleTextView;
import com.garethevans.church.opensongtablet.customviews.MyMaterialSwitch;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.AreYouSureBottomSheet;
import com.garethevans.church.opensongtablet.screensetup.Palette;

public class ChordDetectionPopUp {

    private PopupWindow popupWindow;
    private MyFloatingActionButton closeButton;
    private FloatWindow floatWindow;

    private final Palette tweakedPalette;

    // 🔑 Declare these at class-level so any method can access them
    private MyMaterialSimpleTextView detectedKey;
    private MyMaterialSwitch switchAudioSource; // 🔑 Audio source toggle switch
    private int posX;
    private int posY;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "ChordDetectionPopUp";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Context c;
    private final MainActivityInterface mainActivityInterface;

    private PolyphonicChordDetector chordDetector;
    private MediaProjection activeMediaProjection;

    private final String screen_recorder_disclosure_string, capture_device_audio_full_info;

    public ChordDetectionPopUp(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        tweakedPalette = new Palette(c);
        tweakedPalette.secondaryFixed = mainActivityInterface.getPalette().textColor;
        tweakedPalette.secondary = mainActivityInterface.getPalette().hintColor;
        tweakedPalette.primaryVariant = mainActivityInterface.getPalette().hintColor;
        screen_recorder_disclosure_string = c.getString(R.string.screen_recorder_disclosure);
        capture_device_audio_full_info = c.getString(R.string.capture_device_audio_full_info);
    }

    private boolean doingShowChordDetection = false;
    @SuppressLint("ClickableViewAccessibility")
    public void floatChordDetection(View viewHolder) {
        if (!doingShowChordDetection) {
            doingShowChordDetection = true;
            if (popupWindow != null && popupWindow.isShowing()) {
                try {
                    mainActivityInterface.toggleChordDetection();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG, "Try to show the ChordDetectionPopUp");
                getPositionAndSize();
                setupViews();
                setListeners();

                startChordListening(false);

                popupWindow.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
                popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);

                // 🔑 1. Configure touch properties so it doesn't dismiss on outside touch
                popupWindow.setFocusable(false);
                popupWindow.setOutsideTouchable(false);

                // 🔑 2. Provide an explicit transparent background drawable
                // This is mandatory in Android for a non-focusable PopupWindow to intercept touch bounds correctly
                popupWindow.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

                // 🔑 3. Intercept and block outside touch events from propagating or triggering dismiss
                popupWindow.setTouchInterceptor((view, motionEvent) -> {
                    return motionEvent.getAction() == MotionEvent.ACTION_OUTSIDE; // Consume outside touches so the popup stays open
                });

                popupWindow.showAtLocation(viewHolder, Gravity.TOP | Gravity.START, posX, posY);

                setupDrag();
            }
            mainActivityInterface.getMainHandler().postDelayed(() -> doingShowChordDetection = false, 500);
        }
    }

    @SuppressLint("InflateParams")
    private void setupViews() {
        popupWindow = new PopupWindow(c);

        // 🔑 Pass viewHolder (or an activity root view) as the parent, with attachToRoot = false.
        // This resolves all layout parameters on the root element correctly and silences the warning!
        LayoutInflater inflater = LayoutInflater.from(c);
        floatWindow = (FloatWindow) inflater.inflate(R.layout.popup_chord_detection, null, false);

        popupWindow.setClippingEnabled(true);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(null);

        // Apply Theme Background Color to the custom FloatWindow background drawable
        GradientDrawable drawable = (GradientDrawable) ResourcesCompat.getDrawable(c.getResources(),
                R.drawable.popup_bg, null);
        if (drawable != null) {
            drawable.setColor(mainActivityInterface.getMyThemeColors().getColorOnly(
                    mainActivityInterface.getPalette().secondary));
        }
        floatWindow.setBackground(drawable);

        // Bind Views from XML
        closeButton = floatWindow.findViewById(R.id.closeButton);
        switchAudioSource = floatWindow.findViewById(R.id.switchAudioSource);
        detectedKey = floatWindow.findViewById(R.id.detectedKey);

        // Check if device supports Android 10+ internal audio capture helper in AppPermissions
        switchAudioSource.setVisibility(mainActivityInterface.getAppPermissions().canCaptureDeviceAudio() ? View.VISIBLE:View.GONE);

            // Configure Switch
        switchAudioSource.setChecked(false);
        switchAudioSource.setPalette(tweakedPalette);
        // Note: ensure text/hint colors on MyMaterialSwitch match your palette if needed programmatically

        // Configure Detected Chord Key View
        detectedKey.setTextColor(mainActivityInterface.getPalette().textColor);
        detectedKey.setTypeface(mainActivityInterface.getMyFonts().getLyricFont());

        // Finalize Popup Window Content and Dimensions
        popupWindow.setContentView(floatWindow);
        popupWindow.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
    }
    private void startChordListening(boolean useDeviceAudio) {
        if (!useDeviceAudio && !mainActivityInterface.getAppPermissions().hasAudioPermissions()) {
            return;
        }

        if (chordDetector != null) {
            chordDetector.stopListening();
        }

        chordDetector = new PolyphonicChordDetector();
        chordDetector.setOnChordDetectedListener(detectedChord -> handler.post(() -> {
            if (detectedKey != null) {
                detectedKey.setText(detectedChord);
            }
        }));

        if (useDeviceAudio && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && activeMediaProjection != null) {
            chordDetector.startListening(activeMediaProjection);
        } else {
            chordDetector.startListening();
        }
    }

    private void setListeners() {
        closeButton.setOnClickListener(v -> mainActivityInterface.toggleChordDetection());

        // 🔑 Toggle switch listener for changing audio stream source
        switchAudioSource.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        // Show the popup confirmation box
                        mainActivityInterface.setWhattodo("chordDetectionStream");
                        AreYouSureBottomSheet areYouSureBottomSheet = new AreYouSureBottomSheet("chordDetectionStream", screen_recorder_disclosure_string + "\n\n" + capture_device_audio_full_info, null, null, null, null);
                        areYouSureBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "AreYouSure");
                    } else {
                        // Revert to Microphone
                        mainActivityInterface.stopDeviceAudioCapture();
                        activeMediaProjection = null;
                        startChordListening(false);
                    }
                });
    }

    public void allowStream(boolean allowStream) {
        if (allowStream && mainActivityInterface.getAppPermissions().canCaptureDeviceAudio()) {
            // Request screen capture permission via MainActivity for AudioPlaybackCapture
            mainActivityInterface.requestDeviceAudioCapture(projection -> {
                if (projection != null) {
                    activeMediaProjection = projection;
                    startChordListening(true);
                } else {
                    // Permission denied by user
                    switchAudioSource.setChecked(false);
                    mainActivityInterface.getShowToast().doIt(c.getString(R.string.permissions_refused));
                }
            });
        } else {
            switchAudioSource.setChecked(false);
        }
        mainActivityInterface.setWhattodo("");
    }
    private void getPositionAndSize() {
        posX = 50;
        posY = 200;
        int w = c.getResources().getDisplayMetrics().widthPixels;
        int h = c.getResources().getDisplayMetrics().heightPixels;

        if (posX < 0 || posX > w) {
            posX = 50;
        }
        if (posY < 0 || posY > h) {
            posY = 200;
        }
    }

    private void setupDrag() {
        floatWindow.setOnTouchListener(new View.OnTouchListener() {
            private float initialTouchX, initialTouchY;
            private int initialX, initialY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        initialX = posX;
                        initialY = posY;
                        v.performClick();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) (event.getRawX() - initialTouchX);
                        int dy = (int) (event.getRawY() - initialTouchY);
                        posX = initialX + dx;
                        posY = initialY + dy;

                        if (popupWindow != null && popupWindow.isShowing()) {
                            popupWindow.update(posX, posY, -1, -1);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        return true;
                }
                return false;
            }
        });
    }

    public void closePopup() {
        if (chordDetector != null) {
            chordDetector.stopListening();
            chordDetector = null;
        }

        mainActivityInterface.stopDeviceAudioCapture();
        activeMediaProjection = null;

        if (floatWindow != null && popupWindow != null) {
            floatWindow.post(() -> {
                if (popupWindow != null) {
                    try {
                        popupWindow.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}