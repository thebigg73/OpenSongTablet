package com.garethevans.church.opensongtablet.appdata;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsShareLogsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class ShareLogsFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsShareLogsBinding myView;
    private String log_string="", website_string="";
    private final String writeLog_string="fileWriteActivity.txt", viewLog_string="fileHistory.csv",
            crashLog_string="CrashLog.txt", settings_string="Settings";
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
        changeVisibilities();
    }

    private void changeVisibilities() {
        myView.fileWriteLogLayout.setVisibility(mainActivityInterface.getStorageAccess().getFileWriteLog() ? View.VISIBLE:View.GONE);
        myView.fileViewLayout.setVisibility(mainActivityInterface.getStorageAccess().getFileViewLog() ? View.VISIBLE:View.GONE);
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
        myView.fileWriteReset.setOnClickListener(view -> mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true,writeLog,null,settings_string,"",writeLog_string));
        myView.fileViewReset.setOnClickListener(view -> mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true,viewLog,null,settings_string,"",viewLog_string));
        myView.fileWriteShare.setOnClickListener(view -> shareFile(writeLog_string,"text/plain", writeLog));
        myView.fileViewShare.setOnClickListener(view -> shareFile(viewLog_string,"text/csv", viewLog));
        myView.crashLogReset.setOnClickListener(view -> {
            mainActivityInterface.getStorageAccess().deleteFile(mainActivityInterface.getStorageAccess().getCrashLogUri());
            checkCrashLogExists();
        });
        myView.crashLogShare.setOnClickListener(view -> shareFile(crashLog_string,"text/plain",mainActivityInterface.getStorageAccess().getCrashLogUri()));
    }

    private void checkCrashLogExists() {
        boolean crashLogExists = mainActivityInterface.getStorageAccess().crashLogExists();
        myView.crashLogReset.setEnabled(crashLogExists);
        myView.crashLogShare.setEnabled(crashLogExists);
    }

    private void shareFile(String whichName, String type, Uri whichFile) {
        Intent intent = mainActivityInterface.getExportActions().setShareIntent(whichName,type,whichFile,null);
        intent.putExtra(Intent.EXTRA_SUBJECT, whichName);
        intent.putExtra(Intent.EXTRA_TITLE, whichName);
        if (whichName.equals(crashLog_string)) {
            intent.putExtra(Intent.ACTION_SENDTO,"crashlog@opensongapp.com");
        }
        startActivity(Intent.createChooser(intent, whichName));
    }
}
