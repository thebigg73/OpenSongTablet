package com.garethevans.church.opensongtablet.importsongs;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.SettingsImportFileBinding;
import com.garethevans.church.opensongtablet.filemanagement.AreYouSureBottomSheet;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImportFileFragment extends Fragment {

    private final String TAG = "ImportFileFragment";
    private MainActivityInterface mainActivityInterface;
    private SettingsImportFileBinding myView;
    private Song newSong = new Song();
    private ArrayList<String> folders;
    private boolean isIMGorPDF;
    private String basename, requiredExtension, setcategory, newSetName;
    private Uri tempFile, copyTo;
    private String originalFolder, originalFilename, import_from_file_string="", error_string="",
            mainfoldername_string="", overwrite_string="", filename_string="", file_exists_string="";
    private ExposedDropDownArrayAdapter exposedDropDownArrayAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsImportFileBinding.inflate(inflater,container,false);

        prepareStrings();

        mainActivityInterface.updateToolbar(import_from_file_string);
        myView.nestedScrollView.setExtendedFabToAnimate(myView.importButton);

        // Get a note of the original folder/filename
        originalFolder = mainActivityInterface.getSong().getFolder();
        originalFilename = mainActivityInterface.getSong().getFilename();
        // Hide everything other than the progress bar while we process the song
        myView.progress.setVisibility(View.VISIBLE);
        myView.content.setVisibility(View.GONE);
        myView.importButton.setVisibility(View.GONE);
        myView.imageView.setVisibility(View.GONE);
        myView.folder.setVisibility(View.GONE);
        myView.filename.setVisibility(View.GONE);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            // Get the available folders and base name
            basename = mainActivityInterface.getImportFilename().replaceAll("\\.[^.]*$", "");
            setcategory = mainfoldername_string;
            if (mainActivityInterface.getImportFilename().endsWith(".osts")) {
                // This is actually a set
                folders = mainActivityInterface.getSetActions().getCategories(mainActivityInterface.getSetActions().getAllSets());
                if (basename.contains("__")) {
                    // This set has a category already - get it!
                    String[] bits = basename.split("__");
                    if (bits.length==2) {
                        setcategory = bits[0];
                        basename = bits[1];
                    }
                }
            } else {
                folders = mainActivityInterface.getSQLiteHelper().getFolders();
            }
            newSong.setFilename(mainActivityInterface.getImportFilename());
            isIMGorPDF = mainActivityInterface.getStorageAccess().isIMGorPDF(newSong);
            requiredExtension = "";
            if (mainActivityInterface.getImportFilename().contains(".") && isIMGorPDF) {
                requiredExtension = mainActivityInterface.getImportFilename().substring(mainActivityInterface.getImportFilename().lastIndexOf("."));
                basename = mainActivityInterface.getImportFilename();
            }

            // Set up the folder exposed dropdown
            handler.post(() -> {
                if (getContext()!=null) {
                    exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.folder, R.layout.view_exposed_dropdown_item, folders);
                    myView.folder.setAdapter(exposedDropDownArrayAdapter);
                }
                // Default to the current folder
                myView.folder.setText(mainActivityInterface.getSong().getFolder());
            });
            // Try to read in the song using the import information.  This copies to the Variations/_cache folder
            if (mainActivityInterface.getImportFilename().endsWith(".osts")) {
                readInSetFile();

            } else {
                readInFile();
            }

            handler.post(() -> {
                // Update the views and get the
                updateViews();

                // Add listeners
                setupListeners();

                myView.progress.setVisibility(View.GONE);
                myView.filename.setVisibility(View.VISIBLE);
                myView.folder.setVisibility(View.VISIBLE);
                myView.importButton.setVisibility(View.VISIBLE);
            });
        });

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            import_from_file_string = getString(R.string.import_from_file);
            mainfoldername_string = getString(R.string.mainfoldername);
            overwrite_string = getString(R.string.overwrite);
            filename_string = getString(R.string.filename);
            error_string = getString(R.string.error);
            file_exists_string = getString(R.string.file_exists);
        }
    }

    private void readInSetFile() {
        // Read in as a text string
        String textString = mainActivityInterface.getStorageAccess().readTextFileToString(
                mainActivityInterface.getStorageAccess().getInputStream(mainActivityInterface.getImportUri()));
        if (textString!=null) {
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
            myView.content.post(()-> {
                myView.content.setText(basename);
                myView.content.setHint(newItems.toString().trim());
            });
            myView.folder.post(()-> {
                if (!folders.contains(setcategory)) {
                    folders.add(setcategory);
                    exposedDropDownArrayAdapter.notifyDataSetChanged();
                }
                myView.folder.setText(setcategory);
            });
        }
    }

    private void readInFile() {
        // Make a temporary copy of the song in the Variations/_cache folder
        InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(mainActivityInterface.getImportUri());
        tempFile = mainActivityInterface.getStorageAccess().getUriForItem("Variations","_cache",mainActivityInterface.getImportFilename());
        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+"Read in file Variations/_cache/"+mainActivityInterface.getImportFilename()+"  deleteOld=false");
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false,tempFile,null,"Variations","_cache",mainActivityInterface.getImportFilename());
        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(tempFile);
        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" readInFile copyFile from " + mainActivityInterface.getImportUri()+" to Variations/_cache/"+mainActivityInterface.getImportFilename());

        mainActivityInterface.getStorageAccess().copyFile(inputStream,outputStream);

        if (isIMGorPDF && getContext()!=null) {
            if (mainActivityInterface.getImportFilename().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                // Load in a preview if the version of Android is high enough
                Bitmap bmp = mainActivityInterface.getProcessSong().getBitmapFromPDF(null,null,1,200,200,"N");
                myView.imageView.post(()-> Glide.with(getContext()).load(bmp).into(myView.imageView));
                newSong.setFiletype("PDF");
            } else {
                myView.imageView.post(()->Glide.with(getContext()).load(mainActivityInterface.getImportUri()).into(myView.imageView));
                newSong.setFiletype("IMG");
            }
        } else {
            try {
                newSong.setFolder("**Variation/_cache");
                newSong = mainActivityInterface.getLoadSong().doLoadSongFile(newSong,false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            myView.content.post(()-> {
                        myView.content.setText(newSong.getTitle());
                        myView.content.setHintMonospace();
                        myView.content.setHint(newSong.getLyrics());
                    });

            // Because we have loaded the song (sort of), we need to reset the mainActivity.getSong()
            // As this will have been changed by the load process
            mainActivityInterface.getSong().setFolder(originalFolder);
            mainActivityInterface.getSong().setFilename(originalFilename);
            mainActivityInterface.getPreferences().setMyPreferenceString("songFolder",originalFolder);
            mainActivityInterface.getPreferences().setMyPreferenceString("songFilename",originalFilename);
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

        mainActivityInterface.setImportUri(mainActivityInterface.getStorageAccess().getUriForItem(
                "Songs",myView.folder.getText().toString(),myView.filename.getText().toString()));
        if (mainActivityInterface.getStorageAccess().uriExists(mainActivityInterface.getImportUri())) {
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
        if (mainActivityInterface.getImportFilename().toLowerCase(Locale.ROOT).endsWith(".osts")) {
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
                // Create the new song location
                String folder = myView.folder.getText().toString();
                String filename = myView.filename.getText().toString();

                boolean success;

                if (isIMGorPDF) {
                    // Put the required extension back if it isn't there
                    if (!requiredExtension.isEmpty() && !filename.toLowerCase(Locale.ROOT).contains(requiredExtension.toLowerCase(Locale.ROOT))) {
                        filename = filename + requiredExtension;
                    }
                    myView.filename.setText(filename);
                    // Now copy the file
                    copyTo = mainActivityInterface.getStorageAccess().getUriForItem("Songs", folder, filename);
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" Copy to Songs/"+folder+"/"+filename+"  deleteOld=true");
                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, copyTo, null, "Songs", folder, filename);
                    OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(copyTo);
                    InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(tempFile);
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doImport copyFile from "+tempFile+" to Songs/" + folder + "/" + filename);
                    success = mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream);
                    Log.d(TAG, "success:" + success);

                } else {
                    // This is now a proper song, so write it
                    newSong.setFolder(folder);
                    newSong.setFilename(filename);
                    newSong.setFiletype("XML");
                    copyTo = mainActivityInterface.getStorageAccess().getUriForItem("Songs", folder, filename);
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" Copy to Songs/"+folder+"/"+filename+"  deleteOld=true");
                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, copyTo, null, "Songs", folder, filename);
                    OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(copyTo);
                    String xml = mainActivityInterface.getProcessSong().getXML(newSong);
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doImport writeFileFromString Songs/"+folder+"/"+filename+" with: "+xml);
                    success = mainActivityInterface.getStorageAccess().writeFileFromString(xml, outputStream);
                }

                if (success) {
                    // Now delete the old song and proceed
                    // Remove the temp file from the variations
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doImport deleteFile "+tempFile);
                    mainActivityInterface.getStorageAccess().deleteFile(tempFile);

                    // Add to the database
                    if (isIMGorPDF) {
                        // Add to the persistent database
                        mainActivityInterface.getNonOpenSongSQLiteHelper().createSong(folder, filename);
                    }

                    mainActivityInterface.getSQLiteHelper().createSong(folder, filename);
                    mainActivityInterface.getSQLiteHelper().updateSong(newSong);

                    // Set the song
                    mainActivityInterface.getSong().setFolder(folder);
                    mainActivityInterface.getSong().setFilename(filename);
                    mainActivityInterface.getPreferences().setMyPreferenceString("songFolder", folder);
                    mainActivityInterface.getPreferences().setMyPreferenceString("songFilename", filename);
                    mainActivityInterface.updateSongList();
                    mainActivityInterface.navHome();
                } else {
                    mainActivityInterface.getShowToast().doIt(error_string);
                }
            }
        }
    }

    public void finishImportSet() {
        if (getActivity()!=null) {
            try {
                // Copy the file
                File tempLoc = new File(getActivity().getExternalCacheDir(), "Import");
                Log.d(TAG, "Create folder:" + tempLoc.mkdirs());
                File tempFile = new File(tempLoc, mainActivityInterface.getImportFilename());
                InputStream inputStream = new FileInputStream(tempFile);
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " Finish import  Sets/" + newSetName + "  deleteOld=true");
                mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, copyTo, null, "Sets", "", newSetName);
                OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(copyTo);
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " finishImportSet copyFile from " + tempFile + " to Sets/" + newSetName);
                Log.d(TAG, "copy: " + mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream));
                ArrayList<Uri> thisSet = new ArrayList<>();
                thisSet.add(copyTo);
                mainActivityInterface.getSetActions().loadSets(thisSet, newSetName);
                mainActivityInterface.navHome();
                mainActivityInterface.chooseMenu(true);
            } catch (Exception e) {
                e.printStackTrace();
                mainActivityInterface.getShowToast().doIt(error_string);
            }
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
}
