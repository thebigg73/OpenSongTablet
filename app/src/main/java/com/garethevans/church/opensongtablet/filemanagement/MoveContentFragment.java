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
import com.garethevans.church.opensongtablet.appdata.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.StorageMoveBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class MoveContentFragment extends Fragment {

    //private final String fragName;
    //private final Fragment callingFragment;
    MainActivityInterface mainActivityInterface;
    StorageMoveBinding myView;
    String subfolder;
    String newFolder;
    ArrayList<String> files;
    ArrayList<Uri> uris;
    ArrayList<String> filesChosen;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = (StorageMoveBinding.inflate(inflater, container, false));
        mainActivityInterface.updateToolbar(getString(R.string.folder_move_contents));

        subfolder = getArguments().get("subdir").toString();
        if (subfolder==null || subfolder.isEmpty()) {
            subfolder = getString(R.string.mainfoldername);
        }

        // Get folders we can move into
        getFromFolders();
        getDestinationFolders();

        // Prepare the list of files in the folder
        listFilesInFolder();

        // Listen for the dialog ok/close
        myView.doMove.setOnClickListener(b -> doMove());
        myView.selectAllCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> checkAll(isChecked));

        return myView.getRoot();
    }

    private void listFilesInFolder() {
        // Do this is another thread
        myView.progressBar.setVisibility(View.VISIBLE);
        myView.selectAllCheckBox.setChecked(false);
        myView.folderContentsLayout.removeAllViews();
        new Thread(() -> {
            files = mainActivityInterface.getStorageAccess().listFilesInFolder(requireContext(), mainActivityInterface.getPreferences(), "Songs", subfolder);
            if (files.size() != 0) {
                Collections.sort(files);
                getActivity().runOnUiThread(() -> {
                    for (String f : files) {
                        CheckBox cb = new CheckBox(requireContext());
                        cb.setText(f);
                        cb.setPadding(12,12,12,12);
                        myView.folderContentsLayout.addView(cb);
                    }
                });
            }
            getActivity().runOnUiThread(() -> {
                myView.progressBar.setVisibility(View.GONE);
                myView.folderContentsLayout.invalidate();
            });
        }).start();
    }

    private void getFromFolders() {
        // Do this in another thread
        new Thread(() -> {
            ArrayList<String> availableFromFolders = mainActivityInterface.getStorageAccess().getSongFolders(requireContext(),
                    mainActivityInterface.getStorageAccess().listSongs(requireContext(), mainActivityInterface.getPreferences(), mainActivityInterface.getLocale()), true, null);
            getActivity().runOnUiThread(() -> {
                if (availableFromFolders.size() != 0) {
                    ExposedDropDownArrayAdapter folderFromArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),
                            R.layout.exposed_dropdown, availableFromFolders);
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
        }).start();
    }
    private void getDestinationFolders() {
        // Do this in another thread
        new Thread(() -> {
            // This lists the folders available (minus the current one)
            ArrayList<String> availableMoveFolders = mainActivityInterface.getStorageAccess().getSongFolders(requireContext(),
                    mainActivityInterface.getStorageAccess().listSongs(requireContext(), mainActivityInterface.getPreferences(), mainActivityInterface.getLocale()), true, subfolder);

            getActivity().runOnUiThread(() -> {
                if (availableMoveFolders.size() != 0) {
                    ExposedDropDownArrayAdapter folderArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),
                            R.layout.exposed_dropdown, availableMoveFolders);
                    myView.folderChoice.setAdapter(folderArrayAdapter);
                    myView.folderChoice.setText(availableMoveFolders.get(0));
                }
            });
        }).start();
    }

    private void doMove() {
        // Go through each file and copy them to the new location
        // Then delete each original file if the copy was successful
        myView.progressBar.setVisibility(View.VISIBLE);
        myView.progressText.setVisibility(View.VISIBLE);
        myView.doMove.setVisibility(View.GONE);

        filesChosen = new ArrayList<>();
        for (int x = 0; x < files.size(); x++) {
            if (((CheckBox) myView.folderContentsLayout.getChildAt(x)).isChecked()) {
                filesChosen.add(files.get(x));
                Log.d("MoveContents","Adding "+files.get(x));
            }
        }

        // Where are we moving to?
        newFolder = myView.folderChoice.getText().toString();

        // Do this in a new thread
        new Thread(() -> {
            // Go through the checklists and add the checked ones
            uris = new ArrayList<>();
            for (String file : filesChosen) {
                uris.add(mainActivityInterface.getStorageAccess().getUriForItem(requireContext(), mainActivityInterface.getPreferences(), "Songs", subfolder, file));
            }

            InputStream inputStream;
            Uri outputFile;
            OutputStream outputStream;
            Log.d("MoveContents","filesChosen.size()="+filesChosen.size());
            try {
                for (int x = 0; x < filesChosen.size(); x++) {
                    outputFile = mainActivityInterface.getStorageAccess().getUriForItem(requireContext(), mainActivityInterface.getPreferences(), "Songs", newFolder, filesChosen.get(x));
                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(requireContext(), mainActivityInterface.getPreferences(), outputFile,
                            null, "Songs", newFolder, filesChosen.get(x));
                    inputStream = mainActivityInterface.getStorageAccess().getInputStream(requireContext(), uris.get(x));
                    outputStream = mainActivityInterface.getStorageAccess().getOutputStream(requireContext(), outputFile);
                    // Update the progress
                    String finalMessage = subfolder + "/" + filesChosen.get(x) + " > " + newFolder + "/" + filesChosen.get(x);
                    getActivity().runOnUiThread(() -> myView.progressText.setText(finalMessage));
                    if (mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream)) {
                        mainActivityInterface.getStorageAccess().deleteFile(requireContext(), uris.get(x));
                    } else {
                        Log.d("d","error copying "+finalMessage);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            getActivity().runOnUiThread(() -> {
                myView.progressText.setVisibility(View.GONE);
                myView.doMove.setVisibility(View.VISIBLE);
                mainActivityInterface.getShowToast().doIt(requireContext(),getString(R.string.success));

                // Now reload the folder contents
                listFilesInFolder();
            });
        }).start();
    }

    private void checkAll(boolean isChecked) {
        new Thread(() -> getActivity().runOnUiThread(() -> {
            for (int x = 0; x < getNumCheckBoxes(); x++) {
                ((CheckBox) myView.folderContentsLayout.getChildAt(x)).setChecked(isChecked);
            }
        })).start();
    }

    private int getNumCheckBoxes() {
        return myView.folderContentsLayout.getChildCount();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}