package com.garethevans.church.opensongtablet.controls;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.appdata.ExposedDropDownSelection;
import com.garethevans.church.opensongtablet.customviews.PrefTextLinkView;
import com.garethevans.church.opensongtablet.databinding.SettingsPagebuttonsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.screensetup.ThemeColors;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

// This allows the user to decide on the actions of the 6 customisable page buttons

public class PageButtonFragment extends Fragment {

    private Preferences preferences;
    private MainActivityInterface mainActivityInterface;
    private ThemeColors themeColors;
    private PageButtons pageButtons;
    private ArrayList<FloatingActionButton> myButtons;
    private ArrayList<LinearLayout> myLayouts;
    private ArrayList<SwitchCompat> mySwitches;
    private ArrayList<TextInputLayout> textInputLayouts;
    private ArrayList<AutoCompleteTextView> autoCompleteTextViews;
    private ArrayList<PrefTextLinkView> shortTexts;
    private ArrayList<PrefTextLinkView> longTexts;
    private SettingsPagebuttonsBinding myView;
    private ExposedDropDownArrayAdapter arrayAdapter;
    private ExposedDropDownSelection exposedDropDownSelection;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsPagebuttonsBinding.inflate(inflater,container,false);

        mainActivityInterface.updateToolbar(null,getString(R.string.page_buttons));

        // Set up helpers
        setupHelpers();

        // Set up the page button icons
        setupPageButtons();

