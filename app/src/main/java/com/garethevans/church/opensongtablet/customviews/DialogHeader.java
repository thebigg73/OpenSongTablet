package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DialogHeader extends LinearLayout implements View.OnClickListener {

    private final TextView textView;
    private final FloatingActionButton webHelp;
    private BottomSheetDialogFragment bottomSheetDialogFragment;

    public DialogHeader(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_dialog_header, this);

        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton);
        textView = findViewById(R.id.textView);
        webHelp = findViewById(R.id.webHelp);

        textView.setId(generateViewId());
        webHelp.setId(generateViewId());
        floatingActionButton.setId(generateViewId());

        floatingActionButton.setClickable(true);
        floatingActionButton.setOnClickListener(this);

        int[] set = new int[]{android.R.attr.text};
        TypedArray a = context.obtainStyledAttributes(attrs, set);

        CharSequence text = a.getText(0);
        if (text!=null) {
            textView.setText(text);
        }

        a.recycle();
    }

    public void setText(String titleText) {
        textView.setText(titleText);
    }

    public void setClose(BottomSheetDialogFragment thisFragment) {
        bottomSheetDialogFragment = thisFragment;
    }

    public void setWebHelp(MainActivityInterface mainActivityInterface, String webAddress) {
        // If we pass in a valid web address, we show the web help page
        if (webAddress!=null && !webAddress.isEmpty()) {
            webHelp.setVisibility(View.VISIBLE);
            webHelp.setOnClickListener(v->mainActivityInterface.openDocument(webAddress));
        } else {
            webHelp.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (bottomSheetDialogFragment != null) {
            bottomSheetDialogFragment.dismiss();
        }

    }
}
