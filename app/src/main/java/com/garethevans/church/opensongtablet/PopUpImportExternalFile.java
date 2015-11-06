package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class PopUpImportExternalFile extends DialogFragment {

    static PopUpImportExternalFile newInstance() {
        PopUpImportExternalFile frag;
        frag = new PopUpImportExternalFile();
        return frag;
    }

    public interface MyInterface {
        void refreshAll();
        void onSongInstall();
    }

    private MyInterface mListener;

    @Override
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    EditText fileTitle_EditText;
    TextView fileType_TextView;
    TextView chooseFolder_TextView;
    Spinner chooseFolder_Spinner;
    Button importFile_Cancel;
    Button importFile_Save;
    CheckBox overWrite_CheckBox;

    static ArrayList<String> newtempfolders;
    static String moveToFolder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getResources().getString(R.string.importnewsong));

        final View V = inflater.inflate(R.layout.popup_importexternalfile, container, false);

        // Initialise the views
        fileTitle_EditText = (EditText) V.findViewById(R.id.fileTitle_EditText);
        fileType_TextView = (TextView) V.findViewById(R.id.fileType_TextView);
        chooseFolder_TextView = (TextView) V.findViewById(R.id.chooseFolder_TextView);
        chooseFolder_Spinner = (Spinner) V.findViewById(R.id.chooseFolder_Spinner);
        importFile_Cancel = (Button) V.findViewById(R.id.importFile_Cancel);
        importFile_Save = (Button) V.findViewById(R.id.importFile_Save);
        overWrite_CheckBox = (CheckBox) V.findViewById(R.id.overWrite_CheckBox);

        // By default, we will assume this is a song
        FullscreenActivity.file_type = getResources().getString(R.string.options_song);

        FileInputStream inputStream;

        String scheme = FullscreenActivity.file_uri.getScheme();

        switch (scheme) {
            case "content":
                Cursor cursor = getActivity().getContentResolver().query(FullscreenActivity.file_uri, new String[]{
                        MediaStore.MediaColumns.DISPLAY_NAME
                }, null, null, null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        FullscreenActivity.file_name = cursor.getString(nameIndex);
                    }
                    cursor.close();
                }

                try {
                    InputStream is = getActivity().getContentResolver().openInputStream(FullscreenActivity.file_uri);
                    OutputStream os = new FileOutputStream(FullscreenActivity.homedir + "/" + FullscreenActivity.file_name);
                    FullscreenActivity.file_location = FullscreenActivity.homedir + "/" + FullscreenActivity.file_name;
                    byte[] buffer = new byte[4096];
                    int count;
                    if (is != null) {
                        while ((count = is.read(buffer)) > 0) {
                            os.write(buffer, 0, count);
                        }
                    }
                    os.close();
                    if (is != null) {
                        is.close();
                    }
                    inputStream = new FileInputStream(FullscreenActivity.file_location);
                    FullscreenActivity.file_contents = LoadXML.readTextFile(inputStream);
                } catch (Exception e) {
                    // Error
                    e.printStackTrace();
                }

                break;
            case "file":
                try {
                    inputStream = new FileInputStream(FullscreenActivity.file_location);
                    FullscreenActivity.file_contents = LoadXML.readTextFile(inputStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            default:
                dismiss();
                break;
        }



        if (FullscreenActivity.file_name.endsWith(".ost")) {
            // This is definitely a song
            FullscreenActivity.file_type = getResources().getString(R.string.options_song);
            FullscreenActivity.file_name = FullscreenActivity.file_name.replace(".ost", "");

        } else if (FullscreenActivity.file_name.endsWith(".usr") || FullscreenActivity.file_name.endsWith(".USR")) {
            // This is a song from SongSelect
            FullscreenActivity.file_type = "USR";
            FullscreenActivity.file_name = FullscreenActivity.file_name.replace("_", " ");

        } else if (FullscreenActivity.file_name.endsWith(".osts")) {
            // This is definitely a set
            FullscreenActivity.file_type = getResources().getString(R.string.options_set);
            FullscreenActivity.file_name = FullscreenActivity.file_name.replace(".osts", "");
            // Remove the 'choose folder' views as it will be saved to the sets folder
            chooseFolder_Spinner.setVisibility(View.GONE);
            chooseFolder_TextView.setVisibility(View.GONE);
            // Change the title
            getDialog().setTitle(getActivity().getResources().getString(R.string.importnewset));

        } else if (FullscreenActivity.file_name.endsWith(".backup")) {
            // This is definitely an opensong archive
            FullscreenActivity.file_type = "ONSONGARCHIVE";

            // Move the file to the correct location
            moveToFolder = FullscreenActivity.homedir.toString();
            File importIt = new File (FullscreenActivity.file_location);
            File newFile = new File (FullscreenActivity.homedir + "/" + FullscreenActivity.file_name);
            if (importIt.renameTo(newFile)) {
                // Now it is in the right location, open the onsong import popup;
                dismiss();
                mListener.onSongInstall();
            } else {
                // Error, say no to the user
                Toast.makeText(getActivity(), getResources().getString(R.string.no), Toast.LENGTH_LONG).show();
            }



        } else {
            if (FullscreenActivity.file_contents.contains("<slide")) {
                // Remove the 'choose folder' views as it will be saved to the sets folder
                chooseFolder_Spinner.setVisibility(View.GONE);
                chooseFolder_TextView.setVisibility(View.GONE);
                FullscreenActivity.file_type = getResources().getString(R.string.options_set);
                // Change the title
                getDialog().setTitle(getActivity().getResources().getString(R.string.importnewset));
            } else if (FullscreenActivity.file_contents.contains("<lyrics>")) {
                FullscreenActivity.file_type = getResources().getString(R.string.options_song);
            } else {
                FullscreenActivity.file_type = getResources().getString(R.string.file_type_unknown);
            }
        }

        // Now we have the correct name, file type and location
        // Set up the views
        fileTitle_EditText.setText(FullscreenActivity.file_name);
        fileType_TextView.setText(FullscreenActivity.file_type);
        // The song folder
        ListSongFiles.listSongFolders();
        newtempfolders = new ArrayList<>();
        newtempfolders.add(FullscreenActivity.mainfoldername);
        for (int e = 0; e < FullscreenActivity.mSongFolderNames.length; e++) {
            if (FullscreenActivity.mSongFolderNames[e] != null &&
                    !FullscreenActivity.mSongFolderNames[e].equals(FullscreenActivity.mainfoldername) &&
                    !FullscreenActivity.mSongFolderNames[e].equals("(" + FullscreenActivity.mainfoldername + ")")) {
                newtempfolders.add(FullscreenActivity.mSongFolderNames[e]);
            }
        }
        ArrayAdapter<String> folders = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, newtempfolders);
        folders.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chooseFolder_Spinner.setAdapter(folders);

        moveToFolder = newtempfolders.get(0);
        chooseFolder_Spinner.setSelection(0);

        // Listen for changes in the Spinner
        chooseFolder_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    moveToFolder = newtempfolders.get(position);
                } catch (Exception e) {
                    // Can't find folder
                    parent.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}

        });
        importFile_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        importFile_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Now check that the file doesn't already exist.  If it does alert the user to try again
                File testfile;
                if (moveToFolder.equals(getResources().getString(R.string.mainfoldername))) {
                    testfile = new File(FullscreenActivity.dir + "/" + fileTitle_EditText.getText().toString());
                } else {
                    testfile = new File(FullscreenActivity.dir + "/" + moveToFolder + "/" + fileTitle_EditText.getText().toString());
                }
                if (FullscreenActivity.file_type.equals(getResources().getString(R.string.options_set))) {
                    testfile = new File(FullscreenActivity.dirsets + "/" + fileTitle_EditText.getText().toString());
                }

                // Does it exist?
                if (testfile.exists() && !overWrite_CheckBox.isChecked()) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.file_exists), Toast.LENGTH_LONG).show();
                } else {
                    FullscreenActivity.myToastMessage = getResources().getString(R.string.ok);
                    File from = new File(FullscreenActivity.file_location);
                    if (from.renameTo(testfile)) {
                        FullscreenActivity.songfilename = FullscreenActivity.file_name;
                        FullscreenActivity.whichSongFolder = moveToFolder;
                        mListener.refreshAll();
                        dismiss();
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.no), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        // TEST FOR NOW

        return V;
    }
}