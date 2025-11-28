package com.garethevans.church.opensongtablet.screensetup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyMaterialSimpleTextView;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.card.MaterialCardView;

public class ShowToast {

    // New method uses a hiddent textview that floats over the main layout
    private final MyMaterialSimpleTextView toastBox;

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String TAG = "ShowToast";
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private Handler handlerShow;
    private Handler handlerHide;
    private final long showTime = 2500;
    private String currentMessage = "";
    private final Runnable runnableHide;
    private final Runnable runnableShow;
    private final String success, error;

    public ShowToast(Context c, MyMaterialSimpleTextView toastBox) {
        this.toastBox = toastBox;
        this.c = c;
        success = c.getString(R.string.success);
        error = c.getString(R.string.error);
        mainActivityInterface = (MainActivityInterface) c;
        handlerShow = mainActivityInterface.getMainHandler();
        handlerHide = mainActivityInterface.getMainHandler();
        runnableHide = () -> {
            currentMessage = "";
            if (toastBox != null) {
                toastBox.post(() -> {
                    try {
                        toastBox.setText("");
                        toastBox.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        };
        runnableShow = () -> {
            if (toastBox!=null) {
                toastBox.post(() -> {
                    try {
                        Drawable drawable = ContextCompat.getDrawable(c, R.drawable.rectangle);
                        if (drawable!=null) {
                            DrawableCompat.setTint(drawable,mainActivityInterface.getPalette().secondary);
                        }
                        toastBox.setBackground(drawable);
                        toastBox.setText(currentMessage);
                        int padding = Math.round(c.getResources().getDimension(R.dimen.box_padding));
                        toastBox.setPadding(padding,padding,padding,padding);
                        toastBox.setTextColor(mainActivityInterface.getPalette().textColor);
                        toastBox.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        };
    }

    public void doIt(final String message) {
        // Only proceed if the message is valid and isn't currently shown
        if (message != null && !message.isEmpty() && !message.equals(currentMessage)) {
            currentMessage = message;

            if (handlerShow==null) {
                handlerShow = mainActivityInterface.getMainHandler();
            }
            if (handlerHide==null) {
                handlerHide = mainActivityInterface.getMainHandler();
            }

            // Remove any pending runnables to show/hide
            handlerShow.removeCallbacks(runnableShow);
            handlerHide.removeCallbacks(runnableHide);

            // Show the new message
            handlerShow.post(runnableShow);
            handlerHide.postDelayed(runnableHide, showTime);
        }
    }
    public void doItBottomSheet(final String message, View bsAnchor) {
        // Because the BottomSheet fragment sits above all views, the normal toastBox gets coveree
        // We need to use a popup window here
        PopupWindow popupWindow = new PopupWindow(c);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(32);
        }
        LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.view_toast, null, false);
        popupWindow.setContentView(view);
        popupWindow.setFocusable(false);
        //noinspection deprecation
        popupWindow.setBackgroundDrawable(new BitmapDrawable()); // Necessary for outside touch to work
        popupWindow.setOutsideTouchable(true);
        MyMaterialSimpleTextView textToast = view.findViewById(R.id.textToast);
        textToast.setTextColor(mainActivityInterface.getPalette().textColor);
        MaterialCardView cardView = view.findViewById(R.id.toastCardView);
        cardView.setBackgroundTintList(ColorStateList.valueOf(mainActivityInterface.getPalette().secondary));
        textToast.setOnClickListener(tv -> popupWindow.dismiss());
        popupWindow.getContentView().getRootView().setOnClickListener(v -> popupWindow.dismiss());

        Handler popupHandlerShow = mainActivityInterface.getMainHandler();
        Handler popupHandlerHide = mainActivityInterface.getMainHandler();

        // If there is a normal toastBox, hide it for now (any runnable will properly remove)
        if (toastBox!=null) {
            toastBox.post(() -> {
                toastBox.setText("");
                toastBox.setVisibility(View.GONE);
            });
        }

        // Show the popup toast message above the bottom sheet
        popupHandlerShow.post(() -> {
            textToast.setText(message);
            popupWindow.showAtLocation(bsAnchor, Gravity.CENTER, 0, 0);
        });
        popupHandlerHide.postDelayed(popupWindow::dismiss,showTime);
    }

    public void success() {
        doIt(success);
    }
    public void error() {
        doIt(error);
    }

    public void kill() {
        currentMessage = "";
        if (toastBox!=null) {
            toastBox.post(() -> {
                toastBox.setText("");
                toastBox.setVisibility(View.GONE);
            });
        }
    }

}
