package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.screensetup.Palette;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class DialogHeader extends LinearLayout implements View.OnClickListener {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "DialogHeader";
    private final MyMaterialSimpleTextView textView;
    private final MyFloatingActionButton webHelp, closeButtonDialog, minimiseButtonDialog;
    private BottomSheetDialogFragment bottomSheetDialogFragment;

    public DialogHeader(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context,R.layout.view_dialog_header,this);

        closeButtonDialog = findViewById(R.id.closeButtonDialog);
        minimiseButtonDialog = findViewById(R.id.minimiseButtonDialog);
        textView = findViewById(R.id.textView);
        webHelp = findViewById(R.id.webHelp);

        textView.setId(generateViewId());
        webHelp.setId(generateViewId());
        closeButtonDialog.setId(generateViewId());
        minimiseButtonDialog.setId(generateViewId());

        closeButtonDialog.setClickable(true);
        closeButtonDialog.setOnClickListener(this);

        int[] set = new int[]{android.R.attr.text};
        TypedArray a = context.obtainStyledAttributes(attrs, set);

        CharSequence text = a.getText(0);
        if (text!=null) {
            setText(text.toString());
        }

        a.recycle();

        setPalette(new Palette(context));
    }

    public void setText(String titleText) {
        if (textView != null) {
            textView.post(() -> {
                try {
                    textView.setText(titleText);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void setClose(BottomSheetDialogFragment thisFragment) {
        bottomSheetDialogFragment = thisFragment;
    }

    public void setWebHelp(MainActivityInterface mainActivityInterface, String webAddress) {
        // If we pass in a valid web address, we show the web help page
        if (webAddress!=null && !webAddress.isEmpty()) {
            if (webHelp!=null) {
                webHelp.post(() -> {
                    try {
                        webHelp.setVisibility(View.VISIBLE);
                        webHelp.setOnClickListener(v -> mainActivityInterface.openDocument(webAddress));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } else {
            if (webHelp!=null) {
                webHelp.post(() -> {
                    try {
                        webHelp.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (bottomSheetDialogFragment != null) {
            try {
                bottomSheetDialogFragment.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void showMinimiseButton(boolean show) {
        if (minimiseButtonDialog!=null) {
            minimiseButtonDialog.post(() -> {
                try {
                    minimiseButtonDialog.setVisibility(show ? View.VISIBLE : View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public MyFloatingActionButton getCloseButton() {
        return closeButtonDialog;
    }

    public MyFloatingActionButton getMinimiseButton() {
        return minimiseButtonDialog;
    }

    public void setPalette(Palette palette) {
        if (palette!=null) {
            textView.setTextColor(palette.textColor);
            closeButtonDialog.setColorFilter(palette.textColor);
            minimiseButtonDialog.setColorFilter(palette.textColor);
            webHelp.setColorFilter(palette.textColor);
        }
    }
}
