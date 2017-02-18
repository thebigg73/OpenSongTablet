package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
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

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PopUpImportExternalFile extends DialogFragment {

    static PopUpImportExternalFile newInstance() {
        PopUpImportExternalFile frag;
        frag = new PopUpImportExternalFile();
        return frag;
    }

    public interface MyInterface {
        void refreshAll();
        void onSongImportDone(String message);
        void backupInstall(String message);
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

    TextView itemTitle_TextView;
    EditText fileTitle_EditText;
    TextView fileType_heading;
    TextView fileType_TextView;
    TextView messageOnSong_TextView;
    TextView messageOpenSong_TextView;
    TextView chooseFolder_TextView;
    Spinner chooseFolder_Spinner;
    Button importFile_Cancel;
    Button importFile_Save;
    CheckBox overWrite_CheckBox;
    ImportOnSongBackup import_os;
    Backup_Install import_osb;

    //static ArrayList<String> newtempfolders;
    String moveToFolder;
    String backupchosen;
    //Backup_Install backup_install;
    FileInputStream inputStream;
    String scheme = "";
    ArrayList<String> backups = new ArrayList<>();
    String message = "";
    View V;

    public void onStart() {
        super.onStart();

        // safety check
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
        getDialog().setTitle(getActivity().getResources().getString(R.string.importnewsong));
        getDialog().setCanceledOnTouchOutside(true);

        V = inflater.inflate(R.layout.popup_importexternalfile, container, false);

        // Initialise the views
        itemTitle_TextView = (TextView) V.findViewById(R.id.itemTitle_TextView);
        fileTitle_EditText = (EditText) V.findViewById(R.id.fileTitle_EditText);
        fileType_heading = (TextView) V.findViewById(R.id.fileType_heading);
        fileType_TextView = (TextView) V.findViewById(R.id.fileType_TextView);
        chooseFolder_TextView = (TextView) V.findViewById(R.id.chooseFolder_TextView);
        messageOnSong_TextView = (TextView) V.findViewById(R.id.messageOnSong_TextView);
        messageOpenSong_TextView = (TextView) V.findViewById(R.id.messageOpenSong_TextView);
        chooseFolder_Spinner = (Spinner) V.findViewById(R.id.chooseFolder_Spinner);
        importFile_Cancel = (Button) V.findViewById(R.id.importFile_Cancel);
        importFile_Save = (Button) V.findViewById(R.id.importFile_Save);
        overWrite_CheckBox = (CheckBox) V.findViewById(R.id.overWrite_CheckBox);

        // By default, we will assume this is a song
        FullscreenActivity.file_type = getResources().getString(R.string.options_song);

        // Set the default button actions.  These might be changed depending on the file type
        importFile_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        importFile_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultSaveAction();
            }
        });

        // Decide if this has been actioned by needtoimport
        switch (FullscreenActivity.whattodo) {
            case "doimport":
                try {
                    scheme = FullscreenActivity.file_uri.getScheme();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                switch (scheme) {
                    case "content":
                        schemeContent();
                        break;

                    case "file":
                        if (!FullscreenActivity.file_name.endsWith(".backup") && !FullscreenActivity.file_name.endsWith(".osb")) {
                            schemeFile();
                        }
                        break;
                    default:
                        dismiss();
                        break;
                }

                if (FullscreenActivity.file_name.endsWith(".ost")) {
                    // This is definitely a song
                    setupOSTImport();

                } else if (FullscreenActivity.file_name.endsWith(".usr") || FullscreenActivity.file_name.endsWith(".USR")) {
                    // This is a song from SongSelect
                    setupUSRImport();

                } else if (FullscreenActivity.file_name.endsWith(".osts")) {
                    // This is definitely a set
                    setupOSTSImport();

                } else if (FullscreenActivity.file_name.endsWith(".osb")) {
                    // This is an OpenSong backup
                    setupOSBImport();

                } else if (FullscreenActivity.file_name.endsWith(".backup")) {
                    // This is definitely an opensong archive
                    setupOSImport();

                } else if (Bible.isYouVersionScripture(FullscreenActivity.incoming_text)) {
                    // It is a scripture, so create the Scripture file
                    // Get the bible translation
                    setupBibleImport();

                } else {
                    // Unknown file
                    setupUnknownImport();
                }

                break;
            case "importos":
                setupOSImport();

                break;
            case "importosb":
                setupOSBImport();
                break;
        }
         FullscreenActivity.whattodo = "";

        return V;
    }

    public void defaultSaveAction() {
        // Now check that the file doesn't already exist.  If it does alert the user to try again
        File testfile;
        if (moveToFolder==null) {
            moveToFolder=getResources().getString(R.string.mainfoldername);
        }

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

            try {
                InputStream in = new FileInputStream(from);
                OutputStream out = new FileOutputStream(testfile);

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
                FullscreenActivity.songfilename = FullscreenActivity.file_name;
                FullscreenActivity.whichSongFolder = moveToFolder;
                mListener.refreshAll();
                dismiss();

            } catch (Exception e) {
                Toast.makeText(getActivity(), getResources().getString(R.string.no), Toast.LENGTH_LONG).show();
            }
        }

    }

    public void setupOSTImport() {
        // OpenSongApp .ost Song file
        FullscreenActivity.file_type = getResources().getString(R.string.options_song);
        FullscreenActivity.file_name = FullscreenActivity.file_name.replace(".ost", "");
        fileTitle_EditText.setText(FullscreenActivity.file_name);
        fileType_TextView.setText(FullscreenActivity.file_type);
        messageOnSong_TextView.setVisibility(View.GONE);
        messageOpenSong_TextView.setVisibility(View.GONE);
        showSongFolders();
    }

    public void setupOSTSImport() {
        // OpenSongApp .osts Set file
        FullscreenActivity.file_type = getResources().getString(R.string.options_set);
        FullscreenActivity.file_name = FullscreenActivity.file_name.replace(".osts", "");
        // Remove the 'choose folder' views as it will be saved to the sets folder
        chooseFolder_Spinner.setVisibility(View.GONE);
        chooseFolder_TextView.setVisibility(View.GONE);
        // Change the title
        getDialog().setTitle(getActivity().getResources().getString(R.string.importnewset));
        fileTitle_EditText.setText(FullscreenActivity.file_name);
        fileType_TextView.setText(FullscreenActivity.file_type);
        messageOnSong_TextView.setVisibility(View.GONE);
        messageOpenSong_TextView.setVisibility(View.GONE);
    }

    public void setupOSBImport() {
        // Copy file the the correct location
        if (FullscreenActivity.whattodo.equals("doimport")) {
            copyFile();
        }
        // Hide the views we don't need
        fileTitle_EditText.setVisibility(View.GONE);
        fileType_heading.setVisibility(View.GONE);
        fileType_TextView.setVisibility(View.GONE);
        messageOnSong_TextView.setVisibility(View.GONE);
        overWrite_CheckBox.setVisibility(View.GONE);

        // Change the views to read what we want
        getDialog().setTitle(getActivity().getResources().getString(R.string.backup_import));
        itemTitle_TextView.setText(getActivity().getResources().getString(R.string.backup_import));
        chooseFolder_TextView.setText(getActivity().getResources().getString(R.string.file_chooser));
        importFile_Save.setText(getActivity().getResources().getString(R.string.ok));

        FullscreenActivity.file_type = getResources().getString(R.string.backup_info);

        // Change the views to be what we want
        if(!showOSBFiles()) {
            // No files exist in the appropriate location
            chooseFolder_TextView.setText(getActivity().getResources().getString(R.string.backup_error));
            chooseFolder_Spinner.setVisibility(View.GONE);
            importFile_Save.setVisibility(View.GONE);
        } else {
            // Set the OK button to import
            importFile_Save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    importOSB();
                }
            });
        }
    }

    public void setupUSRImport() {
        // SongSelect .usr file
        FullscreenActivity.file_type = "USR";
        FullscreenActivity.file_name = FullscreenActivity.file_name.replace("_", " ");
        fileTitle_EditText.setText(FullscreenActivity.file_name);
        fileType_TextView.setText(FullscreenActivity.file_type);
        messageOnSong_TextView.setVisibility(View.GONE);
        messageOpenSong_TextView.setVisibility(View.GONE);
        showSongFolders();
    }

    public void setupOSImport() {
        // Copy file the the correct location
        if (FullscreenActivity.whattodo.equals("doimport")) {
            copyFile();
        }

        // Hide the views we don't need
        fileTitle_EditText.setVisibility(View.GONE);
        fileType_heading.setVisibility(View.GONE);
        fileType_TextView.setVisibility(View.GONE);
        messageOpenSong_TextView.setVisibility(View.GONE);
        overWrite_CheckBox.setVisibility(View.GONE);

        // Change the views to read what we want
        getDialog().setTitle(getActivity().getResources().getString(R.string.import_onsong_choose));
        itemTitle_TextView.setText(getActivity().getResources().getString(R.string.import_onsong_choose));
        chooseFolder_TextView.setText(getActivity().getResources().getString(R.string.file_chooser));
        importFile_Save.setText(getActivity().getResources().getString(R.string.ok));

        FullscreenActivity.file_type = "ONSONGARCHIVE";

        // Change the views to be what we want
        if(!showOSFiles()) {
            // No files exist in the appropriate location
            chooseFolder_TextView.setText(getActivity().getResources().getString(R.string.import_onsong_error));
            chooseFolder_Spinner.setVisibility(View.GONE);
            importFile_Save.setVisibility(View.GONE);
        } else {
            // Set the OK button to import
            importFile_Save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    importOS();
                }
            });
        }
    }

    public void setupBibleImport() {
        // YouVersion Bible import
        messageOnSong_TextView.setVisibility(View.GONE);
        messageOpenSong_TextView.setVisibility(View.GONE);
        Log.d("d","Bible true");
        String translation = FullscreenActivity.scripture_title.substring(FullscreenActivity.scripture_title.lastIndexOf(" "));
        String verses = FullscreenActivity.scripture_title.replace(translation, "");
        // Since the scripture is one big line, split it up a little (50 chars max)
        String[] scripture = FullscreenActivity.scripture.split(" ");
        String scriptureline = "";
        ArrayList<String> scripturearray = new ArrayList<>();

        for (String aScripture : scripture) {
            scriptureline = scriptureline + aScripture;
            if (scriptureline.length() > 50) {
                scripturearray.add(scriptureline);
                scriptureline = "";
            }
        }
        scripturearray.add(scriptureline);

        // Convert the array back into one string separated by new lines
        FullscreenActivity.scripture = "";
        for (int x=0;x<scripturearray.size();x++) {
            FullscreenActivity.scripture = FullscreenActivity.scripture + scripturearray.get(x) + "\n";
        }

        FullscreenActivity.scripture = FullscreenActivity.scripture.trim();

        String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<song>" +
                "  <title>"+verses+"</title>\n" +
                "  <author>"+translation+"</author>\n" +
                "  <user1></user1>\n" +
                "  <user2>false</user2>\n" +
                "  <user3></user3>\n" +
                "  <aka></aka>\n" +
                "  <key_line></key_line>\n" +
                "  <hymn_number></hymn_number>\n" +
                "  <lyrics>"+FullscreenActivity.scripture+"</lyrics>\n" +
                "</song>";

        // Write the file
        String filename = FullscreenActivity.homedir + "/" + "Scriptures/YouVerion";
        File newfile = new File(filename);
        if (!newfile.mkdirs()) {
            Log.d("d","Couldn't make scriptue folder");
        }

        try {
            FileOutputStream overWrite = new FileOutputStream(filename, false);
            overWrite.write(text.getBytes());
            overWrite.flush();
            overWrite.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Alert the user that the Scripture has been written
        FullscreenActivity.myToastMessage = getString(R.string.scripture) + " - " + getString(R.string.ok);
        ShowToast.showToast(getActivity());
        dismiss();
    }

    public void setupUnknownImport() {
        // Not too sure what this file is!
        messageOnSong_TextView.setVisibility(View.GONE);
        messageOpenSong_TextView.setVisibility(View.GONE);
        if (FullscreenActivity.file_contents==null) {
            FullscreenActivity.file_contents=getResources().getString(R.string.hasnotbeenimported);
        }

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

        if (FullscreenActivity.file_contents.equals(getResources().getString(R.string.hasnotbeenimported))) {
            FullscreenActivity.myToastMessage = FullscreenActivity.file_contents;
            FullscreenActivity.file_contents = FullscreenActivity.file_type + " " + FullscreenActivity.file_contents;
            ShowToast.showToast(getActivity());
            dismiss();
        }
        fileTitle_EditText.setText(FullscreenActivity.file_name);
        fileType_TextView.setText(FullscreenActivity.file_type);
        showSongFolders();
    }

    public void schemeContent() {
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

        if (!FullscreenActivity.file_name.endsWith(".backup") && !FullscreenActivity.file_name.endsWith(".osb")) {
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
        }
    }

    public void schemeFile() {
        try {
            //if (FullscreenActivity.file_name.endsWith(".ost") ||
            //        FullscreenActivity.file_name.endsWith(".osts"))
            if (!FullscreenActivity.file_name.endsWith(".backup") &&
                    !FullscreenActivity.file_name.endsWith(".osb") &&
                    !FullscreenActivity.file_name.endsWith(".pdf") &&
                    !FullscreenActivity.file_name.endsWith(".doc") &&
                    !FullscreenActivity.file_name.endsWith(".docx") &&
                    !FullscreenActivity.file_name.endsWith(".jpg") &&
                    !FullscreenActivity.file_name.endsWith(".png") &&
                    !FullscreenActivity.file_name.endsWith(".bmp") &&
                    !FullscreenActivity.file_name.endsWith(".gif") &&
                    !FullscreenActivity.file_name.endsWith(".zip") &&
                    !FullscreenActivity.file_name.endsWith(".sqlite")) {
                inputStream = new FileInputStream(FullscreenActivity.file_location);
                FullscreenActivity.file_contents = LoadXML.readTextFile(inputStream);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void copyFile() {
        // Move the file to the correct location
        moveToFolder = FullscreenActivity.homedir.toString();
        File importIt = new File (FullscreenActivity.file_location);
        File newFile = new File (moveToFolder + "/" + FullscreenActivity.file_name);
        Log.d("d","importIt="+importIt);
        Log.d("d","newFile="+newFile);
        if (importIt.renameTo(newFile)) {
            Log.d("d","Move successful");
        } else {
            //have to copy instead
            try {
                if (!newFile.createNewFile()) {
                    Log.d("d","Error creating file");
                }
                final RandomAccessFile file1 = new RandomAccessFile(importIt, "r");
                final RandomAccessFile file2 = new RandomAccessFile(newFile, "rw");
                file2.getChannel().write(file1.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, importIt.length()));
                file1.close();
                file2.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void showSongFolders() {
        ListSongFiles.getAllSongFolders();
        ArrayAdapter<String> folders = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, FullscreenActivity.mSongFolderNames);
        chooseFolder_Spinner.setAdapter(folders);
        moveToFolder = FullscreenActivity.mSongFolderNames[0];
        chooseFolder_Spinner.setSelection(0);
        chooseFolder_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    //moveToFolder = newtempfolders.get(position);
                    moveToFolder = FullscreenActivity.mSongFolderNames[position];
                } catch (Exception e) {
                    // Can't find folder
                    parent.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}

        });
    }

    public boolean showOSFiles() {
        // Populate the list
        File[] backupfilecheck = FullscreenActivity.homedir.listFiles();
        if (backupfilecheck != null) {
            for (File aBackupfilecheck : backupfilecheck) {
                if (aBackupfilecheck!=null && aBackupfilecheck.isFile() && aBackupfilecheck.getPath().endsWith(".backup")) {
                    backups.add(aBackupfilecheck.getName());
                }
            }
        }

        if (backups.size()>0) {
            ArrayAdapter<String> files = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, backups);
            chooseFolder_Spinner.setAdapter(files);
            chooseFolder_Spinner.setSelection(0);
            backupchosen = backups.get(0);
            chooseFolder_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        backupchosen = backups.get(position);
                    } catch (Exception e) {
                        // Can't find file
                        parent.setSelection(0);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}

            });
            return true;
        } else {
            return false;
        }
    }

    public boolean showOSBFiles() {
        // Populate the list
        File[] backupfilecheck = FullscreenActivity.homedir.listFiles();
        if (backupfilecheck != null) {
            for (File aBackupfilecheck : backupfilecheck) {
                if (aBackupfilecheck!=null && aBackupfilecheck.isFile() && aBackupfilecheck.getPath().endsWith(".osb")) {
                    backups.add(aBackupfilecheck.getName());
                }
            }
        }

        if (backups.size()>0) {
            ArrayAdapter<String> files = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, backups);
            chooseFolder_Spinner.setAdapter(files);
            chooseFolder_Spinner.setSelection(0);
            backupchosen = backups.get(0);
            chooseFolder_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        backupchosen = backups.get(position);
                    } catch (Exception e) {
                        // Can't find file
                        parent.setSelection(0);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}

            });
            return true;
        } else {
            return false;
        }
    }

    public void importOS() {
        // Hide the cancel button
        importFile_Cancel.setVisibility(View.GONE);
        //Change the text of the save button
        importFile_Save.setText(getActivity().getResources().getString(R.string.wait));
        importFile_Save.setClickable(false);

        // Now start the AsyncTask
        import_os = new ImportOnSongBackup();
        try {
            import_os.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Log.d("d","Error importing");
        }
    }
    public class ImportOnSongBackup extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            message = getActivity().getResources().getString(R.string.import_onsong_done);

            if (!FullscreenActivity.dironsong.exists()) {
                // No OnSong folder exists - make it
                StorageChooser.createDirectory(FullscreenActivity.dironsong);
            }

            File dbfile = new File(FullscreenActivity.homedir + "/OnSong.Backup.sqlite3");

            if (dbfile.exists()) {
                if (!dbfile.delete()) {
                    Log.d("d","Error deleting db file");
                }
            }

            InputStream is;
            ZipArchiveInputStream zis;
            String filename;
            try {
                is = new FileInputStream(FullscreenActivity.homedir + "/" + backupchosen);
                Log.d("backup", "is=" + is);
                //final ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream("zip", is);
                //File myfile = new File(FullscreenActivity.homedir + "/" + backupchosen);
                zis = new ZipArchiveInputStream(new BufferedInputStream(is),"UTF-8",false);

                ZipArchiveEntry ze;
                while ((ze = (ZipArchiveEntry) zis.getNextEntry()) != null) {
                    final byte[] buffer = new byte[2048];
                    int count;
                    filename = ze.getName();
                    Log.d("d", "filename=" + filename);

                    FileOutputStream fout;
                    if (filename.equals("OnSong.Backup.sqlite3")) {
                        fout = new FileOutputStream(FullscreenActivity.homedir + "/" + filename);
                    } else {
                        fout = new FileOutputStream(FullscreenActivity.dironsong + "/" + filename);
                    }

                    final BufferedOutputStream out = new BufferedOutputStream(fout);

                    try {
                        while ((count = zis.read(buffer)) != -1) {
                            out.write(buffer, 0, count);
                        }
                        out.flush();
                    } catch (Exception e) {
                        message = getActivity().getResources().getString(R.string.import_onsong_error);
                        e.printStackTrace();
                    } finally {
                        try {
                            fout.getFD().sync();
                            out.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
                //in.close();
                zis.close();

            } catch (Exception e) {
                e.printStackTrace();
                message = getActivity().getResources().getString(R.string.import_onsong_error);
                return message;
            }

            //File dbfile = new File(FullscreenActivity.homedir + "/OnSong.Backup.sqlite3");

            if (dbfile.exists()) {
                SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbfile, null);
                // Go through each row and read in the content field
                // Save the files with the .onsong extension

                String query = "SELECT * FROM Song";

                //Cursor points to a location in your results
                Cursor cursor;

                message = getActivity().getResources().getString(R.string.import_onsong_done);
                String str_title;
                String str_content;

                try {
                    cursor = db.rawQuery(query, null);

                    // Move to first row
                    cursor.moveToFirst();

                    while (cursor.moveToNext()) {
                        // Extract data.
                        str_title = cursor.getString(cursor.getColumnIndex("title"));
                        // Make sure title doesn't have /
                        str_title = str_title.replace("/", "_");
                        str_title = TextUtils.htmlEncode(str_title);
                        str_content = cursor.getString(cursor.getColumnIndex("content"));

                        try {
                            // Now write the modified song
                            FileOutputStream overWrite = new FileOutputStream(FullscreenActivity.dironsong +
                                    "/" + str_title + ".onsong", false);
                            overWrite.write(str_content.getBytes());
                            overWrite.flush();
                            overWrite.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    cursor.close();

                } catch (Exception e) {
                    // Error with sql database
                    e.printStackTrace();
                    message = getActivity().getResources().getString(R.string.import_onsong_error);
                }
            }
            return message;
        }

        @Override
        protected void onPostExecute(String doneit) {
            if (mListener!=null) {
                mListener.onSongImportDone(doneit);
            }
            dismiss();
        }
    }

    public void importOSB() {
        // Hide the cancel button
        importFile_Cancel.setVisibility(View.GONE);
        //Change the text of the save button
        importFile_Save.setText(getActivity().getResources().getString(R.string.wait));
        importFile_Save.setClickable(false);

        // Now start the AsyncTask
        import_osb = new Backup_Install();
        try {
            import_osb.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Log.d("d","Error importing");
        }
    }
    public class Backup_Install extends AsyncTask<String, Void, String> {

        @Override
        public void onPreExecute() {
            FullscreenActivity.myToastMessage = getActivity().getString(R.string.backup_import) +
                    "\n" + getActivity().getString(R.string.wait);
            ShowToast.showToast(getActivity());
            message = getActivity().getResources().getString(R.string.assetcopydone);
        }

        @SuppressWarnings("TryFinallyCanBeTryWithResources")
        @Override
        protected String doInBackground(String... strings) {
            ZipInputStream zis = null;
            try {
                zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(FullscreenActivity.homedir+"/"+backupchosen)));
                ZipEntry ze;
                int count;
                byte[] buffer = new byte[8192];
                while ((ze = zis.getNextEntry()) != null) {
                    File file = new File(FullscreenActivity.dir, ze.getName());
                    File dir = ze.isDirectory() ? file : file.getParentFile();
                    if (!dir.isDirectory() && !dir.mkdirs())
                        throw new FileNotFoundException("Failed to ensure directory: " +
                                dir.getAbsolutePath());
                    if (ze.isDirectory())
                        continue;
                    FileOutputStream fout = new FileOutputStream(file);
                    try {
                        while ((count = zis.read(buffer)) != -1)
                            fout.write(buffer, 0, count);
                    } finally {
                        try {
                            fout.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    long time = ze.getTime();
                    if (time > 0)
                        if (!file.setLastModified(time)) {
                            Log.d("d", "Couldn't get last modified time");
                        }
                }
            } catch (Exception e) {
                e.printStackTrace();
                message = getActivity().getResources().getString(R.string.backup_error);
            } finally {
                if (zis!=null) {
                    try {
                        zis.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        message = getActivity().getString(R.string.backup_error);
                    }
                }
            }
            return message;
        }

        @Override
        public void onPostExecute(String s) {
            if (mListener!=null) {
                mListener.backupInstall(s);
            }
            dismiss();
        }

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (import_os!=null) {
            import_os.cancel(true);
        }
        if (import_osb!=null) {
            import_osb.cancel(true);
        }
        this.dismiss();
    }

}