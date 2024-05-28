package com.garethevans.church.opensongtablet.importsongs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsImportBulkBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class ImportBulkFragment extends Fragment {

    // This allows users to specify multiple song files in supported formats (unsupported are ignored)
    // Each text based song (txt, chopro, onsong) song will be converted to OpenSong
    // Songs with xml extension or no extension will be checked for OpenSong formatting
    // PDF files will stay as they are
    // These files will then be saved into an 'Imported' folder and removed from the import folder
    // Users can choose the location of their 'import from' folder

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "ImportBulkFragment";
    private MainActivityInterface mainActivityInterface;
    private SettingsImportBulkBinding myView;
    private String imported_string="", success_string="", error_string="", unknown_string="",
            text_string="", chordpro_string="", onsong_string="", image_or_pdf_string="",
            web_string="", import_bulk_string="", showcase_choose="", showcase_found="",
            showcase_import="", ok_string="", word_string="";
    private ArrayList<Uri> uris;
    private ArrayList<String> foundItems;
    private ActivityResultLauncher<Intent> multiSelect;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsImportBulkBinding.inflate(inflater, container, false);
        prepareLauncher();
        return myView.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareStrings();
        prepareViews();
        prepareListeners();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            imported_string = getString(R.string.imported);
            success_string = getString(R.string.success);
            error_string = getString(R.string.error);
            text_string = getString(R.string.text);
            chordpro_string = getString(R.string.chordpro);
            onsong_string = getString(R.string.onsong);
            image_or_pdf_string = getString(R.string.image) + " / " + getString(R.string.pdf);
            unknown_string = getString(R.string.unknown);
            web_string = getString(R.string.website_import_bulk);
            import_bulk_string = getString(R.string.import_bulk);
            showcase_choose = getString(R.string.import_bulk_showcase_choose);
            showcase_found = getString(R.string.import_bulk_showcase_found);
            showcase_import = getString(R.string.import_bulk_showcase_import);
            ok_string = getString(R.string.okay);
            word_string = getString(R.string.word);
        }
    }

    private void prepareLauncher() {
        multiSelect = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        // This is only called when using lollipop or later
                        lollipopDealWithUri(result.getData());
                    }
                });

    }

    private void prepareViews() {
        myView.progressLayout.setVisibility(View.GONE);
        myView.doImport.setVisibility(View.GONE);
        myView.doImport.hide();
        mainActivityInterface.updateToolbar(import_bulk_string);
        mainActivityInterface.updateToolbarHelp(web_string);
        mainActivityInterface.getShowCase().singleShowCase(getActivity(),myView.fileChooser,ok_string,showcase_choose,true,"import_bulk_choose");
    }

    private void prepareListeners() {
        myView.doImport.setOnClickListener(view -> importSongs());
        myView.fileChooser.setOnClickListener(view -> {
            Uri startUri = mainActivityInterface.getStorageAccess().getUriForItem("Import","","");
            Intent intent;
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
            intent.putExtra("android.provider.extra.INITIAL_URI", startUri);
            intent.setType("*/*");
            multiSelect.launch(intent);
        });
    }


    private void lollipopDealWithUri(Intent resultData) {
        uris = new ArrayList<>();
        foundItems = new ArrayList<>();
        if (resultData != null) {
            // Check for multiple selection or not
            if (resultData.getClipData() != null) {
                for (int i = 0; i < resultData.getClipData().getItemCount(); i++) {
                    Uri thisUri = resultData.getClipData().getItemAt(i).getUri();
                    uris.add(thisUri);
                    foundItems.add(mainActivityInterface.getStorageAccess().getActualFilename(thisUri.toString()));
                }
            } else {
                Uri thisUri = resultData.getData();
                uris.add(thisUri);
                foundItems.add(mainActivityInterface.getStorageAccess().getActualFilename(thisUri.toString()));
            }
        }
        // Display list of songs
        displaySongList();
    }


    private void displaySongList() {
        myView.filesFound.setText("");
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            if (foundItems.size() > 0) {
                StringBuilder stringBuilder = new StringBuilder();
                for (String item : foundItems) {
                    stringBuilder.append(item).append("\n");
                }
                mainActivityInterface.getMainHandler().post(() -> {
                    if (myView != null) {
                        myView.filesFound.setText(stringBuilder.toString());
                        myView.doImport.show();
                        ArrayList<View> targets = new ArrayList<>();
                        ArrayList<String> infos = new ArrayList<>();
                        ArrayList<Boolean> rects = new ArrayList<>();
                        targets.add(myView.filesFound);
                        targets.add(myView.doImport);
                        infos.add(showcase_found);
                        infos.add(showcase_import);
                        rects.add(true);
                        rects.add(true);
                        mainActivityInterface.getShowCase().sequenceShowCase(getActivity(),
                                targets,null,infos,rects,"import_bulk");
                    }
                });
            } else {
                myView.doImport.hide();
            }
        });
    }

    private void importSongs() {
        // Show the progressView
        myView.progressLayout.setVisibility(View.VISIBLE);

        // Do the rest in a new thread
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Create the Import songs folder if it doesn't exist
            mainActivityInterface.getStorageAccess().createFolder("Songs", "", imported_string, false);

            int total = uris.size();
            // Go through each song at a time, check the type and act accordingly
            boolean gotFirstCorrectSong = false;
            for (int x=0; x<total; x++) {
                String filename = foundItems.get(x);
                Uri fileUri = uris.get(x);
                updateProgress(x,total,filename, "");
                String content = "";
                InputStream inputStream;
                Song newSong = new Song();
                newSong.setFolder(imported_string);

                boolean text = mainActivityInterface.getStorageAccess().isSpecificFileExtension("text",filename);
                boolean chordpro = mainActivityInterface.getStorageAccess().isSpecificFileExtension("chordpro",filename);
                boolean imageorpdf = mainActivityInterface.getStorageAccess().isSpecificFileExtension("imageorpdf",filename);
                boolean onsong = mainActivityInterface.getStorageAccess().isSpecificFileExtension("onsong",filename);
                boolean word = mainActivityInterface.getStorageAccess().isSpecificFileExtension("docx",filename);

                boolean goodsong = true;
                String newFilename = null;
                if (filename.contains(".")) {
                    newFilename = filename.substring(0,filename.lastIndexOf("."));
                }

                if (text || chordpro || onsong) {
                    inputStream = mainActivityInterface.getStorageAccess().getInputStream(fileUri);
                    content = mainActivityInterface.getStorageAccess().readTextFileToString(inputStream);
                    newSong.setFiletype("XML");
                }

                if (text) {
                    updateProgress(x,total,filename, text_string);
                    newSong.setLyrics(mainActivityInterface.getConvertTextSong().convertText(content));
                    newSong.setFilename(newFilename);
                } else if (chordpro) {
                    updateProgress(x,total,filename, chordpro_string);
                    newSong = mainActivityInterface.getConvertChoPro().convertChoProToOpenSong(newSong,content);
                    newSong.setFilename(newFilename);
                } else if (onsong) {
                    updateProgress(x,total,filename, onsong_string);
                    newSong = mainActivityInterface.getConvertOnSong().convertOnSongToOpenSong(newSong,content);
                    newSong.setFilename(newFilename);
                } else if (imageorpdf) {
                    updateProgress(x, total, filename, image_or_pdf_string);
                    newFilename = filename;
                    newSong.setFilename(filename);
                    if (filename.toLowerCase().endsWith(".pdf")) {
                        newSong.setFiletype("PDF");
                    } else {
                        newSong.setFiletype("IMG");
                    }
                } else if (word) {
                    updateProgress(x, total, filename, word_string);
                    newFilename = filename.substring(0,filename.lastIndexOf("."));
                    newSong.setFilename(newFilename);
                    newSong.setFiletype("XML");
                    content = mainActivityInterface.getConvertWord().convertDocxToText(fileUri,filename);
                    newSong.setLyrics(content);
                    newSong.setTitle(newFilename);

                } else {
                    updateProgress(x,total,filename, unknown_string);
                    goodsong = false;
                }

                if (newFilename==null) {
                    newFilename = filename;
                }

                if (goodsong) {
                    if (content != null) {
                        if (newSong.getTitle() == null || newSong.getTitle().isEmpty()) {
                            newSong.setTitle(newFilename);
                        }
                    }

                    // Create the new uri/file for writing
                    Uri newUri = mainActivityInterface.getStorageAccess().getUriForItem("Songs", imported_string, newFilename);
                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, newUri, null, "Songs", imported_string, newFilename);

                    boolean success = false;
                    if (imageorpdf) {
                        // Copy the original file
                        success = mainActivityInterface.getStorageAccess().copyUriToUri(fileUri, newUri);
                        // Add the song to the database
                        if (success) {
                            mainActivityInterface.getNonOpenSongSQLiteHelper().createSong(imported_string, newFilename);
                            //mainActivityInterface.getSQLiteHelper().createSong(imported_string, newFilename);
                            mainActivityInterface.getSQLiteHelper().updateSong(newSong);
                            mainActivityInterface.getNonOpenSongSQLiteHelper().updateSong(newSong);
                        }
                    } else if (text || chordpro || onsong || word) {
                        String songXML = mainActivityInterface.getProcessSong().getXML(newSong);
                        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(newUri);
                        success = mainActivityInterface.getStorageAccess().writeFileFromString(songXML,outputStream);
                        if (success) {
                            // Add the song to the database
                            mainActivityInterface.getSQLiteHelper().createSong(imported_string, newFilename);
                            mainActivityInterface.getSQLiteHelper().updateSong(newSong);
                        }
                    }

                    // If successful, say so!
                    if (success) {
                        updateProgress(x+1, total, filename, success_string);
                        // For the firt good song, we will use this song as the song to display
                        if (!gotFirstCorrectSong) {
                            gotFirstCorrectSong = true;
                            mainActivityInterface.setSong(newSong);
                            mainActivityInterface.getPreferences().setMyPreferenceString("songFilename",newSong.getFilename());
                            mainActivityInterface.getPreferences().setMyPreferenceString("songFolder",newSong.getFolder());
                        }
                    } else {
                        updateProgress(x+1, total, filename, error_string);
                    }
                }
            }

            mainActivityInterface.getMainHandler().post(() -> {
                if (myView!=null) {
                    myView.progressLayout.setVisibility(View.GONE);
                    myView.filesFound.setVisibility(View.GONE);
                    mainActivityInterface.updateSongList();
                    mainActivityInterface.getShowToast().doIt(success_string);
                    mainActivityInterface.navHome();
                }
            });
        });
    }

    private void updateProgress(int item, int total, String filename, String progress) {
        if (myView!=null) {
            String message = item + "/" + total + ". " + filename + ": " + progress;
            mainActivityInterface.getMainHandler().post(() -> {
                if (myView != null) {
                    myView.progressText.setText(message);
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myView = null;
    }
}
