package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class PopUpListSetsFragment extends DialogFragment {

    static PopUpListSetsFragment newInstance() {
        PopUpListSetsFragment frag;
        frag = new PopUpListSetsFragment();
        return frag;
    }

    TextView title;
    EditText setListName;
    TextView newSetPromptTitle;
    Spinner setCategory_Spinner;
    Spinner oldCategory_Spinner;
    ImageButton newCategory_ImageButton;
    EditText newCategory_EditText;
    RelativeLayout setCategory;
    TextView setCategory_TextView;
    RelativeLayout filelist_RelativeLayout;
    LinearLayout oldCategory_LinearLayout;
    LinearLayout newCategory_LinearLayout;
    LinearLayout newSetTitle_LinearLayout;
    ImageButton sort_ImageButton;
    CheckBox overWrite_CheckBox;
    static String myTitle;
    static FetchDataTask dataTask;
    static ProgressDialog prog;
    public static String val;
    public static Handler mHandler;
    public static Runnable runnable;
    public static String[] setnames;
    public static ArrayAdapter<String> adapter;
    public ListView setListView1;
    ArrayList<String> cats = new ArrayList<>();
    String[] filteredsetnames;

    public interface MyInterface {
        void refreshAll();
        void openFragment();
        void confirmedAction();
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

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        try {
            dataTask.cancel(true);
        } catch (Exception e) {
            // Don't worry
        }

        try {
            dataTask = null;
        } catch (Exception e) {
            // Don't worry
        }
    }

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

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        myTitle = getActivity().getResources().getString(R.string.options_set);

        switch (FullscreenActivity.whattodo) {
            default:
            case "loadset":
                myTitle = myTitle + " - " + getActivity().getResources().getString(R.string.options_set_load);
                break;

            case "saveset":
                myTitle = myTitle + " - " + getActivity().getResources().getString(R.string.options_set_save);
                break;

            case "deleteset":
                myTitle = myTitle + " - " + getActivity().getResources().getString(R.string.options_set_delete);
                break;

            case "exportset":
                myTitle = myTitle + " - " + getActivity().getResources().getString(R.string.options_set_export);
                break;

            case "managesets":
                myTitle = myTitle + " - " + getActivity().getResources().getString(R.string.managesets);
                break;

        }

        final View V = inflater.inflate(R.layout.popup_setlists, container, false);

        title = V.findViewById(R.id.dialogtitle);
        title.setText(myTitle);
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                FullscreenActivity.myToastMessage = "";
                dismiss();
            }
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(saveMe,getActivity());
                //saveMe.setEnabled(false);
                doAction();
            }
        });

        // Prepare the toast message using the title.  It is cleared if cancel is clicked
        FullscreenActivity.myToastMessage = myTitle + " : " + getActivity().getResources().getString(R.string.ok);

        // Reset the setname chosen
        FullscreenActivity.setnamechosen = "";
        FullscreenActivity.abort = false;

        // Get a note of the available sets
        SetActions.updateOptionListSets();

        setListView1 = V.findViewById(R.id.setListView1);
        setListName = V.findViewById(R.id.setListName);
        newSetPromptTitle = V.findViewById(R.id.newSetPromptTitle);
        oldCategory_Spinner = V.findViewById(R.id.oldCategory_Spinner);
        setCategory_Spinner = V.findViewById(R.id.setCategory_Spinner);
        newCategory_ImageButton = V.findViewById(R.id.newCategory_ImageButton);
        newCategory_EditText = V.findViewById(R.id.newCategory_EditText);
        setCategory_TextView = V.findViewById(R.id.setCategory_TextView);
        setCategory = V.findViewById(R.id.setCategory);
        filelist_RelativeLayout = V.findViewById(R.id.filelist_RelativeLayout);
        oldCategory_LinearLayout = V.findViewById(R.id.oldCategory_LinearLayout);
        newCategory_LinearLayout = V.findViewById(R.id.newCategory_LinearLayout);
        newSetTitle_LinearLayout = V.findViewById(R.id.newSetTitle_LinearLayout);
        overWrite_CheckBox = V.findViewById(R.id.overWrite_CheckBox);
        setListName.setText(FullscreenActivity.lastSetName);
        sort_ImageButton = V.findViewById(R.id.sort_ImageButton);

        // Sort the available set lists
        sortSetLists();


        // Customise the view depending on what we are doing
        adapter = null;

        switch (FullscreenActivity.whattodo) {
            default:
            case "loadset":
                filelist_RelativeLayout.setVisibility(View.VISIBLE);
                oldCategory_LinearLayout.setVisibility(View.GONE);
                newCategory_LinearLayout.setVisibility(View.VISIBLE);
                newSetTitle_LinearLayout.setVisibility(View.GONE);
                newCategory_ImageButton.setVisibility(View.GONE);
                newCategory_EditText.setVisibility(View.GONE);
                overWrite_CheckBox.setVisibility(View.VISIBLE);
                break;

            case "saveset":
                filelist_RelativeLayout.setVisibility(View.VISIBLE);
                oldCategory_LinearLayout.setVisibility(View.GONE);
                newCategory_LinearLayout.setVisibility(View.VISIBLE);
                newSetTitle_LinearLayout.setVisibility(View.VISIBLE);
                newCategory_EditText.setVisibility(View.GONE);
                overWrite_CheckBox.setVisibility(View.GONE);
                break;

            case "deleteset":
                filelist_RelativeLayout.setVisibility(View.VISIBLE);
                oldCategory_LinearLayout.setVisibility(View.GONE);
                newCategory_LinearLayout.setVisibility(View.VISIBLE);
                newSetTitle_LinearLayout.setVisibility(View.GONE);
                setListName.setVisibility(View.GONE);
                newSetPromptTitle.setVisibility(View.GONE);
                newCategory_ImageButton.setVisibility(View.GONE);
                newCategory_EditText.setVisibility(View.GONE);
                overWrite_CheckBox.setVisibility(View.GONE);
                break;

            case "exportset":
                oldCategory_LinearLayout.setVisibility(View.GONE);
                newSetTitle_LinearLayout.setVisibility(View.GONE);
                setListName.setVisibility(View.GONE);
                newSetPromptTitle.setVisibility(View.GONE);
                newCategory_ImageButton.setVisibility(View.GONE);
                newCategory_EditText.setVisibility(View.GONE);
                overWrite_CheckBox.setVisibility(View.GONE);
                break;

            case "managesets":
                setListView1.setVisibility(View.VISIBLE);
                setCategory_TextView.setText(getActivity().getString(R.string.new_category));
                setCategory.setVisibility(View.VISIBLE);
                newSetPromptTitle.setVisibility(View.VISIBLE);
                setListName.setVisibility(View.VISIBLE);
                newCategory_LinearLayout.setVisibility(View.VISIBLE);
                newSetTitle_LinearLayout.setVisibility(View.VISIBLE);
                newCategory_EditText.setVisibility(View.GONE);
                overWrite_CheckBox.setVisibility(View.VISIBLE);
                break;
        }


        // Set The Adapter
        setCorrectAdapter(setnames);

        // Set the category filter
        setCategory_Spinner.setAdapter(setCategories());
        oldCategory_Spinner.setAdapter(setCategories());

        // Look for whichSetCategory and set the spinners
        whichSetCategory();

        if (!FullscreenActivity.whattodo.equals("managesets")) {
            setCategory_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    filterByCategory(i);
                    FullscreenActivity.whichSetCategory = cats.get(i);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }

        if (FullscreenActivity.whattodo.equals("managesets")) {
            oldCategory_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    setCategory_Spinner.setSelection(i);
                    filterByCategory(i);
                    FullscreenActivity.whichSetCategory = cats.get(i);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });
        }


        setListView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the name of the set to do stuff with
                // Since we can select multiple sets, check it isn't already in the setnamechosen field

                // Get the set name
                String msetname = filteredsetnames[position];
                // If we have a category selected, add this to the file name

                if (!FullscreenActivity.whattodo.equals("managesets") && setCategory_Spinner.getSelectedItemPosition()>0) {
                    msetname = cats.get(setCategory_Spinner.getSelectedItemPosition()) + "__" + msetname;
                }

                if (FullscreenActivity.whattodo.equals("managesets") && oldCategory_Spinner.getSelectedItemPosition()>0) {
                    msetname = cats.get(oldCategory_Spinner.getSelectedItemPosition()) + "__" + msetname;
                }

                if (FullscreenActivity.whattodo.equals("exportset")) {
                    FullscreenActivity.setnamechosen = msetname + "%_%";
                } else {
                    if (!FullscreenActivity.setnamechosen.contains(msetname)) {
                        // Add it to the setnamechosen
                        FullscreenActivity.setnamechosen = FullscreenActivity.setnamechosen + msetname + "%_%";
                    } else {
                        // Remove it from the setnamechosen
                        FullscreenActivity.setnamechosen = FullscreenActivity.setnamechosen.replace(msetname + "%_%", "");
                    }
                }
                setListName.setText(filteredsetnames[position]);
            }
        });

        newCategory_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Change button function and image
                changeCategoryButton(true);
            }
        });

        sort_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (FullscreenActivity.sortAlphabetically) {
                    FullscreenActivity.sortAlphabetically = false;
                    sortFilteredSetLists();
                } else {
                    FullscreenActivity.sortAlphabetically = true;
                    sortFilteredSetLists();
                }
            }
        });

        dataTask = new FetchDataTask();

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());

        return V;
    }

    public void doAction() {
        if (FullscreenActivity.setnamechosen.endsWith("%_%")) {
            FullscreenActivity.setnamechosen = FullscreenActivity.setnamechosen.substring(0,FullscreenActivity.setnamechosen.length()-3);
        }

        if (FullscreenActivity.whattodo.equals("loadset") && !FullscreenActivity.setnamechosen.isEmpty() && !FullscreenActivity.setnamechosen.equals("")) {
            doLoadSet();
        } else if (FullscreenActivity.whattodo.equals("saveset") && !setListName.getText().toString().trim().isEmpty() && !setListName.getText().toString().trim().equals("")) {
            doSaveSet();
        } else if (FullscreenActivity.whattodo.equals("deleteset") && !FullscreenActivity.setnamechosen.isEmpty() && !FullscreenActivity.setnamechosen.equals("")) {
            doDeleteSet();
        } else if (FullscreenActivity.whattodo.equals("exportset") && !FullscreenActivity.setnamechosen.isEmpty() && !FullscreenActivity.setnamechosen.equals("")) {
            FullscreenActivity.settoload = FullscreenActivity.setnamechosen;
            doExportSet();
        } else if (FullscreenActivity.whattodo.equals("managesets")) {
            if (!FullscreenActivity.setnamechosen.equals("") && !setListName.getText().toString().equals("")) {
                doRenameSet();
            } else {
                FullscreenActivity.myToastMessage = getActivity().getString(R.string.error_notset);
            }
        }
    }

    public void whichSetCategory() {
        // Try to set the spinners to match the recently used set category
        boolean done = false;
        for (int i=0;i<cats.size();i++) {
            if (cats.get(i).equals(FullscreenActivity.whichSetCategory)) {
                oldCategory_Spinner.setSelection(i);
                setCategory_Spinner.setSelection(i);
                done = true;
            }
        }
        if (!done) {
            // Can't find the set category, so default to the MAIN one (position 0)
            oldCategory_Spinner.setSelection(0);
            setCategory_Spinner.setSelection(0);
        }
    }

    public void changeCategoryButton(boolean makedelete) {
        if (makedelete) {
            setCategory_Spinner.setVisibility(View.GONE);
            newCategory_EditText.setVisibility(View.VISIBLE);
            newCategory_ImageButton.setImageResource(R.drawable.ic_delete_white_36dp);
            newCategory_ImageButton.setVisibility(View.VISIBLE);
            newCategory_ImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    changeCategoryButton(false);
                }
            });
        } else {
            setCategory_Spinner.setVisibility(View.VISIBLE);
            newCategory_EditText.setVisibility(View.GONE);
            newCategory_EditText.setText("");
            newCategory_ImageButton.setImageResource(R.drawable.ic_plus_white_36dp);
            newCategory_ImageButton.setVisibility(View.VISIBLE);
            newCategory_ImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    changeCategoryButton(true);
                }
            });
        }
    }

    public ArrayAdapter<String> setCategories() {
        // Go through the available sets and only show those matching the filter
        // Set categories are identified by mycategory__setname
        // Those with no category are in the main category
        cats = new ArrayList<>();
        String log = "";
        for (int w=0;w<FullscreenActivity.mySetsFileNames.length;w++) {
            if (FullscreenActivity.mySetsFileNames[w].contains("__")) {
                // Split it into category and set name;
                String[] msplit = FullscreenActivity.mySetsFileNames[w].split("__");
                if (!log.contains(msplit[0])) {
                    log += msplit[0] + ",";
                }
            }
        }
        // Now split the log into available categories
        String[] categoriesfound = log.split(",");
        for (String s:categoriesfound) {
            if (!s.equals("")) {
                cats.add(s);
            }
        }

        // Sort the categories alphabetically using locale
        Collator coll = Collator.getInstance(FullscreenActivity.locale);
        coll.setStrength(Collator.SECONDARY);
        Collections.sort(cats, coll);

        cats.add(0,FullscreenActivity.mainfoldername);

        ArrayAdapter<String> myadapter = new ArrayAdapter<>(getActivity(),R.layout.my_spinner,cats);
        myadapter.setDropDownViewResource(R.layout.my_spinner);

        return myadapter;
    }

    public void filterByCategory(int i) {
        // Get the text to filter the sets by
        String filter = cats.get(i);
        ArrayList<String> filtered = new ArrayList<>();

        // Go through the setnames list and only show the available ones
        for (String setname : setnames) {
            if (setname != null && setname.contains(filter + "__")) {
                String addthis = setname.replace(filter + "__", "");
                filtered.add(addthis);
            } else if (filter.equals(FullscreenActivity.mainfoldername)) {
                if (setname != null && !setname.contains("__")) {
                    filtered.add(setname);
                }
            }
        }

        // Sort the categories alphabetically using locale
        Collator coll = Collator.getInstance(FullscreenActivity.locale);
        coll.setStrength(Collator.SECONDARY);
        Collections.sort(filtered, coll);

        filteredsetnames = new String[filtered.size()];

        filteredsetnames = filtered.toArray(filteredsetnames);

        setCorrectAdapter(filteredsetnames);

        // Go through new list and re tick any currently selected ones
        if (FullscreenActivity.whattodo.equals("loadset")) {
            tickSelectedSetsInCategory(filter);
        }
    }

    public void setCorrectAdapter(String[] setstoshow) {
        switch (FullscreenActivity.whattodo) {
            default:
                adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, setstoshow);
                setListView1.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                break;

            case "saveset":
                adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, setstoshow);
                setListView1.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                break;

            case "deleteset":
                adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, setstoshow);
                setListView1.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                break;

            case "exportset":
                adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_checked, setstoshow);
                setListView1.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                break;

            case "managesets":
                adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_checked, setstoshow);
                setListView1.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                break;

        }
        adapter.notifyDataSetChanged();
        setListView1.setAdapter(adapter);
    }

    public void tickSelectedSetsInCategory(String filter) {
        if (filter.equals(FullscreenActivity.mainfoldername)) {
            filter="";
        } else {
            filter=filter+"__";
        }

        for (int f=0;f<filteredsetnames.length;f++) {
            if (FullscreenActivity.setnamechosen.contains(filter+filteredsetnames[f])) {
                setListView1.setItemChecked(f,true);
            } else {
                setListView1.setItemChecked(f,false);
            }
        }
    }

    public void doLoadSet() {
        // Load the set up
        // Show the progress bar
        prog = null;
        prog = new ProgressDialog(getActivity()); //Assuming that you are using fragments.
        prog.setTitle(getString(R.string.options_set_load));
        prog.setMessage(getString(R.string.wait));
        prog.setCancelable(true);
        prog.setIndeterminate(true);
        prog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        prog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                FullscreenActivity.abort = true;
                try {
                    if (dataTask!=null) {
                        dataTask.cancel(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mHandler = new Handler();
        runnable = new Runnable() {
            public void run() {
                prog.setMessage(val);
            }
        };
        prog.show();

        FullscreenActivity.settoload = null;
        FullscreenActivity.abort = false;

        FullscreenActivity.settoload = FullscreenActivity.setnamechosen;
        FullscreenActivity.lastSetName = setListName.getText().toString();

        dataTask = null;
        dataTask = new FetchDataTask();
        try {
            dataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Log.d("d","Error getting data");
        }
    }

    public void doSaveSet() {
        // Save the set into the settoload name
        FullscreenActivity.settoload = setListName.getText().toString().trim();
        FullscreenActivity.lastSetName = setListName.getText().toString().trim();
        String new_cat = newCategory_EditText.getText().toString();

        if (!new_cat.equals("")) {
            FullscreenActivity.settoload = new_cat + "__" + setListName.getText().toString().trim();
        } else if (setCategory_Spinner.getSelectedItemPosition()>0) {
            FullscreenActivity.settoload = cats.get(setCategory_Spinner.getSelectedItemPosition()) +
                    "__" + setListName.getText().toString().trim();
        }

        // Popup the are you sure alert into another dialog fragment
        File newsetname = new File(FullscreenActivity.dirsets + "/" +
                FullscreenActivity.settoload);

        // New structure, only give the are you sure prompt if the set name already exists.
        if (newsetname.exists()) {
            String message = getResources().getString(R.string.options_set_save) + " \'" + setListName.getText().toString().trim() + "\"?";
            FullscreenActivity.myToastMessage = message;
            DialogFragment newFragment = PopUpAreYouSureFragment.newInstance(message);
            newFragment.show(getFragmentManager(), "dialog");
            dismiss();
        } else {
            if (mListener!=null) {
                FullscreenActivity.whattodo = "saveset";
                mListener.confirmedAction();
            }
            try {
                dismiss();
            } catch (Exception e) {
                Log.d("d","Error closing");
            }
        }


        /*if (newsetname.exists() && !overWrite_CheckBox.isChecked()) {
            FullscreenActivity.myToastMessage = getActivity().getString(R.string.renametitle) + " - " +
                    getActivity().getString(R.string.file_exists);

            ShowToast.showToast(getActivity());
        } else {
            String message = getResources().getString(R.string.options_set_save) + " \'" + setListName.getText().toString().trim() + "\"?";
            FullscreenActivity.myToastMessage = message;
            DialogFragment newFragment = PopUpAreYouSureFragment.newInstance(message);
            newFragment.show(getFragmentManager(), "dialog");
            dismiss();
        }*/
        // If the user clicks on the areyousureYesButton, then action is confirmed as ConfirmedAction
    }

    public void doDeleteSet() {
        // Load the set up
        FullscreenActivity.settoload = null;
        FullscreenActivity.settoload = FullscreenActivity.setnamechosen;

        // Popup the are you sure alert into another dialog fragment
        // Get the list of set lists to be deleted
        String setstodelete = FullscreenActivity.setnamechosen.replace("%_%",", ");
        if (setstodelete.endsWith(", ")) {
            setstodelete = setstodelete.substring(0, setstodelete.length() - 2);
        }

        String message = getResources().getString(R.string.options_set_delete) + " \"" + setstodelete + "\"?";
        FullscreenActivity.myToastMessage = message;
        DialogFragment newFragment = PopUpAreYouSureFragment.newInstance(message);
        newFragment.show(getFragmentManager(), "dialog");
        dismiss();
        // If the user clicks on the areyousureYesButton, then action is confirmed as ConfirmedAction

    }

    public void doExportSet() {

        if (mListener!=null) {
            FullscreenActivity.whattodo = "customise_exportset";
            mListener.openFragment();
            dismiss();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class FetchDataTask extends AsyncTask<String,Integer,String> {

        @Override
        public void onPreExecute() {
            // Check the directories and clear them of prior content
            SetActions.checkDirectories();
        }

        @Override
        protected String doInBackground(String... args) {
            // Now users can load multiple sets and merge them, we need to load each one it turn
            // We then add the items to a temp string 'allsongsinset'
            // Once we have loaded them all, we replace the mySet field.

            String allsongsinset = "";

            // Split the string by "%_%" - last item will be empty as each set added ends with this
            String[] tempsets = FullscreenActivity.setnamechosen.split("%_%");

            for (String tempfile:tempsets) {
                if (tempfile!=null && !tempfile.equals("") && !tempfile.isEmpty()) {
                    try {
                        FullscreenActivity.settoload = tempfile;
                        SetActions.loadASet(getActivity());
                    } catch (XmlPullParserException | IOException e) {
                        e.printStackTrace();
                    }
                    allsongsinset = allsongsinset + FullscreenActivity.mySet;
                }
            }

            // Add all the songs of combined sets back to the mySet
            FullscreenActivity.mySet = allsongsinset;

            // Reset the options menu
            SetActions.prepareSetList();
            SetActions.indexSongInSet();

            return "LOADED";
        }

        @Override
        protected void onCancelled(String result) {
            Log.d("dataTask","onCancelled");
        }

        @Override
        protected void onPostExecute(String result) {
            FullscreenActivity.setView = true;

            if (result.equals("LOADED") && !dataTask.isCancelled()) {
                // Get the set first item
                SetActions.prepareFirstItem(getActivity());

                // Save the new set to the preferences
                Preferences.savePreferences();

                // Tell the listener to do something
                mListener.refreshAll();
                FullscreenActivity.whattodo = "editset";
                mListener.openFragment();
                FullscreenActivity.abort = false;
                //Close this dialog
                dismiss();
            }
            prog.dismiss();
        }
    }

    public void sortSetLists() {
        // Sort the set lists either alphabetically or reverse alphabetically
        ArrayList<String> setnames_ar = new ArrayList<>(Arrays.asList(FullscreenActivity.mySetsFileNames));

        if (!FullscreenActivity.sortAlphabetically) {
            Collections.sort(setnames_ar);
            Collections.reverse(setnames_ar);
        } else {
            Collections.sort(setnames_ar);
        }

        setnames = new String[setnames_ar.size()];
        filteredsetnames = new String[setnames_ar.size()];
        setnames = setnames_ar.toArray(setnames);
        filteredsetnames = setnames_ar.toArray(filteredsetnames);

        if (adapter!=null) {
            setCorrectAdapter(filteredsetnames);
        }
    }

    public void sortFilteredSetLists() {
        // Sort the filtered set lists either alphabetically or reverse alphabetically
        ArrayList<String> setnames_ar = new ArrayList<>(Arrays.asList(filteredsetnames));

        if (!FullscreenActivity.sortAlphabetically) {
            Collections.sort(setnames_ar);
            Collections.reverse(setnames_ar);
        } else {
            Collections.sort(setnames_ar);
        }

        setnames = new String[setnames_ar.size()];
        filteredsetnames = new String[setnames_ar.size()];
        setnames = setnames_ar.toArray(setnames);
        filteredsetnames = setnames_ar.toArray(filteredsetnames);

        if (adapter!=null) {
            setCorrectAdapter(filteredsetnames);
        }

        // Need to recheck any ones that were checked before.
        for (int i=0;i<filteredsetnames.length;i++) {
            if (FullscreenActivity.setnamechosen.contains(filteredsetnames[i])) {
                setListView1.setItemChecked(i,true);
            } else {
                setListView1.setItemChecked(i,false);
            }
        }
    }

    public void doRenameSet() {
        // Get the values from the page
        String newcat_edittext = newCategory_EditText.getText().toString();
        String newcat_spinner = cats.get(setCategory_Spinner.getSelectedItemPosition());

        String newsettitle = setListName.getText().toString();

        String newsetname;
        if (!newcat_edittext.equals("")) {
            newsetname = newcat_edittext + "__" + newsettitle;
        } else {
            if (newcat_spinner.equals(FullscreenActivity.mainfoldername)) {
                newsetname = newsettitle;
            } else {
                newsetname = newcat_spinner + "__" + newsettitle;
            }
        }

        File oldsetfile = new File(FullscreenActivity.dirsets+"/"+FullscreenActivity.setnamechosen);
        File newsetfile = new File(FullscreenActivity.dirsets+"/"+newsetname);
        boolean success;

        // Check the new song doesn't exist already
        if (newsetfile.exists() && !overWrite_CheckBox.isChecked()) {
            success = false;
        } else {
            success = oldsetfile.renameTo(newsetfile);
            if (!success) {
                Log.d("d","error renaming");
            }
        }

        if (success) {
            FullscreenActivity.myToastMessage = getActivity().getString(R.string.renametitle) + " - " +
                    getActivity().getString(R.string.success);
        } else {
            FullscreenActivity.myToastMessage = getActivity().getString(R.string.renametitle) + " - " +
                    getActivity().getString(R.string.file_exists);
        }
        ShowToast.showToast(getActivity());

        setListName.setText("");
        FullscreenActivity.setnamechosen="";

        // Refresh the category spinners
        SetActions.updateOptionListSets();
        sortSetLists();
        setCategory_Spinner.setAdapter(setCategories());
        oldCategory_Spinner.setAdapter(setCategories());
        setCorrectAdapter(setnames);
        whichSetCategory();

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (dataTask!=null) {
            dataTask.cancel(true);
        }
        this.dismiss();
    }

}