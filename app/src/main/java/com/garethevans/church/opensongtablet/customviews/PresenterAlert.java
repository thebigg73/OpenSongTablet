package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.textview.MaterialTextView;

public class PresenterAlert extends MaterialTextView {

    private int viewHeight = 0;

    public PresenterAlert(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        setPadding(0,0, 0,0);
    }

    public void updateAlertSettings(MainActivityInterface mainActivityInterface) {
        setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());
        setTextSize(mainActivityInterface.getPresenterSettings().getPresoAlertTextSize());
        setTextColor(mainActivityInterface.getMyThemeColors().getPresoAlertColor());
        setGravity(mainActivityInterface.getPresenterSettings().getPresoInfoAlign());
        setBackgroundColor(ColorUtils.setAlphaComponent(
                mainActivityInterface.getMyThemeColors().getPresoShadowColor(),
                (int)(mainActivityInterface.getPresenterSettings().getPresoInfoBarAlpha()*255)));
    }

    public void showAlert(MainActivityInterface mainActivityInterface) {
        String text = mainActivityInterface.getPresenterSettings().getPresoAlertText();
        boolean show = mainActivityInterface.getPresenterSettings().getAlertOn();

        setText(text);
        if (text==null || text.isEmpty()) {
            show = false;
        }

        updateAlertSettings(mainActivityInterface);

        if ((show && (getVisibility()==View.INVISIBLE||getVisibility()==View.GONE||getAlpha()==0)) ||
                (!show && (getVisibility()==View.VISIBLE)||getAlpha()==1)) {
            float start;
            float end;
            if (show) {
                start = 0f;
                end = 1f;
            } else {
                start = 1f;
                end = 0f;
            }
            mainActivityInterface.getCustomAnimation().faderAnimation(this,
                    mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),start,end);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewHeight = h;
    }

    public int getViewHeight() {
        if (viewHeight==0) {
            viewHeight = getMeasuredHeight();
        }
        return viewHeight;
    }
}
