package com.garethevans.church.opensongtablet.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.InformationBottomSheet;
import com.garethevans.church.opensongtablet.databinding.SettingsDatabaseOptionsBinding;
import com.garethevans.church.opensongtablet.preferences.AreYouSureBottomSheet;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.sqlite.SQLite;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DatabaseUtilitiesFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsDatabaseOptionsBinding myView;
    private String database_management_string="", website_database_utilities="",
            persistent_database_string="", persistent_database_info_string="",
            persistent_database_file_string="", can_be_found_at_string="",
            persistent_database_export_file_string="", persistent_database_backup_file_string="",
            temporary_database_string="", temporary_database_info_string="",
            database_export_info_string="", database_backup_info_string="",
            temporary_database_export_file_string="", error_string="",
            restore_string="", persistent_database_restore_warning_string="",
            persistent_database_invalid_file_string="";
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private Uri importUri;
    private String importFilename;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareStrings();
        mainActivityInterface.updateToolbar(database_management_string);
        mainActivityInterface.updateToolbarHelp(website_database_utilities);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsDatabaseOptionsBinding.inflate(inflater,container,false);

        prepareStrings();

        // Initialise launcher
        initialiseLauncher();

        // Prepare views with extra text
        setupViews();

        // Set up listeners
        setupListeners();

        return myView.getRoot();
    }

    private void initialiseLauncher() {
        // Initialise the launchers
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            mainActivityInterface.setImportUri(null);
            mainActivityInterface.setImportFilename(null);
            if (result.getResultCode() == Activity.RESULT_OK) {
                try {
                    Intent data = result.getData();
                    if (data != null) {
                        mainActivityInterface.setImportUri(data.getData());
                        if (data.getDataString() != null) {
                            mainActivityInterface.setImportFilename(mainActivityInterface.getStorageAccess().
                                    getActualFilename(data.getDataString()));
                        }
                        if (mainActivityInterface.getImportUri()!=null &&
                                mainActivityInterface.getImportFilename()!=null &&
                                mainActivityInterface.getImportFilename().endsWith(".db")) {
                            // Give the user an 'Are you sure?' prompt
                            AreYouSureBottomSheet areYouSureBottomSheet = new AreYouSureBottomSheet(
                                    "restorePersistentDatabase",persistent_database_restore_warning_string,null,
                                    "DataBaseUtilities",this,null);
                            areYouSureBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "AreYouSureBottomSheet");
                        } else {
                            mainActivityInterface.setImportFilename(null);
                            mainActivityInterface.setImportUri(null);
                            mainActivityInterface.getShowToast().doIt(persistent_database_invalid_file_string);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            database_management_string = getString(R.string.database_management);
            website_database_utilities = getString(R.string.website_database_utilities);
            persistent_database_string = getString (R.string.persistent_database);
            persistent_database_info_string = getString(R.string.persistent_database_info);
            persistent_database_file_string = getString(R.string.persistent_database_file);
            persistent_database_export_file_string = getString(R.string.persistent_database_export_file);
            persistent_database_backup_file_string = getString(R.string.persistent_database_backup_file);
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String backupFileName = SQLite.NON_OS_DATABASE_NAME.replace(".db","_backup_"+date+".db");
            persistent_database_backup_file_string = getString(R.string.persistent_database_backup_file) + backupFileName;
            can_be_found_at_string = getString(R.string.can_be_found_at);
            temporary_database_string = getString(R.string.temporary_database);
            temporary_database_info_string = getString(R.string.temporary_database_info);
            database_export_info_string = getString(R.string.database_export_info);
            database_backup_info_string = getString(R.string.database_backup_info);
            temporary_database_export_file_string = getString(R.string.temporary_database_export_file);
            error_string = getString(R.string.error);
            restore_string = getString(R.string.restore);
            persistent_database_restore_warning_string = getString(R.string.persistent_database_restore_warning);
            persistent_database_invalid_file_string = getString(R.string.persistent_database_invalid_file);
        }
    }

    private void setupViews() {
        String text = database_export_info_string + "\n" +
                can_be_found_at_string + ":" + persistent_database_export_file_string;
        myView.exportPersistentDB.setHint(text);

        text = database_backup_info_string + "\n" +
                can_be_found_at_string + ": " + persistent_database_backup_file_string;
        myView.backupDB.setHint(text);

        text = database_export_info_string + "\n" +
                can_be_found_at_string + ": " + temporary_database_export_file_string;
        myView.exportTemporaryDB.setHint(text);
    }

    private void setupListeners() {
        // The help buttons to explain the difference between the persistent and temporary database
        myView.persistentDB.setOnClickListener(view -> {
            InformationBottomSheet informationBottomSheet = new InformationBottomSheet(
                    persistent_database_string,
                    persistent_database_info_string + "\n" +
                    can_be_found_at_string + ": " + persistent_database_file_string,
                    null,null);
            informationBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "InformationBottomSheet");
        });
        myView.temporaryDB.setOnClickListener(view -> {
            InformationBottomSheet informationBottomSheet = new InformationBottomSheet(
                    temporary_database_string,
                    temporary_database_info_string,
                    null,null);
            informationBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "InformationBottomSheet");
        });
        myView.exportPersistentDB.setOnClickListener(v -> {
            showProgressBar(true);
            // Do this on a background thread
            mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                try {
                    mainActivityInterface.getNonOpenSongSQLiteHelper().exportDatabase();
                } catch (Exception e) {
                    mainActivityInterface.getStorageAccess().updateCrashLog(e.toString());
                    mainActivityInterface.getShowToast().doIt(error_string);
                }
                showProgressBar(false);
            });
        });
        myView.exportTemporaryDB.setOnClickListener(v -> {
            showProgressBar(true);
            // Do this on a background thread
            mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                try {
                    mainActivityInterface.getSQLiteHelper().exportDatabase();
                } catch (Exception e) {
                    mainActivityInterface.getStorageAccess().updateCrashLog(e.toString());
                    mainActivityInterface.getShowToast().doIt(error_string);
                }
                showProgressBar(false);
            });
        });
        myView.backupDB.setOnClickListener(v -> {
            showProgressBar(true);
            // Do this on a background thread
            mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                // Copy the current appDB (not the userDB) file)
                // Then prompt the user with an option to quick share the file
                mainActivityInterface.getNonOpenSongSQLiteHelper().backupPersistentDatabase();
                showProgressBar(false);
            });
        });
        myView.restoreDB.setOnClickListener(v -> {
            // Give the user the chance to pick a database file to import
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("application/octet-stream");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"application/octet-stream","application/vnd.sqlite3","application/x-sqlite3"});
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI,
                        mainActivityInterface.getStorageAccess().getUriForItem("Backups","",""));
            }
            intent.addFlags(mainActivityInterface.getStorageAccess().getAddReadUriFlags());
            activityResultLauncher.launch(intent);
        });
        myView.cleanDB.setOnClickListener(view -> {
            showProgressBar(true);
            // Do this in a background thread
            mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                try {
                    mainActivityInterface.getNonOpenSongSQLiteHelper().cleanDatabase(this);
                } catch (Exception e) {
                    e.printStackTrace();
                    mainActivityInterface.getStorageAccess().updateCrashLog(e.toString());
                }
                showProgressBar(false);
            });
        });
    }

    private void showProgressBar(boolean show) {
        mainActivityInterface.getMainHandler().post(() -> {
            if (myView!=null) {
                myView.progress.setVisibility(show ? View.VISIBLE : View.GONE);
                myView.scrimOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    public void doImportDatabaseBackup() {
        // Now that the user has confirmed (are you sure?)
        // (returned from MainActivity confirmedAction)
        // We can proceed to import on a background thread
        showProgressBar(true);
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            try {
                mainActivityInterface.getNonOpenSongSQLiteHelper().importDatabaseBackup();
            } catch (Exception e) {
                mainActivityInterface.getStorageAccess().updateCrashLog(e.toString());
                mainActivityInterface.getShowToast().doIt(error_string);
            }
            showProgressBar(false);
        });
    }

    public void showCleanDatabaseResults(ArrayList<Song> uselessSongs, ArrayList<Song> usefulSongs) {
        // This is called after the database has been queried and got its results
        // Display the information in a bottom sheet
        showProgressBar(false);
        CleanDatabaseBottomSheet cleanDatabaseBottomSheet = new CleanDatabaseBottomSheet(uselessSongs,usefulSongs);
        cleanDatabaseBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"CleanDatabaseBottomSheet");
    }
}
