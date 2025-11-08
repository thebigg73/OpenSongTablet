package com.garethevans.church.opensongtablet.importsongs;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.InformationBottomSheet;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.SettingsImportFileBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.justchords.JustChordsObject;
import com.garethevans.church.opensongtablet.justchords.JustChordsSongObject;
import com.garethevans.church.opensongtablet.preferences.AreYouSureBottomSheet;
import com.garethevans.church.opensongtablet.setmenu.SetItemInfo;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

public class ImportFileFragment extends Fragment {

    private final String TAG = "ImportFileFragment";
    private MainActivityInterface mainActivityInterface;
    private SettingsImportFileBinding myView;
    private Song newSong = new Song();
    private ArrayList<String> folders;
    private boolean isIMGorPDF, showNotFoundToast=false, isSetFile = false, isHTML = false;
    private String basename, requiredExtension, setcategory, newSetName;
    private Uri copyTo;
    private String originalFolder, originalFilename, import_from_file_string = "",
            error_string = "", mainfoldername_string = "", overwrite_string = "",
            filename_string = "", file_exists_string = "", set_name_string = "",
            set_category_string = "", import_set_string="", deeplink_import_file = "",
            set_items_not_found_string = "";
    private ExposedDropDownArrayAdapter exposedDropDownArrayAdapter;
    private ArrayList<SetItemInfo> setItemInfos = null;

    @Override
    public void onResume() {
        super.onResume();
        prepareStrings();
        if (mainActivityInterface.getWhattodo().equals("importset")) {
            mainActivityInterface.updateToolbar(import_set_string);
        } else {
            mainActivityInterface.updateToolbar(import_from_file_string);
        }
        mainActivityInterface.updateToolbarHelp(deeplink_import_file);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsImportFileBinding.inflate(inflater, container, false);

        // Tint the progressBar as the secondary color
        mainActivityInterface.getMyThemeColors().tintProgressBar(myView.progress);

        prepareStrings();

        myView.nestedScrollView.setExtendedFabToAnimate(myView.importButton);

        // Get a note of the original song folder/filename
        originalFolder = mainActivityInterface.getSong().getFolder();
        originalFilename = mainActivityInterface.getSong().getFilename();
        // Hide everything other than the progress bar while we process the song
        showProgress(true);
        myView.content.setVisibility(View.GONE);
        myView.importButton.setVisibility(View.GONE);
        myView.imageView.setVisibility(View.GONE);
        myView.folder.setVisibility(View.GONE);
        myView.filename.setVisibility(View.GONE);

        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Get the available folders and base name
            if (mainActivityInterface.getImportFilename() != null) {
                basename = mainActivityInterface.getImportFilename().replaceAll("\\.[^.]*$", "");
            } else {
                basename = "unknown";
            }
            setcategory = mainfoldername_string;

            if (mainActivityInterface.getWhattodo().equals("importset")) {
                // This is actually a set
                mainActivityInterface.getMainHandler().post(() -> {
                            mainActivityInterface.updateToolbar(import_set_string);
                            myView.filename.setHint(set_name_string);
                            myView.folder.setHint(set_category_string);
                            myView.existing.setVisibility(View.GONE);
                            myView.setLoadFirst.setVisibility(View.VISIBLE);
                            myView.setLoadFirst.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("setLoadFirst", true));
                            myView.setLoadFirst.setOnCheckedChangeListener((compoundButton, b) -> mainActivityInterface.getPreferences().setMyPreferenceBoolean("setLoadFirst", b));
                        });
                isSetFile = true;
                folders = mainActivityInterface.getSetActions().getCategories(mainActivityInterface.getSetActions().getAllSets());
                if (basename.contains("__")) {
                    // This set has a category already - get it!
                    String[] bits = basename.split("__");
                    if (bits.length == 2) {
                        setcategory = bits[0];
                        basename = bits[1];
                    }
                }
            } else {
                folders = mainActivityInterface.getSQLiteHelper().getFolders();
                mainActivityInterface.getMainHandler().post(() -> myView.setLoadFirst.setVisibility(View.GONE));
            }

