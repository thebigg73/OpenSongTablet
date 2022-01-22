package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textview.MaterialTextView;

public class MaterialSwitch extends LinearLayout {

    private final MaterialTextView textView, hintView;
    private final SwitchMaterial switchMaterial;

    public MaterialSwitch(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_material_switch, this);

        textView = findViewById(R.id.textView);
        hintView = findViewById(R.id.hintView);
        switchMaterial = findViewById(R.id.materialSwitch);

        textView.setId(View.generateViewId());
        hintView.setId(View.generateViewId());
        switchMaterial.setId(View.generateViewId());

        int[] set = new int[] {android.R.attr.text, android.R.attr.hint, android.R.attr.checked, R.attr.smallText};
        TypedArray typedArray = context.obtainStyledAttributes(attrs,set);

        String mainText = typedArray.getString(0);
        setText(mainText);

        String hintText = typedArray.getString(1);
        setHint(hintText);

        boolean isSwitched = typedArray.getBoolean(2,false);
        setChecked(isSwitched);

        boolean smallText = typedArray.getBoolean(3, false);
        setSmallText(smallText);

        typedArray.recycle();


    }

    public void setText(String text) {
        textView.setText(text);
        if (text==null || text.isEmpty()) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
        }
    }

    public void setHint(String hint) {
        hintView.setText(hint);
        if (hint==null || hint.isEmpty()) {
            hintView.setVisibility(View.GONE);
        } else {
            hintView.setVisibility(View.VISIBLE);
        }
    }

    public void setChecked(boolean switchedOn) {
        switchMaterial.setChecked(switchedOn);
    }

    public boolean getChecked() {
        return switchMaterial.isChecked();
    }

    public SwitchMaterial getSwitch() {
        return switchMaterial;
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        switchMaterial.setOnCheckedChangeListener(onCheckedChangeListener);
    }

    public void setEnabled(boolean enabled) {
        textView.setEnabled(enabled);
        hintView.setEnabled(enabled);
        switchMaterial.setEnabled(enabled);
        float alpha;
        if (enabled) {
            alpha = 1.0f;
        } else {
            alpha = 0.5f;
        }
        textView.setAlpha(alpha);
        hintView.setAlpha(alpha);
        switchMaterial.setAlpha(alpha);
    }

    public void setSmallText(boolean smallText) {
        if (smallText) {
            textView.setTextSize(14f);
            hintView.setTextSize(12f);
        }
    }
}
