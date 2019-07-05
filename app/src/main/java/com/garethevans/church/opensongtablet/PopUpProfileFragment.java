package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class PopUpProfileFragment extends DialogFragment {

    static PopUpProfileFragment newInstance() {
        PopUpProfileFragment frag;
        frag = new PopUpProfileFragment();
        return frag;
    }

    public interface MyInterface {
        void refreshAll();
        void setupPageButtons(String s);
    }

    private MyInterface mListener;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    StorageAccess storageAccess;
    Preferences preferences;
    private String[] foundFiles;
    private ScrollView profile_overview;
    private RelativeLayout profile_load, profile_save;
    private TextView profileName_TextView;
    private EditText profileName_EditText;
    private ListView profileFilesLoad_ListView, profileFilesSave_ListView;
    String name, what = "overview";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_profile, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.profile));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        storageAccess = new StorageAccess();
        preferences = new Preferences();

        // Initialise the views
        profile_overview = V.findViewById(R.id.profile_overview);
        profile_load = V.findViewById(R.id.profile_load);
        profile_save = V.findViewById(R.id.profile_save);
        profileName_TextView = V.findViewById(R.id.profileName_TextView);
        profileName_EditText = V.findViewById(R.id.profileName_EditText);
        profileFilesLoad_ListView = V.findViewById(R.id.profileFilesLoad_ListView);
        profileFilesSave_ListView = V.findViewById(R.id.profileFilesSave_ListView);
        Button loadProfile_Button = V.findViewById(R.id.loadProfile_Button);
        Button saveProfile_Button = V.findViewById(R.id.saveProfile_Button);
        Button cancelSave_Button = V.findViewById(R.id.cancelSave_Button);
        Button okSave_Button = V.findViewById(R.id.okSave_Button);
        Button cancelLoad_Button = V.findViewById(R.id.cancelLoad_Button);

        // Only show the first view with profile name and options to load or save or reset
        showOverView();

        name = preferences.getMyPreferenceString(getActivity(),"profileName","");
        if (name.equals("")) {
            name = getActivity().getString(R.string.options_song_new);
        }
        profileName_TextView.setText(name);
        profileName_EditText.setText(name);

        // Set up listeners for the overview page
        loadProfile_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoad();
            }
        });
        saveProfile_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSave();
            }
        });

        // Set up listeners for the save page
        okSave_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contents = prepareProfile();
                name = profileName_EditText.getText().toString();
                if (!name.equals("")) {
                    Uri uri = storageAccess.getUriForItem(getActivity(), preferences, "Profiles", "", name);

                    // Check the uri exists for the outputstream to be valid
                    storageAccess.lollipopCreateFileForOutputStream(getActivity(), preferences, uri, null,
                            "Profiles", "", name);

                    OutputStream outputStream = storageAccess.getOutputStream(getActivity(), uri);
                    storageAccess.writeFileFromString(contents,outputStream);
                    StaticVariables.myToastMessage = getString(R.string.ok);
                    preferences.setMyPreferenceString(getActivity(),"profileName",name);
                    profileName_TextView.setText(name);
                    profileName_EditText.setText(name);
                } else {
                    StaticVariables.myToastMessage = getString(R.string.profile) + " " +
                            getString(R.string.hasnotbeenexported);
                }
                ShowToast.showToast(getActivity());
                showOverView();
            }
        });
        cancelSave_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                what = "overview";
                showOverView();
            }
        });

        // Set up listeners for the load page
        cancelLoad_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                what = "overview";
                showOverView();
            }
        });
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void showOverView() {
        profile_overview.setVisibility(View.VISIBLE);
        profile_load.setVisibility(View.GONE);
        profile_save.setVisibility(View.GONE);
        what = "overview";
    }

    private void showLoad() {
        profile_overview.setVisibility(View.GONE);
        profile_load.setVisibility(View.VISIBLE);
        profile_save.setVisibility(View.GONE);
        what = "load";
        setupProfileList();
    }

    private void showSave() {
        profile_overview.setVisibility(View.GONE);
        profile_load.setVisibility(View.GONE);
        profile_save.setVisibility(View.VISIBLE);
        what = "save";
        setupProfileList();
    }

    private void setupProfileList() {
        ArrayList<String> tempFoundFiles = storageAccess.listFilesInFolder(getActivity(), preferences, "Profiles", "");

        // Sort the array list alphabetically by locale rules
        // Add locale sort
        Collator coll = Collator.getInstance(StaticVariables.locale);
        coll.setStrength(Collator.SECONDARY);
        Collections.sort(tempFoundFiles, coll);

        // Convert arraylist to string array
        foundFiles = new String[tempFoundFiles.size()];
        foundFiles = tempFoundFiles.toArray(foundFiles);

        // Add the saved profiles to the listview
        // Populate the file list view
        if (what.equals("save")) {
            profileFilesSave_ListView.setAdapter(new ArrayAdapter<>(Objects.requireNonNull(getActivity()), android.R.layout.simple_list_item_1, foundFiles));
            profileFilesSave_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        profileName_EditText.setText(foundFiles[position]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } else if (what.equals("load")) {
            profileFilesLoad_ListView.setAdapter(new ArrayAdapter<>(Objects.requireNonNull(getActivity()), android.R.layout.simple_list_item_1, foundFiles));
            profileFilesLoad_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        preferences.setMyPreferenceString(getActivity(),"profileName",foundFiles[position]);
                        grabvalues(foundFiles[position]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void grabvalues(String file) throws Exception {
        // Extract all of the key bits of the profile
        XmlPullParserFactory factory;
        factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp;
        xpp = factory.newPullParser();

        Uri uri = storageAccess.getUriForItem(getActivity(), preferences, "Profiles", "", file);
        InputStream inputStream = storageAccess.getInputStream(getActivity(),uri);
        xpp.setInput(inputStream,null);

        int eventType;
        eventType = xpp.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                switch (xpp.getName()) {
                    /*case "alphabeticalSize":
                        FullscreenActivity.alphabeticalSize = getFloatValue(xpp.nextText(), 14.0f);

                        break;
                    case "alwaysPreferredChordFormat":
                        FullscreenActivity.alwaysPreferredChordFormat = getTextValue(xpp.nextText(), "N");

                        break;
                    case "autoProject":
                        FullscreenActivity.autoProject = getBooleanValue(xpp.nextText(), false);

                        break;
                    case "autoscroll_default_or_prompt":
                        FullscreenActivity.autoscroll_default_or_prompt = getTextValue(xpp.nextText(), "prompt");

                        break;
                    case "autoScrollDelay":
                        FullscreenActivity.autoScrollDelay = getIntegerValue(xpp.nextText(), 10);

                        break;
                    case "autostartautoscroll":
                        FullscreenActivity.autostartautoscroll = getBooleanValue(xpp.nextText(), false);

                        break;
                    case "autostartmetronome":
                        FullscreenActivity.autostartmetronome = getBooleanValue(xpp.nextText(), false);

                        break;
                    case "autostartpad":
                        FullscreenActivity.autostartpad = getBooleanValue(xpp.nextText(), false);

                        break;
                    case "backgroundImage1":
                        FullscreenActivity.backgroundImage1 = getTextValue(xpp.nextText(), "ost_bg.png");

                        break;
                    case "backgroundImage2":
                        FullscreenActivity.backgroundImage2 = getTextValue(xpp.nextText(), "ost_bg.png");

                        break;
                    case "backgroundVideo1":
                        FullscreenActivity.backgroundVideo1 = getTextValue(xpp.nextText(), "");

                        break;
                    case "backgroundVideo2":
                        FullscreenActivity.backgroundVideo2 = getTextValue(xpp.nextText(), "");

                        break;
                    case "backgroundToUse":
                        FullscreenActivity.backgroundToUse = getTextValue(xpp.nextText(), "img1");

                        break;
                    case "backgroundTypeToUse":
                        FullscreenActivity.backgroundTypeToUse = getTextValue(xpp.nextText(), "image");

                        break;
                    case "bibleFile":
                        FullscreenActivity.bibleFile = getTextValue(xpp.nextText(), "");

                        break;
                    case "capoFontSizeInfoBar":
                        FullscreenActivity.capoFontSizeInfoBar = getFloatValue(xpp.nextText(), 14.0f);

                        break;
                    case "capoDisplay":
                        FullscreenActivity.capoDisplay = getTextValue(xpp.nextText(), "both");

                        break;
                    case "ccli_automatic":
                        FullscreenActivity.ccli_automatic = getBooleanValue(xpp.nextText(), false);

                        break;
                    case "ccli_church":
                        FullscreenActivity.ccli_church = getTextValue(xpp.nextText(), "");

                        break;
                    case "ccli_licence":
                        FullscreenActivity.ccli_licence = getTextValue(xpp.nextText(), "ccli_licence");

                        break;
                    case "chordfontscalesize":
                        FullscreenActivity.chordfontscalesize = getFloatValue(xpp.nextText(), 0.8f);

                        break;
                    case "chordFormat":
                        FullscreenActivity.chordFormat = getTextValue(xpp.nextText(), "1");

                        break;
                    case "chordInstrument":
                        FullscreenActivity.chordInstrument = getTextValue(xpp.nextText(), "g");

                        break;
                    case "commentfontscalesize":
                        FullscreenActivity.commentfontscalesize = getFloatValue(xpp.nextText(), 0.8f);

                        break;
                    case "customfontname":
                        FullscreenActivity.customfontname = getTextValue(xpp.nextText(), "");
                        break;
                    case "customLogo":
                        FullscreenActivity.customLogo = getTextValue(xpp.nextText(), "");

                        break;
                    case "customLogoSize":
                        FullscreenActivity.customLogoSize = getFloatValue(xpp.nextText(), 0.5f);

                        break;
                    case "customPadAb":
                        FullscreenActivity.customPadAb = getTextValue(xpp.nextText(), null);
                        break;
                    case "customPadA":
                        FullscreenActivity.customPadA = getTextValue(xpp.nextText(), null);
                        break;
                    case "customPadBb":
                        FullscreenActivity.customPadBb = getTextValue(xpp.nextText(), null);
                        break;
                    case "customPadB":
                        FullscreenActivity.customPadB = getTextValue(xpp.nextText(), null);
                        break;
                    case "customPadC":
                        FullscreenActivity.customPadC = getTextValue(xpp.nextText(), null);
                        break;
                    case "customPadDb":
                        FullscreenActivity.customPadDb = getTextValue(xpp.nextText(), null);
                        break;
                    case "customPadD":
                        FullscreenActivity.customPadD = getTextValue(xpp.nextText(), null);
                        break;
                    case "customPadEb":
                        FullscreenActivity.customPadEb = getTextValue(xpp.nextText(), null);
                        break;
                    case "customPadE":
                        FullscreenActivity.customPadE = getTextValue(xpp.nextText(), null);
                        break;
                    case "customPadF":
                        FullscreenActivity.customPadF = getTextValue(xpp.nextText(), null);
                        break;
                    case "customPadGb":
                        FullscreenActivity.customPadGb = getTextValue(xpp.nextText(), null);
                        break;
                    case "customPadG":
                        FullscreenActivity.customPadG = getTextValue(xpp.nextText(), null);
                        break;
                    case "customPadAbm":
                        FullscreenActivity.customPadAbm = getTextValue(xpp.nextText(), null);
                        break;
                    case "customPadAm":
                        FullscreenActivity.customPadAm = getTextValue(xpp.nextText(), null);
                        break;
                    case "customPadBbm":
                        FullscreenActivity.customPadBbm = getTextValue(xpp.nextText(), null);
                        break;
                    case "customPadBm":
                        FullscreenActivity.customPadBm = getTextValue(xpp.nextText(), null);
                        break;
                    case "customPadCm":
                        FullscreenActivity.customPadCm = getTextValue(xpp.nextText(), null);
                        break;
                    case "customPadDbm":
                        FullscreenActivity.customPadDbm = getTextValue(xpp.nextText(), null);
                        break;
                    case "customPadDm":
                        FullscreenActivity.customPadDm = getTextValue(xpp.nextText(), null);
                        break;
                    case "customPadEbm":
                        FullscreenActivity.customPadEbm = getTextValue(xpp.nextText(), null);
                        break;
                    case "customPadEm":
                        FullscreenActivity.customPadEm = getTextValue(xpp.nextText(), null);
                        break;
                    case "customPadFm":
                        FullscreenActivity.customPadFm = getTextValue(xpp.nextText(), null);
                        break;
                    case "customPadGbm":
                        FullscreenActivity.customPadGbm = getTextValue(xpp.nextText(), null);
                        break;
                    case "customPadGm":
                        FullscreenActivity.customPadGm = getTextValue(xpp.nextText(), null);

                        break;
                    case "default_autoscroll_predelay":
                        FullscreenActivity.default_autoscroll_predelay = getIntegerValue(xpp.nextText(), 10);
                        break;
                    case "default_autoscroll_predelay_max":
                        FullscreenActivity.default_autoscroll_predelay_max = getIntegerValue(xpp.nextText(), 30);
                        break;
                    case "default_autoscroll_songlength":
                        FullscreenActivity.default_autoscroll_songlength = getIntegerValue(xpp.nextText(), 180);

                        break;
                    case "drawingEraserSize":
                        FullscreenActivity.drawingEraserSize = getIntegerValue(xpp.nextText(), 20);
                        break;
                    case "drawingHighlightColor":
                        FullscreenActivity.drawingHighlightColor = getTextValue(xpp.nextText(), "yellow");
                        break;
                    case "drawingHighlightSize":
                        FullscreenActivity.drawingHighlightSize = getIntegerValue(xpp.nextText(), 20);
                        break;
                    case "drawingPenColor":
                        FullscreenActivity.drawingPenColor = getTextValue(xpp.nextText(), "yellow");
                        break;
                    case "drawingPenSize":
                        FullscreenActivity.drawingPenSize = getIntegerValue(xpp.nextText(), 20);
                        break;
                    case "drawingTool":
                        FullscreenActivity.drawingTool = getTextValue(xpp.nextText(), "highlighter");

                        break;
                    case "editAsChordPro":
                        FullscreenActivity.editAsChordPro = getBooleanValue(xpp.nextText(), false);

                        break;
                    case "exportOpenSongAppSet":
                        FullscreenActivity.exportOpenSongAppSet = getBooleanValue(xpp.nextText(), false);
                        break;
                    case "exportOpenSongApp":
                        FullscreenActivity.exportOpenSongApp = getBooleanValue(xpp.nextText(), false);
                        break;
                    case "exportDesktop":
                        FullscreenActivity.exportDesktop = getBooleanValue(xpp.nextText(), false);
                        break;
                    case "exportText":
                        FullscreenActivity.exportText = getBooleanValue(xpp.nextText(), false);
                        break;
                    case "exportChordPro":
                        FullscreenActivity.exportChordPro = getBooleanValue(xpp.nextText(), false);
                        break;
                    case "exportOnSong":
                        FullscreenActivity.exportOnSong = getBooleanValue(xpp.nextText(), false);
                        break;
                    case "exportImage":
                        FullscreenActivity.exportImage = getBooleanValue(xpp.nextText(), false);
                        break;
                    case "exportPDF":
                        FullscreenActivity.exportPDF = getBooleanValue(xpp.nextText(), false);

                        break;
                    case "fabSize":
                        FullscreenActivity.fabSize = getIntegerValue(xpp.nextText(), FloatingActionButton.SIZE_MINI);

                        break;
                    case "gesture_doubletap":
                        FullscreenActivity.gesture_doubletap = getTextValue(xpp.nextText(), "2");

                        break;
                    case "gesture_longpress":
                        FullscreenActivity.gesture_longpress = getTextValue(xpp.nextText(), "1");

                        break;
                    case "grouppagebuttons":
                        FullscreenActivity.grouppagebuttons = getBooleanValue(xpp.nextText(), false);

                        break;
                    case "headingfontscalesize":
                        FullscreenActivity.headingfontscalesize = getFloatValue(xpp.nextText(), 0.6f);

                        break;
                    case "hideActionBar":
                        FullscreenActivity.hideActionBar = getBooleanValue(xpp.nextText(), false);

                        break;
                    case "hideLyricsBox":
                        FullscreenActivity.hideLyricsBox = getBooleanValue(xpp.nextText(), false);

                        break;
                    case "highlightShowSecs":
                        FullscreenActivity.highlightShowSecs = getIntegerValue(xpp.nextText(), 0);

                        break;
                    case "languageToLoad":
                        FullscreenActivity.languageToLoad = getTextValue(xpp.nextText(), "");

                        break;
                    case "lastSetName":
                        FullscreenActivity.lastSetName = getTextValue(xpp.nextText(), "");

                        break;

                    case "linespacing":
                        FullscreenActivity.linespacing = getFloatValue(xpp.nextText(), 0.1f);
                        break;

                    case "locale":
                        StaticVariables.locale = new Locale(getTextValue(xpp.nextText(), Preferences.getStoredLocale().toString()));

                        break;
                    case "longpressdownpedalgesture":
                        FullscreenActivity.longpressdownpedalgesture = getTextValue(xpp.nextText(), "");

                        break;
                    case "longpressnextpedalgesture":
                        FullscreenActivity.longpressnextpedalgesture = getTextValue(xpp.nextText(), "4");

                        break;
                    case "longpresspreviouspedalgesture":
                        FullscreenActivity.longpresspreviouspedalgesture = getTextValue(xpp.nextText(), "1");

                        break;
                    case "longpressuppedalgesture":
                        FullscreenActivity.longpressuppedalgesture = getTextValue(xpp.nextText(), "");

                        break;
                    case "maxvolrange":
                        FullscreenActivity.maxvolrange = getIntegerValue(xpp.nextText(), 400);

                        break;
                    case "mediaStore":
                        FullscreenActivity.mediaStore = getTextValue(xpp.nextText(), "int");

                        break;
                    case "metronomepan":
                        FullscreenActivity.metronomepan = getTextValue(xpp.nextText(), "both");

                        break;
                    case "metronomevol":
                        FullscreenActivity.metronomevol = getFloatValue(xpp.nextText(), 0.5f);

                        break;
                    case "midiAuto":
                        FullscreenActivity.midiAuto = getBooleanValue(xpp.nextText(), false);

                        break;
                    case "mAuthor":
                        FullscreenActivity.mAuthor = getTextValue(xpp.nextText(), "Gareth Evans");

                        break;
                    case "mCopyright":
                        FullscreenActivity.mCopyright = getTextValue(xpp.nextText(), "");

                        break;
                    case "mDisplayTheme":
                        StaticVariables.mDisplayTheme = getTextValue(xpp.nextText(), "Theme.Holo");

                        break;
                    case "menuSize":
                        FullscreenActivity.menuSize = getFloatValue(xpp.nextText(), 0.6f);

                        break;
                    case "mFontSize":
                        FullscreenActivity.mFontSize = getFloatValue(xpp.nextText(), 42.0f);

                        break;
                    case "mMaxFontSize":
                        FullscreenActivity.mMaxFontSize = getIntegerValue(xpp.nextText(), 50);

                        break;
                    case "mMinFontSize":
                        FullscreenActivity.mMinFontSize = getIntegerValue(xpp.nextText(), 8);

                        break;
                    case "mTitle":
                        FullscreenActivity.mTitle = getTextValue(xpp.nextText(), "Welcome to OpenSongApp");

                        break;
                    case "multilineCompact":
                        FullscreenActivity.multilineCompact = getBooleanValue(xpp.nextText(), false);

                        break;
                    case "myAlert":
                        FullscreenActivity.myAlert = getTextValue(xpp.nextText(), "");

                        break;
                    case "mychordsfontnum":
                        FullscreenActivity.mychordsfontnum = getIntegerValue(xpp.nextText(), 8);

                        break;
                    case "mylyricsfontnum":
                        FullscreenActivity.mylyricsfontnum = getIntegerValue(xpp.nextText(), 8);

                        break;
                    case "mypresofontnum":
                        FullscreenActivity.mypresofontnum = getIntegerValue(xpp.nextText(), 8);

                        break;
                    case "mypresoinfofontnum":
                        FullscreenActivity.mypresoinfofontnum = getIntegerValue(xpp.nextText(), 8);

                        break;
                    case "mySet":
                        StaticVariables.mySet = getTextValue(xpp.nextText(), "");

                        break;
                    case "override_fullscale":
                        FullscreenActivity.override_fullscale = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "override_widthscale":
                        FullscreenActivity.override_widthscale = getBooleanValue(xpp.nextText(), false);

                        break;
                    case "padpan":
                        FullscreenActivity.padpan = getTextValue(xpp.nextText(), "both");

                        break;
                    case "padvol":
                        FullscreenActivity.padvol = getFloatValue(xpp.nextText(), 1.0f);

                        break;
                    case "page_autoscroll_visible":
                        FullscreenActivity.page_autoscroll_visible = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "page_chord_visible":
                        FullscreenActivity.page_chord_visible = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "page_custom_grouped":
                        FullscreenActivity.page_custom_grouped = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "page_custom_visible":
                        FullscreenActivity.page_custom_visible = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "page_custom1_visible":
                        FullscreenActivity.page_custom1_visible = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "page_custom2_visible":
                        FullscreenActivity.page_custom2_visible = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "page_custom3_visible":
                        FullscreenActivity.page_custom3_visible = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "page_custom4_visible":
                        FullscreenActivity.page_custom4_visible = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "page_extra_grouped":
                        FullscreenActivity.page_extra_grouped = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "page_extra_visible":
                        FullscreenActivity.page_extra_visible = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "page_highlight_visible":
                        FullscreenActivity.page_highlight_visible = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "page_links_visible":
                        FullscreenActivity.page_links_visible = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "page_metronome_visible":
                        FullscreenActivity.page_metronome_visible = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "page_notation_visible":
                        FullscreenActivity.page_notation_visible = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "page_pad_visible":
                        FullscreenActivity.page_pad_visible = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "page_pages_visible":
                        FullscreenActivity.page_pages_visible = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "page_set_visible":
                        FullscreenActivity.page_set_visible = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "page_sticky_visible":
                        FullscreenActivity.page_sticky_visible = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "pagebutton_position":
                        FullscreenActivity.pagebutton_position = getTextValue(xpp.nextText(), "right");

                        break;
                    case "pagebutton_scale":
                        FullscreenActivity.pagebutton_scale = getTextValue(xpp.nextText(), "M");

                        break;
                    case "pageButtonAlpha":
                        FullscreenActivity.pageButtonAlpha = getFloatValue(xpp.nextText(), 0.2f);

                        break;
                    case "pedal1":
                        FullscreenActivity.pedal1 = getIntegerValue(xpp.nextText(), 21);

                        break;
                    case "pedal2":
                        FullscreenActivity.pedal2 = getIntegerValue(xpp.nextText(), 22);

                        break;
                    case "pedal3":
                        FullscreenActivity.pedal3 = getIntegerValue(xpp.nextText(), 19);

                        break;
                    case "pedal4":
                        FullscreenActivity.pedal4 = getIntegerValue(xpp.nextText(), 20);

                        break;
                    case "pedal5":
                        FullscreenActivity.pedal5 = getIntegerValue(xpp.nextText(), -1);

                        break;
                    case "pedal6":
                        FullscreenActivity.pedal6 = getIntegerValue(xpp.nextText(), -1);

                        break;
                    case "pedal1shortaction":
                        FullscreenActivity.pedal1shortaction = getTextValue(xpp.nextText(), "prev");

                        break;
                    case "pedal2shortaction":
                        FullscreenActivity.pedal2shortaction = getTextValue(xpp.nextText(), "next");

                        break;
                    case "pedal3shortaction":
                        FullscreenActivity.pedal3shortaction = getTextValue(xpp.nextText(), "up");

                        break;
                    case "pedal4shortaction":
                        FullscreenActivity.pedal4shortaction = getTextValue(xpp.nextText(), "down");

                        break;
                    case "pedal5shortaction":
                        FullscreenActivity.pedal5shortaction = getTextValue(xpp.nextText(), "");

                        break;
                    case "pedal6shortaction":
                        FullscreenActivity.pedal6shortaction = getTextValue(xpp.nextText(), "");

                        break;
                    case "pedal1longaction":
                        FullscreenActivity.pedal1longaction = getTextValue(xpp.nextText(), "songmenu");

                        break;
                    case "pedal2longaction":
                        FullscreenActivity.pedal2longaction = getTextValue(xpp.nextText(), "set");

                        break;
                    case "pedal3longaction":
                        FullscreenActivity.pedal3longaction = getTextValue(xpp.nextText(), "");

                        break;
                    case "pedal4longaction":
                        FullscreenActivity.pedal4longaction = getTextValue(xpp.nextText(), "");

                        break;
                    case "pedal5longaction":
                        FullscreenActivity.pedal5longaction = getTextValue(xpp.nextText(), "");

                        break;
                    case "pedal6longaction":
                        FullscreenActivity.pedal6longaction = getTextValue(xpp.nextText(), "");

                        break;
                    case "popupAlpha_All":
                        FullscreenActivity.popupAlpha_All = getFloatValue(xpp.nextText(), 0.7f);

                        break;
                    case "popupAlpha_Set":
                        FullscreenActivity.popupAlpha_Set = getFloatValue(xpp.nextText(), 0.8f);

                        break;
                    case "popupDim_All":
                        FullscreenActivity.popupDim_All = getFloatValue(xpp.nextText(), 0.8f);

                        break;
                    case "popupDim_Set":
                        FullscreenActivity.popupDim_Set = getFloatValue(xpp.nextText(), 0.8f);

                        break;
                    case "popupPosition_All":
                        FullscreenActivity.popupPosition_All = getTextValue(xpp.nextText(), "c");

                        break;
                    case "popupPosition_Set":
                        FullscreenActivity.popupPosition_Set = getTextValue(xpp.nextText(), "c");

                        break;
                    case "popupScale_All":
                        FullscreenActivity.popupScale_All = getFloatValue(xpp.nextText(), 0.8f);

                        break;
                    case "popupScale_Set":
                        FullscreenActivity.popupScale_Set = getFloatValue(xpp.nextText(), 0.8f);

                        break;
                    case "prefChord_Aflat_Gsharp":
                        FullscreenActivity.prefChord_Aflat_Gsharp = getTextValue(xpp.nextText(), "b");
                        break;
                    case "prefChord_Bflat_Asharp":
                        FullscreenActivity.prefChord_Bflat_Asharp = getTextValue(xpp.nextText(), "b");
                        break;
                    case "prefChord_Dflat_Csharp":
                        FullscreenActivity.prefChord_Dflat_Csharp = getTextValue(xpp.nextText(), "b");
                        break;
                    case "prefChord_Eflat_Dsharp":
                        FullscreenActivity.prefChord_Eflat_Dsharp = getTextValue(xpp.nextText(), "b");
                        break;
                    case "prefChord_Gflat_Fsharp":
                        FullscreenActivity.prefChord_Gflat_Fsharp = getTextValue(xpp.nextText(), "b");
                        break;
                    case "prefChord_Aflatm_Gsharpm":
                        FullscreenActivity.prefChord_Aflatm_Gsharpm = getTextValue(xpp.nextText(), "#");
                        break;
                    case "prefChord_Bflatm_Asharpm":
                        FullscreenActivity.prefChord_Bflatm_Asharpm = getTextValue(xpp.nextText(), "b");
                        break;
                    case "prefChord_Dflatm_Csharpm":
                        FullscreenActivity.prefChord_Dflatm_Csharpm = getTextValue(xpp.nextText(), "#");
                        break;
                    case "prefChord_Eflatm_Dsharpm":
                        FullscreenActivity.prefChord_Eflatm_Dsharpm = getTextValue(xpp.nextText(), "b");
                        break;
                    case "prefChord_Gflatm_Fsharpm":
                        FullscreenActivity.prefChord_Gflatm_Fsharpm = getTextValue(xpp.nextText(), "#");

                        break;
                    case "presenterChords":
                        FullscreenActivity.presenterChords = getTextValue(xpp.nextText(), "N");

                        break;
                    case "presoAlpha":
                        FullscreenActivity.presoAlpha = getFloatValue(xpp.nextText(), 1.0f);

                        break;
                    case "presoAlertSize":
                        FullscreenActivity.presoAlertSize = getFloatValue(xpp.nextText(), 8.0f);

                        break;
                    case "presoAuthorSize":
                        FullscreenActivity.presoAuthorSize = getFloatValue(xpp.nextText(), 8.0f);

                        break;
                    case "presoAutoScale":
                        FullscreenActivity.presoAutoScale = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "presoCopyrightSize":
                        FullscreenActivity.presoCopyrightSize = getFloatValue(xpp.nextText(), 8.0f);

                        break;
                    case "presoFontSize":
                        FullscreenActivity.presoFontSize = getIntegerValue(xpp.nextText(), 12);

                        break;
                    case "presoInfoAlign":
                        FullscreenActivity.presoInfoAlign = getIntegerValue(xpp.nextText(), Gravity.END);

                        break;
                    case "presoLyricsAlign":
                        FullscreenActivity.presoLyricsAlign = getIntegerValue(xpp.nextText(), Gravity.CENTER_HORIZONTAL);

                        break;
                    case "presoMaxFontSize":
                        FullscreenActivity.presoLyricsAlign = getIntegerValue(xpp.nextText(), 40);

                        break;
                    case "presoShowChords":
                        FullscreenActivity.presoShowChords = getBooleanValue(xpp.nextText(), false);

                        break;
                    case "presoTitleSize":
                        FullscreenActivity.presoTitleSize = getFloatValue(xpp.nextText(), 10.0f);

                        break;
                    case "presoTransitionTime":
                        FullscreenActivity.presoTransitionTime = getIntegerValue(xpp.nextText(), 800);

                        break;
                    case "profile":
                        FullscreenActivity.profile = getTextValue(xpp.nextText(), "");

                        break;
                    case "quickLaunchButton_1":
                        FullscreenActivity.quickLaunchButton_1 = getTextValue(xpp.nextText(), "");

                        break;
                    case "quickLaunchButton_2":
                        FullscreenActivity.quickLaunchButton_2 = getTextValue(xpp.nextText(), "");

                        break;
                    case "quickLaunchButton_3":
                        FullscreenActivity.quickLaunchButton_3 = getTextValue(xpp.nextText(), "");

                        break;
                    case "quickLaunchButton_4":
                        FullscreenActivity.quickLaunchButton_4 = getTextValue(xpp.nextText(), "");

                        break;
                    case "randomFolders":
                        FullscreenActivity.randomFolders = getTextValue(xpp.nextText(), "");

                        break;
                    case "scrollDistance":
                        FullscreenActivity.scrollDistance = getFloatValue(xpp.nextText(), 0.6f);

                        break;
                    case "scrollSpeed":
                        FullscreenActivity.scrollSpeed = getIntegerValue(xpp.nextText(), 1500);

                        break;
                    case "showAlphabeticalIndexInSongMenu":
                        FullscreenActivity.showAlphabeticalIndexInSongMenu = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "showCapoAsNumerals":
                        FullscreenActivity.showCapoAsNumerals = getBooleanValue(xpp.nextText(), false);

                        break;
                    case "showCapoChords":
                        FullscreenActivity.showCapoChords = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "showLyrics":
                        FullscreenActivity.showLyrics = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "showNativeAndCapoChords":
                        FullscreenActivity.showNativeAndCapoChords = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "showChords":
                        FullscreenActivity.showChords = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "showNextInSet":
                        FullscreenActivity.showNextInSet = getTextValue(xpp.nextText(), "bottom");

                        break;
                    case "showSetTickBoxInSongMenu":
                        FullscreenActivity.showSetTickBoxInSongMenu = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "songfilename":
                        StaticVariables.songfilename = getTextValue(xpp.nextText(), "");

                        break;
                    case "stagemodeScale":
                        FullscreenActivity.stagemodeScale = getFloatValue(xpp.nextText(), 0.7f);

                        break;
                    case "stickyNotesShowSecs":
                        FullscreenActivity.stickyNotesShowSecs = getIntegerValue(xpp.nextText(), 8);

                        break;
                    case "stickyOpacity":
                        FullscreenActivity.stickyOpacity = getFloatValue(xpp.nextText(), 0.8f);

                        break;
                    case "stickyTextSize":
                        FullscreenActivity.stickyTextSize = getFloatValue(xpp.nextText(), 14.0f);

                        break;
                    case "stickyWidth":
                        FullscreenActivity.stickyWidth = getIntegerValue(xpp.nextText(), 400);


                        break;
                    case "SWIPE_MAX_OFF_PATH":
                        FullscreenActivity.SWIPE_MAX_OFF_PATH = getIntegerValue(xpp.nextText(), 200);

                        break;
                    case "SWIPE_MIN_DISTANCE":
                        FullscreenActivity.SWIPE_MIN_DISTANCE = getIntegerValue(xpp.nextText(), 250);

                        break;
                    case "SWIPE_THRESHOLD_VELOCITY":
                        FullscreenActivity.SWIPE_THRESHOLD_VELOCITY = getIntegerValue(xpp.nextText(), 600);


                        break;
                    case "swipeDrawer":
                        FullscreenActivity.swipeDrawer = getTextValue(xpp.nextText(), "Y");

                        break;
                    case "swipeForMenus":
                        FullscreenActivity.swipeForMenus = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "swipeForSongs":
                        FullscreenActivity.swipeForSongs = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "swipeSet":
                        FullscreenActivity.swipeSet = getTextValue(xpp.nextText(), "Y");

                        break;
                    case "timerFontSizeAutoScroll":
                        FullscreenActivity.timerFontSizeAutoScroll = getFloatValue(xpp.nextText(), 14.0f);

                        break;
                    case "timerFontSizePad":
                        FullscreenActivity.timerFontSizePad = getFloatValue(xpp.nextText(), 14.0f);

                        break;
                    case "toggleAutoHighlight":
                        FullscreenActivity.toggleAutoHighlight = getBooleanValue(xpp.nextText(), true);

                        break;
                    case "toggleAutoSticky":
                        FullscreenActivity.toggleAutoSticky = getTextValue(xpp.nextText(), "N");

                        break;
                    case "togglePageButtons":
                        FullscreenActivity.togglePageButtons = getTextValue(xpp.nextText(), "Y");

                        break;
                    case "toggleScrollArrows":
                        FullscreenActivity.toggleScrollArrows = getTextValue(xpp.nextText(), "S");

                        break;
                    case "toggleScrollBeforeSwipe":
                        FullscreenActivity.toggleScrollBeforeSwipe = getTextValue(xpp.nextText(), "Y");

                        break;
                    case "toggleYScale":
                        FullscreenActivity.toggleYScale = getTextValue(xpp.nextText(), "W");

                        break;
                    case "transposeStyle":
                        FullscreenActivity.transposeStyle = getTextValue(xpp.nextText(), "sharps");
                        break;

                    case "trimLines":
                        FullscreenActivity.trimLines = getBooleanValue(xpp.nextText(), true);
                        break;

                    case "trimSections":
                        FullscreenActivity.trimSections = getBooleanValue(xpp.nextText(), false);

                        break;
                    case "trimSectionSpace":
                        FullscreenActivity.trimSectionSpace = getBooleanValue(xpp.nextText(), false);

                        break;
                    case "usePresentationOrder":
                        FullscreenActivity.usePresentationOrder = getBooleanValue(xpp.nextText(), false);

                        break;
                    case "visualmetronome":
                        FullscreenActivity.visualmetronome = getBooleanValue(xpp.nextText(), false);

                        //} else if (xpp.getName().equals("whichMode")) {
                        //    FullscreenActivity.whichMode = getTextValue(xpp.nextText(),"Performance");

                        break;
                    case "whichSetCategory":
                        FullscreenActivity.whichSetCategory = getTextValue(xpp.nextText(), FullscreenActivity.mainfoldername);

                        break;
                    case "whichSongFolder":
                        StaticVariables.whichSongFolder = getTextValue(xpp.nextText(), FullscreenActivity.mainfoldername);

                        break;
                    case "xmargin_presentation":
                        FullscreenActivity.xmargin_presentation = getIntegerValue(xpp.nextText(), 50);

                        break;
                    case "ymargin_presentation":
                        FullscreenActivity.ymargin_presentation = getIntegerValue(xpp.nextText(), 25);

                        break;*/
                }

            }

            try {
                eventType = xpp.next();
            } catch (Exception e) {
                //Ooops!
                Log.d("d","error in file, or not xml");
            }
        }

        // Reload the display
        try {
            mListener.refreshAll();
            mListener.setupPageButtons("");
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getIntegerValue(String s, int def) {
        int integer;
        if (s!=null && !s.equals("")) {
            try {
                integer = Integer.parseInt(s);
            } catch (Exception e) {
                integer = def;
            }
        } else {
            integer = def;
        }
        return integer;
    }

    public String getTextValue(String s, String def) {
        String text = def;
        if (s!=null && !s.equals("")) {
            text = s;
        }
        return text;
    }

    public boolean getBooleanValue(String s, boolean def) {
        boolean trueorfalse = def;
        if (s!=null && !s.equals("")) {
            trueorfalse = s.equals("true");
        }
        return trueorfalse;
    }

    public float getFloatValue(String s, float def) {
        float f = def;
        if (s!=null && !s.equals("")) {
            try {
                f = Float.parseFloat(s.replace("f",""));
            } catch (Exception e) {
                f = def;
            }
        }
        return f;
    }

    private String prepareProfile() {

        String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        /*text += "<myprofile>\n";
        text += "  <alphabeticalSize>" + FullscreenActivity.alphabeticalSize + "</alphabeticalSize>\n";
        text += "  <alwaysPreferredChordFormat>" + FullscreenActivity.alwaysPreferredChordFormat + "</alwaysPreferredChordFormat>\n";
        text += "  <autoProject>" + FullscreenActivity.autoProject + "</autoProject>\n";
        text += "  <autoscroll_default_or_prompt>" + FullscreenActivity.autoscroll_default_or_prompt + "</autoscroll_default_or_prompt>\n";
        text += "  <autoScrollDelay>" + FullscreenActivity.autoScrollDelay + "</autoScrollDelay>\n";
        text += "  <autostartautoscroll>" + FullscreenActivity.autostartautoscroll + "</autostartautoscroll>\n";
        text += "  <autostartmetronome>" + FullscreenActivity.autostartmetronome + "</autostartmetronome>\n";
        text += "  <autostartpad>" + FullscreenActivity.autostartpad + "</autostartpad>\n";
        text += "  <backgroundImage1>" + FullscreenActivity.backgroundImage1 + "</backgroundImage1>\n";
        text += "  <backgroundImage2>" + FullscreenActivity.backgroundImage2 + "</backgroundImage2>\n";
        text += "  <backgroundVideo1>" + FullscreenActivity.backgroundVideo1 + "</backgroundVideo1>\n";
        text += "  <backgroundVideo2>" + FullscreenActivity.backgroundVideo2 + "</backgroundVideo2>\n";
        text += "  <backgroundToUse>" + FullscreenActivity.backgroundToUse + "</backgroundToUse>\n";
        text += "  <backgroundTypeToUse>" + FullscreenActivity.backgroundTypeToUse + "</backgroundTypeToUse>\n";
        text += "  <bibleFile>" + FullscreenActivity.bibleFile + "</bibleFile>\n";
        text += "  <capoDisplay>" + FullscreenActivity.capoDisplay + "</capoDisplay>\n";
        text += "  <capoFontSizeInfoBar>" + FullscreenActivity.capoFontSizeInfoBar + "</capoFontSizeInfoBar>\n";
        text += "  <ccli_automatic>" + FullscreenActivity.ccli_automatic + "</ccli_automatic>\n";
        text += "  <ccli_church>" + FullscreenActivity.ccli_church + "</ccli_church>\n";
        text += "  <ccli_licence>" + FullscreenActivity.ccli_licence + "</ccli_licence>\n";
        text += "  <chordfontscalesize>" + FullscreenActivity.chordfontscalesize + "</chordfontscalesize>\n";
        text += "  <chordFormat>" + FullscreenActivity.chordFormat + "</chordFormat>\n";
        text += "  <chordInstrument>" + FullscreenActivity.chordInstrument + "</chordInstrument>\n";
        text += "  <commentfontscalesize>" + FullscreenActivity.commentfontscalesize + "</commentfontscalesize>\n";
        text += "  <customfontname>" + FullscreenActivity.customfontname + "</customfontname>\n";
        text += "  <customLogo>" + FullscreenActivity.customLogo + "</customLogo>\n";
        text += "  <customLogoSize>" + FullscreenActivity.customLogoSize + "</customLogoSize>\n";
        text += "  <customPadAb>" + FullscreenActivity.customPadAb + "</customPadAb>\n";
        text += "  <customPadA>" + FullscreenActivity.customPadA + "</customPadA>\n";
        text += "  <customPadBb>" + FullscreenActivity.customPadBb + "</customPadBb>\n";
        text += "  <customPadB>" + FullscreenActivity.customPadB + "</customPadB>\n";
        text += "  <customPadC>" + FullscreenActivity.customPadC + "</customPadC>\n";
        text += "  <customPadDb>" + FullscreenActivity.customPadDb + "</customPadDb>\n";
        text += "  <customPadD>" + FullscreenActivity.customPadD + "</customPadD>\n";
        text += "  <customPadEb>" + FullscreenActivity.customPadEb + "</customPadEb>\n";
        text += "  <customPadE>" + FullscreenActivity.customPadE + "</customPadE>\n";
        text += "  <customPadF>" + FullscreenActivity.customPadF + "</customPadF>\n";
        text += "  <customPadGb>" + FullscreenActivity.customPadGb + "</customPadGb>\n";
        text += "  <customPadG>" + FullscreenActivity.customPadG + "</customPadG>\n";
        text += "  <customPadAbm>" + FullscreenActivity.customPadAbm + "</customPadAbm>\n";
        text += "  <customPadAm>" + FullscreenActivity.customPadAm + "</customPadAm>\n";
        text += "  <customPadBbm>" + FullscreenActivity.customPadBbm + "</customPadBbm>\n";
        text += "  <customPadBm>" + FullscreenActivity.customPadBm + "</customPadBm>\n";
        text += "  <customPadCm>" + FullscreenActivity.customPadCm + "</customPadCm>\n";
        text += "  <customPadDbm>" + FullscreenActivity.customPadDbm + "</customPadDbm>\n";
        text += "  <customPadDm>" + FullscreenActivity.customPadDm + "</customPadDm>\n";
        text += "  <customPadEbm>" + FullscreenActivity.customPadEbm + "</customPadEbm>\n";
        text += "  <customPadEm>" + FullscreenActivity.customPadEm + "</customPadEm>\n";
        text += "  <customPadFm>" + FullscreenActivity.customPadFm + "</customPadFm>\n";
        text += "  <customPadGbm>" + FullscreenActivity.customPadGbm + "</customPadGbm>\n";
        text += "  <customPadGm>" + FullscreenActivity.customPadGm + "</customPadGm>\n";
        text += "  <default_autoscroll_predelay>" + FullscreenActivity.default_autoscroll_predelay + "</default_autoscroll_predelay>\n";
        text += "  <default_autoscroll_predelay_max>" + FullscreenActivity.default_autoscroll_predelay_max + "</default_autoscroll_predelay_max>\n";
        text += "  <default_autoscroll_songlength>" + FullscreenActivity.default_autoscroll_songlength + "</default_autoscroll_songlength>\n";
        text += "  <drawingEraserSize>" + FullscreenActivity.drawingEraserSize + "</drawingEraserSize>\n";
        text += "  <drawingHighlightColor>" + FullscreenActivity.drawingHighlightColor + "</drawingHighlightColor>\n";
        text += "  <drawingHighlightSize>" + FullscreenActivity.drawingHighlightSize + "</drawingHighlightSize>\n";
        text += "  <drawingPenColor>" + FullscreenActivity.drawingPenColor + "</drawingPenColor>\n";
        text += "  <drawingPenSize>" + FullscreenActivity.drawingPenSize + "</drawingPenSize>\n";
        text += "  <drawingTool>" + FullscreenActivity.drawingTool + "</drawingTool>\n";
        text += "  <editAsChordPro>" + FullscreenActivity.editAsChordPro + "</editAsChordPro>\n";
        text += "  <exportOpenSongAppSet>" + FullscreenActivity.exportOpenSongAppSet + "</exportOpenSongAppSet>\n";
        text += "  <exportOpenSongApp>" + FullscreenActivity.exportOpenSongApp + "</exportOpenSongApp>\n";
        text += "  <exportDesktop>" + FullscreenActivity.exportDesktop + "</exportDesktop>\n";
        text += "  <exportText>" + FullscreenActivity.exportText + "</exportText>\n";
        text += "  <exportChordPro>" + FullscreenActivity.exportChordPro + "</exportChordPro>\n";
        text += "  <exportOnSong>" + FullscreenActivity.exportOnSong + "</exportOnSong>\n";
        text += "  <exportImage>" + FullscreenActivity.exportImage + "</exportImage>\n";
        text += "  <exportPDF>" + FullscreenActivity.exportPDF + "</exportPDF>\n";
        text += "  <fabSize>" + FullscreenActivity.fabSize + "</fabSize>\n";
        text += "  <gesture_doubletap>" + FullscreenActivity.gesture_doubletap + "</gesture_doubletap>\n";
        text += "  <gesture_longpress>" + FullscreenActivity.gesture_longpress + "</gesture_longpress>\n";
        text += "  <grouppagebuttons>" + FullscreenActivity.grouppagebuttons + "</grouppagebuttons>\n";
        text += "  <headingfontscalesize>" + FullscreenActivity.headingfontscalesize + "</headingfontscalesize>\n";
        text += "  <highlightShowSecs>" + FullscreenActivity.highlightShowSecs + "</highlightShowSecs>\n";
        text += "  <hideActionBar>" + FullscreenActivity.hideActionBar + "</hideActionBar>\n";
        text += "  <hideLyricsBox>" + FullscreenActivity.hideLyricsBox + "</hideLyricsBox>\n";
        text += "  <languageToLoad>" + FullscreenActivity.languageToLoad + "</languageToLoad>\n";
        text += "  <lastSetName>" + FullscreenActivity.lastSetName + "</lastSetName>\n";
        text += "  <linespacing>" + FullscreenActivity.linespacing + "</linespacing>\n";
        text += "  <locale>" + StaticVariables.locale + "</locale>\n";
        text += "  <longpressdownpedalgesture>" + FullscreenActivity.longpressdownpedalgesture + "</longpressdownpedalgesture>\n";
        text += "  <longpressnextpedalgesture>" + FullscreenActivity.longpressnextpedalgesture + "</longpressnextpedalgesture>\n";
        text += "  <longpresspreviouspedalgesture>" + FullscreenActivity.longpresspreviouspedalgesture + "</longpresspreviouspedalgesture>\n";
        text += "  <longpressuppedalgesture>" + FullscreenActivity.longpressuppedalgesture + "</longpressuppedalgesture>\n";
        text += "  <maxvolrange>" + FullscreenActivity.maxvolrange + "</maxvolrange>\n";
        text += "  <mediaStore>" + FullscreenActivity.mediaStore + "</mediaStore>\n";
        text += "  <menuSize>" + FullscreenActivity.menuSize + "</menuSize>\n";
        text += "  <metronomepan>" + FullscreenActivity.metronomepan + "</metronomepan>\n";
        text += "  <metronomevol>" + FullscreenActivity.metronomevol + "</metronomevol>\n";
        text += "  <midiAuto>" + FullscreenActivity.midiAuto + "</midiAuto>\n";
        text += "  <mAuthor>" + FullscreenActivity.mAuthor + "</mAuthor>\n";
        text += "  <mCopyright>" + FullscreenActivity.mCopyright + "</mCopyright>\n";
        text += "  <mDisplayTheme>" + StaticVariables.mDisplayTheme + "</mDisplayTheme>\n";
        text += "  <mFontSize>" + FullscreenActivity.mFontSize + "</mFontSize>\n";
        text += "  <mMaxFontSize>" + FullscreenActivity.mMaxFontSize + "</mMaxFontSize>\n";
        text += "  <mMinFontSize>" + FullscreenActivity.mMinFontSize + "</mMinFontSize>\n";
        text += "  <mStorage>" + FullscreenActivity.mTitle + "</mStorage>\n";
        text += "  <multilineCompact>" + FullscreenActivity.multilineCompact + "</multilineCompact>\n";
        text += "  <mTitle>" + FullscreenActivity.mTitle + "</mTitle>\n";
        text += "  <myAlert>" + FullscreenActivity.myAlert + "</myAlert>\n";
        text += "  <mychordsfontnum>" + FullscreenActivity.mychordsfontnum + "</mychordsfontnum>\n";
        text += "  <mylyricsfontnum>" + FullscreenActivity.mylyricsfontnum + "</mylyricsfontnum>\n";
        text += "  <mypresofontnum>" + FullscreenActivity.mylyricsfontnum + "</mypresofontnum>\n";
        text += "  <mypresoinfofontnum>" + FullscreenActivity.mypresoinfofontnum + "</mypresoinfofontnum>\n";
        text += "  <mySet>" + StaticVariables.mySet + "</mySet>\n";
        text += "  <override_fullscale>" + FullscreenActivity.override_fullscale + "</override_fullscale>\n";
        text += "  <override_widthscale>" + FullscreenActivity.override_widthscale + "</override_widthscale>\n";
        text += "  <padpan>" + FullscreenActivity.padpan + "</padpan>\n";
        text += "  <padvol>" + FullscreenActivity.padvol + "</padvol>\n";
        text += "  <page_autoscroll_visible>" + FullscreenActivity.page_autoscroll_visible + "</page_autoscroll_visible>\n";
        text += "  <page_chord_visible>" + FullscreenActivity.page_custom_visible + "</page_chord_visible>\n";
        text += "  <page_custom_grouped>" + FullscreenActivity.page_custom_grouped + "</page_custom_grouped>\n";
        text += "  <page_custom_visible>" + FullscreenActivity.page_custom_visible + "</page_custom_visible>\n";
        text += "  <page_custom1_visible>" + FullscreenActivity.page_custom1_visible + "</page_custom1_visible>\n";
        text += "  <page_custom2_visible>" + FullscreenActivity.page_custom2_visible + "</page_custom2_visible>\n";
        text += "  <page_custom3_visible>" + FullscreenActivity.page_custom3_visible + "</page_custom3_visible>\n";
        text += "  <page_custom4_visible>" + FullscreenActivity.page_custom4_visible + "</page_custom4_visible>\n";
        text += "  <page_extra_grouped>" + FullscreenActivity.page_extra_grouped + "</page_extra_grouped>\n";
        text += "  <page_extra_visible>" + FullscreenActivity.page_extra_visible + "</page_extra_visible>\n";
        text += "  <page_highlight_visible>" + FullscreenActivity.page_highlight_visible + "</page_highlight_visible>\n";
        text += "  <page_links_visible>" + FullscreenActivity.page_links_visible + "</page_links_visible>\n";
        text += "  <page_metronome_visible>" + FullscreenActivity.page_metronome_visible + "</page_metronome_visible>\n";
        text += "  <page_notation_visible>" + FullscreenActivity.page_notation_visible + "</page_notation_visible>\n";
        text += "  <page_pad_visible>" + FullscreenActivity.page_pad_visible + "</page_pad_visible>\n";
        text += "  <page_pages_visible>" + FullscreenActivity.page_pages_visible + "</page_pages_visible>\n";
        text += "  <page_set_visible>" + FullscreenActivity.page_set_visible + "</page_set_visible>\n";
        text += "  <page_sticky_visible>" + FullscreenActivity.page_sticky_visible + "</page_sticky_visible>\n";
        text += "  <pagebutton_position>" + FullscreenActivity.pagebutton_position + "</pagebutton_position>\n";
        text += "  <pagebutton_scale>" + FullscreenActivity.pagebutton_scale + "</pagebutton_scale>\n";
        text += "  <pageButtonAlpha>" + FullscreenActivity.pageButtonAlpha + "</pageButtonAlpha>\n";
        text += "  <pedal1>" + FullscreenActivity.pedal1 + "</pedal1>\n";
        text += "  <pedal2>" + FullscreenActivity.pedal2 + "</pedal2>\n";
        text += "  <pedal3>" + FullscreenActivity.pedal3 + "</pedal3>\n";
        text += "  <pedal4>" + FullscreenActivity.pedal4 + "</pedal4>\n";
        text += "  <pedal5>" + FullscreenActivity.pedal5 + "</pedal5>\n";
        text += "  <pedal6>" + FullscreenActivity.pedal6 + "</pedal6>\n";
        text += "  <pedal1shortaction>" + FullscreenActivity.pedal1shortaction + "</pedal1shortaction>\n";
        text += "  <pedal2shortaction>" + FullscreenActivity.pedal2shortaction + "</pedal2shortaction>\n";
        text += "  <pedal3shortaction>" + FullscreenActivity.pedal3shortaction + "</pedal3shortaction>\n";
        text += "  <pedal4shortaction>" + FullscreenActivity.pedal4shortaction + "</pedal4shortaction>\n";
        text += "  <pedal5shortaction>" + FullscreenActivity.pedal5shortaction + "</pedal5shortaction>\n";
        text += "  <pedal6shortaction>" + FullscreenActivity.pedal6shortaction + "</pedal6shortaction>\n";
        text += "  <pedal1longaction>" + FullscreenActivity.pedal1longaction + "</pedal1longaction>\n";
        text += "  <pedal2longaction>" + FullscreenActivity.pedal2longaction + "</pedal2longaction>\n";
        text += "  <pedal3longaction>" + FullscreenActivity.pedal3longaction + "</pedal3longaction>\n";
        text += "  <pedal4longaction>" + FullscreenActivity.pedal4longaction + "</pedal4longaction>\n";
        text += "  <pedal5longaction>" + FullscreenActivity.pedal5longaction + "</pedal5longaction>\n";
        text += "  <pedal6longaction>" + FullscreenActivity.pedal6longaction + "</pedal6longaction>\n";
        text += "  <popupAlpha_Set>" + FullscreenActivity.popupAlpha_Set + "</popupAlpha_Set>\n";
        text += "  <popupDim_All>" + FullscreenActivity.popupDim_All + "</popupDim_All>\n";
        text += "  <popupDim_Set>" + FullscreenActivity.popupDim_Set + "</popupDim_Set>\n";
        text += "  <popupPosition_All>" + FullscreenActivity.popupPosition_All + "</popupPosition_All>\n";
        text += "  <popupPosition_Set>" + FullscreenActivity.popupPosition_Set + "</popupPosition_Set>\n";
        text += "  <popupScale_All>" + FullscreenActivity.popupScale_All + "</popupScale_All>\n";
        text += "  <popupScale_Set>" + FullscreenActivity.popupScale_Set + "</popupScale_Set>\n";
        text += "  <prefChord_Aflat_Gsharp>" + FullscreenActivity.prefChord_Aflat_Gsharp + "</prefChord_Aflat_Gsharp>\n";
        text += "  <prefChord_Bflat_Asharp>" + FullscreenActivity.prefChord_Bflat_Asharp + "</prefChord_Bflat_Asharp>\n";
        text += "  <prefChord_Dflat_Csharp>" + FullscreenActivity.prefChord_Dflat_Csharp + "</prefChord_Dflat_Csharp>\n";
        text += "  <prefChord_Eflat_Dsharp>" + FullscreenActivity.prefChord_Eflat_Dsharp + "</prefChord_Eflat_Dsharp>\n";
        text += "  <prefChord_Gflat_Fsharp>" + FullscreenActivity.prefChord_Gflat_Fsharp + "</prefChord_Gflat_Fsharp>\n";
        text += "  <prefChord_Aflatm_Gsharpm>" + FullscreenActivity.prefChord_Aflatm_Gsharpm + "</prefChord_Aflatm_Gsharpm>\n";
        text += "  <prefChord_Bflatm_Asharpm>" + FullscreenActivity.prefChord_Bflatm_Asharpm + "</prefChord_Bflatm_Asharpm>\n";
        text += "  <prefChord_Dflatm_Csharpm>" + FullscreenActivity.prefChord_Dflatm_Csharpm + "</prefChord_Dflatm_Csharpm>\n";
        text += "  <prefChord_Eflatm_Dsharpm>" + FullscreenActivity.prefChord_Eflatm_Dsharpm + "</prefChord_Eflatm_Dsharpm>\n";
        text += "  <prefChord_Gflatm_Fsharpm>" + FullscreenActivity.prefChord_Gflatm_Fsharpm + "</prefChord_Gflatm_Fsharpm>\n";
        text += "  <presenterChords>" + FullscreenActivity.presenterChords + "</presenterChords>\n";
        text += "  <presoAlertSize>" + FullscreenActivity.presoAlertSize + "</presoAlertSize>\n";
        text += "  <presoAlpha>" + FullscreenActivity.presoAlpha + "</presoAlpha>\n";
        text += "  <presoAuthorSize>" + FullscreenActivity.presoAuthorSize + "</presoAuthorSize>\n";
        text += "  <presoAutoScale>" + FullscreenActivity.presoAutoScale + "</presoAutoScale>\n";
        text += "  <presoFontSize>" + FullscreenActivity.presoFontSize + "</presoFontSize>\n";
        text += "  <presoCopyrightSize>" + FullscreenActivity.presoCopyrightSize + "</presoCopyrightSize>\n";
        text += "  <presoInfoAlign>" + FullscreenActivity.presoInfoAlign + "</presoInfoAlign>\n";
        text += "  <presoLyricsAlign>" + FullscreenActivity.presoLyricsAlign + "</presoLyricsAlign>\n";
        text += "  <presoMaxFontSize>" + FullscreenActivity.presoMaxFontSize + "</presoMaxFontSize>\n";
        text += "  <presoShowChords>" + FullscreenActivity.presoShowChords + "</presoShowChords>\n";
        text += "  <presoTitleSize>" + FullscreenActivity.presoTitleSize + "</presoTitleSize>\n";
        text += "  <presoTransitionTime>" + FullscreenActivity.presoTransitionTime + "</presoTransitionTime>\n";
        text += "  <profile>" + FullscreenActivity.profile + "</profile>\n";
        text += "  <quickLaunchButton_1>" + FullscreenActivity.quickLaunchButton_1 + "</quickLaunchButton_1>\n";
        text += "  <quickLaunchButton_2>" + FullscreenActivity.quickLaunchButton_2 + "</quickLaunchButton_2>\n";
        text += "  <quickLaunchButton_3>" + FullscreenActivity.quickLaunchButton_3 + "</quickLaunchButton_3>\n";
        text += "  <quickLaunchButton_4>" + FullscreenActivity.quickLaunchButton_4 + "</quickLaunchButton_4>\n";
        text += "  <randomFolders>" + FullscreenActivity.randomFolders + "</randomFolders>\n";
        text += "  <scrollDistance>" + FullscreenActivity.scrollDistance + "</scrollDistance>\n";
        text += "  <scrollSpeed>" + FullscreenActivity.scrollSpeed + "</scrollSpeed>\n";
        text += "  <showAlphabeticalIndexInSongMenu>" + FullscreenActivity.showAlphabeticalIndexInSongMenu + "</showAlphabeticalIndexInSongMenu>\n";
        text += "  <showCapoAsNumerals>" + FullscreenActivity.showCapoAsNumerals + "</showCapoAsNumerals>\n";
        text += "  <showCapoChords>" + FullscreenActivity.showCapoChords + "</showCapoChords>\n";
        text += "  <showChords>" + FullscreenActivity.showChords + "</showChords>\n";
        text += "  <showNativeAndCapoChords>" + FullscreenActivity.showNativeAndCapoChords + "</showNativeAndCapoChords>\n";
        text += "  <showNextInSet>" + FullscreenActivity.showNextInSet + "</showNextInSet>\n";
        text += "  <showLyrics>" + FullscreenActivity.showLyrics + "</showLyrics>\n";
        text += "  <showSetTickBoxInSongMenu>" + FullscreenActivity.showSetTickBoxInSongMenu + "</showSetTickBoxInSongMenu>\n";
        text += "  <songfilename>" + StaticVariables.songfilename + "</songfilename>\n";
        text += "  <stagemodeScale>" + FullscreenActivity.stagemodeScale + "</stagemodeScale>\n";
        text += "  <stickyNotesShowSecs>" + FullscreenActivity.stickyNotesShowSecs + "</stickyNotesShowSecs>\n";
        text += "  <stickyOpacity>" + FullscreenActivity.stickyOpacity + "</stickyOpacity>\n";
        text += "  <stickyTextSize>" + FullscreenActivity.stickyTextSize + "</stickyTextSize>\n";
        text += "  <stickyWidth>" + FullscreenActivity.stickyWidth + "</stickyWidth>\n";
        text += "  <SWIPE_MAX_OFF_PATH>" + FullscreenActivity.SWIPE_MAX_OFF_PATH + "</SWIPE_MAX_OFF_PATH>\n";
        text += "  <SWIPE_MIN_DISTANCE>" + FullscreenActivity.SWIPE_MIN_DISTANCE + "</SWIPE_MIN_DISTANCE>\n";
        text += "  <SWIPE_THRESHOLD_VELOCITY>" + FullscreenActivity.SWIPE_THRESHOLD_VELOCITY + "</SWIPE_THRESHOLD_VELOCITY>\n";
        text += "  <swipeDrawer>" + FullscreenActivity.swipeDrawer + "</swipeDrawer>\n";
        text += "  <swipeForMenus>" + FullscreenActivity.swipeForSongs + "</swipeForMenus>\n";
        text += "  <swipeForSongs>" + FullscreenActivity.swipeForSongs + "</swipeForSongs>\n";
        text += "  <swipeSet>" + FullscreenActivity.swipeSet + "</swipeSet>\n";
        text += "  <timerFontSizeAutoScroll>" + FullscreenActivity.timerFontSizeAutoScroll + "</timerFontSizeAutoScroll>\n";
        text += "  <timerFontSizePad>" + FullscreenActivity.timerFontSizePad + "</timerFontSizePad>\n";
        text += "  <toggleAutoHighlight>" + FullscreenActivity.toggleAutoHighlight + "</toggleAutoHighlight>\n";
        text += "  <toggleAutoSticky>" + FullscreenActivity.toggleAutoSticky + "</toggleAutoSticky>\n";
        text += "  <togglePageButtons>" + FullscreenActivity.togglePageButtons + "</togglePageButtons>\n";
        text += "  <toggleScrollArrows>" + FullscreenActivity.toggleScrollArrows + "</toggleScrollArrows>\n";
        text += "  <toggleScrollBeforeSwipe>" + FullscreenActivity.toggleScrollBeforeSwipe + "</toggleScrollBeforeSwipe>\n";
        text += "  <toggleYScale>" + FullscreenActivity.toggleYScale + "</toggleYScale>\n";
        text += "  <transposeStyle>" + FullscreenActivity.transposeStyle + "</transposeStyle>\n";
        text += "  <trimLines>" + FullscreenActivity.trimLines + "</trimLines>\n";
        text += "  <trimSections>" + FullscreenActivity.trimSections + "</trimSections>\n";
        text += "  <trimSectionSpace>" + FullscreenActivity.trimSectionSpace + "</trimSectionSpace>\n";
        text += "  <usePresentationOrder>" + FullscreenActivity.usePresentationOrder + "</usePresentationOrder>\n";
        text += "  <visualmetronome>" + FullscreenActivity.visualmetronome + "</visualmetronome>\n";
        //text += "  <whichMode>" + FullscreenActivity.whichMode + "</whichMode>\n";
        text += "  <whichSetCategory>" + FullscreenActivity.whichSetCategory + "</whichSetCategory>\n";
        text += "  <whichSongFolder>" + StaticVariables.whichSongFolder + "</whichSongFolder>\n";
        text += "  <xmargin_presentation>" + FullscreenActivity.xmargin_presentation + "</xmargin_presentation>\n";
        text += "  <ymargin_presentation>" + FullscreenActivity.visualmetronome + "</ymargin_presentation>\n";
        text += "</myprofile>";
        */
        return text;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}