package com.garethevans.church.opensongtablet.appdata;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsLanguageBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class LanguageFragment extends Fragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "LanguageFragment";
    private SettingsLanguageBinding myView;
    private MainActivityInterface mainActivityInterface;
    private final String[] languageCodes = new String[] {"af","cs","de","el","en","es","fr","hu","it","ja","pl","pt","ru","si","sr","sv","uk","zh"};
    private String language="", restart="", restart_required="";
    private String[] languages = new String[]{};

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update the toolbar
        mainActivityInterface.updateToolbar(language);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsLanguageBinding.inflate(inflater, container, false);

        prepareStrings();

        // Build the radio group
        buildRadioGroup();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            language = getString(R.string.language);
            languages = getResources().getStringArray(R.array.languagelist);
            restart = getString(R.string.restart);
            restart_required = getString(R.string.restart_required);
        }
    }
    private void buildRadioGroup() {
        String languageCode = mainActivityInterface.getPreferences().getMyPreferenceString("language", "en");
        int id;
        for (int x=0; x<languages.length; x++) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setText(languages[x].toUpperCase());
            radioButton.setId(View.generateViewId());
            radioButton.setTag(languageCodes[x]);

            radioButton.setPadding(24,24,24,24);
            myView.languageGroup.addView(radioButton);

            if (languageCode.equals(languageCodes[x])) {
                id = radioButton.getId();
                myView.languageGroup.check(id);
            }
        }
        myView.languageGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton button = myView.languageGroup.findViewById(checkedId);
            String tag = button.getTag().toString();
            mainActivityInterface.getPreferences().setMyPreferenceString("language", tag);
            InformationBottomSheet informationBottomSheet = new InformationBottomSheet(restart,
                    restart_required, restart, "restart");
            informationBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "restart");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}