        return myView.getRoot();
    }

    private void setupHelpers() {
        preferences = mainActivityInterface.getPreferences();
        pageButtons = mainActivityInterface.getPageButtons();
        themeColors = mainActivityInterface.getMyThemeColors();
        exposedDropDownSelection = new ExposedDropDownSelection();
    }

    private void setupPageButtons() {
        new Thread(() -> {
            requireActivity().runOnUiThread(() -> {
                // We will programatically draw the page buttons and their options based on our preferences
                // Add the buttons to our array (so we can iterate through)
                addMyButtons();
                addButtonLayouts();
                addVisibleSwitches();
                addTextViews();

                // Now iterate through each button and set it up
                for (int x = 0; x < 6; x++) {
                    pageButtons.setPageButton(requireContext(), myButtons.get(x), themeColors.getPageButtonsColor(), x, true);
                    myButtons.get(x).setVisibility(View.VISIBLE);
                    setVisibilityFromBoolean(myLayouts.get(x), pageButtons.getPageButtonVisibility(x));
                    mySwitches.get(x).setChecked(pageButtons.getPageButtonVisibility(x));
                    String string = getString(R.string.button) + " " + (x + 1) + ": " + getString(R.string.visible);
                    mySwitches.get(x).setText(string);
                    int finalX = x;
                    mySwitches.get(x).setOnCheckedChangeListener((buttonView, isChecked) -> changeVisibilityPreference(finalX, isChecked));
                }
            });
            arrayAdapter = new ExposedDropDownArrayAdapter(requireActivity(), R.layout.exposed_dropdown, pageButtons.getPageButtonAvailableText());
            requireActivity().runOnUiThread(() -> {
                for (int x=0;x<6;x++) {
                    setTheDropDowns(x);
                    setTheText(x);
                }
            });
        }).start();
    }

    private void addMyButtons() {
        myButtons = new ArrayList<>();
        myButtons.add(myView.button1);
        myButtons.add(myView.button2);
        myButtons.add(myView.button3);
        myButtons.add(myView.button4);
        myButtons.add(myView.button5);
        myButtons.add(myView.button6);
    }

    private void addVisibleSwitches() {
        mySwitches = new ArrayList<>();
        mySwitches.add(myView.button1Active);
        mySwitches.add(myView.button2Active);
        mySwitches.add(myView.button3Active);
        mySwitches.add(myView.button4Active);
        mySwitches.add(myView.button5Active);
        mySwitches.add(myView.button6Active);
    }
    private void addButtonLayouts() {
        myLayouts = new ArrayList<>();
        myLayouts.add(myView.button1View);
        myLayouts.add(myView.button2View);
        myLayouts.add(myView.button3View);
        myLayouts.add(myView.button4View);
        myLayouts.add(myView.button5View);
        myLayouts.add(myView.button6View);
    }
    private void addTextViews() {
        autoCompleteTextViews = new ArrayList<>();
        textInputLayouts = new ArrayList<>();
        shortTexts = new ArrayList<>();
        longTexts = new ArrayList<>();
        autoCompleteTextViews.add(myView.button1Opt);
        autoCompleteTextViews.add(myView.button2Opt);
        autoCompleteTextViews.add(myView.button3Opt);
        autoCompleteTextViews.add(myView.button4Opt);
        autoCompleteTextViews.add(myView.button5Opt);
        autoCompleteTextViews.add(myView.button6Opt);

        textInputLayouts.add(myView.button1OptLayout);
        textInputLayouts.add(myView.button2OptLayout);
        textInputLayouts.add(myView.button3OptLayout);
        textInputLayouts.add(myView.button4OptLayout);
        textInputLayouts.add(myView.button5OptLayout);
        textInputLayouts.add(myView.button6OptLayout);

        shortTexts.add(myView.button1ShortPress);
        shortTexts.add(myView.button2ShortPress);
        shortTexts.add(myView.button3ShortPress);
        shortTexts.add(myView.button4ShortPress);
        shortTexts.add(myView.button5ShortPress);
        shortTexts.add(myView.button6ShortPress);

        longTexts.add(myView.button1LongPress);
        longTexts.add(myView.button2LongPress);
        longTexts.add(myView.button3LongPress);
        longTexts.add(myView.button4LongPress);
        longTexts.add(myView.button5LongPress);
        longTexts.add(myView.button6LongPress);
    }

    private void changeVisibilityPreference(int x, boolean visible) {
        preferences.setMyPreferenceBoolean(requireContext(),"pageButtonShow"+(x+1), visible);
        setVisibilityFromBoolean(myLayouts.get(x),visible);
        pageButtons.setPageButtonVisibility(x,visible);
        mainActivityInterface.updatePageButtonLayout();
    }

    private void setVisibilityFromBoolean(View view, boolean visibile) {
        if (visibile) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    private void setTheDropDowns(int pos) {
        autoCompleteTextViews.get(pos).setAdapter(arrayAdapter);
        autoCompleteTextViews.get(pos).setText(pageButtons.getPageButtonText(pos));
        exposedDropDownSelection.keepSelectionPosition(textInputLayouts.get(pos),autoCompleteTextViews.get(pos),pageButtons.getPageButtonAvailableText());
        /*autoCompleteTextViews.get(pos).setOnClickListener(new View.OnClickListener() {
            boolean showing = false;
            @Override
            public void onClick(View v) {
                if (showing) {
                    autoCompleteTextViews.get(pos).dismissDropDown();
                    showing = false;
                } else {
                    showing = true;
                    autoCompleteTextViews.get(pos).showDropDown();
                    autoCompleteTextViews.get(pos).setListSelection(pageButtons.getPositionFromText(pageButtons.getPageButtonText(pos)));
                }
            }
        });*/
        autoCompleteTextViews.get(pos).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Get the new values etc. using the position in the list
                saveDropDownChoice(pos, s.toString());
            }
        });
    }
    private void setTheText(int pos) {
        ((TextView)shortTexts.get(pos).findViewById(R.id.subText)).setText(pageButtons.getPageButtonShortText(pos));
        ((TextView)longTexts.get(pos).findViewById(R.id.subText)).setText(pageButtons.getPageButtonLongText(pos));
        if (pageButtons.getPageButtonShortText(pos).isEmpty()) {
            shortTexts.get(pos).setVisibility(View.GONE);
        } else {
            shortTexts.get(pos).setVisibility(View.VISIBLE);
        }
        if (pageButtons.getPageButtonLongText(pos).isEmpty()) {
            longTexts.get(pos).setVisibility(View.GONE);
        } else {
            longTexts.get(pos).setVisibility(View.VISIBLE);
        }
    }

    private void saveDropDownChoice(int x, String text) {
        // x tells us the button we are dealing with and action is, well, the action
        int foundpos = pageButtons.getPositionFromText(text);
        Log.d("saveDropDownChoice","x:"+x+"  text="+text+"  foundpos="+foundpos);
        pageButtons.setPageButtonAction(x,foundpos);
        pageButtons.setPageButtonText(x,foundpos);
        pageButtons.setPageButtonShortText(x,foundpos);
        pageButtons.setPageButtonLongText(x,foundpos);
        pageButtons.setPageButtonDrawable(requireContext(),x,foundpos);
        pageButtons.setPageButton(requireContext(),myButtons.get(x),themeColors.getPageButtonsColor(),x, true);
        setTheText(x);
        preferences.setMyPreferenceString(requireContext(),"pageButton"+(x+1),pageButtons.getPageButtonAction(x));
        mainActivityInterface.updatePageButtonLayout();
    }
}
