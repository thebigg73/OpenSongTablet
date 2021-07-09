/*
package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.garethevans.church.opensongtablet.OLD_TO_DELETE._CustomAnimations;
import com.garethevans.church.opensongtablet.OLD_TO_DELETE._PopUpAreYouSureFragment;
import com.garethevans.church.opensongtablet.OLD_TO_DELETE._PopUpSizeAndAlpha;
import com.garethevans.church.opensongtablet.OLD_TO_DELETE._SetActions;
import com.garethevans.church.opensongtablet.OLD_TO_DELETE._ShowToast;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;

public class PopUpListSetsFragment extends DialogFragment {

    static PopUpListSetsFragment newInstance() {
        PopUpListSetsFragment frag;
        frag = new PopUpListSetsFragment();
        return frag;
    }

    private String myTitle = "";
    private String lastSetCategory;
    private FetchDataTask dataTask;
    private ArrayList<String> cats, allsets, filteredsets;
    private ArrayAdapter<String> category_adapter, sets_adapter;
    private NonScrollListView set_ListView;
    private TextView setCategory_TextView;
    private TextView newSetPromptTitle;
    private EditText newCategory_EditText, setListName;
    private FloatingActionButton sort_FAB, newCategory_FAB, closeMe, saveMe;
    private CheckBox overWrite_CheckBox;
    private Spinner setCategory_Spinner, originalSetCategory_Spinner;
    private LinearLayout currentCategory_LinearLayout;
    private LinearLayout newCategory_LinearLayout;
    private LinearLayout newSetTitle_LinearLayout;
    private RelativeLayout setCategory;
    private View V;
    private ProgressBar progressBar;

    private StorageAccess storageAccess;
    private _SetActions setActions;
    private _Preferences preferences;

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
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            hideKeyboard(newCategory_EditText);
            hideKeyboard(setListName);
            hideKeyboard(originalSetCategory_Spinner);
            hideKeyboard(setCategory_Spinner);
            this.dismiss();
        }
        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        if (getActivity() != null && getActivity().getWindow() != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        V = inflater.inflate(R.layout.popup_listsets, container, false);

        // Initialise the helper classes
        storageAccess = new StorageAccess();
        setActions = new _SetActions();
        preferences = new _Preferences();

        new Thread(new Runnable() {
            @Override
            public void run() {

                myTitle = getTheTitle();

                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        identifyViews(V);
                    }
                });

                // Prepare the toast message using the title.  It is cleared if cancel is clicked
                StaticVariables.myToastMessage = myTitle + " : " + getActivity().getResources().getString(R.string.ok);

                // Reset the setname chosen
                StaticVariables.setnamechosen = "";

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
                        if (cats.contains(lastSetCategory)) {
                            setCategory_Spinner.setSelection(cats.indexOf(lastSetCategory));
                        }
                        originalSetCategory_Spinner.setAdapter(category_adapter);
                        set_ListView.setAdapter(sets_adapter);

                        // Try to set the spinners to match the recently used set category
                        boolean done = false;
                        for (int i = 0; i < cats.size(); i++) {
                            if (cats.get(i).equals(preferences.getMyPreferenceString(getActivity(), "whichSetCategory",
                                    getActivity().getString(R.string.mainfoldername)))) {
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
                                StaticVariables.sortAlphabetically = !StaticVariables.sortAlphabetically;
                                filterByCategory(preferences.getMyPreferenceString(getActivity(), "whichSetCategory",
                                        getActivity().getString(R.string.mainfoldername)));
                            }
                        });

                        // Set the new category listener
                        newCategory_FAB.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Change button function and image
                                createNewCategory();
                            }
                        });

                        //sort_FAB.requestFocus();
                    }

                });
            }
        }).start();

        // Set the popup defaults
        _PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog(), preferences);

        return V;
    }

    private void createNewCategory() {
        // This shows the edit text field and allows the user to add a new set category
        setCategory_Spinner.setVisibility(View.GONE);
        newCategory_EditText.setVisibility(View.VISIBLE);
        //newCategory_EditText.requestFocus();
        newCategory_FAB.setImageResource(R.drawable.ic_chevron_left_white_36dp);
        newCategory_FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCategory_Spinner.setVisibility(View.VISIBLE);
                newCategory_EditText.setVisibility(View.GONE);
                newCategory_FAB.setImageResource(R.drawable.ic_plus_white_36dp);
                newCategory_FAB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        createNewCategory();
                    }
                });
            }
        });
    }

    private String getTheTitle() {
        String myTitle = Objects.requireNonNull(getActivity()).getResources().getString(R.string.set);
        String mTitle;
        switch (StaticVariables.whattodo) {
            default:
            case "loadset":
                mTitle = myTitle + " - " + getActivity().getResources().getString(R.string.load);
                break;

            case "saveset":
                mTitle = myTitle + " - " + getActivity().getResources().getString(R.string.save);
                break;

            case "deleteset":
                mTitle = myTitle + " - " + getActivity().getResources().getString(R.string.delete);
                break;

            case "exportset":
                mTitle = myTitle + " - " + getActivity().getResources().getString(R.string.export);
                break;

            case "managesets":
                mTitle = myTitle + " - " + getActivity().getResources().getString(R.string.managesets);
                break;

        }
        return mTitle;
    }

    private void identifyViews(View V) {
        set_ListView = V.findViewById(R.id.set_ListView);
        setCategory_TextView = V.findViewById(R.id.setCategory_TextView);
        newSetPromptTitle = V.findViewById(R.id.newSetPromptTitle);
        newCategory_EditText = V.findViewById(R.id.newCategory_EditText);
        setListName = V.findViewById(R.id.setListName);
        // Determine the last set name and category
        String lastSetName = preferences.getMyPreferenceString(getActivity(), "setCurrentLastName", "");
        lastSetCategory = "";
        if (lastSetName.contains("__")) {
            String[] bits = lastSetName.split("__");
            try {
                lastSetCategory = bits[0];
                lastSetName = bits[1];
            } catch (Exception e) {
                lastSetCategory = "";
                lastSetName = preferences.getMyPreferenceString(getActivity(), "setCurrentLastName", "");
            }
        }
        if (lastSetName != null && lastSetName.length() > 0) {
            setListName.setText(lastSetName);
        } else {
            setListName.getText().clear();
        }

        sort_FAB = V.findViewById(R.id.sort_FAB);
        newCategory_FAB = V.findViewById(R.id.newCategory_FAB);
        overWrite_CheckBox = V.findViewById(R.id.overWrite_CheckBox);
        setCategory_Spinner = V.findViewById(R.id.setCategory_Spinner);
        originalSetCategory_Spinner = V.findViewById(R.id.originalSetCategory_Spinner);
        currentCategory_LinearLayout = V.findViewById(R.id.currentCategory_LinearLayout);
        newCategory_LinearLayout = V.findViewById(R.id.newCategory_LinearLayout);
        newSetTitle_LinearLayout = V.findViewById(R.id.newSetTitle_LinearLayout);
        setCategory = V.findViewById(R.id.setCategory);
        progressBar = V.findViewById(R.id.progressBar);
        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(myTitle);
        closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _CustomAnimations.animateFAB(closeMe, getActivity());
                closeMe.setEnabled(false);
                StaticVariables.myToastMessage = "";
                hideKeyboard(newCategory_EditText);
                hideKeyboard(setListName);
                hideKeyboard(originalSetCategory_Spinner);
                hideKeyboard(setCategory_Spinner);
                dismiss();
            }
        });
        saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _CustomAnimations.animateFAB(saveMe, getActivity());
                doAction();
            }
        });

    }

    private void hideOrShowViews() {
        //hideKeyboard(newCategory_EditText);
        //hideKeyboard(setListName);
        //hideKeyboard(originalSetCategory_Spinner);
        //hideKeyboard(setCategory_Spinner);

        //saveMe.setFocusable(true);
        //saveMe.requestFocus();

        switch (StaticVariables.whattodo) {
            default:
            case "loadset":
                currentCategory_LinearLayout.setVisibility(View.GONE);
                originalSetCategory_Spinner.setVisibility(View.VISIBLE);
                newCategory_EditText.setVisibility(View.GONE);
                newCategory_LinearLayout.setVisibility(View.VISIBLE);
                newSetTitle_LinearLayout.setVisibility(View.GONE);
                newCategory_FAB.hide();
                setListName.setVisibility(View.GONE);
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
                newCategory_FAB.hide();
                newCategory_EditText.setVisibility(View.GONE);
                overWrite_CheckBox.setVisibility(View.GONE);
                break;

            case "exportset":
                currentCategory_LinearLayout.setVisibility(View.GONE);
                newSetTitle_LinearLayout.setVisibility(View.GONE);
                setListName.setVisibility(View.GONE);
                newSetPromptTitle.setVisibility(View.GONE);
                newCategory_FAB.hide();
                newCategory_EditText.setVisibility(View.GONE);
                overWrite_CheckBox.setVisibility(View.GONE);
                break;

            case "managesets":
                set_ListView.setVisibility(View.VISIBLE);
                setCategory_TextView.setText(Objects.requireNonNull(getActivity()).getString(R.string.new_category));
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

    private void hideKeyboard(View v) {
        try {
            if (v!=null && v.getContext()!=null) {
                InputMethodManager imm =
                        (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listOfAllSets() {
        // Get a note of the available sets first of all
        allsets = setActions.listAllSets(getActivity(), preferences, storageAccess);
        // Get a note of the available set categories from these
        cats = setActions.listSetCategories(getActivity(), allsets);
    }

    private void listOfFilteredSets() {
        filteredsets = setActions.listFilteredSets(getActivity(), allsets,
                preferences.getMyPreferenceString(getActivity(), "whichSetCategory",
                        getString(R.string.mainfoldername)));
    }

    private ArrayAdapter<String> setCorrectAdapter(ListView listView) {
        ArrayAdapter<String> arr;
        switch (StaticVariables.whattodo) {
            default:
            case "deleteset":
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                arr = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), android.R.layout.simple_list_item_multiple_choice, filteredsets);
                break;

            case "saveset":
                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                arr = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), android.R.layout.simple_list_item_1, filteredsets);
                break;


            case "exportset":
            case "managesets":
                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                arr = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), android.R.layout.simple_list_item_checked, filteredsets);
                break;
        }
        return arr;
    }

    private void updateAvailableSets() {
        // Get a record of all the sets available in the SETS folder
        listOfAllSets();
        listOfFilteredSets();

        // Get array adapters for the spinners
        category_adapter = categoryAdapter();
        sets_adapter = setCorrectAdapter(set_ListView);

        setCategory_Spinner.setAdapter(category_adapter);
        originalSetCategory_Spinner.setAdapter(category_adapter);
        set_ListView.setAdapter(sets_adapter);

        // Try to set the spinners to match the recently used set category
        boolean done = false;
        for (int i = 0; i < cats.size(); i++) {
            if (cats.get(i).equals(preferences.getMyPreferenceString(getActivity(), "whichSetCategory",
                    getString(R.string.mainfoldername)))) {
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

    private ArrayAdapter<String> categoryAdapter() {
        ArrayAdapter<String> myadapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), R.layout.my_spinner, cats);
        myadapter.setDropDownViewResource(R.layout.my_spinner);
        return myadapter;
    }

    private void categorySpinnerListener() {
        if (!StaticVariables.whattodo.equals("managesets")) {
            setCategory_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    String s = cats.get(i);
                    preferences.setMyPreferenceString(getActivity(), "whichSetCategory", s);
                    filterByCategory(s);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });

        } else {
            originalSetCategory_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    String s = cats.get(i);
                    preferences.setMyPreferenceString(getActivity(), "whichSetCategory", s);
                    filterByCategory(s);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }
    }

    private void selectedSetListener() {
        set_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the name of the set to do stuff with
                // Since we can select multiple sets, check it isn't already in the setnamechosen field

                // Get the set name
                String msetname = filteredsets.get(position);

                // If we have a category selected, add this to the file name
                if (!StaticVariables.whattodo.equals("managesets") && setCategory_Spinner.getSelectedItemPosition() > 0) {
                    msetname = cats.get(setCategory_Spinner.getSelectedItemPosition()) + "__" + msetname;
                } else if (StaticVariables.whattodo.equals("managesets") && originalSetCategory_Spinner.getSelectedItemPosition() > 0) {
                    msetname = cats.get(originalSetCategory_Spinner.getSelectedItemPosition()) + "__" + msetname;
                }

                if (StaticVariables.whattodo.equals("exportset")) {
                    StaticVariables.setnamechosen = msetname + "%_%";
                } else if (!StaticVariables.whattodo.equals("managesets")) {
                    if (!StaticVariables.setnamechosen.contains(msetname)) {
                        // Add it to the setnamechosen
                        StaticVariables.setnamechosen = StaticVariables.setnamechosen + msetname + "%_%";
                    } else {
                        // Remove it from the setnamechosen
                        StaticVariables.setnamechosen = StaticVariables.setnamechosen.replace(msetname + "%_%", "");
                    }
                } else {
                    StaticVariables.setnamechosen = msetname;
                }
                setListName.setText(filteredsets.get(position));
                //setListName.clearFocus();
            }
        });
    }

    // Called when save tick is clicked
    private void doAction() {
        if (StaticVariables.setnamechosen.endsWith("%_%")) {
            StaticVariables.setnamechosen = StaticVariables.setnamechosen.substring(0, StaticVariables.setnamechosen.length() - 3);
        }

        if (StaticVariables.whattodo.equals("loadset") && !StaticVariables.setnamechosen.isEmpty()) {
            doLoadSet();
        } else if (StaticVariables.whattodo.equals("saveset") && !setListName.getText().toString().trim().isEmpty() && !setListName.getText().toString().trim().equals("")) {
            doSaveSet();
        } else if (StaticVariables.whattodo.equals("deleteset") && !StaticVariables.setnamechosen.isEmpty()) {
            doDeleteSet();
        } else if (StaticVariables.whattodo.equals("exportset") && !StaticVariables.setnamechosen.isEmpty()) {
            StaticVariables.settoload = StaticVariables.setnamechosen;
            doExportSet();
        } else if (StaticVariables.whattodo.equals("managesets")) {
            if (!StaticVariables.setnamechosen.equals("") && !setListName.getText().toString().equals("")) {
                doRenameSet();
            } else {
                StaticVariables.myToastMessage = Objects.requireNonNull(getActivity()).getString(R.string.notset);
            }
        }
    }

    private void filterByCategory(String cat) {
        filteredsets.clear();
        filteredsets = setActions.listFilteredSets(getActivity(), allsets, cat);

        sets_adapter = null;
        set_ListView.setAdapter(null);

        // Set the ListView adapter based on what we are doing (shows filtered files)
        sets_adapter = setCorrectAdapter(set_ListView);
        sets_adapter.notifyDataSetChanged();
        set_ListView.setAdapter(sets_adapter);

        // Go through new list and re tick any currently selected ones
        if (StaticVariables.whattodo.equals("loadset") || StaticVariables.whattodo.equals("managesets") ||
                StaticVariables.whattodo.equals("deleteset") || StaticVariables.whattodo.equals("exportset")) {
            tickSelectedSetsInCategory(cat);
        }
    }

    private void tickSelectedSetsInCategory(String filter) {
        if (filter != null && !filter.equals(getString(R.string.mainfoldername)) && !filter.equals("MAIN") && !filter.equals("")) {
            filter = filter + "__";
        } else {
            filter = "";
        }

        for (int f = 0; f < filteredsets.size(); f++) {
            boolean inmainfolder = filter.equals("") &&
                    (StaticVariables.setnamechosen.startsWith(filteredsets.get(f)) ||
                            StaticVariables.setnamechosen.contains("%_%" + filteredsets.get(f)));
            boolean inotherfolder = StaticVariables.setnamechosen.contains(filter + filteredsets.get(f));

            if (inmainfolder || (!filter.equals("") && inotherfolder)) {
                set_ListView.setItemChecked(f, true);
            } else {
                set_ListView.setItemChecked(f, false);
            }
        }
    }

    private void doLoadSet() {
        // Load the set up

        // Initialise the saved set
        StaticVariables.settoload = null;
        preferences.setMyPreferenceString(getActivity(), "setCurrent", "");

        StaticVariables.settoload = StaticVariables.setnamechosen;
        preferences.setMyPreferenceString(getActivity(), "setCurrentLastName", setListName.getText().toString().replace("%_%", "_"));

        dataTask = null;
        dataTask = new FetchDataTask();
        try {
            dataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doSaveSet() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });

                // Save the set into the settoload name
                StaticVariables.settoload = setListName.getText().toString().trim();
                preferences.setMyPreferenceString(getActivity(), "setCurrentLastName", setListName.getText().toString().trim());
                String new_cat = newCategory_EditText.getText().toString();

                if (!new_cat.equals("")) {
                    StaticVariables.settoload = new_cat + "__" + setListName.getText().toString().trim();
                } else if (setCategory_Spinner.getSelectedItemPosition() > 0) {
                    StaticVariables.settoload = cats.get(setCategory_Spinner.getSelectedItemPosition()) +
                            "__" + setListName.getText().toString().trim();
                }

                // Popup the are you sure alert into another dialog fragment
                final Uri newsetname = storageAccess.getUriForItem(getActivity(), preferences, "Sets", "",
                        StaticVariables.settoload);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // New structure, only give the are you sure prompt if the set name already exists.
                        if (storageAccess.uriExists(getActivity(), newsetname)) {
                            String message = getResources().getString(R.string.save) + " \'" + setListName.getText().toString().trim() + "\"?";
                            StaticVariables.myToastMessage = message;
                            DialogFragment newFragment = _PopUpAreYouSureFragment.newInstance(message);
                            newFragment.show(getActivity().getSupportFragmentManager(), "dialog");
                            hideKeyboard(newCategory_EditText);
                            hideKeyboard(setListName);
                            hideKeyboard(originalSetCategory_Spinner);
                            hideKeyboard(setCategory_Spinner);
                            dismiss();
                        } else {
                            if (mListener != null) {
                                StaticVariables.whattodo = "saveset";
                                mListener.confirmedAction();
                            }
                            try {
                                hideKeyboard(newCategory_EditText);
                                hideKeyboard(setListName);
                                hideKeyboard(originalSetCategory_Spinner);
                                hideKeyboard(setCategory_Spinner);
                                dismiss();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }).start();
    }

    private void doRenameSet() {
        // Get the values from the page

        String newcat_edittext = newCategory_EditText.getText().toString();
        String newcat_spinner = cats.get(setCategory_Spinner.getSelectedItemPosition());

        String newsettitle = setListName.getText().toString();
        StaticVariables.settoload = newsettitle;

        String newsetname;
        if (!newcat_edittext.equals("")) {
            newsetname = newcat_edittext + "__" + newsettitle;
        } else if (newcat_spinner.equals(getString(R.string.mainfoldername)) || newcat_spinner.equals("MAIN")) {
            newsetname = newsettitle;
        } else {
            newsetname = newcat_spinner + "__" + newsettitle;
        }

        Uri newsetfile = storageAccess.getUriForItem(getActivity(), preferences, "Sets", "",
                newsetname);

        Log.d("PopUpListSets", "newsetfile=" + newsetfile);

        boolean exists = storageAccess.uriExists(getActivity(), newsetfile);
        boolean overwrite = overWrite_CheckBox.isChecked();
        boolean success = false;

        if (!exists || overwrite) {
            success = storageAccess.renameSetFile(getActivity(), preferences, StaticVariables.setnamechosen, newsetname);
        }

        Log.d("PopUpListSets", "exists=" + exists);
        Log.d("PopUpListSets", "overwrite=" + overwrite);
        Log.d("PopUpListSets", "success=" + success);


        if (success) {
            updateAvailableSets();
            StaticVariables.myToastMessage = Objects.requireNonNull(getActivity()).getResources().getString(R.string.rename) + " - " +
                    getActivity().getResources().getString(R.string.success);
        } else {
            StaticVariables.myToastMessage = Objects.requireNonNull(getActivity()).getResources().getString(R.string.rename) + " - " +
                    getActivity().getResources().getString(R.string.file_exists);
        }

        _ShowToast.showToast(getActivity());
        preferences.setMyPreferenceString(getActivity(), "setCurrentLastName", StaticVariables.setnamechosen);
        StaticVariables.setnamechosen = "";

        // Close the window
        try {
            hideKeyboard(newCategory_EditText);
            hideKeyboard(setListName);
            hideKeyboard(originalSetCategory_Spinner);
            hideKeyboard(setCategory_Spinner);
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class FetchDataTask extends AsyncTask<String, Integer, String> {

        @Override
        public void onPreExecute() {
            // Check the directories and clear them of prior content
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                setActions.emptyCacheDirectories(getActivity(), preferences, storageAccess);
            } catch (Exception e) {
                Log.d("PopUpListSets","Error clearing cache");
            }
            StaticVariables.mSet = null;

            // Now users can load multiple sets and merge them, we need to load each one it turn
            // We then add the items to a temp string 'allsongsinset'
            // Once we have loaded them all, we replace the mySet field.

            StringBuilder allsongsinset = new StringBuilder();

            // Split the string by "%_%" - last item will be empty as each set added ends with this
            String[] tempsets = StaticVariables.setnamechosen.split("%_%");

            for (String tempfile : tempsets) {
                if (tempfile != null && !tempfile.equals("") && !tempfile.isEmpty()) {
                    try {
                        StaticVariables.settoload = tempfile;
                        setActions.loadASet(getActivity(), preferences, storageAccess);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    allsongsinset.append(preferences.getMyPreferenceString(getActivity(), "setCurrent", ""));
                }
            }

            // Add all the songs of combined sets back to the mySet
            preferences.setMyPreferenceString(getActivity(), "setCurrent", allsongsinset.toString());

            // Reset the options menu
            //setActions.prepareSetList(getActivity(), preferences);
            setActions.indexSongInSet();

            return "LOADED";
        }

        @Override
        protected void onCancelled(String result) {
            Log.d("dataTask", "onCancelled");
        }

        @Override
        protected void onPostExecute(String result) {
            StaticVariables.setView = true;

            if (result.equals("LOADED") && !dataTask.isCancelled()) {
                try {
                    // Get the set first item
                    setActions.prepareFirstItem(getActivity(),preferences);

                    // Tell the listener to do something
                    mListener.refreshAll();
                    StaticVariables.whattodo = "editset";
                    mListener.openFragment();
                    //Close this dialog
                    hideKeyboard(newCategory_EditText);
                    hideKeyboard(setListName);
                    hideKeyboard(originalSetCategory_Spinner);
                    hideKeyboard(setCategory_Spinner);
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (progressBar!=null) {
                try {
                    progressBar.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doExportSet() {
        if (mListener != null) {
            StaticVariables.whattodo = "customise_exportset";
            mListener.openFragment();
            try {
                hideKeyboard(newCategory_EditText);
                hideKeyboard(setListName);
                hideKeyboard(originalSetCategory_Spinner);
                hideKeyboard(setCategory_Spinner);
                dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void doDeleteSet() {
        // Load the set up
        StaticVariables.settoload = StaticVariables.setnamechosen;

        // Popup the are you sure alert into another dialog fragment
        // Get the list of set lists to be deleted
        String setstodelete = StaticVariables.setnamechosen.replace("%_%", ", ");
        if (setstodelete.endsWith(", ")) {
            setstodelete = setstodelete.substring(0, setstodelete.length() - 2);
        }

        String message = getResources().getString(R.string.delete) + " \"" + setstodelete + "\"?";
        StaticVariables.myToastMessage = message;
        DialogFragment newFragment = _PopUpAreYouSureFragment.newInstance(message);
        newFragment.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), message);
        hideKeyboard(newCategory_EditText);
        hideKeyboard(setListName);
        hideKeyboard(originalSetCategory_Spinner);
        hideKeyboard(setCategory_Spinner);
        dismiss();
        // If the user clicks on the areyousureYesButton, then action is confirmed as ConfirmedAction
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        try {
            hideKeyboard(newCategory_EditText);
            hideKeyboard(setListName);
            hideKeyboard(originalSetCategory_Spinner);
            hideKeyboard(setCategory_Spinner);
            this.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}*/
