package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;

public class PopUpProfileFragment extends DialogFragment {

    static PopUpProfileFragment newInstance() {
        PopUpProfileFragment frag;
        frag = new PopUpProfileFragment();
        return frag;
    }

    public interface MyInterface {
        void refreshAll();
        void setupPageButtons(String s);
        void showpagebuttons();
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

    File location;
    File[] tempmyFiles;
    String[] foundFiles;
    ArrayList<String> tempFoundFiles;
    String[] filechecks;
    Collator coll;
    ScrollView profile_overview;
    RelativeLayout profile_load;
    RelativeLayout profile_save;
    TextView profileName_TextView;
    EditText profileName_EditText;
    ListView profileFilesLoad_ListView;
    ListView profileFilesSave_ListView;
    Button loadProfile_Button;
    Button saveProfile_Button;
    Button closeProfile_Button;
    Button cancelSave_Button;
    Button okSave_Button;
    Button cancelLoad_Button;
    String name;
    String what = "overview";

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getResources().getString(R.string.profile));
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_profile, container, false);

        // Initialise the views
        profile_overview = (ScrollView) V.findViewById(R.id.profile_overview);
        profile_load = (RelativeLayout) V.findViewById(R.id.profile_load);
        profile_save = (RelativeLayout) V.findViewById(R.id.profile_save);
        profileName_TextView = (TextView) V.findViewById(R.id.profileName_TextView);
        profileName_EditText = (EditText) V.findViewById(R.id.profileName_EditText);
        profileFilesLoad_ListView = (ListView) V.findViewById(R.id.profileFilesLoad_ListView);
        profileFilesSave_ListView = (ListView) V.findViewById(R.id.profileFilesSave_ListView);
        loadProfile_Button = (Button) V.findViewById(R.id.loadProfile_Button);
        saveProfile_Button = (Button) V.findViewById(R.id.saveProfile_Button);
        closeProfile_Button = (Button) V.findViewById(R.id.closeProfile_Button);
        cancelSave_Button = (Button) V.findViewById(R.id.cancelSave_Button);
        okSave_Button = (Button) V.findViewById(R.id.okSave_Button);
        cancelLoad_Button = (Button) V.findViewById(R.id.cancelLoad_Button);

        // Only show the first view with profile name and options to load or save or reset
        showOverView();

        // Set the profile name if it exists
        if (FullscreenActivity.profile.equals("")) {
            name = getActivity().getString(R.string.options_song_new);
        } else {
            name = FullscreenActivity.profile;
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
        closeProfile_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        // Set up listeners for the save page
        okSave_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contents = prepareProfile();
                name = profileName_EditText.getText().toString();
                if (!name.equals("")) {
                    File myfile = new File(FullscreenActivity.dirprofiles + "/" + name);
                    try {
                        FileOutputStream overWrite = new FileOutputStream(myfile, false);
                        overWrite.write(contents.getBytes());
                        overWrite.flush();
                        overWrite.close();
                        FullscreenActivity.myToastMessage = getString(R.string.ok);
                        FullscreenActivity.profile = name;
                        profileName_TextView.setText(name);
                        profileName_EditText.setText(name);


                    } catch (Exception e) {
                        e.printStackTrace();
                        FullscreenActivity.myToastMessage = getString(R.string.profile) + " " +
                                getString(R.string.hasnotbeenexported);
                    }
                } else {
                    FullscreenActivity.myToastMessage = getString(R.string.profile) + " " +
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
        return V;
    }

    public void showOverView() {
        profile_overview.setVisibility(View.VISIBLE);
        profile_load.setVisibility(View.GONE);
        profile_save.setVisibility(View.GONE);
        what = "overview";
    }

    public void showLoad() {
        profile_overview.setVisibility(View.GONE);
        profile_load.setVisibility(View.VISIBLE);
        profile_save.setVisibility(View.GONE);
        what = "load";
        setupProfileList();
    }

    public void showSave() {
        profile_overview.setVisibility(View.GONE);
        profile_load.setVisibility(View.GONE);
        profile_save.setVisibility(View.VISIBLE);
        what = "save";
        setupProfileList();
    }

    public void setupProfileList() {
        location = new File(FullscreenActivity.homedir + "/Profiles");
        tempmyFiles = location.listFiles();
        tempFoundFiles = new ArrayList<>();

        // Go through each file
        for (File tempmyFile : tempmyFiles) {

            // If we need to check the filetype and it is ok, add it to the array
            if (filechecks != null && filechecks.length > 0) {
                for (String filecheck : filechecks) {
                    if (tempmyFile!=null && tempmyFile.getName().contains(filecheck) && !tempmyFile.isDirectory()) {
                        tempFoundFiles.add(tempmyFile.getName());
                    }
                }

                // Otherwise, no check needed, add to the array (if it isn't a directory)
            } else {
                if (tempmyFile!=null && !tempmyFile.isDirectory()) {
                    tempFoundFiles.add(tempmyFile.getName());
                }
            }
        }

        // Sort the array list alphabetically by locale rules
        // Add locale sort
        coll = Collator.getInstance(FullscreenActivity.locale);
        coll.setStrength(Collator.SECONDARY);
        Collections.sort(tempFoundFiles, coll);

        // Convert arraylist to string array
        foundFiles = new String[tempFoundFiles.size()];
        foundFiles = tempFoundFiles.toArray(foundFiles);

        Log.d("d","what="+what);

        // Add the saved profiles to the listview
        // Populate the file list view
        if (what.equals("save")) {
            profileFilesSave_ListView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, foundFiles));
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
            profileFilesLoad_ListView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, foundFiles));
            profileFilesLoad_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        FullscreenActivity.profile = foundFiles[position];
                        grabvalues(FullscreenActivity.dirprofiles + "/" + foundFiles[position]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void grabvalues(String file) throws Exception {
        // Extract all of the key bits of the profile
        XmlPullParserFactory factory;
        factory = XmlPullParserFactory.newInstance();

        factory.setNamespaceAware(true);
        XmlPullParser xpp;
        xpp = factory.newPullParser();

        InputStream inputStream = new FileInputStream(file);
        xpp.setInput(inputStream,null);

        int eventType;
        eventType = xpp.getEventType();
        String text;
        int integer;
        float floatval;
        boolean trueorfalse;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (xpp.getName().equals("pagebutton_position")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        FullscreenActivity.pagebutton_position = text;
                    }
                } else if (xpp.getName().equals("pagebutton_scale")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        FullscreenActivity.pagebutton_scale = text;
                    }
                } else if (xpp.getName().equals("toggleAutoSticky")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        FullscreenActivity.toggleAutoSticky = text;
                    }
                } else if (xpp.getName().equals("toggleScrollArrows")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                            FullscreenActivity.toggleScrollArrows = text;
                    }
                } else if (xpp.getName().equals("capoDisplay")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        FullscreenActivity.capoDisplay = text;
                    }
                } else if (xpp.getName().equals("mylyricsfontnum")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        integer = Integer.parseInt(text);
                        FullscreenActivity.mylyricsfontnum = integer;
                    }
                } else if (xpp.getName().equals("mychordsfontnum")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        integer = Integer.parseInt(text);
                        FullscreenActivity.mychordsfontnum = integer;
                    }
                } else if (xpp.getName().equals("linespacing")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        integer = Integer.parseInt(text);
                        FullscreenActivity.linespacing = integer;
                    }
                } else if (xpp.getName().equals("togglePageButtons")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        FullscreenActivity.togglePageButtons = text;
                    }
                } else if (xpp.getName().equals("swipeDrawer")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        FullscreenActivity.swipeDrawer = text;
                    }
                } else if (xpp.getName().equals("autostartautoscroll")) {
                    text = xpp.nextText();
                    trueorfalse = text.equals("true");
                    FullscreenActivity.autostartautoscroll = trueorfalse;

                } else if (xpp.getName().equals("visualmetronome")) {
                    text = xpp.nextText();
                    trueorfalse = text.equals("true");
                    FullscreenActivity.visualmetronome = trueorfalse;
                } else if (xpp.getName().equals("mFontSize")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        floatval = Float.parseFloat(text);
                        FullscreenActivity.mFontSize = floatval;
                    }
                } else if (xpp.getName().equals("commentfontscalesize")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        floatval = Float.parseFloat(text);
                        FullscreenActivity.commentfontscalesize = floatval;
                    }
                } else if (xpp.getName().equals("headingfontscalesize")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        floatval = Float.parseFloat(text);
                        FullscreenActivity.headingfontscalesize = floatval;
                    }
                } else if (xpp.getName().equals("mMaxFontSize")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        integer = Integer.parseInt(text);
                        FullscreenActivity.mMaxFontSize = integer;
                    }
                } else if (xpp.getName().equals("mMinFontSize")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        integer = Integer.parseInt(text);
                        FullscreenActivity.mMinFontSize = integer;
                    }
                } else if (xpp.getName().equals("override_fullscale")) {
                    text = xpp.nextText();
                    trueorfalse = text.equals("true");
                    FullscreenActivity.override_fullscale = trueorfalse;
                } else if (xpp.getName().equals("override_widthscale")) {
                    text = xpp.nextText();
                    trueorfalse = text.equals("true");
                    FullscreenActivity.override_widthscale = trueorfalse;
                } else if (xpp.getName().equals("usePresentationOrder")) {
                    text = xpp.nextText();
                    trueorfalse = text.equals("true");
                    FullscreenActivity.usePresentationOrder = trueorfalse;
                } else if (xpp.getName().equals("toggleYScale")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        FullscreenActivity.toggleYScale = text;
                    }
                } else if (xpp.getName().equals("swipeSet")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        FullscreenActivity.swipeSet = text;
                    }
                } else if (xpp.getName().equals("hideactionbaronoff")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        FullscreenActivity.hideActionBar = text.equals("true");
                    }
                } else if (xpp.getName().equals("hideActionBar")) {
                    text = xpp.nextText();
                    FullscreenActivity.hideActionBar = text != null && !text.equals("") && text.equals("true");
                } else if (xpp.getName().equals("swipeForMenus")) {
                    text = xpp.nextText();
                    FullscreenActivity.swipeForMenus = text != null && !text.equals("") && text.equals("true");
                } else if (xpp.getName().equals("swipeForSongs")) {
                    text = xpp.nextText();
                    FullscreenActivity.swipeForSongs = text != null && !text.equals("") && text.equals("true");
                } else if (xpp.getName().equals("transposeStyle")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        FullscreenActivity.transposeStyle = text;
                    }
                } else if (xpp.getName().equals("showChords")) {
                    text = xpp.nextText();
                    FullscreenActivity.showChords = text != null && !text.equals("") && text.equals("true");
                } else if (xpp.getName().equals("mDisplayTheme")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        FullscreenActivity.mDisplayTheme = text;
                    }
                } else if (xpp.getName().equals("chordInstrument")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        FullscreenActivity.chordInstrument = text;
                    }
                } else if (xpp.getName().equals("popupScale_All")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        FullscreenActivity.popupScale_All = Float.parseFloat(text.replace("f",""));
                    }
                } else if (xpp.getName().equals("popupScale_Set")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        FullscreenActivity.popupScale_Set = Float.parseFloat(text.replace("f",""));
                    }
                } else if (xpp.getName().equals("popupDim_All")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        FullscreenActivity.popupDim_All = Float.parseFloat(text.replace("f",""));
                    }
                } else if (xpp.getName().equals("popupDim_Set")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        FullscreenActivity.popupDim_Set = Float.parseFloat(text.replace("f",""));
                    }
                } else if (xpp.getName().equals("popupAlpha_All")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        FullscreenActivity.popupAlpha_All = Float.parseFloat(text.replace("f",""));
                    }
                } else if (xpp.getName().equals("popupAlpha_Set")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        FullscreenActivity.popupAlpha_Set = Float.parseFloat(text.replace("f",""));
                    }
                } else if (xpp.getName().equals("popupPosition_All")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        FullscreenActivity.popupPosition_All = text;
                    }
                } else if (xpp.getName().equals("popupPosition_Set")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        FullscreenActivity.popupPosition_Set = text;
                    }
                } else if (xpp.getName().equals("pageButtonAlpha")) {
                    text = xpp.nextText();
                    if (text!=null && !text.equals("")) {
                        FullscreenActivity.pageButtonAlpha = Float.parseFloat(text.replace("f",""));
                    }
                }

            }

            try {
                eventType = xpp.next();
            } catch (Exception e) {
                //Ooops!
                Log.d("d","error in file, or not xml");
            }
        }

        // Save the new preferences
        Preferences.savePreferences();

        // Reload the display
        dismiss();
        mListener.refreshAll();
        mListener.setupPageButtons("");
        mListener.showpagebuttons();
    }

    public String prepareProfile() {
        String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        text += "<profile>\n";
        text += "  <pagebutton_position>" + FullscreenActivity.pagebutton_position + "</pagebutton_position>\n";
        text += "  <pagebutton_scale>" + FullscreenActivity.pagebutton_scale + "</pagebutton_scale>\n";
        text += "  <toggleAutoSticky>" + FullscreenActivity.toggleAutoSticky + "</toggleAutoSticky>\n";
        text += "  <toggleScrollArrows>" + FullscreenActivity.toggleScrollArrows + "</toggleScrollArrows>\n";
        text += "  <capoDisplay>" + FullscreenActivity.capoDisplay + "</capoDisplay>\n";
        text += "  <mylyricsfontnum>" + FullscreenActivity.mylyricsfontnum + "</mylyricsfontnum>\n";
        text += "  <mychordsfontnum>" + FullscreenActivity.mychordsfontnum + "</mychordsfontnum>\n";
        text += "  <linespacing>" + FullscreenActivity.linespacing + "</linespacing>\n";
        text += "  <togglePageButtons>" + FullscreenActivity.togglePageButtons + "</togglePageButtons>\n";
        text += "  <swipeDrawer>" + FullscreenActivity.swipeDrawer + "</swipeDrawer>\n";
        text += "  <autostartautoscroll>" + FullscreenActivity.autostartautoscroll + "</autostartautoscroll>\n";
        text += "  <visualmetronome>" + FullscreenActivity.visualmetronome + "</visualmetronome>\n";
        text += "  <mFontSize>" + FullscreenActivity.mFontSize + "</mFontSize>\n";
        text += "  <commentfontscalesize>" + FullscreenActivity.commentfontscalesize + "</commentfontscalesize>\n";
        text += "  <headingfontscalesize>" + FullscreenActivity.headingfontscalesize + "</headingfontscalesize>\n";
        text += "  <mMaxFontSize>" + FullscreenActivity.mMaxFontSize + "</mMaxFontSize>\n";
        text += "  <mMinFontSize>" + FullscreenActivity.mMinFontSize + "</mMinFontSize>\n";
        text += "  <override_fullscale>" + FullscreenActivity.override_fullscale + "</override_fullscale>\n";
        text += "  <override_widthscale>" + FullscreenActivity.override_widthscale + "</override_widthscale>\n";
        text += "  <usePresentationOrder>" + FullscreenActivity.usePresentationOrder + "</usePresentationOrder>\n";
        text += "  <toggleYScale>" + FullscreenActivity.toggleYScale + "</toggleYScale>\n";
        text += "  <swipeSet>" + FullscreenActivity.swipeSet + "</swipeSet>\n";
        text += "  <hideActionBar>" + FullscreenActivity.hideActionBar + "</hideActionBar>\n";
        text += "  <transposeStyle>" + FullscreenActivity.transposeStyle + "</transposeStyle>\n";
        text += "  <showChords>" + FullscreenActivity.showChords + "</showChords>\n";
        text += "  <mDisplayTheme>" + FullscreenActivity.mDisplayTheme + "</mDisplayTheme>\n";
        text += "  <chordInstrument>" + FullscreenActivity.chordInstrument + "</chordInstrument>\n";
        text += "  <popupScale_All>" + FullscreenActivity.popupScale_All + "</popupScale_All>\n";
        text += "  <popupAlpha_All>" + FullscreenActivity.popupAlpha_All + "</popupAlpha_All>\n";
        text += "  <popupDim_All>" + FullscreenActivity.popupDim_All + "</popupDim_All>\n";
        text += "  <popupPosition_All>" + FullscreenActivity.popupPosition_All + "</popupPosition_All>\n";
        text += "  <popupScale_Set>" + FullscreenActivity.popupScale_Set + "</popupScale_Set>\n";
        text += "  <popupAlpha_Set>" + FullscreenActivity.popupAlpha_Set + "</popupAlpha_Set>\n";
        text += "  <popupDim_Set>" + FullscreenActivity.popupDim_Set + "</popupDim_Set>\n";
        text += "  <popupPosition_Set>" + FullscreenActivity.popupPosition_Set + "</popupPosition_Set>\n";
        text += "  <pageButtonAlpha>" + FullscreenActivity.pageButtonAlpha + "</pageButtonAlpha>\n";
        text += "</profile>";
        return text;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}