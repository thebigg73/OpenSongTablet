package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class PopUpListSetsFragment extends DialogFragment {

    static PopUpListSetsFragment newInstance() {
        PopUpListSetsFragment frag;
        frag = new PopUpListSetsFragment();
        return frag;
    }

    String myTitle = "";
    FetchDataTask dataTask;
    ProgressDialog prog;
    ArrayList<String> cats, allsets, filteredsets;
    ArrayAdapter<String> category_adapter, sets_adapter;
    NonScrollListView set_ListView;
    //ListView set_ListView;
    TextView title, setCategory_TextView, newSetPromptTitle;
    EditText newCategory_EditText, setListName;
    FloatingActionButton sort_FAB, newCategory_FAB, closeMe, saveMe;
    CheckBox overWrite_CheckBox;
    Spinner setCategory_Spinner, originalSetCategory_Spinner;
    LinearLayout category, currentCategory_LinearLayout, newCategory_LinearLayout, newSetTitle_LinearLayout;
    RelativeLayout setCategory;
    View V;

    StorageAccess storageAccess;
    SetActions setActions;
    Preferences preferences;
    ListSongFiles listSongFiles;

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
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        V = inflater.inflate(R.layout.popup_listsets, container, false);

        new Thread(new Runnable() {
            @Override
            public void run() {

                myTitle = getTheTitle();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        identifyViews(V);
                    }
                });

                // Initialise the helper classes
                storageAccess = new StorageAccess();
                setActions = new SetActions();
                preferences = new Preferences();
                listSongFiles = new ListSongFiles();

                // Prepare the toast message using the title.  It is cleared if cancel is clicked
                FullscreenActivity.myToastMessage = myTitle + " : " + getActivity().getResources().getString(R.string.ok);

                // Reset the setname chosen
                FullscreenActivity.setnamechosen = "";
                FullscreenActivity.abort = false;

                // Get a record of all the sets available in the SETS folder
                listOfAllSets();
                listOfFilteredSets();

                // Customise the view depending on what we are doing

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Hide/show the stuff depending on what we are doing
                        hideOrShowViews();

                        // Get array adapters for the spinners
                        category_adapter = categoryAdapter();
                        sets_adapter = setCorrectAdapter(set_ListView);

                        setCategory_Spinner.setAdapter(category_adapter);
                        originalSetCategory_Spinner.setAdapter(category_adapter);
                        set_ListView.setAdapter(sets_adapter);
                        fixListViewSize();

                        // Try to set the spinners to match the recently used set category
                        boolean done = false;
                        for (int i=0;i<cats.size();i++) {
                            if (cats.get(i).equals(FullscreenActivity.whichSetCategory)) {
                                setCategory_Spinner.setSelection(i);
                                originalSetCategory_Spinner.setSelection(i);
                                done = true;
                            }
                        }
                        if (!done) {
                            // Can't find the set category, so default to the MAIN one (position 0)
                            setCategory_Spinner.setSelection(0);
                            originalSetCategory_Spinner.setSelection(0);
                        }

                        // Set the listeners for the set category spinners
                        categorySpinnerListener();

                        // Set the file list listener
                        selectedSetListener();

                        // Set the sort button listener
                        sort_FAB.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                FullscreenActivity.sortAlphabetically = !FullscreenActivity.sortAlphabetically;
                                filterByCategory(FullscreenActivity.whichSetCategory);
                            }
                        });

                        // Set the new category listener
                        newCategory_FAB.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Change button function and image
                                //changeCategoryButton(true);
                            }
                        });


                    }

                });
            }
        }).start();

        // Set the popup defaults
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());

        return V;
    }

    String getTheTitle() {
        String myTitle = getActivity().getResources().getString(R.string.options_set);
        String mTitle;
        switch (FullscreenActivity.whattodo) {
            default:
            case "loadset":
                mTitle = myTitle + " - " + getActivity().getResources().getString(R.string.options_set_load);
                break;

            case "saveset":
                mTitle = myTitle + " - " + getActivity().getResources().getString(R.string.options_set_save);
                break;

            case "deleteset":
                mTitle = myTitle + " - " + getActivity().getResources().getString(R.string.options_set_delete);
                break;

            case "exportset":
                mTitle = myTitle + " - " + getActivity().getResources().getString(R.string.options_set_export);
                break;

            case "managesets":
                mTitle = myTitle + " - " + getActivity().getResources().getString(R.string.managesets);
                break;

        }
        return mTitle;
    }

    void identifyViews(View V) {
        set_ListView = V.findViewById(R.id.set_ListView);
        setCategory_TextView = V.findViewById(R.id.setCategory_TextView);
        newSetPromptTitle = V.findViewById(R.id.newSetPromptTitle);
        newCategory_EditText = V.findViewById(R.id.newCategory_EditText);
        setListName = V.findViewById(R.id.setListName);
        setListName.setText(FullscreenActivity.lastSetName);
        sort_FAB = V.findViewById(R.id.sort_FAB);
        newCategory_FAB = V.findViewById(R.id.newCategory_FAB);
        overWrite_CheckBox = V.findViewById(R.id.overWrite_CheckBox);
        setCategory_Spinner = V.findViewById(R.id.setCategory_Spinner);
        originalSetCategory_Spinner = V.findViewById(R.id.originalSetCategory_Spinner);
        category = V.findViewById(R.id.category);
        currentCategory_LinearLayout = V.findViewById(R.id.currentCategory_LinearLayout);
        newCategory_LinearLayout = V.findViewById(R.id.newCategory_LinearLayout);
        newSetTitle_LinearLayout = V.findViewById(R.id.newSetTitle_LinearLayout);
        setCategory = V.findViewById(R.id.setCategory);
        title = V.findViewById(R.id.dialogtitle);
        title.setText(myTitle);
        closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                FullscreenActivity.myToastMessage = "";
                dismiss();
            }
        });
        saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(saveMe,getActivity());
                doAction();
            }
        });

    }

    void hideOrShowViews() {
        switch (FullscreenActivity.whattodo) {
            default:
            case "loadset":
                currentCategory_LinearLayout.setVisibility(View.GONE);
                newCategory_LinearLayout.setVisibility(View.VISIBLE);
                newSetTitle_LinearLayout.setVisibility(View.GONE);
                newCategory_FAB.setVisibility(View.GONE);
                newCategory_EditText.setVisibility(View.GONE);
                overWrite_CheckBox.setVisibility(View.VISIBLE);
                break;

            case "saveset":
                currentCategory_LinearLayout.setVisibility(View.GONE);
                newCategory_LinearLayout.setVisibility(View.VISIBLE);
                newSetTitle_LinearLayout.setVisibility(View.VISIBLE);
                newCategory_EditText.setVisibility(View.GONE);
                overWrite_CheckBox.setVisibility(View.GONE);
                break;

            case "deleteset":
                currentCategory_LinearLayout.setVisibility(View.GONE);
                newCategory_LinearLayout.setVisibility(View.VISIBLE);
                newSetTitle_LinearLayout.setVisibility(View.GONE);
                setListName.setVisibility(View.GONE);
                newSetPromptTitle.setVisibility(View.GONE);
                newCategory_FAB.setVisibility(View.GONE);
                newCategory_EditText.setVisibility(View.GONE);
                overWrite_CheckBox.setVisibility(View.GONE);
                break;

            case "exportset":
                currentCategory_LinearLayout.setVisibility(View.GONE);
                newSetTitle_LinearLayout.setVisibility(View.GONE);
                setListName.setVisibility(View.GONE);
                newSetPromptTitle.setVisibility(View.GONE);
                newCategory_FAB.setVisibility(View.GONE);
                newCategory_EditText.setVisibility(View.GONE);
                overWrite_CheckBox.setVisibility(View.GONE);
                break;

            case "managesets":
                set_ListView.setVisibility(View.VISIBLE);
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
    }

    void listOfAllSets() {
        // Get a note of the available sets first of all
        allsets = setActions.listAllSets(getActivity(), storageAccess);

        // Get a note of the available set categories from these
        cats = setActions.listSetCategories(getActivity(), allsets);
    }

    void listOfFilteredSets() {
        filteredsets = setActions.listFilteredSets(getActivity(), allsets, FullscreenActivity.whichSetCategory);
    }

    ArrayAdapter<String> setCorrectAdapter(ListView listView) {
        ArrayAdapter<String> arr;
        switch (FullscreenActivity.whattodo) {
            default:
            case "deleteset":
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                arr = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, filteredsets);
                break;

            case "saveset":
                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                arr = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, filteredsets);
                break;


            case "exportset":
            case "managesets":
                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                arr = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_checked, filteredsets);
            break;
        }
        return arr;
    }

    void fixListViewSize() {
        /*if (sets_adapter == null) {
            // pre-condition
            return;
        }
        sets_adapter.notifyDataSetChanged();
        int set_counts = sets_adapter.getCount();
        Log.d("d","set_counts="+set_counts);
        int totalHeight = 0;
        for (int i = 0; i < set_counts; i++) {
            View listItem = sets_adapter.getView(i, null, set_ListView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = set_ListView.getLayoutParams();
        params.height = totalHeight + (set_ListView.getDividerHeight() * (sets_adapter.getCount() - 1));
        set_ListView.setLayoutParams(params);
        set_ListView.requestLayout();*/
    }
    void updateAvailableSets() {
        // Get a record of all the sets available in the SETS folder
        listOfAllSets();
        listOfFilteredSets();

        // Get array adapters for the spinners
        category_adapter = categoryAdapter();
        sets_adapter = setCorrectAdapter(set_ListView);

        setCategory_Spinner.setAdapter(category_adapter);
        originalSetCategory_Spinner.setAdapter(category_adapter);
        set_ListView.setAdapter(sets_adapter);
        fixListViewSize();

        // Try to set the spinners to match the recently used set category
        boolean done = false;
        for (int i=0;i<cats.size();i++) {
            if (cats.get(i).equals(FullscreenActivity.whichSetCategory)) {
                setCategory_Spinner.setSelection(i);
                originalSetCategory_Spinner.setSelection(i);
                done = true;
            }
        }
        if (!done) {
            // Can't find the set category, so default to the MAIN one (position 0)
            setCategory_Spinner.setSelection(0);
            originalSetCategory_Spinner.setSelection(0);
        }

    }
    ArrayAdapter<String> categoryAdapter() {
        ArrayAdapter<String> myadapter = new ArrayAdapter<>(getActivity(),R.layout.my_spinner,cats);
        myadapter.setDropDownViewResource(R.layout.my_spinner);
        return myadapter;
    }

    void categorySpinnerListener() {
        if (!FullscreenActivity.whattodo.equals("managesets")) {
            setCategory_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    FullscreenActivity.whichSetCategory = cats.get(i);
                    Preferences.savePreferences();
                    filterByCategory(FullscreenActivity.whichSetCategory);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });

        } else {
            originalSetCategory_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    FullscreenActivity.whichSetCategory = cats.get(i);
                    Preferences.savePreferences();
                    filterByCategory(FullscreenActivity.whichSetCategory);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }
    }

    void selectedSetListener() {
        set_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the name of the set to do stuff with
                // Since we can select multiple sets, check it isn't already in the setnamechosen field

                // Get the set name
                String msetname = filteredsets.get(position);

                // If we have a category selected, add this to the file name
                if (!FullscreenActivity.whattodo.equals("managesets") && setCategory_Spinner.getSelectedItemPosition()>0) {
                    msetname = cats.get(setCategory_Spinner.getSelectedItemPosition()) + "__" + msetname;
                } else if (FullscreenActivity.whattodo.equals("managesets") && originalSetCategory_Spinner.getSelectedItemPosition()>0){
                    msetname = cats.get(originalSetCategory_Spinner.getSelectedItemPosition()) + "__" + msetname;
                }

                if (FullscreenActivity.whattodo.equals("exportset")) {
                    FullscreenActivity.setnamechosen = msetname + "%_%";
                } else if (!FullscreenActivity.whattodo.equals("managesets")){
                    if (!FullscreenActivity.setnamechosen.contains(msetname)) {
                        // Add it to the setnamechosen
                        FullscreenActivity.setnamechosen = FullscreenActivity.setnamechosen + msetname + "%_%";
                    } else {
                        // Remove it from the setnamechosen
                        FullscreenActivity.setnamechosen = FullscreenActivity.setnamechosen.replace(msetname + "%_%", "");
                    }
                } else if (FullscreenActivity.whattodo.equals("managesets")) {
                    FullscreenActivity.setnamechosen = msetname;
                }
                setListName.setText(filteredsets.get(position));
            }
        });
    }

    // Called when save tick is clicked
    void doAction() {
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

    void filterByCategory(String cat) {
        filteredsets.clear();
        filteredsets = setActions.listFilteredSets(getActivity(), allsets, cat);

        sets_adapter = null;
        set_ListView.setAdapter(null);

        // Set the ListView adapter based on what we are doing (shows filtered files)
        sets_adapter = setCorrectAdapter(set_ListView);
        sets_adapter.notifyDataSetChanged();
        set_ListView.setAdapter(sets_adapter);
        fixListViewSize();

        // Go through new list and re tick any currently selected ones
        if (FullscreenActivity.whattodo.equals("loadset") || FullscreenActivity.whattodo.equals("managesets") ||
                FullscreenActivity.whattodo.equals("deleteset") || FullscreenActivity.whattodo.equals("exportset")) {
            tickSelectedSetsInCategory(cat);
        }
    }

    void tickSelectedSetsInCategory(String filter) {
        if (filter!=null && !filter.equals(getString(R.string.mainfoldername))) {
            filter = filter + "__";
        } else {
            filter = "";
        }

        for (int f=0;f<filteredsets.size();f++) {
            boolean inmainfolder = filter.equals("") &&
                    (FullscreenActivity.setnamechosen.startsWith(filteredsets.get(f)) ||
                            FullscreenActivity.setnamechosen.contains("%_%"+filteredsets.get(f)));
            boolean inotherfolder = FullscreenActivity.setnamechosen.contains(filter+filteredsets.get(f));

            if (inmainfolder || (!filter.equals("") && inotherfolder)) {
                set_ListView.setItemChecked(f,true);
            } else {
                set_ListView.setItemChecked(f,false);
            }
            /*if (FullscreenActivity.setnamechosen.contains(filter+filteredsets.get(f))) {
                set_ListView.setItemChecked(f,true);
            } else {
                set_ListView.setItemChecked(f,false);
            }*/
        }
    }

    void doLoadSet() {
        // Load the set up
        // Show the progress bar
        prog = new ProgressDialog(getActivity());
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
        /*runnable = new Runnable() {
            public void run() {
                prog.setMessage(val);
            }
        };*/
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
            e.printStackTrace();
        }
    }

    void doSaveSet() {
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
        Uri newsetname = storageAccess.getUriForItem(getActivity(), preferences, "Sets", "",
                FullscreenActivity.settoload);

        // New structure, only give the are you sure prompt if the set name already exists.
        if (storageAccess.uriExists(getActivity(),newsetname)) {
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
                e.printStackTrace();
            }
        }
    }

    void doRenameSet() {
        // Get the values from the page

        String newcat_edittext = newCategory_EditText.getText().toString();
        String newcat_spinner = cats.get(setCategory_Spinner.getSelectedItemPosition());

        String newsettitle = setListName.getText().toString();

        String newsetname;
        if (!newcat_edittext.equals("")) {
            newsetname = newcat_edittext + "__" + newsettitle;
        } else if (newcat_spinner.equals(FullscreenActivity.mainfoldername)) {
            newsetname = newsettitle;
        } else {
            newsetname = newcat_spinner + "__" + newsettitle;
        }

        Uri oldsetfile = storageAccess.getUriForItem(getActivity(), preferences, "Sets", "",
                FullscreenActivity.setnamechosen);
        Uri newsetfile = storageAccess.getUriForItem(getActivity(), preferences, "Sets", "",
                newsetname);

        boolean exists = storageAccess.uriExists(getActivity(), newsetfile);
        boolean overwrite = overWrite_CheckBox.isChecked();
        boolean success = false;
        if (!exists || overwrite) {
            success = storageAccess.renameFile(getActivity(),oldsetfile,newsetname);
        }

        if (success) {
            updateAvailableSets();
            FullscreenActivity.myToastMessage = getActivity().getResources().getString(R.string.renametitle) + " - " +
                    getActivity().getResources().getString(R.string.success);
        } else {
            FullscreenActivity.myToastMessage = getActivity().getResources().getString(R.string.renametitle) + " - " +
                    getActivity().getResources().getString(R.string.file_exists);
        }

        ShowToast.showToast(getActivity());

        setListName.setText("");
        FullscreenActivity.setnamechosen = "";
    }

    @SuppressLint("StaticFieldLeak")
    private class FetchDataTask extends AsyncTask<String, Integer, String> {

        @Override
        public void onPreExecute() {
            // Check the directories and clear them of prior content
            setActions.emptyCacheDirectories(getActivity(), preferences, storageAccess);
            FullscreenActivity.mySet = "";
            FullscreenActivity.mSet = null;
            FullscreenActivity.myParsedSet = null;
        }

        @Override
        protected String doInBackground(String... args) {
            // Now users can load multiple sets and merge them, we need to load each one it turn
            // We then add the items to a temp string 'allsongsinset'
            // Once we have loaded them all, we replace the mySet field.

            StringBuilder allsongsinset = new StringBuilder();

            // Split the string by "%_%" - last item will be empty as each set added ends with this
            String[] tempsets = FullscreenActivity.setnamechosen.split("%_%");

            for (String tempfile : tempsets) {
                if (tempfile != null && !tempfile.equals("") && !tempfile.isEmpty()) {
                    try {
                        FullscreenActivity.settoload = tempfile;
                        setActions.loadASet(getActivity(), preferences, storageAccess);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    allsongsinset.append(FullscreenActivity.mySet);
                }
            }

            // Add all the songs of combined sets back to the mySet
            FullscreenActivity.mySet = allsongsinset.toString();

            // Reset the options menu
            setActions.prepareSetList();
            setActions.indexSongInSet();

            return "LOADED";
        }

        @Override
        protected void onCancelled(String result) {
            Log.d("dataTask", "onCancelled");
        }

        @Override
        protected void onPostExecute(String result) {
            FullscreenActivity.setView = true;

            if (result.equals("LOADED") && !dataTask.isCancelled()) {
                // Get the set first item
                setActions.prepareFirstItem(getActivity(), listSongFiles, storageAccess);

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

    void doExportSet() {
        if (mListener!=null) {
            FullscreenActivity.whattodo = "customise_exportset";
            mListener.openFragment();
            try {
                dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void doDeleteSet() {
        // Load the set up
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

    @Override
    public void onCancel(DialogInterface dialog) {
        try {
            this.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}