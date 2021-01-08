package com.garethevans.church.opensongtablet.pagebuttons;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.SettingsPagebuttonsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.customviews.PrefTextLinkView;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

// This allows the user to decide on the actions of the 6 customisable page buttons

public class PageButtonFragment extends Fragment {

    private Preferences preferences;
    private MainActivityInterface mainActivityInterface;
    private PageButtons pageButtons;
    private ArrayList<String> buttonActions;
    private ArrayList<String> buttonText;
    private ArrayList<String> shortActionText;
    private ArrayList<String> longActionText;
    private SettingsPagebuttonsBinding myView;
    private int opt1, opt2, opt3, opt4, opt5, opt6;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsPagebuttonsBinding.inflate(inflater,container,false);

        // Set up the page button icons
        setupPageButtons();

        // Set the spinner/autocompletetextview options
        setDropdowns();

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void setupPageButtons() {
        new Thread(() -> getActivity().runOnUiThread(() -> {
            // Use the same method as for the page, but detach the listeners
            pageButtons.setupButton(myView.button1,preferences.getMyPreferenceString(getContext(),"pageButtonCustom1Action",""));
            removeListeners(myView.button1);
            pageButtons.setupButton(myView.button2,preferences.getMyPreferenceString(getContext(),"pageButtonCustom2Action",""));
            removeListeners(myView.button2);
            pageButtons.setupButton(myView.button3,preferences.getMyPreferenceString(getContext(),"pageButtonCustom3Action",""));
            removeListeners(myView.button3);
            pageButtons.setupButton(myView.button4,preferences.getMyPreferenceString(getContext(),"pageButtonCustom4Action",""));
            removeListeners(myView.button4);
            pageButtons.setupButton(myView.button5,preferences.getMyPreferenceString(getContext(),"pageButtonCustom5Action",""));
            removeListeners(myView.button5);
            pageButtons.setupButton(myView.button6,preferences.getMyPreferenceString(getContext(),"pageButtonCustom6Action",""));
            removeListeners(myView.button6);
        })).start();
    }

    private void removeListeners(FloatingActionButton fab) {
        fab.setOnClickListener(null);
        fab.setOnLongClickListener(null);
    }

    private void setDropdowns() {
        new Thread(() -> {
            buttonActions = pageButtons.setupButtonActions();
            buttonText = pageButtons.setUpButtonText();
            shortActionText = pageButtons.shortActionText();
            longActionText = pageButtons.longActionText();

            opt1 = buttonActions.indexOf(preferences.getMyPreferenceString(getContext(),"pageButtonCustom1Action",""));
            opt2 = buttonActions.indexOf(preferences.getMyPreferenceString(getContext(),"pageButtonCustom2Action",""));
            opt3 = buttonActions.indexOf(preferences.getMyPreferenceString(getContext(),"pageButtonCustom3Action",""));
            opt4 = buttonActions.indexOf(preferences.getMyPreferenceString(getContext(),"pageButtonCustom4Action",""));
            opt5 = buttonActions.indexOf(preferences.getMyPreferenceString(getContext(),"pageButtonCustom5Action",""));
            opt6 = buttonActions.indexOf(preferences.getMyPreferenceString(getContext(),"pageButtonCustom6Action",""));

            ExposedDropDownArrayAdapter arrayAdapter = new ExposedDropDownArrayAdapter(getActivity(), R.layout._my_spinner, buttonActions);

            getActivity().runOnUiThread(() -> {

                setTheText(myView.button1ShortPress, myView.button1LongPress,opt1);
                setTheText(myView.button2ShortPress, myView.button2LongPress,opt2);
                setTheText(myView.button3ShortPress, myView.button3LongPress,opt3);
                setTheText(myView.button4ShortPress, myView.button4LongPress,opt4);
                setTheText(myView.button5ShortPress, myView.button5LongPress,opt5);
                setTheText(myView.button6ShortPress, myView.button6LongPress,opt6);

            });
        }).start();

    }

    private void setTheText(PrefTextLinkView shortText, PrefTextLinkView longText, int pos) {
        ((TextView)shortText.findViewById(R.id.subText)).setText(shortActionText.get(pos));
        if (longActionText.get(pos).isEmpty()) {
            longText.setVisibility(View.GONE);
            ((TextView)longText.findViewById(R.id.subText)).setText("");

        } else {
            longText.setVisibility(View.VISIBLE);
            ((TextView)longText.findViewById(R.id.subText)).setText(longActionText.get(pos));
        }
    }
}
