package com.garethevans.church.opensongtablet.appdata;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsLanguageBinding;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.google.android.material.snackbar.Snackbar;

public class LanguageFragment extends Fragment {

    SettingsLanguageBinding myView;
    Preferences preferences;
    String languageCode;
    String[] languageCodes = new String[] {"af","cs","de","el","en","es","fr","hu","it","ja","pl","pt","ru","sr","sv","zh"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsLanguageBinding.inflate(inflater, container, false);

        // Set the helpers
        setHelpers();

        // Build the radio group
        buildRadioGroup();

        return myView.getRoot();
    }

    private void setHelpers() {
        preferences = new Preferences();
    }

    private void buildRadioGroup() {
        languageCode = preferences.getMyPreferenceString(getContext(),"language","en");
        String[] languages = getContext().getResources().getStringArray(R.array.languagelist);
        int id = -1;
        for (int x=0; x<languages.length; x++) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setText(languages[x].toUpperCase());
            radioButton.setTextColor(StaticVariables.white);
            radioButton.setId(View.generateViewId());
            radioButton.setTag(languageCodes[x]);

            radioButton.setPadding(24,24,24,24);
            if (languageCode.equals(languageCodes[x])) {
                id = radioButton.getId();
            }
            myView.languageGroup.setOnCheckedChangeListener((group, checkedId) -> {
                RadioButton button = myView.languageGroup.findViewById(checkedId);
                String tag = button.getTag().toString();
                preferences.setMyPreferenceString(getContext(),"language",tag);
                Snackbar.make(getActivity().findViewById(R.id.coordinator),"You will need to restart the app to see the changes",Snackbar.LENGTH_LONG).show();
            });
            myView.languageGroup.addView(radioButton);
        }
        try{
            myView.languageGroup.check(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
