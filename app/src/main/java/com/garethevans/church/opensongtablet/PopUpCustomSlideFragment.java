package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class PopUpCustomSlideFragment extends DialogFragment {

    static PopUpCustomSlideFragment newInstance() {
        PopUpCustomSlideFragment frag;
        frag = new PopUpCustomSlideFragment();
        return frag;
    }

    public interface MyInterface {
        void addSlideToSet();
    }

    private MyInterface mListener;

    @Override
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    // Declare views
    static View V;
    static RadioGroup customRadioGroup;
    static RadioButton noteRadioButton;
    static RadioButton slideRadioButton;
    static TextView slideTitleTextView;
    static TextView slideContentTextView;
    static EditText slideTitleEditText;
    static EditText slideContentEditText;
    static Button customSlideCancel;
    static Button customSlideAdd;

    // Declare variables used
    static String whattype = "note";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getResources().getString(R.string.options_song_edit));
        V = inflater.inflate(R.layout.popup_customslidecreator, container, false);

        // Initialise the basic views
        customRadioGroup = (RadioGroup) V.findViewById(R.id.customRadioGroup);
        noteRadioButton = (RadioButton) V.findViewById(R.id.noteRadioButton);
        slideRadioButton = (RadioButton) V.findViewById(R.id.slideRadioButton);
        slideTitleTextView = (TextView) V.findViewById(R.id.slideTitleTextView);
        slideContentTextView = (TextView) V.findViewById(R.id.slideContentTextView);
        slideTitleEditText = (EditText) V.findViewById(R.id.slideTitleEditText);
        slideContentEditText = (EditText) V.findViewById(R.id.slideContentEditText);
        customSlideCancel = (Button) V.findViewById(R.id.customSlideCancel);
        customSlideAdd = (Button) V.findViewById(R.id.customSlideAdd);

        // By default we want to make a brief note/placeholder
        noteRadioButton.setChecked(true);
        slideRadioButton.setChecked(false);
        switchViewToNote();

        // Set button listeners
        customSlideCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        customSlideAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenActivity.noteorslide = whattype;
                FullscreenActivity.customslide_title = slideTitleEditText.getText().toString();
                FullscreenActivity.customslide_content = slideContentEditText.getText().toString();
                mListener.addSlideToSet();
                dismiss();
            }
        });
        customRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (noteRadioButton.isChecked()) {
                    switchViewToNote();
                } else {
                    switchViewToSlide();
                }
            }
        });
        return V;
    }

    public void switchViewToNote() {
        whattype = "note";
    }

    public void switchViewToSlide() {
        whattype = "slide";
    }

    @Override
    public void onStart() {
        super.onStart();

        // safety check
        if (getDialog() == null) {
            return;
        }
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

}