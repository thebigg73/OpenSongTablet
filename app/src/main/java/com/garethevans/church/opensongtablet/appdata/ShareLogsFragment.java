package com.garethevans.church.opensongtablet.appdata;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.icu.util.Output;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsShareLogsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.sqlite.SQLite;
import com.garethevans.church.opensongtablet.sqlite.SongsDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class ShareLogsFragment extends Fragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "ShareLogsFragment";
    private MainActivityInterface mainActivityInterface;
    private SettingsShareLogsBinding myView;
    private String log_string="", website_string="";
    private final String writeLog_string="fileWriteActivity.txt", viewLog_string="fileHistory.csv",
            crashLog_string="CrashLog.txt", settings_string="Settings",song_list_string="songIndexLog.txt";
    private Uri writeLog, viewLog;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareStrings();
        mainActivityInterface.updateToolbar(log_string);
        mainActivityInterface.updateToolbarHelp(website_string);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsShareLogsBinding.inflate(inflater,container,false);

        myView.getRoot().setBackgroundColor(mainActivityInterface.getPalette().background);

        prepareStrings();

        writeLog = mainActivityInterface.getStorageAccess().getUriForItem(settings_string, "", writeLog_string);
        viewLog = mainActivityInterface.getStorageAccess().getUriForItem(settings_string, "", viewLog_string);

        prepareViews();
        setupListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            log_string = getString(R.string.log);
            website_string = getString(R.string.website_logs);
        }
    }

    private void prepareViews() {
        myView.fileWriteLog.setChecked(mainActivityInterface.getStorageAccess().getFileWriteLog());
        myView.fileViewLog.setChecked(mainActivityInterface.getStorageAccess().getFileViewLog());
        myView.songIndexDebug.setChecked(mainActivityInterface.getSongListBuildIndex().getLogIndexing());
        changeVisibilities();
    }

    private void changeVisibilities() {
        myView.fileWriteLogLayout.setVisibility(mainActivityInterface.getStorageAccess().getFileWriteLog() ? View.VISIBLE:View.GONE);
        myView.fileViewLayout.setVisibility(mainActivityInterface.getStorageAccess().getFileViewLog() ? View.VISIBLE:View.GONE);
        myView.songIndexDebugLayout.setVisibility(mainActivityInterface.getSongListBuildIndex().getLogIndexing() ? View.VISIBLE:View.GONE);
        checkCrashLogExists();
    }

    private void setupListeners() {
        myView.fileWriteLog.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getStorageAccess().setFileWriteLog(b);
            changeVisibilities();
        });
        myView.fileViewLog.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getStorageAccess().setFileViewLog(b);
            changeVisibilities();
        });
        myView.songIndexDebug.setOnCheckedChangeListener(((compoundButton, b) -> {
            mainActivityInterface.getSongListBuildIndex().setLogIndexing(b);
            if (!b) {
                mainActivityInterface.getStorageAccess().doDeleteFile("Settings","",song_list_string);
            } else {
                mainActivityInterface.getStorageAccess().writeFileFromString("Settings","",song_list_string,"",false);
            }
            changeVisibilities();
        }));
        myView.fileWriteReset.setOnClickListener(view -> mainActivityInterface.getStorageAccess().writeFileFromString(settings_string,"",writeLog_string,"",false));
        myView.fileViewReset.setOnClickListener(view -> mainActivityInterface.getStorageAccess().writeFileFromString(settings_string,"",viewLog_string,"", false));
        myView.songIndexDebugReset.setOnClickListener(view -> mainActivityInterface.getStorageAccess().writeFileFromString(settings_string,"",song_list_string,"",false));
        myView.fileWriteShare.setOnClickListener(view -> shareFile(writeLog_string,"text/plain", writeLog));
        myView.fileViewShare.setOnClickListener(view -> shareFile(viewLog_string,"text/csv", viewLog));
        myView.songIndexDebugShare.setOnClickListener(view -> shareSongsDbFiles());
        myView.crashLogReset.setOnClickListener(view -> {
            mainActivityInterface.getStorageAccess().deleteFile(mainActivityInterface.getStorageAccess().getCrashLogUri());
            checkCrashLogExists();
        });
        myView.crashLogShare.setOnClickListener(view -> shareFile(crashLog_string,"text/plain",mainActivityInterface.getStorageAccess().getCrashLogUri()));
    }

    private void checkCrashLogExists() {
        boolean crashLogExists = mainActivityInterface.getStorageAccess().crashLogExists();
        myView.crashLogReset.setEnabled(crashLogExists);
        myView.crashLogReset.setAlpha(crashLogExists ? 1f:0.5f);
        myView.crashLogShare.setEnabled(crashLogExists);
        myView.crashLogShare.setAlpha(crashLogExists ? 1f:0.5f);

    }

    private void shareFile(String whichName, String type, Uri whichFile) {
        Intent intent = mainActivityInterface.getExportActions().setShareIntent(whichName,type,whichFile,null);
        intent.putExtra(Intent.EXTRA_SUBJECT, whichName);
        intent.putExtra(Intent.EXTRA_TITLE, whichName);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"crashlog@opensongapp.com"});

        intent.putExtra(Intent.ACTION_SENDTO, "crashlog@opensongapp.com");
        Log.d(TAG,"whichName:"+whichName+"  crashLog_string:"+crashLog_string);
        if (whichName.equals(crashLog_string)) {
            intent.putExtra(Intent.ACTION_SENDTO,"crashlog@opensongapp.com");
        }
        startActivity(Intent.createChooser(intent, whichName));
    }
    private void shareSongsDbFiles() {
        Context context = getContext();
        if (context != null) {
            ArrayList<Uri> uris = new ArrayList<>();

            // 1. Ensure database is written and checkpointed
            try (Cursor cursor = SongsDatabase.getInstance(context).getWritableDatabase().rawQuery("PRAGMA wal_checkpoint(FULL);", null)) {
                if (cursor != null) {
                    cursor.moveToFirst();
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to execute wal_checkpoint", e);
            }
            File sourceDb = context.getDatabasePath(SQLite.DATABASE_NAME);
            //File shareableDb = new File(context.getFilesDir(), SQLite.DATABASE_NAME);
            // Make this a txt extension so it isn't removed from the share app
            File shareableDb = new File(context.getFilesDir(), "Songs.txt");

            if (sourceDb.exists()) {
                try (FileInputStream fis = new FileInputStream(sourceDb);
                     FileOutputStream fos = new FileOutputStream(shareableDb);
                     FileChannel src = fis.getChannel();
                     FileChannel dst = fos.getChannel()) {

                    dst.transferFrom(src, 0, src.size());
                } catch (IOException e) {
                    Log.e(TAG, "Failed to copy database for sharing", e);
                }
            } else {
                Log.w(TAG, "Source database file does not exist at: " + sourceDb.getAbsolutePath());
            }

            // Now generate the URI using the files-path root
            Uri dbUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", shareableDb);
            uris.add(dbUri);

            Log.d(TAG, "dbUri:" + dbUri);

            // 2. Locate and get the URI for the file in the OpenSong/Settings/ folder
            Uri settingsFileUri = mainActivityInterface.getStorageAccess().getUriForItem("Settings", "", song_list_string);
            if (settingsFileUri != null && mainActivityInterface.getStorageAccess().uriExists(settingsFileUri)) {
                uris.add(settingsFileUri);
            }

            // 3. Create the multiple-file share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.setType("*/*");
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

            // CRUCIAL: Set ClipData so target apps get permission for all URIs in the array
            if (!uris.isEmpty()) {
                ClipData clipData = ClipData.newUri(context.getContentResolver(), "SongIndex Logs", uris.get(0));
                for (int i = 1; i < uris.size(); i++) {
                    clipData.addItem(new ClipData.Item(uris.get(i)));
                }
                shareIntent.setClipData(clipData);
            }

            String whichName = "Song Index Debug";
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, whichName);
            shareIntent.putExtra(Intent.EXTRA_TITLE, whichName);
            shareIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"songindexdebug@opensongapp.com"});

            // Grant temporary read permissions
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(shareIntent, "SongIndex Logs"));
        }
    }
}
