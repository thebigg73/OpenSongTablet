package com.garethevans.church.opensongtablet;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class PopUpLanguageFragment extends DialogFragment {

    private String tempLanguage;
    private Preferences preferences;

    static PopUpLanguageFragment newInstance() {
        PopUpLanguageFragment frag;
        frag = new PopUpLanguageFragment();
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(Objects.requireNonNull(getActivity()).getResources().getString(R.string.language));
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_language, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.language));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getActivity());
            closeMe.setEnabled(false);
            dismiss();
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(saveMe,getActivity());
            saveMe.setEnabled(false);
            doSave();
        });

        preferences = new Preferences();

        // Initialise the views
        ListView languagescroll = V.findViewById(R.id.languagescroll);

        // Go through the language array and create radio buttons for each
        int positionselected;

        switch (preferences.getMyPreferenceString(getActivity(),"language","en")) {
            case "af":
                positionselected = 0;
                break;
            case "cs":
                positionselected = 1;
                break;
            case "de":
                positionselected = 2;
                break;
            case "el":
                positionselected = 3;
                break;
            default:
            case "en":
                positionselected = 4;
                break;
            case "es":
                positionselected = 5;
                break;
            case "fr":
                positionselected = 6;
                break;
            case "hu":
                positionselected = 7;
                break;
            case "it":
                positionselected = 8;
                break;
            case "ja":
                positionselected = 9;
                break;
            case "pl":
                positionselected = 10;
                break;
            case "pt":
                positionselected = 11;
                break;
            case "ru":
                positionselected = 12;
                break;
            case "sr":
                positionselected = 13;
                break;
            case "sv":
                positionselected = 14;
                break;
            case "zh":
                positionselected = 15;
                break;
        }

        ArrayAdapter<String> la = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_single_choice,
                getActivity().getResources().getStringArray(R.array.languagelist));
        languagescroll.setAdapter(la);

        languagescroll.setItemChecked(positionselected, true);

        languagescroll.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
                case 0:
                    tempLanguage = "af";
                    break;
                case 1:
                    tempLanguage = "cs";
                    break;
                case 2:
                    tempLanguage = "de";
                    break;
                case 3:
                    tempLanguage = "el";
                    break;
                case 4:
                    tempLanguage = "en";
                    break;
                case 5:
                    tempLanguage = "es";
                    break;
                case 6:
                    tempLanguage = "fr";
                    break;
                case 7:
                    tempLanguage = "hu";
                    break;
                case 8:
                    tempLanguage = "it";
                    break;
                case 9:
                    tempLanguage = "ja";
                    break;
                case 10:
                    tempLanguage = "pl";
                    break;
                case 11:
                    tempLanguage = "pt";
                    break;
                case 12:
                    tempLanguage = "ru";
                    break;
                case 13:
                    tempLanguage = "sr";
                    break;
                case 14:
                    tempLanguage = "sv";
                    break;
                case 15:
                    tempLanguage = "zh";
                    break;

            }
        });

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void doSave() {
        preferences.setMyPreferenceString(getActivity(),"language",tempLanguage);
        // Unfortunately this means the MAIN folder name isn't right!
        dismiss();
        Objects.requireNonNull(getActivity()).recreate();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

}