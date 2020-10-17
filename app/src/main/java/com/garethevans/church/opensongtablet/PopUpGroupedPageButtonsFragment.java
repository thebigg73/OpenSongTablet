package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class PopUpGroupedPageButtonsFragment extends DialogFragment {

    static PopUpGroupedPageButtonsFragment newInstance() {
        PopUpGroupedPageButtonsFragment frag;
        frag = new PopUpGroupedPageButtonsFragment();
        return frag;
    }

    public interface MyInterface {
        void openFragment();
        void loadSong();
        void gesture5();
        void gesture6();
        void gesture7();
        void displayHighlight(boolean fromautoshow);
    }

    private PopUpGroupedPageButtonsFragment.MyInterface mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (MyInterface) context;
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    private FloatingActionButton group_custom1;
    private FloatingActionButton group_custom2;
    private FloatingActionButton group_custom3;
    private FloatingActionButton group_custom4;

    private Preferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }

        View V = inflater.inflate(R.layout.popup_groupedpagebuttons, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.pagebuttons));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getActivity());
            closeMe.setEnabled(false);
            dismiss();
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        preferences = new Preferences();

        // Initialise the views
        FloatingActionButton group_set = V.findViewById(R.id.group_set);
        FloatingActionButton group_pad = V.findViewById(R.id.group_pad);
        FloatingActionButton group_autoscroll = V.findViewById(R.id.group_autoscroll);
        FloatingActionButton group_metronome = V.findViewById(R.id.group_metronome);
        FloatingActionButton group_chords = V.findViewById(R.id.group_chords);
        FloatingActionButton group_links = V.findViewById(R.id.group_links);
        FloatingActionButton group_sticky = V.findViewById(R.id.group_sticky);
        FloatingActionButton group_notation = V.findViewById(R.id.group_notation);
        FloatingActionButton group_highlight = V.findViewById(R.id.group_highlight);
        FloatingActionButton group_pages = V.findViewById(R.id.group_pages);
        group_custom1 = V.findViewById(R.id.group_custom1);
        group_custom2 = V.findViewById(R.id.group_custom2);
        group_custom3 = V.findViewById(R.id.group_custom3);
        group_custom4 = V.findViewById(R.id.group_custom4);

        // Set the quicklaunch icons
        setupQuickLaunchButtons();

        // Set the colors
        int color;
        switch (StaticVariables.mDisplayTheme) {
            case "dark":
            default:
                 color = preferences.getMyPreferenceInt(getActivity(),"dark_pageButtonsColor",StaticVariables.purplyblue);
                break;
            case "light":
                color = preferences.getMyPreferenceInt(getActivity(),"light_pageButtonsColor",StaticVariables.purplyblue);
                break;
            case "custom1":
                color = preferences.getMyPreferenceInt(getActivity(),"custom1_pageButtonsColor",StaticVariables.purplyblue);
                break;
            case "custom2":
                color = preferences.getMyPreferenceInt(getActivity(),"custom2_pageButtonsColor",StaticVariables.purplyblue);
                break;
        }

        group_set.setBackgroundTintList(ColorStateList.valueOf(color));
        group_pad.setBackgroundTintList(ColorStateList.valueOf(color));
        group_autoscroll.setBackgroundTintList(ColorStateList.valueOf(color));
        group_metronome.setBackgroundTintList(ColorStateList.valueOf(color));
        group_chords.setBackgroundTintList(ColorStateList.valueOf(color));
        group_links.setBackgroundTintList(ColorStateList.valueOf(color));
        group_sticky.setBackgroundTintList(ColorStateList.valueOf(color));
        group_notation.setBackgroundTintList(ColorStateList.valueOf(color));
        group_highlight.setBackgroundTintList(ColorStateList.valueOf(color));
        group_pages.setBackgroundTintList(ColorStateList.valueOf(color));
        group_custom1.setBackgroundTintList(ColorStateList.valueOf(color));
        group_custom2.setBackgroundTintList(ColorStateList.valueOf(color));
        group_custom3.setBackgroundTintList(ColorStateList.valueOf(color));
        group_custom4.setBackgroundTintList(ColorStateList.valueOf(color));

        // Set shortclick listeners
        group_set.setOnClickListener((view -> openAction("editset")));
        group_pad.setOnClickListener(view -> openAction("page_pad"));
        group_autoscroll.setOnClickListener(view -> openAction("page_autoscroll"));
        group_metronome.setOnClickListener(view -> openAction("page_metronome"));
        group_chords.setOnClickListener(view -> openAction("page_chords"));
        group_links.setOnClickListener(view -> openAction("page_links"));
        group_sticky.setOnClickListener(view -> openAction("page_sticky"));
        group_notation.setOnClickListener(view -> {
            if (StaticVariables.mNotation.equals("")) {
                openAction("abcnotation_edit");
            } else {
                openAction("abcnotation");
            }
        });
        group_highlight.setOnClickListener(view -> {
            FullscreenActivity.highlightOn = !FullscreenActivity.highlightOn;
            FullscreenActivity.whattodo = "page_highlight";
            if (mListener!=null) {
                mListener.displayHighlight(false);
            }
            dismiss();
        });
        group_pages.setOnClickListener(view -> openAction("page_pageselect"));
        group_custom1.setOnClickListener(view -> customButtonAction(preferences.getMyPreferenceString(getActivity(),"pageButtonCustom1Action","")));
        group_custom2.setOnClickListener(view -> customButtonAction(preferences.getMyPreferenceString(getActivity(),"pageButtonCustom2Action","")));
        group_custom3.setOnClickListener(view -> customButtonAction(preferences.getMyPreferenceString(getActivity(),"pageButtonCustom3Action","")));
        group_custom4.setOnClickListener(view -> customButtonAction(preferences.getMyPreferenceString(getActivity(),"pageButtonCustom4Action","")));

        // Set longclick listeners
        group_pad.setOnLongClickListener(view -> {
            if (mListener!=null) {
                mListener.gesture6();
            }
            dismiss();
            return true;
        });
        group_notation.setOnLongClickListener(view -> {
            openAction("abcnotation_edit");
            return true;
        });
        group_autoscroll.setOnLongClickListener(view -> {
            if (mListener!=null) {
                mListener.gesture5();
            }
            dismiss();
            return true;
        });

        group_metronome.setOnLongClickListener(view -> {
            if (mListener!=null) {
                mListener.gesture7();
            }
            dismiss();
            return true;
        });

        group_custom1.setOnLongClickListener(view -> {
            openAction("quicklaunch");
            return true;
        });
        group_custom2.setOnLongClickListener(view -> {
            openAction("quicklaunch");
            return true;
        });
        group_custom3.setOnLongClickListener(view -> {
            openAction("quicklaunch");
            return true;
        });
        group_custom4.setOnLongClickListener(view -> {
            openAction("quicklaunch");
            return true;
        });

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void setupQuickLaunchButtons() {
        // Based on the user's choices for the custom quicklaunch buttons,
        // set the appropriate icons and onClick listeners
        group_custom1.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(requireActivity(), preferences.getMyPreferenceString(getActivity(),"pageButtonCustom1Action","")));
        group_custom2.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(requireActivity(), preferences.getMyPreferenceString(getActivity(),"pageButtonCustom2Action","")));
        group_custom3.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(requireActivity(), preferences.getMyPreferenceString(getActivity(),"pageButtonCustom3Action","")));
        group_custom4.setImageDrawable(PopUpQuickLaunchSetup.getButtonImage(requireActivity(), preferences.getMyPreferenceString(getActivity(),"pageButtonCustom4Action","")));
    }

    private void customButtonAction(String s) {
        boolean val;
        switch (s) {
            case "":
            default:
                openAction("quicklaunch");
                break;

            case "editsong":
            case "changetheme":
            case "autoscale":
            case "changefonts":
            case "profiles":
            case "gestures":
            case "footpedal":
            case "transpose":
            case "fullsearch":
            case "editset":
                openAction(s);
                break;

            case "showchords":
                val = preferences.getMyPreferenceBoolean(getActivity(),"displayChords",true);
                preferences.setMyPreferenceBoolean(getActivity(),"displayChords",!val);
                saveSongAndLoadIt();
                break;

            case "showlyrics":
                val = preferences.getMyPreferenceBoolean(getActivity(),"displayLyrics",true);
                preferences.setMyPreferenceBoolean(getActivity(),"displayLyrics",!val);
                saveSongAndLoadIt();
                break;
        }
    }

    private void openAction(String s) {
        FullscreenActivity.whattodo = s;
        if (mListener!=null) {
            mListener.openFragment();
        }
        dismiss();
    }

    private void saveSongAndLoadIt() {
        if (mListener!=null) {
            mListener.loadSong();
        }
        dismiss();
    }
}
