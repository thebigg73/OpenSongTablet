package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.radiobutton.MaterialRadioButton;

public class MaterialRadioButtonItem extends LinearLayout {

    private final MaterialRadioButton radioButton;
    private final MyMaterialSimpleTextView textView;
    private final MyMaterialSimpleTextView hintView;
    private final float xxlarge, xlarge, large, medium, small, xsmall;

    public MaterialRadioButtonItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_material_radiobutton,this);
        //ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, context.getTheme());
        //LayoutInflater.from(contextThemeWrapper).inflate(R.layout.view_material_radiobutton,this);
        //inflate(context, R.layout.view_material_radiobutton,this);

        xxlarge = context.getResources().getDimension(R.dimen.text_xxlarge);
        xlarge = context.getResources().getDimension(R.dimen.text_xlarge);
        large = context.getResources().getDimension(R.dimen.text_large);
        medium = context.getResources().getDimension(R.dimen.text_medium);
        small = context.getResources().getDimension(R.dimen.text_small);
        xsmall = context.getResources().getDimension(R.dimen.text_xsmall);

        radioButton = findViewById(R.id.radioButton);
        textView = findViewById(R.id.textView);
        hintView = findViewById(R.id.hintView);

        textView.setId(View.generateViewId());
        hintView.setId(View.generateViewId());
        radioButton.setId(View.generateViewId());

        int[] set = new int[] {android.R.attr.text, android.R.attr.hint, android.R.attr.checked};
        TypedArray typedArray = context.obtainStyledAttributes(attrs,set);

        String mainText = typedArray.getString(0);
        setText(mainText);

        String hintText = typedArray.getString(1);
        setHint(hintText);

        boolean checked = typedArray.getBoolean(2,false);
        setChecked(checked);

        typedArray.recycle();
    }

    public void setText(String text) {
        if (textView!=null) {
            if (text == null || text.isEmpty()) {
                textView.post(() -> {
                    try {
                        textView.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                textView.post(() -> {
                    try {
                        textView.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            textView.post(() -> {
                try {
                    textView.setText(text);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void setHint(String hint) {
        if (hintView!=null) {
            if (hint == null || hint.isEmpty()) {
                hintView.post(() -> {
                    try {
                        hintView.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                hintView.post(() -> {
                    try {
                        hintView.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            hintView.post(() -> {
                try {
                    hintView.setText(hint);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void setChecked(boolean checked) {
        if (radioButton!=null) {
            radioButton.post(() -> {
                try {
                    radioButton.setChecked(checked);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public boolean isChecked() {
        return radioButton.isChecked();
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        radioButton.setOnCheckedChangeListener(onCheckedChangeListener);
    }

    public void setEnabled(boolean enabled) {
        textView.setEnabled(enabled);
        hintView.setEnabled(enabled);
        radioButton.setEnabled(enabled);
    }

}
