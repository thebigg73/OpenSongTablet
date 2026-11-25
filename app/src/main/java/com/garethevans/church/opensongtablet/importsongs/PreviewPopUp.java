package com.garethevans.church.opensongtablet.importsongs;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.Layout;
import android.util.Log;
import android.util.SparseLongArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.DialogHeader;
import com.garethevans.church.opensongtablet.customviews.FloatWindow;
import com.garethevans.church.opensongtablet.customviews.MyExtendedFloatingActionButton;
import com.garethevans.church.opensongtablet.customviews.MyFloatingActionButton;
import com.garethevans.church.opensongtablet.customviews.MyMaterialButton;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.vision.text.Text;

import java.util.ArrayList;

public class PreviewPopUp {

    private PopupWindow popupWindow;
    private MyFloatingActionButton closeButton;
    private FloatWindow floatWindow;
    private int posX;
    private int posY;
    private int w,h;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "PreviewPopUp";
    private final ArrayList<View> songViews;
    private final ImportOnlineFragment onlineFragment;

    private final Context c;
    private final MainActivityInterface mainActivityInterface;

    public PreviewPopUp(Context c, ArrayList<View> songViews, ImportOnlineFragment onlineFragment) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        this.songViews = songViews;
        this.onlineFragment = onlineFragment;
    }

    private boolean doingShowPopUp = false;
    public void floatPreview(View viewHolder) {
        // Make sure there is a delay to avoid double action
        if (!doingShowPopUp) {
            doingShowPopUp = true;
            // If the popup is showing already, dismiss it
            if (popupWindow != null && popupWindow.isShowing()) {
                try {
                    popupWindow.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                Log.d(TAG, "Try to show the preview");
                // Set up the views
                getPositionAndSize();
                setupViews();
                setListeners();

                Log.d(TAG, "About to show popup");
                long showStart = System.currentTimeMillis();

                // Post it to the next message queue turn
                viewHolder.post(() -> {
                    popupWindow.showAtLocation(viewHolder, Gravity.TOP | Gravity.START, posX, posY);
                    setPopupDim(true, 0.5f);
                    Log.d(TAG, "Popup shown in: " + (System.currentTimeMillis() - showStart) + "ms");
                });
            }
            mainActivityInterface.getMainHandler().postDelayed(() -> doingShowPopUp = false, 500);
        }
    }

    private void setupViews() {
        // The popup
        popupWindow = new PopupWindow(c);
        popupWindow.setOnDismissListener(() -> setPopupDim( false, 0f));
        getPositionAndSize();
        popupWindow.setWidth(w);
        popupWindow.setHeight(h);

        // The main layout
        floatWindow = new FloatWindow(c);
        floatWindow.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        View view = View.inflate(c,R.layout.view_import_song_preview,null);
        FrameLayout frameLayout = view.findViewById(R.id.relativeLayout);
        frameLayout.setClipChildren(false);
        frameLayout.setClipToPadding(false);

        Drawable rawDrawable = ResourcesCompat.getDrawable(c.getResources(), R.drawable.popup_bg, null);
        if (rawDrawable != null) {
            Drawable mutated = rawDrawable.mutate();
            if (mutated instanceof GradientDrawable) {
                ((GradientDrawable) mutated).setColor(0xFFFFFFFF);
            }
            frameLayout.setBackground(mutated);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                frameLayout.setClipToOutline(true);
            }
        }
        popupWindow.setBackgroundDrawable(null);

        DialogHeader dialogHeader = view.findViewById(R.id.dialogHeader);
        closeButton = dialogHeader.getCloseButton();
        dialogHeader.getCloseButton().setOnClickListener(view1 -> destroyPopup());



        //CoordinatorLayout.LayoutParams layoutParams = new CoordinatorLayout.LayoutParams(w + 16, h + 16);
        //floatWindow.setLayoutParams(layoutParams);
        //LinearLayout pageLinearLayout = new LinearLayout(c);
        //pageLinearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        //pageLinearLayout.setOrientation(LinearLayout.VERTICAL);

        //LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(w + 16, h + 16);
        //floatWindow.setLayoutParams(layoutParams);
        //floatWindow.setOrientation(LinearLayout.VERTICAL);

        //floatWindow.setPadding(16, 16, 16, 16);

        // Add close button
        //closeButton = new MyFloatingActionButton(c);
        //LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
        //        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //buttonParams.gravity = Gravity.END;
        //closeButton.setLayoutParams(buttonParams);
        //closeButton.setSize(FloatingActionButton.SIZE_MINI);

        //Drawable closeIcon = ContextCompat.getDrawable(c, R.drawable.close);
        //if (closeIcon != null) {
        //    closeIcon = DrawableCompat.wrap(closeIcon).mutate();
        //    closeIcon.setColorFilter(0xFF000000, PorterDuff.Mode.SRC_IN);
        //}
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //    closeButton.makeFlat();
        //}
        //closeButton.setImageDrawable(closeIcon);
        //closeButton.setImageTintList(null);
        //closeButton.setBackgroundColor(Color.TRANSPARENT);
        //closeButton.setBackgroundTintList(new ColorStateList(
        //        new int[][]{new int[0]},
        //        new int[]{Color.TRANSPARENT}
        //));
        //pageLinearLayout.addView(closeButton);
        //floatWindow.addView(closeButton);

        // Scroll containers
        //HorizontalScrollView horizontalScrollView = new HorizontalScrollView(c);
        //horizontalScrollView.setLayoutParams(new LinearLayout.LayoutParams(w, h));
        //horizontalScrollView.setBackgroundColor(0xFFFFFF);

        //ScrollView scrollView = new ScrollView(c);
        //scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        //LinearLayout linearLayout = new LinearLayout(c);
        //linearLayout.setLayoutParams(new ViewGroup.LayoutParams(
        //        ViewGroup.LayoutParams.WRAP_CONTENT, 0));
        //linearLayout.setWeightSum(1);
        //linearLayout.setOrientation(LinearLayout.VERTICAL);
        //linearLayout.setClipChildren(false);
        //linearLayout.setClipToPadding(false);
        //linearLayout.setBackgroundColor(0xFFFFFF);

        LinearLayout songLayout = view.findViewById(R.id.songContent);
        //long start = System.currentTimeMillis();
        for (int x = 0; x < songViews.size(); x++) {
            songLayout.addView(songViews.get(x));
        }

        MyMaterialButton continueButton = view.findViewById(R.id.continueButton);
        continueButton.setOnClickListener((view2) -> {
            if (onlineFragment!=null) {
                destroyPopup();
                onlineFragment.setupSaveLayout();
            }
        });



        /*scrollView.addView(linearLayout);
        horizontalScrollView.addView(scrollView);
        //floatWindow.addView(horizontalScrollView);
        pageLinearLayout.addView(horizontalScrollView);
        //floatWindow.addView(pageLinearLayout);

        // Add 'Continue' button
        MyMaterialButton continueButton = new MyMaterialButton(c);
        continueButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        continueButton.setText(c.getString(R.string.continue_text));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            continueButton.setElevation(128);
        }
        continueButton.setGravity(Gravity.END | Gravity.BOTTOM);
        //floatWindow.addView(continueButton);
        continueButton.setOnClickListener((view) -> {
            if (onlineFragment!=null) {
                destroyPopup();
                onlineFragment.setupSaveLayout();
            }
        });
*/

        floatWindow.addView(view);
        //floatWindow.addView(pageLinearLayout);
        //floatWindow.addView(continueButton);

        popupWindow.setContentView(floatWindow);
    }

    private void setListeners() {
        closeButton.setOnClickListener(v -> popupWindow.dismiss());
    }

    private void getPositionAndSize() {
        int screenWidth = c.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = c.getResources().getDisplayMetrics().heightPixels;
        w = Math.round((float)c.getResources().getDisplayMetrics().widthPixels*0.85f) - 16;
        h = Math.round((float)c.getResources().getDisplayMetrics().heightPixels*0.85f) - 16;
        posX = (screenWidth - w) / 2;
        posY = (screenHeight - h) / 2;

        //w = screenWidth;
        //h = screenHeight;
        //posX = 0;
        //posY = 0;
    }


    private void setPopupDim(boolean dim, float dimAmount) {
        if (popupWindow == null || popupWindow.getContentView() == null) return;

        View container = popupWindow.getContentView().getRootView();
        Context context = popupWindow.getContentView().getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null || !(container.getLayoutParams() instanceof WindowManager.LayoutParams)) return;

        WindowManager.LayoutParams params = (WindowManager.LayoutParams) container.getLayoutParams();
        if (dim) {
            params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            params.dimAmount = dimAmount; // e.g., 0.5f
        } else {
            params.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            params.dimAmount = 0f;
        }
        wm.updateViewLayout(container, params);
    }

    public void destroyPopup() {
        if (floatWindow != null && popupWindow != null) {
            floatWindow.post(() -> {
                try {
                    if (popupWindow != null) {
                        popupWindow.dismiss();
                        popupWindow = null;
                    }
                    if (closeButton != null) {
                        closeButton = null;
                    }
                    if (floatWindow != null) {
                        floatWindow = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
