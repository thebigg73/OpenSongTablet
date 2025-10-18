package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;

public class MyCheckbox extends LinearLayout {

    private CheckBox checkBox;
    private MyMaterialSimpleTextView textView;
    private MyMaterialSimpleTextView extraText;

    public MyCheckbox(Context context) {
        super(context);
        setup(context);
    }

    public MyCheckbox(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    private void setup(Context c) {
        inflate(c, R.layout.view_checkbox_list_item, this);
        checkBox = findViewById(R.id.checkBoxItem);
        textView = findViewById(R.id.itemName);
        extraText = findViewById(R.id.modifiedDate);
    }

    public void setChecked(boolean checked) {
        checkBox.setChecked(checked);
    }
    public boolean getChecked() {
        return checkBox.isChecked();
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public void setExtraText(String text) {
        extraText.setText(text);
    }
}
