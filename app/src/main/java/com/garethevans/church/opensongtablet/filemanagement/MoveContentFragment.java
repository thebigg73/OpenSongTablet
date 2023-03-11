package com.garethevans.church.opensongtablet.filemanagement;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.StorageMoveBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.android.material.snackbar.Snackbar;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class MoveContentFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private StorageMoveBinding myView;
    private String subfolder, newFolder, folder_move_contents_string="", mainfoldername_string="",
            success_string="";
    private ArrayList<String> files, filesChosen;
    private ArrayList<Uri> uris;
    private final String TAG = "MoveContentsFragment";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = (StorageMoveBinding.inflate(inflater, container, false));

        prepareStrings();

        mainActivityInterface.updateToolbar(folder_move_contents_string);

        if (getArguments()!=null && getArguments().containsKey("subdir")) {
            subfolder = getArguments().get("subdir").toString();
        }
        if (subfolder==null || subfolder.isEmpty()) {
            subfolder = mainfoldername_string;
        }

        // Get folders we can move into
        getFromFolders();
        getDestinationFolders();

        // Prepare the list of files in the folder
        listFilesInFolder();

        // Listen for the dialog okay/close
        myView.doMove.setOnClickListener(b -> doMove());
        myView.selectAllCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> checkAll(isChecked));

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            folder_move_contents_string = getString(R.string.folder_move_contents);
            mainfoldername_string = getString(R.string.mainfoldername);
            success_string = getString(R.string.success);
        }
    }
    private void listFilesInFolder() {
        // Do this is another thread
        myView.progressBar.setVisibility(View.VISIBLE);
        myView.selectAllCheckBox.setChecked(false);
        myView.folderContentsLayout.removeAllViews();
        new Thread(() -> {
            files = mainActivityInterface.getStorageAccess().listFilesInFolder("Songs", subfolder);
            if (files.size() != 0) {
                Collections.sort(files);
                if (getActivity()!=null && getContext()!=null) {
                    getActivity().runOnUiThread(() -> {
                        for (String f : files) {
                            CheckBox cb = new CheckBox(getContext());
                            cb.setText(f);
                            cb.setPadding(12, 12, 12, 12);
                            myView.folderContentsLayout.addView(cb);
                        }
                    });
                }
            }
            if (getActivity()!=null) {
                getActivity().runOnUiThread(() -> {
                    myView.progressBar.setVisibility(View.GONE);
                    myView.folderContentsLayout.invalidate();
                });
            }
        }).start();
    }

    private void getFromFolders() {
        // Do this in another thread
        new Thread(() -> {
            ArrayList<String> availableFromFolders = mainActivityInterface.getStorageAccess().getSongFolders(
                    mainActivityInterface.getStorageAccess().listSongs(), true, null);
            if (getActivity()!=null) {
                getActivity().runOnUiThread(() -> {
                    if (availableFromFolders.size() != 0 && getContext()!=null) {
                        ExposedDropDownArrayAdapter folderFromArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                                myView.currentFolderChoice, R.layout.view_exposed_dropdown_item, availableFromFolders);
                        myView.currentFolderChoice.setAdapter(folderFromArrayAdapter);
                        myView.currentFolderChoice.setText(subfolder);
                        myView.currentFolderChoice.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                subfolder = s.toString();
                                getDestinationFolders();
                                listFilesInFolder();
                            }
                        });
                    }
                });
            }
        }).start();
    }
    private void getDestinationFolders() {
        // Do this in another thread
        new Thread(() -> {
            // This lists the folders available (minus the current one)
            ArrayList<String> availableMoveFolders = mainActivityInterface.getStorageAccess().getSongFolders(
                    mainActivityInterface.getStorageAccess().listSongs(), true, subfolder);

            if (getActivity()!=null) {
                getActivity().runOnUiThread(() -> {
                    if (availableMoveFolders.size() != 0 && getContext()!=null) {
                        ExposedDropDownArrayAdapter folderArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                                myView.folderChoice, R.layout.view_exposed_dropdown_item, availableMoveFolders);
                        myView.folderChoice.setAdapter(folderArrayAdapter);
                        myView.folderChoice.setText(availableMoveFolders.get(0));
                    }
                });
            }
        }).start();
    }

    private void doMove() {
        // Don't allow this until song indexing is complete as it can mess up the database!
        // If we don't pass this test, we are shown a snackbar message
        filesChosen = new ArrayList<>();

        for (int x = 0; x < files.size(); x++) {
            if (((CheckBox) myView.folderContentsLayout.getChildAt(x)).isChecked()) {
                filesChosen.add(files.get(x));
                Log.d(TAG, "Adding " + files.get(x));
            }
        }

        if (checkIndexingStatus() && safeMove(filesChosen)) {
            // Go through each file and copy them to the new location
            // Then delete each original file if the copy was successful
            // Finally update the database
            myView.progressBar.setVisibility(View.VISIBLE);
            myView.progressText.setVisibility(View.VISIBLE);
            myView.doMove.setVisibility(View.GONE);

            // Where are we moving to?
            newFolder = myView.folderChoice.getText().toString();

            // Do this in a new thread
            new Thread(() -> {
                // Go through the checklists and add the checked ones
                uris = new ArrayList<>();
                for (String file : filesChosen) {
                    uris.add(mainActivityInterface.getStorageAccess().getUriForItem("Songs", subfolder, file));
                }

                InputStream inputStream;
                Uri outputFile;
                OutputStream outputStream;
                Song tempSong = new Song(); // Just for location to get highlighter name

                Log.d(TAG, "filesChosen.size()=" + filesChosen.size());
                try {
                    for (int x = 0; x < filesChosen.size(); x++) {
                        outputFile = mainActivityInterface.getStorageAccess().getUriForItem("Songs", newFolder, filesChosen.get(x));
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doMove Create Songs/"+newFolder+"/"+filesChosen.get(x)+"  deleteOld=true");
                        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(
                                true, outputFile,
                                null, "Songs", newFolder, filesChosen.get(x));
                        inputStream = mainActivityInterface.getStorageAccess().getInputStream(uris.get(x));
                        outputStream = mainActivityInterface.getStorageAccess().getOutputStream(outputFile);
                        // Update the progress
                        String finalMessage = subfolder + "/" + filesChosen.get(x) + " > " + newFolder + "/" + filesChosen.get(x);
                        if (getActivity()!=null) {
                            getActivity().runOnUiThread(() -> myView.progressText.setText(finalMessage));
                        }
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doMove copyFile from "+uris.get(x)+" to Songs/" + newFolder+"/"+filesChosen.get(x));
                        if (mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream)) {
                            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doMove deleteFile "+uris.get(x));
                            mainActivityInterface.getStorageAccess().deleteFile(uris.get(x));
                            // Check we weren't viewing this file - if so, update our preference
                            if (mainActivityInterface.getSong().getFilename().equals(filesChosen.get(x)) &&
                            mainActivityInterface.getSong().getFolder().equals(subfolder)) {
                                mainActivityInterface.getSong().setFolder(newFolder);
                                mainActivityInterface.getPreferences().setMyPreferenceString(
                                        "songFolder",newFolder);
                            }
                        } else {
                            Log.d(TAG, "error copying " + finalMessage);
                        }

                        // Sort the databases.
                        mainActivityInterface.getSQLiteHelper().renameSong(subfolder, newFolder, filesChosen.get(x), filesChosen.get(x));
                        if (!mainActivityInterface.getStorageAccess().isTextFile(uris.get(x))) {
                            // Likely to be in the persistent nonOpenSong database too
                            mainActivityInterface.getNonOpenSongSQLiteHelper().renameSong(subfolder, newFolder, filesChosen.get(x), filesChosen.get(x));
                        }

                        // Try to rename highlighter files (it they exist)
                        tempSong.setFilename(filesChosen.get(x));
                        tempSong.setFolder(subfolder);
                        String portraitOld = mainActivityInterface.getProcessSong().getHighlighterFilename(tempSong,true);
                        String landscapeOld = mainActivityInterface.getProcessSong().getHighlighterFilename(tempSong,false);
                        Uri portraitOldUri = mainActivityInterface.getStorageAccess().getUriForItem("Highlighter","",portraitOld);
                        Uri landscapeOldUri = mainActivityInterface.getStorageAccess().getUriForItem("Highlighter","",landscapeOld);
                        if (mainActivityInterface.getStorageAccess().uriExists(portraitOldUri) ||
                        mainActivityInterface.getStorageAccess().uriExists(landscapeOldUri)) {
                            // Update the new song details
                            tempSong.setFiletype((filesChosen.get(x)));
                            tempSong.setFolder(newFolder);
                            String portraitNew = mainActivityInterface.getProcessSong().getHighlighterFilename(tempSong,true);
                            String landscapeNew = mainActivityInterface.getProcessSong().getHighlighterFilename(tempSong,false);

                            // Deal with portrait
                            if (mainActivityInterface.getStorageAccess().uriExists(portraitOldUri)) {
                                renameHighlighterFiles(portraitOldUri,portraitNew);
                            }

                            // Deal with landscape
                            if (mainActivityInterface.getStorageAccess().uriExists(landscapeOldUri)) {
                                renameHighlighterFiles(landscapeOldUri,landscapeNew);
                            }
                        }
                    }

                    // Update everything needed for indexing and song menus
                    try {
                        ArrayList<String> songIds = mainActivityInterface.getStorageAccess().listSongs();
                        // Write a crude text file (line separated) with the song Ids (folder/file)
                        mainActivityInterface.getStorageAccess().writeSongIDFile(songIds);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Update the song menu using the database
                    mainActivityInterface.updateSongMenu(mainActivityInterface.getSong());

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (getActivity()!=null) {
                    getActivity().runOnUiThread(() -> {
                        myView.progressText.setVisibility(View.GONE);
                        myView.doMove.setVisibility(View.VISIBLE);
                        mainActivityInterface.getShowToast().doIt(success_string);

                        // Now reload the folder contents
                        listFilesInFolder();
                    });
                }
            }).start();
        }
    }

    private void renameHighlighterFiles(Uri oldUri, String newFilename) {
        Uri highlighterOutputUri = mainActivityInterface.getStorageAccess().getUriForItem("Highlighter","",newFilename);
        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" Create Highlighter/"+newFilename+"  deleteOld=false");
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false, highlighterOutputUri,null,"Highlighter",
                "",newFilename);
        InputStream highlighterInputStream = mainActivityInterface.getStorageAccess().getInputStream(oldUri);
        OutputStream highlighterOutputStream = mainActivityInterface.getStorageAccess().getOutputStream(highlighterOutputUri);
        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" renameHighlighter copyFile from "+oldUri+" to Highligher/" + newFilename);

        boolean success = mainActivityInterface.getStorageAccess().copyFile(highlighterInputStream,highlighterOutputStream);
        if (success) {
            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" renameHighlighterFile deleteFile "+oldUri);
            mainActivityInterface.getStorageAccess().deleteFile(oldUri);
        }
        try {
            highlighterInputStream.close();
            highlighterOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void checkAll(boolean isChecked) {
        new Thread(() -> {
            if (getActivity()!=null) {
                getActivity().runOnUiThread(() -> {
                    for (int x = 0; x < getNumCheckBoxes(); x++) {
                        ((CheckBox) myView.folderContentsLayout.getChildAt(x)).setChecked(isChecked);
                    }
                });
            }
        }).start();
    }

    private int getNumCheckBoxes() {
        return myView.folderContentsLayout.getChildCount();
    }

    private boolean checkIndexingStatus() {
        if (mainActivityInterface.getSongListBuildIndex().getIndexComplete()) {
            return true;
        } else {
            Snackbar.make(myView.getRoot(), R.string.search_index_wait, Snackbar.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean safeMove(ArrayList<String> filesChosen) {
        // Check we haven't got the same location and end destination folders and they aren't empty
        String loc = myView.currentFolderChoice.getText().toString();
        String dest = myView.folderChoice.getText().toString();
        if (!loc.equals(dest) && !dest.isEmpty() && filesChosen.size() > 0) {
            return true;
        } else if (filesChosen.size() == 0) {
            Snackbar.make(myView.getRoot(), R.string.nothing_selected, Snackbar.LENGTH_LONG).show();
            return false;
        } else {
            Snackbar.make(myView.getRoot(), R.string.choose_folder, Snackbar.LENGTH_LONG).show();
            return false;
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}