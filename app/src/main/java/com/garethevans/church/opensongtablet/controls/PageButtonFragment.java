package com.garethevans.church.opensongtablet.controls;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDown;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.customviews.MaterialTextView;
import com.garethevans.church.opensongtablet.databinding.SettingsPagebuttonsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;

// This allows the user to decide on the actions of the 6 customisable page buttons

public class PageButtonFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private ArrayList<FloatingActionButton> myButtons;
    private ArrayList<LinearLayout> myLayouts;
    private ArrayList<SwitchCompat> mySwitches;
    private ArrayList<ExposedDropDown> exposedDropDowns;
    private ArrayList<MaterialTextView> shortTexts;
    private ArrayList<MaterialTextView> longTexts;
    private SettingsPagebuttonsBinding myView;
    private ExposedDropDownArrayAdapter arrayAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsPagebuttonsBinding.inflate(inflater,container,false);

        mainActivityInterface.updateToolbar(getString(R.string.page_buttons));

        // Set up the page button icons
        setupPageButtons();

        return myView.getRoot();
    }

    private void setupPageButtons() {
        new Thread(() -> {
            requireActivity().runOnUiThread(() -> {
                int opacity = (int)(mainActivityInterface.getMyThemeColors().getPageButtonsSplitAlpha()*100);
                if (opacity<myView.opacity.getValueFrom()) {
                    opacity = (int)myView.opacity.getValueFrom();
                }
                myView.opacity.setLabelFormatter(value -> ((int)value)+"%");
                myView.opacity.setValue(opacity);
                myView.opacity.setHint(opacity+"%");
                myView.opacity.addOnChangeListener((slider, value, fromUser) -> myView.opacity.setHint((int)value+"%"));
                myView.opacity.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
                    @Override
                    public void onStartTrackingTouch(@NonNull Slider slider) { }

                    @Override
                    public void onStopTrackingTouch(@NonNull Slider slider) {
                        float value = myView.opacity.getValue() /100f;
                        int newColor = mainActivityInterface.getMyThemeColors().changePageButtonAlpha(value);
                        mainActivityInterface.getPreferences().setMyPreferenceInt(
                                mainActivityInterface.getMyThemeColors().getThemeName()+"_pageButtonsColor",
                                newColor);
                    }
                });
                // We will programatically draw the page buttons and their options based on our preferences
                // Add the buttons to our array (so we can iterate through)
                addMyButtons();
                addButtonLayouts();
                addVisibleSwitches();
                addTextViews();

                // Now iterate through each button and set it up
                for (int x = 0; x < 6; x++) {
                    mainActivityInterface.getPageButtons().setPageButton(requireContext(), myButtons.get(x), x, true);
                    myButtons.get(x).setVisibility(View.VISIBLE);
                    setVisibilityFromBoolean(myLayouts.get(x), mainActivityInterface.getPageButtons().getPageButtonVisibility(x));
                    mySwitches.get(x).setChecked(mainActivityInterface.getPageButtons().getPageButtonVisibility(x));
                    String string = getString(R.string.button) + " " + (x + 1) + ": " + getString(R.string.visible);
                    mySwitches.get(x).setText(string);
                    int finalX = x;
                    mySwitches.get(x).setOnCheckedChangeListener((buttonView, isChecked) -> changeVisibilityPreference(finalX, isChecked));
                }
            });
            arrayAdapter = new ExposedDropDownArrayAdapter(requireActivity(), R.layout.view_exposed_dropdown_item, mainActivityInterface.getPageButtons().getPageButtonAvailableText());
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
        exposedDropDowns = new ArrayList<>();
        shortTexts = new ArrayList<>();
        longTexts = new ArrayList<>();
        exposedDropDowns.add(myView.button1Opt);
        exposedDropDowns.add(myView.button2Opt);
        exposedDropDowns.add(myView.button3Opt);
        exposedDropDowns.add(myView.button4Opt);
        exposedDropDowns.add(myView.button5Opt);
        exposedDropDowns.add(myView.button6Opt);

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
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("pageButtonShow"+(x+1), visible);
        setVisibilityFromBoolean(myLayouts.get(x),visible);
        mainActivityInterface.getPageButtons().setPageButtonVisibility(x,visible);
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
        exposedDropDowns.get(pos).setAdapter(arrayAdapter);
        exposedDropDowns.get(pos).setText(mainActivityInterface.getPageButtons().getPageButtonText(pos));
        arrayAdapter.keepSelectionPosition(exposedDropDowns.get(pos),mainActivityInterface.getPageButtons().getPageButtonAvailableText());
        exposedDropDowns.get(pos).addTextChangedListener(new TextWatcher() {
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
        shortTexts.get(pos).setHint(mainActivityInterface.getPageButtons().getPageButtonShortText(pos));
        longTexts.get(pos).setHint(mainActivityInterface.getPageButtons().getPageButtonLongText(pos));
        if (mainActivityInterface.getPageButtons().getPageButtonShortText(pos).isEmpty()) {
            shortTexts.get(pos).setVisibility(View.GONE);
        } else {
            shortTexts.get(pos).setVisibility(View.VISIBLE);
        }
        if (mainActivityInterface.getPageButtons().getPageButtonLongText(pos).isEmpty()) {
            longTexts.get(pos).setVisibility(View.GONE);
        } else {
            longTexts.get(pos).setVisibility(View.VISIBLE);
        }
    }

    private void saveDropDownChoice(int x, String text) {
        // x tells us the button we are dealing with and action is, well, the action
        int foundpos = mainActivityInterface.getPageButtons().getPositionFromText(text);
        mainActivityInterface.getPageButtons().setPageButtonAction(x,foundpos);
        mainActivityInterface.getPageButtons().setPageButtonText(x,foundpos);
        mainActivityInterface.getPageButtons().setPageButtonShortText(x,foundpos);
        mainActivityInterface.getPageButtons().setPageButtonLongText(x,foundpos);
        mainActivityInterface.getPageButtons().setPageButtonDrawable(requireContext(),x,foundpos);
        mainActivityInterface.getPageButtons().setPageButton(requireContext(), myButtons.get(x), x,true);
        setTheText(x);
        mainActivityInterface.getPreferences().setMyPreferenceString("pageButton"+(x+1),mainActivityInterface.getPageButtons().getPageButtonAction(x));
        mainActivityInterface.updatePageButtonLayout();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainActivityInterface.updatePageButtonLayout();
    }
}