            newSong.setFilename(mainActivityInterface.getImportFilename());
            isIMGorPDF = mainActivityInterface.getStorageAccess().isIMGorPDF(newSong);
            requiredExtension = "";
            if (mainActivityInterface.getImportFilename()!=null && mainActivityInterface.getImportFilename().contains(".") && isIMGorPDF) {
                requiredExtension = mainActivityInterface.getImportFilename().substring(mainActivityInterface.getImportFilename().lastIndexOf("."));
                basename = mainActivityInterface.getImportFilename();
            }

            // Set up the folder exposed dropdown
            mainActivityInterface.getMainHandler().post(() -> {
                if (getContext() != null) {
                    exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.folder, R.layout.view_exposed_dropdown_item, folders);
                    myView.folder.setAdapter(exposedDropDownArrayAdapter);
                }
                // Default to the current folder
                myView.folder.setText(mainActivityInterface.getSong().getFolder());
            });
            // Try to read in the song using the import information.  This copies to the Variations/_cache folder
            if (isSetFile) {
                readInSetFile();

            } else {
                readInFile();
            }

            mainActivityInterface.getMainHandler().post(() -> {
                // Update the views and get the file
                updateViews();

                // Add listeners
                setupListeners();

                showProgress(false);
                myView.filename.setVisibility(View.VISIBLE);
                myView.folder.setVisibility(View.VISIBLE);
                myView.importButton.setVisibility(View.VISIBLE);
            });
        });

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext() != null) {
            import_from_file_string = getString(R.string.import_from_file);
            mainfoldername_string = getString(R.string.mainfoldername);
            overwrite_string = getString(R.string.overwrite);
            filename_string = getString(R.string.filename);
            error_string = getString(R.string.error);
            file_exists_string = getString(R.string.file_exists);
            set_category_string = getString(R.string.category);
            set_name_string = getString(R.string.set_name);
            import_set_string = getString(R.string.set_import);
            deeplink_import_file = getString(R.string.deeplink_import_file);
            set_items_not_found_string = getString(R.string.set_items_not_found);
        }
    }


    private void readInSetFile() {
        // Read in as a text string
        isHTML = mainActivityInterface.getStorageAccess().isSpecificFileExtension("html",mainActivityInterface.getImportFilename());
        showNotFoundToast = false;
        if (isHTML) {
            setItemInfos = mainActivityInterface.getConvertOnSong().getOnSongHTMLSetList(mainActivityInterface.getImportUri());
            if (setItemInfos!=null && !setItemInfos.isEmpty()) {
                StringBuilder newItems = new StringBuilder();
                for (SetItemInfo setItemInfo:setItemInfos) {
                    String foundOrNot = "";
                    if (setItemInfo.songfilename.startsWith("__NOTFOUND__")) {
                        showNotFoundToast = true;
                        foundOrNot = "* ";
                        setItemInfo.songfilename = setItemInfo.songfilename.replace("__NOTFOUND__","");
                    }
                    newItems.append(foundOrNot).append((setItemInfo.songitem+1)).append(". ").
                            append(setItemInfo.songfolder).append("/").
                            append(setItemInfo.songfilename);
                    if (setItemInfo.songkey!=null && !setItemInfo.songkey.isEmpty()) {
                        newItems.append(" (").append(setItemInfo.songkey).append(")");
                    }
                    newItems.append("\n");
                }

                myView.content.post(() -> {
                    if (showNotFoundToast) {
                        InformationBottomSheet informationBottomSheet = new InformationBottomSheet(import_set_string,set_items_not_found_string,null,null);
                        informationBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"InformationBottomSheet");
                    }
                    basename = mainActivityInterface.getConvertOnSong().getSetTitle();
                    myView.filename.setText(basename);
                    myView.content.setText(basename);
                    myView.content.setHint(newItems.toString().trim());
                });
            }
        } else {
            String textString = mainActivityInterface.getStorageAccess().readTextFileToString(
                    mainActivityInterface.getStorageAccess().getInputStream(mainActivityInterface.getImportUri()));
            if (textString != null) {
                String[] items = textString.split("<slide_group name=\"");
                StringBuilder newItems = new StringBuilder();
                for (String item : items) {
                    if (item.contains("\"")) {
                        item = item.substring(0, item.indexOf("\""));
                    }
                    item = item.replace("# ", "");
                    if (!item.contains("<?xml")) {
                        newItems.append(item.trim()).append("\n");
                    }
                }
                myView.content.post(() -> {
                    myView.content.setText(basename);
                    myView.content.setHint(newItems.toString().trim());
                });
                myView.folder.post(() -> {
                    if (!folders.contains(setcategory)) {
                        folders.add(setcategory);
                        exposedDropDownArrayAdapter.notifyDataSetChanged();
                    }
                    myView.folder.setText(setcategory);
                });
            }
        }
    }

    private void readInFile() {
        if (isIMGorPDF && getActivity()!=null && getContext()!=null) {
            if (mainActivityInterface.getStorageAccess().isSpecificFileExtension("PDF",mainActivityInterface.getImportFilename()) &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Load in a preview if the version of Android is high enough
                Bitmap bmp = mainActivityInterface.getProcessSong().getBitmapFromPDF(null,null,1,200,200,"N", true);
                myView.imageView.post(()-> Glide.with(getContext()).load(bmp).into(myView.imageView));
                newSong.setFiletype("PDF");
            } else {
                myView.imageView.post(()-> Glide.with(getContext()).load(mainActivityInterface.getImportUri()).into(myView.imageView));
                newSong.setFiletype("IMG");
            }
        } else if (mainActivityInterface.getImportFilename()!=null){
            // Because ost files aren't normally allowed (BAD extension) in the song folder
            // This would cause the file to be read as text if it has this extension
            // We set a pass go variable for now so that isn't checked again when reading the import
            if (mainActivityInterface.getImportFilename().endsWith(".ost")) {
                newSong.setFiletype("XML");
            }

            boolean isText = mainActivityInterface.getStorageAccess().isSpecificFileExtension("text",mainActivityInterface.getImportFilename());
            boolean isChoPro = mainActivityInterface.getStorageAccess().isSpecificFileExtension("chordpro",mainActivityInterface.getImportFilename());
            boolean isOnSong = mainActivityInterface.getStorageAccess().isSpecificFileExtension("onsong",mainActivityInterface.getImportFilename());
            boolean isWord = mainActivityInterface.getStorageAccess().isSpecificFileExtension("docx",mainActivityInterface.getImportFilename());
            boolean isJustChords = mainActivityInterface.getStorageAccess().isSpecificFileExtension("justchords",mainActivityInterface.getImportFilename());
            String content = "";

            if (isText || isChoPro || isOnSong || isJustChords) {
                content = mainActivityInterface.getStorageAccess().readTextFileToString(mainActivityInterface.getStorageAccess().getInputStream(mainActivityInterface.getImportUri()));
                newSong.setLyrics(content);
            }
            if (isText) {
                newSong.setLyrics(mainActivityInterface.getConvertTextSong().convertText(content));
            } else if (isChoPro) {
                newSong = mainActivityInterface.getConvertChoPro().convertTextToTags(mainActivityInterface.getImportUri(),newSong);
            } else if (isOnSong) {
                newSong = mainActivityInterface.getConvertOnSong().convertTextToTags(mainActivityInterface.getImportUri(), newSong);
            } else if (isWord) {
                newSong.setLyrics(mainActivityInterface.getConvertWord().convertDocxToText(mainActivityInterface.getImportUri(), mainActivityInterface.getImportFilename()));
            } else if (isJustChords) {
                JustChordsObject justChordsObject = mainActivityInterface.getConvertJustChords().getJustChordsObjectFromImportUri();
                JustChordsSongObject justChordsSongObject = mainActivityInterface.getConvertJustChords().getJustChordsSongObject(justChordsObject, 0);
                if (justChordsSongObject != null) {
                    newSong = mainActivityInterface.getConvertJustChords().getOpenSongFromJustChordsSong(justChordsSongObject);
                    basename = newSong.getTitle();
                }
            } else {
                mainActivityInterface.getLoadSong().setImportingFile(true);
                try {
                    newSong.setFolder(mainActivityInterface.getMainfoldername());
                    newSong.setFilename("importUri");
                    newSong.setFiletype("XML");
                    if (mainActivityInterface.getImportUri()!=null) {
                        newSong.setEncoding(mainActivityInterface.getImportUri().toString());
                    }
                    newSong = mainActivityInterface.getLoadSong().doLoadSongFile(newSong, false);
                    newSong.setFilename(newSong.getTitle());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            myView.content.post(()-> {
                        myView.content.setText(newSong.getTitle());
                        if (getContext()!=null) {
                            myView.content.setHintMonospace(getContext());
                        }
                        myView.content.setHint(newSong.getLyrics());
                    });
            // Because we have loaded the song (sort of), we need to reset the mainActivity.getSong()
            // As this will have been changed by the load process
            if (mainActivityInterface.getLoadSong().getImportingFile()) {
                mainActivityInterface.getLoadSong().setImportingFile(false);
                mainActivityInterface.getSong().setFolder(originalFolder);
                mainActivityInterface.getSong().setFilename(originalFilename);
                mainActivityInterface.getPreferences().setMyPreferenceString("songFolder", originalFolder);
                mainActivityInterface.getPreferences().setMyPreferenceString("songFilename", originalFilename);
            }
        }
    }

    private void updateViews() {
        myView.filename.setText(basename);

        if (isIMGorPDF) {
            myView.imageView.setVisibility(View.VISIBLE);
        } else {
            myView.content.setVisibility(View.VISIBLE);
        }

        // Check if file with this name already exists
        checkIfFileExists();
    }

    private boolean checkIfFileExists() {
        newSong.setFilename(myView.filename.getText().toString());
        newSong.setTitle(myView.filename.getText().toString());
        newSong.setFolder(myView.folder.getText().toString());

        String folder = "Songs";
        String subfolder = myView.folder.getText().toString();
        String filename = myView.filename.getText().toString();

        if (isSetFile) {
            folder = "Sets";
            subfolder = "";
            filename = myView.folder.getText().toString() +
                    mainActivityInterface.getSetActions().getSetCategorySeparator() +
                    myView.filename.getText().toString();
        }
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(folder,subfolder,filename);
        if (!isSetFile && mainActivityInterface.getStorageAccess().uriExists(uri)) {
            myView.existing.setVisibility(View.VISIBLE);
            return true;
        } else {
            myView.existing.setVisibility(View.GONE);
            return false;
        }
    }

    private void setupListeners() {
        myView.filename.addTextChangedListener(new MyTextWatcher());
        myView.folder.addTextChangedListener(new MyTextWatcher());
        myView.importButton.setOnClickListener(v->doImport());
    }

    private void doImport() {
        if (isSetFile) {
            // Import the set if it doesn't already exist and filename isn't empty
            String folderprefix = "";
            if (!myView.folder.getText().toString().isEmpty() &&
                !myView.folder.getText().toString().equals(mainfoldername_string)) {
                folderprefix = myView.folder.getText().toString() + "__";
            }
            String filename = myView.filename.getText().toString();
            if (!filename.isEmpty()) {
                newSetName = folderprefix + filename;
                copyTo = mainActivityInterface.getStorageAccess().getUriForItem("Sets", "",
                        newSetName);
                if (mainActivityInterface.getStorageAccess().uriExists(copyTo)) {
                    AreYouSureBottomSheet areYouSureBottomSheet = new AreYouSureBottomSheet("importSetIntent",
                            overwrite_string, null, "ImportFileFragment_Set", this, null);
                    areYouSureBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "AreYouSure");
                } else {
                    finishImportSet();
                }
            } else {
                mainActivityInterface.getShowToast().doIt(filename_string+" "+error_string);
            }


        } else {
            // Only proceed if song doesn't exist or we have checked the overwrite button
            if (checkIfFileExists() && !myView.overwrite.isChecked()) {
                // Don't proceed
                mainActivityInterface.getShowToast().doIt(file_exists_string);
            } else {
                showProgress(true);
                // Create the new song location
                String folder = myView.folder.getText().toString();
                String filename = myView.filename.getText().toString();

                // Put the required extension back if it isn't there
                if (!requiredExtension.isEmpty() && !filename.toLowerCase(Locale.ROOT).contains(requiredExtension.toLowerCase(Locale.ROOT))) {
                    filename = filename + requiredExtension;
                }
                myView.filename.setText(filename);

                String newFilename = filename;
                mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                    boolean success;
                    if (isIMGorPDF) {
                        // Now copy the file
                        copyTo = mainActivityInterface.getStorageAccess().getUriForItem("Songs", folder, newFilename);
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" Copy to Songs/"+folder+"/"+newFilename+"  deleteOld=true");
                        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, copyTo, null, "Songs", folder, newFilename);
                        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(copyTo);
                        InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(mainActivityInterface.getImportUri());
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doImport copyFile from "+mainActivityInterface.getImportUri()+" to Songs/" + folder + "/" + newFilename);
                        success = mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream);

                    } else if (newSong.getFilename()!=null &&
                            !newSong.getFilename().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                        // This is now a proper song, so write it
                        newSong.setFolder(folder);
                        newSong.setFilename(newFilename);
                        newSong.setFiletype("XML");
                        copyTo = mainActivityInterface.getStorageAccess().getUriForItem("Songs", folder, newFilename);
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" Copy to Songs/"+folder+"/"+newFilename+"  deleteOld=true");
                        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, copyTo, null, "Songs", folder, newFilename);
                        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(copyTo);
                        String xml = mainActivityInterface.getProcessSong().getXML(newSong);
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doImport writeFileFromString Songs/"+folder+"/"+newFilename+" with: "+xml);
                        success = mainActivityInterface.getStorageAccess().writeFileFromString(xml, outputStream);
                    } else {
                        success = false;
                    }

                    if (success) {
                        // Add to the database
                        if (isIMGorPDF) {
                            // Add to the persistent database
                            mainActivityInterface.getNonOpenSongSQLiteHelper().createSong(folder, newFilename);
                        }

                        mainActivityInterface.getSQLiteHelper().createSong(folder, newFilename);
                        mainActivityInterface.getSQLiteHelper().updateSong(newSong);

                        // Set the song
                        mainActivityInterface.getSong().setFolder(folder);
                        mainActivityInterface.getSong().setFilename(newFilename);
                        mainActivityInterface.getPreferences().setMyPreferenceString("songFolder", folder);
                        mainActivityInterface.getPreferences().setMyPreferenceString("songFilename", newFilename);
                        mainActivityInterface.updateSongList();

                        mainActivityInterface.getMainHandler().post(() -> {
                            showProgress(false);
                            mainActivityInterface.navHome();
                        });
                    } else {
                        mainActivityInterface.getMainHandler().post(() ->
                                mainActivityInterface.getShowToast().error());
                    }
                });
            }
        }
    }

    public void finishImportSet() {
        if (getActivity()!=null) {
            mainActivityInterface.getMainHandler().post(() -> {
                showProgress(true);
                myView.importButton.setEnabled(false);
                myView.importButton.hide();
                myView.importProgressScrim.setVisibility(View.VISIBLE);
                mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                    try {
                        // Clear the existing set
                        boolean oktocontinue = false;
                        // Get a note of how many items were in the currently loaded set
                        int oldSize = mainActivityInterface.getCurrentSet().getCurrentSetSize();
                        // Initialise the current set
                        mainActivityInterface.getCurrentSet().initialiseTheSet();
                        mainActivityInterface.getCurrentSet().setSetCurrent("");
                        mainActivityInterface.getCurrentSet().setSetCurrentBeforeEdits("");
                        //mainActivityInterface.getSetActions().clearCurrentSet();

                        // Notify the set menu to update to an empty set
                        mainActivityInterface.notifySetFragment("clear",oldSize);
                        mainActivityInterface.getCurrentSet().setIndexSongInSet(-1);
                        mainActivityInterface.getCurrentSet().setPrevIndexSongInSet(-1);

                        if (isHTML && setItemInfos!=null && !setItemInfos.isEmpty()) {
                            // Use the setItemInfo to create a new set from our OnSong HTML set
                            mainActivityInterface.getCurrentSet().setSetCurrentLastName(mainActivityInterface.getConvertOnSong().getSetTitle());
                            for (SetItemInfo setItemInfo:setItemInfos) {
                                mainActivityInterface.getCurrentSet().addItemToSet(setItemInfo, false);
                            }
                            // Save this as an actual set file
                            mainActivityInterface.getSetActions().setUseThisLastModifiedDate(mainActivityInterface.getTimeTools().getNowIsoTime());
                            String xml = mainActivityInterface.getSetActions().createSetXML(mainActivityInterface.getCurrentSet());
                            mainActivityInterface.getSetActions().setUseThisLastModifiedDate(null);

                            copyTo = mainActivityInterface.getStorageAccess().getUriForItem("Sets", "", newSetName);
                            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " Finish import  Sets/" + newSetName + "  deleteOld=true");
                            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true,copyTo,null,"Sets","",newSetName);
                            OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(copyTo);
                            mainActivityInterface.getStorageAccess().writeFileFromString(xml,outputStream);
                            // Now clear the currentSet aagin
                            mainActivityInterface.getSetActions().clearCurrentSet();
                            oktocontinue = true;

                        } else if (!isHTML) {
                            // Copy the file
                            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(mainActivityInterface.getImportUri());
                            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " Finish import  Sets/" + newSetName + "  deleteOld=true");
                            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, copyTo, null, "Sets", "", newSetName);
                            OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(copyTo);
                            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " finishImportSet copyFile from " + mainActivityInterface.getImportUri() + " to Sets/" + newSetName);
                            // Don't delete!
                            Log.d(TAG, "copy: " + mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream));
                            oktocontinue = true;
                        }

                        if (oktocontinue) {
                            ArrayList<Uri> thisSet = new ArrayList<>();
                            thisSet.add(copyTo);
                            mainActivityInterface.getMainHandler().post(() -> {
                                mainActivityInterface.setWhattodo("pendingLoadSet");
                                mainActivityInterface.getSetActions().loadSets(thisSet, mainActivityInterface.getCurrentSet(), newSetName);
                                showProgress(false);
                                //mainActivityInterface.navHome();
                                mainActivityInterface.getShowToast().success();


                                //mainActivityInterface.setWhattodo("pendingLoadSet");
                                mainActivityInterface.getCurrentSet().updateSetTitleView();
                                mainActivityInterface.navHome();
                                Log.d(TAG,"getting here");
                                //mainActivityInterface.chooseMenu(true);
                            });
                            //mainActivityInterface.getMainHandler().postDelayed(() -> mainActivityInterface.chooseMenu(true), 1000);
                        } else {
                            mainActivityInterface.getShowToast().doIt(error_string);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        mainActivityInterface.getMainHandler().post(() -> {
                            mainActivityInterface.getShowToast().error();
                            myView.importButton.setEnabled(true);
                            myView.importButton.show();
                            myView.importProgressScrim.setVisibility(View.GONE);
                        });
                    }
                });
            });
        }
    }

    private class MyTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            checkIfFileExists();
        }
    }

    private void showProgress(boolean show) {
        mainActivityInterface.getMainHandler().post(() -> {
                if (myView!=null) {
                    try {
                        myView.progress.setVisibility(show ? View.VISIBLE : View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        });
    }
}
